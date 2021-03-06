package io.github.otaviof.ravine.router;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RouterRouteNotFoundException extends Exception {
    RouterRouteNotFoundException(String message) {
        super(message);
    }
}
