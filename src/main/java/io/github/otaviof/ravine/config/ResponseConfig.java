package io.github.otaviof.ravine.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseConfig {
    @Min(100)
    @Max(599)
    private int httpCode = 200;

    private String contentType = "application/json";

    private String body = "";
}
