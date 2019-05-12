package io.github.otaviof.ravine.confluent;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.otaviof.ravine.config.SubjectConfig;
import io.github.otaviof.ravine.integration.PrepareBackend;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaRegistryTest {
    private final SchemaRegistry schemaRegistry = new SchemaRegistry("http://127.0.0.1:8681");
    private SubjectConfig subject;

    @BeforeAll
    static void waitForSchemaRegistry() throws IOException, RestClientException {
        PrepareBackend.waitForSchemaRegistry();
    }

    @BeforeEach
    void prepare() {
        subject = new SubjectConfig();
        subject.setName("person");
        subject.setVersion(1);
    }

    @Test
    void subjectExists() throws Exception {
        Assertions.assertThat(schemaRegistry.subjectExists(subject.getName())).isTrue();
    }

    @Test
    void latestVersion() throws Exception {
        Assertions.assertThat(1).isEqualTo(schemaRegistry.latestVersion(subject.getName()));
    }

    @Test
    void getSubject() throws Exception {
        var schemaData = schemaRegistry.getSubject(subject.getName());

        Assertions.assertThat(schemaData).isEqualTo(schemaRegistry.getSubject(subject));
    }
}