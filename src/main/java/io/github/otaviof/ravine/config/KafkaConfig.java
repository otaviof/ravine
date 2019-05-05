package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

/**
 * Global Kafka related configuration.
 */
@Getter
@Setter
public class KafkaConfig {
    @NotEmpty
    private String schemaRegistryUrl;

    @NotEmpty
    private String brokers;
}
