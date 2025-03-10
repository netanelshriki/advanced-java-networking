package com.network.api.udp;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Optional;

import com.network.config.NetworkConfig;
import com.network.serialization.Serializer;

/**
 * Configuration for UDP clients.
 * 
 * <p>This interface defines the configuration properties specific to UDP clients.
 */
public interface UdpClientConfig extends NetworkConfig {
    
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
     * Gets whether broadcast is enabled on this socket.
     * 
     * @return true if broadcast is enabled, false otherwise
     */
    boolean isBroadcastEnabled();
    
    /**
     * Gets whether multicast is enabled on this socket.
     * 
     * @return true if multicast is enabled, false otherwise
     */
    boolean isMulticastEnabled();
    
    /**
     * Gets the UDP receive buffer size.
     * 
     * @return the receive buffer size in bytes
     */
    int getReceiveBufferSize();
    
    /**
     * Gets the UDP send buffer size.
     * 
     * @return the send buffer size in bytes
     */
    int getSendBufferSize();
    
    /**
     * Gets the maximum datagram size.
     * 
     * @return the maximum datagram size in bytes
     */
    int getMaxDatagramSize();
    
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
    UdpClientBuilder toBuilder();
}