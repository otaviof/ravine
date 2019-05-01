package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaConfig {
    private String schemaRegistryUrl;
    private String brokers;
}
