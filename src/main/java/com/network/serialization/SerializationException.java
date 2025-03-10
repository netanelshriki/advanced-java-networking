package com.network.serialization;

import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;

/**
 * Exception thrown when an error occurs during serialization or deserialization.
 */
public class SerializationException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new serialization exception with the specified message.
     * 
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(ErrorCode.PROTOCOL_ERROR, "Serialization error: " + message);
    }
    
    /**
     * Creates a new serialization exception with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public SerializationException(Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, "Serialization error", cause);
    }
    
    /**
     * Creates a new serialization exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, "Serialization error: " + message, cause);
    }
    
    /**
     * Creates a new serialization exception for a specific class and operation.
     * 
     * @param targetClass the class being serialized or deserialized
     * @param operation the operation being performed ("serialize" or "deserialize")
     * @param cause the cause of this exception
     */
    public SerializationException(Class<?> targetClass, String operation, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, 
            "Failed to " + operation + " class " + targetClass.getName(), cause);
    }
    
    /**
     * Creates a new serialization exception for a specific class, operation, and reason.
     * 
     * @param targetClass the class being serialized or deserialized
     * @param operation the operation being performed ("serialize" or "deserialize")
     * @param reason the reason for the failure
     */
    public SerializationException(Class<?> targetClass, String operation, String reason) {
        super(ErrorCode.PROTOCOL_ERROR, 
            "Failed to " + operation + " class " + targetClass.getName() + ": " + reason);
    }
    
    /**
     * Creates a new serialization exception for a specific class, operation, reason, and cause.
     * 
     * @param targetClass the class being serialized or deserialized
     * @param operation the operation being performed ("serialize" or "deserialize")
     * @param reason the reason for the failure
     * @param cause the cause of this exception
     */
    public SerializationException(Class<?> targetClass, String operation, String reason, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, 
            "Failed to " + operation + " class " + targetClass.getName() + ": " + reason, cause);
    }
}