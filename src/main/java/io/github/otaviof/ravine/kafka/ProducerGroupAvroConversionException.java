package io.github.otaviof.ravine.kafka;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public
class ProducerGroupAvroConversionException extends Exception {
    ProducerGroupAvroConversionException(String message) {
        super(message);
    }
}
