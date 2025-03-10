package com.network.serialization;

import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * Builder for creating serializer instances.
 * 
 * <p>This interface defines methods for configuring serialization options
 * and creating serializer instances with those configurations.
 */
public interface SerializerBuilder {
    
    /**
     * Sets whether to pretty-print serialized output.
     * 
     * <p>Pretty-printing adds indentation and line breaks to make
     * the serialized output more human-readable.
     * 
     * @param prettyPrint true to enable pretty-printing, false to disable
     * @return this builder
     */
    SerializerBuilder withPrettyPrint(boolean prettyPrint);
    
    /**
     * Sets the charset to use for string serialization and deserialization.
     * 
     * @param charset the charset
     * @return this builder
     * @throws IllegalArgumentException if charset is null
     */
    SerializerBuilder withCharset(Charset charset);
    
    /**
     * Sets whether to ignore unknown properties during deserialization.
     * 
     * <p>If true, unknown properties in the serialized data will be ignored
     * when deserializing to a Java object. If false, an exception will be thrown.
     * 
     * @param ignoreUnknown true to ignore unknown properties, false to throw an exception
     * @return this builder
     */
    SerializerBuilder withIgnoreUnknownProperties(boolean ignoreUnknown);
    
    /**
     * Sets whether to fail on null values during serialization.
     * 
     * <p>If true, null values in Java objects will cause serialization to fail.
     * If false, null values will be serialized normally.
     * 
     * @param failOnNull true to fail on null values, false to serialize them normally
     * @return this builder
     */
    SerializerBuilder withFailOnNull(boolean failOnNull);
    
    /**
     * Sets whether to fail on empty beans during serialization.
     * 
     * <p>If true, attempting to serialize an empty bean (one with no properties)
     * will cause serialization to fail. If false, empty beans will be serialized
     * as empty objects.
     * 
     * @param failOnEmptyBeans true to fail on empty beans, false to serialize them normally
     * @return this builder
     */
    SerializerBuilder withFailOnEmptyBeans(boolean failOnEmptyBeans);
    
    /**
     * Sets whether dates should be serialized as timestamps.
     * 
     * <p>If true, dates will be serialized as numeric timestamps.
     * If false, dates will be serialized in a format specified by the serializer.
     * 
     * @param writeDatesAsTimestamps true to write dates as timestamps, false to use a formatted representation
     * @return this builder
     */
    SerializerBuilder withWriteDatesAsTimestamps(boolean writeDatesAsTimestamps);
    
    /**
     * Sets whether to include null values in the serialized output.
     * 
     * <p>If true, properties with null values will be included in the serialized output.
     * If false, properties with null values will be omitted.
     * 
     * @param includeNulls true to include null values, false to omit them
     * @return this builder
     */
    SerializerBuilder withIncludeNulls(boolean includeNulls);
    
    /**
     * Sets a custom property for the serializer.
     * 
     * <p>This method allows setting serializer-specific properties that are not
     * covered by the other methods in this interface.
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
     * Builds a serializer with the configured options.
     * 
     * @return the configured serializer
     */
    Serializer build();
}