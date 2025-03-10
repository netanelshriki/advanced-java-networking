package com.network.impl.http;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;
import com.network.api.http.HttpResponseException;
import com.network.serialization.Serializer;

/**
 * Default implementation of {@link HttpResponse}.
 * 
 * <p>This class represents an HTTP response with all associated data,
 * such as status code, headers, and body.
 * 
 * @param <T> the type of the deserialized body, or Void if not deserialized
 */
public class DefaultHttpResponse<T> implements HttpResponse<T> {
    
    private final int statusCode;
    private final String statusMessage;
    private final URI requestUri;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final T deserializedBody;
    private final HttpRequest request;
    private final Duration responseTime;
    private final Duration timeToFirstByte;
    private final Serializer serializer;
    
    /**
     * Creates a new HTTP response.
     * 
     * @param builder the builder with the response configuration
     */
    private DefaultHttpResponse(Builder<T> builder) {
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.requestUri = builder.requestUri;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.body = builder.body;
        this.deserializedBody = builder.deserializedBody;
        this.request = builder.request;
        this.responseTime = builder.responseTime;
        this.timeToFirstByte = builder.timeToFirstByte;
        this.serializer = builder.serializer;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String getStatusMessage() {
        return statusMessage;
    }
    
    @Override
    public URI getRequestUri() {
        return requestUri;
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
    public T getBody() {
        return deserializedBody;
    }
    
    @Override
    public <U> U getBodyAs(Class<U> type) {
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
        return getHeader("Content-Type");
    }
    
    @Override
    public Duration getResponseTime() {
        return responseTime;
    }
    
    @Override
    public Duration getTimeToFirstByte() {
        return timeToFirstByte;
    }
    
    @Override
    public HttpRequest getRequest() {
        return request;
    }
    
    @Override
    public HttpResponse<Void> asUntyped() {
        return new DefaultHttpResponse<>(new Builder<Void>()
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .requestUri(requestUri)
            .headers(headers)
            .body(body)
            .request(request)
            .responseTime(responseTime)
            .timeToFirstByte(timeToFirstByte)
            .serializer(serializer));
    }
    
    @Override
    public HttpResponse<T> throwOnError() throws HttpResponseException {
        if (isError()) {
            throw new HttpResponseException(this);
        }
        return this;
    }
    
    @Override
    public HttpResponse<T> assertStatusCode(int expectedStatusCode) throws HttpResponseException {
        if (statusCode != expectedStatusCode) {
            throw new HttpResponseException(this, "Expected status code " + expectedStatusCode + 
                " but got " + statusCode);
        }
        return this;
    }
    
    @Override
    public <U> HttpResponse<U> as(Class<U> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        
        U bodyAs = getBodyAs(type);
        
        return new DefaultHttpResponse<>(new Builder<U>()
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .requestUri(requestUri)
            .headers(headers)
            .body(body)
            .deserializedBody(bodyAs)
            .request(request)
            .responseTime(responseTime)
            .timeToFirstByte(timeToFirstByte)
            .serializer(serializer));
    }
    
    /**
     * Creates a new builder for {@link DefaultHttpResponse}.
     * 
     * @param <T> the type of the deserialized body
     * @return a new builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    /**
     * Builder for {@link DefaultHttpResponse}.
     * 
     * @param <T> the type of the deserialized body
     */
    public static class Builder<T> {
        
        private int statusCode;
        private String statusMessage = "";
        private URI requestUri;
        private final Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;
        private T deserializedBody;
        private HttpRequest request;
        private Duration responseTime = Duration.ZERO;
        private Duration timeToFirstByte = Duration.ZERO;
        private Serializer serializer;
        
        /**
         * Sets the status code.
         * 
         * @param statusCode the status code
         * @return this builder
         */
        public Builder<T> statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }
        
        /**
         * Sets the status message.
         * 
         * @param statusMessage the status message
         * @return this builder
         */
        public Builder<T> statusMessage(String statusMessage) {
            this.statusMessage = statusMessage != null ? statusMessage : "";
            return this;
        }
        
        /**
         * Sets the request URI.
         * 
         * @param requestUri the request URI
         * @return this builder
         */
        public Builder<T> requestUri(URI requestUri) {
            this.requestUri = requestUri;
            return this;
        }
        
        /**
         * Adds a header.
         * 
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        public Builder<T> header(String name, String value) {
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
        public Builder<T> headers(Map<String, List<String>> headers) {
            if (headers != null) {
                headers.forEach((name, values) -> {
                    if (values != null) {
                        values.forEach(value -> header(name, value));
                    }
                });
            }
            return this;
        }
        
        /**
         * Sets the response body.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder<T> body(byte[] body) {
            this.body = body;
            return this;
        }
        
        /**
         * Sets the response body as a string.
         * 
         * @param body the body
         * @param charset the charset to use for encoding
         * @return this builder
         */
        public Builder<T> body(String body, java.nio.charset.Charset charset) {
            if (body != null) {
                this.body = body.getBytes(charset != null ? charset : java.nio.charset.StandardCharsets.UTF_8);
            } else {
                this.body = null;
            }
            return this;
        }
        
        /**
         * Sets the response body as a string using UTF-8 encoding.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder<T> body(String body) {
            return body(body, java.nio.charset.StandardCharsets.UTF_8);
        }
        
        /**
         * Sets the deserialized body.
         * 
         * @param body the deserialized body
         * @return this builder
         */
        public Builder<T> deserializedBody(T body) {
            this.deserializedBody = body;
            return this;
        }
        
        /**
         * Sets the request that produced this response.
         * 
         * @param request the request
         * @return this builder
         */
        public Builder<T> request(HttpRequest request) {
            this.request = request;
            return this;
        }
        
        /**
         * Sets the response time.
         * 
         * @param responseTime the response time
         * @return this builder
         */
        public Builder<T> responseTime(Duration responseTime) {
            if (responseTime != null) {
                this.responseTime = responseTime;
            }
            return this;
        }
        
        /**
         * Sets the time to first byte.
         * 
         * @param timeToFirstByte the time to first byte
         * @return this builder
         */
        public Builder<T> timeToFirstByte(Duration timeToFirstByte) {
            if (timeToFirstByte != null) {
                this.timeToFirstByte = timeToFirstByte;
            }
            return this;
        }
        
        /**
         * Sets the serializer.
         * 
         * @param serializer the serializer
         * @return this builder
         */
        public Builder<T> serializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }
        
        /**
         * Builds the HTTP response.
         * 
         * @return the built response
         */
        public DefaultHttpResponse<T> build() {
            return new DefaultHttpResponse<>(this);
        }
    }
}