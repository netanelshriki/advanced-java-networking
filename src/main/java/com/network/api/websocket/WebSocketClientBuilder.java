package com.network.api.websocket;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;

import com.network.config.NetworkConfigBuilder;
import com.network.serialization.Serializer;

/**
 * Builder for WebSocket clients.
 * 
 * <p>This interface defines the methods for building WebSocket clients with
 * various configuration options.
 */
public interface WebSocketClientBuilder extends NetworkConfigBuilder<WebSocketClientBuilder, WebSocketClientConfig> {
    
    /**
     * Sets the URL for the WebSocket connection.
     * 
     * @param url the WebSocket URL
     * @return this builder
     * @throws IllegalArgumentException if url is null
     */
    WebSocketClientBuilder withUrl(URL url);
    
    /**
     * Sets the URL for the WebSocket connection.
     * 
     * @param url the WebSocket URL as a string
     * @return this builder
     * @throws IllegalArgumentException if url is null or not a valid URL
     */
    WebSocketClientBuilder withUrl(String url);
    
    /**
     * Sets an HTTP header to include in the WebSocket handshake request.
     * 
     * @param name the header name
     * @param value the header value
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    WebSocketClientBuilder withHeader(String name, String value);
    
    /**
     * Sets the HTTP headers to include in the WebSocket handshake request.
     * 
     * @param headers the headers
     * @return this builder
     * @throws IllegalArgumentException if headers is null
     */
    WebSocketClientBuilder withHeaders(Map<String, String> headers);
    
    /**
     * Sets the WebSocket subprotocols to request.
     * 
     * @param subprotocols the subprotocols
     * @return this builder
     */
    WebSocketClientBuilder withSubprotocols(String... subprotocols);
    
    /**
     * Sets the compression enabled flag.
     * 
     * <p>If enabled, the client will negotiate compression with the server.
     * 
     * @param compression true to enable compression, false to disable
     * @return this builder
     */
    WebSocketClientBuilder withCompression(boolean compression);
    
    /**
     * Sets the maximum message size.
     * 
     * <p>Messages larger than this size will be rejected.
     * 
     * @param maxMessageSize the maximum message size in bytes
     * @return this builder
     * @throws IllegalArgumentException if maxMessageSize is not positive
     */
    WebSocketClientBuilder withMaxMessageSize(int maxMessageSize);
    
    /**
     * Sets the maximum frame size.
     * 
     * <p>Frames larger than this size will be rejected.
     * 
     * @param maxFrameSize the maximum frame size in bytes
     * @return this builder
     * @throws IllegalArgumentException if maxFrameSize is not positive
     */
    WebSocketClientBuilder withMaxFrameSize(int maxFrameSize);
    
    /**
     * Sets whether to automatically reconnect when the connection is lost.
     * 
     * @param autoReconnect true to enable auto-reconnect, false to disable
     * @return this builder
     */
    WebSocketClientBuilder withAutoReconnect(boolean autoReconnect);
    
    /**
     * Sets the maximum number of reconnect attempts.
     * 
     * <p>After reaching this number of attempts, the client will stop trying
     * to reconnect. A value of 0 means unlimited attempts.
     * 
     * @param maxAttempts the maximum number of reconnect attempts
     * @return this builder
     * @throws IllegalArgumentException if maxAttempts is negative
     */
    WebSocketClientBuilder withMaxReconnectAttempts(int maxAttempts);
    
    /**
     * Sets whether to automatically connect when the client is built.
     * 
     * <p>If true, the client will attempt to connect when built.
     * If false, the client will not connect until {@link WebSocketClient#connect()}
     * is called.
     * 
     * @param autoConnect true to enable auto-connect, false to disable
     * @return this builder
     */
    WebSocketClientBuilder withAutoConnect(boolean autoConnect);
    
    /**
     * Sets the default serializer for data conversion.
     * 
     * <p>The serializer is used to convert between JSON strings and objects when
     * using the serialization methods.
     * 
     * @param serializer the serializer to use
     * @return this builder
     * @throws IllegalArgumentException if serializer is null
     */
    WebSocketClientBuilder withSerializer(Serializer serializer);
    
    /**
     * Sets the charset for string conversions.
     * 
     * <p>The charset is used to convert between strings and bytes when
     * using the string methods.
     * 
     * @param charset the charset to use
     * @return this builder
     * @throws IllegalArgumentException if charset is null
     */
    WebSocketClientBuilder withCharset(Charset charset);
    
    /**
     * Configures the client using the given consumer.
     * 
     * <p>This method allows for more complex configuration that may require
     * multiple builder calls.
     * 
     * @param configurer the configurer
     * @return this builder
     * @throws IllegalArgumentException if configurer is null
     */
    WebSocketClientBuilder configure(Consumer<WebSocketClientBuilder> configurer);
    
    /**
     * Builds the WebSocket client.
     * 
     * @return the built WebSocket client
     * @throws IllegalStateException if the builder is not properly configured
     */
    WebSocketClient build();
}