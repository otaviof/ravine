package io.github.otaviof.ravine.confluent;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.otaviof.ravine.config.SubjectConfig;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;

/**
 * Represents a Confluent Schema Registry client.
 */
@Slf4j
public class SchemaRegistry {
    private static SchemaRegistryClient client = null;

    /**
     * Constructor.
     *
     * @param url schema-registry url;
     */
    public SchemaRegistry(String url) {
        if (client == null) {
            client = new CachedSchemaRegistryClient(url, 10000);
        }
    }

    /**
     * Get avro schema of subject name;
     *
     * @param subjectName string subject name;
     * @return avro schema;
     * @throws SchemaRegistryException on parsing and api communication;
     */
    public Schema getSubject(String subjectName) throws SchemaRegistryException {
        return getSchema(subjectName, 0);
    }

    /**
     * Get avro schema of subject using SubjectConfig as input.
     *
     * @param subject SubjectConfig object;
     * @return avro schema;
     * @throws SchemaRegistryException on parsing and api communication;
     */
    public Schema getSubject(SubjectConfig subject) throws SchemaRegistryException {
        log.info("Downloading Schema-Registry subject '{}' (v{})",
                subject.getName(), subject.getVersion());

        if (!subjectExists(subject.getName())) {
            log.error("Subject '{}' is not found on Schema-Registry", subject.getName());
            return null;
        }

        return getSchema(subject.getName(), subject.getVersion());
    }

    /**
     * Check if a given subject name exists in Schema Registry.
     *
     * @param subjectName subject name;
     * @return boolean;
     * @throws SchemaRegistryException on both parsing and api communication errors;
     */
    public boolean subjectExists(String subjectName) throws SchemaRegistryException {
        log.info("Checking if '{}' subject exists", subjectName);

        try {
            return client.getAllSubjects().contains(subjectName);
        } catch (IOException | RestClientException e) {
            log.error("On reading all subjects: '{}'", e.getMessage());
            throw new SchemaRegistryException("Error reading all-subjects from Schema-Registry");
        }
    }

    /**
     * Get latest version of a subject name.
     *
     * @param subjectName subject name;
     * @return int subject version;
     * @throws IOException on parsing data;
     * @throws RestClientException on api communication;
     */
    public int latestVersion(String subjectName) throws IOException, RestClientException {
        log.info("Checking latest version of '{}' subject", subjectName);
        return Collections.max(client.getAllVersions(subjectName));
    }

    /**
     * Download a schema using name and version. When version is zero it gets latest version of
     * schema.
     *
     * @param subjectName subject name;
     * @param subjectVersion subject version;
     * @return avro schema;
     * @throws SchemaRegistryException when not possible to reach or parse schemas;
     */
    private Schema getSchema(String subjectName, int subjectVersion)
            throws SchemaRegistryException {
        try {
            if (subjectVersion == 0) {
                subjectVersion = latestVersion(subjectName);
            }
            return client.getBySubjectAndId(subjectName, subjectVersion);
        } catch (IOException e) {
            log.error("Error on parsing schema: '{}'", e.getMessage());
            throw new SchemaRegistryException("Error parsing schema");
        } catch (RestClientException e) {
            log.error("Error on REST-API communication: '{}'", e.getMessage());
            throw new SchemaRegistryException("Error on REST-API communication");
        }
    }
}
