package com.network.serialization;

import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;

/**
 * Exception thrown when a serialization or deserialization operation fails.
 * 
 * <p>This exception provides additional information about the operation that failed,
 * such as the object type and serialization format.
 */
public class SerializationException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    private final SerializationOperation operation;
    private final Class<?> objectType;
    private final String format;
    
    /**
     * Creates a new serialization exception with the specified message.
     * 
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.operation = SerializationOperation.UNKNOWN;
        this.objectType = null;
        this.format = null;
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
        this.objectType = null;
        this.format = null;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, object type, and format.
     * 
     * @param operation the serialization operation that failed
     * @param objectType the type of the object being serialized or deserialized
     * @param format the serialization format
     */
    public SerializationException(SerializationOperation operation, Class<?> objectType, String format) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(operation, objectType, format));
        this.operation = operation;
        this.objectType = objectType;
        this.format = format;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, object type, format, and cause.
     * 
     * @param operation the serialization operation that failed
     * @param objectType the type of the object being serialized or deserialized
     * @param format the serialization format
     * @param cause the cause of this exception
     */
    public SerializationException(SerializationOperation operation, Class<?> objectType, String format, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, buildMessage(operation, objectType, format), cause);
        this.operation = operation;
        this.objectType = objectType;
        this.format = format;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, object type, format, and message.
     * 
     * @param operation the serialization operation that failed
     * @param objectType the type of the object being serialized or deserialized
     * @param format the serialization format
     * @param message the detail message
     */
    public SerializationException(SerializationOperation operation, Class<?> objectType, String format, String message) {
        super(ErrorCode.PROTOCOL_ERROR, message);
        this.operation = operation;
        this.objectType = objectType;
        this.format = format;
    }
    
    /**
     * Creates a new serialization exception with the specified operation, object type, format, message, and cause.
     * 
     * @param operation the serialization operation that failed
     * @param objectType the type of the object being serialized or deserialized
     * @param format the serialization format
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SerializationException(SerializationOperation operation, Class<?> objectType, String format, String message, Throwable cause) {
        super(ErrorCode.PROTOCOL_ERROR, message, cause);
        this.operation = operation;
        this.objectType = objectType;
        this.format = format;
    }
    
    /**
     * Gets the serialization operation that failed.
     * 
     * @return the serialization operation
     */
    public SerializationOperation getOperation() {
        return operation;
    }
    
    /**
     * Gets the type of the object being serialized or deserialized.
     * 
     * @return the object type, or null if not available
     */
    public Class<?> getObjectType() {
        return objectType;
    }
    
    /**
     * Gets the serialization format.
     * 
     * @return the format, or null if not available
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * Builds a standard error message from the operation, object type, and format.
     * 
     * @param operation the serialization operation that failed
     * @param objectType the type of the object being serialized or deserialized
     * @param format the serialization format
     * @return the error message
     */
    private static String buildMessage(SerializationOperation operation, Class<?> objectType, String format) {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to ");
        
        switch (operation) {
            case SERIALIZE:
                sb.append("serialize");
                break;
            case DESERIALIZE:
                sb.append("deserialize");
                break;
            case MAP_TO_OBJECT:
                sb.append("convert map to object");
                break;
            case OBJECT_TO_MAP:
                sb.append("convert object to map");
                break;
            default:
                sb.append("perform serialization operation on");
                break;
        }
        
        sb.append(" object");
        
        if (objectType != null) {
            sb.append(" of type '").append(objectType.getName()).append("'");
        }
        
        if (format != null) {
            sb.append(" using format '").append(format).append("'");
        }
        
        return sb.toString();
    }
    
    /**
     * Serialization operations that can fail.
     */
    public enum SerializationOperation {
        /**
         * Converting an object to a serialized format.
         */
        SERIALIZE,
        
        /**
         * Converting a serialized format to an object.
         */
        DESERIALIZE,
        
        /**
         * Converting a map to an object.
         */
        MAP_TO_OBJECT,
        
        /**
         * Converting an object to a map.
         */
        OBJECT_TO_MAP,
        
        /**
         * Unknown operation.
         */
        UNKNOWN
    }
}