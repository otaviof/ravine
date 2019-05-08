package io.github.otaviof.ravine.config;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

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

    private Map<String, String> properties = new HashMap<>();
}
