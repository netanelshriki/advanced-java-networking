package com.network.api.http.middleware;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;

/**
 * Interface for HTTP middleware components.
 * 
 * <p>Middleware provides a mechanism to intercept and modify HTTP requests and responses.
 * Middleware components are executed in a pipeline, where each component can modify the
 * request before it is sent, and the response after it is received.
 * 
 * <p>Middleware is executed in the order it is added to the client for requests, and in
 * reverse order for responses. This allows for proper nesting of operations.
 */
public interface HttpMiddleware {

    /**
     * Called before a request is sent.
     * 
     * <p>This method can modify the request context before the request is sent.
     * 
     * @param context the request context
     */
    void beforeRequest(HttpRequestContext context);
    
    /**
     * Called after a response is received.
     * 
     * <p>This method can modify the response after it is received.
     * 
     * @param context the request context
     * @param response the response
     */
    void afterResponse(HttpRequestContext context, HttpResponse response);
}
