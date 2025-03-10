package com.network.exception;

import java.time.Duration;

/**
 * Exception thrown when a timeout occurs.
 * 
 * <p>This exception provides information about the operation that timed out
 * and the timeout duration.
 */
public class TimeoutException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    private final String operation;
    private final Duration timeout;
    
    /**
     * Creates a new timeout exception with the specified message.
     * 
     * @param message the detail message
     */
    public TimeoutException(String message) {
        super(ErrorCode.CONNECTION_TIMEOUT, message);
        this.operation = "unknown";
        this.timeout = null;
    }
    
    /**
     * Creates a new timeout exception with the specified operation.
     * 
     * @param operation the operation that timed out
     */
    public TimeoutException(String operation, Duration timeout) {
        super(ErrorCode.CONNECTION_TIMEOUT, "Timeout occurred for operation '" + operation + "' after " + timeout);
        this.operation = operation;
        this.timeout = timeout;
    }
    
    /**
     * Creates a new timeout exception with the specified operation and cause.
     * 
     * @param operation the operation that timed out
     * @param timeout the timeout duration
     * @param cause the cause of this exception
     */
    public TimeoutException(String operation, Duration timeout, Throwable cause) {
        super(ErrorCode.CONNECTION_TIMEOUT, "Timeout occurred for operation '" + operation + "' after " + timeout, cause);
        this.operation = operation;
        this.timeout = timeout;
    }
    
    /**
     * Creates a new timeout exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public TimeoutException(String message, Throwable cause) {
        super(ErrorCode.CONNECTION_TIMEOUT, message, cause);
        this.operation = "unknown";
        this.timeout = null;
    }
    
    /**
     * Gets the operation that timed out.
     * 
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Gets the timeout duration.
     * 
     * @return the timeout duration, or null if not available
     */
    public Duration getTimeout() {
        return timeout;
    }
}