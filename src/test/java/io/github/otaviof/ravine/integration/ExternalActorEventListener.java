package io.github.otaviof.ravine.integration;

import io.github.otaviof.ravine.kafka.AvroProducer;
import io.github.otaviof.ravine.kafka.AvroProducerException;
import io.github.otaviof.ravine.router.Event;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

/**
 * Listen over events produced by the External Actor, and proxy messages to the producer instance,
 * informed on constructor.
 */
@Slf4j
class ExternalActorEventListener implements ApplicationListener<Event> {
    private final AvroProducer producer;

    ExternalActorEventListener(AvroProducer producer) {
        this.producer = producer;
    }

    /**
     * On every event received, forward the payload to the producer instance.
     *
     * @param event Event;
     */
    @Override
    public void onApplicationEvent(Event event) {
        try {
            var schemaName = event.getV().getSchema().getName();

            log.info("[TEST] event received, type: '{}', key: '{}', value: '{}'",
                    schemaName, event.getK(), event.getV());

            this.producer.send(event.getK(), event.getV(), new HashMap<>());
        } catch (AvroProducerException e) {
            e.printStackTrace();
        }
    }
}
