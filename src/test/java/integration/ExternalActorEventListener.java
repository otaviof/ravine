package integration;

import io.github.otaviof.ravine.kafka.AvroProducer;
import io.github.otaviof.ravine.kafka.AvroProducerException;
import io.github.otaviof.ravine.router.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

@Slf4j
public class ExternalActorEventListener implements ApplicationListener<Event> {
    private final AvroProducer producer;

    @Override
    public void onApplicationEvent(Event event) {
        try {
            log.info("Test event received, key: '{}'", event.getK());
            this.producer.send(event.getK(), event.getV());
        } catch (AvroProducerException e) {
            e.printStackTrace();
        }
    }

    public ExternalActorEventListener(AvroProducer producer) {
        this.producer = producer;
    }
}
