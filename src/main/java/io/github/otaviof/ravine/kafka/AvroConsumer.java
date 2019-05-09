package io.github.otaviof.ravine.kafka;

import static org.apache.kafka.streams.KafkaStreams.State.ERROR;
import static org.apache.kafka.streams.KafkaStreams.State.RUNNING;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.github.otaviof.ravine.config.KafkaConfig;
import io.github.otaviof.ravine.config.KafkaRouteConfig;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.streams.TracingKafkaClientSupplier;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Wrapper around Kafka Streams to create a generic Avro consumer, using Runnable approach. This
 * instance also exposes the stream state and a shutdown approach.
 */
@Slf4j
public class AvroConsumer extends AbstractHealthIndicator {
    private final Tracer tracer;
    private final ApplicationEventPublisher publisher;
    private final KafkaConfig kafkaConfig;
    private final KafkaRouteConfig routeConfig;

    @Getter
    private final String topic;

    @Getter
    private KafkaStreams streams;

    public AvroConsumer(
            Tracer tracer,
            ApplicationEventPublisher publisher,
            KafkaConfig kafkaConfig,
            KafkaRouteConfig routeConfig) {
        this.tracer = tracer;
        this.publisher = publisher;
        this.kafkaConfig = kafkaConfig;
        this.routeConfig = routeConfig;
        this.topic = routeConfig.getTopic();

        build();
    }

    /**
     * Setup consumer properties.
     *
     * @return Properties instance;
     */
    private Properties consumerProperties() {
        var brokers = kafkaConfig.getBrokers();
        var p = new Properties();

        log.info("Consumer bootstrap servers: '{}'", brokers);
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

        p.put(StreamsConfig.APPLICATION_ID_CONFIG, routeConfig.getGroupId());
        p.put(StreamsConfig.CLIENT_ID_CONFIG, routeConfig.getClientId());
        p.put(StreamsConfig.consumerPrefix(ConsumerConfig.GROUP_ID_CONFIG),
                routeConfig.getGroupId());

        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // mandatory for consumers as well, in order to consumer generic avro
        p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaConfig.getSchemaRegistryUrl());

        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, routeConfig.getValueSerde());
        p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, routeConfig.getValueSerde());

        p.putAll(kafkaConfig.getProperties());
        p.putAll(routeConfig.getProperties());

        return p;
    }

    /**
     * Setup stream topology.
     */
    private void build() {
        var builder = new StreamsBuilder();
        var topology = builder.build();
        var supplier = new TracingKafkaClientSupplier(tracer);

        log.info("Starting Kafka stream consumer processor on topic '{}'...", topic);
        topology.addSource("SOURCE", topic)
                .addProcessor("RavineStreamProcessor", () -> new StreamProcessor(publisher),
                        "SOURCE");

        streams = new KafkaStreams(topology, consumerProperties(), supplier);
    }

    /**
     * Check Kafka Streams state interface to assure is running.
     *
     * @return boolean;
     */
    public boolean isRunning() {
        log.info("Consumer state on topic '{}': {}", topic, streams.state());
        return streams.state() == RUNNING;
    }

    /**
     * Register consumers health checks into the registry, and show a aggregated status.
     *
     * @param builder health builder object;
     */
    @Override
    protected void doHealthCheck(Builder builder) {
        var state = streams.state();

        if (state == RUNNING) {
            builder.up();
            return;
        }
        if (state == ERROR) {
            log.error("Kafka consumer on topic '{}' state: '{}'", topic, state);
            builder.down();
            return;
        }

        log.info("Kafka consumer on topic '{}' state: '{}'", topic, state);
        builder.unknown();
    }
}
