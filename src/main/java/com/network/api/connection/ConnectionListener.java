package com.network.api.connection;

/**
 * Listener for connection events.
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
     */
    void onDisconnect(Connection connection);
    
    /**
     * Called when an error occurs on a connection.
     * 
     * @param connection the connection where the error occurred
     * @param throwable the error that occurred
     */
    void onError(Connection connection, Throwable throwable);
}
