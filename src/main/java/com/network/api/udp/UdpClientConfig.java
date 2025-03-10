package com.network.api.udp;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
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
     * Gets whether broadcasting is enabled.
     * 
     * @return true if broadcasting is enabled, false otherwise
     */
    boolean isBroadcastEnabled();
    
    /**
     * Gets the datagram buffer size.
     * 
     * @return the buffer size in bytes
     */
    int getDatagramBufferSize();
    
    /**
     * Gets whether multicast is enabled.
     * 
     * @return true if multicast is enabled, false otherwise
     */
    boolean isMulticastEnabled();
    
    /**
     * Gets the multicast interface.
     * 
     * @return an Optional containing the interface name, or empty if not set
     */
    Optional<String> getMulticastInterface();
    
    /**
     * Gets the multicast TTL (time to live).
     * 
     * @return the TTL
     */
    int getMulticastTTL();
    
    /**
     * Gets the multicast groups to join when the client is connected.
     * 
     * @return the multicast groups
     */
    List<InetSocketAddress> getMulticastGroups();
    
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