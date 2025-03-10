package com.network.api.udp;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.network.api.NetworkClient;
import com.network.api.connection.Connection;
import com.network.exception.NetworkException;

/**
 * Client for UDP socket connections.
 * 
 * <p>This interface defines the operations for a UDP client that can send
 * and receive datagrams, and register event listeners.
 */
public interface UdpClient extends NetworkClient {
    
    /**
     * Gets the remote address this client is sending to.
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
     * Sends data to the remote address.
     * 
     * @param data the data to send
     * @throws NetworkException if an error occurs
     */
    void send(byte[] data) throws NetworkException;
    
    /**
     * Sends data to the remote address asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendAsync(byte[] data);
    
    /**
     * Sends data to the specified address.
     * 
     * @param data the data to send
     * @param address the address to send to
     * @throws NetworkException if an error occurs
     */
    void send(byte[] data, InetSocketAddress address) throws NetworkException;
    
    /**
     * Sends data to the specified address asynchronously.
     * 
     * @param data the data to send
     * @param address the address to send to
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendAsync(byte[] data, InetSocketAddress address);
    
    /**
     * Receives data from the socket.
     * 
     * <p>This method blocks until data is received or a timeout occurs.
     * 
     * @return the received data as a {@link UdpDatagram}
     * @throws NetworkException if an error occurs or a timeout occurs
     */
    UdpDatagram receive() throws NetworkException;
    
    /**
     * Receives data from the socket asynchronously.
     * 
     * <p>This method returns a future that completes when data is received
     * or a timeout occurs.
     * 
     * @return a CompletableFuture that completes with the received data
     */
    CompletableFuture<UdpDatagram> receiveAsync();
    
    /**
     * Sets whether to broadcast datagrams.
     * 
     * <p>If enabled, datagrams can be sent to a broadcast address.
     * 
     * @param broadcast true to enable broadcasting, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withBroadcast(boolean broadcast);
    
    /**
     * Sets the socket buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withBufferSize(int size);
    
    /**
     * Registers a callback for when data is received.
     * 
     * @param callback the callback to execute when data is received
     * @return this client instance for method chaining
     */
    UdpClient onDataReceived(BiConsumer<Connection, UdpDatagram> callback);
    
    /**
     * Factory method to create a new UDP client builder.
     * 
     * @return a new UDP client builder
     */
    static UdpClientBuilder builder() {
        // This will be implemented by a concrete factory class
        throw new UnsupportedOperationException("Not yet implemented");
    }
}