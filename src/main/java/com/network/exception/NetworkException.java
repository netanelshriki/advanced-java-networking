package com.network.exception;

/**
 * Base exception for all network-related exceptions in the library.
 * 
 * <p>This class serves as the parent for all specific networking exceptions
 * thrown by the library. It provides common functionality for all networking
 * exceptions, such as error codes and categorization.
 */
public class NetworkException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final ErrorCode errorCode;
    private final ErrorCategory category;
    
    /**
     * Creates a new network exception with the specified message.
     * 
     * @param message the detail message
     */
    public NetworkException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNKNOWN;
        this.category = ErrorCategory.UNKNOWN;
    }
    
    /**
     * Creates a new network exception with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public NetworkException(Throwable cause) {
        super(cause);
        this.errorCode = ErrorCode.UNKNOWN;
        this.category = ErrorCategory.UNKNOWN;
    }
    
    /**
     * Creates a new network exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.UNKNOWN;
        this.category = ErrorCategory.UNKNOWN;
    }
    
    /**
     * Creates a new network exception with the specified error code.
     * 
     * @param errorCode the error code
     */
    public NetworkException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.category = errorCode.getCategory();
    }
    
    /**
     * Creates a new network exception with the specified error code and cause.
     * 
     * @param errorCode the error code
     * @param cause the cause of this exception
     */
    public NetworkException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.category = errorCode.getCategory();
    }
    
    /**
     * Creates a new network exception with the specified error code and detail message.
     * 
     * @param errorCode the error code
     * @param message the detail message
     */
    public NetworkException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.category = errorCode.getCategory();
    }
    
    /**
     * Creates a new network exception with the specified error code, detail message, and cause.
     * 
     * @param errorCode the error code
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public NetworkException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = errorCode.getCategory();
    }
    
    /**
     * Gets the error code associated with this exception.
     * 
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the error category of this exception.
     * 
     * @return the error category
     */
    public ErrorCategory getCategory() {
        return category;
    }
    
    /**
     * Enum representing the different categories of network errors.
     */
    public enum ErrorCategory {
        /**
         * Errors related to connection establishment or management.
         */
        CONNECTION,
        
        /**
         * Errors related to configuration.
         */
        CONFIGURATION,
        
        /**
         * Errors related to data transmission.
         */
        TRANSMISSION,
        
        /**
         * Errors related to protocol handling.
         */
        PROTOCOL,
        
        /**
         * Errors related to security.
         */
        SECURITY,
        
        /**
         * Timeout errors.
         */
        TIMEOUT,
        
        /**
         * Input/output errors.
         */
        IO,
        
        /**
         * Unknown or unspecified errors.
         */
        UNKNOWN
    }
    
    /**
     * Enum representing specific error codes.
     */
    public enum ErrorCode {
        // Connection errors
        CONNECTION_REFUSED(1001, "Connection refused", ErrorCategory.CONNECTION),
        CONNECTION_RESET(1002, "Connection reset", ErrorCategory.CONNECTION),
        CONNECTION_TIMEOUT(1003, "Connection timeout", ErrorCategory.TIMEOUT),
        CONNECTION_CLOSED(1004, "Connection closed", ErrorCategory.CONNECTION),
        CONNECTION_ERROR(1005, "Connection error", ErrorCategory.CONNECTION),
        
        // Configuration errors
        INVALID_ADDRESS(2001, "Invalid address", ErrorCategory.CONFIGURATION),
        INVALID_PORT(2002, "Invalid port", ErrorCategory.CONFIGURATION),
        INVALID_TIMEOUT(2003, "Invalid timeout", ErrorCategory.CONFIGURATION),
        INVALID_CONFIGURATION(2004, "Invalid configuration", ErrorCategory.CONFIGURATION),
        
        // Transmission errors
        SEND_ERROR(3001, "Error sending data", ErrorCategory.TRANSMISSION),
        RECEIVE_ERROR(3002, "Error receiving data", ErrorCategory.TRANSMISSION),
        
        // Protocol errors
        PROTOCOL_ERROR(4001, "Protocol error", ErrorCategory.PROTOCOL),
        INVALID_RESPONSE(4002, "Invalid response", ErrorCategory.PROTOCOL),
        
        // Security errors
        AUTHENTICATION_FAILED(5001, "Authentication failed", ErrorCategory.SECURITY),
        CERTIFICATE_ERROR(5002, "Certificate error", ErrorCategory.SECURITY),
        
        // I/O errors
        IO_ERROR(6001, "I/O error", ErrorCategory.IO),
        
        // Unknown errors
        UNKNOWN(9999, "Unknown error", ErrorCategory.UNKNOWN);
        
        private final int code;
        private final String message;
        private final ErrorCategory category;
        
        ErrorCode(int code, String message, ErrorCategory category) {
            this.code = code;
            this.message = message;
            this.category = category;
        }
        
        /**
         * Gets the numeric code for this error.
         * 
         * @return the numeric code
         */
        public int getCode() {
            return code;
        }
        
        /**
         * Gets the default message for this error.
         * 
         * @return the default message
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Gets the category of this error.
         * 
         * @return the error category
         */
        public ErrorCategory getCategory() {
            return category;
        }
    }
}