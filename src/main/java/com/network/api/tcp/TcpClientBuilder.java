package com.network.api.tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.function.Consumer;

import com.network.config.NetworkConfigBuilder;
import com.network.serialization.Serializer;

/**
 * Builder for TCP clients.
 * 
 * <p>This interface defines the methods for building TCP clients with
 * various configuration options.
 */
public interface TcpClientBuilder extends NetworkConfigBuilder<TcpClientBuilder, TcpClientConfig> {
    
    /**
     * Sets the remote address to connect to.
     * 
     * @param host the host name or IP address
     * @param port the port number
     * @return this builder
     * @throws IllegalArgumentException if host is null or port is not valid
     */
    TcpClientBuilder withAddress(String host, int port);
    
    /**
     * Sets the remote address to connect to.
     * 
     * @param address the socket address
     * @return this builder
     * @throws IllegalArgumentException if address is null
     */
    TcpClientBuilder withAddress(InetSocketAddress address);
    
    /**
     * Sets the local address to bind to.
     * 
     * @param host the local host name or IP address
     * @param port the local port number, or 0 for any available port
     * @return this builder
     * @throws IllegalArgumentException if host is null or port is not valid
     */
    TcpClientBuilder withLocalAddress(String host, int port);
    
    /**
     * Sets the local address to bind to.
     * 
     * @param address the local socket address
     * @return this builder
     * @throws IllegalArgumentException if address is null
     */
    TcpClientBuilder withLocalAddress(InetSocketAddress address);
    
    /**
     * Sets whether to use keep-alive for TCP connections.
     * 
     * <p>Keep-alive enables the periodic transmission of messages on an otherwise
     * idle connection to verify that the connection is still active.
     * 
     * @param keepAlive true to enable keep-alive, false to disable
     * @return this builder
     */
    TcpClientBuilder withKeepAlive(boolean keepAlive);
    
    /**
     * Sets whether to use Nagle's algorithm for TCP connections.
     * 
     * <p>Nagle's algorithm is used to coalesce multiple small outgoing messages
     * into larger segments, potentially reducing the number of packets sent over
     * the network. Disabling it may reduce latency at the cost of network efficiency.
     * 
     * @param tcpNoDelay true to disable Nagle's algorithm, false to enable
     * @return this builder
     */
    TcpClientBuilder withTcpNoDelay(boolean tcpNoDelay);
    
    /**
     * Sets the TCP linger value.
     * 
     * <p>The linger value determines the amount of time to wait when closing
     * a socket to ensure that all data has been transmitted.
     * 
     * @param linger the linger time in seconds, or 0 to disable
     * @return this builder
     * @throws IllegalArgumentException if linger is negative
     */
    TcpClientBuilder withLinger(int linger);
    
    /**
     * Sets the TCP receive buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    TcpClientBuilder withReceiveBufferSize(int size);
    
    /**
     * Sets the TCP send buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    TcpClientBuilder withSendBufferSize(int size);
    
    /**
     * Sets whether to automatically reconnect when the connection is lost.
     * 
     * @param autoReconnect true to enable auto-reconnect, false to disable
     * @return this builder
     */
    TcpClientBuilder withAutoReconnect(boolean autoReconnect);
    
    /**
     * Sets the auto-reconnect backoff strategy.
     * 
     * <p>The backoff strategy determines how long to wait before attempting
     * to reconnect after a connection failure.
     * 
     * @param initialBackoff the initial backoff duration
     * @param maxBackoff the maximum backoff duration
     * @param strategy the backoff strategy
     * @return this builder
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    TcpClientBuilder withReconnectBackoff(Duration initialBackoff, Duration maxBackoff, RetryBackoffStrategy strategy);
    
    /**
     * Sets the maximum number of reconnect attempts.
     * 
     * <p>After reaching this number of attempts, the client will stop trying
     * to reconnect. A value of 0 means unlimited attempts.
     * 
     * @param maxAttempts the maximum number of reconnect attempts
     * @return this builder
     * @throws IllegalArgumentException if maxAttempts is negative
     */
    TcpClientBuilder withMaxReconnectAttempts(int maxAttempts);
    
    /**
     * Sets whether to automatically connect when the client is built.
     * 
     * <p>If true, the client will attempt to connect when built.
     * If false, the client will not connect until {@link TcpClient#connect()}
     * is called.
     * 
     * @param autoConnect true to enable auto-connect, false to disable
     * @return this builder
     */
    TcpClientBuilder withAutoConnect(boolean autoConnect);
    
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
    TcpClientBuilder withSerializer(Serializer serializer);
    
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
    TcpClientBuilder withCharset(Charset charset);
    
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
    TcpClientBuilder configure(Consumer<TcpClientBuilder> configurer);
    
    /**
     * Builds the TCP client.
     * 
     * @return the built TCP client
     * @throws IllegalStateException if the builder is not properly configured
     */
    TcpClient build();
}