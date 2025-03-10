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
 * with methods for accessing request properties.
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
     * Creates a new HTTP request with the specified properties.
     * 
     * @param builder the builder containing the request properties
     */
    public DefaultHttpRequest(DefaultHttpRequestBuilder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.headers = Collections.unmodifiableMap(builder.headers);
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
        
        // Case-insensitive header lookup
        String normalizedName = name.toLowerCase();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase().equals(normalizedName)) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    return Optional.of(values.get(0));
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<String> getHeaderValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        // Case-insensitive header lookup
        String normalizedName = name.toLowerCase();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase().equals(normalizedName)) {
                List<String> values = entry.getValue();
                if (values != null) {
                    return Collections.unmodifiableList(values);
                }
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
        return body != null ? body.clone() : new byte[0];
    }
    
    @Override
    public String getBodyAsString() {
        if (!hasBody()) {
            return "";
        }
        
        // Determine charset from Content-Type header
        String charset = "UTF-8";
        Optional<String> contentType = getHeader("Content-Type");
        if (contentType.isPresent()) {
            String value = contentType.get();
            int charsetIndex = value.toLowerCase().indexOf("charset=");
            if (charsetIndex != -1) {
                int start = charsetIndex + 8; // "charset=".length()
                int end = value.indexOf(';', start);
                if (end == -1) {
                    end = value.length();
                }
                charset = value.substring(start, end).trim();
            }
        }
        
        try {
            return new String(body, charset);
        } catch (Exception e) {
            // Fallback to UTF-8 if charset is invalid
            return new String(body);
        }
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
        if (values != null && !values.isEmpty()) {
            return Optional.of(values.get(0));
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<String> getQueryParamValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        List<String> values = queryParams.get(name);
        if (values != null) {
            return Collections.unmodifiableList(values);
        }
        
        return Collections.emptyList();
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
    public Serializer getSerializer() {
        return serializer;
    }
    
    @Override
    public String toString() {
        return method + " " + uri;
    }
    
    /**
     * Builder for creating {@link DefaultHttpRequest} instances.
     */
    public static class Builder {
        private HttpMethod method = HttpMethod.GET;
        private URI uri;
        private Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;
        private Map<String, List<String>> queryParams = new HashMap<>();
        private Map<String, String> pathParams = new HashMap<>();
        private boolean followRedirects = true;
        private Integer timeout;
        private HttpRequestContext context = new HttpRequestContext();
        private Serializer serializer;
        
        /**
         * Sets the HTTP method for the request.
         * 
         * @param method the HTTP method
         * @return this builder
         */
        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }
        
        /**
         * Sets the URI for the request.
         * 
         * @param uri the URI
         * @return this builder
         */
        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }
        
        /**
         * Adds a header to the request.
         * 
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        public Builder header(String name, String value) {
            if (name != null) {
                List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
                if (value != null) {
                    values.add(value);
                }
            }
            return this;
        }
        
        /**
         * Sets the body of the request as bytes.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }
        
        /**
         * Adds a query parameter to the request.
         * 
         * @param name the parameter name
         * @param value the parameter value
         * @return this builder
         */
        public Builder queryParam(String name, String value) {
            if (name != null) {
                List<String> values = queryParams.computeIfAbsent(name, k -> new ArrayList<>());
                if (value != null) {
                    values.add(value);
                }
            }
            return this;
        }
        
        /**
         * Adds a path parameter to the request.
         * 
         * @param name the parameter name
         * @param value the parameter value
         * @return this builder
         */
        public Builder pathParam(String name, String value) {
            if (name != null && value != null) {
                pathParams.put(name, value);
            }
            return this;
        }
        
        /**
         * Sets whether to follow redirects.
         * 
         * @param followRedirects true to follow redirects, false to not
         * @return this builder
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }
        
        /**
         * Sets the timeout for the request.
         * 
         * @param timeout the timeout in milliseconds
         * @return this builder
         */
        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }
        
        /**
         * Sets the context for the request.
         * 
         * @param context the context
         * @return this builder
         */
        public Builder context(HttpRequestContext context) {
            this.context = context != null ? context : new HttpRequestContext();
            return this;
        }
        
        /**
         * Sets the serializer for the request.
         * 
         * @param serializer the serializer
         * @return this builder
         */
        public Builder serializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }
        
        /**
         * Builds the request.
         * 
         * @return the built request
         */
        public DefaultHttpRequest build() {
            if (uri == null) {
                throw new IllegalStateException("URI must be set");
            }
            
            return new DefaultHttpRequest(this);
        }
    }
    
    /**
     * Creates a new builder for {@link DefaultHttpRequest}.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
}