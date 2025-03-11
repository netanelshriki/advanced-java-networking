package com.network.api.retry;

import java.time.Duration;

/**
 * Strategy for determining backoff delay between retry attempts.
 */
public interface RetryBackoffStrategy {
    
    /**
     * Calculates the delay for the next retry attempt.
     * 
     * @param attempt the number of the attempt (1-based)
     * @return the delay before the next attempt
     */
    Duration getDelay(int attempt);
    
    /**
     * Creates a new fixed backoff strategy.
     * 
     * @param delay the fixed delay
     * @return a new fixed backoff strategy
     */
    static RetryBackoffStrategy fixed(Duration delay) {
        return attempt -> delay;
    }
    
    /**
     * Creates a new exponential backoff strategy.
     * 
     * @param initialDelay the initial delay
     * @param multiplier the multiplier for each subsequent attempt
     * @param maxDelay the maximum delay
     * @return a new exponential backoff strategy
     */
    static RetryBackoffStrategy exponential(Duration initialDelay, double multiplier, Duration maxDelay) {
        return attempt -> {
            long delayMillis = (long) (initialDelay.toMillis() * Math.pow(multiplier, attempt - 1));
            return Duration.ofMillis(Math.min(delayMillis, maxDelay.toMillis()));
        };
    }
    
    /**
     * Creates a new linear backoff strategy.
     * 
     * @param initialDelay the initial delay
     * @param increment the increment for each subsequent attempt
     * @param maxDelay the maximum delay
     * @return a new linear backoff strategy
     */
    static RetryBackoffStrategy linear(Duration initialDelay, Duration increment, Duration maxDelay) {
        return attempt -> {
            long delayMillis = initialDelay.toMillis() + (attempt - 1) * increment.toMillis();
            return Duration.ofMillis(Math.min(delayMillis, maxDelay.toMillis()));
        };
    }
    
    /**
     * Creates a new random backoff strategy.
     * 
     * @param minDelay the minimum delay
     * @param maxDelay the maximum delay
     * @return a new random backoff strategy
     */
    static RetryBackoffStrategy random(Duration minDelay, Duration maxDelay) {
        return attempt -> {
            long range = maxDelay.toMillis() - minDelay.toMillis();
            long delayMillis = minDelay.toMillis() + (long) (Math.random() * range);
            return Duration.ofMillis(delayMillis);
        };
    }
}
