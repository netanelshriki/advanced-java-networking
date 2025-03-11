package com.network.api.websocket;

/**
 * Enum representing standard WebSocket close codes as defined in
 * <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455 Section 7.4.1</a>.
 */
public enum WebSocketCloseCode {

    /**
     * Normal closure; the connection successfully completed whatever purpose for which it was created.
     */
    NORMAL_CLOSURE(1000),
    
    /**
     * The endpoint is going away, either because of a server failure or because the browser is navigating away from the page that opened the connection.
     */
    GOING_AWAY(1001),
    
    /**
     * The endpoint is terminating the connection due to a protocol error.
     */
    PROTOCOL_ERROR(1002),
    
    /**
     * The connection is being terminated because the endpoint received data of a type it cannot accept (e.g., a text-only endpoint received binary data).
     */
    UNSUPPORTED_DATA(1003),
    
    /**
     * Reserved. The specific meaning might be defined in the future.
     */
    RESERVED(1004),
    
    /**
     * No status code was provided even though one was expected.
     */
    NO_STATUS_RECEIVED(1005),
    
    /**
     * The connection was closed abnormally, e.g., without sending or receiving a Close control frame.
     */
    ABNORMAL_CLOSURE(1006),
    
    /**
     * The endpoint is terminating the connection because a message was received that contained inconsistent data.
     */
    INVALID_FRAME_PAYLOAD_DATA(1007),
    
    /**
     * The endpoint is terminating the connection because it received a message that violates its policy.
     */
    POLICY_VIOLATION(1008),
    
    /**
     * The endpoint is terminating the connection because a data frame was received that is too large.
     */
    MESSAGE_TOO_BIG(1009),
    
    /**
     * The client is terminating the connection because it expected the server to negotiate one or more extensions, but the server didn't.
     */
    MANDATORY_EXTENSION(1010),
    
    /**
     * The server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request.
     */
    INTERNAL_ERROR(1011),
    
    /**
     * The server is terminating the connection because it is restarting.
     */
    SERVICE_RESTART(1012),
    
    /**
     * The server is terminating the connection due to a temporary condition.
     */
    TRY_AGAIN_LATER(1013),
    
    /**
     * The server was acting as a gateway or proxy and received an invalid response from the upstream server.
     */
    BAD_GATEWAY(1014),
    
    /**
     * The connection was closed due to a failure to perform a TLS handshake.
     */
    TLS_HANDSHAKE_FAILURE(1015);
    
    private final int code;
    
    WebSocketCloseCode(int code) {
        this.code = code;
    }
    
    /**
     * Gets the numeric close code.
     * 
     * @return the numeric close code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Gets the WebSocketCloseCode enum value for the specified numeric code.
     * 
     * @param code the numeric code
     * @return the corresponding enum value, or null if not found
     */
    public static WebSocketCloseCode fromCode(int code) {
        for (WebSocketCloseCode closeCode : values()) {
            if (closeCode.getCode() == code) {
                return closeCode;
            }
        }
        return null;
    }
}
