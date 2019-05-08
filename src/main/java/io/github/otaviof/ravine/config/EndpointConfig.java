package io.github.otaviof.ravine.config;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the configuration for the endpoint attribute, applied on every route entry.
 */
@Getter
@Setter
public class EndpointConfig {
    @Pattern(regexp = "^\\/.*", message = "path must start with '/'")
    private String path;

    @NotEmpty
    private List<String> methods = Collections.singletonList("post");

    private ResponseConfig response;
}
