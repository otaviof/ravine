package io.github.otaviof.ravine.config;

import java.util.List;
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
    private StartUpConfig startup;

    @NotNull
    private KafkaConfig kafka;

    @NotEmpty
    private List<RouteConfig> routes;

    /**
     * Filter configuration to return RouteConfig based on path.
     *
     * @param path path we are looking for;
     * @return RouteConfig;
     */
    public RouteConfig getRouteByPath(String path) {
        return routes.stream()
                .filter(r -> r.getEndpoint().getPath().equals(path))
                .findAny()
                .orElse(null);
    }
}
