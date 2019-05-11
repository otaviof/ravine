package io.github.otaviof.ravine.router;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class RouterRouteMethodNotAllowedException extends Exception {
    public RouterRouteMethodNotAllowedException(String message) {
        super(message);
    }
}
