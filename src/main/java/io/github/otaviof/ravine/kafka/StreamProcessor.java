package io.github.otaviof.ravine.kafka;

import io.github.otaviof.ravine.router.Event;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
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
    private final Tracer tracer;
    private final ApplicationEventPublisher publisher;

    private ProcessorContext context;

    StreamProcessor(Tracer tracer, ApplicationEventPublisher publisher) {
        this.tracer = tracer;
        this.publisher = publisher;
    }

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
     * Process a message in stream, by inspecting headers, and forwarding the event. Actions are
     * surrounded in a tracing span approach.
     *
     * @param k message key;
     * @param v message payload;
     */
    @Override
    public void process(String k, GenericRecord v) {
        var span = tracingSpan();

        try (var scope = tracer.scopeManager().activate(span)) {
            var ravineKey = k;

            log.info("Processing event from topic '{}' with key '{}'", context.topic(), k);

            for (Header h : context.headers()) {
                var key = h.key();
                var value = new String(h.value());

                log.trace("Header: key='{}', value='{}'", key, value);
                if (AvroProducer.RAVINE_KEY.equals(key)) {
                    ravineKey = value;
                    log.debug("Using ravine-key '{}' from header entry on consumed record.",
                            ravineKey);
                }
            }

            publisher.publishEvent(new Event(this, ravineKey, v));
            context.commit();
        } finally {
            span.finish();
        }
    }

    /**
     * Empty close method.
     */
    @Override
    public void close() {
        // not implemented
    }

    /**
     * Extract the tracing span based in record headers.
     *
     * @return Span;
     */
    private Span tracingSpan() {
        var spanBuilder = tracer.buildSpan(this.getClass().getName());
        var spanContext = TracingKafkaUtils.extractSpanContext(context.headers(), tracer);

        if (spanContext != null) {
            spanBuilder.asChildOf(spanContext);
        }

        var span = spanBuilder.start();
        context.headers().forEach(h -> span.setTag(h.key(), new String(h.value())));
        return span;
    }
}
