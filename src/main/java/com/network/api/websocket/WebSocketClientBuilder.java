package com.network.api.websocket;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

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
     * Sets the URI to connect to.
     * 
     * @param uri the URI
     * @return this builder
     * @throws IllegalArgumentException if uri is null
     */
    WebSocketClientBuilder withUri(URI uri);
    
    /**
     * Sets the URI to connect to.
     * 
     * @param uri the URI as a string
     * @return this builder
     * @throws IllegalArgumentException if uri is null or not a valid URI
     */
    WebSocketClientBuilder withUrl(String uri);
    
    /**
     * Sets the maximum frame size.
     * 
     * <p>This is the maximum size of WebSocket frames that can be received by this client.
     * 
     * @param size the maximum frame size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    WebSocketClientBuilder withMaxFrameSize(int size);
    
    /**
     * Sets the maximum message size.
     * 
     * <p>This is the maximum size of WebSocket messages that can be received by this client.
     * 
     * @param size the maximum message size in bytes
     * @return this builder
     * @throws IllegalArgumentException if size is not positive
     */
    WebSocketClientBuilder withMaxMessageSize(int size);
    
    /**
     * Sets whether to enable message compression.
     * 
     * @param compression true to enable compression, false to disable
     * @return this builder
     */
    WebSocketClientBuilder withCompression(boolean compression);
    
    /**
     * Adds a sub-protocol to be requested during the WebSocket handshake.
     * 
     * @param protocol the sub-protocol to add
     * @return this builder
     * @throws IllegalArgumentException if protocol is null or empty
     */
    WebSocketClientBuilder withSubProtocol(String protocol);
    
    /**
     * Adds sub-protocols to be requested during the WebSocket handshake.
     * 
     * @param protocols the sub-protocols to add
     * @return this builder
     * @throws IllegalArgumentException if protocols is null
     */
    WebSocketClientBuilder withSubProtocols(String... protocols);
    
    /**
     * Adds a custom header to be sent during the WebSocket handshake.
     * 
     * @param name the header name
     * @param value the header value
     * @return this builder
     * @throws IllegalArgumentException if name is null or empty
     */
    WebSocketClientBuilder withHeader(String name, String value);
    
    /**
     * Adds custom headers to be sent during the WebSocket handshake.
     * 
     * @param headers the headers to add
     * @return this builder
     * @throws IllegalArgumentException if headers is null
     */
    WebSocketClientBuilder withHeaders(Map<String, String> headers);
    
    /**
     * Sets whether to automatically reconnect when the connection is lost.
     * 
     * @param autoReconnect true to enable auto-reconnect, false to disable
     * @return this builder
     */
    WebSocketClientBuilder withAutoReconnect(boolean autoReconnect);
    
    /**
     * Sets the auto-reconnect backoff strategy.
     * 
     * <p>The backoff strategy determines how long to wait before attempting
     * to reconnect after a connection failure.
     * 
     * @param initialBackoff the initial backoff duration
     * @param maxBackoff the maximum backoff duration
     * @param strategy the backoff strategy
     * @return this builder
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    WebSocketClientBuilder withReconnectBackoff(Duration initialBackoff, Duration maxBackoff, RetryBackoffStrategy strategy);
    
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
     * Sets the ping interval.
     * 
     * <p>If set to a positive duration, the client will automatically send ping
     * messages at this interval to keep the connection alive.
     * 
     * @param interval the ping interval, or Duration.ZERO to disable
     * @return this builder
     * @throws IllegalArgumentException if interval is negative
     */
    WebSocketClientBuilder withPingInterval(Duration interval);
    
    /**
     * Sets the pong timeout.
     * 
     * <p>If a pong response is not received within this timeout after sending a ping,
     * the connection will be considered closed.
     * 
     * @param timeout the pong timeout
     * @return this builder
     * @throws IllegalArgumentException if timeout is negative
     */
    WebSocketClientBuilder withPongTimeout(Duration timeout);
    
    /**
     * Sets whether to verify SSL certificates.
     * 
     * @param verify true to verify, false to not
     * @return this builder
     */
    WebSocketClientBuilder withVerifySsl(boolean verify);
    
    /**
     * Sets the SSL context to use for secure connections.
     * 
     * @param sslContext the SSL context
     * @return this builder
     * @throws IllegalArgumentException if sslContext is null
     */
    WebSocketClientBuilder withSslContext(SSLContext sslContext);
    
    /**
     * Sets the default serializer for data conversion.
     * 
     * <p>The serializer is used to convert between bytes and objects when
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