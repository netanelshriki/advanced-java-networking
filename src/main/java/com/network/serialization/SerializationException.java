package com.network.serialization;

import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;

/**
 * Exception thrown when serialization or deserialization fails.
 * 
 * <p>This exception provides additional information about the serialization
 * operation that failed, such as the object type and the direction
 * (serialization or deserialization).
 */
public class SerializationException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Direction of the serialization operation that failed.
     */
    public enum Direction {
        /**
         * Serialization (object to bytes/string).
         */
        SERIALIZE,
        
        /**
         * Deserialization (bytes/string to object).
         */
        DESERIALIZE
    }
    
    private final Direction direction;
    private final Class<?> objectType;
    
    /**
     * Creates a new serialization exception with the specified message.
     * 
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.direction = null;
        this.objectType = null;
    }
    
    /**
     * Creates a new serialization exception with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public SerializationException(Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, cause);
        this.direction = null;
        this.objectType = null;
    }
    
    /**
     * Creates a new serialization exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, message, cause);
        this.direction = null;
        this.objectType = null;
    }
    
    /**
     * Creates a new serialization exception with the specified direction and object type.
     * 
     * @param direction the direction of the serialization operation
     * @param objectType the type of object being serialized or deserialized
     */
    public SerializationException(Direction direction, Class<?> objectType) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(direction, objectType, null));
        this.direction = direction;
        this.objectType = objectType;
    }
    
    /**
     * Creates a new serialization exception with the specified direction, object type, and cause.
     * 
     * @param direction the direction of the serialization operation
     * @param objectType the type of object being serialized or deserialized
     * @param cause the cause of this exception
     */
    public SerializationException(Direction direction, Class<?> objectType, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(direction, objectType, null), cause);
        this.direction = direction;
        this.objectType = objectType;
    }
    
    /**
     * Creates a new serialization exception with the specified direction, object type, and message.
     * 
     * @param direction the direction of the serialization operation
     * @param objectType the type of object being serialized or deserialized
     * @param message the detail message
     */
    public SerializationException(Direction direction, Class<?> objectType, String message) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(direction, objectType, message));
        this.direction = direction;
        this.objectType = objectType;
    }
    
    /**
     * Creates a new serialization exception with the specified direction, object type, message, and cause.
     * 
     * @param direction the direction of the serialization operation
     * @param objectType the type of object being serialized or deserialized
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(Direction direction, Class<?> objectType, String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(direction, objectType, message), cause);
        this.direction = direction;
        this.objectType = objectType;
    }
    
    /**
     * Gets the direction of the serialization operation that failed.
     * 
     * @return the direction, or null if not available
     */
    public Direction getDirection() {
        return direction;
    }
    
    /**
     * Gets the type of object being serialized or deserialized.
     * 
     * @return the object type, or null if not available
     */
    public Class<?> getObjectType() {
        return objectType;
    }
    
    /**
     * Builds a standard error message from the direction, object type, and message.
     * 
     * @param direction the direction of the serialization operation
     * @param objectType the type of object being serialized or deserialized
     * @param message the detail message
     * @return the built message
     */
    private static String buildMessage(Direction direction, Class<?> objectType, String message) {
        StringBuilder sb = new StringBuilder();
        
        if (direction != null) {
            sb.append(direction == Direction.SERIALIZE ? "Serialization" : "Deserialization");
            sb.append(" failed");
            
            if (objectType != null) {
                sb.append(" for type ").append(objectType.getName());
            }
            
            if (message != null && !message.isEmpty()) {
                sb.append(": ").append(message);
            }
        } else if (message != null && !message.isEmpty()) {
            sb.append(message);
        } else {
            sb.append("Serialization error");
        }
        
        return sb.toString();
    }
}