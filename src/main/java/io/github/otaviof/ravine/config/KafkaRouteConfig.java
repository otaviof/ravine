package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Represents the Kafka related configuration applied on each route. It works for producer and consumer
 * settings in the same way.
 */
@Getter
@Setter
public class KafkaRouteConfig {
    @NotEmpty
    private String topic;

    @NotEmpty
    private String valueSerde = "io.confluent.kafka.streams.serdes.avro.GenericAvroSerde";

    @Pattern(regexp = "^(-1|0|1|all)$")
    private String acks = "all";

    @Min(1)
    private int timeoutMs = 1000;

    @NotEmpty
    private String clientId = String.format("ravine-%s", RandomStringUtils.randomAlphabetic(10));

    @NotEmpty
    private String groupId = String.format("ravine-%s", RandomStringUtils.randomAlphabetic(10));
}
