package com.network.api.websocket;

import java.net.URI;
import java.util.Map;

/**
 * Builder for creating {@link WebSocketClient} instances.
 */
public interface WebSocketClientBuilder {

    /**
     * Builds a new {@link WebSocketClient} with the current settings.
     * 
     * @return a new WebSocket client
     */
    WebSocketClient build();
    
    /**
     * Sets the URL for the WebSocket connection.
     * 
     * @param url the URL (e.g., "ws://example.com/socket")
     * @return this builder instance
     * @throws IllegalArgumentException if the URL is invalid
     */
    WebSocketClientBuilder withUrl(String url);
    
    /**
     * Sets the URI for the WebSocket connection.
     * 
     * @param uri the URI
     * @return this builder instance
     */
    WebSocketClientBuilder withUri(URI uri);
    
    /**
     * Adds a header to the WebSocket handshake request.
     * 
     * @param name the header name
     * @param value the header value
     * @return this builder instance
     */
    WebSocketClientBuilder withHeader(String name, String value);
    
    /**
     * Adds multiple headers to the WebSocket handshake request.
     * 
     * @param headers the headers to add
     * @return this builder instance
     */
    WebSocketClientBuilder withHeaders(Map<String, String> headers);
}
