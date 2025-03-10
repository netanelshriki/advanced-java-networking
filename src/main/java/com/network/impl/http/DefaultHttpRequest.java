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
import com.network.serialization.SerializationException;
import com.network.serialization.SerializerFactory;

/**
 * Default implementation of {@link HttpRequest}.
 * 
 * <p>This class provides a concrete implementation of the HTTP request
 * interface with all necessary functionality.
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
        
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                List<String> values = entry.getValue();
                if (!values.isEmpty()) {
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
        
        return new String(body, getCharset());
    }
    
    @Override
    public <T> T getBodyAs(Class<T> type) {
        if (!hasBody() || type == null) {
            return null;
        }
        
        Serializer serializer = getSerializer();
        if (serializer == null) {
            throw new IllegalStateException("No serializer available for deserializing body");
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
        if (serializer != null) {
            return serializer;
        }
        
        // Try to get serializer from content type
        Optional<String> contentType = getContentType();
        if (contentType.isPresent()) {
            return SerializerFactory.getInstance().getSerializer(contentType.get());
        }
        
        // Fall back to default serializer
        return SerializerFactory.getInstance().getDefaultSerializer();
    }
    
    /**
     * Gets the charset for this request.
     * 
     * @return the charset
     */
    public Charset getCharset() {
        // Try to get charset from content type
        Optional<String> contentType = getContentType();
        if (contentType.isPresent()) {
            String ct = contentType.get();
            int charsetIndex = ct.indexOf("charset=");
            if (charsetIndex >= 0) {
                try {
                    String charsetName = ct.substring(charsetIndex + 8).trim();
                    // Strip optional quotes
                    if (charsetName.startsWith("\"") && charsetName.endsWith("\"")) {
                        charsetName = charsetName.substring(1, charsetName.length() - 1);
                    }
                    return Charset.forName(charsetName);
                } catch (Exception e) {
                    // Ignore charset parsing errors
                }
            }
        }
        
        // Fall back to explicitly set charset or default to UTF-8
        return charset != null ? charset : StandardCharsets.UTF_8;
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
        private Charset charset;
        
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
         */
        public Builder method(HttpMethod method) {
            this.method = method;
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
                List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
                if (value != null) {
                    values.add(value);
                }
            }
            return this;
        }
        
        /**
         * Sets the request body.
         * 
         * @param body the body as bytes
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
         * Adds a path parameter.
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
         * Sets the request timeout.
         * 
         * @param timeout the timeout in milliseconds
         * @return this builder
         */
        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
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
         * Sets the charset for the request.
         * 
         * @param charset the charset
         * @return this builder
         */
        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }
        
        /**
         * Builds the HTTP request.
         * 
         * @return the built HTTP request
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