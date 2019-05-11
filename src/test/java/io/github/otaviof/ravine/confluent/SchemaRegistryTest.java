package io.github.otaviof.ravine.confluent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.otaviof.ravine.config.SubjectConfig;
import io.github.otaviof.ravine.integration.PrepareBackend;
import java.io.IOException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaRegistryTest {
    private final SchemaRegistry schemaRegistry = new SchemaRegistry("http://127.0.0.1:8681");
    private SubjectConfig subject;

    @BeforeClass
    public static void prepare() throws IOException, RestClientException {
        PrepareBackend.waitForSchemaRegistry();
    }

    @Before
    public void setUp() {
        subject = new SubjectConfig();
        subject.setName("person");
        subject.setVersion(1);
    }

    @Test
    public void subjectExists() throws Exception {
        assertTrue(schemaRegistry.subjectExists(subject.getName()));
    }

    @Test
    public void latestVersion() throws Exception {
        assertEquals(1, schemaRegistry.latestVersion(subject.getName()));
    }

    @Test
    public void getSubject() throws Exception {
        var schema = schemaRegistry.getSubject(subject.getName());
        assertEquals(schema, schemaRegistry.getSubject(subject));
    }
}