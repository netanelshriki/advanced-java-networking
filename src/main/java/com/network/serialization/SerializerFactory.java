package com.network.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating and managing serializers.
 * 
 * <p>This class provides methods for creating serializers for various content types
 * and maintaining a registry of serializers.
 */
public final class SerializerFactory {
    
    private static final Map<String, Serializer> serializersByContentType = new HashMap<>();
    private static final Map<String, SerializerBuilder> buildersByContentType = new HashMap<>();
    
    // Register default serializers
    static {
        registerSerializer("application/json", new JsonSerializer());
        registerSerializerBuilder("application/json", JsonSerializer.builder());
    }
    
    private SerializerFactory() {
        // Prevent instantiation
        throw new AssertionError("SerializerFactory class should not be instantiated");
    }
    
    /**
     * Creates a serializer for the specified content type.
     * 
     * <p>If a serializer for the content type is not registered, this method
     * falls back to a JSON serializer.
     * 
     * @param contentType the content type
     * @return a serializer for the content type
     */
    public static Serializer createSerializer(String contentType) {
        if (contentType == null) {
            return getDefaultSerializer();
        }
        
        // Extract the base content type (ignore parameters)
        String baseContentType = contentType.split(";")[0].trim().toLowerCase();
        
        synchronized (serializersByContentType) {
            Serializer serializer = serializersByContentType.get(baseContentType);
            if (serializer != null) {
                return serializer;
            }
            
            // Try to create from builder
            SerializerBuilder builder = buildersByContentType.get(baseContentType);
            if (builder != null) {
                Serializer newSerializer = builder.build();
                serializersByContentType.put(baseContentType, newSerializer);
                return newSerializer;
            }
            
            // Fall back to default
            return getDefaultSerializer();
        }
    }
    
    /**
     * Creates a builder for the specified content type.
     * 
     * <p>If a builder for the content type is not registered, this method
     * falls back to a JSON serializer builder.
     * 
     * @param contentType the content type
     * @return a builder for the content type
     */
    public static SerializerBuilder createBuilder(String contentType) {
        if (contentType == null) {
            return getDefaultBuilder();
        }
        
        // Extract the base content type (ignore parameters)
        String baseContentType = contentType.split(";")[0].trim().toLowerCase();
        
        synchronized (buildersByContentType) {
            SerializerBuilder builder = buildersByContentType.get(baseContentType);
            if (builder != null) {
                return builder;
            }
            
            // Fall back to default
            return getDefaultBuilder();
        }
    }
    
    /**
     * Registers a serializer for a content type.
     * 
     * @param contentType the content type
     * @param serializer the serializer
     */
    public static void registerSerializer(String contentType, Serializer serializer) {
        if (contentType == null || serializer == null) {
            return;
        }
        
        String baseContentType = contentType.split(";")[0].trim().toLowerCase();
        
        synchronized (serializersByContentType) {
            serializersByContentType.put(baseContentType, serializer);
        }
    }
    
    /**
     * Registers a serializer builder for a content type.
     * 
     * @param contentType the content type
     * @param builder the serializer builder
     */
    public static void registerSerializerBuilder(String contentType, SerializerBuilder builder) {
        if (contentType == null || builder == null) {
            return;
        }
        
        String baseContentType = contentType.split(";")[0].trim().toLowerCase();
        
        synchronized (buildersByContentType) {
            buildersByContentType.put(baseContentType, builder);
        }
    }
    
    /**
     * Gets the serializer registered for a content type.
     * 
     * @param contentType the content type
     * @return an Optional containing the serializer, or empty if not registered
     */
    public static Optional<Serializer> getSerializer(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        
        String baseContentType = contentType.split(";")[0].trim().toLowerCase();
        
        synchronized (serializersByContentType) {
            return Optional.ofNullable(serializersByContentType.get(baseContentType));
        }
    }
    
    /**
     * Gets the builder registered for a content type.
     * 
     * @param contentType the content type
     * @return an Optional containing the builder, or empty if not registered
     */
    public static Optional<SerializerBuilder> getBuilder(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        
        String baseContentType = contentType.split(";")[0].trim().toLowerCase();
        
        synchronized (buildersByContentType) {
            return Optional.ofNullable(buildersByContentType.get(baseContentType));
        }
    }
    
    /**
     * Gets the default serializer (JSON).
     * 
     * @return the default serializer
     */
    public static Serializer getDefaultSerializer() {
        return new JsonSerializer();
    }
    
    /**
     * Gets the default serializer builder (JSON).
     * 
     * @return the default serializer builder
     */
    public static SerializerBuilder getDefaultBuilder() {
        return JsonSerializer.builder();
    }
    
    /**
     * Clears all registered serializers and builders.
     * 
     * <p>This method is primarily intended for testing purposes.
     */
    public static void clearRegistry() {
        synchronized (serializersByContentType) {
            serializersByContentType.clear();
        }
        
        synchronized (buildersByContentType) {
            buildersByContentType.clear();
        }
        
        // Re-register defaults
        registerSerializer("application/json", new JsonSerializer());
        registerSerializerBuilder("application/json", JsonSerializer.builder());
    }
}