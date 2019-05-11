package io.github.otaviof.ravine.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Primary configuration object for Ravine.
 */
@Component
@ConfigurationProperties("ravine")
@Validated
@Getter
@Setter
public class Config {
    @NotNull
    private StartupConfig startup = new StartupConfig();

    @NotNull
    private CacheConfig cache = new CacheConfig();

    @NotNull
    private KafkaConfig kafka = new KafkaConfig();

    @NotEmpty
    private List<RouteConfig> routes = new ArrayList<>();

    /**
     * Filter configuration to return a RouteConfig based on name.
     *
     * @param name route name;
     * @return RouteConfig;
     */
    public RouteConfig getRouteByName(String name) {
        return routes.stream()
                .filter(r -> r.getName().equals(name))
                .findAny()
                .orElse(null);
    }

    /**
     * Filter configuration to return a RouteConfig based on path.
     *
     * @param path path we are looking for;
     * @return RouteConfig;
     */
    public RouteConfig getRouteByPath(String path) {
        return routes.stream()
                .filter(r -> r.getEndpoint() != null)
                .filter(r -> r.getEndpoint().getPath().equals(path))
                .findAny()
                .orElse(null);
    }

    /**
     * Return a map with KafkaRouteConfigs for response entries.
     *
     * @return Map, key as route-name, and value as response KafkaRouteConfig;
     */
    public Map<String, KafkaRouteConfig> getResponseKafkaRouteConfigs() {
        return routes.stream()
                .filter(r -> r.getResponse() != null)
                .collect(Collectors.toMap(RouteConfig::getName, RouteConfig::getResponse));
    }
}
