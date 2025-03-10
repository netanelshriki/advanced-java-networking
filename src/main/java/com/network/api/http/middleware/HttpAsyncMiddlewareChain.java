package com.network.api.http.middleware;

import java.util.concurrent.CompletableFuture;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;

/**
 * Interface for asynchronous HTTP middleware chains.
 * 
 * <p>An asynchronous middleware chain represents the remaining middleware to be executed
 * in the request processing pipeline. It allows middleware to control whether
 * to continue the chain or to short-circuit it, all in an asynchronous manner.
 */
public interface HttpAsyncMiddlewareChain {
    
    /**
     * Continues the middleware chain asynchronously.
     * 
     * <p>This method invokes the next middleware in the chain, or
     * executes the actual HTTP request if there are no more middleware.
     * 
     * @param request the HTTP request
     * @return a CompletableFuture that completes with the HTTP response
     */
    CompletableFuture<HttpResponse> next(HttpRequest request);
}