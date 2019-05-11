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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicatorRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Represents all consumers in this application, where messages received by them are funneled into
 * ApplicationEvents mechanism.
 */
@Component
@Configurable
@Slf4j
public class ConsumerGroup implements ApplicationEventPublisherAware {
    private final Tracer tracer;
    private final Config config;
    private final Map<AvroConsumer, Thread> consumerThreads;
    private ApplicationEventPublisher publisher;

    @Autowired
    private HealthAggregator healthAggregator;

    @Autowired
    private HealthIndicatorRegistry healthIndicatorRegistry;

    public ConsumerGroup(Tracer tracer, Config config) {
        this.tracer = tracer;
        this.config = config;
        this.consumerThreads = new HashMap<>();
    }

    /**
     * Receive ApplicationEvent publisher instance.
     *
     * @param publisher event publisher instance;
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
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

            var consumer = new AvroConsumer(tracer, publisher, config.getKafka(),
                    route.getResponse());
            var thread = new Thread(new AvroConsumerRunnable(consumer));

            thread.start();
            consumerThreads.put(consumer, thread);
        }
    }

    /**
     * Aggregate and expose a bean wth health-indicators of all instantiated consumers.
     *
     * @return HealthIndicator with all consumer results;
     */
    @Bean
    public HealthIndicator consumersHealthIndicator() {
        var composite = new CompositeHealthIndicator(healthAggregator, healthIndicatorRegistry);

        for (AvroConsumer c : consumerThreads.keySet()) {
            healthIndicatorRegistry.register(String.format("kafka-consumer--%s", c.getTopic()), c);
        }

        return composite;
    }
}
