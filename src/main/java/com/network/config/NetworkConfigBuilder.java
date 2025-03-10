package com.network.config;

import java.time.Duration;

import com.network.config.NetworkConfig.RetryBackoffStrategy;

/**
 * Builder interface for network configurations.
 * 
 * <p>This interface defines the common builder methods for creating
 * network configurations. It follows the builder pattern to provide
 * a fluent API for configuration creation.
 * 
 * @param <B> the builder type (for method chaining)
 * @param <T> the configuration type being built
 */
public interface NetworkConfigBuilder<B extends NetworkConfigBuilder<B, T>, T extends NetworkConfig> {
    
    /**
     * Sets the connection timeout.
     * 
     * @param timeout the connection timeout
     * @return this builder
     * @throws IllegalArgumentException if timeout is negative
     */
    B withConnectionTimeout(Duration timeout);
    
    /**
     * Sets the read timeout.
     * 
     * @param timeout the read timeout
     * @return this builder
     * @throws IllegalArgumentException if timeout is negative
     */
    B withReadTimeout(Duration timeout);
    
    /**
     * Sets the write timeout.
     * 
     * @param timeout the write timeout
     * @return this builder
     * @throws IllegalArgumentException if timeout is negative
     */
    B withWriteTimeout(Duration timeout);
    
    /**
     * Sets the idle timeout.
     * 
     * @param timeout the idle timeout
     * @return this builder
     * @throws IllegalArgumentException if timeout is negative
     */
    B withIdleTimeout(Duration timeout);
    
    /**
     * Sets the maximum number of retry attempts.
     * 
     * @param maxRetryAttempts the maximum retry attempts
     * @return this builder
     * @throws IllegalArgumentException if maxRetryAttempts is negative
     */
    B withMaxRetryAttempts(int maxRetryAttempts);
    
    /**
     * Sets the retry backoff strategy.
     * 
     * @param strategy the retry backoff strategy
     * @return this builder
     * @throws IllegalArgumentException if strategy is null
     */
    B withRetryBackoffStrategy(RetryBackoffStrategy strategy);
    
    /**
     * Sets whether keep-alive is enabled.
     * 
     * @param enabled true to enable keep-alive, false to disable
     * @return this builder
     */
    B withKeepAlive(boolean enabled);
    
    /**
     * Sets the keep-alive interval.
     * 
     * @param interval the keep-alive interval
     * @return this builder
     * @throws IllegalArgumentException if interval is negative
     */
    B withKeepAliveInterval(Duration interval);
    
    /**
     * Sets the buffer size.
     * 
     * @param bufferSize the buffer size in bytes
     * @return this builder
     * @throws IllegalArgumentException if bufferSize is not positive
     */
    B withBufferSize(int bufferSize);
    
    /**
     * Sets a custom configuration property.
     * 
     * @param key the property key
     * @param value the property value
     * @return this builder
     * @throws IllegalArgumentException if key is null
     */
    B withProperty(String key, Object value);
    
    /**
     * Sets all timeouts to the same value.
     * 
     * <p>This sets the connection, read, write, and idle timeouts.
     * 
     * @param timeout the timeout value
     * @return this builder
     * @throws IllegalArgumentException if timeout is negative
     */
    B withTimeout(Duration timeout);
    
    /**
     * Configures retries with the specified parameters.
     * 
     * @param maxAttempts the maximum number of retry attempts
     * @param strategy the retry backoff strategy
     * @return this builder
     * @throws IllegalArgumentException if maxAttempts is negative or strategy is null
     */
    B withRetry(int maxAttempts, RetryBackoffStrategy strategy);
    
    /**
     * Builds the configuration.
     * 
     * @return the built configuration
     * @throws IllegalStateException if the configuration is invalid
     */
    T build();
}