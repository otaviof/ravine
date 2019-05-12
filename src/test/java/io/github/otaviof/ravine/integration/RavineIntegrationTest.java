package io.github.otaviof.ravine.integration;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.otaviof.ravine.Ravine;
import io.github.otaviof.ravine.config.Config;
import io.opentracing.Tracer;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Integration tests for Ravine. It prepare the backend services before starting the Spring Boot
 * application, and then executing http-requests to exercise endpoints configured for testing.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Ravine.class)
@ContextConfiguration(classes = IntegrationTestConfig.class, loader = SpringBootContextLoader.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
class RavineIntegrationTest {
    private static final String PERSON_PAYLOAD = "{ \"firstName\": \"ravine\", \"lastName\": \"test\" }";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    Config config;

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    Tracer tracer;

    private ExternalActor externalActor = null;

    /**
     * Prepare required backend services to interact with Ravine.
     *
     * @throws Exception on timeout waiting, or errors interacting with backend services;
     */
    @BeforeAll
    static void prepare() throws Exception {
        PrepareBackend.prepare();
    }

    /**
     * Instantiate a external actor, to consume request topic and produce events on response topic
     * in order to simulate a external application interacting with Ravine.
     */
    @BeforeEach
    void prepareExternalActor() {
        if (externalActor != null) {
            return;
        }

        log.info("Instantiating external actor to simulate a third party application...");

        var path = config.getRoutes().get(0).getEndpoint().getPath();
        externalActor = new ExternalActor(tracer, publisher, config, path);

        externalActor.bootstrap();
        await().atMost(60, TimeUnit.SECONDS).until(externalActor::isConsumerReady);
    }

    /**
     * Execute a GET request to request data. The data is produced by the ExternalActor, which is
     * inspecting request parameters (informed as Kafka headers) to create a dummy response. When
     * successful the response status-code should be as configured.
     *
     * @throws Exception on errors;
     */
    @Test
    void executeGetRequest() throws Exception {
        var route = config.getRouteByName("get-endpoint-example");

        Assertions.assertThat(route.getRequest().getTopic())
                .isNotEqualTo(route.getResponse().getTopic());

        mockMvc.perform(MockMvcRequestBuilders
                .get(route.getEndpoint().getPath())
                .accept(route.getEndpoint().getResponse().getContentType()))
                .andExpect(status().is(route.getEndpoint().getResponse().getHttpCode()))
                .andExpect(MockMvcResultMatchers.content().string("{}"));
    }

    /**
     * Execute a POST request to dispatch a given object, and wait for the same content to come
     * back. The posted content is handled by the ExternalActor, which will consume from request
     * topic and produce exactly the same content back to response topic, which this test is going
     * to assert.
     *
     * @throws Exception on errors;
     */
    @Test
    void executePostRequest() throws Exception {
        var route = config.getRouteByName("post-endpoint-example");

        Assertions.assertThat(route.getRequest().getTopic())
                .isNotEqualTo(route.getResponse().getTopic());

        mockMvc.perform(MockMvcRequestBuilders
                .post(route.getEndpoint().getPath())
                .content(PERSON_PAYLOAD)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(route.getEndpoint().getResponse().getContentType()))
                .andExpect(status().is(route.getEndpoint().getResponse().getHttpCode()))
                .andExpect(MockMvcResultMatchers.jsonPath(".firstName").exists())
                .andExpect(MockMvcResultMatchers.jsonPath(".lastName").exists());
    }

    /**
     * Execute a PUT request to dispatch a given payload to Kafka, and expected the configured
     * response. On having configured response we assert that the Kafka producer is working as
     * expected, and configured rules are applied.
     *
     * @throws Exception on request errors;
     */
    @Test
    void executePutRequest() throws Exception {
        var route = config.getRouteByName("put-endpoint-example");

        // response topic should not be defined
        Assertions.assertThat(route.getResponse()).isNull();

        mockMvc.perform(MockMvcRequestBuilders
                .put(route.getEndpoint().getPath())
                .content(PERSON_PAYLOAD)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(route.getEndpoint().getResponse().getHttpCode()))
                .andExpect(MockMvcResultMatchers.content()
                        .string(route.getEndpoint().getResponse().getBody()));
    }
}
