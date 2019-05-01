package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartUpConfig {
    int timeoutMs = 45000;
    int checkIntervalMs = 1000;
}
