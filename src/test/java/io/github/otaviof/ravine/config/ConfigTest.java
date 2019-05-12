package io.github.otaviof.ravine.config;

import io.github.otaviof.ravine.Ravine;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {Ravine.class, Config.class},
        initializers = ConfigFileApplicationContextInitializer.class)
class ConfigTest {
    @Autowired
    Config config;

    @Test
    void getRouteByName() {
        var route = config.getRouteByName("get-endpoint-example");

        Assertions.assertThat(route).isNotNull();
        Assertions.assertThat(route.getEndpoint().getPath()).startsWith("/");
    }

    @Test
    void getRouteByPath() {
        var route = config.getRouteByPath("/v1/group/app/action/get");

        Assertions.assertThat(route).isNotNull();
        Assertions.assertThat("get-endpoint-example").isEqualTo(route.getName());
    }

    @Test
    void getResponseKafkaRouteConfigs() {
        var responseKafkaRouteConfigs = config.getResponseKafkaRouteConfigs();

        Assertions.assertThat(responseKafkaRouteConfigs).isNotNull().isNotEmpty();
        Assertions.assertThat(responseKafkaRouteConfigs).containsKeys("post-endpoint-example");
    }
}