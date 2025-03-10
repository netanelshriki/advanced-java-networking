package com.network.serialization;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Builder for creating {@link Serializer} instances.
 * 
 * <p>This interface defines methods for configuring serialization options
 * and creating a serializer with those options.
 */
public interface SerializerBuilder {
    
    /**
     * Sets whether to pretty print the serialized output.
     * 
     * <p>Pretty printing formats the output with indentation and line breaks
     * to make it more human-readable. This may increase the size of the
     * serialized output.
     * 
     * @param prettyPrint true to pretty print, false to compact output
     * @return this builder
     */
    SerializerBuilder withPrettyPrint(boolean prettyPrint);
    
    /**
     * Sets the charset to use for string encoding and decoding.
     * 
     * @param charset the charset
     * @return this builder
     * @throws IllegalArgumentException if charset is null
     */
    SerializerBuilder withCharset(Charset charset);
    
    /**
     * Sets the date format to use for serializing dates.
     * 
     * @param dateFormat the date format
     * @return this builder
     * @throws IllegalArgumentException if dateFormat is null
     */
    SerializerBuilder withDateFormat(DateFormat dateFormat);
    
    /**
     * Sets the date format pattern to use for serializing dates.
     * 
     * @param pattern the date format pattern
     * @return this builder
     * @throws IllegalArgumentException if pattern is null
     */
    SerializerBuilder withDateFormat(String pattern);
    
    /**
     * Sets whether to include null values in the serialized output.
     * 
     * @param includeNulls true to include nulls, false to exclude them
     * @return this builder
     */
    SerializerBuilder withIncludeNulls(boolean includeNulls);
    
    /**
     * Sets whether to write dates as timestamps.
     * 
     * @param asTimestamp true to write dates as timestamps, false to use the date format
     * @return this builder
     */
    SerializerBuilder withWriteDatesAsTimestamp(boolean asTimestamp);
    
    /**
     * Sets whether field names should be case-sensitive during deserialization.
     * 
     * @param caseSensitive true for case-sensitive, false for case-insensitive
     * @return this builder
     */
    SerializerBuilder withCaseSensitiveFields(boolean caseSensitive);
    
    /**
     * Sets whether to fail on unknown properties during deserialization.
     * 
     * @param failOnUnknown true to fail, false to ignore unknown properties
     * @return this builder
     */
    SerializerBuilder withFailOnUnknownProperties(boolean failOnUnknown);
    
    /**
     * Sets the fields to include during serialization.
     * 
     * <p>If non-empty, only the specified fields will be included in the
     * serialized output. All other fields will be excluded.
     * 
     * @param fields the fields to include
     * @return this builder
     * @throws IllegalArgumentException if fields is null
     */
    SerializerBuilder withIncludeFields(Set<String> fields);
    
    /**
     * Sets the fields to exclude during serialization.
     * 
     * <p>If non-empty, the specified fields will be excluded from the
     * serialized output. All other fields will be included.
     * 
     * @param fields the fields to exclude
     * @return this builder
     * @throws IllegalArgumentException if fields is null
     */
    SerializerBuilder withExcludeFields(Set<String> fields);
    
    /**
     * Configures the builder using the specified consumer.
     * 
     * <p>This method allows for more complex configuration that may require
     * multiple builder method calls.
     * 
     * @param configurer the configurer
     * @return this builder
     * @throws IllegalArgumentException if configurer is null
     */
    SerializerBuilder configure(Consumer<SerializerBuilder> configurer);
    
    /**
     * Builds a serializer with the configured options.
     * 
     * @return a new serializer
     */
    Serializer build();
}