package io.github.otaviof.ravine.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
public class RouteTimeoutException extends Exception {
    public RouteTimeoutException(String message) {
        super(message);
    }
}
