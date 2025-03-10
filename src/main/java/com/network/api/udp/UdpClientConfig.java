package com.network.api.udp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
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
     * Gets the remote address this client will send datagrams to.
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
     * Gets whether broadcast is enabled.
     * 
     * @return true if broadcast is enabled, false otherwise
     */
    boolean isBroadcastEnabled();
    
    /**
     * Gets the datagram size.
     * 
     * @return the datagram size in bytes
     */
    int getDatagramSize();
    
    /**
     * Gets whether reuse address is enabled.
     * 
     * @return true if reuse address is enabled, false otherwise
     */
    boolean isReuseAddressEnabled();
    
    /**
     * Gets the traffic class.
     * 
     * @return the traffic class, or 0 if not set
     */
    int getTrafficClass();
    
    /**
     * Gets the multicast groups this client is joined to.
     * 
     * @return the list of multicast groups
     */
    List<InetAddress> getMulticastGroups();
    
    /**
     * Gets the network interfaces for multicast groups.
     * 
     * @return a map of multicast groups to network interfaces
     */
    List<MulticastGroup> getMulticastGroupInterfaces();
    
    /**
     * Gets whether multicast loopback mode is enabled.
     * 
     * @return true if multicast loopback mode is enabled, false otherwise
     */
    boolean isMulticastLoopbackModeEnabled();
    
    /**
     * Gets the multicast time-to-live.
     * 
     * @return the multicast TTL, or 1 if not set
     */
    int getMulticastTimeToLive();
    
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
    
    /**
     * Class representing a multicast group joined on a specific network interface.
     */
    class MulticastGroup {
        private final InetAddress group;
        private final NetworkInterface networkInterface;
        
        /**
         * Creates a new multicast group entry.
         * 
         * @param group the multicast group address
         * @param networkInterface the network interface, or null if not specified
         */
        public MulticastGroup(InetAddress group, NetworkInterface networkInterface) {
            this.group = group;
            this.networkInterface = networkInterface;
        }
        
        /**
         * Gets the multicast group address.
         * 
         * @return the group address
         */
        public InetAddress getGroup() {
            return group;
        }
        
        /**
         * Gets the network interface.
         * 
         * @return an Optional containing the network interface, or empty if not specified
         */
        public Optional<NetworkInterface> getNetworkInterface() {
            return Optional.ofNullable(networkInterface);
        }
    }
}