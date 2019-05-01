package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteConfig {
    private String name;
    private String route;
    private SubjectConfig subject;
    private KafkaRouteConfig request;
    private KafkaRouteConfig response;
}
