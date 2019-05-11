package io.github.otaviof.ravine.integration;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.otaviof.ravine.Ravine;
import io.github.otaviof.ravine.config.Config;
import io.opentracing.Tracer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Ravine.class)
@ContextConfiguration(classes = IntegrationTestConfig.class, loader = SpringBootContextLoader.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
public class RavineIntegrationTest {
    private static final String PERSON_PAYLOAD = "{ \"firstName\": \"ravine\", \"lastName\": \"test\" }";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    Config config;

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    Tracer tracer;

    @BeforeClass
    public static void prepare() throws IOException, RestClientException {
        PrepareBackend.prepare();
    }

    @Before
    public void usingDifferentTopics() {
        var routes = config.getRoutes().get(0);
        Assert.assertNotEquals(routes.getRequest().getTopic(), routes.getResponse().getTopic());
    }

    @Before
    public void prepareExternalActor() {
        var path = config.getRoutes().get(0).getEndpoint().getPath();
        var externalActor = new ExternalActor(tracer, publisher, config, path);

        externalActor.bootstrap();
        await().atMost(60, TimeUnit.SECONDS).until(externalActor::isConsumerReady);
    }

    @Test
    public void executePostRequest() throws Exception {
        var path = config.getRoutes().get(0).getEndpoint().getPath();

        mockMvc.perform(MockMvcRequestBuilders
                .post(path)
                .content(PERSON_PAYLOAD)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(".firstName").exists())
                .andExpect(MockMvcResultMatchers.jsonPath(".lastName").exists());
    }

    @Test
    public void executePutRequest() throws Exception {
        var path = config.getRoutes().get(1).getEndpoint().getPath();

        mockMvc.perform(MockMvcRequestBuilders
                .put(path)
                .content(PERSON_PAYLOAD)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
