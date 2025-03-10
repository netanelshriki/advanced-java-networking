package com.network.api.websocket;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a WebSocket message.
 * 
 * <p>This class encapsulates the data and metadata of a WebSocket message,
 * such as the message type, content, and timestamp.
 */
public class WebSocketMessage {
    
    /**
     * The type of WebSocket message.
     */
    public enum Type {
        /**
         * Text message.
         */
        TEXT,
        
        /**
         * Binary message.
         */
        BINARY,
        
        /**
         * Ping message.
         */
        PING,
        
        /**
         * Pong message.
         */
        PONG,
        
        /**
         * Close message.
         */
        CLOSE
    }
    
    private final Type type;
    private final byte[] data;
    private final String text;
    private final int closeCode;
    private final String closeReason;
    private final Instant timestamp;
    
    /**
     * Creates a new text message.
     * 
     * @param text the text content
     */
    public static WebSocketMessage text(String text) {
        return new WebSocketMessage(Type.TEXT, null, text, 0, null, Instant.now());
    }
    
    /**
     * Creates a new binary message.
     * 
     * @param data the binary content
     */
    public static WebSocketMessage binary(byte[] data) {
        return new WebSocketMessage(Type.BINARY, data, null, 0, null, Instant.now());
    }
    
    /**
     * Creates a new ping message.
     * 
     * @param data the ping content, or null for an empty ping
     */
    public static WebSocketMessage ping(byte[] data) {
        return new WebSocketMessage(Type.PING, data, null, 0, null, Instant.now());
    }
    
    /**
     * Creates a new pong message.
     * 
     * @param data the pong content, or null for an empty pong
     */
    public static WebSocketMessage pong(byte[] data) {
        return new WebSocketMessage(Type.PONG, data, null, 0, null, Instant.now());
    }
    
    /**
     * Creates a new close message.
     * 
     * @param code the close code
     * @param reason the close reason, or null
     */
    public static WebSocketMessage close(int code, String reason) {
        return new WebSocketMessage(Type.CLOSE, null, null, code, reason, Instant.now());
    }
    
    /**
     * Creates a new WebSocket message.
     * 
     * @param type the message type
     * @param data the binary content, or null for text messages
     * @param text the text content, or null for binary messages
     * @param closeCode the close code, or 0 for non-close messages
     * @param closeReason the close reason, or null for non-close messages
     * @param timestamp the timestamp when the message was created
     */
    private WebSocketMessage(Type type, byte[] data, String text, int closeCode, String closeReason, Instant timestamp) {
        this.type = type;
        this.data = data != null ? Arrays.copyOf(data, data.length) : null;
        this.text = text;
        this.closeCode = closeCode;
        this.closeReason = closeReason;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }
    
    /**
     * Gets the message type.
     * 
     * @return the message type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Gets the binary content.
     * 
     * <p>This method returns the binary content for binary, ping, and pong messages.
     * For text and close messages, it returns null.
     * 
     * @return the binary content, or null if not applicable
     */
    public byte[] getData() {
        return data != null ? Arrays.copyOf(data, data.length) : null;
    }
    
    /**
     * Gets the text content.
     * 
     * <p>This method returns the text content for text messages.
     * For binary, ping, pong, and close messages, it returns null.
     * 
     * @return the text content, or null if not applicable
     */
    public String getText() {
        return text;
    }
    
    /**
     * Gets the close code.
     * 
     * <p>This method returns the close code for close messages.
     * For other message types, it returns 0.
     * 
     * @return the close code, or 0 if not applicable
     */
    public int getCloseCode() {
        return closeCode;
    }
    
    /**
     * Gets the close reason.
     * 
     * <p>This method returns the close reason for close messages.
     * For other message types, it returns null.
     * 
     * @return the close reason, or null if not applicable
     */
    public String getCloseReason() {
        return closeReason;
    }
    
    /**
     * Gets the timestamp when this message was created.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Checks if this is a text message.
     * 
     * @return true if this is a text message, false otherwise
     */
    public boolean isText() {
        return type == Type.TEXT;
    }
    
    /**
     * Checks if this is a binary message.
     * 
     * @return true if this is a binary message, false otherwise
     */
    public boolean isBinary() {
        return type == Type.BINARY;
    }
    
    /**
     * Checks if this is a ping message.
     * 
     * @return true if this is a ping message, false otherwise
     */
    public boolean isPing() {
        return type == Type.PING;
    }
    
    /**
     * Checks if this is a pong message.
     * 
     * @return true if this is a pong message, false otherwise
     */
    public boolean isPong() {
        return type == Type.PONG;
    }
    
    /**
     * Checks if this is a close message.
     * 
     * @return true if this is a close message, false otherwise
     */
    public boolean isClose() {
        return type == Type.CLOSE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebSocketMessage that = (WebSocketMessage) o;
        return closeCode == that.closeCode &&
               type == that.type &&
               Arrays.equals(data, that.data) &&
               Objects.equals(text, that.text) &&
               Objects.equals(closeReason, that.closeReason) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(type, text, closeCode, closeReason, timestamp);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WebSocketMessage{type=").append(type);
        
        switch (type) {
            case TEXT:
                sb.append(", text='").append(text).append('\'');
                break;
            case BINARY:
                sb.append(", data=").append(data != null ? data.length + " bytes" : "null");
                break;
            case PING:
                sb.append(", pingData=").append(data != null ? data.length + " bytes" : "null");
                break;
            case PONG:
                sb.append(", pongData=").append(data != null ? data.length + " bytes" : "null");
                break;
            case CLOSE:
                sb.append(", closeCode=").append(closeCode);
                if (closeReason != null) {
                    sb.append(", closeReason='").append(closeReason).append('\'');
                }
                break;
        }
        
        sb.append(", timestamp=").append(timestamp).append('}');
        return sb.toString();
    }
}