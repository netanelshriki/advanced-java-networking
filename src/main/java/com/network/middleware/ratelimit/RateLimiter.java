package com.network.middleware.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of rate limiting strategies for controlling request rates.
 * 
 * <p>This class provides various rate limiting algorithms, including:
 * <ul>
 *   <li>Token Bucket - Smooths out bursts while allowing for occasional spikes</li>
 *   <li>Leaky Bucket - Ensures a constant outflow rate</li>
 *   <li>Fixed Window - Simple counting within time windows</li>
 *   <li>Sliding Window - More accurately tracks recent request rates</li>
 * </ul>
 * 
 * <p>The rate limiter automatically handles cleanup of expired state to prevent memory leaks.
 */
public class RateLimiter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimiter.class);
    
    private final RateLimitStrategy strategy;
    private final long permitsPerSecond;
    private final long burstCapacity;
    private final boolean blockOnLimit;
    private final ScheduledExecutorService scheduler;
    
    // For token bucket algorithm
    private final AtomicLong availableTokens = new AtomicLong(0);
    private final AtomicReference<Instant> lastRefillTime = new AtomicReference<>(Instant.now());
    
    // For sliding window algorithm
    private final ConcurrentHashMap<Long, AtomicLong> windowCounts = new ConcurrentHashMap<>();
    
    // For concurrency limiting
    private final Semaphore concurrencyLimiter;
    
    /**
     * Creates a new RateLimiter with the specified configuration.
     * 
     * @param builder the builder used to create this rate limiter
     */
    private RateLimiter(Builder builder) {
        this.strategy = builder.strategy;
        this.permitsPerSecond = builder.permitsPerSecond;
        this.burstCapacity = builder.burstCapacity > 0 ? builder.burstCapacity : permitsPerSecond;
        this.blockOnLimit = builder.blockOnLimit;
        this.concurrencyLimiter = builder.maxConcurrency > 0 ? new Semaphore(builder.maxConcurrency, true) : null;
        
        // Initialize tokens for token bucket
        if (strategy == RateLimitStrategy.TOKEN_BUCKET) {
            availableTokens.set(burstCapacity);
        }
        
        // Set up scheduled cleanup
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "rate-limiter-maintenance");
            thread.setDaemon(true);
            return thread;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanup();
            } catch (Exception e) {
                LOG.error("Error during rate limiter maintenance", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Acquires a permit from the rate limiter, potentially blocking if necessary.
     * 
     * @return true if a permit was acquired, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean acquire() throws InterruptedException {
        if (concurrencyLimiter != null) {
            if (!concurrencyLimiter.tryAcquire(blockOnLimit ? Long.MAX_VALUE : 0, TimeUnit.MILLISECONDS)) {
                LOG.debug("Concurrency limit reached, request rejected");
                return false;
            }
        }
        
        boolean acquired = false;
        try {
            switch (strategy) {
                case TOKEN_BUCKET:
                    acquired = acquireTokenBucket();
                    break;
                    
                case LEAKY_BUCKET:
                    acquired = acquireLeakyBucket();
                    break;
                    
                case FIXED_WINDOW:
                    acquired = acquireFixedWindow();
                    break;
                    
                case SLIDING_WINDOW:
                    acquired = acquireSlidingWindow();
                    break;
                    
                default:
                    throw new IllegalStateException("Unknown rate limit strategy: " + strategy);
            }
            
            if (!acquired && concurrencyLimiter != null) {
                concurrencyLimiter.release();
            }
            
            return acquired;
            
        } catch (InterruptedException e) {
            if (concurrencyLimiter != null) {
                concurrencyLimiter.release();
            }
            throw e;
        }
    }
    
    /**
     * Acquires a permit from the rate limiter asynchronously.
     * 
     * @return a CompletableFuture that completes with true if a permit was acquired, false otherwise
     */
    public CompletableFuture<Boolean> acquireAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        CompletableFuture.runAsync(() -> {
            try {
                future.complete(acquire());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * Releases a permit back to the rate limiter.
     * 
     * <p>This is only needed for concurrency limiting.
     */
    public void release() {
        if (concurrencyLimiter != null) {
            concurrencyLimiter.release();
        }
    }
    
    /**
     * Gets the current rate limit, in permits per second.
     * 
     * @return the rate limit
     */
    public long getPermitsPerSecond() {
        return permitsPerSecond;
    }
    
    /**
     * Gets the burst capacity, in permits.
     * 
     * @return the burst capacity
     */
    public long getBurstCapacity() {
        return burstCapacity;
    }
    
    /**
     * Gets the rate limiting strategy.
     * 
     * @return the strategy
     */
    public RateLimitStrategy getStrategy() {
        return strategy;
    }
    
    /**
     * Gets the number of currently available permits.
     * 
     * <p>This is only meaningful for token bucket strategy.
     * 
     * @return the number of available permits
     */
    public long getAvailablePermits() {
        if (strategy == RateLimitStrategy.TOKEN_BUCKET) {
            refillTokens();
            return Math.min(availableTokens.get(), burstCapacity);
        }
        return 0;
    }
    
    /**
     * Gets the maximum number of concurrent requests allowed.
     * 
     * @return the maximum concurrency, or 0 if concurrency limiting is not enabled
     */
    public int getMaxConcurrency() {
        return concurrencyLimiter != null ? concurrencyLimiter.availablePermits() : 0;
    }
    
    /**
     * Acquires a permit using the token bucket algorithm.
     * 
     * @return true if a permit was acquired, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private boolean acquireTokenBucket() throws InterruptedException {
        // Refill tokens based on elapsed time
        refillTokens();
        
        // Try to take a token
        while (true) {
            long currentTokens = availableTokens.get();
            if (currentTokens <= 0) {
                // No tokens available
                if (!blockOnLimit) {
                    return false;
                }
                
                // Calculate time to wait for one token
                long waitTimeMillis = 1000 / permitsPerSecond;
                LOG.trace("Token bucket exhausted, waiting {}ms for next token", waitTimeMillis);
                Thread.sleep(waitTimeMillis);
                refillTokens();
                continue;
            }
            
            // Try to decrement the token count
            if (availableTokens.compareAndSet(currentTokens, currentTokens - 1)) {
                return true;
            }
            
            // Someone else took a token, try again
        }
    }
    
    /**
     * Refills tokens in the token bucket based on elapsed time.
     */
    private void refillTokens() {
        if (strategy != RateLimitStrategy.TOKEN_BUCKET) {
            return;
        }
        
        Instant now = Instant.now();
        Instant lastRefill = lastRefillTime.get();
        
        // Calculate tokens to add based on elapsed time
        long elapsedNanos = Duration.between(lastRefill, now).toNanos();
        long tokensToAdd = (long) ((double) elapsedNanos / 1_000_000_000 * permitsPerSecond);
        
        if (tokensToAdd > 0) {
            if (lastRefillTime.compareAndSet(lastRefill, now)) {
                availableTokens.getAndUpdate(current -> Math.min(current + tokensToAdd, burstCapacity));
            }
        }
    }
    
    /**
     * Acquires a permit using the leaky bucket algorithm.
     * 
     * @return true if a permit was acquired, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private boolean acquireLeakyBucket() throws InterruptedException {
        // Calculate the time between permits
        long permitsPerNano = 1_000_000_000 / permitsPerSecond;
        
        // Get the current time
        long now = System.nanoTime();
        
        // Check if we can proceed immediately
        long lastPermitAt = lastRefillTime.get().toEpochMilli() * 1_000_000;
        long nextPermitAt = lastPermitAt + permitsPerNano;
        
        if (now >= nextPermitAt) {
            // We can proceed immediately
            lastRefillTime.set(Instant.now());
            return true;
        }
        
        if (!blockOnLimit) {
            return false;
        }
        
        // Wait until we can proceed
        long waitTimeMillis = (nextPermitAt - now) / 1_000_000;
        LOG.trace("Leaky bucket delay, waiting {}ms", waitTimeMillis);
        Thread.sleep(waitTimeMillis);
        lastRefillTime.set(Instant.now());
        return true;
    }
    
    /**
     * Acquires a permit using the fixed window algorithm.
     * 
     * @return true if a permit was acquired, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private boolean acquireFixedWindow() throws InterruptedException {
        // Get the current window
        long windowKey = System.currentTimeMillis() / 1000;
        
        while (true) {
            // Get or create the counter for this window
            AtomicLong counter = windowCounts.computeIfAbsent(windowKey, k -> new AtomicLong(0));
            
            // Check if we can increment the counter
            long count = counter.get();
            if (count >= permitsPerSecond) {
                // Window is full
                if (!blockOnLimit) {
                    return false;
                }
                
                // Wait for the next window
                long nextWindowStart = (windowKey + 1) * 1000;
                long waitTime = nextWindowStart - System.currentTimeMillis();
                if (waitTime > 0) {
                    LOG.trace("Fixed window full, waiting {}ms for next window", waitTime);
                    Thread.sleep(waitTime);
                }
                
                // Update the window key
                windowKey = System.currentTimeMillis() / 1000;
                continue;
            }
            
            // Try to increment the counter
            if (counter.compareAndSet(count, count + 1)) {
                return true;
            }
            
            // Someone else incremented the counter, try again
        }
    }
    
    /**
     * Acquires a permit using the sliding window algorithm.
     * 
     * @return true if a permit was acquired, false otherwise
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private boolean acquireSlidingWindow() throws InterruptedException {
        // Get the current window
        long currentSecond = System.currentTimeMillis() / 1000;
        
        while (true) {
            // Calculate the total count across the current window
            long totalCount = 0;
            for (long i = currentSecond; i > currentSecond - 60; i--) {
                AtomicLong count = windowCounts.get(i);
                if (count != null) {
                    totalCount += count.get();
                }
            }
            
            // Check if we can proceed
            if (totalCount >= permitsPerSecond) {
                // Window is full
                if (!blockOnLimit) {
                    return false;
                }
                
                // Wait a bit and try again
                LOG.trace("Sliding window limit reached, waiting 100ms");
                Thread.sleep(100);
                currentSecond = System.currentTimeMillis() / 1000;
                continue;
            }
            
            // Increment the counter for the current second
            AtomicLong counter = windowCounts.computeIfAbsent(currentSecond, k -> new AtomicLong(0));
            long count = counter.getAndIncrement();
            
            // Double-check that we haven't exceeded the limit
            if (count < permitsPerSecond) {
                return true;
            }
            
            // We exceeded the limit, undo the increment and try again
            counter.decrementAndGet();
        }
    }
    
    /**
     * Cleans up expired state to prevent memory leaks.
     */
    private void cleanup() {
        if (strategy == RateLimitStrategy.FIXED_WINDOW || strategy == RateLimitStrategy.SLIDING_WINDOW) {
            long now = System.currentTimeMillis() / 1000;
            
            // Remove windows older than 60 seconds
            windowCounts.keySet().removeIf(key -> key < now - 60);
        }
    }
    
    @Override
    public void close() {
        scheduler.shutdownNow();
    }
    
    /**
     * Creates a new builder for creating RateLimiter instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link RateLimiter} instances.
     */
    public static class Builder {
        private RateLimitStrategy strategy = RateLimitStrategy.TOKEN_BUCKET;
        private long permitsPerSecond = 10;
        private long burstCapacity = 0;
        private boolean blockOnLimit = false;
        private int maxConcurrency = 0;
        
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
         * Sets the number of permits per second.
         * 
         * @param permitsPerSecond the number of permits per second
         * @return this builder instance
         */
        public Builder permitsPerSecond(long permitsPerSecond) {
            if (permitsPerSecond <= 0) {
                throw new IllegalArgumentException("permitsPerSecond must be > 0");
            }
            this.permitsPerSecond = permitsPerSecond;
            return this;
        }
        
        /**
         * Sets the burst capacity.
         * 
         * <p>This is only used by the token bucket algorithm.
         * 
         * @param burstCapacity the burst capacity
         * @return this builder instance
         */
        public Builder burstCapacity(long burstCapacity) {
            if (burstCapacity < 0) {
                throw new IllegalArgumentException("burstCapacity must be >= 0");
            }
            this.burstCapacity = burstCapacity;
            return this;
        }
        
        /**
         * Sets whether to block when the limit is reached.
         * 
         * <p>If true, the acquire method will block until a permit is available.
         * If false, the acquire method will return false immediately if no permit is available.
         * 
         * @param blockOnLimit true to block, false to return immediately
         * @return this builder instance
         */
        public Builder blockOnLimit(boolean blockOnLimit) {
            this.blockOnLimit = blockOnLimit;
            return this;
        }
        
        /**
         * Sets the maximum number of concurrent requests.
         * 
         * <p>This is independent of the rate limit.
         * 
         * @param maxConcurrency the maximum concurrency
         * @return this builder instance
         */
        public Builder maxConcurrency(int maxConcurrency) {
            if (maxConcurrency < 0) {
                throw new IllegalArgumentException("maxConcurrency must be >= 0");
            }
            this.maxConcurrency = maxConcurrency;
            return this;
        }
        
        /**
         * Builds a new {@link RateLimiter} instance with the current settings.
         * 
         * @return a new RateLimiter instance
         */
        public RateLimiter build() {
            return new RateLimiter(this);
        }
    }
}
