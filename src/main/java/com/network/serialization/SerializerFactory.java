package com.network.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating serializer instances.
 * 
 * <p>This class provides methods for creating and retrieving serializers
 * for different formats and content types.
 */
public class SerializerFactory {
    
    private static final Map<String, Serializer> SERIALIZERS = new HashMap<>();
    
    static {
        // Register default serializers
        registerSerializer("application/json", new JsonSerializer());
        registerSerializer("json", new JsonSerializer());
    }
    
    private SerializerFactory() {
        // Prevent instantiation
    }
    
    /**
     * Gets a serializer for the specified content type.
     * 
     * @param contentType the content type
     * @return an Optional containing the serializer, or empty if not found
     */
    public static Optional<Serializer> getSerializer(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return Optional.empty();
        }
        
        // Normalize content type
        contentType = normalizeContentType(contentType);
        
        // Try to find an exact match
        Serializer serializer = SERIALIZERS.get(contentType);
        if (serializer != null) {
            return Optional.of(serializer);
        }
        
        // Try to find a match for the base type
        int semicolonIndex = contentType.indexOf(';');
        if (semicolonIndex > 0) {
            String baseType = contentType.substring(0, semicolonIndex).trim();
            serializer = SERIALIZERS.get(baseType);
            if (serializer != null) {
                return Optional.of(serializer);
            }
        }
        
        // Try to match by prefix
        for (Map.Entry<String, Serializer> entry : SERIALIZERS.entrySet()) {
            if (contentType.startsWith(entry.getKey() + "+")) {
                return Optional.of(entry.getValue());
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Gets a serializer for the specified format.
     * 
     * <p>The format can be a content type or a short name like "json".
     * 
     * @param format the format
     * @return an Optional containing the serializer, or empty if not found
     */
    public static Optional<Serializer> getSerializerForFormat(String format) {
        if (format == null || format.isEmpty()) {
            return Optional.empty();
        }
        
        // Normalize format
        format = format.toLowerCase().trim();
        
        // Try to find a direct match
        Serializer serializer = SERIALIZERS.get(format);
        if (serializer != null) {
            return Optional.of(serializer);
        }
        
        // Try to match content type
        return getSerializer(format);
    }
    
    /**
     * Registers a serializer for the specified content type.
     * 
     * @param contentType the content type
     * @param serializer the serializer
     */
    public static void registerSerializer(String contentType, Serializer serializer) {
        if (contentType == null || contentType.isEmpty()) {
            throw new IllegalArgumentException("Content type must not be null or empty");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("Serializer must not be null");
        }
        
        // Normalize content type
        contentType = normalizeContentType(contentType);
        
        SERIALIZERS.put(contentType, serializer);
    }
    
    /**
     * Creates a new JSON serializer.
     * 
     * @return a new JSON serializer
     */
    public static JsonSerializer createJsonSerializer() {
        return new JsonSerializer();
    }
    
    /**
     * Creates a new JSON serializer builder.
     * 
     * @return a new JSON serializer builder
     */
    public static JsonSerializer.JsonSerializerBuilder createJsonSerializerBuilder() {
        return JsonSerializer.builder();
    }
    
    /**
     * Gets a JSON serializer.
     * 
     * <p>This method returns the registered JSON serializer, or creates a new one
     * if not registered.
     * 
     * @return a JSON serializer
     */
    public static Serializer getJsonSerializer() {
        return getSerializerForFormat("json")
            .orElseGet(SerializerFactory::createJsonSerializer);
    }
    
    /**
     * Normalizes a content type string.
     * 
     * @param contentType the content type to normalize
     * @return the normalized content type
     */
    private static String normalizeContentType(String contentType) {
        return contentType.toLowerCase().trim();
    }
}