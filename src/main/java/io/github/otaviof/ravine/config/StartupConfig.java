package io.github.otaviof.ravine.config;

import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Global settings for startup configuration.
 */
@Getter
@Setter
public class StartupConfig {
    @Min(1)
    int timeoutMs = 45000;

    @Min(1)
    int checkIntervalMs = 1000;
}
