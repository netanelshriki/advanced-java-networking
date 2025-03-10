package com.network.api.websocket;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.network.api.NetworkClient;
import com.network.api.connection.Connection;
import com.network.exception.NetworkException;

/**
 * Client for WebSocket connections.
 * 
 * <p>This interface defines the operations for a WebSocket client that can establish
 * connections, send and receive messages, and register event listeners.
 */
public interface WebSocketClient extends NetworkClient {
    
    /**
     * Gets the URL this client is connected or will connect to.
     * 
     * @return the WebSocket URL
     */
    String getUrl();
    
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
     * @param data the application data to include in the ping, or null for none
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendPing(byte[] data) throws NetworkException;
    
    /**
     * Sends a close frame to initiate a graceful connection close.
     * 
     * @param statusCode the close status code
     * @param reason the close reason, or null for none
     * @throws NetworkException if an error occurs or if not connected
     */
    void close(int statusCode, String reason) throws NetworkException;
    
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
     * <p>Note that the client automatically responds to pings with pongs,
     * so this callback is purely informational.
     * 
     * @param callback the callback to execute when a ping is received
     * @return this client instance for method chaining
     */
    WebSocketClient onPing(BiConsumer<Connection, byte[]> callback);
    
    /**
     * Registers a callback for when a pong message is received.
     * 
     * @param callback the callback to execute when a pong is received
     * @return this client instance for method chaining
     */
    WebSocketClient onPong(BiConsumer<Connection, byte[]> callback);
    
    /**
     * Sets an HTTP header to include in the WebSocket handshake request.
     * 
     * @param name the header name
     * @param value the header value
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if name is null
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withHeader(String name, String value);
    
    /**
     * Sets the WebSocket subprotocols to request.
     * 
     * @param subprotocols the subprotocols
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withSubprotocols(String... subprotocols);
    
    /**
     * Sets the compression enabled flag.
     * 
     * <p>If enabled, the client will negotiate compression with the server.
     * 
     * @param compression true to enable compression, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withCompression(boolean compression);
    
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