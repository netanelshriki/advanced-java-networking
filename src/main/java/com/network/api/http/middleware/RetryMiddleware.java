package com.network.api.http.middleware;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequest.HttpMethod;
import com.network.api.http.HttpResponse;
import com.network.config.NetworkConfig.RetryBackoffStrategy;
import com.network.exception.NetworkException;

/**
 * Middleware for automatically retrying failed HTTP requests.
 * 
 * <p>This middleware intercepts HTTP responses and retries the request
 * if it meets the configured retry criteria, such as specific status codes
 * or exception types.
 */
public class RetryMiddleware extends AbstractHttpMiddleware {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryMiddleware.class);
    
    private final int maxRetries;
    private final RetryBackoffStrategy backoffStrategy;
    private final Duration initialBackoff;
    private final Set<Integer> retryStatusCodes;
    private final Set<Class<? extends Throwable>> retryExceptions;
    private final Predicate<HttpResponse> retryPredicate;
    private final Set<HttpMethod> retryMethods;
    private final ScheduledExecutorService scheduler;
    
    /**
     * Creates a new retry middleware with default settings.
     * 
     * <p>By default, retries up to 3 times with exponential backoff
     * starting at 100ms, for 5xx server errors and network exceptions.
     */
    public RetryMiddleware() {
        this(3, RetryBackoffStrategy.EXPONENTIAL, Duration.ofMillis(100),
             new HashSet<>(Arrays.asList(500, 502, 503, 504)),
             new HashSet<>(Arrays.asList(NetworkException.class)),
             null,
             new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.DELETE)));
    }
    
    /**
     * Creates a new retry middleware with the specified settings.
     * 
     * @param maxRetries the maximum number of retry attempts
     * @param backoffStrategy the backoff strategy to use
     * @param initialBackoff the initial backoff duration
     * @param retryStatusCodes the HTTP status codes to retry
     * @param retryExceptions the exception types to retry
     * @param retryPredicate additional predicate for retry decision
     * @param retryMethods the HTTP methods to retry
     */
    public RetryMiddleware(int maxRetries, RetryBackoffStrategy backoffStrategy, Duration initialBackoff,
                          Set<Integer> retryStatusCodes, Set<Class<? extends Throwable>> retryExceptions,
                          Predicate<HttpResponse> retryPredicate, Set<HttpMethod> retryMethods) {
        super("RetryMiddleware", 450); // High priority, but after logging
        this.maxRetries = maxRetries;
        this.backoffStrategy = backoffStrategy;
        this.initialBackoff = initialBackoff;
        this.retryStatusCodes = retryStatusCodes != null ? retryStatusCodes : new HashSet<>();
        this.retryExceptions = retryExceptions != null ? retryExceptions : new HashSet<>();
        this.retryPredicate = retryPredicate;
        this.retryMethods = retryMethods != null ? retryMethods : new HashSet<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    @Override
    public HttpResponse process(HttpRequest request, HttpMiddlewareChain chain) {
        // Skip retries if method is not in retry methods
        if (!shouldRetryMethod(request.getMethod())) {
            return chain.next(request);
        }
        
        int attempt = 0;
        Throwable lastException = null;
        HttpResponse lastResponse = null;
        
        while (attempt <= maxRetries) {
            try {
                // Attempt the request
                HttpResponse response = chain.next(request);
                
                // Check if we should retry based on the response
                if (attempt < maxRetries && shouldRetry(response)) {
                    lastResponse = response;
                    attempt++;
                    
                    // Wait for the backoff duration
                    Duration backoff = calculateBackoff(attempt);
                    logger.debug("Retrying request to {} after {}ms (attempt {}/{})",
                                request.getUri(), backoff.toMillis(), attempt, maxRetries);
                    
                    try {
                        Thread.sleep(backoff.toMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", e);
                    }
                    
                    continue;
                }
                
                // No need to retry
                return response;
            } catch (Exception e) {
                // Check if we should retry based on the exception
                if (attempt < maxRetries && shouldRetry(e)) {
                    lastException = e;
                    attempt++;
                    
                    // Wait for the backoff duration
                    Duration backoff = calculateBackoff(attempt);
                    logger.debug("Retrying request to {} after {}ms due to {} (attempt {}/{})",
                                request.getUri(), backoff.toMillis(), e.getClass().getSimpleName(), attempt, maxRetries);
                    
                    try {
                        Thread.sleep(backoff.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                    
                    continue;
                }
                
                // No need to retry or max retries reached
                throw e;
            }
        }
        
        // Max retries reached
        if (lastResponse != null) {
            logger.warn("Max retries ({}) reached for request to {}, returning last response: {} {}",
                      maxRetries, request.getUri(), lastResponse.getStatusCode(), lastResponse.getStatusMessage());
            return lastResponse;
        } else {
            logger.warn("Max retries ({}) reached for request to {}, throwing last exception: {}",
                      maxRetries, request.getUri(), lastException.getMessage());
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException) lastException;
            } else {
                throw new RuntimeException("Failed after " + maxRetries + " retries", lastException);
            }
        }
    }
    
    @Override
    public CompletableFuture<HttpResponse> processAsync(HttpRequest request, HttpAsyncMiddlewareChain chain) {
        // Skip retries if method is not in retry methods
        if (!shouldRetryMethod(request.getMethod())) {
            return chain.next(request);
        }
        
        return doAsyncRetry(request, chain, 0, null, null);
    }
    
    private CompletableFuture<HttpResponse> doAsyncRetry(HttpRequest request, HttpAsyncMiddlewareChain chain,
                                                        int attempt, HttpResponse lastResponse, Throwable lastException) {
        return chain.next(request)
            .thenCompose(response -> {
                // Check if we should retry based on the response
                if (attempt < maxRetries && shouldRetry(response)) {
                    // Schedule the next retry after the backoff duration
                    Duration backoff = calculateBackoff(attempt + 1);
                    logger.debug("Retrying request to {} after {}ms (attempt {}/{})",
                               request.getUri(), backoff.toMillis(), attempt + 1, maxRetries);
                    
                    CompletableFuture<HttpResponse> future = new CompletableFuture<>();
                    scheduler.schedule(() -> {
                        doAsyncRetry(request, chain, attempt + 1, response, lastException)
                            .whenComplete((r, e) -> {
                                if (e != null) {
                                    future.completeExceptionally(e);
                                } else {
                                    future.complete(r);
                                }
                            });
                    }, backoff.toMillis(), TimeUnit.MILLISECONDS);
                    
                    return future;
                }
                
                // No need to retry
                return CompletableFuture.completedFuture(response);
            })
            .exceptionally(e -> {
                // Check if we should retry based on the exception
                if (attempt < maxRetries && shouldRetry(e)) {
                    // Schedule the next retry after the backoff duration
                    Duration backoff = calculateBackoff(attempt + 1);
                    logger.debug("Retrying request to {} after {}ms due to {} (attempt {}/{})",
                               request.getUri(), backoff.toMillis(), e.getClass().getSimpleName(), attempt + 1, maxRetries);
                    
                    CompletableFuture<HttpResponse> future = new CompletableFuture<>();
                    scheduler.schedule(() -> {
                        doAsyncRetry(request, chain, attempt + 1, lastResponse, e)
                            .whenComplete((r, ex) -> {
                                if (ex != null) {
                                    future.completeExceptionally(ex);
                                } else {
                                    future.complete(r);
                                }
                            });
                    }, backoff.toMillis(), TimeUnit.MILLISECONDS);
                    
                    return future.join();
                }
                
                // Max retries reached or not retryable
                if (lastResponse != null) {
                    logger.warn("Max retries ({}) reached for request to {}, returning last response: {} {}",
                              maxRetries, request.getUri(), lastResponse.getStatusCode(), lastResponse.getStatusMessage());
                    return lastResponse;
                } else {
                    logger.warn("Max retries ({}) reached for request to {}, throwing last exception: {}",
                              maxRetries, request.getUri(), e.getMessage());
                    throw new RuntimeException("Failed after " + maxRetries + " retries", e);
                }
            });
    }
    
    private boolean shouldRetryMethod(HttpMethod method) {
        return retryMethods.contains(method);
    }
    
    private boolean shouldRetry(HttpResponse response) {
        if (retryStatusCodes.contains(response.getStatusCode())) {
            return true;
        }
        
        if (retryPredicate != null && retryPredicate.test(response)) {
            return true;
        }
        
        return false;
    }
    
    private boolean shouldRetry(Throwable exception) {
        for (Class<? extends Throwable> exceptionClass : retryExceptions) {
            if (exceptionClass.isInstance(exception)) {
                return true;
            }
        }
        
        // Check cause if available
        if (exception.getCause() != null) {
            return shouldRetry(exception.getCause());
        }
        
        return false;
    }
    
    private Duration calculateBackoff(int attempt) {
        switch (backoffStrategy) {
            case NONE:
                return initialBackoff;
            case FIXED:
                return initialBackoff;
            case LINEAR:
                return initialBackoff.multipliedBy(attempt);
            case EXPONENTIAL:
                return initialBackoff.multipliedBy((long) Math.pow(2, attempt - 1));
            case RANDOM:
                long max = initialBackoff.toMillis() * 2;
                long min = initialBackoff.toMillis() / 2;
                long range = max - min + 1;
                long backoff = (long) (Math.random() * range) + min;
                return Duration.ofMillis(backoff);
            default:
                return initialBackoff;
        }
    }
    
    /**
     * Builder for creating {@link RetryMiddleware} instances.
     */
    public static class Builder {
        private int maxRetries = 3;
        private RetryBackoffStrategy backoffStrategy = RetryBackoffStrategy.EXPONENTIAL;
        private Duration initialBackoff = Duration.ofMillis(100);
        private Set<Integer> retryStatusCodes = new HashSet<>(Arrays.asList(500, 502, 503, 504));
        private Set<Class<? extends Throwable>> retryExceptions = new HashSet<>(Arrays.asList(NetworkException.class));
        private Predicate<HttpResponse> retryPredicate;
        private Set<HttpMethod> retryMethods = new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.DELETE));
        
        /**
         * Sets the maximum number of retry attempts.
         * 
         * @param maxRetries the maximum retry attempts
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        /**
         * Sets the backoff strategy to use.
         * 
         * @param strategy the backoff strategy
         * @return this builder
         */
        public Builder backoffStrategy(RetryBackoffStrategy strategy) {
            this.backoffStrategy = strategy;
            return this;
        }
        
        /**
         * Sets the initial backoff duration.
         * 
         * <p>This is the base duration used by the backoff strategy.
         * 
         * @param initialBackoff the initial backoff duration
         * @return this builder
         */
        public Builder initialBackoff(Duration initialBackoff) {
            this.initialBackoff = initialBackoff;
            return this;
        }
        
        /**
         * Sets the HTTP status codes to retry.
         * 
         * @param statusCodes the status codes
         * @return this builder
         */
        public Builder retryStatusCodes(Integer... statusCodes) {
            this.retryStatusCodes = new HashSet<>(Arrays.asList(statusCodes));
            return this;
        }
        
        /**
         * Sets the exception types to retry.
         * 
         * @param exceptions the exception types
         * @return this builder
         */
        @SafeVarargs
        public final Builder retryExceptions(Class<? extends Throwable>... exceptions) {
            this.retryExceptions = new HashSet<>(Arrays.asList(exceptions));
            return this;
        }
        
        /**
         * Sets an additional predicate for retry decisions.
         * 
         * <p>This predicate is called for each response to determine
         * if it should be retried, in addition to the status code check.
         * 
         * @param predicate the retry predicate
         * @return this builder
         */
        public Builder retryPredicate(Predicate<HttpResponse> predicate) {
            this.retryPredicate = predicate;
            return this;
        }
        
        /**
         * Sets the HTTP methods to retry.
         * 
         * <p>By default, only idempotent methods (GET, HEAD, OPTIONS, DELETE)
         * are retried. Add other methods with caution, as retrying methods
         * like POST might cause duplicate operations.
         * 
         * @param methods the HTTP methods
         * @return this builder
         */
        public Builder retryMethods(HttpMethod... methods) {
            this.retryMethods = new HashSet<>(Arrays.asList(methods));
            return this;
        }
        
        /**
         * Builds a new retry middleware with the configured settings.
         * 
         * @return the built middleware
         */
        public RetryMiddleware build() {
            return new RetryMiddleware(maxRetries, backoffStrategy, initialBackoff,
                                     retryStatusCodes, retryExceptions, retryPredicate, retryMethods);
        }
    }
    
    /**
     * Creates a new builder for the retry middleware.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
}