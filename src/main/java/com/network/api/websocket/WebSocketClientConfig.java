package com.network.api.websocket;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.network.config.NetworkConfig;
import com.network.serialization.Serializer;

/**
 * Configuration for WebSocket clients.
 * 
 * <p>This interface defines the configuration properties specific to WebSocket clients.
 */
public interface WebSocketClientConfig extends NetworkConfig {
    
    /**
     * Gets the WebSocket URL this client will connect to.
     * 
     * @return the WebSocket URL
     */
    URL getUrl();
    
    /**
     * Gets the HTTP headers to include in the WebSocket handshake request.
     * 
     * @return the headers
     */
    Map<String, String> getHeaders();
    
    /**
     * Gets the WebSocket subprotocols to request.
     * 
     * @return the subprotocols
     */
    List<String> getSubprotocols();
    
    /**
     * Gets whether compression is enabled.
     * 
     * @return true if compression is enabled, false otherwise
     */
    boolean isCompressionEnabled();
    
    /**
     * Gets the maximum message size.
     * 
     * @return the maximum message size in bytes
     */
    int getMaxMessageSize();
    
    /**
     * Gets the maximum frame size.
     * 
     * @return the maximum frame size in bytes
     */
    int getMaxFrameSize();
    
    /**
     * Gets whether auto-reconnect is enabled.
     * 
     * @return true if auto-reconnect is enabled, false otherwise
     */
    boolean isAutoReconnectEnabled();
    
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