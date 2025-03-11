package com.network.middleware.http;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.exception.NetworkException;
import com.network.middleware.resilience.CircuitBreaker;
import com.network.middleware.resilience.CircuitBreakerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * HTTP middleware that implements the circuit breaker pattern.
 * 
 * <p>The circuit breaker pattern prevents sending requests to a service that is likely to fail,
 * which can help prevent cascading failures and allow the service time to recover.
 * 
 * <p>This middleware uses a {@link CircuitBreaker} to track the success and failure of requests
 * and automatically stops sending requests when the failure rate is too high. When the circuit
 * is open, requests will fail immediately with a {@link CircuitBreakerOpenException} without
 * sending the actual request.
 */
public class CircuitBreakerMiddleware implements HttpMiddleware {

    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreakerMiddleware.class);
    
    private final CircuitBreaker circuitBreaker;
    private final Predicate<HttpResponse> failurePredicate;
    
    /**
     * Creates a new CircuitBreakerMiddleware with the specified circuit breaker and failure predicate.
     * 
     * @param circuitBreaker the circuit breaker to use
     * @param failurePredicate predicate to determine if a response should be considered a failure
     */
    public CircuitBreakerMiddleware(CircuitBreaker circuitBreaker, Predicate<HttpResponse> failurePredicate) {
        this.circuitBreaker = circuitBreaker;
        this.failurePredicate = failurePredicate;
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        if (!circuitBreaker.allowRequest()) {
            LOG.debug("Circuit breaker '{}' is open, rejecting request to {}", 
                    circuitBreaker.getName(), context.getRequest().getUri());
            
            throw new CircuitBreakerOpenException(
                    "Circuit breaker is open: " + circuitBreaker.getName(),
                    circuitBreaker
            );
        }
        
        // Add circuit breaker state to request headers (if implementation supports it)
        if (context.getRequest() instanceof MutableHttpRequest) {
            MutableHttpRequest req = (MutableHttpRequest) context.getRequest();
            req.addHeader("X-Circuit-Breaker", circuitBreaker.getName());
            req.addHeader("X-Circuit-Breaker-State", circuitBreaker.getState().name());
        }
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        // Record success or failure based on the response
        if (isFailure(response)) {
            LOG.debug("Circuit breaker '{}' recording failure for status code: {}", 
                    circuitBreaker.getName(), response.getStatusCode());
            circuitBreaker.recordFailure(null);
            
            // Add circuit breaker state to response headers (if implementation supports it)
            if (response instanceof MutableHttpResponse) {
                MutableHttpResponse resp = (MutableHttpResponse) response;
                resp.addHeader("X-Circuit-Breaker", circuitBreaker.getName());
                resp.addHeader("X-Circuit-Breaker-State", circuitBreaker.getState().name());
                resp.addHeader("X-Circuit-Breaker-Failure-Count", String.valueOf(circuitBreaker.getFailureCount()));
            }
        } else {
            LOG.debug("Circuit breaker '{}' recording success for status code: {}", 
                    circuitBreaker.getName(), response.getStatusCode());
            circuitBreaker.recordSuccess();
            
            // Add circuit breaker state to response headers (if implementation supports it)
            if (response instanceof MutableHttpResponse) {
                MutableHttpResponse resp = (MutableHttpResponse) response;
                resp.addHeader("X-Circuit-Breaker", circuitBreaker.getName());
                resp.addHeader("X-Circuit-Breaker-State", circuitBreaker.getState().name());
                resp.addHeader("X-Circuit-Breaker-Success-Count", String.valueOf(circuitBreaker.getSuccessCount()));
            }
        }
    }
    
    /**
     * Determines if the response should be considered a failure.
     * 
     * @param response the HTTP response
     * @return true if the response should be considered a failure, false otherwise
     */
    private boolean isFailure(HttpResponse response) {
        // Use the custom predicate if available
        if (failurePredicate != null) {
            return failurePredicate.test(response);
        }
        
        // Default behavior - consider 5xx responses as failures
        return response.getStatusCode() >= 500;
    }
    
    /**
     * Creates a new builder for creating CircuitBreakerMiddleware instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link CircuitBreakerMiddleware} instances.
     */
    public static class Builder {
        private CircuitBreaker circuitBreaker;
        private Predicate<HttpResponse> failurePredicate;
        
        /**
         * Sets the circuit breaker to use.
         * 
         * @param circuitBreaker the circuit breaker
         * @return this builder instance
         */
        public Builder circuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            return this;
        }
        
        /**
         * Sets the predicate to determine if a response should be considered a failure.
         * 
         * <p>If the predicate returns true, the response will be counted as a failure.
         * If the predicate returns false, the response will be counted as a success.
         * 
         * @param failurePredicate the failure predicate
         * @return this builder instance
         */
        public Builder failurePredicate(Predicate<HttpResponse> failurePredicate) {
            this.failurePredicate = failurePredicate;
            return this;
        }
        
        /**
         * Considers responses with status codes in the specified range as failures.
         * 
         * @param minStatusCode the minimum status code (inclusive)
         * @param maxStatusCode the maximum status code (inclusive)
         * @return this builder instance
         */
        public Builder failOnStatusCodeRange(int minStatusCode, int maxStatusCode) {
            return failurePredicate(response -> 
                    response.getStatusCode() >= minStatusCode && 
                    response.getStatusCode() <= maxStatusCode
            );
        }
        
        /**
         * Considers 5xx responses (server errors) as failures.
         * 
         * @return this builder instance
         */
        public Builder failOnServerErrors() {
            return failOnStatusCodeRange(500, 599);
        }
        
        /**
         * Builds a new {@link CircuitBreakerMiddleware} instance with the current settings.
         * 
         * @return a new CircuitBreakerMiddleware instance
         */
        public CircuitBreakerMiddleware build() {
            if (circuitBreaker == null) {
                circuitBreaker = CircuitBreaker.builder().build();
            }
            
            return new CircuitBreakerMiddleware(circuitBreaker, failurePredicate);
        }
    }
    
    /**
     * Exception thrown when a request is rejected because the circuit breaker is open.
     */
    public static class CircuitBreakerOpenException extends NetworkException {
        private final CircuitBreaker circuitBreaker;
        
        /**
         * Creates a new CircuitBreakerOpenException with the specified message and circuit breaker.
         * 
         * @param message the exception message
         * @param circuitBreaker the circuit breaker that rejected the request
         */
        public CircuitBreakerOpenException(String message, CircuitBreaker circuitBreaker) {
            super(message, null);
            this.circuitBreaker = circuitBreaker;
        }
        
        /**
         * Gets the circuit breaker that rejected the request.
         * 
         * @return the circuit breaker
         */
        public CircuitBreaker getCircuitBreaker() {
            return circuitBreaker;
        }
        
        /**
         * Gets the state of the circuit breaker when the request was rejected.
         * 
         * @return the circuit breaker state
         */
        public CircuitBreakerState getCircuitBreakerState() {
            return circuitBreaker.getState();
        }
    }
    
    /**
     * Interface for modifiable HTTP requests.
     * 
     * <p>This interface is used internally by the middleware to add headers to the request.
     * Implementations of HTTP request should also implement this interface.
     */
    private interface MutableHttpRequest {
        /**
         * Adds a header to the request.
         * 
         * @param name the header name
         * @param value the header value
         */
        void addHeader(String name, String value);
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
