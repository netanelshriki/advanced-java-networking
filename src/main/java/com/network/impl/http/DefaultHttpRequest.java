package com.network.impl.http;

import com.network.api.http.HttpMethod;
import com.network.api.http.HttpRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the {@link HttpRequest} interface.
 */
class DefaultHttpRequest implements HttpRequest, MutableHttpRequest {

    private final URI uri;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Duration timeout;
    private final DefaultHttpClient client;
    
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
    
    /**
     * Creates a new DefaultHttpRequest with a client reference.
     * 
     * @param uri     the request URI
     * @param method  the HTTP method
     * @param headers the request headers
     * @param body    the request body
     * @param timeout the request timeout
     * @param client  the HTTP client that created this request
     */
    DefaultHttpRequest(URI uri, HttpMethod method, Map<String, String> headers, byte[] body, Duration timeout, DefaultHttpClient client) {
        this.uri = uri;
        this.method = method;
        this.headers = new ConcurrentHashMap<>(headers);
        this.body = body;
        this.timeout = timeout;
        this.client = client;
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
     * Gets the HTTP client that created this request.
     * 
     * @return the HTTP client, or null if not available
     */
    DefaultHttpClient getClient() {
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

/**
 * Interface for modifiable HTTP requests.
 * This is used by middleware to add headers to requests.
 */
interface MutableHttpRequest {
    /**
     * Adds a header to the request.
     * 
     * @param name  the header name
     * @param value the header value
     */
    void addHeader(String name, String value);
}
