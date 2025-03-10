package com.network.api.websocket;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.network.config.NetworkConfig;
import com.network.serialization.Serializer;

/**
 * Configuration for WebSocket clients.
 * 
 * <p>This interface defines the configuration properties specific to WebSocket clients.
 */
public interface WebSocketClientConfig extends NetworkConfig {
    
    /**
     * Gets the URI this client will connect to.
     * 
     * @return the URI
     */
    URI getUri();
    
    /**
     * Gets the maximum frame size.
     * 
     * @return the maximum frame size in bytes
     */
    int getMaxFrameSize();
    
    /**
     * Gets the maximum message size.
     * 
     * @return the maximum message size in bytes
     */
    int getMaxMessageSize();
    
    /**
     * Gets whether message compression is enabled.
     * 
     * @return true if compression is enabled, false otherwise
     */
    boolean isCompressionEnabled();
    
    /**
     * Gets the sub-protocols requested during the WebSocket handshake.
     * 
     * @return the list of sub-protocols
     */
    List<String> getSubProtocols();
    
    /**
     * Gets the custom headers sent during the WebSocket handshake.
     * 
     * @return the map of headers
     */
    Map<String, String> getHeaders();
    
    /**
     * Gets whether auto-reconnect is enabled.
     * 
     * @return true if auto-reconnect is enabled, false otherwise
     */
    boolean isAutoReconnectEnabled();
    
    /**
     * Gets the initial reconnect backoff duration.
     * 
     * @return the initial backoff duration
     */
    Duration getInitialReconnectBackoff();
    
    /**
     * Gets the maximum reconnect backoff duration.
     * 
     * @return the maximum backoff duration
     */
    Duration getMaxReconnectBackoff();
    
    /**
     * Gets the reconnect backoff strategy.
     * 
     * @return the backoff strategy
     */
    RetryBackoffStrategy getReconnectBackoffStrategy();
    
    /**
     * Gets the maximum number of reconnect attempts.
     * 
     * @return the maximum reconnect attempts, or 0 for unlimited
     */
    int getMaxReconnectAttempts();
    
    /**
     * Gets whether auto-connect is enabled.
     * 
     * @return true if auto-connect is enabled, false otherwise
     */
    boolean isAutoConnectEnabled();
    
    /**
     * Gets the ping interval.
     * 
     * @return the ping interval, or Duration.ZERO if disabled
     */
    Duration getPingInterval();
    
    /**
     * Gets the pong timeout.
     * 
     * @return the pong timeout
     */
    Duration getPongTimeout();
    
    /**
     * Gets whether SSL certificate verification is enabled.
     * 
     * @return true if SSL certificate verification is enabled, false otherwise
     */
    boolean isVerifySslEnabled();
    
    /**
     * Gets the SSL context for secure connections.
     * 
     * @return an Optional containing the SSL context, or empty if not set
     */
    Optional<SSLContext> getSslContext();
    
    /**
     * Gets the serializer used for data conversion.
     * 
     * @return an Optional containing the serializer, or empty if not set
     */
    Optional<Serializer> getSerializer();
    
    /**
     * Gets the charset used for string conversions.
     * 
     * @return the charset
     */
    Charset getCharset();
    
    /**
     * Creates a builder pre-configured with the values from this configuration.
     * 
     * @return a new builder
     */
    @Override
    WebSocketClientBuilder toBuilder();
}