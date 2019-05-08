package io.github.otaviof.ravine.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Schema related configuration, applied on every route entry.
 */
@Getter
@Setter
public class SubjectConfig {
    @NotEmpty
    private String name;

    @Min(0)
    private int version = 1;
}
