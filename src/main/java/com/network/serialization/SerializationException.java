package com.network.serialization;

import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;

/**
 * Exception thrown when serialization or deserialization fails.
 */
public class SerializationException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    private final SerializationOperation operation;
    private final Class<?> targetType;
    
    /**
     * Creates a new serialization exception with the specified message.
     * 
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.operation = null;
        this.targetType = null;
    }
    
    /**
     * Creates a new serialization exception with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public SerializationException(Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, cause);
        this.operation = null;
        this.targetType = null;
    }
    
    /**
     * Creates a new serialization exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, message, cause);
        this.operation = null;
        this.targetType = null;
    }
    
    /**
     * Creates a new serialization exception with the specified operation and target type.
     * 
     * @param operation the serialization operation that failed
     * @param targetType the target type of the operation
     */
    public SerializationException(SerializationOperation operation, Class<?> targetType) {
        super(ErrorCode.PROTOCOL_ERROR, 
              "Failed to " + operation.name().toLowerCase() + " " + 
              (targetType != null ? targetType.getName() : "data"));
        this.operation = operation;
        this.targetType = targetType;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, target type, and cause.
     * 
     * @param operation the serialization operation that failed
     * @param targetType the target type of the operation
     * @param cause the cause of this exception
     */
    public SerializationException(SerializationOperation operation, Class<?> targetType, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, 
              "Failed to " + operation.name().toLowerCase() + " " + 
              (targetType != null ? targetType.getName() : "data"), 
              cause);
        this.operation = operation;
        this.targetType = targetType;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, target type, and message.
     * 
     * @param operation the serialization operation that failed
     * @param targetType the target type of the operation
     * @param message the detail message
     */
    public SerializationException(SerializationOperation operation, Class<?> targetType, String message) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.operation = operation;
        this.targetType = targetType;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, target type, message, and cause.
     * 
     * @param operation the serialization operation that failed
     * @param targetType the target type of the operation
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(SerializationOperation operation, Class<?> targetType, 
                                 String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, message, cause);
        this.operation = operation;
        this.targetType = targetType;
    }
    
    /**
     * Gets the serialization operation that failed.
     * 
     * @return the operation, or null if not available
     */
    public SerializationOperation getOperation() {
        return operation;
    }
    
    /**
     * Gets the target type of the serialization operation.
     * 
     * @return the target type, or null if not available
     */
    public Class<?> getTargetType() {
        return targetType;
    }
    
    /**
     * Enum representing serialization operations.
     */
    public enum SerializationOperation {
        /** Serializing an object to a format. */
        SERIALIZE,
        
        /** Deserializing a format to an object. */
        DESERIALIZE,
        
        /** Converting an object to a map. */
        TO_MAP,
        
        /** Converting a map to an object. */
        FROM_MAP
    }
}