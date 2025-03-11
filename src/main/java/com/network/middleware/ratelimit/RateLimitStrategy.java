package com.network.middleware.ratelimit;

/**
 * Enum representing different rate limiting strategies.
 */
public enum RateLimitStrategy {
    /**
     * Token Bucket Algorithm.
     * 
     * <p>This algorithm allows for bursts of traffic up to a certain limit (the burst capacity),
     * but ensures that the average rate remains at or below the configured rate limit. Tokens are
     * added to the bucket at a constant rate, and each request consumes one token. If the bucket
     * is empty, the request is either rejected or delayed until a token becomes available.
     * 
     * <p>This is a good general-purpose algorithm that allows for occasional traffic spikes
     * while still maintaining control over the average request rate.
     */
    TOKEN_BUCKET,
    
    /**
     * Leaky Bucket Algorithm.
     * 
     * <p>This algorithm enforces a strict throughput rate by processing requests at a constant rate.
     * It's like a bucket with a hole in the bottom; water (requests) can be added at any rate, but
     * will only leak out (be processed) at a fixed rate. If the bucket overflows, excess requests
     * are either rejected or queued.
     * 
     * <p>This is useful for scenarios where you need to enforce a strict constant rate, such as
     * when calling an API with a fixed rate limit.
     */
    LEAKY_BUCKET,
    
    /**
     * Fixed Window Algorithm.
     * 
     * <p>This algorithm divides time into fixed intervals (e.g., 1 second) and allows a certain
     * number of requests in each interval. When a new interval begins, the counter is reset.
     * 
     * <p>This is the simplest algorithm to implement, but it can lead to traffic spikes at the
     * boundary between windows. For example, if the limit is 100 requests per minute, a client
     * could send 100 requests at 00:59 and then another 100 requests at 01:00, effectively
     * sending 200 requests in a 2-second period.
     */
    FIXED_WINDOW,
    
    /**
     * Sliding Window Algorithm.
     * 
     * <p>This algorithm is similar to the fixed window algorithm, but instead of resetting the
     * counter at fixed intervals, it keeps track of the request count for a sliding window of
     * time. For example, if the limit is 100 requests per minute, it would keep track of the
     * number of requests in the past 60 seconds.
     * 
     * <p>This provides a more accurate rate limit at the cost of additional complexity and
     * memory usage.
     */
    SLIDING_WINDOW
}
