package com.network.api.connection;

import java.io.Closeable;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.network.api.metrics.ConnectionMetrics;
import com.network.exception.ConnectionException;

/**
 * Represents a network connection.
 * 
 * <p>This interface provides methods for interacting with a network connection
 * including sending and receiving data, checking connection status, and
 * retrieving connection information.
 * 
 * <p>All implementations must be thread-safe.
 */
public interface Connection extends Closeable {
    
    /**
     * Unique identifier for this connection.
     * 
     * @return the connection ID
     */
    String getId();
    
    /**
     * Gets the remote address this connection is connected to.
     * 
     * @return the remote address
     */
    SocketAddress getRemoteAddress();
    
    /**
     * Gets the local address this connection is bound to.
     * 
     * @return the local address
     */
    SocketAddress getLocalAddress();
    
    /**
     * Checks if this connection is open.
     * 
     * @return true if the connection is open, false otherwise
     */
    boolean isOpen();
    
    /**
     * Gets the time when this connection was established.
     * 
     * @return the time when this connection was established
     */
    Instant getCreationTime();
    
    /**
     * Gets the time of the last activity on this connection.
     * 
     * @return the time of the last activity
     */
    Instant getLastActivityTime();
    
    /**
     * Sends data over this connection.
     * 
     * @param data the data to send
     * @throws ConnectionException if an error occurs while sending
     */
    void send(byte[] data) throws ConnectionException;
    
    /**
     * Sends data over this connection asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendAsync(byte[] data);
    
    /**
     * Closes this connection.
     * 
     * <p>This method is idempotent. If the connection is already closed,
     * this method will return immediately.
     */
    @Override
    void close();
    
    /**
     * Closes this connection asynchronously.
     * 
     * <p>This method is idempotent. If the connection is already closed,
     * the returned future will complete immediately.
     * 
     * @return a CompletableFuture that completes when the connection is closed
     */
    CompletableFuture<Void> closeAsync();
    
    /**
     * Gets the metrics for this connection.
     * 
     * @return the connection metrics
     */
    ConnectionMetrics getMetrics();
    
    /**
     * Sets a connection attribute.
     * 
     * <p>Attributes can be used to store custom data associated with
     * the connection.
     * 
     * @param key the attribute key
     * @param value the attribute value
     * @return this connection instance for method chaining
     */
    Connection setAttribute(String key, Object value);
    
    /**
     * Gets a connection attribute.
     * 
     * @param <T> the expected type of the attribute value
     * @param key the attribute key
     * @param type the class of the expected type
     * @return an Optional containing the attribute value, or empty if not found
     */
    <T> Optional<T> getAttribute(String key, Class<T> type);
    
    /**
     * Gets all connection attributes.
     * 
     * @return a map of all attributes
     */
    Map<String, Object> getAttributes();
    
    /**
     * Sets the idle timeout for this connection.
     * 
     * <p>If no activity occurs on this connection for the specified duration,
     * the connection will be automatically closed.
     * 
     * @param timeout the idle timeout
     * @return this connection instance for method chaining
     * @throws IllegalArgumentException if timeout is negative
     */
    Connection withIdleTimeout(Duration timeout);
    
    /**
     * Enables or disables keep-alive for this connection.
     * 
     * @param keepAlive true to enable keep-alive, false to disable
     * @return this connection instance for method chaining
     */
    Connection withKeepAlive(boolean keepAlive);
    
    /**
     * Gets the protocol used by this connection.
     * 
     * @return the protocol
     */
    Protocol getProtocol();
    
    /**
     * Enum representing the supported protocols.
     */
    enum Protocol {
        TCP, UDP, HTTP, WEBSOCKET, UNKNOWN
    }
}