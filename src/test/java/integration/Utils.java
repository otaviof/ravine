package integration;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
class Utils {
    /**
     * Simple tcp socket probe.
     *
     * @param hostAndPortString String, with Kafka's bootstrap server;
     * @return Boolean;
     */
    static Boolean isPortOpen(String hostAndPortString) throws InterruptedException {
        String[] hostAndPort = hostAndPortString.split(":");
        String hostname = hostAndPort[0];
        Integer port = Integer.parseInt(hostAndPort[1]);

        log.info("Checking if port '{}' is open at hostname '{}'", port, hostname);

        try (Socket socket = new Socket(hostname, port)) {
            log.info("Port '{}' is open at '{}'", port, hostname);
            return true;
        } catch (Exception e) {
            log.warn("Port '{}' is NOT YET open at '{}' ('{}')", port, hostname, e.getMessage());
            Thread.sleep(500);
            return false;
        }

    }

    /**
     * Create a kafka topic using admin client.
     *
     * @param broker brokers list;
     * @param topics topic names;
     */
    static void createKafkaTopics(String broker, List<String> topics) {
        log.info("Creating Kafka topics '{}' in Kafka broker '{}'...", topics, broker);

        var adminProperties = new Properties();
        adminProperties.put("bootstrap.servers", broker);

        var admin = KafkaAdminClient.create(adminProperties);
        var topicsCollection = topics.stream()
                .map(t -> new NewTopic(t, 1, (short) 1))
                .collect(Collectors.toList());

        admin.createTopics(topicsCollection);
    }

    /**
     * Read a resource based file, based in informed path.
     *
     * @param path resource path;
     * @return String, file contents;
     * @throws IOException on not being able to read;
     */
    private static String readFile(String path) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        byte[] encoded = new byte[0];
        if (resource != null) {
            encoded = Files.readAllBytes(Paths.get(resource.getPath()));
        }
        return new String(encoded, Charset.defaultCharset());
    }

    /**
     * Register a subject in Schema-Registry.
     *
     * @param schemaRegistryUrl schema-registry url;
     * @param subject           subject name;
     * @param avscPath          path to avro schema definition file;
     * @throws IOException         when having issues to read file;
     * @throws RestClientException on communicating with Schema-Registry;
     */
    static void registerSubject(String schemaRegistryUrl, String subject, String avscPath)
            throws IOException, RestClientException {
        log.info("Registering subject '{}' based on '{}' in Schema-Registry '{}'",
                subject, avscPath, schemaRegistryUrl);

        var schemaPayload = readFile(avscPath);
        var schema = new Schema.Parser().parse(schemaPayload);
        var client = new CachedSchemaRegistryClient(schemaRegistryUrl, 10000);

        client.register(subject, schema);
    }

}
