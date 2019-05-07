package io.github.otaviof.ravine;

import io.github.otaviof.ravine.router.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;

/**
 * Define endpoints accepted by this app.
 */
@EnableAutoConfiguration
@RestController
@Slf4j
public class ApiController {
    private final Router router;

    /**
     * Accept all POST requests send to Ravine, entry point to route a payload via Kafka
     * and wait for response to arrive.
     *
     * @param req  servlet request;
     * @param body request body as array of bytes;
     * @return response entity based in a generic avro record;
     */
    @RequestMapping(
            consumes = "application/json",
            produces = "application/json",
            method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public String handler(
            HttpServletRequest req,
            @RequestBody byte[] body,
            HttpServletResponse res
    ) throws Throwable {
        var path = req.getRequestURI().substring(req.getContextPath().length());
        var contentLength = req.getContentLength();
        var bos = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : StreamUtils.BUFFER_SIZE);

        StreamUtils.copy(body, bos);

        log.info("Handling request for '{}' path", path);

        var routingResult = router.route(req.getMethod(), path, bos.toByteArray());
        res.setStatus(routingResult.getStatusCode());

        return routingResult.getPayload();
    }

    public ApiController(Router router) {
        this.router = router;
    }
}
