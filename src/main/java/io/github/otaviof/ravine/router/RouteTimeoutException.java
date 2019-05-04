package io.github.otaviof.ravine.router;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
class RouteTimeoutException extends RuntimeException {
    public RouteTimeoutException(String message) {
        super(message);
    }
}
