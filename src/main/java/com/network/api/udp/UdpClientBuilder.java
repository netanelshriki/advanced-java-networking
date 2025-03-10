package com.network.api.udp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.function.Consumer;

import com.network.config.NetworkConfigBuilder;
import com.network.serialization.Serializer;

/**
 * Builder for UDP clients.
 * 
 * <p>This interface defines the methods for building UDP clients with
 * various configuration options.
 */
public interface UdpClientBuilder extends NetworkConfigBuilder<UdpClientBuilder, UdpClientConfig> {
    
    /**
     * Sets the remote address to send datagrams to.
     * 
     * @param host the host name or IP address
     * @param port the port number
     * @return this builder
     * @throws IllegalArgumentException if host is null or port is not valid
     */
    UdpClientBuilder withAddress(String host, int port);
    
    /**
     * Sets the remote address to send datagrams to.
     * 
     * @param address the socket address
     * @return this builder
     * @throws IllegalArgumentException if address is null
     */
    UdpClientBuilder withAddress(InetSocketAddress address);
    
    /**
     * Sets the local address to bind to.
     * 
     * @param host the local host name or IP address
     * @param port the local port number, or 0 for any available port
     * @return this builder
     * @throws IllegalArgumentException if host is null or port is not valid
     */
    UdpClientBuilder withLocalAddress(String host, int port);
    
    /**
     * Sets the local address to bind to.
     * 
     * @param address the local socket address
     * @return this builder
     * @throws IllegalArgumentException if address is null
     */
    UdpClientBuilder withLocalAddress(InetSocketAddress address);
    
    /**
     * Sets the broadcast flag.
     * 
     * <p>If enabled, the client can send broadcast datagrams to all hosts
     * on the local network.
     * 
     * @param broadcast true to enable broadcast, false to disable
     * @return this builder
     */
    UdpClientBuilder withBroadcast(boolean broadcast);
    
    /**
     * Sets the datagram size.
     * 
     * <p>This is the maximum size of datagrams that can be received by this client.
     * 
     * @param size the datagram size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    UdpClientBuilder withDatagramSize(int size);
    
    /**
     * Sets the reuse address flag.
     * 
     * <p>If enabled, the client can bind to an address that is already in use.
     * 
     * @param reuseAddress true to enable reuse address, false to disable
     * @return this builder
     */
    UdpClientBuilder withReuseAddress(boolean reuseAddress);
    
    /**
     * Sets the traffic class.
     * 
     * <p>The traffic class is used to set quality of service parameters for
     * the outgoing datagrams.
     * 
     * @param trafficClass the traffic class
     * @return this builder
     * @throws IllegalArgumentException if trafficClass is negative or greater than 255
     */
    UdpClientBuilder withTrafficClass(int trafficClass);
    
    /**
     * Joins a multicast group.
     * 
     * <p>This client will receive datagrams sent to this multicast group.
     * 
     * @param multicastAddress the multicast group address
     * @return this builder
     * @throws IllegalArgumentException if multicastAddress is null or not a multicast address
     */
    UdpClientBuilder joinMulticastGroup(InetAddress multicastAddress);
    
    /**
     * Joins a multicast group on a specific network interface.
     * 
     * <p>This client will receive datagrams sent to this multicast group
     * on the specified network interface.
     * 
     * @param multicastAddress the multicast group address
     * @param networkInterface the network interface
     * @return this builder
     * @throws IllegalArgumentException if multicastAddress is null or not a multicast address,
     *         or if networkInterface is null
     */
    UdpClientBuilder joinMulticastGroup(InetAddress multicastAddress, NetworkInterface networkInterface);
    
    /**
     * Sets whether to perform loopback of multicast datagrams.
     * 
     * <p>If enabled, multicast datagrams sent by this client will be received
     * by this client if it is joined to the multicast group.
     * 
     * @param loopbackMode true to enable loopback, false to disable
     * @return this builder
     */
    UdpClientBuilder withMulticastLoopbackMode(boolean loopbackMode);
    
    /**
     * Sets the multicast time-to-live.
     * 
     * <p>The TTL value specifies how many router hops a multicast datagram can
     * traverse before it is discarded.
     * 
     * @param ttl the time-to-live value
     * @return this builder
     * @throws IllegalArgumentException if ttl is negative or greater than 255
     */
    UdpClientBuilder withMulticastTimeToLive(int ttl);
    
    /**
     * Sets whether to automatically connect when the client is built.
     * 
     * <p>If true, the client will attempt to connect when built.
     * If false, the client will not connect until {@link UdpClient#connect()}
     * is called.
     * 
     * @param autoConnect true to enable auto-connect, false to disable
     * @return this builder
     */
    UdpClientBuilder withAutoConnect(boolean autoConnect);
    
    /**
     * Sets the default serializer for data conversion.
     * 
     * <p>The serializer is used to convert between bytes and objects when
     * using the serialization methods.
     * 
     * @param serializer the serializer to use
     * @return this builder
     * @throws IllegalArgumentException if serializer is null
     */
    UdpClientBuilder withSerializer(Serializer serializer);
    
    /**
     * Sets the charset for string conversions.
     * 
     * <p>The charset is used to convert between strings and bytes when
     * using the string methods.
     * 
     * @param charset the charset to use
     * @return this builder
     * @throws IllegalArgumentException if charset is null
     */
    UdpClientBuilder withCharset(Charset charset);
    
    /**
     * Configures the client using the given consumer.
     * 
     * <p>This method allows for more complex configuration that may require
     * multiple builder calls.
     * 
     * @param configurer the configurer
     * @return this builder
     * @throws IllegalArgumentException if configurer is null
     */
    UdpClientBuilder configure(Consumer<UdpClientBuilder> configurer);
    
    /**
     * Builds the UDP client.
     * 
     * @return the built UDP client
     * @throws IllegalStateException if the builder is not properly configured
     */
    UdpClient build();
}