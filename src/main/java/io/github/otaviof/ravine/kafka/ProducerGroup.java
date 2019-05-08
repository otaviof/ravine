package io.github.otaviof.ravine.kafka;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.config.RouteConfig;
import io.github.otaviof.ravine.confluent.SchemaRegistry;
import io.github.otaviof.ravine.confluent.SchemaRegistryException;
import io.github.otaviof.ravine.errors.AvroProducerException;
import io.github.otaviof.ravine.errors.InvalidPayloadException;
import io.opentracing.Tracer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.springframework.stereotype.Component;

/**
 * Group all Kafka producers in a single instance, organizing producers by the path the represent in
 * configuration object.
 */
@Component
@Slf4j
public class ProducerGroup {
    private final Tracer tracer;
    private final Config config;

    private final Map<String, AvroProducer> producers;
    private final Map<String, Schema> reqSchemas;
    private final SchemaRegistry schemaRegistry;

    public ProducerGroup(Tracer tracer, Config config) throws SchemaRegistryException {
        this.tracer = tracer;
        this.config = config;
        this.schemaRegistry = new SchemaRegistry(config.getKafka().getSchemaRegistryUrl());

        this.reqSchemas = new HashMap<>();
        this.producers = new HashMap<>();

        bootstrap();
    }

    /**
     * Produce a message in Kafka topic.
     *
     * @param path on behalf of path, used to search configuration;
     * @param key record key;
     * @param payload future record payload;
     * @throws AvroProducerException on having errors to serialize;
     */
    public void send(String path, String key, byte[] payload) throws AvroProducerException {
        var record = convertToAvro(payload, reqSchemas.get(path));
        log.info("Producing message with key '{}' for path '{}'", key, path);
        producers.get(path).send(key, record);
    }

    /**
     * Serialize a array of bytes into a generic Avro object, based on class defined Schema.
     *
     * @param payload array of bytes with submitted payload;
     * @param schema Avro schema;
     * @return GenericRecord representation;
     */
    private GenericRecord convertToAvro(byte[] payload, Schema schema) {
        var input = new ByteArrayInputStream(payload);
        var output = new ByteArrayOutputStream();
        var enc = EncoderFactory.get().binaryEncoder(output, null);
        var reader = new GenericDatumReader<GenericRecord>(schema);
        var writer = new GenericDatumWriter<GenericRecord>(schema);

        log.info("Parsing request body against Schema '{}'", schema.getName());
        log.debug("Message body informed is: '{}'", new String(payload));

        try {
            var dec = DecoderFactory.get().jsonDecoder(schema, input);
            var record = reader.read(null, dec);

            writer.write(record, enc);
            enc.flush();

            return record;
        } catch (IOException | AvroTypeException e) {
            log.error("Error on parsing message body: '{}', caused by '{}'",
                    e.getMessage(), e.getCause());
            throw new InvalidPayloadException(e.getMessage());
        }
    }

    /**
     * Prepare the producer by reaching out to Schema-Registry to obtain Schema, and regular Kafka
     * producer boilerplate.
     *
     * @throws SchemaRegistryException when issues on Schema-Registry client;
     */
    private void bootstrap() throws SchemaRegistryException {
        for (RouteConfig route : config.getRoutes()) {
            var routePath = route.getEndpoint().getPath();
            var subject = route.getSubject();
            var spanName = String.format("name=\"%s\", subject=\"%s\", version=\"%d\"",
                    route.getName(), subject.getName(), subject.getVersion());

            log.info("Kafka producer named '{}' for '{}' route", route.getName(), routePath);
            producers.put(routePath,
                    new AvroProducer(tracer, spanName, config.getKafka(), route.getRequest()));

            log.info("Registering schema for route...");
            reqSchemas.put(routePath, schemaRegistry.getSubject(subject));
        }
    }
}
