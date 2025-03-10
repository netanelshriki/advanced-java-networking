package com.network.api.http.middleware;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;

/**
 * Interface for HTTP middleware chains.
 * 
 * <p>A middleware chain represents the remaining middleware to be executed
 * in the request processing pipeline. It allows middleware to control whether
 * to continue the chain or to short-circuit it.
 */
public interface HttpMiddlewareChain {
    
    /**
     * Continues the middleware chain.
     * 
     * <p>This method invokes the next middleware in the chain, or
     * executes the actual HTTP request if there are no more middleware.
     * 
     * @param request the HTTP request
     * @return the HTTP response
     */
    HttpResponse next(HttpRequest request);
}