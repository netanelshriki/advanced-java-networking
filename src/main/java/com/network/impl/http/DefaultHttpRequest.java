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
 * <p>This class provides a concrete implementation of the HTTP request
 * interface with immutable properties.
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
        
        List<String> values = headers.get(name.toLowerCase());
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(values.get(0));
    }
    
    @Override
    public List<String> getHeaderValues(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        
        List<String> values = headers.get(name.toLowerCase());
        if (values == null) {
            return Collections.emptyList();
        }
        
        return Collections.unmodifiableList(values);
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
        
        // Determine the charset from the Content-Type header
        Optional<String> contentType = getHeader("Content-Type");
        String charset = "UTF-8";
        if (contentType.isPresent()) {
            String value = contentType.get();
            int charsetIndex = value.toLowerCase().indexOf("charset=");
            if (charsetIndex != -1) {
                int endIndex = value.indexOf(';', charsetIndex);
                if (endIndex == -1) {
                    endIndex = value.length();
                }
                charset = value.substring(charsetIndex + 8, endIndex).trim();
            }
        }
        
        try {
            return new String(body, charset);
        } catch (Exception e) {
            // Fallback to UTF-8
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
        if (values == null) {
            return Collections.emptyList();
        }
        
        return Collections.unmodifiableList(values);
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
        private HttpRequestContext context = new HttpRequestContext();
        private Serializer serializer;
        
        /**
         * Creates a new builder with default values.
         */
        public Builder() {
            // Use default values
        }
        
        /**
         * Sets the HTTP method for the request.
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
         * Sets the URI for the request.
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
         * Adds a header to the request.
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
            
            String normalizedName = name.toLowerCase();
            if (value == null) {
                headers.remove(normalizedName);
            } else {
                List<String> values = headers.computeIfAbsent(normalizedName, k -> new ArrayList<>());
                values.add(value);
            }
            
            return this;
        }
        
        /**
         * Sets the headers for the request.
         * 
         * <p>This replaces any existing headers with the same names.
         * 
         * @param headers the headers
         * @return this builder
         * @throws IllegalArgumentException if headers is null
         */
        public Builder headers(Map<String, String> headers) {
            if (headers == null) {
                throw new IllegalArgumentException("Headers must not be null");
            }
            
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header(entry.getKey(), entry.getValue());
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
         * Sets the body of the request as a string.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder body(String body) {
            if (body == null) {
                this.body = null;
                return this;
            }
            
            this.body = body.getBytes();
            return this;
        }
        
        /**
         * Sets the body of the request as an object.
         * 
         * <p>The object is serialized using the request's serializer.
         * 
         * @param body the body
         * @return this builder
         * @throws IllegalStateException if no serializer is available
         */
        public Builder body(Object body) {
            if (body == null) {
                this.body = null;
                return this;
            }
            
            if (serializer == null) {
                throw new IllegalStateException("No serializer available for serialization");
            }
            
            try {
                this.body = serializer.serialize(body);
                
                // Set the Content-Type header if not already set
                if (!headers.containsKey("content-type")) {
                    header("Content-Type", serializer.getContentType());
                }
                
                return this;
            } catch (SerializationException e) {
                throw new IllegalStateException("Failed to serialize body", e);
            }
        }
        
        /**
         * Adds a query parameter to the request.
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
                List<String> values = queryParams.computeIfAbsent(name, k -> new ArrayList<>());
                values.add(value);
            }
            
            return this;
        }
        
        /**
         * Adds query parameters to the request.
         * 
         * @param params the parameters
         * @return this builder
         * @throws IllegalArgumentException if params is null
         */
        public Builder queryParams(Map<String, String> params) {
            if (params == null) {
                throw new IllegalArgumentException("Query parameters must not be null");
            }
            
            for (Map.Entry<String, String> entry : params.entrySet()) {
                queryParam(entry.getKey(), entry.getValue());
            }
            
            return this;
        }
        
        /**
         * Adds a path parameter to the request.
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
         * Adds path parameters to the request.
         * 
         * @param params the parameters
         * @return this builder
         * @throws IllegalArgumentException if params is null
         */
        public Builder pathParams(Map<String, String> params) {
            if (params == null) {
                throw new IllegalArgumentException("Path parameters must not be null");
            }
            
            pathParams.putAll(params);
            return this;
        }
        
        /**
         * Sets whether to follow redirects for the request.
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
         * @param timeoutMillis the timeout in milliseconds
         * @return this builder
         * @throws IllegalArgumentException if timeoutMillis is negative
         */
        public Builder timeout(int timeoutMillis) {
            if (timeoutMillis < 0) {
                throw new IllegalArgumentException("Timeout must not be negative");
            }
            this.timeout = timeoutMillis;
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
            if (key == null) {
                throw new IllegalArgumentException("Attribute key must not be null");
            }
            
            context.setAttribute(key, value);
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
         * Sets the content type header for the request.
         * 
         * @param contentType the content type
         * @return this builder
         * @throws IllegalArgumentException if contentType is null
         */
        public Builder contentType(String contentType) {
            if (contentType == null) {
                throw new IllegalArgumentException("Content type must not be null");
            }
            
            header("Content-Type", contentType);
            return this;
        }
        
        /**
         * Sets the accept header for the request.
         * 
         * @param accept the accept header value
         * @return this builder
         * @throws IllegalArgumentException if accept is null
         */
        public Builder accept(String accept) {
            if (accept == null) {
                throw new IllegalArgumentException("Accept must not be null");
            }
            
            header("Accept", accept);
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
}