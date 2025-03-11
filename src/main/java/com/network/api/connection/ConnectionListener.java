package com.network.api.connection;

/**
 * Listener for connection events.
 */
public interface ConnectionListener {
    
    /**
     * Called when a connection is established.
     * 
     * @param connection the connection
     */
    void onConnect(Connection connection);
    
    /**
     * Called when a connection is closed.
     * 
     * @param connection the connection
     */
    void onDisconnect(Connection connection);
    
    /**
     * Called when an error occurs on a connection.
     * 
     * @param connection the connection where the error occurred
     * @param throwable the error
     */
    void onError(Connection connection, Throwable throwable);
}
