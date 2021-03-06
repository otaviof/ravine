package io.github.otaviof.ravine.kafka;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AvroProducerException extends Exception {
    public AvroProducerException(String message) {
        super(message);
    }
}
