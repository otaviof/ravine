package io.github.otaviof.ravine.router;

import lombok.Getter;
import org.apache.avro.generic.GenericRecord;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

/**
 * Event to carry on a generic Avro payload, plus metadata.
 */
@Getter
public class Event extends ApplicationEvent {
    private final String k;
    private final GenericRecord v;
    private Date createdAt;

    public Event(Object source, String k, GenericRecord v) {
        super(source);
        this.k = k;
        this.v = v;
        this.createdAt = new Date();
    }
}
