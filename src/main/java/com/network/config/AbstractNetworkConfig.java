package com.network.config;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base implementation of {@link NetworkConfig}.
 * 
 * <p>This class provides a common implementation of the NetworkConfig interface
 * that can be extended by specific configuration classes.
 */
public abstract class AbstractNetworkConfig implements NetworkConfig {
    
    private final Duration connectionTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final Duration idleTimeout;
    private final int maxRetryAttempts;
    private final RetryBackoffStrategy retryBackoffStrategy;
    private final boolean keepAliveEnabled;
    private final Duration keepAliveInterval;
    private final int bufferSize;
    private final Map<String, Object> properties;
    
    /**
     * Creates a new network configuration with the specified parameters.
     * 
     * @param builder the builder containing the configuration values
     */
    protected AbstractNetworkConfig(AbstractNetworkConfigBuilder<?, ?> builder) {
        this.connectionTimeout = builder.connectionTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.idleTimeout = builder.idleTimeout;
        this.maxRetryAttempts = builder.maxRetryAttempts;
        this.retryBackoffStrategy = builder.retryBackoffStrategy;
        this.keepAliveEnabled = builder.keepAliveEnabled;
        this.keepAliveInterval = builder.keepAliveInterval;
        this.bufferSize = builder.bufferSize;
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.properties));
    }
    
    @Override
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }
    
    @Override
    public Duration getReadTimeout() {
        return readTimeout;
    }
    
    @Override
    public Duration getWriteTimeout() {
        return writeTimeout;
    }
    
    @Override
    public Duration getIdleTimeout() {
        return idleTimeout;
    }
    
    @Override
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    @Override
    public RetryBackoffStrategy getRetryBackoffStrategy() {
        return retryBackoffStrategy;
    }
    
    @Override
    public boolean isKeepAliveEnabled() {
        return keepAliveEnabled;
    }
    
    @Override
    public Duration getKeepAliveInterval() {
        return keepAliveInterval;
    }
    
    @Override
    public int getBufferSize() {
        return bufferSize;
    }
    
    @Override
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        if (key == null || type == null) {
            return Optional.empty();
        }
        
        Object value = properties.get(key);
        if (value == null) {
            return Optional.empty();
        }
        
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        
        return Optional.empty();
    }
    
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    /**
     * Abstract base builder for network configurations.
     * 
     * @param <B> the builder type
     * @param <T> the configuration type
     */
    public abstract static class AbstractNetworkConfigBuilder<B extends NetworkConfigBuilder<B, T>, T extends NetworkConfig> 
            implements NetworkConfigBuilder<B, T> {
        
        protected Duration connectionTimeout = Duration.ofSeconds(30);
        protected Duration readTimeout = Duration.ofSeconds(30);
        protected Duration writeTimeout = Duration.ofSeconds(30);
        protected Duration idleTimeout = Duration.ofMinutes(5);
        protected int maxRetryAttempts = 3;
        protected RetryBackoffStrategy retryBackoffStrategy = RetryBackoffStrategy.EXPONENTIAL;
        protected boolean keepAliveEnabled = true;
        protected Duration keepAliveInterval = Duration.ofSeconds(30);
        protected int bufferSize = 8192;
        protected Map<String, Object> properties = new HashMap<>();
        
        protected AbstractNetworkConfigBuilder() {
            // Protected constructor to prevent direct instantiation
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withConnectionTimeout(Duration timeout) {
            validateTimeout(timeout, "Connection timeout");
            this.connectionTimeout = timeout;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withReadTimeout(Duration timeout) {
            validateTimeout(timeout, "Read timeout");
            this.readTimeout = timeout;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withWriteTimeout(Duration timeout) {
            validateTimeout(timeout, "Write timeout");
            this.writeTimeout = timeout;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withIdleTimeout(Duration timeout) {
            validateTimeout(timeout, "Idle timeout");
            this.idleTimeout = timeout;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withMaxRetryAttempts(int maxRetryAttempts) {
            if (maxRetryAttempts < 0) {
                throw new IllegalArgumentException("Max retry attempts must not be negative");
            }
            this.maxRetryAttempts = maxRetryAttempts;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withRetryBackoffStrategy(RetryBackoffStrategy strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Retry backoff strategy must not be null");
            }
            this.retryBackoffStrategy = strategy;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withKeepAlive(boolean enabled) {
            this.keepAliveEnabled = enabled;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withKeepAliveInterval(Duration interval) {
            validateTimeout(interval, "Keep-alive interval");
            this.keepAliveInterval = interval;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withBufferSize(int bufferSize) {
            if (bufferSize <= 0) {
                throw new IllegalArgumentException("Buffer size must be positive");
            }
            this.bufferSize = bufferSize;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withProperty(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("Property key must not be null");
            }
            this.properties.put(key, value);
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withTimeout(Duration timeout) {
            withConnectionTimeout(timeout);
            withReadTimeout(timeout);
            withWriteTimeout(timeout);
            withIdleTimeout(timeout);
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public B withRetry(int maxAttempts, RetryBackoffStrategy strategy) {
            withMaxRetryAttempts(maxAttempts);
            withRetryBackoffStrategy(strategy);
            return (B) this;
        }
        
        @Override
        public abstract T build();
        
        /**
         * Validates that a timeout is not negative.
         * 
         * @param timeout the timeout to validate
         * @param name the name of the timeout for error messages
         * @throws IllegalArgumentException if the timeout is negative
         */
        protected void validateTimeout(Duration timeout, String name) {
            if (timeout == null) {
                throw new IllegalArgumentException(name + " must not be null");
            }
            if (timeout.isNegative()) {
                throw new IllegalArgumentException(name + " must not be negative");
            }
        }
    }
}