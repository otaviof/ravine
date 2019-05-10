package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CacheConfig {
    private int maximumSize = 2000;
    private int expireMs = 15000;
}
