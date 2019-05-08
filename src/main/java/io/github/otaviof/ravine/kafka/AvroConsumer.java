package io.github.otaviof.ravine.kafka;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.github.otaviof.ravine.config.KafkaConfig;
import io.github.otaviof.ravine.config.KafkaRouteConfig;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.streams.TracingKafkaClientSupplier;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Wrapper around Kafka Streams to create a generic Avro consumer, using Runnable approach. This
 * instance also exposes the stream state and a shutdown approach.
 */
@Slf4j
public class AvroConsumer implements Runnable {
    private final Tracer tracer;
    private final ApplicationEventPublisher publisher;
    private final KafkaConfig kafkaConfig;
    private final KafkaRouteConfig routeConfig;

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

        build();
    }

    /**
     * Start consumer thread.
     */
    @Override
    public void run() {
        log.info("Starting consumer...");
        streams.start();
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

        return p;
    }

    /**
     * Setup stream topology.
     */
    private void build() {
        var builder = new StreamsBuilder();
        var topology = builder.build();
        var supplier = new TracingKafkaClientSupplier(tracer);
        var topic = routeConfig.getTopic();

        log.info("Starting Kafka stream consumer processor on topic '{}'...", topic);
        topology
                .addSource("SOURCE", topic)
                .addProcessor("RavineStreamProcessor", () -> new StreamProcessor(publisher),
                        "SOURCE");

        streams = new KafkaStreams(topology, consumerProperties(), supplier);
    }

    /**
     * Run shutdown procedure on thread.
     */
    public void stop() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.close()));
    }

    /**
     * Check Kafka Streams state interface to assure is running.
     *
     * @return boolean;
     */
    public boolean isRunning() {
        log.info("Consumer state on topic '{}': {}", routeConfig.getTopic(), streams.state());
        return streams.state() == KafkaStreams.State.RUNNING;
    }
}
