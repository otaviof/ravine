package io.github.otaviof.ravine.router;

import io.github.otaviof.ravine.config.ResponseConfig;
import lombok.Getter;

/**
 * Wrapper to save results of a given endpoint route.
 */
@Getter
public class Response {
    private int statusCode;
    private String contentType;
    private String payload;

    public Response(int statusCode, String contentType, String payload) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.payload = payload;
    }

    public Response(ResponseConfig responseConfig) {
        this.statusCode = responseConfig.getHttpCode();
        this.contentType = responseConfig.getContentType();
        this.payload = responseConfig.getBody();
    }
}
