package com.network.serialization;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for serializers.
 * 
 * <p>This interface defines methods for configuring serialization options
 * and creating serializer instances.
 */
public interface SerializerBuilder {
    
    /**
     * Sets whether to pretty print the serialized output.
     * 
     * <p>Pretty printing formats the output with indentation and line breaks
     * to make it more human-readable. This is typically only used for
     * text-based formats like JSON and XML.
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
     * Sets whether to ignore unknown properties during deserialization.
     * 
     * <p>If enabled, properties in the input that don't exist in the target class
     * will be ignored. If disabled, they will cause an error.
     * 
     * @param ignoreUnknown true to ignore unknown properties, false to error
     * @return this builder
     */
    SerializerBuilder withIgnoreUnknownProperties(boolean ignoreUnknown);
    
    /**
     * Sets the date format to use for date serialization/deserialization.
     * 
     * @param dateFormat the date format
     * @return this builder
     * @throws IllegalArgumentException if dateFormat is null
     */
    SerializerBuilder withDateFormat(DateFormat dateFormat);
    
    /**
     * Sets the date format to use for date serialization/deserialization.
     * 
     * @param dateFormat the date format pattern
     * @return this builder
     * @throws IllegalArgumentException if dateFormat is null
     */
    SerializerBuilder withDateFormat(String dateFormat);
    
    /**
     * Sets whether to fail on empty beans.
     * 
     * <p>If enabled, attempting to serialize an object with no properties
     * will cause an error. If disabled, it will produce an empty object.
     * 
     * @param failOnEmptyBeans true to fail on empty beans, false to allow them
     * @return this builder
     */
    SerializerBuilder withFailOnEmptyBeans(boolean failOnEmptyBeans);
    
    /**
     * Sets whether to serialize null values.
     * 
     * <p>If enabled, properties with null values will be included in the output.
     * If disabled, they will be omitted.
     * 
     * @param includeNulls true to include null values, false to omit them
     * @return this builder
     */
    SerializerBuilder withIncludeNulls(boolean includeNulls);
    
    /**
     * Sets a custom property for the serializer.
     * 
     * <p>Custom properties can be used to configure implementation-specific
     * features that aren't covered by the standard builder methods.
     * 
     * @param key the property key
     * @param value the property value
     * @return this builder
     * @throws IllegalArgumentException if key is null
     */
    SerializerBuilder withProperty(String key, Object value);
    
    /**
     * Sets custom properties for the serializer.
     * 
     * @param properties the properties
     * @return this builder
     * @throws IllegalArgumentException if properties is null
     */
    SerializerBuilder withProperties(Map<String, Object> properties);
    
    /**
     * Configures the serializer using the given consumer.
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
}