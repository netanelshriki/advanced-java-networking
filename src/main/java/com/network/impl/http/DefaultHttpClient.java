package com.network.impl.http;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.api.http.*;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.exception.NetworkException;
import com.network.middleware.http.CachingMiddleware;
import com.network.middleware.http.RetryMiddleware;
import com.network.serialization.Serializer;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link HttpClient} interface.
 * 
 * <p>This implementation is based on the Java 11 HttpClient API and provides
 * support for HTTP/1.1 and HTTP/2 protocols.</p>
 */
public class DefaultHttpClient implements HttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpClient.class);
    
    private final java.net.http.HttpClient httpClient;
    private final HttpClientConfig config;
    private final URL baseUrl;
    private final Map<String, String> defaultHeaders;
    private final List<HttpMiddleware> middlewares;
    private final Serializer serializer;
    private final List<ConnectionListener> connectionListeners;
    private final AtomicBoolean connected;
    
    /**
     * Creates a new DefaultHttpClient with the specified configuration.
     * 
     * @param config the client configuration
     */
    DefaultHttpClient(DefaultHttpClientConfig config) {
        this.config = config;
        this.baseUrl = config.getBaseUrl();
        this.defaultHeaders = new ConcurrentHashMap<>(config.getDefaultHeaders());
        this.middlewares = new ArrayList<>(config.getMiddlewares());
        this.serializer = config.getSerializer();
        this.connectionListeners = new ArrayList<>();
        this.connected = new AtomicBoolean(false);
        
        // Build Java HttpClient
        java.net.http.HttpClient.Builder builder = java.net.http.HttpClient.newBuilder();
        
        // Configure timeouts
        if (config.getConnectTimeout() != null) {
            builder.connectTimeout(config.getConnectTimeout());
        }
        
        // Configure proxy if set
        if (config.getProxy() != null) {
            builder.proxy(config.getProxy());
        }
        
        // Configure redirect policy
        if (config.isFollowRedirects()) {
            builder.followRedirects(java.net.http.HttpClient.Redirect.NORMAL);
        } else {
            builder.followRedirects(java.net.http.HttpClient.Redirect.NEVER);
        }
        
        // Configure executor service if provided
        if (config.getExecutor() != null) {
            builder.executor(config.getExecutor());
        }
        
        this.httpClient = builder.build();
    }
    
    @Override
    public URL getBaseUrl() {
        return baseUrl;
    }
    
    @Override
    public Map<String, String> getDefaultHeaders() {
        return Collections.unmodifiableMap(defaultHeaders);
    }
    
    @Override
    public HttpRequestBuilder request() {
        return new DefaultHttpRequestBuilder(this);
    }
    
    @Override
    public HttpAsyncRequestBuilder requestAsync() {
        return new DefaultHttpAsyncRequestBuilder(this);
    }
    
    @Override
    public HttpResponse send(HttpRequest request) throws NetworkException {
        try {
            // Create context for middleware
            HttpRequestContext context = new DefaultHttpRequestContext(request, this);
            
            // Apply request middleware
            try {
                for (HttpMiddleware middleware : middlewares) {
                    middleware.beforeRequest(context);
                }
            } catch (CachingMiddleware.CacheHitException e) {
                // Cache hit, return cached response
                LOG.debug("Cache hit, returning cached response");
                return e.getCachedResponse();
            } catch (RetryMiddleware.RetryException e) {
                // This shouldn't happen in beforeRequest, but just in case
                LOG.warn("Unexpected RetryException in beforeRequest", e);
                throw new NetworkException("Unexpected retry", e);
            }
            
            // Build Java HTTP request
            java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
                    .uri(request.getUri());
            
            // Add headers
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
            
            // Set method and body
            switch (request.getMethod()) {
                case GET:
                    requestBuilder.GET();
                    break;
                case POST:
                    if (request.getBody() != null) {
                        requestBuilder.POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(request.getBody()));
                    } else {
                        requestBuilder.POST(java.net.http.HttpRequest.BodyPublishers.noBody());
                    }
                    break;
                case PUT:
                    if (request.getBody() != null) {
                        requestBuilder.PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(request.getBody()));
                    } else {
                        requestBuilder.PUT(java.net.http.HttpRequest.BodyPublishers.noBody());
                    }
                    break;
                case DELETE:
                    requestBuilder.DELETE();
                    break;
                case PATCH:
                    if (request.getBody() != null) {
                        requestBuilder.method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofByteArray(request.getBody()));
                    } else {
                        requestBuilder.method("PATCH", java.net.http.HttpRequest.BodyPublishers.noBody());
                    }
                    break;
                case HEAD:
                    requestBuilder.method("HEAD", java.net.http.HttpRequest.BodyPublishers.noBody());
                    break;
                case OPTIONS:
                    requestBuilder.method("OPTIONS", java.net.http.HttpRequest.BodyPublishers.noBody());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
            }
            
            // Set timeout
            if (request.getTimeout() != null) {
                requestBuilder.timeout(request.getTimeout());
            } else if (config.getRequestTimeout() != null) {
                requestBuilder.timeout(config.getRequestTimeout());
            }
            
            HttpResponse response = null;
            int retryCount = 0;
            final int MAX_RETRIES = 5; // Safeguard against infinite loops
            
            while (response == null && retryCount < MAX_RETRIES) {
                try {
                    // Send request
                    java.net.http.HttpResponse<byte[]> javaResponse = httpClient.send(
                            requestBuilder.build(),
                            java.net.http.HttpResponse.BodyHandlers.ofByteArray()
                    );
                    
                    // Create response
                    DefaultHttpResponse httpResponse = new DefaultHttpResponse(
                            javaResponse.statusCode(),
                            javaResponse.body(),
                            convertHeaders(javaResponse.headers().map()),
                            javaResponse.uri(),
                            request
                    );
                    
                    // Apply response middleware in reverse order
                    try {
                        for (int i = middlewares.size() - 1; i >= 0; i--) {
                            middlewares.get(i).afterResponse(context, httpResponse);
                        }
                        response = httpResponse;
                    } catch (RetryMiddleware.RetryException e) {
                        // Middleware requested a retry
                        LOG.debug("Retry requested by middleware, attempt {}", retryCount + 1);
                        retryCount++;
                        // Continue loop to retry
                    }
                } catch (Exception e) {
                    // Store exception in context for middleware to use
                    context.setAttribute("exception", e);
                    
                    // Create a dummy response for middleware
                    DefaultHttpResponse errorResponse = new DefaultHttpResponse(
                            500, // Internal Server Error
                            null,
                            Collections.emptyMap(),
                            request.getUri(),
                            request
                    );
                    
                    // Apply response middleware to see if we should retry
                    try {
                        for (int i = middlewares.size() - 1; i >= 0; i--) {
                            middlewares.get(i).afterResponse(context, errorResponse);
                        }
                        // If we get here, no middleware requested a retry
                        throw new NetworkException("Failed to send HTTP request", e);
                    } catch (RetryMiddleware.RetryException re) {
                        // Middleware requested a retry
                        LOG.debug("Retry requested after exception, attempt {}", retryCount + 1);
                        retryCount++;
                        // Continue loop to retry
                    }
                }
            }
            
            if (response == null) {
                throw new NetworkException("Failed to send HTTP request after " + retryCount + " retries");
            }
            
            return response;
        } catch (Exception e) {
            if (e instanceof NetworkException) {
                throw (NetworkException) e;
            }
            throw new NetworkException("Failed to send HTTP request", e);
        }
    }
    
    @Override
    public CompletableFuture<HttpResponse> sendAsync(HttpRequest request) {
        CompletableFuture<HttpResponse> futureResponse = new CompletableFuture<>();
        
        // Create context for middleware
        HttpRequestContext context = new DefaultHttpRequestContext(request, this);
        
        // Apply request middleware
        try {
            for (HttpMiddleware middleware : middlewares) {
                middleware.beforeRequest(context);
            }
        } catch (CachingMiddleware.CacheHitException e) {
            // Cache hit, return cached response
            LOG.debug("Cache hit, returning cached response");
            return CompletableFuture.completedFuture(e.getCachedResponse());
        } catch (Exception e) {
            futureResponse.completeExceptionally(e);
            return futureResponse;
        }
        
        // Build Java HTTP request
        java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
                .uri(request.getUri());
        
        // Add headers
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }
        
        // Set method and body
        try {
            switch (request.getMethod()) {
                case GET:
                    requestBuilder.GET();
                    break;
                case POST:
                    if (request.getBody() != null) {
                        requestBuilder.POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(request.getBody()));
                    } else {
                        requestBuilder.POST(java.net.http.HttpRequest.BodyPublishers.noBody());
                    }
                    break;
                case PUT:
                    if (request.getBody() != null) {
                        requestBuilder.PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(request.getBody()));
                    } else {
                        requestBuilder.PUT(java.net.http.HttpRequest.BodyPublishers.noBody());
                    }
                    break;
                case DELETE:
                    requestBuilder.DELETE();
                    break;
                case PATCH:
                    if (request.getBody() != null) {
                        requestBuilder.method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofByteArray(request.getBody()));
                    } else {
                        requestBuilder.method("PATCH", java.net.http.HttpRequest.BodyPublishers.noBody());
                    }
                    break;
                case HEAD:
                    requestBuilder.method("HEAD", java.net.http.HttpRequest.BodyPublishers.noBody());
                    break;
                case OPTIONS:
                    requestBuilder.method("OPTIONS", java.net.http.HttpRequest.BodyPublishers.noBody());
                    break;
                default:
                    futureResponse.completeExceptionally(
                            new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod()));
                    return futureResponse;
            }
        } catch (Exception e) {
            futureResponse.completeExceptionally(e);
            return futureResponse;
        }
        
        // Set timeout
        if (request.getTimeout() != null) {
            requestBuilder.timeout(request.getTimeout());
        } else if (config.getRequestTimeout() != null) {
            requestBuilder.timeout(config.getRequestTimeout());
        }
        
        // Send request asynchronously
        httpClient.sendAsync(
                requestBuilder.build(),
                java.net.http.HttpResponse.BodyHandlers.ofByteArray()
        ).thenAccept(javaResponse -> {
            // Create response
            DefaultHttpResponse httpResponse = new DefaultHttpResponse(
                    javaResponse.statusCode(),
                    javaResponse.body(),
                    convertHeaders(javaResponse.headers().map()),
                    javaResponse.uri(),
                    request
            );
            
            // Apply response middleware in reverse order
            try {
                for (int i = middlewares.size() - 1; i >= 0; i--) {
                    middlewares.get(i).afterResponse(context, httpResponse);
                }
                futureResponse.complete(httpResponse);
            } catch (RetryMiddleware.RetryException e) {
                // Middleware requested a retry - for async, we'll handle this recursively
                LOG.debug("Retry requested by middleware for async request");
                
                // Recursive call to retry the request
                sendAsync(request).thenAccept(futureResponse::complete)
                                 .exceptionally(ex -> {
                                     futureResponse.completeExceptionally(ex);
                                     return null;
                                 });
            } catch (Exception e) {
                futureResponse.completeExceptionally(e);
            }
        }).exceptionally(e -> {
            // Store exception in context for middleware to use
            context.setAttribute("exception", e);
            
            // Create a dummy response for middleware
            DefaultHttpResponse errorResponse = new DefaultHttpResponse(
                    500, // Internal Server Error
                    null,
                    Collections.emptyMap(),
                    request.getUri(),
                    request
            );
            
            // Apply response middleware to see if we should retry
            try {
                for (int i = middlewares.size() - 1; i >= 0; i--) {
                    middlewares.get(i).afterResponse(context, errorResponse);
                }
                // If we get here, no middleware requested a retry
                futureResponse.completeExceptionally(new NetworkException("Failed to send HTTP request", e));
            } catch (RetryMiddleware.RetryException re) {
                // Middleware requested a retry - for async, we'll handle this recursively
                LOG.debug("Retry requested after exception for async request");
                
                // Recursive call to retry the request
                sendAsync(request).thenAccept(futureResponse::complete)
                                 .exceptionally(ex -> {
                                     futureResponse.completeExceptionally(ex);
                                     return null;
                                 });
            } catch (Exception ex) {
                futureResponse.completeExceptionally(new NetworkException("Failed to send HTTP request", e));
            }
            
            return null;
        });
        
        return futureResponse;
    }
    
    @Override
    public void connect() throws NetworkException {
        // HTTP clients are connectionless by nature, but we'll simulate a connection
        // to comply with the NetworkClient interface
        if (connected.compareAndSet(false, true)) {
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onConnect(getConnection());
                } catch (Exception e) {
                    // Ignore exceptions from listeners
                    LOG.warn("Exception in connection listener", e);
                }
            }
        }
    }
    
    @Override
    public CompletableFuture<Void> connectAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            connect();
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
    
    @Override
    public void disconnect() {
        // Simulate disconnection
        if (connected.compareAndSet(true, false)) {
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onDisconnect(getConnection());
                } catch (Exception e) {
                    // Ignore exceptions from listeners
                    LOG.warn("Exception in disconnection listener", e);
                }
            }
        }
    }
    
    @Override
    public CompletableFuture<Void> disconnectAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        disconnect();
        future.complete(null);
        return future;
    }
    
    @Override
    public boolean isConnected() {
        return connected.get();
    }
    
    @Override
    public Connection getConnection() {
        return isConnected() ? new HttpConnection(this) : null;
    }
    
    @Override
    public NetworkClient addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
        return this;
    }
    
    @Override
    public boolean removeConnectionListener(ConnectionListener listener) {
        return connectionListeners.remove(listener);
    }
    
    @Override
    public NetworkClient onConnect(Consumer<Connection> callback) {
        return addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                callback.accept(connection);
            }
            
            @Override
            public void onDisconnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onError(Throwable throwable) {
                // Do nothing
            }
        });
    }
    
    @Override
    public NetworkClient onDisconnect(Consumer<Connection> callback) {
        return addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onDisconnect(Connection connection) {
                callback.accept(connection);
            }
            
            @Override
            public void onError(Throwable throwable) {
                // Do nothing
            }
        });
    }
    
    @Override
    public NetworkClient onError(Consumer<Throwable> callback) {
        return addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onDisconnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onError(Throwable throwable) {
                callback.accept(throwable);
            }
        });
    }
    
    @Override
    public NetworkClient withConnectionTimeout(Duration timeout) {
        if (isConnected()) {
            throw new IllegalStateException("Cannot change connection timeout while connected");
        }
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        // For HTTP client, this is a no-op as the timeout is set in the constructor
        return this;
    }
    
    @Override
    public void close() {
        disconnect();
    }
    
    /**
     * Gets the serializer used by this client.
     * 
     * @return the serializer
     */
    Serializer getSerializer() {
        return serializer;
    }
    
    /**
     * Converts a map of list of strings to a map of strings by joining the lists.
     * 
     * @param multiMap the map of lists
     * @return a map with single string values
     */
    private Map<String, String> convertHeaders(Map<String, List<String>> multiMap) {
        Map<String, String> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, List<String>> entry : multiMap.entrySet()) {
            result.put(entry.getKey(), String.join(", ", entry.getValue()));
        }
        return result;
    }
    
    /**
     * Simple connection implementation for HTTP.
     */
    private static class HttpConnection implements Connection {
        private final HttpClient client;
        
        HttpConnection(HttpClient client) {
            this.client = client;
        }
        
        @Override
        public boolean isConnected() {
            return client.isConnected();
        }
        
        @Override
        public void close() {
            client.disconnect();
        }
    }
}
