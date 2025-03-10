package com.network.api.websocket;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.network.api.NetworkClient;
import com.network.api.connection.Connection;
import com.network.exception.NetworkException;

/**
 * Client for WebSocket connections.
 * 
 * <p>This interface defines the operations for a WebSocket client that can
 * establish connections, send and receive messages, and register event listeners.
 */
public interface WebSocketClient extends NetworkClient {
    
    /**
     * Gets the WebSocket URI this client is connected or will connect to.
     * 
     * @return the WebSocket URI
     */
    URI getUri();
    
    /**
     * Sends a text message over the current connection.
     * 
     * @param message the message to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendText(String message) throws NetworkException;
    
    /**
     * Sends a text message over the current connection asynchronously.
     * 
     * @param message the message to send
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendTextAsync(String message);
    
    /**
     * Sends a binary message over the current connection.
     * 
     * @param data the data to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendBinary(byte[] data) throws NetworkException;
    
    /**
     * Sends a binary message over the current connection asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes when the data has been sent
     */
    CompletableFuture<Void> sendBinaryAsync(byte[] data);
    
    /**
     * Sends a ping message over the current connection.
     * 
     * @param data the ping data, or null for an empty ping
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendPing(byte[] data) throws NetworkException;
    
    /**
     * Sends a ping message over the current connection asynchronously.
     * 
     * @param data the ping data, or null for an empty ping
     * @return a CompletableFuture that completes when the ping has been sent
     */
    CompletableFuture<Void> sendPingAsync(byte[] data);
    
    /**
     * Sends a pong message over the current connection.
     * 
     * <p>Pong messages are typically sent in response to ping messages,
     * but can also be sent unsolicited as a unidirectional heartbeat.
     * 
     * @param data the pong data, or null for an empty pong
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendPong(byte[] data) throws NetworkException;
    
    /**
     * Sends a pong message over the current connection asynchronously.
     * 
     * @param data the pong data, or null for an empty pong
     * @return a CompletableFuture that completes when the pong has been sent
     */
    CompletableFuture<Void> sendPongAsync(byte[] data);
    
    /**
     * Closes the WebSocket connection with the specified status code and reason.
     * 
     * @param statusCode the status code
     * @param reason the reason for closing, or null
     * @throws NetworkException if an error occurs
     */
    void close(int statusCode, String reason) throws NetworkException;
    
    /**
     * Closes the WebSocket connection with the specified status code and reason asynchronously.
     * 
     * @param statusCode the status code
     * @param reason the reason for closing, or null
     * @return a CompletableFuture that completes when the connection is closed
     */
    CompletableFuture<Void> closeAsync(int statusCode, String reason);
    
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
     * Registers a callback for when a ping message is received.
     * 
     * <p>Note that the WebSocket protocol requires that a pong message be sent
     * in response to a ping message. This is handled automatically, but you can
     * register a callback to be notified when a ping is received.
     * 
     * @param callback the callback to execute when a ping message is received
     * @return this client instance for method chaining
     */
    WebSocketClient onPing(BiConsumer<Connection, byte[]> callback);
    
    /**
     * Registers a callback for when a pong message is received.
     * 
     * @param callback the callback to execute when a pong message is received
     * @return this client instance for method chaining
     */
    WebSocketClient onPong(BiConsumer<Connection, byte[]> callback);
    
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