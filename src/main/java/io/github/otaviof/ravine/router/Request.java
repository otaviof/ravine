package io.github.otaviof.ravine.router;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;

/**
 * Represents a request, holds together the attributes needed to route it, and parse an actual
 * servlet request components, like parameters and headers. Here headers are meant to become Kafka
 * request headers, on which http-headers are part of, plus request parameters.
 */
@Getter
public class Request {
    public static final String SEPARATOR = ",";
    public static final String RAVINE_REQUEST_PARAMETER_NAMES = "ravine-request-parameter-names";
    public static final String RAVINE_REQUEST_HEADER_NAMES = "ravine-request-header-names";

    private final String method;
    private final String path;
    private final byte[] body;
    private final Map<String, String> headers;

    /**
     * Constructor. Creates a new Request with empty body.
     *
     * @param req servlet request;
     */
    public Request(HttpServletRequest req) {
        this.method = req.getMethod();
        this.path = req.getRequestURI().substring(req.getContextPath().length());
        this.headers = extractHeaders(req);
        this.body = "{}".getBytes();
    }

    /**
     * Constructor. Creates a new Request instance dealing with the body as well.
     *
     * @param req servlet request;
     * @param body body bytes;
     * @throws IOException on dealing with byte copy;
     */
    public Request(HttpServletRequest req, byte[] body) throws IOException {
        this.method = req.getMethod();
        this.path = req.getRequestURI().substring(req.getContextPath().length());
        this.body = trimBody(req, body);
        this.headers = extractHeaders(req);
    }

    /**
     * Time body bytes to the same size declared on header.
     *
     * @param req servlet request;
     * @param body body bytes;
     * @return trimmed body bytes;
     * @throws IOException on buffer copy error;
     */
    private byte[] trimBody(HttpServletRequest req, byte[] body) throws IOException {
        var length = req.getContentLength();
        var bos = new ByteArrayOutputStream(length >= 0 ? length : StreamUtils.BUFFER_SIZE);

        StreamUtils.copy(body, bos);

        return bos.toByteArray();
    }

    /**
     * Extract request headers and parameters from request, preparing data to become Kafka headers.
     *
     * @param req servlet request;
     * @return prepared map with headers and parameters;
     */
    private Map<String, String> extractHeaders(HttpServletRequest req) {
        var h = new HashMap<String, String>();
        var reqParamsNames = Collections.list(req.getParameterNames());
        var reqHeadersNames = Collections.list(req.getHeaderNames());

        // extract request parameters
        h.put(RAVINE_REQUEST_PARAMETER_NAMES, StringUtils.join(reqParamsNames, SEPARATOR));
        reqParamsNames.forEach(s -> h.put(s, req.getParameter(s)));

        // extract request headers
        h.put(RAVINE_REQUEST_HEADER_NAMES, StringUtils.join(reqHeadersNames, SEPARATOR));
        reqHeadersNames.forEach(s -> h.put(s, req.getHeader(s)));

        return h;
    }
}
