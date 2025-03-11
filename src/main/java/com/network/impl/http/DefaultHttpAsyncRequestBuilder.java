package com.network.impl.http;

import com.network.api.http.HttpAsyncRequestBuilder;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpMethod;
import com.network.api.http.HttpResponse;
import com.network.api.http.TypedHttpAsyncRequestBuilder;
import com.network.serialization.Serializer;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of the {@link HttpAsyncRequestBuilder} interface.
 */
public class DefaultHttpAsyncRequestBuilder implements HttpAsyncRequestBuilder {

    private final HttpClient client;
    private String path;
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;
    private HttpMethod method;
    private Duration timeout;
    
    /**
     * Creates a new DefaultHttpAsyncRequestBuilder.
     * 
     * @param client the HTTP client
     */
    DefaultHttpAsyncRequestBuilder(HttpClient client) {
        this.client = client;
        
        // Copy default headers from client
        headers.putAll(client.getDefaultHeaders());
    }

    @Override
    public HttpAsyncRequestBuilder path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder queryParam(String name, String value) {
        queryParams.put(name, value);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder queryParams(Map<String, String> params) {
        queryParams.putAll(params);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder pathParam(String name, String value) {
        pathParams.put(name, value);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder pathParams(Map<String, String> params) {
        pathParams.putAll(params);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder contentType(String contentType) {
        headers.put("Content-Type", contentType);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder accept(String accept) {
        headers.put("Accept", accept);
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder body(byte[] body) {
        this.body = body;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder body(String body) {
        this.body = body.getBytes();
        return this;
    }

    @Override
    public <T> HttpAsyncRequestBuilder body(T body, Class<T> bodyClass) {
        if (body == null) {
            this.body = null;
            return this;
        }
        
        // Use the client's serializer if available
        Serializer serializer = getSerializer();
        if (serializer == null) {
            throw new IllegalStateException("No serializer configured");
        }
        
        this.body = serializer.serialize(body);
        
        // Set appropriate content type if not already set
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", serializer.getContentType());
        }
        
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder get() {
        this.method = HttpMethod.GET;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder post() {
        this.method = HttpMethod.POST;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder put() {
        this.method = HttpMethod.PUT;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder delete() {
        this.method = HttpMethod.DELETE;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder patch() {
        this.method = HttpMethod.PATCH;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder head() {
        this.method = HttpMethod.HEAD;
        return this;
    }

    @Override
    public HttpAsyncRequestBuilder options() {
        this.method = HttpMethod.OPTIONS;
        return this;
    }

    @Override
    public <T> TypedHttpAsyncRequestBuilder<T> deserializeAs(Class<T> responseType) {
        return new DefaultTypedHttpAsyncRequestBuilder<>(this, responseType);
    }

    @Override
    public CompletableFuture<HttpResponse> execute() {
        DefaultHttpRequest request = build();
        return client.sendAsync(request);
    }

    /**
     * Builds an HTTP request from the current state of this builder.
     * 
     * @return a new HTTP request
     */
    DefaultHttpRequest build() {
        if (method == null) {
            throw new IllegalStateException("HTTP method not set");
        }
        
        String resolvedPath = resolvePath();
        URI uri = buildUri(resolvedPath);
        
        return new DefaultHttpRequest(uri, method, headers, body, timeout);
    }
    
    /**
     * Gets the HTTP client associated with this builder.
     * 
     * @return the HTTP client
     */
    HttpClient getClient() {
        return client;
    }
    
    /**
     * Gets the serializer from the client.
     * 
     * @return the serializer, or null if not available
     */
    private Serializer getSerializer() {
        if (client instanceof DefaultHttpClient) {
            return ((DefaultHttpClient) client).getSerializer();
        }
        return null;
    }
    
    /**
     * Resolves path parameters in the path.
     * 
     * @return the resolved path
     */
    private String resolvePath() {
        if (path == null) {
            return "";
        }
        
        String resolvedPath = path;
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            resolvedPath = resolvedPath.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return resolvedPath;
    }
    
    /**
     * Builds a URI from the resolved path and query parameters.
     * 
     * @param resolvedPath the resolved path
     * @return the URI
     */
    private URI buildUri(String resolvedPath) {
        try {
            // If the path is absolute, use it directly
            if (resolvedPath.startsWith("http://") || resolvedPath.startsWith("https://")) {
                URI uri = new URI(resolvedPath);
                
                // Append query parameters if any
                if (!queryParams.isEmpty()) {
                    String query = buildQueryString();
                    return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                            uri.getPath(), query, uri.getFragment());
                }
                
                return uri;
            }
            
            // If the client has a base URL, resolve the path against it
            if (client.getBaseUrl() != null) {
                URI baseUri = client.getBaseUrl().toURI();
                
                // Ensure the path starts with a slash
                if (!resolvedPath.isEmpty() && !resolvedPath.startsWith("/")) {
                    resolvedPath = "/" + resolvedPath;
                }
                
                // Resolve the path against the base URL
                URI resolvedUri = baseUri.resolve(resolvedPath);
                
                // Append query parameters if any
                if (!queryParams.isEmpty()) {
                    String query = buildQueryString();
                    return new URI(resolvedUri.getScheme(), resolvedUri.getUserInfo(), resolvedUri.getHost(),
                            resolvedUri.getPort(), resolvedUri.getPath(), query, resolvedUri.getFragment());
                }
                
                return resolvedUri;
            }
            
            // If there's no base URL, treat the path as absolute
            String query = buildQueryString();
            return new URI(resolvedPath + (query.isEmpty() ? "" : "?" + query));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + resolvedPath, e);
        }
    }
    
    /**
     * Builds a query string from the query parameters.
     * 
     * @return the query string
     */
    private String buildQueryString() {
        if (queryParams.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        return sb.toString();
    }
}
