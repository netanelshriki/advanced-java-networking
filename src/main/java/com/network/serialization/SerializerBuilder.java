package com.network.serialization;

import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * Builder for serializers.
 * 
 * <p>This interface defines the methods for configuring and creating
 * serializer instances with various options.
 * 
 * @param <T> the type of the builder (for method chaining)
 */
public interface SerializerBuilder<T extends SerializerBuilder<T>> {
    
    /**
     * Sets the charset to use for string conversions.
     * 
     * @param charset the charset
     * @return this builder
     * @throws IllegalArgumentException if charset is null
     */
    T withCharset(Charset charset);
    
    /**
     * Sets the date format pattern to use for serializing dates.
     * 
     * <p>The pattern should be compatible with {@link java.text.SimpleDateFormat}.
     * 
     * @param pattern the date format pattern
     * @return this builder
     * @throws IllegalArgumentException if pattern is null or invalid
     */
    T withDateFormat(String pattern);
    
    /**
     * Sets whether to pretty-print serialized output.
     * 
     * <p>Pretty-printing adds indentation and line breaks to make
     * the serialized output more human-readable.
     * 
     * @param prettyPrint true to enable pretty-printing, false to disable
     * @return this builder
     */
    T withPrettyPrint(boolean prettyPrint);
    
    /**
     * Sets whether to serialize null values.
     * 
     * <p>If disabled, properties with null values will be omitted
     * from the serialized output.
     * 
     * @param serializeNulls true to serialize null values, false to omit them
     * @return this builder
     */
    T withSerializeNulls(boolean serializeNulls);
    
    /**
     * Sets whether to use the property name as-is.
     * 
     * <p>If disabled, property names may be transformed according
     * to the serializer's naming strategy, such as to camelCase or snake_case.
     * 
     * @param usePropertyName true to use property names as-is, false to transform them
     * @return this builder
     */
    T withUsePropertyName(boolean usePropertyName);
    
    /**
     * Sets the naming strategy to use for property names.
     * 
     * <p>The naming strategy determines how Java property names are
     * transformed in the serialized output.
     * 
     * @param strategy the naming strategy
     * @return this builder
     * @throws IllegalArgumentException if strategy is null
     */
    T withNamingStrategy(NamingStrategy strategy);
    
    /**
     * Sets the field visibility level for serialization.
     * 
     * <p>This determines which fields are included in serialization
     * based on their visibility modifiers.
     * 
     * @param visibility the field visibility level
     * @return this builder
     * @throws IllegalArgumentException if visibility is null
     */
    T withFieldVisibility(FieldVisibility visibility);
    
    /**
     * Sets a custom property for the serializer.
     * 
     * <p>Custom properties allow for serializer-specific configuration
     * that is not covered by the standard builder methods.
     * 
     * @param key the property key
     * @param value the property value
     * @return this builder
     * @throws IllegalArgumentException if key is null
     */
    T withProperty(String key, Object value);
    
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
    T configure(Consumer<T> configurer);
    
    /**
     * Builds the serializer with the configured options.
     * 
     * @return the built serializer
     * @throws IllegalStateException if the builder is not properly configured
     */
    Serializer build();
    
    /**
     * Enum representing naming strategies for serialized property names.
     */
    enum NamingStrategy {
        /**
         * Use the property names as-is.
         */
        IDENTITY,
        
        /**
         * Convert property names to camelCase.
         */
        CAMEL_CASE,
        
        /**
         * Convert property names to PascalCase.
         */
        PASCAL_CASE,
        
        /**
         * Convert property names to snake_case.
         */
        SNAKE_CASE,
        
        /**
         * Convert property names to kebab-case.
         */
        KEBAB_CASE,
        
        /**
         * Convert property names to UPPER_SNAKE_CASE.
         */
        UPPER_SNAKE_CASE
    }
    
    /**
     * Enum representing field visibility levels for serialization.
     */
    enum FieldVisibility {
        /**
         * Include only public fields.
         */
        PUBLIC,
        
        /**
         * Include public and protected fields.
         */
        PROTECTED,
        
        /**
         * Include public, protected, and package-private fields.
         */
        PACKAGE,
        
        /**
         * Include all fields (public, protected, package-private, and private).
         */
        PRIVATE,
        
        /**
         * Include only fields that are explicitly annotated for serialization.
         */
        ANNOTATED_ONLY,
        
        /**
         * Exclude all fields (use only getters/setters).
         */
        NONE
    }
}