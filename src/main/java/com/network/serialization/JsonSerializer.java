package com.network.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON serializer implementation using Jackson.
 * 
 * <p>This class provides serialization and deserialization between Java objects
 * and JSON format using the Jackson library.
 */
public class JsonSerializer implements Serializer {
    
    private static final String CONTENT_TYPE = "application/json";
    
    private final ObjectMapper mapper;
    private final Charset charset;
    
    /**
     * Creates a new JSON serializer with default settings.
     */
    public JsonSerializer() {
        this(configureDefaultMapper(new ObjectMapper()), StandardCharsets.UTF_8);
    }
    
    /**
     * Creates a new JSON serializer with the specified object mapper and charset.
     * 
     * @param mapper the object mapper to use
     * @param charset the charset to use for string conversions
     */
    public JsonSerializer(ObjectMapper mapper, Charset charset) {
        this.mapper = mapper;
        this.charset = charset;
    }
    
    /**
     * Configures a Jackson ObjectMapper with default settings.
     * 
     * @param mapper the mapper to configure
     * @return the configured mapper
     */
    private static ObjectMapper configureDefaultMapper(ObjectMapper mapper) {
        return mapper
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    @Override
    public String getContentType() {
        return CONTENT_TYPE + "; charset=" + charset.name().toLowerCase();
    }
    
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(object.getClass(), "serialize", e);
        }
    }
    
    @Override
    public String serializeToString(Object object) throws SerializationException {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(object.getClass(), "serialize to string", e);
        }
    }
    
    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        try {
            mapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new SerializationException(object.getClass(), "serialize to output stream", e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        try {
            return mapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SerializationException(type, "deserialize from bytes", e);
        }
    }
    
    @Override
    public <T> T deserialize(String string, Class<T> type) throws SerializationException {
        try {
            return mapper.readValue(string, type);
        } catch (IOException e) {
            throw new SerializationException(type, "deserialize from string", e);
        }
    }
    
    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) throws SerializationException {
        try {
            return mapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new SerializationException(type, "deserialize from input stream", e);
        }
    }
    
    @Override
    public Map<String, Object> toMap(Object object) throws SerializationException {
        try {
            return mapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new SerializationException(object.getClass(), "convert to map", e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromMap(Map<String, Object> map, Class<T> type) throws SerializationException {
        try {
            return mapper.convertValue(map, type);
        } catch (Exception e) {
            throw new SerializationException(type, "convert from map", e);
        }
    }
    
    @Override
    public SerializerBuilder builder() {
        return new Builder(this);
    }
    
    /**
     * Builder for {@link JsonSerializer}.
     */
    public static class Builder implements SerializerBuilder {
        
        private final ObjectMapper mapper;
        private Charset charset = StandardCharsets.UTF_8;
        
        /**
         * Creates a new builder with default settings.
         */
        public Builder() {
            this.mapper = configureDefaultMapper(new ObjectMapper());
        }
        
        /**
         * Creates a new builder initialized with settings from the specified serializer.
         * 
         * @param serializer the serializer to copy settings from
         */
        public Builder(JsonSerializer serializer) {
            this.mapper = serializer.mapper.copy();
            this.charset = serializer.charset;
        }
        
        @Override
        public Builder withPrettyPrint(boolean prettyPrint) {
            if (prettyPrint) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            } else {
                mapper.disable(SerializationFeature.INDENT_OUTPUT);
            }
            return this;
        }
        
        @Override
        public Builder withCharset(Charset charset) {
            if (charset == null) {
                throw new IllegalArgumentException("Charset must not be null");
            }
            this.charset = charset;
            return this;
        }
        
        @Override
        public Builder withIgnoreUnknownProperties(boolean ignoreUnknown) {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, !ignoreUnknown);
            return this;
        }
        
        @Override
        public Builder withFailOnNull(boolean failOnNull) {
            mapper.configure(SerializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, failOnNull);
            return this;
        }
        
        @Override
        public Builder withFailOnEmptyBeans(boolean failOnEmptyBeans) {
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, failOnEmptyBeans);
            return this;
        }
        
        @Override
        public Builder withWriteDatesAsTimestamps(boolean writeDatesAsTimestamps) {
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, writeDatesAsTimestamps);
            return this;
        }
        
        @Override
        public Builder withIncludeNulls(boolean includeNulls) {
            if (includeNulls) {
                mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            } else {
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }
            return this;
        }
        
        @Override
        public Builder withProperty(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("Property key must not be null");
            }
            
            // Handle Jackson-specific properties
            switch (key) {
                case "ACCEPT_SINGLE_VALUE_AS_ARRAY":
                    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, (Boolean) value);
                    break;
                case "UNWRAP_ROOT_VALUE":
                    mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, (Boolean) value);
                    break;
                case "WRAP_ROOT_VALUE":
                    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, (Boolean) value);
                    break;
                case "ORDER_MAP_ENTRIES_BY_KEYS":
                    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, (Boolean) value);
                    break;
                default:
                    // Unknown property
                    break;
            }
            
            return this;
        }
        
        @Override
        public Builder configure(Consumer<SerializerBuilder> configurer) {
            if (configurer == null) {
                throw new IllegalArgumentException("Configurer must not be null");
            }
            configurer.accept(this);
            return this;
        }
        
        @Override
        public JsonSerializer build() {
            return new JsonSerializer(mapper, charset);
        }
    }
    
    /**
     * Creates a new builder for {@link JsonSerializer}.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
}