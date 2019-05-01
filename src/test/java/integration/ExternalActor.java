package integration;

import io.github.otaviof.ravine.config.Config;
import io.github.otaviof.ravine.kafka.AvroConsumer;
import io.github.otaviof.ravine.kafka.AvroProducer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

public class ExternalActor {
    private final ExternalActorEventListener listener;
    private final AvroConsumer consumer;
    private Thread consumerThread;
    private final AvroProducer producer;

    @MockBean
    private ApplicationEventPublisher publisher;

    public boolean isConsumerReady() {
        return consumer.isRunning();
    }

    public void bootstrap() {
        this.consumerThread = new Thread(this.consumer);
        this.consumerThread.start();
    }

    public ExternalActor(Config config, String path) {
        var requestConfig =  config.getRouteByPath(path).getResponse();
        requestConfig.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer");

        var responseConfig = config.getRouteByPath(path).getRequest();
        responseConfig.setValueSerde("io.confluent.kafka.streams.serdes.avro.GenericAvroSerde");
        responseConfig.setGroupId(UUID.randomUUID().toString());

        this.producer = new AvroProducer(config.getKafka(), requestConfig);
        this.listener = new ExternalActorEventListener(this.producer);
        this.consumer = new AvroConsumer(publisher, config.getKafka(), responseConfig);
    }
}
