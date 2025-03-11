package com.network.api.connection;

/**
 * Represents a network connection.
 */
public interface Connection extends AutoCloseable {
    
    /**
     * Checks if the connection is established.
     * 
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the protocol used by this connection.
     * 
     * @return the protocol
     */
    String getProtocol();
    
    /**
     * Closes the connection and releases any resources.
     */
    @Override
    void close();
}
