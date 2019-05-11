package io.github.otaviof.ravine.router;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
public class RouterRouteTimeoutException extends Exception {
    RouterRouteTimeoutException(String message) {
        super(message);
    }
}
