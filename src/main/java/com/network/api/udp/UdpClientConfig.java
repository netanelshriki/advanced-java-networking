package com.network.api.udp;

import java.net.InetAddress;
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
     * Gets the remote address to send datagrams to.
     * 
     * @return the remote address
     */
    InetSocketAddress getRemoteAddress();
    
    /**
     * Gets the local address to bind to.
     * 
     * @return an Optional containing the local address, or empty if not set
     */
    Optional<InetSocketAddress> getLocalAddress();
    
    /**
     * Gets the network interface to use for multicast.
     * 
     * @return an Optional containing the network interface, or empty if not set
     */
    Optional<InetAddress> getNetworkInterface();
    
    /**
     * Gets whether broadcasting is enabled.
     * 
     * @return true if broadcasting is enabled, false otherwise
     */
    boolean isBroadcastEnabled();
    
    /**
     * Gets the multicast time-to-live (TTL).
     * 
     * @return the time-to-live
     */
    int getMulticastTtl();
    
    /**
     * Gets whether address reuse is enabled.
     * 
     * @return true if address reuse is enabled, false otherwise
     */
    boolean isReuseAddressEnabled();
    
    /**
     * Gets the maximum datagram size.
     * 
     * @return the maximum datagram size in bytes
     */
    int getMaxDatagramSize();
    
    /**
     * Gets the socket buffer size.
     * 
     * @return the buffer size in bytes
     */
    int getBufferSize();
    
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