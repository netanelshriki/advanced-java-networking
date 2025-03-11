package com.network.api.websocket;

import com.network.api.NetworkClient;
import com.network.api.connection.Connection;
import com.network.exception.NetworkException;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Client for WebSocket connections.
 * 
 * <p>This interface defines the operations for a WebSocket client that can establish
 * connections, send and receive messages, and register event listeners.
 */
public interface WebSocketClient extends NetworkClient {

    /**
     * Gets the URI this client is connected or will connect to.
     * 
     * @return the URI
     */
    URI getUri();
    
    /**
     * Sends a text message over the current connection.
     * 
     * @param message the message to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void send(String message) throws NetworkException;
    
    /**
     * Sends a binary message over the current connection.
     * 
     * @param message the message to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void send(byte[] message) throws NetworkException;
    
    /**
     * Sends a text message over the current connection asynchronously.
     * 
     * @param message the message to send
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendAsync(String message);
    
    /**
     * Sends a binary message over the current connection asynchronously.
     * 
     * @param message the message to send
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendAsync(byte[] message);
    
    /**
     * Registers a callback for when a message is received.
     * 
     * @param callback the callback to execute when a message is received
     * @return this client instance for method chaining
     */
    WebSocketClient onMessage(BiConsumer<Connection, WebSocketMessage> callback);
    
    /**
     * Registers a callback for when a text message is received.
     * 
     * @param callback the callback to execute when a text message is received
     * @return this client instance for method chaining
     */
    WebSocketClient onTextMessage(BiConsumer<Connection, String> callback);
    
    /**
     * Registers a callback for when a binary message is received.
     * 
     * @param callback the callback to execute when a binary message is received
     * @return this client instance for method chaining
     */
    WebSocketClient onBinaryMessage(BiConsumer<Connection, byte[]> callback);
    
    /**
     * Factory method to create a new WebSocket client builder.
     * 
     * @return a new WebSocket client builder
     */
    static WebSocketClientBuilder builder() {
        // This will be implemented by a concrete factory class
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
