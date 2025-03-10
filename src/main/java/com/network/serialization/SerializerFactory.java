package com.network.serialization;

import com.network.serialization.json.JsonSerializer;

/**
 * Factory for creating {@link Serializer} instances.
 * 
 * <p>This class provides static factory methods for creating serializers
 * for various formats, such as JSON, XML, and Protocol Buffers.
 */
public final class SerializerFactory {
    
    private SerializerFactory() {
        // Prevent instantiation
        throw new AssertionError("SerializerFactory class should not be instantiated");
    }
    
    /**
     * Creates a new JSON serializer.
     * 
     * @return a new JSON serializer
     */
    public static Serializer createJsonSerializer() {
        return new JsonSerializer();
    }
    
    /**
     * Creates a new JSON serializer builder.
     * 
     * @return a new JSON serializer builder
     */
    public static SerializerBuilder createJsonSerializerBuilder() {
        return createJsonSerializer().builder();
    }
    
    /**
     * Gets a serializer for the specified content type.
     * 
     * <p>This method attempts to find a serializer that can handle the
     * specified content type. If no matching serializer is found, an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param contentType the content type
     * @return a serializer for the content type
     * @throws IllegalArgumentException if no serializer is found for the content type
     */
    public static Serializer getSerializerForContentType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        
        String lowerContentType = contentType.toLowerCase();
        
        if (lowerContentType.contains("json")) {
            return createJsonSerializer();
        }
        
        // Add more content type handlers here as they are implemented
        
        throw new IllegalArgumentException("No serializer found for content type: " + contentType);
    }
    
    /**
     * Gets the default serializer.
     * 
     * <p>This is the serializer used when no specific serializer is specified.
     * 
     * @return the default serializer (JSON)
     */
    public static Serializer getDefaultSerializer() {
        return createJsonSerializer();
    }
}