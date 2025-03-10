package com.network.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Builder for serializers.
 * 
 * <p>This interface defines the methods for building serializers with
 * various configuration options.
 */
public interface SerializerBuilder {
    
    /**
     * Sets whether to use pretty printing for serialized output.
     * 
     * <p>Pretty printing adds indentation and line breaks to make the
     * output more human-readable, at the cost of increased size.
     * 
     * @param prettyPrint true to enable pretty printing, false to disable
     * @return this builder
     */
    SerializerBuilder withPrettyPrint(boolean prettyPrint);
    
    /**
     * Sets the charset to use for string encoding/decoding.
     * 
     * @param charset the charset
     * @return this builder
     * @throws IllegalArgumentException if charset is null
     */
    SerializerBuilder withCharset(Charset charset);
    
    /**
     * Sets the character set name to use for string encoding/decoding.
     * 
     * @param charsetName the charset name
     * @return this builder
     * @throws IllegalArgumentException if charsetName is null or invalid
     */
    default SerializerBuilder withCharset(String charsetName) {
        if (charsetName == null) {
            throw new IllegalArgumentException("Charset name must not be null");
        }
        return withCharset(Charset.forName(charsetName));
    }
    
    /**
     * Sets whether to ignore unknown properties during deserialization.
     * 
     * <p>If true, unknown properties in the input will be ignored.
     * If false, encountering unknown properties will cause an error.
     * 
     * @param ignoreUnknown true to ignore unknown properties, false to throw an error
     * @return this builder
     */
    SerializerBuilder withIgnoreUnknownProperties(boolean ignoreUnknown);
    
    /**
     * Sets whether to use snake case (lowercase with underscores) for property names.
     * 
     * <p>If true, properties will be serialized using snake_case naming convention.
     * If false, the default naming convention will be used (typically camelCase).
     * 
     * @param useSnakeCase true to use snake case, false to use the default
     * @return this builder
     */
    SerializerBuilder withSnakeCase(boolean useSnakeCase);
    
    /**
     * Sets whether to include null values in serialized output.
     * 
     * <p>If true, properties with null values will be included in the output.
     * If false, properties with null values will be omitted.
     * 
     * @param includeNulls true to include null values, false to omit them
     * @return this builder
     */
    SerializerBuilder withIncludeNulls(boolean includeNulls);
    
    /**
     * Sets whether to fail on empty beans during serialization.
     * 
     * <p>If true, attempting to serialize an empty bean (a class with no properties)
     * will throw an error. If false, empty beans will be serialized as empty objects.
     * 
     * @param failOnEmptyBeans true to fail on empty beans, false to allow them
     * @return this builder
     */
    SerializerBuilder withFailOnEmptyBeans(boolean failOnEmptyBeans);
    
    /**
     * Sets whether to include type information in serialized output.
     * 
     * <p>If true, type information will be included in the output, which allows
     * for more precise deserialization of polymorphic types. If false, no type
     * information will be included.
     * 
     * @param includeTypeInfo true to include type information, false to omit it
     * @return this builder
     */
    SerializerBuilder withIncludeTypeInfo(boolean includeTypeInfo);
    
    /**
     * Sets a custom property for the serializer.
     * 
     * <p>This method allows setting implementation-specific properties that are
     * not covered by the standard builder methods.
     * 
     * @param key the property key
     * @param value the property value
     * @return this builder
     * @throws IllegalArgumentException if key is null
     */
    SerializerBuilder withProperty(String key, Object value);
    
    /**
     * Configures the builder using the given consumer.
     * 
     * <p>This method allows for more complex configuration that may require
     * multiple builder calls.
     * 
     * @param configurer the configurer
     * @return this builder
     * @throws IllegalArgumentException if configurer is null
     */
    SerializerBuilder configure(Consumer<SerializerBuilder> configurer);
    
    /**
     * Builds the serializer.
     * 
     * @return the built serializer
     * @throws IllegalStateException if the builder is not properly configured
     */
    Serializer build();
    
    /**
     * Static factory method for creating a serializer builder for JSON.
     * 
     * <p>By default, the builder will use UTF-8 encoding and will not pretty-print.
     * 
     * @return a new JSON serializer builder
     */
    static SerializerBuilder json() {
        // This will be implemented by a concrete factory class
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Static factory method for creating a serializer builder for Protocol Buffers.
     * 
     * @return a new Protocol Buffers serializer builder
     */
    static SerializerBuilder protobuf() {
        // This will be implemented by a concrete factory class
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Static factory method for creating a serializer builder for XML.
     * 
     * <p>By default, the builder will use UTF-8 encoding and will not pretty-print.
     * 
     * @return a new XML serializer builder
     */
    static SerializerBuilder xml() {
        // This will be implemented by a concrete factory class
        throw new UnsupportedOperationException("Not yet implemented");
    }
}