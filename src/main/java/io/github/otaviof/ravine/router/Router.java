package io.github.otaviof.ravine.router;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.config.ResponseConfig;
import io.github.otaviof.ravine.config.RouteConfig;
import io.github.otaviof.ravine.errors.AvroProducerException;
import io.github.otaviof.ravine.errors.MethodNotAllowedOnPathException;
import io.github.otaviof.ravine.errors.ProducerErrorException;
import io.github.otaviof.ravine.errors.RouteNotFoundException;
import io.github.otaviof.ravine.errors.RouteTimeoutException;
import io.github.otaviof.ravine.kafka.ConsumerGroup;
import io.github.otaviof.ravine.kafka.ProducerGroup;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

/**
 * Router instance is responsible by handling sub-path events, and route requests on Kafka. It represents
 * component that holds two actors, producer and consumer instances.
 */
@Component
@Slf4j
public class Router {
    private final Config config;
    private final SpecificEventListener listener;
    private final ProducerGroup producerGroup;

    /**
     * Handle a given route, by producing the payload on Kafka, given it's a valid Avro payload, and
     * wait for message to arrive on output topic, or timeout.
     *
     * @param method request method;
     * @param path request path;
     * @param body request body;
     * @return RouteConfig with event content and status-code;
     * @throws RouteNotFoundException on not being able to route based on path;
     * @throws MethodNotAllowedOnPathException http request method is not configured on path;
     * @throws ProducerErrorException error on producing a message;
     * @throws RouteTimeoutException timeout on waiting for response;
     */
    public RoutingResult route(String method, String path, byte[] body) throws
            RouteNotFoundException,
            MethodNotAllowedOnPathException,
            ProducerErrorException,
            RouteTimeoutException {
        log.info("Routing request '{}' for path '{}' ({} bytes)", method, path, body.length);

        var routeConfig = prepare(method, path);
        var response = routeConfig.getEndpoint().getResponse();
        var uuid = UUID.randomUUID().toString();

        try {
            producerGroup.send(path, uuid, body);
        } catch (AvroProducerException e) {
            throw new ProducerErrorException(e.getMessage());
        }

        if (routeConfig.getResponse() == null) {
            log.info("Empty response topic, therefore just dispatching event.");
            return new RoutingResult(response.getHttpCode(), response.getContentType(), response.getBody());
        }

        if (response == null) {
            response = new ResponseConfig();
        }

        return new RoutingResult(response.getHttpCode(),
                response.getContentType(),
                waitForResponse(path, uuid, routeConfig.getResponse().getTimeoutMs()));
    }

    /**
     * Waiting for a response event to arrive within timeout.
     *
     * @param path    request path;
     * @param uuid    event key;
     * @param timeout ms to wait;
     * @return String with event content;
     * @throws RouteTimeoutException on timeout;
     */
    private String waitForResponse(String path, String uuid, int timeout) throws RouteTimeoutException {
        log.info("Waiting for '{}' ms for UUID '{}' to come back...", timeout, uuid);

        try {
            await().atMost(timeout, TimeUnit.MILLISECONDS)
                    .until(() -> listener.inCache(uuid));
            return listener.getEvent(uuid).getV().toString();
        } catch (ConditionTimeoutException e) {
            var msg = String.format("No response-event after '%d' ms for path '%s'.", timeout, path);
            log.error(msg);
            throw new RouteTimeoutException(msg);
        } finally {
            listener.expireOlderThan(80000);
        }
    }

    /**
     * Load configuration for route, and check if method in use is allowed.
     *
     * @param method http request method;
     * @param path   route path;
     * @return RouteConfig for path;
     * @throws RouteNotFoundException          on not being able to route based on path;
     * @throws MethodNotAllowedOnPathException when not part of route config;
     */
    private RouteConfig prepare(String method, String path) throws
            MethodNotAllowedOnPathException,
            RouteNotFoundException {
        var routeConfig = config.getRouteByPath(path);

        if (path == null || path.isEmpty() || routeConfig == null) {
            log.warn("Path '{}' is not found!", path);
            throw new RouteNotFoundException(String.format("route for path '%s' is not found", path));
        }

        var allowedMethods = routeConfig.getEndpoint().getMethods().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (!allowedMethods.contains(method.toLowerCase())) {
            throw new MethodNotAllowedOnPathException(
                    String.format("method '%s' is not allowed on path '%s'", method, path));
        }

        return routeConfig;
    }

    public Router(
            Config config,
            SpecificEventListener listener,
            ConsumerGroup consumerGroup,
            ProducerGroup producerGroup
    ) {
        this.config = config;
        this.listener = listener;

        consumerGroup.bootstrap();
        consumerGroup.waitForConsumers();

        this.producerGroup = producerGroup;
    }
}
