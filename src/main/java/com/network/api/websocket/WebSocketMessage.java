package com.network.api.websocket;

/**
 * Represents a WebSocket message.
 * 
 * <p>A WebSocket message can contain either text or binary data.
 */
public interface WebSocketMessage {

    /**
     * Gets the type of this message.
     * 
     * @return the message type
     */
    WebSocketMessageType getType();
    
    /**
     * Gets the text content of this message.
     * 
     * @return the text content, or null if this is a binary message
     */
    String getTextContent();
    
    /**
     * Gets the binary content of this message.
     * 
     * @return the binary content, or null if this is a text message
     */
    byte[] getBinaryContent();
    
    /**
     * Checks if this message contains text.
     * 
     * @return true if this message contains text, false otherwise
     */
    boolean isText();
    
    /**
     * Checks if this message contains binary data.
     * 
     * @return true if this message contains binary data, false otherwise
     */
    boolean isBinary();
}
