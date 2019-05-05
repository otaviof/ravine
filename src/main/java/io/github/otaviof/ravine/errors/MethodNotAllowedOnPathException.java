package io.github.otaviof.ravine.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class MethodNotAllowedOnPathException extends Exception {
    public MethodNotAllowedOnPathException(String message) {
        super(message);
    }
}
