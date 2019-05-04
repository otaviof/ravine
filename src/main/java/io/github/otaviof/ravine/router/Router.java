package io.github.otaviof.ravine.router;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.kafka.AvroProducerException;
import io.github.otaviof.ravine.kafka.ConsumerGroup;
import io.github.otaviof.ravine.kafka.ProducerGroup;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Router instance is responsible by handling sub-path events, and route requests on Kafka. It represents
 * component that holds two actors, producer and consumer instances.
 */
@Component
@Slf4j
public class Router {
    private final Config config;
    private final SpecificEventListener specificEventListener;
    private final ProducerGroup producerGroup;

    /**
     * Handle a given route, by producing the payload on Kafka, given it's a valid Avro payload, and
     * wait for message to arrive on output topic, or timeout.
     *
     * @param path request path;
     * @param body request body;
     * @return String representation of output event;
     * @throws AvroProducerException when not able to produce on Kafka;
     */
    public String route(String path, byte[] body) throws AvroProducerException {
        var routeConfig = config.getRouteByPath(path);

        log.info("Routing request for path '{}' ({} bytes)", path, body.length);

        if (path == null || path.isEmpty() || routeConfig == null) {
            log.warn("Path '{}' is not found!", path);
            throw new RouteNotFoundException(
                    String.format("route for path '%s' is not found", path));
        }

        var uuid = UUID.randomUUID().toString();
        var timeout = routeConfig.getResponse().getTimeoutMs();

        producerGroup.send(path, uuid, body);

        try {
            log.info("Waiting for maximum of '{} ms' for UUID '{}' to come back...", timeout, uuid);
            await().atMost(timeout, TimeUnit.MILLISECONDS)
                    .until(() -> specificEventListener.inCache(uuid));
        } catch (ConditionTimeoutException e) {
            log.error("Timeout on calling out path '{}' after '{}' ms.", path, timeout);
            throw new RouteTimeoutException(e.getMessage());
        } finally {
            specificEventListener.expireOlderThan(80000);
        }

        return specificEventListener.getEvent(uuid).getV().toString();
    }

    public Router(
            Config config,
            SpecificEventListener specificEventListener,
            ConsumerGroup consumerGroup,
            ProducerGroup producerGroup
    ) {
        this.config = config;
        this.specificEventListener = specificEventListener;

        consumerGroup.bootstrap();
        consumerGroup.waitForConsumers();

        this.producerGroup = producerGroup;
    }
}
