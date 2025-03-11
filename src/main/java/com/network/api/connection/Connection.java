package com.network.api.connection;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.network.api.metrics.ConnectionMetrics;
import com.network.exception.ConnectionException;

/**
 * Represents a network connection.
 */
public interface Connection extends AutoCloseable {
    
    /**
     * Gets the unique ID of this connection.
     * 
     * @return the connection ID
     */
    default String getId() {
        return "unknown";
    }
    
    /**
     * Gets the remote address of this connection.
     * 
     * @return the remote address
     */
    default java.net.SocketAddress getRemoteAddress() {
        return null;
    }
    
    /**
     * Gets the local address of this connection.
     * 
     * @return the local address
     */
    default java.net.SocketAddress getLocalAddress() {
        return null;
    }
    
    /**
     * Checks if the connection is established.
     * 
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Checks if the connection is open.
     * 
     * @return true if open, false otherwise
     */
    default boolean isOpen() {
        return isConnected();
    }
    
    /**
     * Gets the time when this connection was created.
     * 
     * @return the creation time
     */
    default Instant getCreationTime() {
        return Instant.now();
    }
    
    /**
     * Gets the time of the last activity on this connection.
     * 
     * @return the last activity time
     */
    default Instant getLastActivityTime() {
        return Instant.now();
    }
    
    /**
     * Sends data over this connection.
     * 
     * @param data the data to send
     * @throws ConnectionException if an error occurs
     */
    default void send(byte[] data) throws ConnectionException {
        throw new UnsupportedOperationException("Send not supported");
    }
    
    /**
     * Sends data over this connection asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes when the data is sent
     */
    default CompletableFuture<Void> sendAsync(byte[] data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            send(data);
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Closes this connection asynchronously.
     * 
     * @return a CompletableFuture that completes when the connection is closed
     */
    default CompletableFuture<Void> closeAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            close();
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Gets the metrics for this connection.
     * 
     * @return the connection metrics
     */
    default ConnectionMetrics getMetrics() {
        return null;
    }
    
    /**
     * Sets an attribute on this connection.
     * 
     * @param key the attribute key
     * @param value the attribute value
     * @return this connection for chaining
     */
    default Connection setAttribute(String key, Object value) {
        return this;
    }
    
    /**
     * Gets an attribute from this connection.
     * 
     * @param <T> the expected type of the attribute
     * @param key the attribute key
     * @param type the expected type class
     * @return an Optional containing the attribute value, or empty if not found
     */
    default <T> Optional<T> getAttribute(String key, Class<T> type) {
        return Optional.empty();
    }
    
    /**
     * Gets all attributes on this connection.
     * 
     * @return a map of all attributes
     */
    default Map<String, Object> getAttributes() {
        return java.util.Collections.emptyMap();
    }
    
    /**
     * Sets the idle timeout for this connection.
     * 
     * @param timeout the idle timeout
     * @return this connection for chaining
     */
    default Connection withIdleTimeout(Duration timeout) {
        return this;
    }
    
    /**
     * Sets whether keep-alive is enabled for this connection.
     * 
     * @param keepAlive true to enable keep-alive, false to disable
     * @return this connection for chaining
     */
    default Connection withKeepAlive(boolean keepAlive) {
        return this;
    }
    
    /**
     * Gets the protocol used by this connection.
     * 
     * @return the protocol
     */
    Protocol getProtocol();
    
    /**
     * Closes the connection and releases any resources.
     */
    @Override
    void close();
}