package com.network.serialization;

import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;

/**
 * Exception thrown when serialization or deserialization fails.
 * 
 * <p>This exception is thrown by {@link Serializer} implementations when
 * they encounter errors during serialization or deserialization.
 */
public class SerializationException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    private final SerializationOperation operation;
    private final Class<?> type;
    
    /**
     * Creates a new serialization exception with the specified message.
     * 
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.operation = SerializationOperation.UNKNOWN;
        this.type = null;
    }
    
    /**
     * Creates a new serialization exception with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public SerializationException(Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, cause);
        this.operation = SerializationOperation.UNKNOWN;
        this.type = null;
    }
    
    /**
     * Creates a new serialization exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, message, cause);
        this.operation = SerializationOperation.UNKNOWN;
        this.type = null;
    }
    
    /**
     * Creates a new serialization exception with the specified operation and type.
     * 
     * @param operation the serialization operation that failed
     * @param type the class involved in the operation
     */
    public SerializationException(SerializationOperation operation, Class<?> type) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(operation, type, null));
        this.operation = operation;
        this.type = type;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, type, and cause.
     * 
     * @param operation the serialization operation that failed
     * @param type the class involved in the operation
     * @param cause the cause of this exception
     */
    public SerializationException(SerializationOperation operation, Class<?> type, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(operation, type, null), cause);
        this.operation = operation;
        this.type = type;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, type, and custom message.
     * 
     * @param operation the serialization operation that failed
     * @param type the class involved in the operation
     * @param message the detail message
     */
    public SerializationException(SerializationOperation operation, Class<?> type, String message) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(operation, type, message));
        this.operation = operation;
        this.type = type;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, type, custom message, and cause.
     * 
     * @param operation the serialization operation that failed
     * @param type the class involved in the operation
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(SerializationOperation operation, Class<?> type, String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(operation, type, message), cause);
        this.operation = operation;
        this.type = type;
    }
    
    /**
     * Gets the serialization operation that failed.
     * 
     * @return the operation
     */
    public SerializationOperation getOperation() {
        return operation;
    }
    
    /**
     * Gets the class involved in the operation.
     * 
     * @return the class, or null if not available
     */
    public Class<?> getType() {
        return type;
    }
    
    /**
     * Builds a standard error message for serialization exceptions.
     * 
     * @param operation the serialization operation
     * @param type the class involved in the operation
     * @param customMessage a custom message, or null to use the default
     * @return the error message
     */
    private static String buildMessage(SerializationOperation operation, Class<?> type, String customMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to ");
        
        switch (operation) {
            case SERIALIZE:
                sb.append("serialize");
                break;
            case DESERIALIZE:
                sb.append("deserialize");
                break;
            case TO_MAP:
                sb.append("convert to map");
                break;
            case FROM_MAP:
                sb.append("convert from map");
                break;
            default:
                sb.append("perform operation on");
                break;
        }
        
        if (type != null) {
            sb.append(" ").append(type.getName());
        }
        
        if (customMessage != null && !customMessage.isEmpty()) {
            sb.append(": ").append(customMessage);
        }
        
        return sb.toString();
    }
    
    /**
     * Enum representing serialization operations.
     */
    public enum SerializationOperation {
        /**
         * Serialization (converting an object to a serialized format).
         */
        SERIALIZE,
        
        /**
         * Deserialization (converting a serialized format to an object).
         */
        DESERIALIZE,
        
        /**
         * Converting an object to a map.
         */
        TO_MAP,
        
        /**
         * Converting a map to an object.
         */
        FROM_MAP,
        
        /**
         * Unknown or unspecified operation.
         */
        UNKNOWN
    }
}