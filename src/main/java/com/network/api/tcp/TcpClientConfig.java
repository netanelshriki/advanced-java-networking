package com.network.api.tcp;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;

import com.network.config.NetworkConfig;
import com.network.serialization.Serializer;

/**
 * Configuration for TCP clients.
 * 
 * <p>This interface defines the configuration properties specific to TCP clients.
 */
public interface TcpClientConfig extends NetworkConfig {
    
    /**
     * Gets the remote address this client will connect to.
     * 
     * @return the remote address
     */
    InetSocketAddress getRemoteAddress();
    
    /**
     * Gets the local address this client will bind to.
     * 
     * @return an Optional containing the local address, or empty if not set
     */
    Optional<InetSocketAddress> getLocalAddress();
    
    /**
     * Gets whether keep-alive is enabled for TCP connections.
     * 
     * @return true if keep-alive is enabled, false otherwise
     */
    boolean isKeepAliveEnabled();
    
    /**
     * Gets whether Nagle's algorithm is disabled for TCP connections.
     * 
     * @return true if Nagle's algorithm is disabled, false otherwise
     */
    boolean isTcpNoDelay();
    
    /**
     * Gets the TCP linger value.
     * 
     * @return the linger time in seconds, or 0 if disabled
     */
    int getLinger();
    
    /**
     * Gets the TCP receive buffer size.
     * 
     * @return the receive buffer size in bytes
     */
    int getReceiveBufferSize();
    
    /**
     * Gets the TCP send buffer size.
     * 
     * @return the send buffer size in bytes
     */
    int getSendBufferSize();
    
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
    TcpClientBuilder toBuilder();
}