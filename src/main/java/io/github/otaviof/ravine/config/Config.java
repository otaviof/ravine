package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Primary configuration object for Ravine.
 */
@Component
@ConfigurationProperties("ravine")
@Getter
@Setter
public class Config {
    private StartUpConfig startUp;
    private KafkaConfig kafka;
    private List<RouteConfig> routes;

    /**
     * Filter configuration to return RouteConfig based on path.
     *
     * @param path path we are looking for;
     * @return RouteConfig;
     */
    public RouteConfig getRouteByPath(String path) {
        return routes.stream().filter(r -> r.getRoute().equals(path)).findAny().orElse(null);
    }
}
