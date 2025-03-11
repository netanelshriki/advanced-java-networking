package com.network.api;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.exception.NetworkException;

/**
 * Base interface for all network clients.
 * <p>
 * This interface defines common operations for all network clients,
 * regardless of the underlying protocol.
 * </p>
 */
public interface NetworkClient extends AutoCloseable {
    
    /**
     * Connects to the remote server.
     * 
     * @throws NetworkException if an error occurs while connecting
     */
    void connect() throws NetworkException;
    
    /**
     * Connects to the remote server asynchronously.
     * 
     * @return a future that completes when the connection is established
     */
    CompletableFuture<Void> connectAsync();
    
    /**
     * Disconnects from the remote server.
     */
    void disconnect();
    
    /**
     * Disconnects from the remote server asynchronously.
     * 
     * @return a future that completes when the disconnection is complete
     */
    CompletableFuture<Void> disconnectAsync();
    
    /**
     * Checks if the client is currently connected.
     * 
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the current connection.
     * 
     * @return the connection, or null if not connected
     */
    Connection getConnection();
    
    /**
     * Adds a connection listener.
     * 
     * @param listener the listener to add
     * @return this client for chaining
     */
    NetworkClient addConnectionListener(ConnectionListener listener);
    
    /**
     * Removes a connection listener.
     * 
     * @param listener the listener to remove
     * @return true if the listener was removed, false otherwise
     */
    boolean removeConnectionListener(ConnectionListener listener);
    
    /**
     * Sets a callback to be invoked when the client connects.
     * 
     * @param callback the callback to invoke
     * @return this client for chaining
     */
    NetworkClient onConnect(Consumer<Connection> callback);
    
    /**
     * Sets a callback to be invoked when the client disconnects.
     * 
     * @param callback the callback to invoke
     * @return this client for chaining
     */
    NetworkClient onDisconnect(Consumer<Connection> callback);
    
    /**
     * Sets a callback to be invoked when an error occurs.
     * 
     * @param callback the callback to invoke
     * @return this client for chaining
     */
    NetworkClient onError(Consumer<Throwable> callback);
    
    /**
     * Sets the connection timeout.
     * 
     * @param timeout the timeout
     * @return this client for chaining
     * @throws IllegalArgumentException if the timeout is negative
     * @throws IllegalStateException if the client is already connected
     */
    NetworkClient withConnectionTimeout(Duration timeout);
    
    /**
     * Closes the client and releases any resources.
     */
    @Override
    void close();
}
