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
 * <p>This interface defines the operations for a WebSocket client that can
 * establish connections, send and receive messages, and register event listeners.
 */
public interface WebSocketClient extends NetworkClient {

    /**
     * Gets the URI this client is connected or will connect to.
     * 
     * @return the URI
     */
    URI getUri();
    
    /**
     * Gets the subprotocols that this client supports.
     * 
     * @return the supported subprotocols
     */
    String[] getSubprotocols();
    
    /**
     * Gets the negotiated subprotocol.
     * 
     * <p>This is the subprotocol that was agreed upon during the WebSocket
     * handshake, or null if not connected or no subprotocol was negotiated.
     * 
     * @return the negotiated subprotocol, or null if not available
     */
    String getNegotiatedSubprotocol();
    
    /**
     * Sends a text message.
     * 
     * @param message the message to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendText(String message) throws NetworkException;
    
    /**
     * Sends a text message asynchronously.
     * 
     * @param message the message to send
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendTextAsync(String message);
    
    /**
     * Sends a binary message.
     * 
     * @param data the data to send
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendBinary(byte[] data) throws NetworkException;
    
    /**
     * Sends a binary message asynchronously.
     * 
     * @param data the data to send
     * @return a CompletableFuture that completes when the message has been sent
     */
    CompletableFuture<Void> sendBinaryAsync(byte[] data);
    
    /**
     * Sends a ping message.
     * 
     * <p>The server should respond with a pong message.
     * 
     * @param data the optional data to include in the ping, or null
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendPing(byte[] data) throws NetworkException;
    
    /**
     * Sends a ping message asynchronously.
     * 
     * <p>The server should respond with a pong message.
     * 
     * @param data the optional data to include in the ping, or null
     * @return a CompletableFuture that completes when the ping has been sent
     */
    CompletableFuture<Void> sendPingAsync(byte[] data);
    
    /**
     * Sends a pong message in response to a ping.
     * 
     * @param data the data from the ping message, or null
     * @throws NetworkException if an error occurs or if not connected
     */
    void sendPong(byte[] data) throws NetworkException;
    
    /**
     * Sends a pong message in response to a ping asynchronously.
     * 
     * @param data the data from the ping message, or null
     * @return a CompletableFuture that completes when the pong has been sent
     */
    CompletableFuture<Void> sendPongAsync(byte[] data);
    
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
     * <p>The client will automatically respond with a pong message.
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
     * Sets headers to include in the WebSocket handshake.
     * 
     * @param headers the headers
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withHeaders(Map<String, String> headers);
    
    /**
     * Sets a header to include in the WebSocket handshake.
     * 
     * @param name the header name
     * @param value the header value
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withHeader(String name, String value);
    
    /**
     * Sets the subprotocols that this client supports.
     * 
     * <p>The server will select one of these protocols during the WebSocket
     * handshake, or none if it doesn't support any of them.
     * 
     * @param subprotocols the supported subprotocols
     * @return this client instance for method chaining
     * @throws IllegalStateException if the client is already connected
     */
    WebSocketClient withSubprotocols(String... subprotocols);
    
    /**
     * Sets whether to enable compression for WebSocket messages.
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