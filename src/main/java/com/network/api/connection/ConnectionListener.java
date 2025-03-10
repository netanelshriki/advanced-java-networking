package com.network.api.connection;

/**
 * Listener interface for connection events.
 * 
 * <p>Implementations of this interface can be registered with a
 * {@link com.network.api.NetworkClient} to receive notification
 * of connection events.
 */
public interface ConnectionListener {
    
    /**
     * Called when a connection is established.
     * 
     * @param connection the established connection
     */
    void onConnect(Connection connection);
    
    /**
     * Called when a connection is closed.
     * 
     * @param connection the closed connection
     * @param reason the reason for disconnection, or null if not available
     */
    void onDisconnect(Connection connection, String reason);
    
    /**
     * Called when data is received on a connection.
     * 
     * @param connection the connection that received data
     * @param data the received data
     */
    void onDataReceived(Connection connection, byte[] data);
    
    /**
     * Called when data is sent on a connection.
     * 
     * @param connection the connection that sent data
     * @param size the size of data sent in bytes
     */
    void onDataSent(Connection connection, int size);
    
    /**
     * Called when an error occurs on a connection.
     * 
     * @param connection the connection where the error occurred
     * @param throwable the error that occurred
     */
    void onError(Connection connection, Throwable throwable);
}