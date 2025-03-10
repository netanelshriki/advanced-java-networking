package com.network.api.udp;

import java.net.InetAddress;
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
     * Sets the network interface to use for multicast.
     * 
     * @param networkInterface the network interface
     * @return this builder
     * @throws IllegalArgumentException if networkInterface is null
     */
    UdpClientBuilder withNetworkInterface(InetAddress networkInterface);
    
    /**
     * Sets whether to enable broadcasting.
     * 
     * <p>If enabled, datagrams can be sent to a broadcast address.
     * 
     * @param broadcast true to enable broadcasting, false to disable
     * @return this builder
     */
    UdpClientBuilder withBroadcast(boolean broadcast);
    
    /**
     * Sets the multicast time-to-live (TTL).
     * 
     * <p>This sets the time-to-live for multicast datagrams
     * (i.e., how many "hops" they can make).
     * 
     * @param ttl the time-to-live
     * @return this builder
     * @throws IllegalArgumentException if ttl is negative
     */
    UdpClientBuilder withMulticastTtl(int ttl);
    
    /**
     * Sets whether to reuse addresses.
     * 
     * <p>If enabled, the socket can be bound to an address that is already in use.
     * 
     * @param reuseAddress true to enable reuse, false to disable
     * @return this builder
     */
    UdpClientBuilder withReuseAddress(boolean reuseAddress);
    
    /**
     * Sets the maximum datagram size.
     * 
     * <p>This is the maximum size of datagrams that can be received.
     * 
     * @param size the maximum datagram size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    UdpClientBuilder withMaxDatagramSize(int size);
    
    /**
     * Sets the socket buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    UdpClientBuilder withBufferSize(int size);
    
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