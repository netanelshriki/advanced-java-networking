package com.network.middleware.http;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.exception.NetworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * HTTP middleware that implements rate limiting.
 * 
 * <p>This middleware limits the rate of requests to avoid overwhelming APIs or
 * services. It supports various rate limiting strategies, including fixed window,
 * sliding window, and token bucket.
 * 
 * <p>Rate limits can be applied globally or based on request attributes such as
 * the URI or method. The middleware also respects rate limit headers returned by
 * the server, allowing it to adapt to server-side rate limits.
 */
public class RateLimitingMiddleware implements HttpMiddleware, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitingMiddleware.class);
    
    private final RateLimitStrategy strategy;
    private final int limit;
    private final Duration window;
    private final Function<HttpRequestContext, String> keySelector;
    private final boolean respectRateLimitHeaders;
    private final boolean waitForPermits;
    private final Duration maxWaitTime;
    private final ScheduledExecutorService scheduler;
    
    private Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    /**
     * Creates a new RateLimitingMiddleware with the specified configuration.
     * 
     * @param builder the builder used to create this middleware
     */
    private RateLimitingMiddleware(Builder builder) {
        this.strategy = builder.strategy;
        this.limit = builder.limit;
        this.window = builder.window;
        this.keySelector = builder.keySelector;
        this.respectRateLimitHeaders = builder.respectRateLimitHeaders;
        this.waitForPermits = builder.waitForPermits;
        this.maxWaitTime = builder.maxWaitTime;
        
        // Create a scheduler for cleanup tasks
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "rate-limiter-cleanup");
            thread.setDaemon(true);
            return thread;
        });
        
        // Schedule cleanup of expired limiters
        if (strategy == RateLimitStrategy.FIXED_WINDOW || strategy == RateLimitStrategy.SLIDING_WINDOW) {
            long windowMillis = window.toMillis();
            scheduler.scheduleAtFixedRate(this::cleanupExpiredLimiters, 
                    windowMillis / 2, windowMillis / 2, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        String key = keySelector.apply(context);
        RateLimiter limiter = getLimiter(key);
        
        boolean permitted;
        if (waitForPermits) {
            try {
                permitted = limiter.tryAcquire(maxWaitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NetworkException("Rate limiting interrupted", e);
            }
        } else {
            permitted = limiter.tryAcquire();
        }
        
        if (!permitted) {
            LOG.debug("Rate limit exceeded for {}", key);
            throw new RateLimitExceededException("Rate limit exceeded", limit, window);
        }
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        if (!respectRateLimitHeaders) {
            return;
        }
        
        // Check for rate limit headers
        int remainingRequests = parseHeader(response, "X-RateLimit-Remaining");
        int limitFromHeader = parseHeader(response, "X-RateLimit-Limit");
        long resetSeconds = parseHeader(response, "X-RateLimit-Reset");
        
        if (limitFromHeader > 0 && remainingRequests >= 0) {
            String key = keySelector.apply(context);
            Duration windowFromHeader = resetSeconds > 0 
                    ? Duration.ofSeconds(resetSeconds) 
                    : window;
            
            // Update the limiter for this key
            RateLimiter limiter = limiters.computeIfPresent(key, (k, l) -> {
                LOG.debug("Updating rate limiter from headers: limit={}, remaining={}, reset={}s",
                        limitFromHeader, remainingRequests, resetSeconds);
                return createLimiter(limitFromHeader, windowFromHeader, remainingRequests);
            });
            
            if (limiter == null) {
                LOG.debug("Creating new rate limiter from headers: limit={}, remaining={}, reset={}s",
                        limitFromHeader, remainingRequests, resetSeconds);
                limiters.put(key, createLimiter(limitFromHeader, windowFromHeader, remainingRequests));
            }
        }
    }
    
    /**
     * Gets or creates a rate limiter for the specified key.
     * 
     * @param key the rate limiter key
     * @return the rate limiter
     */
    private RateLimiter getLimiter(String key) {
        return limiters.computeIfAbsent(key, k -> createLimiter(limit, window, limit));
    }
    
    /**
     * Creates a rate limiter with the specified configuration.
     * 
     * @param limit the rate limit
     * @param window the time window
     * @param initialPermits the initial number of permits
     * @return the rate limiter
     */
    private RateLimiter createLimiter(int limit, Duration window, int initialPermits) {
        switch (strategy) {
            case FIXED_WINDOW:
                return new FixedWindowRateLimiter(limit, window, initialPermits);
                
            case SLIDING_WINDOW:
                return new SlidingWindowRateLimiter(limit, window, initialPermits);
                
            case TOKEN_BUCKET:
                return new TokenBucketRateLimiter(limit, window, initialPermits);
                
            case SEMAPHORE:
                return new SemaphoreRateLimiter(limit);
                
            default:
                throw new IllegalStateException("Unsupported rate limit strategy: " + strategy);
        }
    }
    
    /**
     * Parses a header value as an integer.
     * 
     * @param response the HTTP response
     * @param headerName the header name
     * @return the parsed value, or -1 if the header is not present or invalid
     */
    private int parseHeader(HttpResponse response, String headerName) {
        String value = response.getHeader(headerName);
        if (value == null) {
            return -1;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Removes expired rate limiters to free up memory.
     */
    private void cleanupExpiredLimiters() {
        limiters.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    @Override
    public void close() {
        scheduler.shutdownNow();
    }
    
    /**
     * Creates a new builder for creating RateLimitingMiddleware instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link RateLimitingMiddleware} instances.
     */
    public static class Builder {
        private RateLimitStrategy strategy = RateLimitStrategy.TOKEN_BUCKET;
        private int limit = 100;
        private Duration window = Duration.ofMinutes(1);
        private Function<HttpRequestContext, String> keySelector = context -> "global";
        private boolean respectRateLimitHeaders = true;
        private boolean waitForPermits = false;
        private Duration maxWaitTime = Duration.ofSeconds(10);
        
        /**
         * Sets the rate limiting strategy.
         * 
         * @param strategy the strategy
         * @return this builder instance
         */
        public Builder strategy(RateLimitStrategy strategy) {
            this.strategy = strategy;
            return this;
        }
        
        /**
         * Sets the rate limit.
         * 
         * @param limit the rate limit
         * @return this builder instance
         */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }
        
        /**
         * Sets the time window for rate limiting.
         * 
         * @param window the time window
         * @return this builder instance
         */
        public Builder window(Duration window) {
            this.window = window;
            return this;
        }
        
        /**
         * Sets the key selector for determining the rate limit key.
         * 
         * @param keySelector the key selector
         * @return this builder instance
         */
        public Builder keySelector(Function<HttpRequestContext, String> keySelector) {
            this.keySelector = keySelector;
            return this;
        }
        
        /**
         * Sets whether to respect rate limit headers returned by the server.
         * 
         * @param respectRateLimitHeaders true to respect headers, false otherwise
         * @return this builder instance
         */
        public Builder respectRateLimitHeaders(boolean respectRateLimitHeaders) {
            this.respectRateLimitHeaders = respectRateLimitHeaders;
            return this;
        }
        
        /**
         * Sets whether to wait for permits when the rate limit is reached.
         * 
         * @param waitForPermits true to wait, false to fail immediately
         * @return this builder instance
         */
        public Builder waitForPermits(boolean waitForPermits) {
            this.waitForPermits = waitForPermits;
            return this;
        }
        
        /**
         * Sets the maximum time to wait for permits.
         * 
         * @param maxWaitTime the maximum wait time
         * @return this builder instance
         */
        public Builder maxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
            return this;
        }
        
        /**
         * Configures rate limiting based on the request URI.
         * 
         * @return this builder instance
         */
        public Builder perUri() {
            return keySelector(context -> 
                    context.getRequest().getMethod() + " " + context.getRequest().getUri().getPath());
        }
        
        /**
         * Configures rate limiting based on a URI pattern.
         * 
         * @param pattern the URI pattern
         * @param replacement the replacement string
         * @return this builder instance
         */
        public Builder perUriPattern(Pattern pattern, String replacement) {
            return keySelector(context -> {
                String uri = context.getRequest().getUri().getPath();
                String normalizedUri = pattern.matcher(uri).replaceAll(replacement);
                return context.getRequest().getMethod() + " " + normalizedUri;
            });
        }
        
        /**
         * Configures rate limiting based on the request method.
         * 
         * @return this builder instance
         */
        public Builder perMethod() {
            return keySelector(context -> context.getRequest().getMethod().toString());
        }
        
        /**
         * Builds a new {@link RateLimitingMiddleware} instance with the current settings.
         * 
         * @return a new RateLimitingMiddleware instance
         */
        public RateLimitingMiddleware build() {
            return new RateLimitingMiddleware(this);
        }
    }
    
    /**
     * Interface for rate limiters.
     */
    private interface RateLimiter {
        /**
         * Tries to acquire a permit without waiting.
         * 
         * @return true if a permit was acquired, false otherwise
         */
        boolean tryAcquire();
        
        /**
         * Tries to acquire a permit, waiting up to the specified time if necessary.
         * 
         * @param timeout the maximum time to wait
         * @return true if a permit was acquired, false otherwise
         * @throws InterruptedException if the thread is interrupted while waiting
         */
        boolean tryAcquire(Duration timeout) throws InterruptedException;
        
        /**
         * Checks if this rate limiter has expired.
         * 
         * @return true if expired, false otherwise
         */
        boolean isExpired();
    }
    
    /**
     * Rate limiter that uses a fixed time window.
     */
    private static class FixedWindowRateLimiter implements RateLimiter {
        private final int limit;
        private final Duration window;
        private final Object lock = new Object();
        
        private int count;
        private Instant windowStart;
        private Instant windowEnd;
        
        FixedWindowRateLimiter(int limit, Duration window, int initialRemaining) {
            this.limit = limit;
            this.window = window;
            this.count = limit - initialRemaining;
            this.windowStart = Instant.now();
            this.windowEnd = windowStart.plus(window);
        }
        
        @Override
        public boolean tryAcquire() {
            synchronized (lock) {
                Instant now = Instant.now();
                
                // Check if we've entered a new window
                if (now.isAfter(windowEnd)) {
                    // Reset for a new window
                    count = 0;
                    windowStart = now;
                    windowEnd = now.plus(window);
                }
                
                // Check if we've reached the limit
                if (count >= limit) {
                    return false;
                }
                
                // Acquire a permit
                count++;
                return true;
            }
        }
        
        @Override
        public boolean tryAcquire(Duration timeout) throws InterruptedException {
            long endTime = System.currentTimeMillis() + timeout.toMillis();
            
            while (System.currentTimeMillis() < endTime) {
                if (tryAcquire()) {
                    return true;
                }
                
                // Calculate time to next window
                synchronized (lock) {
                    long sleepTime = windowEnd.toEpochMilli() - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        // Add a small buffer to ensure we're in the next window
                        sleepTime = Math.min(sleepTime + 50, 
                                endTime - System.currentTimeMillis());
                        
                        if (sleepTime > 0) {
                            lock.wait(sleepTime);
                        }
                    }
                }
            }
            
            return false;
        }
        
        @Override
        public boolean isExpired() {
            return Instant.now().isAfter(windowEnd.plus(window));
        }
    }
    
    /**
     * Rate limiter that uses a sliding time window.
     */
    private static class SlidingWindowRateLimiter implements RateLimiter {
        private final int limit;
        private final Duration window;
        private final long windowMillis;
        private final Object lock = new Object();
        
        private long[] requestTimestamps;
        private int index;
        private int size;
        
        SlidingWindowRateLimiter(int limit, Duration window, int initialRemaining) {
            this.limit = limit;
            this.window = window;
            this.windowMillis = window.toMillis();
            this.requestTimestamps = new long[limit];
            
            // Initialize with some requests
            if (initialRemaining < limit) {
                long now = System.currentTimeMillis();
                int initialCount = limit - initialRemaining;
                
                for (int i = 0; i < initialCount; i++) {
                    requestTimestamps[i] = now;
                }
                
                this.index = initialCount % limit;
                this.size = initialCount;
            }
        }
        
        @Override
        public boolean tryAcquire() {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                long windowStart = now - windowMillis;
                
                // Remove expired timestamps
                while (size > 0 && requestTimestamps[(index - size + limit) % limit] < windowStart) {
                    size--;
                }
                
                // Check if we've reached the limit
                if (size >= limit) {
                    return false;
                }
                
                // Acquire a permit
                requestTimestamps[index] = now;
                index = (index + 1) % limit;
                size++;
                
                return true;
            }
        }
        
        @Override
        public boolean tryAcquire(Duration timeout) throws InterruptedException {
            long endTime = System.currentTimeMillis() + timeout.toMillis();
            
            while (System.currentTimeMillis() < endTime) {
                if (tryAcquire()) {
                    return true;
                }
                
                // Calculate time to next available permit
                synchronized (lock) {
                    if (size >= limit) {
                        long oldestTimestamp = requestTimestamps[(index - size + limit) % limit];
                        long sleepTime = (oldestTimestamp + windowMillis) - System.currentTimeMillis();
                        
                        if (sleepTime > 0) {
                            // Add a small buffer to ensure the oldest request expires
                            sleepTime = Math.min(sleepTime + 50, 
                                    endTime - System.currentTimeMillis());
                            
                            if (sleepTime > 0) {
                                lock.wait(sleepTime);
                            }
                        }
                    }
                }
            }
            
            return false;
        }
        
        @Override
        public boolean isExpired() {
            synchronized (lock) {
                return size == 0 && 
                       System.currentTimeMillis() - requestTimestamps[index] > windowMillis * 2;
            }
        }
    }
    
    /**
     * Rate limiter that uses the token bucket algorithm.
     */
    private static class TokenBucketRateLimiter implements RateLimiter {
        private final int capacity;
        private final double refillRate;
        private final Object lock = new Object();
        
        private double tokens;
        private long lastRefillTime;
        
        TokenBucketRateLimiter(int capacity, Duration refillPeriod, int initialTokens) {
            this.capacity = capacity;
            this.refillRate = (double) capacity / refillPeriod.toMillis();
            this.tokens = initialTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        @Override
        public boolean tryAcquire() {
            synchronized (lock) {
                refill();
                
                if (tokens < 1.0) {
                    return false;
                }
                
                tokens -= 1.0;
                return true;
            }
        }
        
        @Override
        public boolean tryAcquire(Duration timeout) throws InterruptedException {
            long endTime = System.currentTimeMillis() + timeout.toMillis();
            
            while (System.currentTimeMillis() < endTime) {
                if (tryAcquire()) {
                    return true;
                }
                
                // Calculate time until next token
                synchronized (lock) {
                    if (tokens < 1.0) {
                        double tokensNeeded = 1.0 - tokens;
                        long sleepTime = (long) (tokensNeeded / refillRate);
                        
                        if (sleepTime > 0) {
                            sleepTime = Math.min(sleepTime + 50, 
                                    endTime - System.currentTimeMillis());
                            
                            if (sleepTime > 0) {
                                lock.wait(sleepTime);
                            }
                        }
                    }
                }
            }
            
            return false;
        }
        
        @Override
        public boolean isExpired() {
            // Token bucket limiters don't expire
            return false;
        }
        
        /**
         * Refills the token bucket based on elapsed time.
         */
        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            
            if (elapsed > 0) {
                double newTokens = elapsed * refillRate;
                tokens = Math.min(capacity, tokens + newTokens);
                lastRefillTime = now;
            }
        }
    }
    
    /**
     * Rate limiter that uses a semaphore for concurrent request limiting.
     */
    private static class SemaphoreRateLimiter implements RateLimiter {
        private final Semaphore semaphore;
        
        SemaphoreRateLimiter(int permits) {
            this.semaphore = new Semaphore(permits);
        }
        
        @Override
        public boolean tryAcquire() {
            return semaphore.tryAcquire();
        }
        
        @Override
        public boolean tryAcquire(Duration timeout) throws InterruptedException {
            return semaphore.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        
        @Override
        public boolean isExpired() {
            // Semaphore limiters don't expire
            return false;
        }
    }
    
    /**
     * Exception thrown when a rate limit is exceeded.
     */
    public static class RateLimitExceededException extends NetworkException {
        private final int limit;
        private final Duration window;
        
        /**
         * Creates a new RateLimitExceededException with the specified message and limits.
         * 
         * @param message the exception message
         * @param limit the rate limit
         * @param window the time window
         */
        public RateLimitExceededException(String message, int limit, Duration window) {
            super(message, null);
            this.limit = limit;
            this.window = window;
        }
        
        /**
         * Gets the rate limit.
         * 
         * @return the rate limit
         */
        public int getLimit() {
            return limit;
        }
        
        /**
         * Gets the time window.
         * 
         * @return the time window
         */
        public Duration getWindow() {
            return window;
        }
    }
    
    /**
     * Enum representing the possible rate limiting strategies.
     */
    public enum RateLimitStrategy {
        /**
         * Fixed window rate limiting.
         * 
         * <p>This strategy allows a fixed number of requests within a time window.
         * The count resets at the end of each window.
         */
        FIXED_WINDOW,
        
        /**
         * Sliding window rate limiting.
         * 
         * <p>This strategy allows a fixed number of requests within a sliding time window.
         * It provides smoother transitions between windows compared to fixed window.
         */
        SLIDING_WINDOW,
        
        /**
         * Token bucket rate limiting.
         * 
         * <p>This strategy uses a token bucket that refills at a constant rate.
         * Each request consumes a token, and requests are rejected when the bucket is empty.
         */
        TOKEN_BUCKET,
        
        /**
         * Semaphore-based concurrency limiting.
         * 
         * <p>This strategy limits the number of concurrent requests rather than
         * the request rate. It's useful for limiting load on backend systems.
         */
        SEMAPHORE
    }
}
