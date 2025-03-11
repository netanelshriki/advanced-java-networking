package com.network.middleware.http;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * HTTP middleware that implements a client-side cache for responses.
 * 
 * <p>This middleware caches responses based on the request URI and method. It supports
 * time-based expiration, cache size limits, and custom cache keys.
 * 
 * <p>The cache implementation is thread-safe and supports concurrent access.
 */
public class CachingMiddleware implements HttpMiddleware, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(CachingMiddleware.class);
    
    private final Map<String, CacheEntry> cache;
    private final Function<HttpRequestContext, String> cacheKeyGenerator;
    private final Duration defaultTtl;
    private final int maxSize;
    private final boolean respectCacheHeaders;
    private final ScheduledExecutorService cleanupExecutor;
    
    /**
     * Creates a new CachingMiddleware with the specified configuration.
     * 
     * @param builder the builder used to create this middleware
     */
    private CachingMiddleware(Builder builder) {
        this.cache = new ConcurrentHashMap<>();
        this.cacheKeyGenerator = builder.cacheKeyGenerator;
        this.defaultTtl = builder.defaultTtl;
        this.maxSize = builder.maxSize;
        this.respectCacheHeaders = builder.respectCacheHeaders;
        
        // Set up a scheduled task to clean up expired entries
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "cache-cleanup");
            thread.setDaemon(true);
            return thread;
        });
        
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 
                1, 1, TimeUnit.MINUTES);
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        // Skip caching for non-cacheable methods
        if (!isCacheableMethod(context.getRequest().getMethod().toString())) {
            return;
        }
        
        // Generate the cache key for this request
        String cacheKey = generateCacheKey(context);
        
        // Look for a cached response
        CacheEntry entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            LOG.debug("Cache hit for {}", cacheKey);
            
            // Throw a CacheHitException to short-circuit the request pipeline
            // This exception will be caught by the HTTP client and the cached response will be returned
            throw new CacheHitException(entry.getResponse());
        }
        
        LOG.debug("Cache miss for {}", cacheKey);
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        // Skip caching for non-cacheable methods
        if (!isCacheableMethod(context.getRequest().getMethod().toString())) {
            return;
        }
        
        // Skip caching for non-success responses
        if (!response.isSuccessful()) {
            return;
        }
        
        // Determine if the response should be cached
        if (!isCacheable(response)) {
            return;
        }
        
        // Determine the TTL for this response
        Duration ttl = determineTtl(response);
        
        // Generate the cache key for this request
        String cacheKey = generateCacheKey(context);
        
        // Check if we need to evict entries due to size constraints
        if (cache.size() >= maxSize) {
            evictOldestEntry();
        }
        
        // Store the response in the cache
        cache.put(cacheKey, new CacheEntry(response, ttl));
        
        LOG.debug("Cached response for {} with TTL {}", cacheKey, ttl);
    }
    
    /**
     * Generates a cache key for the request.
     * 
     * @param context the request context
     * @return the cache key
     */
    private String generateCacheKey(HttpRequestContext context) {
        if (cacheKeyGenerator != null) {
            return cacheKeyGenerator.apply(context);
        }
        
        // Default implementation: use method + URI
        return context.getRequest().getMethod() + " " + context.getRequest().getUri();
    }
    
    /**
     * Checks if the request method is cacheable.
     * 
     * <p>By default, only GET and HEAD requests are cacheable.
     * 
     * @param method the HTTP method
     * @return true if the method is cacheable, false otherwise
     */
    private boolean isCacheableMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }
    
    /**
     * Checks if the response is cacheable.
     * 
     * <p>This checks HTTP cache control headers if respectCacheHeaders is true.
     * 
     * @param response the HTTP response
     * @return true if the response is cacheable, false otherwise
     */
    private boolean isCacheable(HttpResponse response) {
        if (!respectCacheHeaders) {
            return true;
        }
        
        // Check Cache-Control header
        String cacheControl = response.getHeader("Cache-Control");
        if (cacheControl != null) {
            if (cacheControl.contains("no-store") || cacheControl.contains("no-cache")) {
                return false;
            }
        }
        
        // Check Pragma header
        String pragma = response.getHeader("Pragma");
        if (pragma != null && pragma.contains("no-cache")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Determines the time-to-live for a response.
     * 
     * @param response the HTTP response
     * @return the time-to-live
     */
    private Duration determineTtl(HttpResponse response) {
        if (!respectCacheHeaders) {
            return defaultTtl;
        }
        
        // Check Cache-Control max-age
        String cacheControl = response.getHeader("Cache-Control");
        if (cacheControl != null) {
            for (String directive : cacheControl.split(",")) {
                directive = directive.trim();
                if (directive.startsWith("max-age=")) {
                    try {
                        long seconds = Long.parseLong(directive.substring(8));
                        return Duration.ofSeconds(seconds);
                    } catch (NumberFormatException e) {
                        // Ignore invalid max-age
                    }
                }
            }
        }
        
        // Check Expires header
        String expires = response.getHeader("Expires");
        if (expires != null) {
            try {
                // Using java.time.Instant to parse HTTP date
                Instant expiresAt = Instant.parse(expires);
                Duration ttl = Duration.between(Instant.now(), expiresAt);
                if (!ttl.isNegative()) {
                    return ttl;
                }
            } catch (Exception e) {
                // Ignore invalid Expires header
            }
        }
        
        return defaultTtl;
    }
    
    /**
     * Removes all expired entries from the cache.
     */
    private void cleanupExpiredEntries() {
        LOG.debug("Cleaning up expired cache entries");
        
        // Iterate over the cache and remove expired entries
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Evicts the oldest entry from the cache.
     */
    private void evictOldestEntry() {
        if (cache.isEmpty()) {
            return;
        }
        
        String oldestKey = null;
        Instant oldestTimestamp = Instant.MAX;
        
        // Find the oldest entry
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().getCreationTime().isBefore(oldestTimestamp)) {
                oldestKey = entry.getKey();
                oldestTimestamp = entry.getValue().getCreationTime();
            }
        }
        
        // Remove the oldest entry
        if (oldestKey != null) {
            cache.remove(oldestKey);
            LOG.debug("Evicted oldest cache entry: {}", oldestKey);
        }
    }
    
    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        cache.clear();
        LOG.debug("Cache cleared");
    }
    
    /**
     * Gets the number of entries in the cache.
     * 
     * @return the cache size
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Checks if the cache contains a valid entry for the specified key.
     * 
     * @param key the cache key
     * @return true if the cache contains a valid entry, false otherwise
     */
    public boolean containsKey(String key) {
        CacheEntry entry = cache.get(key);
        return entry != null && !entry.isExpired();
    }
    
    @Override
    public void close() {
        cleanupExecutor.shutdownNow();
    }
    
    /**
     * Creates a new builder for creating CachingMiddleware instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link CachingMiddleware} instances.
     */
    public static class Builder {
        private Function<HttpRequestContext, String> cacheKeyGenerator;
        private Duration defaultTtl = Duration.ofMinutes(5);
        private int maxSize = 1000;
        private boolean respectCacheHeaders = true;
        
        /**
         * Sets the cache key generator.
         * 
         * @param cacheKeyGenerator the cache key generator
         * @return this builder instance
         */
        public Builder cacheKeyGenerator(Function<HttpRequestContext, String> cacheKeyGenerator) {
            this.cacheKeyGenerator = cacheKeyGenerator;
            return this;
        }
        
        /**
         * Sets the default time-to-live for cached responses.
         * 
         * @param defaultTtl the default TTL
         * @return this builder instance
         */
        public Builder defaultTtl(Duration defaultTtl) {
            this.defaultTtl = defaultTtl;
            return this;
        }
        
        /**
         * Sets the maximum number of entries in the cache.
         * 
         * @param maxSize the maximum cache size
         * @return this builder instance
         */
        public Builder maxSize(int maxSize) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("maxSize must be > 0");
            }
            this.maxSize = maxSize;
            return this;
        }
        
        /**
         * Sets whether to respect HTTP cache control headers.
         * 
         * @param respectCacheHeaders true to respect cache headers, false otherwise
         * @return this builder instance
         */
        public Builder respectCacheHeaders(boolean respectCacheHeaders) {
            this.respectCacheHeaders = respectCacheHeaders;
            return this;
        }
        
        /**
         * Builds a new {@link CachingMiddleware} instance with the current settings.
         * 
         * @return a new CachingMiddleware instance
         */
        public CachingMiddleware build() {
            return new CachingMiddleware(this);
        }
    }
    
    /**
     * Represents a cached response with metadata.
     */
    private static class CacheEntry {
        private final HttpResponse response;
        private final Instant creationTime;
        private final Instant expirationTime;
        
        /**
         * Creates a new CacheEntry.
         * 
         * @param response the cached response
         * @param ttl the time-to-live
         */
        CacheEntry(HttpResponse response, Duration ttl) {
            this.response = response;
            this.creationTime = Instant.now();
            this.expirationTime = creationTime.plus(ttl);
        }
        
        /**
         * Gets the cached response.
         * 
         * @return the response
         */
        HttpResponse getResponse() {
            return response;
        }
        
        /**
         * Gets the time when this entry was created.
         * 
         * @return the creation time
         */
        Instant getCreationTime() {
            return creationTime;
        }
        
        /**
         * Checks if this entry has expired.
         * 
         * @return true if expired, false otherwise
         */
        boolean isExpired() {
            return Instant.now().isAfter(expirationTime);
        }
    }
    
    /**
     * Exception thrown when a cache hit occurs.
     * 
     * <p>This exception is used to short-circuit the request pipeline and return
     * the cached response instead of sending a new request.
     */
    public static class CacheHitException extends RuntimeException {
        private final HttpResponse cachedResponse;
        
        /**
         * Creates a new CacheHitException with the cached response.
         * 
         * @param cachedResponse the cached response
         */
        public CacheHitException(HttpResponse cachedResponse) {
            super("Cache hit");
            this.cachedResponse = cachedResponse;
        }
        
        /**
         * Gets the cached response.
         * 
         * @return the cached response
         */
        public HttpResponse getCachedResponse() {
            return cachedResponse;
        }
    }
}
