package com.network.serialization;

import java.nio.charset.Charset;
import java.util.Set;

/**
 * Builder interface for creating serializers.
 * 
 * <p>This interface defines methods for configuring and creating serializers.
 */
public interface SerializerBuilder {
    
    /**
     * Sets whether to use pretty printing.
     * 
     * <p>Pretty printing formats the serialized output in a human-readable way,
     * with indentation and line breaks. This is useful for debugging but may
     * increase the size of the serialized output.
     * 
     * @param prettyPrint true to enable pretty printing, false to disable
     * @return this builder
     */
    SerializerBuilder withPrettyPrint(boolean prettyPrint);
    
    /**
     * Sets the charset to use for string conversions.
     * 
     * @param charset the charset
     * @return this builder
     * @throws IllegalArgumentException if charset is null
     */
    SerializerBuilder withCharset(Charset charset);
    
    /**
     * Sets whether to include null values in the serialized output.
     * 
     * @param includeNulls true to include null values, false to exclude them
     * @return this builder
     */
    SerializerBuilder withIncludeNulls(boolean includeNulls);
    
    /**
     * Sets whether to use field names or getter methods for property access.
     * 
     * @param useFields true to use field names, false to use getter methods
     * @return this builder
     */
    SerializerBuilder withUseFields(boolean useFields);
    
    /**
     * Sets whether to use camel case or snake case for property names.
     * 
     * <p>Camel case uses names like "firstName", while snake case uses names
     * like "first_name".
     * 
     * @param useSnakeCase true to use snake case, false to use camel case
     * @return this builder
     */
    SerializerBuilder withSnakeCase(boolean useSnakeCase);
    
    /**
     * Sets the date format to use for date serialization.
     * 
     * @param dateFormat the date format pattern (e.g., "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
     * @return this builder
     * @throws IllegalArgumentException if dateFormat is null
     */
    SerializerBuilder withDateFormat(String dateFormat);
    
    /**
     * Adds a class to be explicitly registered with the serializer.
     * 
     * <p>Some serialization formats require classes to be registered in advance
     * for efficient serialization or to support polymorphism.
     * 
     * @param clazz the class to register
     * @return this builder
     * @throws IllegalArgumentException if clazz is null
     */
    SerializerBuilder registerClass(Class<?> clazz);
    
    /**
     * Sets the set of classes to be explicitly registered with the serializer.
     * 
     * <p>This replaces any previously registered classes.
     * 
     * @param classes the classes to register
     * @return this builder
     * @throws IllegalArgumentException if classes is null
     */
    SerializerBuilder registerClasses(Set<Class<?>> classes);
    
    /**
     * Sets whether to fail on unknown properties during deserialization.
     * 
     * <p>If true, the serializer will throw an exception when encountering
     * properties in the input that don't exist in the target class. If false,
     * these properties will be ignored.
     * 
     * @param failOnUnknown true to fail on unknown properties, false to ignore them
     * @return this builder
     */
    SerializerBuilder withFailOnUnknownProperties(boolean failOnUnknown);
    
    /**
     * Sets a custom property of the serializer.
     * 
     * <p>This method allows for configuring implementation-specific options
     * that are not covered by the standard builder methods.
     * 
     * @param key the property key
     * @param value the property value
     * @return this builder
     * @throws IllegalArgumentException if key is null
     */
    SerializerBuilder withProperty(String key, Object value);
    
    /**
     * Builds the serializer.
     * 
     * @return the built serializer
     * @throws IllegalStateException if the builder is not properly configured
     */
    Serializer build();
}