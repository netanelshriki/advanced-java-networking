package com.network.api.websocket;

import java.net.URI;
import java.util.Map;
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
     * Gets the URI this client is connected or will connect to.
     * 
     * @return the URI
     */
    URI getUri();
    
    /**
     * Sends a text message over the WebSocket connection.
     * 
     * @param message the text message to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendText(String message) throws NetworkException;
    
    /**
     * Sends a text message over the WebSocket connection asynchronously.
     * 
     * @param message the text message to send
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendTextAsync(String message);
    
    /**
     * Sends a binary message over the WebSocket connection.
     * 
     * @param data the binary message to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendBinary(byte[] data) throws NetworkException;
    
    /**
     * Sends a binary message over the WebSocket connection asynchronously.
     * 
     * @param data the binary message to send
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendBinaryAsync(byte[] data);
    
    /**
     * Sends a ping message over the WebSocket connection.
     * 
     * @param data the ping payload, or null for no payload
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendPing(byte[] data) throws NetworkException;
    
    /**
     * Sends a ping message over the WebSocket connection asynchronously.
     * 
     * @param data the ping payload, or null for no payload
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendPingAsync(byte[] data);
    
    /**
     * Closes the WebSocket connection with a normal closure code.
     * 
     * @throws NetworkException if an error occurs
     */
    void close() throws NetworkException;
    
    /**
     * Closes the WebSocket connection with the specified code and reason.
     * 
     * @param code the closure code
     * @param reason the closure reason, or null for no reason
     * @throws NetworkException if an error occurs
     */
    void close(int code, String reason) throws NetworkException;
    
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
     * Sets the maximum frame size.
     * 
     * <p>This is the maximum size of WebSocket frames that can be received by this client.
     * 
     * @param size the maximum frame size in bytes
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if size is not positive
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withMaxFrameSize(int size);
    
    /**
     * Sets the maximum message size.
     * 
     * <p>This is the maximum size of WebSocket messages that can be received by this client.
     * 
     * @param size the maximum message size in bytes
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if size is not positive
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withMaxMessageSize(int size);
    
    /**
     * Sets whether to enable message compression.
     * 
     * @param compression true to enable compression, false to disable
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withCompression(boolean compression);
    
    /**
     * Adds a sub-protocol to be requested during the WebSocket handshake.
     * 
     * @param protocol the sub-protocol to add
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if protocol is null or empty
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withSubProtocol(String protocol);
    
    /**
     * Adds a custom header to be sent during the WebSocket handshake.
     * 
     * @param name the header name
     * @param value the header value
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if name is null or empty
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withHeader(String name, String value);
    
    /**
     * Sets the ping interval.
     * 
     * <p>If set to a positive duration, the client will automatically send ping
     * messages at this interval to keep the connection alive.
     * 
     * @param interval the ping interval, or Duration.ZERO to disable
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if interval is negative
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withPingInterval(java.time.Duration interval);
    
    /**
     * Sets the pong timeout.
     * 
     * <p>If a pong response is not received within this timeout after sending a ping,
     * the connection will be considered closed.
     * 
     * @param timeout the pong timeout
     * @return this client instance for method chaining
     * @throws IllegalArgumentException if timeout is negative
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withPongTimeout(java.time.Duration timeout);
    
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