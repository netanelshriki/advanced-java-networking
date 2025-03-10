package com.network.api.udp;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.network.api.NetworkClient;
import com.network.api.connection.Connection;
import com.network.exception.NetworkException;

/**
 * Client for UDP datagram communication.
 * 
 * <p>This interface defines the operations for a UDP client that can send
 * and receive datagrams, and register event listeners.
 */
public interface UdpClient extends NetworkClient {
    
    /**
     * Gets the remote address this client is connected or will send to.
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
     * Sends a datagram to the specified address.
     * 
     * @param data the data to send
     * @param address the address to send to
     * @throws NetworkException if an error occurs
     */
    void send(byte[] data, InetSocketAddress address) throws NetworkException;
    
    /**
     * Sends a datagram to the specified address asynchronously.
     * 
     * @param data the data to send
     * @param address the address to send to
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendAsync(byte[] data, InetSocketAddress address);
    
    /**
     * Registers a callback for when data is received.
     * 
     * @param callback the callback to execute when data is received
     * @return this client instance for method chaining
     */
    UdpClient onDataReceived(BiConsumer<Connection, UdpDatagram> callback);
    
    /**
     * Sets whether to enable broadcasting.
     * 
     * <p>Broadcasting allows sending datagrams to all devices on the network.
     * 
     * @param broadcast true to enable broadcasting, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withBroadcast(boolean broadcast);
    
    /**
     * Sets the datagram buffer size.
     * 
     * <p>This is the maximum size of datagrams that can be received.
     * 
     * @param size the buffer size in bytes
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if size is not positive
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withDatagramBufferSize(int size);
    
    /**
     * Sets whether to enable IP multicast.
     * 
     * <p>Multicast allows sending datagrams to a group of devices on the network.
     * 
     * @param multicast true to enable multicast, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withMulticast(boolean multicast);
    
    /**
     * Joins a multicast group.
     * 
     * @param multicastAddress the multicast group address
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if multicastAddress is not a valid multicast address
     * @throws IllegalStateException if the client is not connected or multicast is not enabled
     * @throws NetworkException if an error occurs while joining the group
     */
    UdpClient joinMulticastGroup(InetSocketAddress multicastAddress) throws NetworkException;
    
    /**
     * Leaves a multicast group.
     * 
     * @param multicastAddress the multicast group address
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if multicastAddress is not a valid multicast address
     * @throws IllegalStateException if the client is not connected or multicast is not enabled
     * @throws NetworkException if an error occurs while leaving the group
     */
    UdpClient leaveMulticastGroup(InetSocketAddress multicastAddress) throws NetworkException;
    
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