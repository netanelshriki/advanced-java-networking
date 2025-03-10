package com.network.api.http.middleware;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;

/**
 * Abstract base implementation of {@link HttpMiddleware}.
 * 
 * <p>This class provides a common implementation of middleware with
 * synchronous and asynchronous processing methods. It handles the
 * basic flow of the middleware chain and error handling.
 */
public abstract class AbstractHttpMiddleware implements HttpMiddleware {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpMiddleware.class);
    
    private final String name;
    private final int order;
    
    /**
     * Creates a new middleware with the specified name and default order.
     * 
     * @param name the middleware name
     */
    protected AbstractHttpMiddleware(String name) {
        this(name, 500); // Default middle priority
    }
    
    /**
     * Creates a new middleware with the specified name and order.
     * 
     * @param name the middleware name
     * @param order the middleware order
     */
    protected AbstractHttpMiddleware(String name, int order) {
        this.name = name;
        this.order = order;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    @Override
    public HttpResponse process(HttpRequest request, HttpMiddlewareChain chain) {
        HttpRequest processedRequest = request;
        
        try {
            // Pre-process request
            processedRequest = preProcess(request);
            
            // Continue the chain with the processed request
            HttpResponse response = chain.next(processedRequest);
            
            // Post-process response
            return postProcess(processedRequest, response);
        } catch (Exception e) {
            // Handle errors
            logger.error("Error in middleware '{}' while processing request: {}", getName(), e.getMessage(), e);
            return handleError(processedRequest, e);
        }
    }
    
    @Override
    public CompletableFuture<HttpResponse> processAsync(HttpRequest request, HttpAsyncMiddlewareChain chain) {
        try {
            // Pre-process request
            HttpRequest processedRequest = preProcess(request);
            
            // Continue the chain with the processed request
            return chain.next(processedRequest)
                .thenApply(response -> {
                    try {
                        // Post-process response
                        return postProcess(processedRequest, response);
                    } catch (Exception e) {
                        // Handle errors during post-processing
                        logger.error("Error in middleware '{}' while post-processing response: {}", getName(), e.getMessage(), e);
                        return handleError(processedRequest, e);
                    }
                })
                .exceptionally(e -> {
                    // Handle errors during chain execution
                    logger.error("Error in middleware '{}' chain execution: {}", getName(), e.getMessage(), e);
                    return handleError(processedRequest, e);
                });
        } catch (Exception e) {
            // Handle errors during pre-processing
            logger.error("Error in middleware '{}' while pre-processing request: {}", getName(), e.getMessage(), e);
            CompletableFuture<HttpResponse> future = new CompletableFuture<>();
            future.complete(handleError(request, e));
            return future;
        }
    }
    
    /**
     * Pre-processes the request before it is sent.
     * 
     * <p>Override this method to modify the request before it's passed
     * to the next middleware in the chain.
     * 
     * @param request the HTTP request
     * @return the processed request
     */
    protected HttpRequest preProcess(HttpRequest request) {
        return request;
    }
    
    /**
     * Post-processes the response after it is received.
     * 
     * <p>Override this method to modify the response before it's returned
     * to the previous middleware in the chain.
     * 
     * @param request the HTTP request that generated the response
     * @param response the HTTP response
     * @return the processed response
     */
    protected HttpResponse postProcess(HttpRequest request, HttpResponse response) {
        return response;
    }
    
    /**
     * Handles errors that occur during middleware processing.
     * 
     * <p>Override this method to provide custom error handling.
     * 
     * @param request the HTTP request that generated the error
     * @param error the error that occurred
     * @return the HTTP response to return, or null to propagate the error
     */
    protected HttpResponse handleError(HttpRequest request, Throwable error) {
        // By default, propagate the error
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else {
            throw new RuntimeException("Error in middleware '" + getName() + "'", error);
        }
    }
}