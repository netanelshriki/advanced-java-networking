package com.network.api;

import java.time.Duration;

/**
 * Strategy for determining delay between retry attempts.
 */
public interface RetryBackoffStrategy {
    
    /**
     * Calculates the delay for the next retry attempt.
     * 
     * @param attemptCount the number of attempts so far (0-based)
     * @return the delay before the next attempt
     */
    Duration calculateDelay(int attemptCount);
    
    /**
     * Creates a constant backoff strategy with a fixed delay.
     * 
     * @param delay the delay between attempts
     * @return a constant backoff strategy
     */
    static RetryBackoffStrategy constant(Duration delay) {
        return attemptCount -> delay;
    }
    
    /**
     * Creates an exponential backoff strategy.
     * 
     * @param initialDelay the initial delay
     * @param multiplier the multiplier for each attempt
     * @param maxDelay the maximum delay
     * @return an exponential backoff strategy
     */
    static RetryBackoffStrategy exponential(Duration initialDelay, double multiplier, Duration maxDelay) {
        return attemptCount -> {
            long delayMillis = (long) (initialDelay.toMillis() * Math.pow(multiplier, attemptCount));
            return Duration.ofMillis(Math.min(delayMillis, maxDelay.toMillis()));
        };
    }
    
    /**
     * Creates a linear backoff strategy.
     * 
     * @param initialDelay the initial delay
     * @param increment the increment for each attempt
     * @param maxDelay the maximum delay
     * @return a linear backoff strategy
     */
    static RetryBackoffStrategy linear(Duration initialDelay, Duration increment, Duration maxDelay) {
        return attemptCount -> {
            long delayMillis = initialDelay.toMillis() + increment.toMillis() * attemptCount;
            return Duration.ofMillis(Math.min(delayMillis, maxDelay.toMillis()));
        };
    }
}
