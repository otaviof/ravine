package io.github.otaviof.ravine.confluent;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.otaviof.ravine.config.SubjectConfig;
import io.github.otaviof.ravine.integration.PrepareBackend;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SchemaRegistryTest {
    private final SchemaRegistry schemaRegistry = new SchemaRegistry("http://127.0.0.1:8681");
    private SubjectConfig subject;

    @BeforeAll
    public static void waitForSchemaRegistry() throws IOException, RestClientException {
        PrepareBackend.waitForSchemaRegistry();
    }

    @BeforeEach
    public void prepare() {
        subject = new SubjectConfig();
        subject.setName("person");
        subject.setVersion(1);
    }

    @Test
    public void subjectExists() throws Exception {
        Assertions.assertThat(schemaRegistry.subjectExists(subject.getName())).isTrue();
    }

    @Test
    public void latestVersion() throws Exception {
        Assertions.assertThat(1).isEqualTo(schemaRegistry.latestVersion(subject.getName()));
    }

    @Test
    public void getSubject() throws Exception {
        var schema = schemaRegistry.getSubject(subject.getName());

        Assertions.assertThat(schema).isEqualTo(schemaRegistry.getSubject(subject));
    }
}