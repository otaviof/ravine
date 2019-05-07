package integration;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@Slf4j
public class PrepareBackend {
    /**
     * Waiting for Kafka, and when ready create request and response topics.
     */
    private static void waitForKafka() {
        var broker = "kafka.localtest.me:9092";

        log.info("Waiting for Kafka broker at '{}'", broker);
        await().atMost(60, TimeUnit.SECONDS).until(() -> Utils.isPortOpen(broker));

        Utils.createKafkaTopics(broker,
                Arrays.asList("kafka_request_topic", "kafka_response_topic", "kafka_dump_topic"));
    }

    /**
     * Wait for Schema-Registry and when ready, register "person" subject.
     *
     * @throws IOException         on reading avro definition file;
     * @throws RestClientException on registering schema;
     */
    public static void waitForSchemaRegistry() throws IOException, RestClientException {
        var schemaRegistryUrl = "schemaregistry.localtest.me:8681";

        log.info("Waiting for Schema-Registry at '{}'", schemaRegistryUrl);
        await().atMost(60, TimeUnit.SECONDS).until(() -> Utils.isPortOpen(schemaRegistryUrl));

        Utils.registerSubject(String.format("http://%s", schemaRegistryUrl), "person", "avro/person.avsc");
    }


    /**
     * Wait for Kafka and Schema-Registry.
     *
     * @throws IOException on loading schema file;
     * @throws RestClientException on communication with Schema-Registry;
     */
    public static void prepare() throws IOException, RestClientException {
        waitForKafka();
        waitForSchemaRegistry();
    }
}
