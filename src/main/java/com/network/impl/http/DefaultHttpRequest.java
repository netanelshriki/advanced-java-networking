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
import com.network.serialization.SerializationException;
import com.network.serialization.Serializer;

/**
 * Default implementation of {@link HttpRequest}.
 * 
 * <p>This class provides a concrete implementation of the HTTP request
 * interface with support for headers, query parameters, and body.
 */
public class DefaultHttpRequest implements HttpRequest {
    
    private final HttpMethod method;
    private final URI uri;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final boolean followRedirects;
    private final Integer timeout;
    private final HttpRequestContext context;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> pathParams;
    private final Serializer serializer;
    private final Charset charset;
    
    /**
     * Creates a new HTTP request with the specified builder.
     * 
     * @param builder the builder containing the request values
     */
    public DefaultHttpRequest(DefaultHttpRequestBuilder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.headers = Collections.unmodifiableMap(convertHeaders(builder.headers));
        this.body = builder.body;
        this.followRedirects = builder.followRedirects;
        this.timeout = builder.timeout;
        this.context = builder.context != null ? builder.context : new HttpRequestContext();
        this.queryParams = Collections.unmodifiableMap(convertQueryParams(builder.queryParams));
        this.pathParams = Collections.unmodifiableMap(builder.pathParams);
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
        if (name == null) {
            return Optional.empty();
        }
        
        List<String> values = getHeaderValues(name);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(values.get(0));
    }
    
    @Override
    public List<String> getHeaderValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        // Headers are case-insensitive
        name = name.toLowerCase();
        
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase().equals(name)) {
                return entry.getValue();
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
        
        // Determine charset from Content-Type header, if present
        Charset bodyCharset = getCharsetFromContentType().orElse(charset);
        
        return new String(body, bodyCharset);
    }
    
    @Override
    public <T> T getBodyAs(Class<T> type) {
        if (!hasBody() || type == null) {
            return null;
        }
        
        if (serializer == null) {
            throw new IllegalStateException("No serializer available for deserialization");
        }
        
        try {
            return serializer.deserialize(body, type);
        } catch (SerializationException e) {
            throw new IllegalStateException("Failed to deserialize body", e);
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
        
        List<String> values = getQueryParamValues(name);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(values.get(0));
    }
    
    @Override
    public List<String> getQueryParamValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        return queryParams.getOrDefault(name, Collections.emptyList());
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
        
        return Optional.ofNullable(pathParams.get(name));
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
    public Serializer getSerializer() {
        return serializer;
    }
    
    /**
     * Gets the charset used for string conversions.
     * 
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }
    
    /**
     * Converts a map of header values to a map of header value lists.
     * 
     * @param headers the header values map
     * @return a map of header value lists
     */
    private Map<String, List<String>> convertHeaders(Map<String, String> headers) {
        Map<String, List<String>> result = new HashMap<>();
        
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            
            if (name != null && value != null) {
                List<String> values = result.computeIfAbsent(name, k -> new ArrayList<>());
                values.add(value);
            }
        }
        
        return result;
    }
    
    /**
     * Converts a map of query parameter values to a map of query parameter value lists.
     * 
     * @param params the query parameter values map
     * @return a map of query parameter value lists
     */
    private Map<String, List<String>> convertQueryParams(Map<String, String> params) {
        Map<String, List<String>> result = new HashMap<>();
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            
            if (name != null && value != null) {
                List<String> values = result.computeIfAbsent(name, k -> new ArrayList<>());
                values.add(value);
            }
        }
        
        return result;
    }
    
    /**
     * Extracts the charset from the Content-Type header, if present.
     * 
     * @return an Optional containing the charset, or empty if not found
     */
    private Optional<Charset> getCharsetFromContentType() {
        Optional<String> contentType = getContentType();
        if (!contentType.isPresent()) {
            return Optional.empty();
        }
        
        String contentTypeValue = contentType.get();
        int charsetIndex = contentTypeValue.toLowerCase().indexOf("charset=");
        if (charsetIndex == -1) {
            return Optional.empty();
        }
        
        String charsetName = contentTypeValue.substring(charsetIndex + 8).trim();
        int semicolonIndex = charsetName.indexOf(';');
        if (semicolonIndex != -1) {
            charsetName = charsetName.substring(0, semicolonIndex).trim();
        }
        
        try {
            return Optional.of(Charset.forName(charsetName));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}