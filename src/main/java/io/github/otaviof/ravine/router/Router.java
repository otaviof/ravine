package io.github.otaviof.ravine.router;

import static org.awaitility.Awaitility.await;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.config.ResponseConfig;
import io.github.otaviof.ravine.config.RouteConfig;
import io.github.otaviof.ravine.kafka.AvroProducerException;
import io.github.otaviof.ravine.kafka.ConsumerGroup;
import io.github.otaviof.ravine.kafka.ProducerGroup;
import io.github.otaviof.ravine.kafka.ProducerGroupAvroConversionException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;

/**
 * Router instance is responsible by handling sub-path events, and route requests on Kafka. It
 * represents component that holds two actors, producer and consumer instances.
 */
@Component
@Slf4j
public class Router {
    private final Config config;
    private final EventListener listener;
    private final ProducerGroup producerGroup;

    public Router(
            Config config,
            EventListener listener,
            ConsumerGroup consumerGroup,
            ProducerGroup producerGroup) {
        this.config = config;
        this.listener = listener;

        consumerGroup.bootstrap();
        consumerGroup.waitForConsumers();

        this.producerGroup = producerGroup;
    }

    /**
     * Handle a given route, by producing the payload on Kafka, given it's a valid Avro payload, and
     * wait for message to arrive on output topic, or timeout.
     *
     * @param request ravine request;
     * @return RouteConfig with event content and status-code;
     * @throws RouterRouteNotFoundException on not being able to route based on path;
     * @throws RouterRouteMethodNotAllowedException http request method is not configured on path;
     * @throws AvroProducerException error on producing a message;
     * @throws RouterRouteTimeoutException timeout on waiting for response;
     */
    public Response route(Request request) throws
            RouterRouteNotFoundException,
            RouterRouteMethodNotAllowedException,
            AvroProducerException,
            RouterRouteTimeoutException,
            ProducerGroupAvroConversionException {
        var routeConfig = prepare(request.getMethod(), request.getPath());
        var responseConfig = routeConfig.getEndpoint().getResponse() != null ?
                routeConfig.getEndpoint().getResponse() : new ResponseConfig();
        var uuid = UUID.randomUUID().toString();

        log.info("Routing request '{}' for path '{}' ({} bytes)",
                request.getMethod(), request.getPath(), request.getBody().length);

        producerGroup.send(request.getPath(), uuid, request.getBody(), request.getHeaders());

        if (routeConfig.getResponse() == null) {
            log.info("Empty response topic, therefore just dispatching event.");
            return new Response(responseConfig);
        }

        return new Response(
                responseConfig.getHttpCode(),
                responseConfig.getContentType(),
                waitForResponse(request.getPath(), uuid, routeConfig.getResponse().getTimeoutMs()));
    }

    /**
     * Waiting for a response event to arrive within timeout.
     *
     * @param path request path;
     * @param uuid event key;
     * @param timeout ms to wait;
     * @return String with event content;
     * @throws RouterRouteTimeoutException on timeout;
     */
    private String waitForResponse(String path, String uuid, int timeout) throws
            RouterRouteTimeoutException {
        log.info("Waiting for '{}' ms for UUID '{}' to come back...", timeout, uuid);

        try {
            await().atMost(timeout, TimeUnit.MILLISECONDS).until(() -> listener.inCache(uuid));
            return listener.getEvent(uuid).getV().toString();
        } catch (ConditionTimeoutException e) {
            var msg = String
                    .format("No response-event after '%d' ms for path '%s'.", timeout, path);
            log.error(msg);
            throw new RouterRouteTimeoutException(msg);
        }
    }

    /**
     * Load configuration for route, and check if method in use is allowed.
     *
     * @param method http request method;
     * @param path route path;
     * @return RouteConfig for path;
     * @throws RouterRouteNotFoundException on not being able to route based on path;
     * @throws RouterRouteMethodNotAllowedException when not part of route config;
     */
    private RouteConfig prepare(String method, String path)
            throws RouterRouteMethodNotAllowedException, RouterRouteNotFoundException {
        var routeConfig = config.getRouteByPath(path);

        if (path == null || path.isEmpty() || routeConfig == null) {
            log.warn("Path '{}' is not found!", path);
            throw new RouterRouteNotFoundException(
                    String.format("route for path '%s' is not found", path));
        }

        var allowedMethods = routeConfig.getEndpoint().getMethods().stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());

        if (!allowedMethods.contains(method.toLowerCase())) {
            throw new RouterRouteMethodNotAllowedException(
                    String.format("method '%s' is not allowed on path '%s'", method, path));
        }

        return routeConfig;
    }
}
