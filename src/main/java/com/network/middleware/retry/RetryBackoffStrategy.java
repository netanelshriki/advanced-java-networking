package com.network.middleware.retry;

/**
 * Enum representing different backoff strategies for retries.
 */
public enum RetryBackoffStrategy {
    /**
     * Fixed delay strategy. Each retry uses the same delay.
     */
    FIXED,
    
    /**
     * Linear backoff strategy. Delay increases linearly with each retry.
     * For example, with a base delay of 100ms:
     * <ul>
     *   <li>1st retry: 100ms</li>
     *   <li>2nd retry: 200ms</li>
     *   <li>3rd retry: 300ms</li>
     * </ul>
     */
    LINEAR,
    
    /**
     * Exponential backoff strategy. Delay increases exponentially with each retry.
     * For example, with a base delay of 100ms:
     * <ul>
     *   <li>1st retry: 100ms</li>
     *   <li>2nd retry: 200ms</li>
     *   <li>3rd retry: 400ms</li>
     *   <li>4th retry: 800ms</li>
     * </ul>
     */
    EXPONENTIAL
}
