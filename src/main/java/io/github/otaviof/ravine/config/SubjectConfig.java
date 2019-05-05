package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

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
