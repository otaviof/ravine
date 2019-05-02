package integration;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.otaviof.ravine.Ravine;
import io.github.otaviof.ravine.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Ravine.class)
@ContextConfiguration(classes = IntegrationTestConfig.class, loader = SpringBootContextLoader.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
public class RavineTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    Config config;

    @BeforeClass
    public static void prepare() throws IOException, RestClientException {
        PrepareBackends.prepare();
    }

    @Test
    public void executeRequest() throws Exception {
        var path = config.getRoutes().get(0).getRoute();
        var externalActor = new ExternalActor(config, path);

        externalActor.bootstrap();
        await().atMost(60, TimeUnit.SECONDS).until(externalActor::isConsumerReady);

        mockMvc.perform(MockMvcRequestBuilders
                .post(path)
                .content("{ \"firstName\": \"ravine\", \"lastName\": \"test\" }")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath(".firstName").exists())
                .andExpect(MockMvcResultMatchers.jsonPath(".lastName").exists());
    }
}
