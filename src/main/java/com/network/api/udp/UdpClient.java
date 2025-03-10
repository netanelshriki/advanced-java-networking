package com.network.api.udp;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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
     * Sends a datagram and waits for a reply.
     * 
     * <p>This method sends a datagram to the remote address and waits for a
     * reply datagram, with a timeout.
     * 
     * @param data the data to send
     * @return the reply data
     * @throws NetworkException if an error occurs or if the timeout is reached
     */
    byte[] sendAndReceive(byte[] data) throws NetworkException;
    
    /**
     * Sends a datagram and waits for a reply asynchronously.
     * 
     * <p>This method sends a datagram to the remote address and waits for a
     * reply datagram, with a timeout.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes with the reply data
     */
    CompletableFuture<byte[]> sendAndReceiveAsync(byte[] data);
    
    /**
     * Registers a callback for when a datagram is received.
     * 
     * @param callback the callback to execute when a datagram is received
     * @return this client instance for method chaining
     */
    UdpClient onDatagramReceived(BiConsumer<Connection, byte[]> callback);
    
    /**
     * Sets the broadcast flag.
     * 
     * <p>If enabled, the client can send broadcast datagrams to all hosts
     * on the local network.
     * 
     * @param broadcast true to enable broadcast, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withBroadcast(boolean broadcast);
    
    /**
     * Sets the datagram size.
     * 
     * <p>This is the maximum size of datagrams that can be received by this client.
     * 
     * @param size the datagram size in bytes
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if size is not positive
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withDatagramSize(int size);
    
    /**
     * Sets the reuse address flag.
     * 
     * <p>If enabled, the client can bind to an address that is already in use.
     * 
     * @param reuseAddress true to enable reuse address, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withReuseAddress(boolean reuseAddress);
    
    /**
     * Sets the traffic class.
     * 
     * <p>The traffic class is used to set quality of service parameters for
     * the outgoing datagrams.
     * 
     * @param trafficClass the traffic class
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if trafficClass is negative or greater than 255
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withTrafficClass(int trafficClass);
    
    /**
     * Sets whether to perform loopback of multicast datagrams.
     * 
     * <p>If enabled, multicast datagrams sent by this client will be received
     * by this client if it is joined to the multicast group.
     * 
     * @param loopbackMode true to enable loopback, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withMulticastLoopbackMode(boolean loopbackMode);
    
    /**
     * Sets the multicast time-to-live.
     * 
     * <p>The TTL value specifies how many router hops a multicast datagram can
     * traverse before it is discarded.
     * 
     * @param ttl the time-to-live value
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if ttl is negative or greater than 255
     * @throws IllegalStateException if the client is already connected
     */
    UdpClient withMulticastTimeToLive(int ttl);
    
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