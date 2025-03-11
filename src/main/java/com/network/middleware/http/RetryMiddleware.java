package com.network.middleware.http;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.middleware.retry.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP middleware that implements retry logic for failed requests.
 * 
 * <p>This middleware automatically retries failed requests based on a configurable
 * retry policy. It can retry on specific HTTP status codes, exceptions, or based on
 * a custom predicate.
 * 
 * <p>The middleware tracks the number of attempts made for each request in the
 * request context attribute "retry.attempt". It also adds response headers with
 * retry information.
 */
public class RetryMiddleware implements HttpMiddleware {

    private static final Logger LOG = LoggerFactory.getLogger(RetryMiddleware.class);
    
    private static final String RETRY_ATTEMPT_ATTR = "retry.attempt";
    private static final String RETRY_DELAY_ATTR = "retry.delay";
    private static final String MAX_RETRIES_HEADER = "X-Retry-Max";
    private static final String RETRY_COUNT_HEADER = "X-Retry-Count";
    
    private final RetryPolicy retryPolicy;
    
    /**
     * Creates a new RetryMiddleware with the specified retry policy.
     * 
     * @param retryPolicy the retry policy
     */
    public RetryMiddleware(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        // Initialize the retry attempt counter if not already present
        if (!context.hasAttribute(RETRY_ATTEMPT_ATTR)) {
            context.setAttribute(RETRY_ATTEMPT_ATTR, new AtomicInteger(0));
            LOG.debug("Starting request with retry policy: max attempts={}", retryPolicy.getMaxAttempts());
        } else {
            // Increment the retry counter
            AtomicInteger attempt = (AtomicInteger) context.getAttribute(RETRY_ATTEMPT_ATTR);
            attempt.incrementAndGet();
            LOG.debug("Retrying request (attempt {})", attempt.get());
        }
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        AtomicInteger attempt = (AtomicInteger) context.getAttribute(RETRY_ATTEMPT_ATTR);
        int currentAttempt = attempt.get();
        
        // Add retry headers to the response
        ((MutableHttpResponse) response).addHeader(MAX_RETRIES_HEADER, String.valueOf(retryPolicy.getMaxAttempts()));
        ((MutableHttpResponse) response).addHeader(RETRY_COUNT_HEADER, String.valueOf(currentAttempt));
        
        // Check if we need to retry
        boolean shouldRetry = shouldRetry(response, context);
        
        if (shouldRetry && currentAttempt < retryPolicy.getMaxAttempts()) {
            // Calculate the delay for the next retry
            Duration delay = retryPolicy.calculateDelay(currentAttempt);
            
            // Store the delay in the context for potential use by the caller
            context.setAttribute(RETRY_DELAY_ATTR, delay);
            
            LOG.debug("Will retry after {}", delay);
            
            try {
                // Sleep for the calculated delay
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.warn("Retry delay interrupted", e);
            }
            
            // Force a retry by throwing a retry exception
            // This exception will be caught by the HTTP client and the request will be retried
            throw new RetryException(response);
        } else if (shouldRetry) {
            LOG.debug("Max retry attempts reached ({}), giving up", retryPolicy.getMaxAttempts());
        }
    }
    
    /**
     * Determines if a request should be retried based on the response and the retry policy.
     * 
     * @param response the HTTP response
     * @param context the request context
     * @return true if the request should be retried, false otherwise
     */
    private boolean shouldRetry(HttpResponse response, HttpRequestContext context) {
        // Check if the response status code is in the retry set
        if (retryPolicy.shouldRetry(response.getStatusCode())) {
            return true;
        }
        
        // Check if there's an exception in the context
        if (context.hasAttribute("exception")) {
            Throwable exception = (Throwable) context.getAttribute("exception");
            return retryPolicy.shouldRetry(exception);
        }
        
        return false;
    }
    
    /**
     * Creates a new builder for creating RetryMiddleware instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link RetryMiddleware} instances.
     */
    public static class Builder {
        private RetryPolicy retryPolicy;
        
        /**
         * Sets the retry policy.
         * 
         * @param retryPolicy the retry policy
         * @return this builder instance
         */
        public Builder policy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }
        
        /**
         * Builds a new {@link RetryMiddleware} instance with the current settings.
         * 
         * @return a new RetryMiddleware instance
         */
        public RetryMiddleware build() {
            if (retryPolicy == null) {
                retryPolicy = RetryPolicy.builder().build();
            }
            return new RetryMiddleware(retryPolicy);
        }
    }
    
    /**
     * Exception thrown to force a retry of a request.
     */
    private static class RetryException extends RuntimeException {
        private final HttpResponse response;
        
        RetryException(HttpResponse response) {
            super("Retrying request due to response: " + response.getStatusCode());
            this.response = response;
        }
        
        HttpResponse getResponse() {
            return response;
        }
    }
    
    /**
     * Interface for modifiable HTTP responses.
     * 
     * <p>This interface is used internally by the middleware to add headers to the response.
     * Implementations of {@link HttpResponse} should also implement this interface.
     */
    private interface MutableHttpResponse {
        /**
         * Adds a header to the response.
         * 
         * @param name the header name
         * @param value the header value
         */
        void addHeader(String name, String value);
    }
}
