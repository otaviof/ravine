package integration;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.kafka.AvroConsumer;
import io.github.otaviof.ravine.kafka.AvroProducer;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

class ExternalActor {
    private final AvroConsumer consumer;

    boolean isConsumerReady() {
        return consumer.isRunning();
    }

    void bootstrap() {
        Thread consumerThread = new Thread(this.consumer);
        consumerThread.start();
    }

    ExternalActor(ApplicationEventPublisher publisher, Config config, String path) {
        var requestConfig =  config.getRouteByPath(path).getResponse();
        requestConfig.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer");
        requestConfig.setGroupId(UUID.randomUUID().toString());
        requestConfig.setTimeoutMs(3000);

        var responseConfig = config.getRouteByPath(path).getRequest();
        responseConfig.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerde");
        responseConfig.setGroupId(UUID.randomUUID().toString());
        responseConfig.setTimeoutMs(5000);

        var producer = new AvroProducer(config.getKafka(), requestConfig);
        var listener = new ExternalActorEventListener(producer);
        this.consumer = new AvroConsumer(publisher, config.getKafka(), responseConfig);
    }
}
