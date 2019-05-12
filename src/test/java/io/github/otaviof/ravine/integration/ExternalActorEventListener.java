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
    private final String schemaName;

    ExternalActorEventListener(AvroProducer producer, String schemaName) {
        this.producer = producer;
        this.schemaName = schemaName;
    }

    /**
     * On every event received, forward the payload to the producer instance.
     *
     * @param event Event;
     */
    @Override
    public void onApplicationEvent(Event event) {
        try {
            var schema = event.getV().getSchema().getName();

            log.info("[TEST] event received, type: '{}', key: '{}', value: '{}'",
                    schema, event.getK(), event.getV());

            if (schemaName.equals(schema)) {
                this.producer.send(event.getK(), event.getV(), new HashMap<>());
            } else {
                log.info("[TEST] Skipping record!");
            }
        } catch (AvroProducerException e) {
            e.printStackTrace();
        }
    }
}
