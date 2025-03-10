package com.network.config;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Base interface for network configurations.
 * 
 * <p>This interface defines the common configuration properties for
 * all network components, such as timeouts, retries, and general settings.
 */
public interface NetworkConfig {
    
    /**
     * Gets the connection timeout.
     * 
     * <p>This is the maximum time to wait for a connection to be established.
     * 
     * @return the connection timeout
     */
    Duration getConnectionTimeout();
    
    /**
     * Gets the read timeout.
     * 
     * <p>This is the maximum time to wait for data to be received.
     * 
     * @return the read timeout
     */
    Duration getReadTimeout();
    
    /**
     * Gets the write timeout.
     * 
     * <p>This is the maximum time to wait for data to be sent.
     * 
     * @return the write timeout
     */
    Duration getWriteTimeout();
    
    /**
     * Gets the idle timeout.
     * 
     * <p>This is the maximum time a connection can be idle before it is closed.
     * 
     * @return the idle timeout
     */
    Duration getIdleTimeout();
    
    /**
     * Gets the maximum number of retry attempts.
     * 
     * @return the maximum retry attempts
     */
    int getMaxRetryAttempts();
    
    /**
     * Gets the retry backoff strategy.
     * 
     * @return the retry backoff strategy
     */
    RetryBackoffStrategy getRetryBackoffStrategy();
    
    /**
     * Gets whether keep-alive is enabled.
     * 
     * @return true if keep-alive is enabled, false otherwise
     */
    boolean isKeepAliveEnabled();
    
    /**
     * Gets the keep-alive interval.
     * 
     * @return the keep-alive interval
     */
    Duration getKeepAliveInterval();
    
    /**
     * Gets the buffer size.
     * 
     * @return the buffer size in bytes
     */
    int getBufferSize();
    
    /**
     * Gets a custom configuration property.
     * 
     * @param <T> the type of the property value
     * @param key the property key
     * @param type the class of the expected type
     * @return an Optional containing the property value, or empty if not found
     */
    <T> Optional<T> getProperty(String key, Class<T> type);
    
    /**
     * Gets all custom configuration properties.
     * 
     * @return a map of all custom properties
     */
    Map<String, Object> getProperties();
    
    /**
     * Creates a builder for this configuration type.
     * 
     * @param <T> the type of the builder
     * @return a new builder
     */
    <T extends NetworkConfigBuilder<?, ?>> T toBuilder();
    
    /**
     * Enum representing retry backoff strategies.
     */
    enum RetryBackoffStrategy {
        /**
         * No backoff between retries.
         */
        NONE,
        
        /**
         * Fixed time backoff between retries.
         */
        FIXED,
        
        /**
         * Linear increasing backoff between retries.
         */
        LINEAR,
        
        /**
         * Exponential increasing backoff between retries.
         */
        EXPONENTIAL,
        
        /**
         * Random backoff between retries.
         */
        RANDOM
    }
}