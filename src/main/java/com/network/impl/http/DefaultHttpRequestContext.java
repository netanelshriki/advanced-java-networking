package com.network.impl.http;

import com.network.api.http.HttpClient;
import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link HttpRequestContext} interface.
 */
class DefaultHttpRequestContext implements HttpRequestContext {

    private final HttpRequest request;
    private final HttpClient client;
    private final Map<String, Object> attributes;
    
    /**
     * Creates a new DefaultHttpRequestContext.
     * 
     * @param request the HTTP request
     * @param client  the HTTP client
     */
    DefaultHttpRequestContext(HttpRequest request, HttpClient client) {
        this.request = request;
        this.client = client;
        this.attributes = new HashMap<>();
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public HttpClient getClient() {
        return client;
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
}
