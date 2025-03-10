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

/**
 * Default implementation of {@link HttpRequest}.
 * 
 * <p>This class represents an HTTP request with all associated data,
 * such as method, URI, headers, and body.
 */
public class DefaultHttpRequest implements HttpRequest {
    
    private final HttpMethod method;
    private final URI uri;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> pathParams;
    private final byte[] body;
    private final String contentType;
    private final boolean followRedirects;
    private final Integer timeout;
    private final HttpRequestContext context;
    private final Serializer serializer;
    
    /**
     * Creates a new HTTP request.
     * 
     * @param builder the builder with the request configuration
     */
    private DefaultHttpRequest(Builder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.queryParams = Collections.unmodifiableMap(builder.queryParams);
        this.pathParams = Collections.unmodifiableMap(builder.pathParams);
        this.body = builder.body;
        this.contentType = builder.contentType;
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
        
        // Case-insensitive header lookup
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
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
        
        // Use the serializer's charset to decode the bytes
        if (serializer != null && serializer instanceof com.network.serialization.json.JsonSerializer) {
            return new String(body, ((com.network.serialization.json.JsonSerializer) serializer).getCharset());
        }
        
        // Default to UTF-8
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }
    
    @Override
    public <T> T getBodyAs(Class<T> type) {
        if (!hasBody() || type == null) {
            return null;
        }
        
        if (serializer == null) {
            throw new IllegalStateException("No serializer configured for deserialization");
        }
        
        return serializer.deserialize(body, type);
    }
    
    @Override
    public Optional<String> getContentType() {
        if (contentType != null) {
            return Optional.of(contentType);
        }
        
        // Look in headers
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
        return values != null ? values : Collections.emptyList();
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
     * Creates a new builder for {@link DefaultHttpRequest}.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for {@link DefaultHttpRequest}.
     */
    public static class Builder {
        
        private HttpMethod method = HttpMethod.GET;
        private URI uri;
        private final Map<String, List<String>> headers = new HashMap<>();
        private final Map<String, List<String>> queryParams = new HashMap<>();
        private final Map<String, String> pathParams = new HashMap<>();
        private byte[] body;
        private String contentType;
        private boolean followRedirects = true;
        private Integer timeout;
        private HttpRequestContext context = new HttpRequestContext();
        private Serializer serializer;
        
        /**
         * Sets the HTTP method.
         * 
         * @param method the HTTP method
         * @return this builder
         */
        public Builder method(HttpMethod method) {
            if (method != null) {
                this.method = method;
            }
            return this;
        }
        
        /**
         * Sets the request URI.
         * 
         * @param uri the URI
         * @return this builder
         */
        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }
        
        /**
         * Adds a header.
         * 
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        public Builder header(String name, String value) {
            if (name != null) {
                headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value != null ? value : "");
            }
            return this;
        }
        
        /**
         * Adds headers.
         * 
         * @param headers the headers
         * @return this builder
         */
        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                headers.forEach(this::header);
            }
            return this;
        }
        
        /**
         * Adds a query parameter.
         * 
         * @param name the parameter name
         * @param value the parameter value
         * @return this builder
         */
        public Builder queryParam(String name, String value) {
            if (name != null) {
                queryParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value != null ? value : "");
            }
            return this;
        }
        
        /**
         * Adds query parameters.
         * 
         * @param params the parameters
         * @return this builder
         */
        public Builder queryParams(Map<String, String> params) {
            if (params != null) {
                params.forEach(this::queryParam);
            }
            return this;
        }
        
        /**
         * Adds a path parameter.
         * 
         * @param name the parameter name
         * @param value the parameter value
         * @return this builder
         */
        public Builder pathParam(String name, String value) {
            if (name != null) {
                pathParams.put(name, value != null ? value : "");
            }
            return this;
        }
        
        /**
         * Adds path parameters.
         * 
         * @param params the parameters
         * @return this builder
         */
        public Builder pathParams(Map<String, String> params) {
            if (params != null) {
                params.forEach(this::pathParam);
            }
            return this;
        }
        
        /**
         * Sets the request body.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }
        
        /**
         * Sets the request body as a string.
         * 
         * @param body the body
         * @param charset the charset to use for encoding
         * @return this builder
         */
        public Builder body(String body, java.nio.charset.Charset charset) {
            if (body != null) {
                this.body = body.getBytes(charset != null ? charset : java.nio.charset.StandardCharsets.UTF_8);
            } else {
                this.body = null;
            }
            return this;
        }
        
        /**
         * Sets the request body as a string using UTF-8 encoding.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder body(String body) {
            return body(body, java.nio.charset.StandardCharsets.UTF_8);
        }
        
        /**
         * Sets the request body as a serialized object.
         * 
         * @param body the body object
         * @param serializer the serializer to use
         * @return this builder
         */
        public Builder body(Object body, Serializer serializer) {
            if (body != null && serializer != null) {
                this.body = serializer.serialize(body);
                
                // Set content type if not already set
                if (this.contentType == null) {
                    this.contentType = serializer.getContentType();
                }
            } else {
                this.body = null;
            }
            return this;
        }
        
        /**
         * Sets the content type.
         * 
         * @param contentType the content type
         * @return this builder
         */
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            // Also set the Content-Type header
            if (contentType != null) {
                header("Content-Type", contentType);
            }
            return this;
        }
        
        /**
         * Sets whether to follow redirects.
         * 
         * @param followRedirects true to follow redirects, false otherwise
         * @return this builder
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }
        
        /**
         * Sets the request timeout.
         * 
         * @param timeoutMillis the timeout in milliseconds
         * @return this builder
         */
        public Builder timeout(int timeoutMillis) {
            if (timeoutMillis >= 0) {
                this.timeout = timeoutMillis;
            } else {
                this.timeout = null;
            }
            return this;
        }
        
        /**
         * Sets the request context.
         * 
         * @param context the context
         * @return this builder
         */
        public Builder context(HttpRequestContext context) {
            if (context != null) {
                this.context = context;
            }
            return this;
        }
        
        /**
         * Sets the serializer.
         * 
         * @param serializer the serializer
         * @return this builder
         */
        public Builder serializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }
        
        /**
         * Builds the HTTP request.
         * 
         * @return the built request
         * @throws IllegalStateException if the builder is not properly configured
         */
        public DefaultHttpRequest build() {
            if (uri == null) {
                throw new IllegalStateException("URI must not be null");
            }
            
            return new DefaultHttpRequest(this);
        }
    }
}