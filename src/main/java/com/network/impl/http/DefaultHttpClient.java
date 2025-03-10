package com.network.impl.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.api.http.HttpAsyncRequestBuilder;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpClientConfig;
import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequestBuilder;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.exception.NetworkException;
import com.network.impl.http.middleware.DefaultHttpMiddlewareChain;
import com.network.impl.http.middleware.DefaultHttpMiddlewareChain.HttpExecutor;
import com.network.serialization.Serializer;
import com.network.serialization.SerializerFactory;

/**
 * Default implementation of {@link HttpClient}.
 * 
 * <p>This class uses the Java 11+ HTTP client as the underlying implementation
 * and provides additional features like middleware, serialization, and request builders.
 */
public class DefaultHttpClient implements HttpClient, HttpExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpClient.class);
    
    private final HttpClientConfig config;
    private final java.net.http.HttpClient httpClient;
    private final ExecutorService executor;
    private final List<ConnectionListener> connectionListeners;
    private final Serializer serializer;
    
    /**
     * Creates a new HTTP client with the specified configuration.
     * 
     * @param config the client configuration
     */
    public DefaultHttpClient(HttpClientConfig config) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.executor = Executors.newCachedThreadPool();
        this.connectionListeners = Collections.synchronizedList(new ArrayList<>());
        this.serializer = config.getSerializer().orElseGet(SerializerFactory::getDefaultSerializer);
        
        // Create the underlying HTTP client
        java.net.http.HttpClient.Builder builder = java.net.http.HttpClient.newBuilder()
            .connectTimeout(config.getConnectionTimeout())
            .executor(executor)
            .version(Version.HTTP_2);
        
        // Configure redirects
        if (config.isFollowRedirects()) {
            builder.followRedirects(Redirect.NORMAL);
        } else {
            builder.followRedirects(Redirect.NEVER);
        }
        
        // Configure SSL
        if (config.isVerifySsl()) {
            config.getSslContext().ifPresent(builder::sslContext);
        } else {
            try {
                // Create a trust-all SSL context
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(null, new javax.net.ssl.TrustManager[] {
                    new javax.net.ssl.X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                    }
                }, new java.security.SecureRandom());
                builder.sslContext(sslContext);
            } catch (Exception e) {
                logger.warn("Failed to create trust-all SSL context", e);
            }
        }
        
        // Configure proxy
        config.getProxyHost().ifPresent(host -> {
            builder.proxy(java.net.ProxySelector.of(new java.net.InetSocketAddress(host, config.getProxyPort())));
        });
        
        this.httpClient = builder.build();
    }
    
    @Override
    public URL getBaseUrl() {
        return config.getBaseUrl().orElse(null);
    }
    
    @Override
    public Map<String, String> getDefaultHeaders() {
        return config.getDefaultHeaders();
    }
    
    @Override
    public HttpRequestBuilder request() {
        return new DefaultHttpRequestBuilder(this, serializer);
    }
    
    @Override
    public HttpAsyncRequestBuilder requestAsync() {
        return new DefaultHttpAsyncRequestBuilder(this, serializer);
    }
    
    @Override
    public HttpResponse send(HttpRequest request) throws NetworkException {
        try {
            // Create middleware chain
            DefaultHttpMiddlewareChain chain = new DefaultHttpMiddlewareChain(config.getMiddleware(), this);
            
            // Execute request through middleware chain
            return chain.next(request);
        } catch (Exception e) {
            if (e instanceof NetworkException) {
                throw (NetworkException) e;
            }
            throw new NetworkException("Failed to send request", e);
        }
    }
    
    @Override
    public CompletableFuture<HttpResponse> sendAsync(HttpRequest request) {
        try {
            // Create middleware chain
            DefaultHttpMiddlewareChain chain = new DefaultHttpMiddlewareChain(config.getMiddleware(), this);
            
            // Execute request through middleware chain
            return chain.next(request);
        } catch (Exception e) {
            CompletableFuture<HttpResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e instanceof NetworkException ? e : new NetworkException("Failed to send request", e));
            return future;
        }
    }
    
    @Override
    public void connect() throws NetworkException {
        // HTTP is connectionless, so this is a no-op
    }
    
    @Override
    public CompletableFuture<Void> connectAsync() {
        // HTTP is connectionless, so this is a no-op
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public void disconnect() {
        // HTTP is connectionless, so this is a no-op
    }
    
    @Override
    public CompletableFuture<Void> disconnectAsync() {
        // HTTP is connectionless, so this is a no-op
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public boolean isConnected() {
        // HTTP is connectionless, so we're always "connected"
        return true;
    }
    
    @Override
    public Connection getConnection() {
        // HTTP is connectionless, so there's no connection
        return null;
    }
    
    @Override
    public HttpClient addConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.add(listener);
        }
        return this;
    }
    
    @Override
    public boolean removeConnectionListener(ConnectionListener listener) {
        return connectionListeners.remove(listener);
    }
    
    @Override
    public HttpClient onConnect(Consumer<Connection> callback) {
        // HTTP is connectionless, so this is a no-op
        return this;
    }
    
    @Override
    public HttpClient onDisconnect(Consumer<Connection> callback) {
        // HTTP is connectionless, so this is a no-op
        return this;
    }
    
    @Override
    public HttpClient onError(Consumer<Throwable> callback) {
        if (callback != null) {
            addConnectionListener(new ConnectionListener() {
                @Override
                public void onConnect(Connection connection) {
                    // Not interested
                }
                
                @Override
                public void onDisconnect(Connection connection, String reason) {
                    // Not interested
                }
                
                @Override
                public void onDataReceived(Connection connection, byte[] data) {
                    // Not interested
                }
                
                @Override
                public void onDataSent(Connection connection, int size) {
                    // Not interested
                }
                
                @Override
                public void onError(Connection connection, Throwable throwable) {
                    callback.accept(throwable);
                }
            });
        }
        return this;
    }
    
    @Override
    public HttpClient withConnectionTimeout(Duration timeout) {
        // Cannot change timeout after creation
        throw new UnsupportedOperationException("Cannot change connection timeout after client creation");
    }
    
    @Override
    public void close() {
        executor.shutdown();
    }
    
    @Override
    public HttpResponse execute(HttpRequest request) {
        try {
            return executeRequest(request);
        } catch (Exception e) {
            // Notify listeners
            notifyError(null, e);
            
            if (e instanceof NetworkException) {
                throw (NetworkException) e;
            }
            throw new NetworkException("Failed to execute request", e);
        }
    }
    
    @Override
    public CompletableFuture<HttpResponse> executeAsync(HttpRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeRequest(request);
            } catch (Exception e) {
                // Notify listeners
                notifyError(null, e);
                
                if (e instanceof NetworkException) {
                    throw (NetworkException) e;
                }
                throw new NetworkException("Failed to execute request", e);
            }
        }, executor);
    }
    
    /**
     * Executes an HTTP request.
     * 
     * @param request the request to execute
     * @return the response
     * @throws Exception if an error occurs
     */
    private HttpResponse executeRequest(HttpRequest request) throws Exception {
        // Convert to Java HTTP request
        java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
            .uri(request.getUri())
            .timeout(request.getTimeout().map(Duration::ofMillis).orElse(config.getReadTimeout()));
        
        // Set method and body
        if (request.hasBody()) {
            BodyPublisher bodyPublisher = BodyPublishers.ofByteArray(request.getBodyAsBytes());
            requestBuilder.method(request.getMethod().name(), bodyPublisher);
        } else {
            requestBuilder.method(request.getMethod().name(), BodyPublishers.noBody());
        }
        
        // Set headers
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                requestBuilder.header(entry.getKey(), value);
            }
        }
        
        // Set default headers if not already set
        for (Map.Entry<String, String> entry : config.getDefaultHeaders().entrySet()) {
            if (!request.getHeaders().containsKey(entry.getKey())) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        
        // Set User-Agent if not already set
        if (!request.getHeaders().containsKey("User-Agent")) {
            config.getUserAgent().ifPresent(ua -> requestBuilder.header("User-Agent", ua));
        }
        
        // Build the request
        java.net.http.HttpRequest httpRequest = requestBuilder.build();
        
        // Track timing
        Instant startTime = Instant.now();
        Instant firstByteTime = null;
        
        // Send the request
        java.net.http.HttpResponse<byte[]> httpResponse = httpClient.send(
            httpRequest, BodyHandlers.ofByteArray());
        
        // Record time to first byte (approximation)
        firstByteTime = Instant.now();
        
        // Calculate response time
        Instant endTime = Instant.now();
        Duration responseTime = Duration.between(startTime, endTime);
        Duration timeToFirstByte = Duration.between(startTime, firstByteTime);
        
        // Extract headers
        Map<String, List<String>> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : httpResponse.headers().map().entrySet()) {
            headers.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        
        // Build the response
        DefaultHttpResponse.Builder<Void> responseBuilder = DefaultHttpResponse.builder()
            .statusCode(httpResponse.statusCode())
            .statusMessage(getStatusMessage(httpResponse.statusCode()))
            .requestUri(request.getUri())
            .headers(headers)
            .body(httpResponse.body())
            .request(request)
            .responseTime(responseTime)
            .timeToFirstByte(timeToFirstByte)
            .serializer(serializer);
        
        return responseBuilder.build();
    }
    
    /**
     * Gets the status message for a status code.
     * 
     * @param statusCode the status code
     * @return the status message
     */
    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 100: return "Continue";
            case 101: return "Switching Protocols";
            case 102: return "Processing";
            case 103: return "Early Hints";
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 203: return "Non-Authoritative Information";
            case 204: return "No Content";
            case 205: return "Reset Content";
            case 206: return "Partial Content";
            case 207: return "Multi-Status";
            case 208: return "Already Reported";
            case 226: return "IM Used";
            case 300: return "Multiple Choices";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 303: return "See Other";
            case 304: return "Not Modified";
            case 305: return "Use Proxy";
            case 307: return "Temporary Redirect";
            case 308: return "Permanent Redirect";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 402: return "Payment Required";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 406: return "Not Acceptable";
            case 407: return "Proxy Authentication Required";
            case 408: return "Request Timeout";
            case 409: return "Conflict";
            case 410: return "Gone";
            case 411: return "Length Required";
            case 412: return "Precondition Failed";
            case 413: return "Payload Too Large";
            case 414: return "URI Too Long";
            case 415: return "Unsupported Media Type";
            case 416: return "Range Not Satisfiable";
            case 417: return "Expectation Failed";
            case 418: return "I'm a teapot";
            case 421: return "Misdirected Request";
            case 422: return "Unprocessable Entity";
            case 423: return "Locked";
            case 424: return "Failed Dependency";
            case 425: return "Too Early";
            case 426: return "Upgrade Required";
            case 428: return "Precondition Required";
            case 429: return "Too Many Requests";
            case 431: return "Request Header Fields Too Large";
            case 451: return "Unavailable For Legal Reasons";
            case 500: return "Internal Server Error";
            case 501: return "Not Implemented";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            case 505: return "HTTP Version Not Supported";
            case 506: return "Variant Also Negotiates";
            case 507: return "Insufficient Storage";
            case 508: return "Loop Detected";
            case 510: return "Not Extended";
            case 511: return "Network Authentication Required";
            default: return "Unknown Status";
        }
    }
    
    /**
     * Resolves a path against the base URL.
     * 
     * @param path the path to resolve
     * @return the resolved URI
     */
    URI resolvePath(String path) {
        try {
            // If the path is already a full URL, use it as is
            if (path.startsWith("http://") || path.startsWith("https://")) {
                return new URI(path);
            }
            
            // If we have a base URL, resolve against it
            if (config.getBaseUrl().isPresent()) {
                URL baseUrl = config.getBaseUrl().get();
                
                // Ensure proper separator between base URL and path
                String basePath = baseUrl.getPath();
                if (!basePath.endsWith("/") && !path.startsWith("/")) {
                    path = "/" + path;
                } else if (basePath.endsWith("/") && path.startsWith("/")) {
                    path = path.substring(1);
                }
                
                // Combine base URL and path
                return new URI(baseUrl.getProtocol(), 
                               baseUrl.getUserInfo(), 
                               baseUrl.getHost(), 
                               baseUrl.getPort(), 
                               basePath + path, 
                               null, 
                               null);
            } else {
                // No base URL, use path as is
                return new URI(path);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path: " + path, e);
        }
    }
    
    /**
     * Replaces path parameters in a URI.
     * 
     * @param uri the URI with placeholders
     * @param pathParams the path parameters
     * @return the URI with placeholders replaced
     */
    URI replacePlaceholders(URI uri, Map<String, String> pathParams) {
        if (pathParams == null || pathParams.isEmpty()) {
            return uri;
        }
        
        String path = uri.getRawPath();
        
        // Replace placeholders in path
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            String pattern = "\\{" + entry.getKey() + "\\}";
            path = path.replaceAll(pattern, entry.getValue());
        }
        
        // Build new URI with replaced path
        try {
            return new URI(uri.getScheme(), 
                          uri.getUserInfo(), 
                          uri.getHost(), 
                          uri.getPort(), 
                          path, 
                          uri.getQuery(), 
                          uri.getFragment());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI after placeholder replacement", e);
        }
    }
    
    /**
     * Applies query parameters to a URI.
     * 
     * @param uri the URI
     * @param queryParams the query parameters
     * @return the URI with query parameters
     */
    URI applyQueryParams(URI uri, Map<String, List<String>> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return uri;
        }
        
        StringBuilder queryBuilder = new StringBuilder();
        
        // If the URI already has a query, start with it
        if (uri.getRawQuery() != null) {
            queryBuilder.append(uri.getRawQuery());
        }
        
        // Add the query parameters
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            for (String value : entry.getValue()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(encodeQueryParam(entry.getKey()))
                           .append("=")
                           .append(encodeQueryParam(value));
            }
        }
        
        // Build new URI with query parameters
        try {
            return new URI(uri.getScheme(), 
                          uri.getUserInfo(), 
                          uri.getHost(), 
                          uri.getPort(), 
                          uri.getPath(), 
                          queryBuilder.toString(), 
                          uri.getFragment());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI after query parameter application", e);
        }
    }
    
    /**
     * Encodes a query parameter.
     * 
     * @param param the parameter to encode
     * @return the encoded parameter
     */
    private String encodeQueryParam(String param) {
        if (param == null) {
            return "";
        }
        
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            return param;
        }
    }
    
    /**
     * Notifies all registered connection listeners of an error event.
     * 
     * @param connection the connection where the error occurred, or null if no connection
     * @param throwable the error that occurred
     */
    private void notifyError(Connection connection, Throwable throwable) {
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onError(connection, throwable);
            } catch (Exception e) {
                logger.error("Error notifying listener of error event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Request builder for the default HTTP client.
     */
    private class DefaultHttpRequestBuilder implements HttpRequestBuilder {
        
        private final DefaultHttpClient client;
        private final Serializer serializer;
        private final DefaultHttpRequest.Builder requestBuilder;
        
        /**
         * Creates a new request builder.
         * 
         * @param client the HTTP client
         * @param serializer the serializer to use
         */
        public DefaultHttpRequestBuilder(DefaultHttpClient client, Serializer serializer) {
            this.client = client;
            this.serializer = serializer;
            this.requestBuilder = DefaultHttpRequest.builder().serializer(serializer);
        }
        
        @Override
        public HttpRequestBuilder method(HttpMethod method) {
            requestBuilder.method(method);
            return this;
        }
        
        @Override
        public HttpRequestBuilder url(URL url) {
            try {
                requestBuilder.uri(url.toURI());
                return this;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URL: " + url, e);
            }
        }
        
        @Override
        public HttpRequestBuilder uri(URI uri) {
            requestBuilder.uri(uri);
            return this;
        }
        
        @Override
        public HttpRequestBuilder url(String url) {
            try {
                requestBuilder.uri(new URI(url));
                return this;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URL: " + url, e);
            }
        }
        
        @Override
        public HttpRequestBuilder path(String path) {
            URI uri = client.resolvePath(path);
            requestBuilder.uri(uri);
            return this;
        }
        
        @Override
        public HttpRequestBuilder queryParam(String name, String value) {
            requestBuilder.queryParam(name, value);
            return this;
        }
        
        @Override
        public HttpRequestBuilder queryParams(Map<String, String> params) {
            requestBuilder.queryParams(params);
            return this;
        }
        
        @Override
        public HttpRequestBuilder pathParam(String name, String value) {
            requestBuilder.pathParam(name, value);
            return this;
        }
        
        @Override
        public HttpRequestBuilder pathParams(Map<String, String> params) {
            requestBuilder.pathParams(params);
            return this;
        }
        
        @Override
        public HttpRequestBuilder header(String name, String value) {
            requestBuilder.header(name, value);
            return this;
        }
        
        @Override
        public HttpRequestBuilder headers(Map<String, String> headers) {
            requestBuilder.headers(headers);
            return this;
        }
        
        @Override
        public HttpRequestBuilder contentType(String contentType) {
            requestBuilder.contentType(contentType);
            return this;
        }
        
        @Override
        public HttpRequestBuilder accept(String accept) {
            requestBuilder.header("Accept", accept);
            return this;
        }
        
        @Override
        public HttpRequestBuilder body(String body) {
            requestBuilder.body(body);
            return this;
        }
        
        @Override
        public HttpRequestBuilder body(byte[] body) {
            requestBuilder.body(body);
            return this;
        }
        
        @Override
        public HttpRequestBuilder body(Object body) {
            requestBuilder.body(body, serializer);
            return this;
        }
        
        @Override
        public HttpRequestBuilder formParams(Map<String, String> params) {
            if (params == null || params.isEmpty()) {
                return this;
            }
            
            // Build form body
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formBody.length() > 0) {
                    formBody.append("&");
                }
                formBody.append(encodeQueryParam(entry.getKey()))
                       .append("=")
                       .append(encodeQueryParam(entry.getValue()));
            }
            
            // Set body and content type
            requestBuilder.body(formBody.toString());
            requestBuilder.contentType("application/x-www-form-urlencoded");
            
            return this;
        }
        
        @Override
        public HttpRequestBuilder serializer(Serializer serializer) {
            if (serializer != null) {
                requestBuilder.serializer(serializer);
            }
            return this;
        }
        
        @Override
        public HttpRequestBuilder followRedirects(boolean followRedirects) {
            requestBuilder.followRedirects(followRedirects);
            return this;
        }
        
        @Override
        public HttpRequestBuilder timeout(int timeoutMillis) {
            requestBuilder.timeout(timeoutMillis);
            return this;
        }
        
        @Override
        public HttpRequestBuilder attribute(String key, Object value) {
            requestBuilder.context(requestBuilder.context.setAttribute(key, value));
            return this;
        }
        
        @Override
        public <T> TypedHttpRequestBuilder<T> deserializeAs(Class<T> type) {
            return new DefaultTypedHttpRequestBuilder<>(this, type);
        }
        
        @Override
        public HttpResponse execute() throws NetworkException {
            HttpRequest request = build();
            return client.send(request);
        }
        
        @Override
        public HttpRequest build() {
            // Get the base URI
            URI uri = requestBuilder.uri;
            
            // Replace path parameters
            uri = client.replacePlaceholders(uri, requestBuilder.pathParams);
            
            // Apply query parameters
            uri = client.applyQueryParams(uri, requestBuilder.queryParams);
            
            // Update URI in builder
            requestBuilder.uri(uri);
            
            return requestBuilder.build();
        }
    }
    
    /**
     * Typed request builder for the default HTTP client.
     * 
     * @param <T> the type to deserialize to
     */
    private class DefaultTypedHttpRequestBuilder<T> implements TypedHttpRequestBuilder<T> {
        
        private final DefaultHttpRequestBuilder delegate;
        private final Class<T> type;
        
        /**
         * Creates a new typed request builder.
         * 
         * @param delegate the delegate builder
         * @param type the type to deserialize to
         */
        public DefaultTypedHttpRequestBuilder(DefaultHttpRequestBuilder delegate, Class<T> type) {
            this.delegate = delegate;
            this.type = type;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> method(HttpMethod method) {
            delegate.method(method);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> url(URL url) {
            delegate.url(url);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> uri(URI uri) {
            delegate.uri(uri);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> url(String url) {
            delegate.url(url);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> path(String path) {
            delegate.path(path);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> queryParam(String name, String value) {
            delegate.queryParam(name, value);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> queryParams(Map<String, String> params) {
            delegate.queryParams(params);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> pathParam(String name, String value) {
            delegate.pathParam(name, value);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> pathParams(Map<String, String> params) {
            delegate.pathParams(params);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> header(String name, String value) {
            delegate.header(name, value);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> headers(Map<String, String> headers) {
            delegate.headers(headers);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> contentType(String contentType) {
            delegate.contentType(contentType);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> accept(String accept) {
            delegate.accept(accept);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> body(String body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> body(byte[] body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> body(Object body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> formParams(Map<String, String> params) {
            delegate.formParams(params);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> serializer(Serializer serializer) {
            delegate.serializer(serializer);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> followRedirects(boolean followRedirects) {
            delegate.followRedirects(followRedirects);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> timeout(int timeoutMillis) {
            delegate.timeout(timeoutMillis);
            return this;
        }
        
        @Override
        public TypedHttpRequestBuilder<T> attribute(String key, Object value) {
            delegate.attribute(key, value);
            return this;
        }
        
        @Override
        public HttpResponse<T> execute() throws NetworkException {
            HttpRequest request = build();
            HttpResponse response = client.send(request);
            return response.as(type);
        }
        
        @Override
        public HttpRequest build() {
            return delegate.build();
        }
    }
    
    /**
     * Asynchronous request builder for the default HTTP client.
     */
    private class DefaultHttpAsyncRequestBuilder implements HttpAsyncRequestBuilder {
        
        private final DefaultHttpRequestBuilder delegate;
        
        /**
         * Creates a new asynchronous request builder.
         * 
         * @param client the HTTP client
         * @param serializer the serializer to use
         */
        public DefaultHttpAsyncRequestBuilder(DefaultHttpClient client, Serializer serializer) {
            this.delegate = new DefaultHttpRequestBuilder(client, serializer);
        }
        
        @Override
        public HttpAsyncRequestBuilder method(HttpMethod method) {
            delegate.method(method);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder url(URL url) {
            delegate.url(url);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder uri(URI uri) {
            delegate.uri(uri);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder url(String url) {
            delegate.url(url);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder path(String path) {
            delegate.path(path);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder queryParam(String name, String value) {
            delegate.queryParam(name, value);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder queryParams(Map<String, String> params) {
            delegate.queryParams(params);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder pathParam(String name, String value) {
            delegate.pathParam(name, value);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder pathParams(Map<String, String> params) {
            delegate.pathParams(params);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder header(String name, String value) {
            delegate.header(name, value);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder headers(Map<String, String> headers) {
            delegate.headers(headers);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder contentType(String contentType) {
            delegate.contentType(contentType);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder accept(String accept) {
            delegate.accept(accept);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder body(String body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder body(byte[] body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder body(Object body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder formParams(Map<String, String> params) {
            delegate.formParams(params);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder serializer(Serializer serializer) {
            delegate.serializer(serializer);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder followRedirects(boolean followRedirects) {
            delegate.followRedirects(followRedirects);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder timeout(int timeoutMillis) {
            delegate.timeout(timeoutMillis);
            return this;
        }
        
        @Override
        public HttpAsyncRequestBuilder attribute(String key, Object value) {
            delegate.attribute(key, value);
            return this;
        }
        
        @Override
        public <T> TypedHttpAsyncRequestBuilder<T> deserializeAs(Class<T> type) {
            return new DefaultTypedHttpAsyncRequestBuilder<>(this, type);
        }
        
        @Override
        public CompletableFuture<HttpResponse> execute() {
            HttpRequest request = build();
            return client.sendAsync(request);
        }
        
        @Override
        public HttpRequest build() {
            return delegate.build();
        }
    }
    
    /**
     * Typed asynchronous request builder for the default HTTP client.
     * 
     * @param <T> the type to deserialize to
     */
    private class DefaultTypedHttpAsyncRequestBuilder<T> implements TypedHttpAsyncRequestBuilder<T> {
        
        private final DefaultHttpAsyncRequestBuilder delegate;
        private final Class<T> type;
        
        /**
         * Creates a new typed asynchronous request builder.
         * 
         * @param delegate the delegate builder
         * @param type the type to deserialize to
         */
        public DefaultTypedHttpAsyncRequestBuilder(DefaultHttpAsyncRequestBuilder delegate, Class<T> type) {
            this.delegate = delegate;
            this.type = type;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> method(HttpMethod method) {
            delegate.method(method);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> url(URL url) {
            delegate.url(url);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> uri(URI uri) {
            delegate.uri(uri);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> url(String url) {
            delegate.url(url);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> path(String path) {
            delegate.path(path);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> queryParam(String name, String value) {
            delegate.queryParam(name, value);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> queryParams(Map<String, String> params) {
            delegate.queryParams(params);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> pathParam(String name, String value) {
            delegate.pathParam(name, value);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> pathParams(Map<String, String> params) {
            delegate.pathParams(params);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> header(String name, String value) {
            delegate.header(name, value);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> headers(Map<String, String> headers) {
            delegate.headers(headers);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> contentType(String contentType) {
            delegate.contentType(contentType);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> accept(String accept) {
            delegate.accept(accept);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> body(String body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> body(byte[] body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> body(Object body) {
            delegate.body(body);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> formParams(Map<String, String> params) {
            delegate.formParams(params);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> serializer(Serializer serializer) {
            delegate.serializer(serializer);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> followRedirects(boolean followRedirects) {
            delegate.followRedirects(followRedirects);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> timeout(int timeoutMillis) {
            delegate.timeout(timeoutMillis);
            return this;
        }
        
        @Override
        public TypedHttpAsyncRequestBuilder<T> attribute(String key, Object value) {
            delegate.attribute(key, value);
            return this;
        }
        
        @Override
        public CompletableFuture<HttpResponse<T>> execute() {
            HttpRequest request = build();
            return client.sendAsync(request).thenApply(response -> response.as(type));
        }
        
        @Override
        public HttpRequest build() {
            return delegate.build();
        }
    }
}