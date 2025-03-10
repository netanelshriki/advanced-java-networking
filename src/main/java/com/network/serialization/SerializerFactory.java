package com.network.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory for creating serializers by content type.
 * 
 * <p>This class provides methods for registering and retrieving serializers
 * for different content types.
 */
public final class SerializerFactory {
    
    private static final Map<String, Supplier<Serializer>> serializers = new HashMap<>();
    
    static {
        // Register default serializers
        register("application/json", JsonSerializer::create);
        register("application/json;*", JsonSerializer::create);
        register("text/json", JsonSerializer::create);
        register("text/json;*", JsonSerializer::create);
    }
    
    private SerializerFactory() {
        // Prevent instantiation
        throw new AssertionError("SerializerFactory class should not be instantiated");
    }
    
    /**
     * Registers a serializer for a content type.
     * 
     * @param contentType the content type
     * @param supplier the supplier that creates serializer instances
     * @throws IllegalArgumentException if contentType or supplier is null
     */
    public static void register(String contentType, Supplier<Serializer> supplier) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier must not be null");
        }
        
        serializers.put(contentType.toLowerCase(), supplier);
    }
    
    /**
     * Gets a serializer for a content type.
     * 
     * <p>This method returns a serializer that can handle the specified content type.
     * If no exact match is found, it tries to match the base content type without parameters.
     * 
     * @param contentType the content type
     * @return the serializer, or null if no matching serializer is found
     */
    public static Serializer getSerializer(String contentType) {
        if (contentType == null) {
            return null;
        }
        
        String normalizedContentType = normalizeContentType(contentType);
        
        // Try exact match first
        Supplier<Serializer> supplier = serializers.get(normalizedContentType);
        
        // If not found, try with wildcard parameters
        if (supplier == null && normalizedContentType.contains(";")) {
            String baseType = normalizedContentType.split(";")[0];
            supplier = serializers.get(baseType);
            
            // Try with wildcard
            if (supplier == null) {
                supplier = serializers.get(baseType + ";*");
            }
        }
        
        // If still not found, try JSON as default for many types
        if (supplier == null) {
            if (normalizedContentType.contains("json") || 
                normalizedContentType.contains("javascript")) {
                supplier = serializers.get("application/json");
            }
        }
        
        return supplier != null ? supplier.get() : null;
    }
    
    /**
     * Gets the default serializer.
     * 
     * <p>This method returns the JSON serializer as the default.
     * 
     * @return the default serializer
     */
    public static Serializer getDefaultSerializer() {
        return JsonSerializer.create();
    }
    
    /**
     * Normalizes a content type string.
     * 
     * <p>This method converts the content type to lowercase and trims whitespace.
     * 
     * @param contentType the content type to normalize
     * @return the normalized content type
     */
    private static String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        
        String normalized = contentType.trim().toLowerCase();
        
        // Extract base content type and parameters
        int paramIndex = normalized.indexOf(';');
        if (paramIndex > 0) {
            String baseType = normalized.substring(0, paramIndex).trim();
            String params = normalized.substring(paramIndex).trim();
            return baseType + params;
        }
        
        return normalized;
    }
}