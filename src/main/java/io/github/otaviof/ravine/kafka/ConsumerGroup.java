package io.github.otaviof.ravine.kafka;

import static org.awaitility.Awaitility.await;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.config.RouteConfig;
import io.opentracing.Tracer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * Represents all consumers in this application, where messages received by them are funneled into
 * ApplicationEvents mechanism.
 */
@Component
@Slf4j
public class ConsumerGroup implements ApplicationEventPublisherAware {
    private final Tracer tracer;
    private final Config config;
    private final Map<AvroConsumer, Thread> consumerThreads;
    private ApplicationEventPublisher eventPublisher;

    public ConsumerGroup(Tracer tracer, Config config) {
        this.tracer = tracer;
        this.config = config;
        this.consumerThreads = new HashMap<>();
    }

    /**
     * Receive ApplicationEvent publisher instance.
     *
     * @param eventPublisher publisher instance;
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Loop over consumer status, make sure they are all running within a time window.
     */
    @PostConstruct
    public void waitForConsumers() {
        var timeout = config.getStartup().getTimeoutMs();

        log.info("Waiting for consumers to be ready (max '{} ms')...", timeout);

        await().atMost(timeout, TimeUnit.MILLISECONDS).until(() -> {
            Thread.sleep(config.getStartup().getCheckIntervalMs());

            var runningConsumers = consumerThreads.keySet().stream()
                    .filter(AvroConsumer::isRunning)
                    .collect(Collectors.toList());

            log.info("Amount of consumers reporting ready: '{}'/'{}'",
                    runningConsumers.size(), consumerThreads.size());

            return runningConsumers.size() == consumerThreads.size();
        });
    }

    /**
     * Instantiate threads for Kafka streams consumers.
     */
    public void bootstrap() {
        for (RouteConfig route : config.getRoutes()) {
            log.info("Creating consumer for route named '{}'", route.getName());

            if (route.getResponse() == null) {
                log.info("Skipping consumer on route!");
                continue;
            }

            var consumer = new AvroConsumer(
                    tracer, eventPublisher, config.getKafka(), route.getResponse());
            var thread = new Thread(consumer);

            thread.start();
            consumerThreads.put(consumer, thread);
        }
    }
}
