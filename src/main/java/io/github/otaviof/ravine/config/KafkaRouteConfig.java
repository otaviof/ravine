package io.github.otaviof.ravine.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class KafkaRouteConfig {
    private String topic;
    private String valueSerde = "io.confluent.kafka.streams.serdes.avro.GenericAvroSerde";
    private int timeoutMs = 1000;
    private String clientId = UUID.randomUUID().toString();
    private String groupId = UUID.randomUUID().toString();
}
