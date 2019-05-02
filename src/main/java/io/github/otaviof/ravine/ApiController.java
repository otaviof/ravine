package io.github.otaviof.ravine;

import io.github.otaviof.ravine.kafka.AvroProducerException;
import io.github.otaviof.ravine.router.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Define endpoints accepted by this app.
 */
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
    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String post(HttpServletRequest req, @RequestBody byte[] body)
            throws IOException, AvroProducerException {
        var path = req.getRequestURI().substring(req.getContextPath().length());
        var contentLength = req.getContentLength();
        var bos = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : StreamUtils.BUFFER_SIZE);

        StreamUtils.copy(body, bos);

        log.info("Handling request for '{}' path", path);
        return router.route(path, bos.toByteArray());
    }

    public ApiController(Router router) {
        this.router = router;
    }
}
