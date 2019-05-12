package io.github.otaviof.ravine.integration;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.config.KafkaRouteConfig;
import io.github.otaviof.ravine.config.RouteConfig;
import io.github.otaviof.ravine.kafka.AvroConsumer;
import io.github.otaviof.ravine.kafka.AvroConsumerRunnable;
import io.github.otaviof.ravine.kafka.AvroProducer;
import io.opentracing.Tracer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Pretends to be a external application interacting via Kafka with Ravine. It consume from all
 * configured request topics, and produce the exact same payload on the respective response topic.
 * Therefore, it acts as a third-party application interacting with Ravine.
 */
@Slf4j
class ExternalActor {
    private final Map<AvroConsumer, Thread> consumerThreads;

    ExternalActor(Tracer tracer, ApplicationEventPublisher publisher, Config config) {
        this.consumerThreads = new HashMap<>();

        for (RouteConfig routeConfig : config.getRoutes()) {
            if (routeConfig.getResponse() != null) {
                var producerConfig = prepareRequestKafkaRouteConfig(routeConfig);
                var producer = new AvroProducer(tracer, "integration", config.getKafka(),
                        producerConfig);
                var subject = routeConfig.getSubject();
                var listener = new ExternalActorEventListener(producer,
                        subject == null ? "RavineEmptyRecord" : subject.getName());
            }

            var consumerConfig = prepareResponseKafkaRouteConfig(routeConfig);
            var consumer = new AvroConsumer(tracer, publisher, config.getKafka(), consumerConfig);
            var thread = new Thread(new AvroConsumerRunnable(consumer));

            this.consumerThreads.put(consumer, thread);
        }
    }

    private KafkaRouteConfig prepareRequestKafkaRouteConfig(RouteConfig route) {
        var cfg = route.getResponse();

        cfg.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer");
        cfg.setGroupId(UUID.randomUUID().toString());
        cfg.setTimeoutMs(3000);

        return cfg;
    }

    private KafkaRouteConfig prepareResponseKafkaRouteConfig(RouteConfig route) {
        var cfg = route.getRequest();

        cfg.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerde");
        cfg.setGroupId(UUID.randomUUID().toString());
        cfg.setTimeoutMs(5000);

        return cfg;
    }

    boolean isConsumerReady() {
        for (Map.Entry<AvroConsumer, Thread> entry : consumerThreads.entrySet()) {
            if (!entry.getKey().isRunning()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Start the consumer threads.
     */
    void bootstrap() {
        log.info("Starting test-actor consumer threads...");
        for (Map.Entry<AvroConsumer, Thread> entry : consumerThreads.entrySet()) {
            entry.getValue().start();
        }
    }
}
