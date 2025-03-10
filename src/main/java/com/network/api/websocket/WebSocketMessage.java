package com.network.api.websocket;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a WebSocket message.
 * 
 * <p>This class encapsulates the data and metadata of a WebSocket message,
 * such as the message type, payload, and timestamp.
 */
public class WebSocketMessage {
    
    private final MessageType type;
    private final Object payload;
    private final Instant receiveTime;
    
    /**
     * Creates a new WebSocket message with the current time.
     * 
     * @param type the message type
     * @param payload the message payload
     */
    public WebSocketMessage(MessageType type, Object payload) {
        this(type, payload, Instant.now());
    }
    
    /**
     * Creates a new WebSocket message with a specific receive time.
     * 
     * @param type the message type
     * @param payload the message payload
     * @param receiveTime the time the message was received
     */
    public WebSocketMessage(MessageType type, Object payload, Instant receiveTime) {
        this.type = type;
        this.payload = payload;
        this.receiveTime = receiveTime != null ? receiveTime : Instant.now();
    }
    
    /**
     * Gets the message type.
     * 
     * @return the message type
     */
    public MessageType getType() {
        return type;
    }
    
    /**
     * Gets the message payload.
     * 
     * <p>The payload type depends on the message type:
     * <ul>
     *   <li>TEXT: String</li>
     *   <li>BINARY: byte[]</li>
     *   <li>PING: byte[]</li>
     *   <li>PONG: byte[]</li>
     * </ul>
     * 
     * @return the payload
     */
    public Object getPayload() {
        return payload;
    }
    
    /**
     * Gets the message payload as a string.
     * 
     * @return the payload as a string, or null if the payload is not a string
     *         or the message type is not TEXT
     */
    public String getTextPayload() {
        if (type == MessageType.TEXT && payload instanceof String) {
            return (String) payload;
        }
        return null;
    }
    
    /**
     * Gets the message payload as bytes.
     * 
     * @return the payload as bytes, or null if the payload is not bytes
     *         or the message type is not BINARY, PING, or PONG
     */
    public byte[] getBinaryPayload() {
        if ((type == MessageType.BINARY || type == MessageType.PING || type == MessageType.PONG) 
                && payload instanceof byte[]) {
            return (byte[]) payload;
        }
        return null;
    }
    
    /**
     * Gets the time the message was received.
     * 
     * @return the receive time
     */
    public Instant getReceiveTime() {
        return receiveTime;
    }
    
    /**
     * Checks if this message is a text message.
     * 
     * @return true if the message type is TEXT, false otherwise
     */
    public boolean isText() {
        return type == MessageType.TEXT;
    }
    
    /**
     * Checks if this message is a binary message.
     * 
     * @return true if the message type is BINARY, false otherwise
     */
    public boolean isBinary() {
        return type == MessageType.BINARY;
    }
    
    /**
     * Checks if this message is a ping message.
     * 
     * @return true if the message type is PING, false otherwise
     */
    public boolean isPing() {
        return type == MessageType.PING;
    }
    
    /**
     * Checks if this message is a pong message.
     * 
     * @return true if the message type is PONG, false otherwise
     */
    public boolean isPong() {
        return type == MessageType.PONG;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebSocketMessage that = (WebSocketMessage) o;
        
        boolean payloadEquals;
        if (payload instanceof byte[] && that.payload instanceof byte[]) {
            payloadEquals = Arrays.equals((byte[]) payload, (byte[]) that.payload);
        } else {
            payloadEquals = Objects.equals(payload, that.payload);
        }
        
        return type == that.type &&
               payloadEquals &&
               Objects.equals(receiveTime, that.receiveTime);
    }
    
    @Override
    public int hashCode() {
        int payloadHash;
        if (payload instanceof byte[]) {
            payloadHash = Arrays.hashCode((byte[]) payload);
        } else {
            payloadHash = Objects.hashCode(payload);
        }
        
        return Objects.hash(type, payloadHash, receiveTime);
    }
    
    @Override
    public String toString() {
        String payloadStr;
        if (payload instanceof byte[]) {
            payloadStr = "binary[" + ((byte[]) payload).length + " bytes]";
        } else {
            payloadStr = String.valueOf(payload);
        }
        
        return "WebSocketMessage{" +
               "type=" + type +
               ", payload=" + payloadStr +
               ", receiveTime=" + receiveTime +
               '}';
    }
    
    /**
     * Enum representing WebSocket message types.
     */
    public enum MessageType {
        /** Text message. */
        TEXT,
        
        /** Binary message. */
        BINARY,
        
        /** Ping message. */
        PING,
        
        /** Pong message. */
        PONG
    }
    
    /**
     * Creates a new text message.
     * 
     * @param text the text payload
     * @return a new text message
     */
    public static WebSocketMessage text(String text) {
        return new WebSocketMessage(MessageType.TEXT, text);
    }
    
    /**
     * Creates a new binary message.
     * 
     * @param data the binary payload
     * @return a new binary message
     */
    public static WebSocketMessage binary(byte[] data) {
        return new WebSocketMessage(MessageType.BINARY, data);
    }
    
    /**
     * Creates a new ping message.
     * 
     * @param data the ping payload, or null for an empty ping
     * @return a new ping message
     */
    public static WebSocketMessage ping(byte[] data) {
        return new WebSocketMessage(MessageType.PING, data != null ? data : new byte[0]);
    }
    
    /**
     * Creates a new pong message.
     * 
     * @param data the pong payload, or null for an empty pong
     * @return a new pong message
     */
    public static WebSocketMessage pong(byte[] data) {
        return new WebSocketMessage(MessageType.PONG, data != null ? data : new byte[0]);
    }
}