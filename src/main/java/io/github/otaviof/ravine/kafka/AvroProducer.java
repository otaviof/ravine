package io.github.otaviof.ravine.kafka;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.github.otaviof.ravine.config.KafkaConfig;
import io.github.otaviof.ravine.config.KafkaRouteConfig;
import io.github.otaviof.ravine.errors.AvroProducerException;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaProducer;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Avro producer representation, handles configuration and send methods.
 */
@Slf4j
public class AvroProducer {
    static final String RAVINE_KEY = "ravine-key";

    private final KafkaConfig kafkaConfig;
    private final KafkaRouteConfig routeConfig;

    private final TracingKafkaProducer<String, GenericRecord> producer;

    public AvroProducer(
            Tracer tracer, String name, KafkaConfig kafkaConfig, KafkaRouteConfig routeConfig) {
        this.kafkaConfig = kafkaConfig;
        this.routeConfig = routeConfig;

        BiFunction<String, ProducerRecord, String> spanNameProvider = (operation, record) -> name;

        log.info("Creating a producer on topic '{}'", routeConfig.getTopic());
        var kafkaProducer = new KafkaProducer<String, GenericRecord>(producerProperties());
        this.producer = new TracingKafkaProducer<>(kafkaProducer, tracer, spanNameProvider);
    }

    /**
     * Prepare producer properties.
     *
     * @return Properties instance;
     */
    private Properties producerProperties() {
        var p = new Properties();

        log.info("Kafka bootstrap servers '{}'", kafkaConfig.getBrokers());
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());

        log.info("Schema-Registry URL '{}'", kafkaConfig.getSchemaRegistryUrl());
        p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaConfig.getSchemaRegistryUrl());

        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, routeConfig.getValueSerde());

        log.info("Producer blocking timeout is '{}' ms, and '{}' set as acknowledge mode.",
                routeConfig.getTimeoutMs(), routeConfig.getAcks());
        p.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, routeConfig.getTimeoutMs());
        p.put(ProducerConfig.ACKS_CONFIG, routeConfig.getAcks());

        return p;
    }

    /**
     * Produce a message on pre-configured topic, using a synchronous approach.
     *
     * @param k key;
     * @param v value;
     * @throws AvroProducerException on producing exceptions;
     */
    public void send(String k, GenericRecord v) throws AvroProducerException {
        var topic = routeConfig.getTopic();
        var record = new ProducerRecord<>(topic, k, v);

        record.headers().add(RAVINE_KEY, k.getBytes());

        try {
            producer.send(record).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.error("Error producing message on topic '{}': '{}'", topic, e.getMessage());
            throw new AvroProducerException(e.getMessage());
        }
    }
}
