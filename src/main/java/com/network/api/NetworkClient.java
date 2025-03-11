package com.network.api;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.exception.NetworkException;

/**
 * Base interface for all network clients.
 * 
 * <p>This interface defines the common operations for all network clients,
 * such as connecting, disconnecting, and checking connection status.
 */
public interface NetworkClient extends AutoCloseable {
    
    /**
     * Connects to the remote endpoint.
     * 
     * @throws NetworkException if an error occurs
     */
    void connect() throws NetworkException;
    
    /**
     * Connects to the remote endpoint asynchronously.
     * 
     * @return a CompletableFuture that completes when the connection is established
     */
    CompletableFuture<Void> connectAsync();
    
    /**
     * Disconnects from the remote endpoint.
     */
    void disconnect();
    
    /**
     * Disconnects from the remote endpoint asynchronously.
     * 
     * @return a CompletableFuture that completes when the disconnect is complete
     */
    CompletableFuture<Void> disconnectAsync();
    
    /**
     * Checks if the client is connected.
     * 
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the underlying connection.
     * 
     * @return the connection, or null if not connected
     */
    Connection getConnection();
    
    /**
     * Adds a connection listener.
     * 
     * @param listener the listener to add
     * @return this client
     */
    NetworkClient addConnectionListener(ConnectionListener listener);
    
    /**
     * Removes a connection listener.
     * 
     * @param listener the listener to remove
     * @return true if the listener was removed, false if it wasn't registered
     */
    boolean removeConnectionListener(ConnectionListener listener);
    
    /**
     * Registers a callback to be invoked when the client connects.
     * 
     * @param callback the callback to invoke
     * @return this client
     */
    NetworkClient onConnect(Consumer<Connection> callback);
    
    /**
     * Registers a callback to be invoked when the client disconnects.
     * 
     * @param callback the callback to invoke
     * @return this client
     */
    NetworkClient onDisconnect(Consumer<Connection> callback);
    
    /**
     * Registers a callback to be invoked when an error occurs.
     * 
     * @param callback the callback to invoke
     * @return this client
     */
    NetworkClient onError(Consumer<Throwable> callback);
    
    /**
     * Sets the connection timeout.
     * 
     * @param timeout the timeout
     * @return this client
     * @throws IllegalStateException if the client is already connected
     * @throws IllegalArgumentException if the timeout is negative
     */
    NetworkClient withConnectionTimeout(Duration timeout);
    
    /**
     * Closes this client, disconnecting if necessary.
     */
    @Override
    void close();
}
