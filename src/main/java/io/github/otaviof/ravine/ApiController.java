package io.github.otaviof.ravine;

import io.github.otaviof.ravine.kafka.AvroProducerException;
import io.github.otaviof.ravine.kafka.ProducerGroupAvroConversionException;
import io.github.otaviof.ravine.router.Request;
import io.github.otaviof.ravine.router.Router;
import io.github.otaviof.ravine.router.RouterRouteMethodNotAllowedException;
import io.github.otaviof.ravine.router.RouterRouteNotFoundException;
import io.github.otaviof.ravine.router.RouterRouteTimeoutException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define endpoints accepted by this app.
 */
@EnableAutoConfiguration
@RestController
@Slf4j
public class ApiController {
    private final Router router;

    public ApiController(Router router) {
        this.router = router;
    }

    /**
     * Accept all POST requests send to Ravine, entry point to route a payload via Kafka and wait
     * for response to arrive.
     *
     * @param req servlet request;
     * @param body request body as array of bytes;
     * @return response entity based in a generic avro record;
     * @throws IOException on copying body buffer;
     * @throws RouterRouteNotFoundException on not being able to route based on path;
     * @throws RouterRouteMethodNotAllowedException http request method is not configured on path;
     * @throws AvroProducerException error on producing a message;
     * @throws RouterRouteTimeoutException timeout on waiting for response;
     */
    @RequestMapping(
            consumes = "application/json",
            method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public String handler(HttpServletRequest req, @RequestBody byte[] body, HttpServletResponse res)
            throws
            IOException,
            RouterRouteMethodNotAllowedException,
            AvroProducerException,
            RouterRouteNotFoundException,
            RouterRouteTimeoutException,
            ProducerGroupAvroConversionException {
        var request = new Request(req, body);

        log.info("Handling request for '{}' path, '{}' bytes", request.getPath(),
                request.getBody().length);

        var routingResult = router.route(request);
        res.setStatus(routingResult.getStatusCode());
        res.setContentType(routingResult.getContentType());

        return routingResult.getPayload();
    }
}
