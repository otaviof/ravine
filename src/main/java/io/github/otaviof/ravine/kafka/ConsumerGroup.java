package io.github.otaviof.ravine.kafka;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.config.RouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

/**
 * Represents all consumers in this application, where messages received by them are funneled into
 * ApplicationEvents mechanism.
 */
@Component
@Slf4j
public class ConsumerGroup implements ApplicationEventPublisherAware {
    private final Config config;

    private ApplicationEventPublisher eventPublisher;
    private Map<AvroConsumer, Thread> consumerThreads;

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
        log.info("Waiting for consumers to be ready (max '{} ms')...", config.getStartUp().getTimeoutMs());
        await().atMost(config.getStartUp().getTimeoutMs(), TimeUnit.MILLISECONDS).until(() -> {
            Thread.sleep(config.getStartUp().getCheckIntervalMs());
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
            log.info("Creating a consumer for route responses...");
            var consumer = new AvroConsumer(eventPublisher, config.getKafka(), route.getResponse());
            var thread = new Thread(consumer);
            thread.start();
            consumerThreads.put(consumer, thread);
        }
    }

    public ConsumerGroup(Config config) {
        this.config = config;
        this.consumerThreads = new HashMap<>();
    }
}
