package com.network.impl.http;

import com.network.api.http.HttpMethod;
import com.network.api.http.HttpRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link HttpRequest} interface.
 */
class DefaultHttpRequest implements HttpRequest {

    private final URI uri;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Duration timeout;
    
    /**
     * Creates a new DefaultHttpRequest.
     * 
     * @param uri     the request URI
     * @param method  the HTTP method
     * @param headers the request headers
     * @param body    the request body
     * @param timeout the request timeout
     */
    DefaultHttpRequest(URI uri, HttpMethod method, Map<String, String> headers, byte[] body, Duration timeout) {
        this.uri = uri;
        this.method = method;
        this.headers = new HashMap<>(headers);
        this.body = body;
        this.timeout = timeout;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return method + " " + uri + ", headers: " + headers.size() + 
               ", body: " + (body == null ? "null" : body.length + " bytes");
    }
}
