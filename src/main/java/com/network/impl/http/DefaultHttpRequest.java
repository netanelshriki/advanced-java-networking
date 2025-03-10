package com.network.impl.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequestContext;
import com.network.serialization.Serializer;
import com.network.serialization.SerializationException;

/**
 * Default implementation of {@link HttpRequest}.
 * 
 * <p>This class provides a concrete implementation of the HTTP request interface
 * with methods for accessing and manipulating request properties.
 */
public class DefaultHttpRequest implements HttpRequest {
    
    private final HttpMethod method;
    private final URI uri;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> pathParams;
    private final boolean followRedirects;
    private final Integer timeout;
    private final HttpRequestContext context;
    private final Serializer serializer;
    
    /**
     * Creates a new HTTP request.
     * 
     * @param builder the builder containing the request properties
     */
    DefaultHttpRequest(DefaultHttpRequestBuilder builder) {
        this.method = builder.method;
        this.uri = builder.buildUri();
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.body = builder.body;
        this.queryParams = Collections.unmodifiableMap(builder.queryParams);
        this.pathParams = Collections.unmodifiableMap(builder.pathParams);
        this.followRedirects = builder.followRedirects;
        this.timeout = builder.timeout;
        this.context = builder.context;
        this.serializer = builder.serializer;
    }
    
    @Override
    public HttpMethod getMethod() {
        return method;
    }
    
    @Override
    public URI getUri() {
        return uri;
    }
    
    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    @Override
    public Optional<String> getHeader(String name) {
        if (name == null) {
            return Optional.empty();
        }
        
        // Find header case-insensitively
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
                return Optional.of(entry.getValue().get(0));
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<String> getHeaderValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        // Find header case-insensitively
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return new ArrayList<>(entry.getValue());
            }
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public boolean hasBody() {
        return body != null && body.length > 0;
    }
    
    @Override
    public byte[] getBodyAsBytes() {
        return body != null ? body : new byte[0];
    }
    
    @Override
    public String getBodyAsString() {
        if (!hasBody()) {
            return "";
        }
        
        // Determine charset from content type
        String charset = "UTF-8";
        Optional<String> contentType = getContentType();
        if (contentType.isPresent()) {
            String ct = contentType.get().toLowerCase();
            int charsetIdx = ct.indexOf("charset=");
            if (charsetIdx != -1) {
                int startIdx = charsetIdx + 8;
                int endIdx = ct.indexOf(';', startIdx);
                if (endIdx == -1) {
                    endIdx = ct.length();
                }
                charset = ct.substring(startIdx, endIdx).trim();
            }
        }
        
        try {
            return new String(body, charset);
        } catch (Exception e) {
            // Fall back to UTF-8
            return new String(body);
        }
    }
    
    @Override
    public <T> T getBodyAs(Class<T> type) {
        if (!hasBody() || type == null || serializer == null) {
            return null;
        }
        
        try {
            return serializer.deserialize(body, type);
        } catch (SerializationException e) {
            throw new IllegalStateException("Failed to deserialize request body", e);
        }
    }
    
    @Override
    public Optional<String> getContentType() {
        return getHeader("Content-Type");
    }
    
    @Override
    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }
    
    @Override
    public Optional<String> getQueryParam(String name) {
        if (name == null) {
            return Optional.empty();
        }
        
        List<String> values = queryParams.get(name);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(values.get(0));
    }
    
    @Override
    public List<String> getQueryParamValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        List<String> values = queryParams.get(name);
        return values != null ? new ArrayList<>(values) : Collections.emptyList();
    }
    
    @Override
    public Map<String, String> getPathParams() {
        return pathParams;
    }
    
    @Override
    public Optional<String> getPathParam(String name) {
        if (name == null) {
            return Optional.empty();
        }
        
        String value = pathParams.get(name);
        return Optional.ofNullable(value);
    }
    
    @Override
    public boolean isFollowRedirects() {
        return followRedirects;
    }
    
    @Override
    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(timeout);
    }
    
    @Override
    public HttpRequestContext getContext() {
        return context;
    }
    
    /**
     * Gets the serializer associated with this request.
     * 
     * @return the serializer, or null if not set
     */
    Serializer getSerializer() {
        return serializer;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(' ').append(uri);
        
        if (!headers.isEmpty()) {
            sb.append("\nHeaders:");
            headers.forEach((name, values) -> {
                sb.append("\n  ").append(name).append(": ");
                if (values.size() == 1) {
                    sb.append(values.get(0));
                } else {
                    sb.append(values);
                }
            });
        }
        
        if (hasBody()) {
            sb.append("\nBody: ");
            // Limit body length for toString
            String bodyStr = getBodyAsString();
            if (bodyStr.length() > 1000) {
                sb.append(bodyStr, 0, 1000).append("... [truncated]");
            } else {
                sb.append(bodyStr);
            }
        }
        
        return sb.toString();
    }
}