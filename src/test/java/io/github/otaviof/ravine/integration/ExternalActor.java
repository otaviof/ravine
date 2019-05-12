package io.github.otaviof.ravine.integration;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.kafka.AvroConsumer;
import io.github.otaviof.ravine.kafka.AvroConsumerRunnable;
import io.github.otaviof.ravine.kafka.AvroProducer;
import io.opentracing.Tracer;
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
    private final AvroConsumer consumer;

    ExternalActor(Tracer tracer, ApplicationEventPublisher publisher, Config config, String path) {
        var requestConfig = config.getRouteByPath(path).getResponse();
        requestConfig.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer");
        requestConfig.setGroupId(UUID.randomUUID().toString());
        requestConfig.setTimeoutMs(3000);

        var responseConfig = config.getRouteByPath(path).getRequest();
        responseConfig.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerde");
        responseConfig.setGroupId(UUID.randomUUID().toString());
        responseConfig.setTimeoutMs(5000);

        var producer = new AvroProducer(
                tracer, "integration-tests", config.getKafka(), requestConfig);
        var listener = new ExternalActorEventListener(producer);
        this.consumer = new AvroConsumer(tracer, publisher, config.getKafka(), responseConfig);
    }

    boolean isConsumerReady() {
        return consumer.isRunning();
    }

    /**
     * Start the consumer threads.
     */
    void bootstrap() {
        log.info("Starting test-actor consumer thread...");
        Thread consumerThread = new Thread(new AvroConsumerRunnable(this.consumer));
        consumerThread.start();
    }
}
