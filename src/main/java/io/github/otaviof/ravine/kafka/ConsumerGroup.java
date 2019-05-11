package io.github.otaviof.ravine.kafka;

import static org.awaitility.Awaitility.await;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.config.KafkaRouteConfig;
import io.opentracing.Tracer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
     * Plan which topics should be consumed, create a map based on  getResponseKafkaRouteConfigs,
     * but applies simple deduplication to not consume from the same topic at once.
     *
     * @return map based on getResponseKafkaRouteConfigs;
     * @throws ConsumerGroupException on having two topics with different serialization settings;
     */
    private Map<String, KafkaRouteConfig> plan() throws ConsumerGroupException {
        var toConsumeConfigs = new HashMap<String, KafkaRouteConfig>();

        for (Entry<String, KafkaRouteConfig> entry : config.getResponseKafkaRouteConfigs()
                .entrySet()) {
            var topic = entry.getValue().getTopic();
            var serde = entry.getValue().getValueSerde();

            for (KafkaRouteConfig cfg : toConsumeConfigs.values()) {
                if (cfg.getTopic().equals(topic) && !cfg.getValueSerde().equals(serde)) {
                    var msg = String.format(
                            "Topic '%s' was defined with different serializers: '%s' vs. '%s'!",
                            topic, serde, cfg.getValueSerde());
                    throw new ConsumerGroupException(msg);
                }
            }

            toConsumeConfigs.put(entry.getKey(), entry.getValue());
        }

        return toConsumeConfigs;
    }

    /**
     * Bootstrap consumers threads, based on plan.
     *
     * @throws ConsumerGroupException on having two topics with different serialization settings;
     */
    public void bootstrap() throws ConsumerGroupException {
        for (Entry<String, KafkaRouteConfig> entry : plan().entrySet()) {
            var name = entry.getKey();
            var cfg = entry.getValue();

            log.info("Creating consumer for '{}', on topic '{}'", name, cfg.getTopic());

            var exists = consumerThreads.keySet().stream()
                    .filter(c -> c.getTopic().equals(cfg.getTopic()))
                    .findAny()
                    .orElse(null);

            if (exists != null) {
                log.info("Skipping topic '{}' is being consumed already!", cfg.getTopic());
                continue;
            }

            var consumer = new AvroConsumer(tracer, publisher, config.getKafka(), cfg);
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
