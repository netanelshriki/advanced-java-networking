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
 * <p>This class provides a concrete implementation of the HTTP request interface
 * with all required properties.
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
     * @param builder the builder containing the request properties
     */
    protected DefaultHttpRequest(Builder builder) {
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
        this.charset = builder.charset;
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
        
        // Headers are case-insensitive
        String normalizedName = name.toLowerCase();
        
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase().equals(normalizedName)) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    return Optional.of(values.get(0));
                }
                break;
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<String> getHeaderValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        // Headers are case-insensitive
        String normalizedName = name.toLowerCase();
        
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase().equals(normalizedName)) {
                List<String> values = entry.getValue();
                if (values != null) {
                    return Collections.unmodifiableList(values);
                }
                break;
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
        
        // Use specified charset or default to UTF-8
        Charset cs = charset != null ? charset : StandardCharsets.UTF_8;
        
        // Check if Content-Type header specifies a charset
        Optional<String> contentType = getHeader("Content-Type");
        if (contentType.isPresent()) {
            String ct = contentType.get();
            int charsetIndex = ct.toLowerCase().indexOf("charset=");
            if (charsetIndex != -1) {
                String charsetName = ct.substring(charsetIndex + 8).trim();
                // Remove any trailing parameters
                int endIndex = charsetName.indexOf(';');
                if (endIndex != -1) {
                    charsetName = charsetName.substring(0, endIndex).trim();
                }
                try {
                    cs = Charset.forName(charsetName);
                } catch (Exception e) {
                    // Ignore and use default charset
                }
            }
        }
        
        return new String(body, cs);
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
            throw new IllegalStateException("Failed to deserialize body as " + type.getName(), e);
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
     * Builder for {@link DefaultHttpRequest}.
     */
    public static class Builder {
        
        private HttpMethod method = HttpMethod.GET;
        private URI uri;
        private final Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;
        private final Map<String, List<String>> queryParams = new HashMap<>();
        private final Map<String, String> pathParams = new HashMap<>();
        private boolean followRedirects = true;
        private Integer timeout;
        private final HttpRequestContext context = new HttpRequestContext();
        private Serializer serializer;
        private Charset charset = StandardCharsets.UTF_8;
        
        /**
         * Creates a new builder with default values.
         */
        public Builder() {
            // Use default values
        }
        
        /**
         * Sets the HTTP method.
         * 
         * @param method the HTTP method
         * @return this builder
         * @throws IllegalArgumentException if method is null
         */
        public Builder method(HttpMethod method) {
            if (method == null) {
                throw new IllegalArgumentException("Method must not be null");
            }
            this.method = method;
            return this;
        }
        
        /**
         * Sets the URI.
         * 
         * @param uri the URI
         * @return this builder
         * @throws IllegalArgumentException if uri is null
         */
        public Builder uri(URI uri) {
            if (uri == null) {
                throw new IllegalArgumentException("URI must not be null");
            }
            this.uri = uri;
            return this;
        }
        
        /**
         * Adds a header.
         * 
         * @param name the header name
         * @param value the header value
         * @return this builder
         * @throws IllegalArgumentException if name is null
         */
        public Builder header(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("Header name must not be null");
            }
            
            if (value == null) {
                headers.remove(name);
            } else {
                headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            }
            
            return this;
        }
        
        /**
         * Sets the body as bytes.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }
        
        /**
         * Adds a query parameter.
         * 
         * @param name the parameter name
         * @param value the parameter value
         * @return this builder
         * @throws IllegalArgumentException if name is null
         */
        public Builder queryParam(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("Query parameter name must not be null");
            }
            
            if (value == null) {
                queryParams.remove(name);
            } else {
                queryParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            }
            
            return this;
        }
        
        /**
         * Adds a path parameter.
         * 
         * @param name the parameter name
         * @param value the parameter value
         * @return this builder
         * @throws IllegalArgumentException if name is null
         */
        public Builder pathParam(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("Path parameter name must not be null");
            }
            
            if (value == null) {
                pathParams.remove(name);
            } else {
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
         * Sets the timeout.
         * 
         * @param timeout the timeout in milliseconds
         * @return this builder
         * @throws IllegalArgumentException if timeout is negative
         */
        public Builder timeout(Integer timeout) {
            if (timeout != null && timeout < 0) {
                throw new IllegalArgumentException("Timeout must not be negative");
            }
            this.timeout = timeout;
            return this;
        }
        
        /**
         * Sets an attribute in the request context.
         * 
         * @param key the attribute key
         * @param value the attribute value
         * @return this builder
         * @throws IllegalArgumentException if key is null
         */
        public Builder attribute(String key, Object value) {
            context.setAttribute(key, value);
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
         * Sets the charset.
         * 
         * @param charset the charset
         * @return this builder
         * @throws IllegalArgumentException if charset is null
         */
        public Builder charset(Charset charset) {
            if (charset == null) {
                throw new IllegalArgumentException("Charset must not be null");
            }
            this.charset = charset;
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