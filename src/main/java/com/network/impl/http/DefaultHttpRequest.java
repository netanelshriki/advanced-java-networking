package com.network.impl.http;

import com.network.api.http.HttpClient;
import com.network.api.http.HttpMethod;
import com.network.api.http.HttpRequest;
import com.network.middleware.http.CircuitBreakerMiddleware.MutableHttpRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link HttpRequest} interface.
 */
class DefaultHttpRequest implements HttpRequest, MutableHttpRequest {

    private final URI uri;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Duration timeout;
    private final HttpClient client;
    
    /**
     * Creates a new DefaultHttpRequest.
     * 
     * @param uri     the request URI
     * @param method  the HTTP method
     * @param headers the request headers
     * @param body    the request body
     * @param timeout the request timeout
     * @param client  the client that created this request
     */
    DefaultHttpRequest(URI uri, HttpMethod method, Map<String, String> headers, byte[] body, Duration timeout, HttpClient client) {
        this.uri = uri;
        this.method = method;
        this.headers = new HashMap<>(headers);
        this.body = body;
        this.timeout = timeout;
        this.client = client;
    }
    
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
        this(uri, method, headers, body, timeout, null);
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
    
    /**
     * Gets the client that created this request.
     * 
     * @return the client, or null if not available
     */
    HttpClient getClient() {
        return client;
    }

    @Override
    public String toString() {
        return method + " " + uri + ", headers: " + headers.size() + 
               ", body: " + (body == null ? "null" : body.length + " bytes");
    }
    
    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
}
