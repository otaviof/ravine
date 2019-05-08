package io.github.otaviof.ravine.router;

import java.util.Date;
import lombok.Getter;
import org.apache.avro.generic.GenericRecord;
import org.springframework.context.ApplicationEvent;

/**
 * Event to carry on a generic Avro payload, plus metadata.
 */
@Getter
public class Event extends ApplicationEvent {
    private final String k;
    private final transient GenericRecord v;
    private final Date createdAt;

    public Event(Object source, String k, GenericRecord v) {
        super(source);
        this.k = k;
        this.v = v;
        this.createdAt = new Date();
    }
}
