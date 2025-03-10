package com.network.exception;

import java.net.SocketAddress;

/**
 * Exception thrown when a connection-related error occurs.
 * 
 * <p>This exception provides additional information about the connection
 * for which the error occurred, such as the remote and local addresses.
 */
public class ConnectionException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;
    private final String connectionId;
    
    /**
     * Creates a new connection exception with the specified message.
     * 
     * @param message the detail message
     */
    public ConnectionException(String message) {
        super(message);
        this.remoteAddress = null;
        this.localAddress = null;
        this.connectionId = null;
    }
    
    /**
     * Creates a new connection exception with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public ConnectionException(Throwable cause) {
        super(cause);
        this.remoteAddress = null;
        this.localAddress = null;
        this.connectionId = null;
    }
    
    /**
     * Creates a new connection exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.remoteAddress = null;
        this.localAddress = null;
        this.connectionId = null;
    }
    
    /**
     * Creates a new connection exception with the specified error code.
     * 
     * @param errorCode the error code
     */
    public ConnectionException(ErrorCode errorCode) {
        super(errorCode);
        this.remoteAddress = null;
        this.localAddress = null;
        this.connectionId = null;
    }
    
    /**
     * Creates a new connection exception with the specified error code and cause.
     * 
     * @param errorCode the error code
     * @param cause the cause of this exception
     */
    public ConnectionException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.remoteAddress = null;
        this.localAddress = null;
        this.connectionId = null;
    }
    
    /**
     * Creates a new connection exception with the specified error code, remote address, and local address.
     * 
     * @param errorCode the error code
     * @param remoteAddress the remote address of the connection
     * @param localAddress the local address of the connection
     */
    public ConnectionException(ErrorCode errorCode, SocketAddress remoteAddress, SocketAddress localAddress) {
        super(errorCode);
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.connectionId = null;
    }
    
    /**
     * Creates a new connection exception with the specified error code, remote address, local address, and cause.
     * 
     * @param errorCode the error code
     * @param remoteAddress the remote address of the connection
     * @param localAddress the local address of the connection
     * @param cause the cause of this exception
     */
    public ConnectionException(ErrorCode errorCode, SocketAddress remoteAddress, SocketAddress localAddress, Throwable cause) {
        super(errorCode, cause);
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.connectionId = null;
    }
    
    /**
     * Creates a new connection exception with the specified error code, remote address, local address, and connection ID.
     * 
     * @param errorCode the error code
     * @param remoteAddress the remote address of the connection
     * @param localAddress the local address of the connection
     * @param connectionId the ID of the connection
     */
    public ConnectionException(ErrorCode errorCode, SocketAddress remoteAddress, SocketAddress localAddress, String connectionId) {
        super(errorCode);
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.connectionId = connectionId;
    }
    
    /**
     * Creates a new connection exception with the specified error code, remote address, local address, connection ID, and cause.
     * 
     * @param errorCode the error code
     * @param remoteAddress the remote address of the connection
     * @param localAddress the local address of the connection
     * @param connectionId the ID of the connection
     * @param cause the cause of this exception
     */
    public ConnectionException(ErrorCode errorCode, SocketAddress remoteAddress, SocketAddress localAddress, String connectionId, Throwable cause) {
        super(errorCode, cause);
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.connectionId = connectionId;
    }
    
    /**
     * Creates a new connection exception with the specified error code, custom message, remote address, local address, and connection ID.
     * 
     * @param errorCode the error code
     * @param message the detail message
     * @param remoteAddress the remote address of the connection
     * @param localAddress the local address of the connection
     * @param connectionId the ID of the connection
     */
    public ConnectionException(ErrorCode errorCode, String message, SocketAddress remoteAddress, SocketAddress localAddress, String connectionId) {
        super(errorCode, message);
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.connectionId = connectionId;
    }
    
    /**
     * Creates a new connection exception with the specified error code, custom message, remote address, local address, connection ID, and cause.
     * 
     * @param errorCode the error code
     * @param message the detail message
     * @param remoteAddress the remote address of the connection
     * @param localAddress the local address of the connection
     * @param connectionId the ID of the connection
     * @param cause the cause of this exception
     */
    public ConnectionException(ErrorCode errorCode, String message, SocketAddress remoteAddress, SocketAddress localAddress, String connectionId, Throwable cause) {
        super(errorCode, message, cause);
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.connectionId = connectionId;
    }
    
    /**
     * Gets the remote address of the connection.
     * 
     * @return the remote address, or null if not available
     */
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    /**
     * Gets the local address of the connection.
     * 
     * @return the local address, or null if not available
     */
    public SocketAddress getLocalAddress() {
        return localAddress;
    }
    
    /**
     * Gets the ID of the connection.
     * 
     * @return the connection ID, or null if not available
     */
    public String getConnectionId() {
        return connectionId;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (remoteAddress != null || localAddress != null || connectionId != null) {
            sb.append(" [");
            
            if (connectionId != null) {
                sb.append("connectionId=").append(connectionId);
                if (remoteAddress != null || localAddress != null) {
                    sb.append(", ");
                }
            }
            
            if (remoteAddress != null) {
                sb.append("remoteAddress=").append(remoteAddress);
                if (localAddress != null) {
                    sb.append(", ");
                }
            }
            
            if (localAddress != null) {
                sb.append("localAddress=").append(localAddress);
            }
            
            sb.append("]");
        }
        
        return sb.toString();
    }
}