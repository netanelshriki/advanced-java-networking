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
     * Sends a datagram to the remote address.
     * 
     * @param data the data to send
     * @throws NetworkException if an error occurs
     */
    void send(byte[] data) throws NetworkException;
    
    /**
     * Sends a datagram to the remote address asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendAsync(byte[] data);
    
    /**
     * Sends a datagram to a specific address.
     * 
     * @param data the data to send
     * @param address the address to send to
     * @throws NetworkException if an error occurs
     */
    void sendTo(byte[] data, InetSocketAddress address) throws NetworkException;
    
    /**
     * Sends a datagram to a specific address asynchronously.
     * 
     * @param data the data to send
     * @param address the address to send to
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendToAsync(byte[] data, InetSocketAddress address);
    
    /**
     * Broadcasts a datagram to all devices on the network.
     * 
     * <p>This requires broadcast permission and will send to the broadcast
     * address on the specified port.
     * 
     * @param data the data to send
     * @param port the port to send to
     * @throws NetworkException if an error occurs
     */
    void broadcast(byte[] data, int port) throws NetworkException;
    
    /**
     * Broadcasts a datagram to all devices on the network asynchronously.
     * 
     * <p>This requires broadcast permission and will send to the broadcast
     * address on the specified port.
     * 
     * @param data the data to send
     * @param port the port to send to
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> broadcastAsync(byte[] data, int port);
    
    /**
     * Receives a datagram.
     * 
     * <p>This method blocks until a datagram is received or a timeout occurs.
     * 
     * @return the received datagram
     * @throws NetworkException if an error occurs or a timeout occurs
     */
    UdpDatagram receive() throws NetworkException;
    
    /**
     * Receives a datagram asynchronously.
     * 
     * @return a CompletableFuture that completes with the received datagram
     */
    CompletableFuture<UdpDatagram> receiveAsync();
    
    /**
     * Registers a callback for when a datagram is received.
     * 
     * @param callback the callback to execute when a datagram is received
     * @return this client instance for method chaining
     */
    UdpClient onDatagramReceived(Consumer<UdpDatagram> callback);
    
    /**
     * Registers a callback for when data is received.
     * 
     * <p>This is similar to {@link #onDatagramReceived(Consumer)}, but provides
     * only the data and not the datagram wrapper.
     * 
     * @param callback the callback to execute when data is received
     * @return this client instance for method chaining
     */
    UdpClient onDataReceived(BiConsumer<Connection, byte[]> callback);
    
    /**
     * Sets whether to enable broadcast on this socket.
     * 
     * <p>Broadcast must be enabled to use the {@link #broadcast(byte[], int)}
     * method.
     * 
     * @param enableBroadcast true to enable broadcast, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withBroadcast(boolean enableBroadcast);
    
    /**
     * Sets the UDP receive buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withReceiveBufferSize(int size);
    
    /**
     * Sets the UDP send buffer size.
     * 
     * @param size the buffer size in bytes
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withSendBufferSize(int size);
    
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