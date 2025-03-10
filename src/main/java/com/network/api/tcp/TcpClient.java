package com.network.api.tcp;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.network.api.NetworkClient;
import com.network.api.connection.Connection;
import com.network.exception.NetworkException;

/**
 * Client for TCP socket connections.
 * 
 * <p>This interface defines the operations for a TCP client that can establish
 * connections, send and receive data, and register event listeners.
 */
public interface TcpClient extends NetworkClient {

    /**
     * Gets the remote address this client is connected or will connect to.
     * 
     * @return the remote address
     */
    InetSocketAddress getRemoteAddress();
    
    /**
     * Gets the local address this client is bound to.
     * 
     * @return the local address, or null if not bound
     */
    InetSocketAddress getLocalAddress();
    
    /**
     * Sends data over the current connection.
     * 
     * @param data the data to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void send(byte[] data) throws NetworkException;
    
    /**
     * Sends data over the current connection asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendAsync(byte[] data);
    
    /**
     * Sends data over the current connection and waits for a reply.
     * 
     * @param data the data to send
     * @return the reply data
     * @throws NetworkException if an error occurs or if not connected
     */
    byte[] sendAndReceive(byte[] data) throws NetworkException;
    
    /**
     * Sends data over the current connection and waits for a reply asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes with the reply data
     */
    CompletableFuture<byte[]> sendAndReceiveAsync(byte[] data);
    
    /**
     * Registers a callback for when data is received.
     * 
     * @param callback the callback to execute when data is received
     * @return this client instance for method chaining
     */
    TcpClient onDataReceived(BiConsumer<Connection, byte[]> callback);
    
    /**
     * Sets whether to use keep-alive for TCP connections.
     * 
     * <p>Keep-alive enables the periodic transmission of messages on an otherwise
     * idle connection to verify that the connection is still active.
     * 
     * @param keepAlive true to enable keep-alive, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    TcpClient withKeepAlive(boolean keepAlive);
    
    /**
     * Sets whether to use Nagle's algorithm for TCP connections.
     * 
     * <p>Nagle's algorithm is used to coalesce multiple small outgoing messages
     * into larger segments, potentially reducing the number of packets sent over
     * the network. Disabling it may reduce latency at the cost of network efficiency.
     * 
     * @param tcpNoDelay true to disable Nagle's algorithm, false to enable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    TcpClient withTcpNoDelay(boolean tcpNoDelay);
    
    /**
     * Sets the TCP linger value.
     * 
     * <p>The linger value determines the amount of time to wait when closing
     * a socket to ensure that all data has been transmitted.
     * 
     * @param linger the linger time in seconds, or 0 to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    TcpClient withLinger(int linger);
    
    /**
     * Sets the TCP receive buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    TcpClient withReceiveBufferSize(int size);
    
    /**
     * Sets the TCP send buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    TcpClient withSendBufferSize(int size);
    
    /**
     * Factory method to create a new TCP client builder.
     * 
     * @return a new TCP client builder
     */
    static TcpClientBuilder builder() {
        // This will be implemented by a concrete factory class
        throw new UnsupportedOperationException("Not yet implemented");
    }
}