package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

@Getter
@Setter
public class KafkaRouteConfig {
    private String topic;
    private String valueSerde = "io.confluent.kafka.streams.serdes.avro.GenericAvroSerde";
    private int timeoutMs = 1000;
    private String clientId = String.format("ravine-%s", RandomStringUtils.randomAlphabetic(10));
    private String groupId = String.format("ravine-%s", RandomStringUtils.randomAlphabetic(10));
}
