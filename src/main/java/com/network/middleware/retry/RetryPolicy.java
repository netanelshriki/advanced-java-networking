package com.network.middleware.retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Configuration for retry behavior.
 * 
 * <p>This class defines how retries should be performed, including the
 * maximum number of attempts, the delay between attempts, and the conditions
 * under which retries should be performed.
 * 
 * <p>Instances of this class are immutable and can be created using the
 * {@link Builder} class.
 */
public class RetryPolicy {

    /**
     * The default maximum number of retry attempts.
     */
    public static final int DEFAULT_MAX_ATTEMPTS = 3;
    
    /**
     * The default base delay between retry attempts.
     */
    public static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(100);
    
    /**
     * The default maximum delay between retry attempts.
     */
    public static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(30);
    
    private final int maxAttempts;
    private final Duration baseDelay;
    private final Duration maxDelay;
    private final RetryBackoffStrategy backoffStrategy;
    private final Set<Integer> retryStatusCodes;
    private final Set<Class<? extends Throwable>> retryExceptions;
    private final Predicate<Throwable> retryPredicate;
    private final boolean retryOnTimeout;
    private final double jitterFactor;
    
    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.baseDelay = builder.baseDelay;
        this.maxDelay = builder.maxDelay;
        this.backoffStrategy = builder.backoffStrategy;
        this.retryStatusCodes = Collections.unmodifiableSet(new HashSet<>(builder.retryStatusCodes));
        this.retryExceptions = Collections.unmodifiableSet(new HashSet<>(builder.retryExceptions));
        this.retryPredicate = builder.retryPredicate;
        this.retryOnTimeout = builder.retryOnTimeout;
        this.jitterFactor = builder.jitterFactor;
    }
    
    /**
     * Gets the maximum number of retry attempts.
     * 
     * @return the maximum number of retry attempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    /**
     * Gets the base delay between retry attempts.
     * 
     * @return the base delay
     */
    public Duration getBaseDelay() {
        return baseDelay;
    }
    
    /**
     * Gets the maximum delay between retry attempts.
     * 
     * @return the maximum delay
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }
    
    /**
     * Gets the backoff strategy to use for calculating retry delays.
     * 
     * @return the backoff strategy
     */
    public RetryBackoffStrategy getBackoffStrategy() {
        return backoffStrategy;
    }
    
    /**
     * Gets the set of HTTP status codes that should trigger a retry.
     * 
     * @return the set of status codes
     */
    public Set<Integer> getRetryStatusCodes() {
        return retryStatusCodes;
    }
    
    /**
     * Gets the set of exception types that should trigger a retry.
     * 
     * @return the set of exception types
     */
    public Set<Class<? extends Throwable>> getRetryExceptions() {
        return retryExceptions;
    }
    
    /**
     * Gets the predicate used to determine if an exception should trigger a retry.
     * 
     * @return the retry predicate
     */
    public Predicate<Throwable> getRetryPredicate() {
        return retryPredicate;
    }
    
    /**
     * Checks if retries should be performed on timeout.
     * 
     * @return true if retries should be performed on timeout, false otherwise
     */
    public boolean isRetryOnTimeout() {
        return retryOnTimeout;
    }
    
    /**
     * Gets the jitter factor used to add randomness to retry delays.
     * 
     * @return the jitter factor
     */
    public double getJitterFactor() {
        return jitterFactor;
    }
    
    /**
     * Calculates the delay to use before the next retry attempt.
     * 
     * @param attempt the current attempt number (0-based)
     * @return the delay to use before the next retry
     */
    public Duration calculateDelay(int attempt) {
        Duration delay;
        
        switch (backoffStrategy) {
            case FIXED:
                delay = baseDelay;
                break;
                
            case LINEAR:
                delay = baseDelay.multipliedBy(attempt + 1);
                break;
                
            case EXPONENTIAL:
                delay = baseDelay.multipliedBy((long) Math.pow(2, attempt));
                break;
                
            default:
                delay = baseDelay;
                break;
        }
        
        // Apply jitter if needed
        if (jitterFactor > 0) {
            double jitter = 1.0 - ThreadLocalRandom.current().nextDouble(0, jitterFactor);
            delay = Duration.ofMillis((long) (delay.toMillis() * jitter));
        }
        
        // Ensure the delay doesn't exceed the maximum
        if (delay.compareTo(maxDelay) > 0) {
            delay = maxDelay;
        }
        
        return delay;
    }
    
    /**
     * Checks if a retry should be performed for the given HTTP status code.
     * 
     * @param statusCode the HTTP status code
     * @return true if a retry should be performed, false otherwise
     */
    public boolean shouldRetry(int statusCode) {
        return retryStatusCodes.contains(statusCode);
    }
    
    /**
     * Checks if a retry should be performed for the given exception.
     * 
     * @param exception the exception
     * @return true if a retry should be performed, false otherwise
     */
    public boolean shouldRetry(Throwable exception) {
        if (exception == null) {
            return false;
        }
        
        // Check if the exception type is in the retry exceptions set
        for (Class<? extends Throwable> retryException : retryExceptions) {
            if (retryException.isInstance(exception)) {
                return true;
            }
        }
        
        // Check the custom predicate if available
        return retryPredicate != null && retryPredicate.test(exception);
    }
    
    /**
     * Creates a new builder instance for creating RetryPolicy objects.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link RetryPolicy} instances.
     */
    public static class Builder {
        private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
        private Duration baseDelay = DEFAULT_BASE_DELAY;
        private Duration maxDelay = DEFAULT_MAX_DELAY;
        private RetryBackoffStrategy backoffStrategy = RetryBackoffStrategy.EXPONENTIAL;
        private final Set<Integer> retryStatusCodes = new HashSet<>();
        private final Set<Class<? extends Throwable>> retryExceptions = new HashSet<>();
        private Predicate<Throwable> retryPredicate;
        private boolean retryOnTimeout = false;
        private double jitterFactor = 0.0;
        
        /**
         * Creates a new Builder with default values.
         */
        public Builder() {
            // Add default status codes to retry on
            retryStatusCodes.add(408); // Request Timeout
            retryStatusCodes.add(429); // Too Many Requests
            retryStatusCodes.add(500); // Internal Server Error
            retryStatusCodes.add(502); // Bad Gateway
            retryStatusCodes.add(503); // Service Unavailable
            retryStatusCodes.add(504); // Gateway Timeout
            
            // Add default exceptions to retry on
            retryExceptions.add(java.net.ConnectException.class);
            retryExceptions.add(java.net.SocketTimeoutException.class);
            retryExceptions.add(java.net.UnknownHostException.class);
        }
        
        /**
         * Sets the maximum number of retry attempts.
         * 
         * @param maxAttempts the maximum number of retry attempts
         * @return this builder instance
         */
        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts < 0) {
                throw new IllegalArgumentException("maxAttempts must be >= 0");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }
        
        /**
         * Sets the base delay between retry attempts.
         * 
         * @param baseDelay the base delay
         * @return this builder instance
         */
        public Builder baseDelay(Duration baseDelay) {
            if (baseDelay.isNegative()) {
                throw new IllegalArgumentException("baseDelay must be >= 0");
            }
            this.baseDelay = baseDelay;
            return this;
        }
        
        /**
         * Sets the maximum delay between retry attempts.
         * 
         * @param maxDelay the maximum delay
         * @return this builder instance
         */
        public Builder maxDelay(Duration maxDelay) {
            if (maxDelay.isNegative()) {
                throw new IllegalArgumentException("maxDelay must be >= 0");
            }
            this.maxDelay = maxDelay;
            return this;
        }
        
        /**
         * Sets the backoff strategy to use for calculating retry delays.
         * 
         * @param backoffStrategy the backoff strategy
         * @return this builder instance
         */
        public Builder backoffStrategy(RetryBackoffStrategy backoffStrategy) {
            this.backoffStrategy = backoffStrategy;
            return this;
        }
        
        /**
         * Sets the backoff strategy to exponential with the specified base delay.
         * 
         * @param baseDelay the base delay
         * @return this builder instance
         */
        public Builder exponentialBackoff(Duration baseDelay) {
            this.baseDelay = baseDelay;
            this.backoffStrategy = RetryBackoffStrategy.EXPONENTIAL;
            return this;
        }
        
        /**
         * Sets the backoff strategy to linear with the specified base delay.
         * 
         * @param baseDelay the base delay
         * @return this builder instance
         */
        public Builder linearBackoff(Duration baseDelay) {
            this.baseDelay = baseDelay;
            this.backoffStrategy = RetryBackoffStrategy.LINEAR;
            return this;
        }
        
        /**
         * Sets the backoff strategy to fixed with the specified delay.
         * 
         * @param delay the delay
         * @return this builder instance
         */
        public Builder fixedBackoff(Duration delay) {
            this.baseDelay = delay;
            this.backoffStrategy = RetryBackoffStrategy.FIXED;
            return this;
        }
        
        /**
         * Sets the HTTP status codes that should trigger a retry.
         * 
         * @param statusCodes the status codes
         * @return this builder instance
         */
        public Builder retryStatusCodes(Integer... statusCodes) {
            this.retryStatusCodes.clear();
            this.retryStatusCodes.addAll(Arrays.asList(statusCodes));
            return this;
        }
        
        /**
         * Adds an HTTP status code that should trigger a retry.
         * 
         * @param statusCode the status code
         * @return this builder instance
         */
        public Builder addRetryStatusCode(int statusCode) {
            this.retryStatusCodes.add(statusCode);
            return this;
        }
        
        /**
         * Sets the exception types that should trigger a retry.
         * 
         * @param exceptionTypes the exception types
         * @return this builder instance
         */
        @SafeVarargs
        public final Builder retryExceptions(Class<? extends Throwable>... exceptionTypes) {
            this.retryExceptions.clear();
            Collections.addAll(this.retryExceptions, exceptionTypes);
            return this;
        }
        
        /**
         * Adds an exception type that should trigger a retry.
         * 
         * @param exceptionType the exception type
         * @return this builder instance
         */
        public Builder addRetryException(Class<? extends Throwable> exceptionType) {
            this.retryExceptions.add(exceptionType);
            return this;
        }
        
        /**
         * Sets a custom predicate to determine if a retry should be performed for an exception.
         * 
         * @param retryPredicate the retry predicate
         * @return this builder instance
         */
        public Builder retryPredicate(Predicate<Throwable> retryPredicate) {
            this.retryPredicate = retryPredicate;
            return this;
        }
        
        /**
         * Sets whether retries should be performed on timeout.
         * 
         * @param retryOnTimeout true to retry on timeout, false otherwise
         * @return this builder instance
         */
        public Builder retryOnTimeout(boolean retryOnTimeout) {
            this.retryOnTimeout = retryOnTimeout;
            return this;
        }
        
        /**
         * Sets the jitter factor used to add randomness to retry delays.
         * 
         * <p>The jitter factor is a value between 0 and 1 that determines the maximum
         * amount of randomness to add to the retry delay. A value of 0 means no jitter,
         * while a value of 0.25 means the delay can be reduced by up to 25%.
         * 
         * @param jitterFactor the jitter factor (0-1)
         * @return this builder instance
         */
        public Builder jitterFactor(double jitterFactor) {
            if (jitterFactor < 0 || jitterFactor > 1) {
                throw new IllegalArgumentException("jitterFactor must be between 0 and 1");
            }
            this.jitterFactor = jitterFactor;
            return this;
        }
        
        /**
         * Builds a new {@link RetryPolicy} instance with the current settings.
         * 
         * @return a new RetryPolicy instance
         */
        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
