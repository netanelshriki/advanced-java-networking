package com.network.serialization;

import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;

/**
 * Exception thrown when serialization or deserialization fails.
 * 
 * <p>This exception provides information about what went wrong during
 * the serialization or deserialization process.
 */
public class SerializationException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    private final Class<?> targetType;
    private final Direction direction;
    
    /**
     * Creates a new serialization exception with the specified message.
     * 
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.targetType = null;
        this.direction = Direction.UNKNOWN;
    }
    
    /**
     * Creates a new serialization exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, message, cause);
        this.targetType = null;
        this.direction = Direction.UNKNOWN;
    }
    
    /**
     * Creates a new serialization exception with the specified message, target type, and direction.
     * 
     * @param message the detail message
     * @param targetType the target type that was being serialized or deserialized
     * @param direction the direction (serialization or deserialization)
     */
    public SerializationException(String message, Class<?> targetType, Direction direction) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.targetType = targetType;
        this.direction = direction;
    }
    
    /**
     * Creates a new serialization exception with the specified message, cause, target type, and direction.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     * @param targetType the target type that was being serialized or deserialized
     * @param direction the direction (serialization or deserialization)
     */
    public SerializationException(String message, Throwable cause, Class<?> targetType, Direction direction) {
        super(ErrorCode.PROTOCOL_ERROR, message, cause);
        this.targetType = targetType;
        this.direction = direction;
    }
    
    /**
     * Gets the target type that was being serialized or deserialized.
     * 
     * @return the target type, or null if not available
     */
    public Class<?> getTargetType() {
        return targetType;
    }
    
    /**
     * Gets the direction (serialization or deserialization).
     * 
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (targetType != null || direction != Direction.UNKNOWN) {
            sb.append(" [");
            
            if (targetType != null) {
                sb.append("targetType=").append(targetType.getName());
                if (direction != Direction.UNKNOWN) {
                    sb.append(", ");
                }
            }
            
            if (direction != Direction.UNKNOWN) {
                sb.append("direction=").append(direction);
            }
            
            sb.append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Enumeration of serialization directions.
     */
    public enum Direction {
        /**
         * Serialization (object to bytes/string).
         */
        SERIALIZE,
        
        /**
         * Deserialization (bytes/string to object).
         */
        DESERIALIZE,
        
        /**
         * Unknown direction.
         */
        UNKNOWN
    }
}