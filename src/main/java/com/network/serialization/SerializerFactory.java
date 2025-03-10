package com.network.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating serializers based on content type.
 * 
 * <p>This class provides methods for registering and retrieving serializers
 * by content type. It also provides default serializers for common formats.
 */
public final class SerializerFactory {
    
    private static final SerializerFactory INSTANCE = new SerializerFactory();
    
    private final Map<String, Serializer> serializers = new ConcurrentHashMap<>();
    private final Map<String, SerializerBuilder> builders = new ConcurrentHashMap<>();
    
    /**
     * Creates a new serializer factory.
     */
    private SerializerFactory() {
        // Register default serializers
        registerDefaultSerializers();
    }
    
    /**
     * Gets the singleton instance of the serializer factory.
     * 
     * @return the serializer factory instance
     */
    public static SerializerFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Registers the default serializers.
     */
    private void registerDefaultSerializers() {
        // Register JSON serializer
        registerSerializer("application/json", new JsonSerializer());
        registerSerializer("text/json", new JsonSerializer());
        
        // Register JSON builder
        registerBuilder("application/json", JsonSerializer.builder());
        registerBuilder("text/json", JsonSerializer.builder());
    }
    
    /**
     * Registers a serializer for a content type.
     * 
     * @param contentType the content type
     * @param serializer the serializer
     * @return this factory
     * @throws IllegalArgumentException if contentType or serializer is null
     */
    public SerializerFactory registerSerializer(String contentType, Serializer serializer) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("Serializer must not be null");
        }
        
        String normalized = normalizeContentType(contentType);
        serializers.put(normalized, serializer);
        return this;
    }
    
    /**
     * Registers a serializer builder for a content type.
     * 
     * @param contentType the content type
     * @param builder the serializer builder
     * @return this factory
     * @throws IllegalArgumentException if contentType or builder is null
     */
    public SerializerFactory registerBuilder(String contentType, SerializerBuilder builder) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        if (builder == null) {
            throw new IllegalArgumentException("Builder must not be null");
        }
        
        String normalized = normalizeContentType(contentType);
        builders.put(normalized, builder);
        return this;
    }
    
    /**
     * Gets the serializer for a content type.
     * 
     * <p>If no serializer is registered for the exact content type, this method
     * will try to find a serializer for the base content type (without parameters).
     * For example, if no serializer is registered for "application/json; charset=utf-8",
     * it will look for a serializer for "application/json".
     * 
     * @param contentType the content type
     * @return the serializer, or null if not found
     */
    public Serializer getSerializer(String contentType) {
        if (contentType == null) {
            return getDefaultSerializer();
        }
        
        String normalized = normalizeContentType(contentType);
        Serializer serializer = serializers.get(normalized);
        
        if (serializer == null) {
            // Try base content type without parameters
            String baseContentType = getBaseContentType(normalized);
            if (!baseContentType.equals(normalized)) {
                serializer = serializers.get(baseContentType);
            }
        }
        
        return serializer != null ? serializer : getDefaultSerializer();
    }
    
    /**
     * Gets the serializer builder for a content type.
     * 
     * <p>If no builder is registered for the exact content type, this method
     * will try to find a builder for the base content type (without parameters).
     * 
     * @param contentType the content type
     * @return the serializer builder, or null if not found
     */
    public SerializerBuilder getBuilder(String contentType) {
        if (contentType == null) {
            return getDefaultBuilder();
        }
        
        String normalized = normalizeContentType(contentType);
        SerializerBuilder builder = builders.get(normalized);
        
        if (builder == null) {
            // Try base content type without parameters
            String baseContentType = getBaseContentType(normalized);
            if (!baseContentType.equals(normalized)) {
                builder = builders.get(baseContentType);
            }
        }
        
        return builder != null ? builder : getDefaultBuilder();
    }
    
    /**
     * Gets the default serializer.
     * 
     * <p>The default serializer is used when no content type is specified
     * or when no serializer is registered for a content type.
     * 
     * @return the default serializer
     */
    public Serializer getDefaultSerializer() {
        return serializers.get("application/json");
    }
    
    /**
     * Gets the default serializer builder.
     * 
     * <p>The default builder is used when no content type is specified
     * or when no builder is registered for a content type.
     * 
     * @return the default serializer builder
     */
    public SerializerBuilder getDefaultBuilder() {
        return builders.get("application/json");
    }
    
    /**
     * Gets all registered serializers.
     * 
     * @return a map of content types to serializers
     */
    public Map<String, Serializer> getSerializers() {
        return new HashMap<>(serializers);
    }
    
    /**
     * Gets all registered serializer builders.
     * 
     * @return a map of content types to serializer builders
     */
    public Map<String, SerializerBuilder> getBuilders() {
        return new HashMap<>(builders);
    }
    
    /**
     * Normalizes a content type by converting it to lowercase and removing whitespace.
     * 
     * @param contentType the content type
     * @return the normalized content type
     */
    private String normalizeContentType(String contentType) {
        return contentType.trim().toLowerCase();
    }
    
    /**
     * Gets the base content type without parameters.
     * 
     * <p>For example, "application/json; charset=utf-8" becomes "application/json".
     * 
     * @param contentType the content type
     * @return the base content type
     */
    private String getBaseContentType(String contentType) {
        int index = contentType.indexOf(';');
        if (index != -1) {
            return contentType.substring(0, index).trim();
        }
        return contentType;
    }
}