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
import com.network.serialization.SerializationException;
import com.network.serialization.Serializer;

/**
 * Default implementation of {@link HttpResponse}.
 * 
 * <p>This class provides a concrete implementation of the HTTP response
 * interface with common functionality for processing responses.
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
    private final String contentType;
    private final Serializer serializer;
    
    /**
     * Creates a new HTTP response with the specified parameters.
     * 
     * @param builder the builder containing the response data
     */
    protected DefaultHttpResponse(Builder<T> builder) {
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.requestUri = builder.requestUri;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.body = builder.body;
        this.deserializedBody = builder.deserializedBody;
        this.request = builder.request;
        this.responseTime = builder.responseTime;
        this.timeToFirstByte = builder.timeToFirstByte;
        this.contentType = extractContentType(builder.headers);
        this.serializer = builder.serializer;
    }
    
    /**
     * Extracts the content type from the response headers.
     * 
     * @param headers the response headers
     * @return the content type, or null if not found
     */
    private String extractContentType(Map<String, List<String>> headers) {
        List<String> contentTypeHeaders = headers.get("Content-Type");
        if (contentTypeHeaders != null && !contentTypeHeaders.isEmpty()) {
            return contentTypeHeaders.get(0);
        }
        
        // Try case-insensitive lookup
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if ("content-type".equalsIgnoreCase(entry.getKey())) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    return values.get(0);
                }
                break;
            }
        }
        
        return null;
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
        
        // Direct lookup
        List<String> values = headers.get(name);
        if (values != null) {
            return Collections.unmodifiableList(values);
        }
        
        // Case-insensitive lookup
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
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
        
        // Determine charset from content type
        String charset = "UTF-8";
        if (contentType != null) {
            int charsetIndex = contentType.toLowerCase().indexOf("charset=");
            if (charsetIndex != -1) {
                int endIndex = contentType.indexOf(';', charsetIndex);
                if (endIndex == -1) {
                    endIndex = contentType.length();
                }
                charset = contentType.substring(charsetIndex + 8, endIndex).trim();
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
    @SuppressWarnings("unchecked")
    public T getBody() {
        return deserializedBody;
    }
    
    @Override
    public <U> U getBodyAs(Class<U> type) {
        if (!hasBody() || type == null) {
            return null;
        }
        
        if (serializer == null) {
            throw new IllegalStateException("No serializer available to deserialize response body");
        }
        
        try {
            return serializer.deserialize(body, type);
        } catch (SerializationException e) {
            throw new IllegalStateException("Failed to deserialize response body to " + type.getName(), e);
        }
    }
    
    @Override
    public Optional<String> getContentType() {
        return Optional.ofNullable(contentType);
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
        return new Builder<Void>()
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .requestUri(requestUri)
            .headers(headers)
            .body(body)
            .request(request)
            .responseTime(responseTime)
            .timeToFirstByte(timeToFirstByte)
            .serializer(serializer)
            .build();
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
            throw new HttpResponseException(
                this, 
                "Expected status code " + expectedStatusCode + " but got " + statusCode);
        }
        return this;
    }
    
    @Override
    public <U> HttpResponse<U> as(Class<U> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        
        U convertedBody = getBodyAs(type);
        
        return new Builder<U>()
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .requestUri(requestUri)
            .headers(headers)
            .body(body)
            .deserializedBody(convertedBody)
            .request(request)
            .responseTime(responseTime)
            .timeToFirstByte(timeToFirstByte)
            .serializer(serializer)
            .build();
    }
    
    /**
     * Builder for {@link DefaultHttpResponse}.
     * 
     * @param <T> the type of the deserialized body
     */
    public static class Builder<T> {
        
        private int statusCode;
        private String statusMessage;
        private URI requestUri;
        private Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;
        private T deserializedBody;
        private HttpRequest request;
        private Duration responseTime = Duration.ZERO;
        private Duration timeToFirstByte = Duration.ZERO;
        private Serializer serializer;
        
        /**
         * Creates a new builder with default values.
         */
        public Builder() {
            // Default constructor
        }
        
        /**
         * Sets the status code of the response.
         * 
         * @param statusCode the status code
         * @return this builder
         */
        public Builder<T> statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }
        
        /**
         * Sets the status message of the response.
         * 
         * @param statusMessage the status message
         * @return this builder
         */
        public Builder<T> statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }
        
        /**
         * Sets the request URI of the response.
         * 
         * @param requestUri the request URI
         * @return this builder
         */
        public Builder<T> requestUri(URI requestUri) {
            this.requestUri = requestUri;
            return this;
        }
        
        /**
         * Sets the headers of the response.
         * 
         * @param headers the headers
         * @return this builder
         */
        public Builder<T> headers(Map<String, List<String>> headers) {
            if (headers != null) {
                this.headers = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    if (entry.getKey() != null) {
                        this.headers.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                    }
                }
            }
            return this;
        }
        
        /**
         * Adds a header to the response.
         * 
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        public Builder<T> header(String name, String value) {
            if (name != null) {
                List<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
                if (value != null) {
                    values.add(value);
                }
            }
            return this;
        }
        
        /**
         * Sets the body of the response as bytes.
         * 
         * @param body the body
         * @return this builder
         */
        public Builder<T> body(byte[] body) {
            this.body = body;
            return this;
        }
        
        /**
         * Sets the deserialized body of the response.
         * 
         * @param deserializedBody the deserialized body
         * @return this builder
         */
        public Builder<T> deserializedBody(T deserializedBody) {
            this.deserializedBody = deserializedBody;
            return this;
        }
        
        /**
         * Sets the request that produced the response.
         * 
         * @param request the request
         * @return this builder
         */
        public Builder<T> request(HttpRequest request) {
            this.request = request;
            return this;
        }
        
        /**
         * Sets the response time of the response.
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
         * Sets the time to first byte of the response.
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
         * Sets the serializer to use for deserializing the response body.
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
         * @return the built HTTP response
         */
        public HttpResponse<T> build() {
            return new DefaultHttpResponse<>(this);
        }
    }
}