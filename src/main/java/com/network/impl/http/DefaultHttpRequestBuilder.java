package com.network.impl.http;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.network.api.http.HttpAsyncRequestBuilder;
import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequest.HttpMethod;
import com.network.api.http.HttpRequestBuilder;
import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.TypedHttpRequestBuilder;
import com.network.exception.NetworkException;
import com.network.serialization.SerializationException;
import com.network.serialization.Serializer;

/**
 * Default implementation of {@link HttpRequestBuilder}.
 * 
 * <p>This class provides a concrete implementation of the HTTP request builder
 * interface for constructing HTTP requests.
 */
public class DefaultHttpRequestBuilder implements HttpRequestBuilder {
    
    private final DefaultHttpClient client;
    HttpMethod method = HttpMethod.GET;
    private URL baseUrl;
    private URI uri;
    private String path;
    final Map<String, List<String>> headers = new HashMap<>();
    final Map<String, List<String>> queryParams = new HashMap<>();
    final Map<String, String> pathParams = new HashMap<>();
    byte[] body;
    boolean followRedirects;
    Integer timeout;
    final HttpRequestContext context = new HttpRequestContext();
    Serializer serializer;
    
    /**
     * Creates a new HTTP request builder.
     * 
     * @param client the HTTP client that will execute the request
     */
    DefaultHttpRequestBuilder(DefaultHttpClient client) {
        this.client = client;
        this.baseUrl = client.getBaseUrl();
        this.followRedirects = client.getConfig().isFollowRedirects();
        
        // Add default headers
        client.getDefaultHeaders().forEach(this::header);
        
        // Set default content type and accept headers if not already set
        client.getConfig().getDefaultContentType().ifPresent(ct -> 
            headers.computeIfAbsent("Content-Type", k -> new ArrayList<>()).add(ct));
        
        client.getConfig().getDefaultAccept().ifPresent(a -> 
            headers.computeIfAbsent("Accept", k -> new ArrayList<>()).add(a));
        
        // Set default serializer
        this.serializer = client.getConfig().getSerializer().orElse(null);
    }
    
