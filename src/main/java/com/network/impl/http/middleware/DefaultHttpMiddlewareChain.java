package com.network.impl.http.middleware;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpAsyncMiddlewareChain;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.api.http.middleware.HttpMiddlewareChain;

/**
 * Default implementation of {@link HttpMiddlewareChain} and {@link HttpAsyncMiddlewareChain}.
 * 
 * <p>This class handles the execution of middleware chains for both synchronous
 * and asynchronous operations.
 */
public class DefaultHttpMiddlewareChain implements HttpMiddlewareChain, HttpAsyncMiddlewareChain {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpMiddlewareChain.class);
    
    private final List<HttpMiddleware> middleware;
    private final int index;
    private final HttpExecutor executor;
    
    /**
     * Creates a new middleware chain.
     * 
     * @param middleware the middleware to execute
     * @param executor the executor to use when no more middleware remain
     */
    public DefaultHttpMiddlewareChain(List<HttpMiddleware> middleware, HttpExecutor executor) {
        this(middleware, 0, executor);
    }
    
    /**
     * Creates a new middleware chain starting at the specified index.
     * 
     * @param middleware the middleware to execute
     * @param index the index to start at
     * @param executor the executor to use when no more middleware remain
     */
    private DefaultHttpMiddlewareChain(List<HttpMiddleware> middleware, int index, HttpExecutor executor) {
        this.middleware = middleware;
        this.index = index;
        this.executor = executor;
    }
    
    @Override
    public HttpResponse next(HttpRequest request) {
        if (index < middleware.size()) {
            HttpMiddleware currentMiddleware = middleware.get(index);
            logger.debug("Executing middleware: {}", currentMiddleware.getName());
            
            HttpMiddlewareChain nextChain = new DefaultHttpMiddlewareChain(middleware, index + 1, executor);
            return currentMiddleware.process(request, nextChain);
        } else {
            logger.debug("No more middleware, executing request");
            return executor.execute(request);
        }
    }
    
    @Override
    public CompletableFuture<HttpResponse> next(HttpRequest request) {
        if (index < middleware.size()) {
            HttpMiddleware currentMiddleware = middleware.get(index);
            logger.debug("Executing async middleware: {}", currentMiddleware.getName());
            
            HttpAsyncMiddlewareChain nextChain = new DefaultHttpMiddlewareChain(middleware, index + 1, executor);
            return currentMiddleware.processAsync(request, nextChain);
        } else {
            logger.debug("No more middleware, executing async request");
            return executor.executeAsync(request);
        }
    }
    
    /**
     * Interface for executing HTTP requests at the end of a middleware chain.
     */
    public interface HttpExecutor {
        
        /**
         * Executes an HTTP request synchronously.
         * 
         * @param request the request to execute
         * @return the response
         */
        HttpResponse execute(HttpRequest request);
        
        /**
         * Executes an HTTP request asynchronously.
         * 
         * @param request the request to execute
         * @return a CompletableFuture that completes with the response
         */
        CompletableFuture<HttpResponse> executeAsync(HttpRequest request);
    }
}