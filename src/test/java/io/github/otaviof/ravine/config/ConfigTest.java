package io.github.otaviof.ravine.config;

import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigTest {
    private Config config = new Config();

    @BeforeEach
    void populate() {
        var routeConfig = new RouteConfig();
        var endpointConfig = new EndpointConfig();

        routeConfig.setName("test");
        endpointConfig.setPath("/test");
        routeConfig.setEndpoint(endpointConfig);

        config.setRoutes(Collections.singletonList(routeConfig));
    }

    @Test
    void getRouteByPath() {
        var route = config.getRouteByPath("/test");

        Assertions.assertThat(route).isNotNull();
        Assertions.assertThat("test").isEqualTo(route.getName());
    }
}