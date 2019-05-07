package integration;

import io.github.otaviof.ravine.errors.AvroProducerException;
import io.github.otaviof.ravine.kafka.AvroProducer;
import io.github.otaviof.ravine.router.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

@Slf4j
class ExternalActorEventListener implements ApplicationListener<Event> {
    private final AvroProducer producer;

    @Override
    public void onApplicationEvent(Event event) {
        try {
            log.info("[TEST] event received, key: '{}', value: '{}'", event.getK(), event.getV());
            this.producer.send(event.getK(), event.getV());
        } catch (AvroProducerException e) {
            e.printStackTrace();
        }
    }

    ExternalActorEventListener(AvroProducer producer) {
        this.producer = producer;
    }
}
