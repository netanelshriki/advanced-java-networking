package com.network.api;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.exception.NetworkException;

/**
 * Base interface for all network clients.
 * 
 * <p>This interface defines the common operations for all network clients
 * irrespective of the underlying protocol. It provides methods for connecting,
 * disconnecting, checking connection status, and registering event listeners.
 * 
 * <p>All implementations must be thread-safe.
 */
public interface NetworkClient extends Closeable {
    
    /**
     * Connects to the remote endpoint.
     * 
     * <p>This method is idempotent. If the client is already connected,
     * this method will return immediately.
     * 
     * @throws NetworkException if connection fails
     */
    void connect() throws NetworkException;
    
    /**
     * Connects to the remote endpoint asynchronously.
     * 
     * <p>This method is idempotent. If the client is already connected,
     * the returned future will complete immediately.
     * 
     * @return a CompletableFuture that completes when connection is established
     */
    CompletableFuture<Void> connectAsync();
    
    /**
     * Disconnects from the remote endpoint.
     * 
     * <p>This method is idempotent. If the client is already disconnected,
     * this method will return immediately.
     */
    void disconnect();
    
    /**
     * Disconnects from the remote endpoint asynchronously.
     * 
     * <p>This method is idempotent. If the client is already disconnected,
     * the returned future will complete immediately.
     * 
     * @return a CompletableFuture that completes when disconnection is finished
     */
    CompletableFuture<Void> disconnectAsync();
    
    /**
     * Checks if the client is currently connected.
     * 
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the current connection, if connected.
     * 
     * @return the current connection or null if not connected
     */
    Connection getConnection();
    
    /**
     * Registers a listener for connection events.
     * 
     * @param listener the listener to register
     * @return this client instance for method chaining
     */
    NetworkClient addConnectionListener(ConnectionListener listener);
    
    /**
     * Removes a previously registered connection listener.
     * 
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    boolean removeConnectionListener(ConnectionListener listener);
    
    /**
     * Convenience method to register a callback for when a connection is established.
     * 
     * @param callback the callback to execute when connected
     * @return this client instance for method chaining
     */
    NetworkClient onConnect(Consumer<Connection> callback);
    
    /**
     * Convenience method to register a callback for when a connection is closed.
     * 
     * @param callback the callback to execute when disconnected
     * @return this client instance for method chaining
     */
    NetworkClient onDisconnect(Consumer<Connection> callback);
    
    /**
     * Convenience method to register a callback for when an error occurs.
     * 
     * @param callback the callback to execute when an error occurs
     * @return this client instance for method chaining
     */
    NetworkClient onError(Consumer<Throwable> callback);
    
    /**
     * Sets the connection timeout for this client.
     * 
     * @param timeout the connection timeout
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if timeout is negative
     * @throws IllegalStateException if the client is already connected
     */
    NetworkClient withConnectionTimeout(Duration timeout);
    
    /**
     * Closes this client and releases any resources associated with it.
     * 
     * <p>If the client is connected, it will be disconnected.
     * This method is idempotent.
     */
    @Override
    void close();
}