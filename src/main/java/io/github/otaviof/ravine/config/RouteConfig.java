package io.github.otaviof.ravine.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a route entry in main configuration.
 */
@Getter
@Setter
public class RouteConfig {
    @NotEmpty
    private String name;

    @NotNull
    private EndpointConfig endpoint;

    private SubjectConfig subject;

    @NotNull
    private KafkaRouteConfig request;

    private KafkaRouteConfig response;
}
