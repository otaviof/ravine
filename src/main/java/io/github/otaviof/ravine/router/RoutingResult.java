package io.github.otaviof.ravine.router;

import lombok.Getter;

/**
 * Wrapper to save results of a given endpoint route.
 */
@Getter
public class RoutingResult {
    private int statusCode;
    private String contentType;
    private String payload;

    public RoutingResult(int statusCode, String contentType, String payload) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.payload = payload;
    }
}
