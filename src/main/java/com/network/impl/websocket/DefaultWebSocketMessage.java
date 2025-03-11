package com.network.impl.websocket;

import com.network.api.websocket.WebSocketMessage;
import com.network.api.websocket.WebSocketMessageType;

/**
 * Default implementation of the {@link WebSocketMessage} interface.
 */
class DefaultWebSocketMessage implements WebSocketMessage {

    private final WebSocketMessageType type;
    private final String textContent;
    private final byte[] binaryContent;
    
    /**
     * Creates a new DefaultWebSocketMessage.
     * 
     * @param type          the message type
     * @param textContent   the text content or null for binary messages
     * @param binaryContent the binary content or null for text messages
     */
    DefaultWebSocketMessage(WebSocketMessageType type, String textContent, byte[] binaryContent) {
        this.type = type;
        this.textContent = textContent;
        this.binaryContent = binaryContent;
    }

    @Override
    public WebSocketMessageType getType() {
        return type;
    }

    @Override
    public String getTextContent() {
        return textContent;
    }

    @Override
    public byte[] getBinaryContent() {
        return binaryContent;
    }

    @Override
    public boolean isText() {
        return type == WebSocketMessageType.TEXT;
    }

    @Override
    public boolean isBinary() {
        return type == WebSocketMessageType.BINARY;
    }

    @Override
    public String toString() {
        if (isText()) {
            return "TextMessage: " + textContent;
        } else {
            return "BinaryMessage: " + (binaryContent != null ? binaryContent.length + " bytes" : "null");
        }
    }
}
