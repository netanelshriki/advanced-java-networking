package com.network.api.connection;

/**
 * Represents a network connection.
 * 
 * <p>This interface provides access to the underlying connection
 * and allows for checking its state and protocol.
 */
public interface Connection extends AutoCloseable {
    
    /**
     * Checks if the connection is active.
     * 
     * @return true if the connection is active, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the protocol used by this connection.
     * 
     * @return the protocol name
     */
    String getProtocol();
    
    /**
     * Closes the connection.
     */
    @Override
    void close();
}
