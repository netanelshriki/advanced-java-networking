package com.network.serialization;

import com.network.exception.NetworkException;

/**
 * Exception thrown when a serialization error occurs.
 */
public class SerializationException extends NetworkException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Direction of serialization operation.
     */
    public enum Direction {
        /**
         * Serializing from object to bytes.
         */
        SERIALIZE,
        
        /**
         * Deserializing from bytes to object.
         */
        DESERIALIZE
    }
    
    private final Direction direction;
    private final Class<?> targetType;
    
    /**
     * Creates a new serialization exception.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @param direction the direction of serialization
     */
    public SerializationException(String message, Throwable cause, Direction direction) {
        super(message, cause);
        this.direction = direction;
        this.targetType = null;
    }
    
    /**
     * Creates a new serialization exception.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @param direction the direction of serialization
     * @param targetType the target type
     */
    public SerializationException(String message, Throwable cause, Direction direction, Class<?> targetType) {
        super(message, cause);
        this.direction = direction;
        this.targetType = targetType;
    }
    
    /**
     * Gets the direction of serialization.
     * 
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }
    
    /**
     * Gets the target type.
     * 
     * @return the target type, or null if not applicable
     */
    public Class<?> getTargetType() {
        return targetType;
    }
}
