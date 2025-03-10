package com.network.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.network.serialization.json.JacksonJsonSerializer;

/**
 * Factory for creating serializers.
 * 
 * <p>This class provides methods for creating serializers for different content types,
 * as well as for registering custom serializer implementations.
 */
public final class SerializerFactory {
    
    private static final Map<String, Supplier<SerializerBuilder>> BUILDERS = new ConcurrentHashMap<>();
    private static final Map<String, Serializer> INSTANCES = new ConcurrentHashMap<>();
    
    static {
        // Register default serializers
        registerBuilder("application/json", JacksonJsonSerializer::builder);
        registerBuilder("text/json", JacksonJsonSerializer::builder);
        registerBuilder("json", JacksonJsonSerializer::builder);
    }
    
    private SerializerFactory() {
        // Prevent instantiation
        throw new AssertionError("SerializerFactory class should not be instantiated");
    }
    
    /**
     * Registers a serializer builder for a content type.
     * 
     * @param contentType the content type
     * @param builderSupplier the supplier for creating builders
     * @throws IllegalArgumentException if contentType or builderSupplier is null
     */
    public static void registerBuilder(String contentType, Supplier<SerializerBuilder> builderSupplier) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        if (builderSupplier == null) {
            throw new IllegalArgumentException("Builder supplier must not be null");
        }
        
        BUILDERS.put(normalizeContentType(contentType), builderSupplier);
    }
    
    /**
     * Gets a serializer builder for the specified content type.
     * 
     * @param contentType the content type
     * @return a new serializer builder
     * @throws IllegalArgumentException if contentType is null or unsupported
     */
    public static SerializerBuilder getBuilder(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        
        String normalizedType = normalizeContentType(contentType);
        Supplier<SerializerBuilder> builderSupplier = BUILDERS.get(normalizedType);
        
        if (builderSupplier == null) {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
        
        return builderSupplier.get();
    }
    
    /**
     * Gets a serializer for the specified content type.
     * 
     * <p>This method returns a shared instance for each content type,
     * creating it if necessary.
     * 
     * @param contentType the content type
     * @return the serializer
     * @throws IllegalArgumentException if contentType is null or unsupported
     */
    public static Serializer getSerializer(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type must not be null");
        }
        
        String normalizedType = normalizeContentType(contentType);
        return INSTANCES.computeIfAbsent(normalizedType, type -> {
            SerializerBuilder builder = getBuilder(type);
            return builder.build();
        });
    }
    
    /**
     * Gets a JSON serializer.
     * 
     * <p>This is a convenience method equivalent to {@code getSerializer("application/json")}.
     * 
     * @return the JSON serializer
     */
    public static Serializer getJsonSerializer() {
        return getSerializer("application/json");
    }
    
    /**
     * Creates a custom JSON serializer with the specified configuration.
     * 
     * @param configurer the configurer for customizing the builder
     * @return the custom JSON serializer
     * @throws IllegalArgumentException if configurer is null
     */
    public static Serializer createJsonSerializer(java.util.function.Consumer<SerializerBuilder> configurer) {
        if (configurer == null) {
            throw new IllegalArgumentException("Configurer must not be null");
        }
        
        SerializerBuilder builder = getBuilder("application/json");
        configurer.accept(builder);
        return builder.build();
    }
    
    /**
     * Normalizes a content type by removing parameters and converting to lowercase.
     * 
     * <p>For example, "application/json; charset=utf-8" becomes "application/json".
     * 
     * @param contentType the content type
     * @return the normalized content type
     */
    private static String normalizeContentType(String contentType) {
        // Extract base content type without parameters
        int paramIndex = contentType.indexOf(';');
        if (paramIndex > 0) {
            contentType = contentType.substring(0, paramIndex);
        }
        
        // Trim and convert to lowercase
        return contentType.trim().toLowerCase();
    }
    
    /**
     * Gets all supported content types.
     * 
     * @return a map of content type to sample serializer
     */
    public static Map<String, Serializer> getSupportedTypes() {
        Map<String, Serializer> result = new HashMap<>();
        
        for (String contentType : BUILDERS.keySet()) {
            result.put(contentType, getSerializer(contentType));
        }
        
        return result;
    }
}