package com.network.api.udp;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
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
     * Sets the remote address to connect to.
     * 
     * @param host the host name or IP address
     * @param port the port number
     * @return this builder
     * @throws IllegalArgumentException if host is null or port is not valid
     */
    UdpClientBuilder withAddress(String host, int port);
    
    /**
     * Sets the remote address to connect to.
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
     * Sets whether to enable broadcasting.
     * 
     * <p>Broadcasting allows sending datagrams to all devices on the network.
     * 
     * @param broadcast true to enable broadcasting, false to disable
     * @return this builder
     */
    UdpClientBuilder withBroadcast(boolean broadcast);
    
    /**
     * Sets the datagram buffer size.
     * 
     * <p>This is the maximum size of datagrams that can be received.
     * 
     * @param size the buffer size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    UdpClientBuilder withDatagramBufferSize(int size);
    
    /**
     * Sets whether to enable IP multicast.
     * 
     * <p>Multicast allows sending datagrams to a group of devices on the network.
     * 
     * @param multicast true to enable multicast, false to disable
     * @return this builder
     */
    UdpClientBuilder withMulticast(boolean multicast);
    
    /**
     * Sets the multicast interface to use.
     * 
     * <p>This is the network interface that will be used for multicast.
     * 
     * @param interfaceName the network interface name
     * @return this builder
     * @throws IllegalArgumentException if interfaceName is null
     */
    UdpClientBuilder withMulticastInterface(String interfaceName);
    
    /**
     * Sets the multicast TTL (time to live).
     * 
     * <p>The TTL determines how many hops a multicast packet can make.
     * 
     * @param ttl the time to live
     * @return this builder
     * @throws IllegalArgumentException if ttl is negative
     */
    UdpClientBuilder withMulticastTTL(int ttl);
    
    /**
     * Sets the multicast groups to join when the client is connected.
     * 
     * @param multicastAddresses the multicast group addresses
     * @return this builder
     * @throws IllegalArgumentException if any address is not a valid multicast address
     */
    UdpClientBuilder withMulticastGroups(InetSocketAddress... multicastAddresses);
    
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