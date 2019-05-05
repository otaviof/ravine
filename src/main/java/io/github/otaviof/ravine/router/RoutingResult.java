package io.github.otaviof.ravine.router;

import lombok.Getter;

/**
 * Wrapper to save results of a given endpoint route.
 */
@Getter
class RoutingResult {
    private int statusCode;
    private String payload;

    RoutingResult(int statusCode, String payload) {
        this.statusCode = statusCode;
        this.payload = payload;
    }
}
