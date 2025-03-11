package com.network.impl.http;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequestContext;
import com.network.serialization.Serializer;

/**
 * Default implementation of {@link HttpRequest}.
 * 
 * <p>This class provides a concrete implementation of the HTTP request interface
 * with all required functionality.
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
    private final Charset charset;
    
    /**
     * Creates a new HTTP request with the specified builder.
     * 
     * @param builder the builder containing the request values
     */
    DefaultHttpRequest(DefaultHttpRequestBuilder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.body = builder.body;
        this.queryParams = Collections.unmodifiableMap(new HashMap<>(builder.queryParams));
        this.pathParams = Collections.unmodifiableMap(new HashMap<>(builder.pathParams));
        this.followRedirects = builder.followRedirects;
        this.timeout = builder.timeout;
        this.context = builder.context;
        this.serializer = builder.serializer;
        this.charset = builder.charset != null ? builder.charset : StandardCharsets.UTF_8;
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
        List<String> values = getHeaderValues(name);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }
    
    @Override
    public List<String> getHeaderValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        // Case-insensitive header lookup
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return Collections.unmodifiableList(entry.getValue());
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
        
        return new String(body, getCharsetFromContentType().orElse(charset));
    }
    
    @Override
    public <T> T getBodyAs(Class<T> type) {
        if (!hasBody() || type == null) {
            return null;
        }
        
        try {
            return serializer.deserialize(body, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize body to " + type.getName(), e);
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
        List<String> values = getQueryParamValues(name);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }
    
    @Override
    public List<String> getQueryParamValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        List<String> values = queryParams.get(name);
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
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
     * Gets the serializer for this request.
     * 
     * @return the serializer, or null if not available
     */
    public Serializer getSerializer() {
        return serializer;
    }
    
    /**
     * Gets the charset for this request.
     * 
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }
    
    /**
     * Extracts the charset from the Content-Type header.
     * 
     * @return an Optional containing the charset, or empty if not found
     */
    private Optional<Charset> getCharsetFromContentType() {
        Optional<String> contentType = getContentType();
        if (!contentType.isPresent()) {
            return Optional.empty();
        }
        
        String value = contentType.get();
        int index = value.toLowerCase().indexOf("charset=");
        if (index == -1) {
            return Optional.empty();
        }
        
        try {
            String charsetName = value.substring(index + 8).trim();
            // Remove quotes if present
            if (charsetName.startsWith("\"") && charsetName.endsWith("\"")) {
                charsetName = charsetName.substring(1, charsetName.length() - 1);
            }
            // Handle ;
            int semicolonIndex = charsetName.indexOf(';');
            if (semicolonIndex != -1) {
                charsetName = charsetName.substring(0, semicolonIndex).trim();
            }
            
            return Optional.of(Charset.forName(charsetName));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(' ').append(uri);
        
        if (!headers.isEmpty()) {
            sb.append(" (Headers: ").append(headers.size()).append(')');
        }
        
        if (hasBody()) {
            sb.append(" (Body: ").append(body.length).append(" bytes)");
        }
        
        return sb.toString();
    }
}