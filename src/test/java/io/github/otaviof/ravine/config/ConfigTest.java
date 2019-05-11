package io.github.otaviof.ravine.config;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigTest {
    private Config config = new Config();

    @Before
    public void populate() {
        var routeConfig = new RouteConfig();
        var endpointConfig = new EndpointConfig();

        routeConfig.setName("test");
        endpointConfig.setPath("/test");
        routeConfig.setEndpoint(endpointConfig);

        config.setRoutes(Collections.singletonList(routeConfig));
    }

    @Test
    public void getRouteByPath() {
        var route = config.getRouteByPath("/test");

        Assert.assertNotNull(route);
        Assert.assertEquals("test", route.getName());
    }
}