package io.github.otaviof.ravine;

import io.github.otaviof.ravine.config.SubjectConfig;
import io.github.otaviof.ravine.confluent.SchemaRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SchemaRegistryTest {
    private SubjectConfig subject;

    SchemaRegistry schemaRegistry = new SchemaRegistry("http://127.0.0.1:8681");

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