    @Override
    public HttpRequestBuilder method(HttpMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null");
        }
        this.method = method;
        return this;
    }
    
    @Override
    public HttpRequestBuilder url(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null");
        }
        try {
            this.uri = url.toURI();
            this.baseUrl = null;
            this.path = null;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
        return this;
    }
    
    @Override
    public HttpRequestBuilder uri(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI must not be null");
        }
        this.uri = uri;
        this.baseUrl = null;
        this.path = null;
        return this;
    }
    
    @Override
    public HttpRequestBuilder url(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null");
        }
        try {
            return url(new URL(url));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
    
    @Override
    public HttpRequestBuilder path(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        if (baseUrl == null) {
            throw new IllegalArgumentException("Base URL must be set before path");
        }
        this.path = path;
        this.uri = null;
        return this;
    }
    
    @Override
    public HttpRequestBuilder queryParam(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name must not be null");
        }
        queryParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }
    
    @Override
    public HttpRequestBuilder queryParams(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        params.forEach(this::queryParam);
        return this;
    }
    
    @Override
    public HttpRequestBuilder pathParam(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name must not be null");
        }
        pathParams.put(name, value);
        return this;
    }
    
    @Override
    public HttpRequestBuilder pathParams(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        pathParams.putAll(params);
        return this;
    }
    
    @Override
    public HttpRequestBuilder header(String name, String value) {
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
    
    @Override
    public HttpRequestBuilder headers(Map<String, String> headers) {
        if (headers == null) {
            throw new IllegalArgumentException("Headers must not be null");
        }
        headers.forEach(this::header);
        return this;
    }
    
    @Override
    public HttpRequestBuilder contentType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        headers.remove("Content-Type");
        headers.computeIfAbsent("Content-Type", k -> new ArrayList<>()).add(contentType);
        return this;
    }
    
    @Override
    public HttpRequestBuilder accept(String accept) {
        if (accept == null) {
            throw new IllegalArgumentException("Accept must not be null");
        }
        headers.remove("Accept");
        headers.computeIfAbsent("Accept", k -> new ArrayList<>()).add(accept);
        return this;
    }
    
    @Override
    public HttpRequestBuilder body(String body) {
        this.body = body != null ? body.getBytes(StandardCharsets.UTF_8) : null;
        return this;
    }
    
    @Override
    public HttpRequestBuilder body(byte[] body) {
        this.body = body;
        return this;
    }
    
    @Override
    public HttpRequestBuilder body(Object body) {
        if (body == null) {
            this.body = null;
            return this;
        }
        
        if (serializer == null) {
            throw new IllegalStateException("No serializer available for body serialization");
        }
        
        try {
            this.body = serializer.serialize(body);
            
            // Add content type if not set
            if (!headers.containsKey("Content-Type")) {
                headers.computeIfAbsent("Content-Type", k -> new ArrayList<>())
                    .add(serializer.getContentType());
            }
            
            return this;
        } catch (SerializationException e) {
            throw new IllegalArgumentException("Failed to serialize body", e);
        }
    }
    
    @Override
    public HttpRequestBuilder formParams(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Form parameters must not be null");
        }
        
        // Set content type to form-urlencoded
        contentType("application/x-www-form-urlencoded");
        
        // Build form data
        StringBuilder formData = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formData.length() > 0) {
                    formData.append('&');
                }
                formData.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            // Should never happen with UTF-8
            throw new IllegalStateException("UTF-8 encoding not supported", e);
        }
        
        return body(formData.toString());
    }
    
    @Override
    public HttpRequestBuilder serializer(Serializer serializer) {
        if (serializer == null) {
            throw new IllegalArgumentException("Serializer must not be null");
        }
        this.serializer = serializer;
        return this;
    }
    
    @Override
    public HttpRequestBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }
    
    @Override
    public HttpRequestBuilder timeout(int timeoutMillis) {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Timeout must not be negative");
        }
        this.timeout = timeoutMillis;
        return this;
    }
    
    @Override
    public HttpRequestBuilder attribute(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Attribute key must not be null");
        }
        context.setAttribute(key, value);
        return this;
    }
    
    @Override
    public <T> TypedHttpRequestBuilder<T> deserializeAs(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        return new DefaultTypedHttpRequestBuilder<>(this, type);
    }
    
    @Override
    public HttpResponse execute() throws NetworkException {
        return client.send(build());
    }
    
    @Override
    public HttpRequest build() {
        return new DefaultHttpRequest(this);
    }
    
    /**
     * Builds the URI for this request.
     * 
     * @return the built URI
     * @throws IllegalStateException if the URI cannot be built
     */
    URI buildUri() {
        if (uri != null) {
            return applyQueryParams(uri);
        }
        
        if (baseUrl == null) {
            throw new IllegalStateException("URL or base URL must be set");
        }
        
        if (path == null) {
            try {
                return applyQueryParams(baseUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid base URL: " + baseUrl, e);
            }
        }
        
        // Apply path parameters
        String resolvedPath = path;
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            resolvedPath = resolvedPath.replace(placeholder, 
                entry.getValue() != null ? entry.getValue() : "");
        }
        
        // Resolve path against base URL
        try {
            URI baseUri = baseUrl.toURI();
            
            // Ensure path starts with a slash
            if (!resolvedPath.startsWith("/") && !baseUri.getPath().endsWith("/")) {
                resolvedPath = "/" + resolvedPath;
            }
            
            // Build resolved URI
            URI resolvedUri = new URI(
                baseUri.getScheme(),
                baseUri.getUserInfo(),
                baseUri.getHost(),
                baseUri.getPort(),
                baseUri.getPath() + resolvedPath,
                null,
                null
            );
            
            return applyQueryParams(resolvedUri);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to build URI", e);
        }
    }
    
    /**
     * Applies query parameters to a URI.
     * 
     * @param uri the URI to apply parameters to
     * @return the URI with applied parameters
     */
    private URI applyQueryParams(URI uri) {
        if (queryParams.isEmpty()) {
            return uri;
        }
        
        try {
            StringBuilder query = new StringBuilder();
            if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                query.append(uri.getQuery());
            }
            
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                for (String value : entry.getValue()) {
                    if (query.length() > 0) {
                        query.append('&');
                    }
                    query.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(value != null ? value : "", "UTF-8"));
                }
            }
            
            return new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath(),
                query.toString(),
                uri.getFragment()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to apply query parameters", e);
        }
    }
    
    /**
     * Default implementation of {@link TypedHttpRequestBuilder}.
     * 
     * @param <T> the type to deserialize responses to
     */
    private class DefaultTypedHttpRequestBuilder<T> implements TypedHttpRequestBuilder<T> {
        
        private final DefaultHttpRequestBuilder builder;
        private final Class<T> type;
        
        /**
         * Creates a new typed HTTP request builder.
         * 
         * @param builder the parent builder
         * @param type the type to deserialize responses to
         */
        DefaultTypedHttpRequestBuilder(DefaultHttpRequestBuilder builder, Class<T> type) {
            this.builder = builder;
            this.type = type;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> method(HttpMethod method) {
            builder.method(method);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> url(URL url) {
            builder.url(url);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> uri(URI uri) {
            builder.uri(uri);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> url(String url) {
            builder.url(url);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> path(String path) {
            builder.path(path);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> queryParam(String name, String value) {
            builder.queryParam(name, value);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> queryParams(Map<String, String> params) {
            builder.queryParams(params);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> pathParam(String name, String value) {
            builder.pathParam(name, value);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> pathParams(Map<String, String> params) {
            builder.pathParams(params);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> header(String name, String value) {
            builder.header(name, value);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> headers(Map<String, String> headers) {
            builder.headers(headers);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> contentType(String contentType) {
            builder.contentType(contentType);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> accept(String accept) {
            builder.accept(accept);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> body(String body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> body(byte[] body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> body(Object body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> formParams(Map<String, String> params) {
            builder.formParams(params);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> serializer(Serializer serializer) {
            builder.serializer(serializer);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> followRedirects(boolean followRedirects) {
            builder.followRedirects(followRedirects);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> timeout(int timeoutMillis) {
            builder.timeout(timeoutMillis);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> attribute(String key, Object value) {
            builder.attribute(key, value);
            return this;
        }
        
        @Override
        public HttpResponse<T> execute() throws NetworkException {
            HttpRequest request = builder.build();
            HttpResponse response = client.send(request);
            return new DefaultHttpResponse<>(response, type, builder.serializer);
        }
        
        @Override
        public HttpRequest build() {
            return builder.build();
        }
    }
    
    /**
     * Default implementation of {@link HttpAsyncRequestBuilder}.
     */
    public class DefaultHttpAsyncRequestBuilder implements HttpAsyncRequestBuilder {
        
        private final DefaultHttpRequestBuilder builder;
        
        /**
         * Creates a new asynchronous HTTP request builder.
         * 
         * @param builder the synchronous builder to delegate to
         */
        DefaultHttpAsyncRequestBuilder(DefaultHttpRequestBuilder builder) {
            this.builder = builder;
        }
        
        @Override
        public HttpAsyncRequestBuilder method(HttpMethod method) {
            builder.method(method);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder url(URL url) {
            builder.url(url);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder uri(URI uri) {
            builder.uri(uri);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder url(String url) {
            builder.url(url);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder path(String path) {
            builder.path(path);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder queryParam(String name, String value) {
            builder.queryParam(name, value);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder queryParams(Map<String, String> params) {
            builder.queryParams(params);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder pathParam(String name, String value) {
            builder.pathParam(name, value);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder pathParams(Map<String, String> params) {
            builder.pathParams(params);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder header(String name, String value) {
            builder.header(name, value);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder headers(Map<String, String> headers) {
            builder.headers(headers);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder contentType(String contentType) {
            builder.contentType(contentType);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder accept(String accept) {
            builder.accept(accept);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder body(String body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder body(byte[] body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder body(Object body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder formParams(Map<String, String> params) {
            builder.formParams(params);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder serializer(Serializer serializer) {
            builder.serializer(serializer);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder followRedirects(boolean followRedirects) {
            builder.followRedirects(followRedirects);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder timeout(int timeoutMillis) {
            builder.timeout(timeoutMillis);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder attribute(String key, Object value) {
            builder.attribute(key, value);
            return this;
        }
        
        @Override
        public <V> DefaultTypedHttpAsyncRequestBuilder<V> deserializeAs(Class<V> type) {
            if (type == null) {
                throw new IllegalArgumentException("Type must not be null");
            }
            return new DefaultTypedHttpAsyncRequestBuilder<>(this, type);
        }
        
        @Override
        public CompletableFuture<HttpResponse> execute() {
            HttpRequest request = builder.build();
            return client.sendAsync(request);
        }
        
        @Override
        public HttpRequest build() {
            return builder.build();
        }
    }
    
    /**
     * Default implementation of {@link TypedHttpAsyncRequestBuilder}.
     * 
     * @param <T> the type to deserialize responses to
     */
    private class DefaultTypedHttpAsyncRequestBuilder<T> implements com.network.api.http.TypedHttpAsyncRequestBuilder<T> {
        
        private final DefaultHttpAsyncRequestBuilder builder;
        private final Class<T> type;
        
        /**
         * Creates a new typed asynchronous HTTP request builder.
         * 
         * @param builder the parent builder
         * @param type the type to deserialize responses to
         */
        DefaultTypedHttpAsyncRequestBuilder(DefaultHttpAsyncRequestBuilder builder, Class<T> type) {
            this.builder = builder;
            this.type = type;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> method(HttpMethod method) {
            builder.method(method);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> url(URL url) {
            builder.url(url);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> uri(URI uri) {
            builder.uri(uri);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> url(String url) {
            builder.url(url);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> path(String path) {
            builder.path(path);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> queryParam(String name, String value) {
            builder.queryParam(name, value);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> queryParams(Map<String, String> params) {
            builder.queryParams(params);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> pathParam(String name, String value) {
            builder.pathParam(name, value);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> pathParams(Map<String, String> params) {
            builder.pathParams(params);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> header(String name, String value) {
            builder.header(name, value);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> headers(Map<String, String> headers) {
            builder.headers(headers);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> contentType(String contentType) {
            builder.contentType(contentType);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> accept(String accept) {
            builder.accept(accept);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> body(String body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> body(byte[] body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> body(Object body) {
            builder.body(body);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> formParams(Map<String, String> params) {
            builder.formParams(params);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> serializer(Serializer serializer) {
            builder.serializer(serializer);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> followRedirects(boolean followRedirects) {
            builder.followRedirects(followRedirects);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> timeout(int timeoutMillis) {
            builder.timeout(timeoutMillis);
            return this;
        }
        
        @Override
        public com.network.api.http.TypedHttpAsyncRequestBuilder<T> attribute(String key, Object value) {
            builder.attribute(key, value);
            return this;
        }
        
        @Override
        public CompletableFuture<HttpResponse<T>> execute() {
            HttpRequest request = builder.build();
            return client.sendAsync(request)
                .thenApply(response -> new DefaultHttpResponse<>(response, type, DefaultHttpRequestBuilder.this.serializer));
        }
        
        @Override
        public HttpRequest build() {
            return builder.build();
        }
    }
}