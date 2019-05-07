package io.github.otaviof.ravine.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public
class AvroProducerException extends Throwable {
    public AvroProducerException(String message) {
        super(message);
    }
}
