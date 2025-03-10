package com.network.api.http;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.network.api.NetworkClient;
import com.network.exception.NetworkException;

/**
 * Client for making HTTP requests.
 * 
 * <p>This interface defines the operations for making HTTP requests
 * and handling responses. It supports both synchronous and asynchronous
 * request execution.
 */
public interface HttpClient extends NetworkClient {
    
    /**
     * Gets the base URL for this client.
     * 
     * <p>All relative URLs will be resolved against this base URL.
     * 
     * @return the base URL, or null if not set
     */
    URL getBaseUrl();
    
    /**
     * Gets the default headers that will be sent with every request.
     * 
     * @return the default headers
     */
    Map<String, String> getDefaultHeaders();
    
    /**
     * Creates a new request builder for making HTTP requests.
     * 
     * @return a new request builder
     */
    HttpRequestBuilder request();
    
    /**
     * Creates a new request builder for making asynchronous HTTP requests.
     * 
     * @return a new asynchronous request builder
     */
    HttpAsyncRequestBuilder requestAsync();
    
    /**
     * Sends an HTTP request.
     * 
     * @param request the request to send
     * @return the response
     * @throws NetworkException if an error occurs
     */
    HttpResponse send(HttpRequest request) throws NetworkException;
    
    /**
     * Sends an HTTP request asynchronously.
     * 
     * @param request the request to send
     * @return a CompletableFuture that completes with the response
     */
    CompletableFuture<HttpResponse> sendAsync(HttpRequest request);
    
    /**
     * Factory method to create a new HTTP client builder.
     * 
     * @return a new HTTP client builder
     */
    static HttpClientBuilder builder() {
        // This will be implemented by a concrete factory class
        throw new UnsupportedOperationException("Not yet implemented");
    }
}