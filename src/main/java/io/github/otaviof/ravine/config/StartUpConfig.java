package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;

/**
 * Global settings for startup configuration.
 */
@Getter
@Setter
public class StartUpConfig {
    @Min(1)
    int timeoutMs = 45000;

    @Min(1)
    int checkIntervalMs = 1000;
}
