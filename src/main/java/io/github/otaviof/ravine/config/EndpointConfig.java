package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Collections;
import java.util.List;

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

    @Min(100)
    @Max(599)
    private int responseCode = 200;
}
