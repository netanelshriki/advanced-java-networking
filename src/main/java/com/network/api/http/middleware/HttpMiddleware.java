package com.network.api.http.middleware;

import java.util.concurrent.CompletableFuture;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;

/**
 * Interface for HTTP middleware.
 * 
 * <p>Middleware can be used to intercept and modify HTTP requests and responses.
 * This provides a way to add cross-cutting concerns like logging, metrics, authentication,
 * retries, etc. without modifying the core client code.
 * 
 * <p>Middleware are executed in a chain, with each middleware having the opportunity
 * to modify the request before it's sent, and to modify the response before it's returned
 * to the caller.
 */
public interface HttpMiddleware {
    
    /**
     * Processes an HTTP request and response.
     * 
     * <p>This method is called in the middleware chain. The middleware should:
     * <ol>
     *   <li>Optionally modify the request</li>
     *   <li>Call the next middleware in the chain</li>
     *   <li>Optionally modify the response</li>
     *   <li>Return the (possibly modified) response</li>
     * </ol>
     * 
     * <p>Note that the middleware chain is executed synchronously in order.
     * 
     * @param request the HTTP request
     * @param chain the next middleware in the chain
     * @return the HTTP response
     */
    HttpResponse process(HttpRequest request, HttpMiddlewareChain chain);
    
    /**
     * Processes an HTTP request and response asynchronously.
     * 
     * <p>This method is called in the middleware chain. The middleware should:
     * <ol>
     *   <li>Optionally modify the request</li>
     *   <li>Call the next middleware in the chain</li>
     *   <li>Optionally modify the response when the CompletableFuture completes</li>
     *   <li>Return the (possibly modified) response future</li>
     * </ol>
     * 
     * <p>Note that the middleware chain is executed asynchronously in order.
     * 
     * @param request the HTTP request
     * @param chain the next middleware in the chain
     * @return a CompletableFuture that completes with the HTTP response
     */
    CompletableFuture<HttpResponse> processAsync(HttpRequest request, HttpAsyncMiddlewareChain chain);
    
    /**
     * Gets the name of this middleware.
     * 
     * <p>The name is used for debugging and logging purposes.
     * 
     * @return the middleware name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
    
    /**
     * Gets the order of this middleware.
     * 
     * <p>Middleware are executed in ascending order of their order value.
     * Lower values execute earlier in the request processing phase, and later
     * in the response processing phase.
     * 
     * <p>Standard middleware order ranges:
     * <ul>
     *   <li>0-99: Tracing, logging</li>
     *   <li>100-199: Metrics</li>
     *   <li>200-299: Authentication</li>
     *   <li>300-399: Caching</li>
     *   <li>400-499: Retry</li>
     *   <li>500-599: Circuit breaking</li>
     *   <li>600-699: Timeout</li>
     *   <li>700-799: Rate limiting</li>
     *   <li>800-899: Request/response transformation</li>
     *   <li>900-999: Custom business logic</li>
     * </ul>
     * 
     * @return the middleware order
     */
    default int getOrder() {
        return 500; // Default middle priority
    }
}