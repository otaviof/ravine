package io.github.otaviof.ravine.kafka;

import io.github.otaviof.ravine.router.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Low level API stream processor in order to access Kafka headers.
 */
@Slf4j
public class StreamProcessor implements Processor<String, GenericRecord> {
    private final ApplicationEventPublisher publisher;
    private ProcessorContext context;

    /**
     * Receive the message's context.
     *
     * @param context context;
     */
    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    /**
     * Process a message in stream, by inspecting headers, and forwarding the event.
     *
     * @param key message key;
     * @param value message payload;
     */
    @Override
    public void process(String key, GenericRecord value) {
        var ravineKey = key;

        log.info("Processing event from topic '{}' with key '{}'", context.topic(), key);

        for (Header h : context.headers()) {
            var k = h.key();
            var v = new String(h.value());

            log.trace("Header: key='{}', value='{}'", k, v);
            if (AvroProducer.RAVINE_KEY.equals(k)) {
                ravineKey = v;
                log.debug("Using ravine-key '{}' from header entry on consumed record.", ravineKey);
            }
        }

        publisher.publishEvent(new Event(this, ravineKey, value));
        context.commit();
    }

    /**
     * Empty close method.
     */
    @Override
    public void close() {
        // not implemented
    }

    StreamProcessor(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
}
