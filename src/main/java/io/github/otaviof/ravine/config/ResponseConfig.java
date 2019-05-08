package io.github.otaviof.ravine.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.entity.ContentType;

@Getter
@Setter
public class ResponseConfig {
    @Min(100)
    @Max(599)
    private int httpCode = 200;

    private String contentType = ContentType.APPLICATION_JSON.toString();

    private String body = "";
}
