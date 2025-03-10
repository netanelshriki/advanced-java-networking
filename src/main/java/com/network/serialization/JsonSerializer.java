package com.network.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.network.serialization.SerializationException.SerializationOperation;

/**
 * JSON implementation of the {@link Serializer} interface.
 * 
 * <p>This class uses Jackson for JSON serialization and deserialization.
 */
public class JsonSerializer implements Serializer {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    private static final String CONTENT_TYPE = "application/json";
    
    private final ObjectMapper objectMapper;
    private final Charset charset;
    
    /**
     * Creates a new JSON serializer with default settings.
     */
    public JsonSerializer() {
        this.objectMapper = new ObjectMapper();
        this.charset = StandardCharsets.UTF_8;
        
        // Configure defaults
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Register modules for Java 8 date/time types
        objectMapper.findAndRegisterModules();
    }
    
    /**
     * Creates a new JSON serializer with the specified object mapper and charset.
     * 
     * @param objectMapper the object mapper to use
     * @param charset the charset to use
     */
    private JsonSerializer(ObjectMapper objectMapper, Charset charset) {
        this.objectMapper = objectMapper;
        this.charset = charset;
    }
    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(SerializationOperation.SERIALIZE, 
                object.getClass(), e.getMessage(), e);
        }
    }
    
    @Override
    public String serializeToString(Object object) throws SerializationException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(SerializationOperation.SERIALIZE, 
                object.getClass(), e.getMessage(), e);
        }
    }
    
    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        try {
            objectMapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new SerializationException(SerializationOperation.SERIALIZE, 
                object.getClass(), e.getMessage(), e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SerializationException(SerializationOperation.DESERIALIZE, 
                type, e.getMessage(), e);
        }
    }
    
    @Override
    public <T> T deserialize(String string, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(string, type);
        } catch (JsonProcessingException e) {
            throw new SerializationException(SerializationOperation.DESERIALIZE, 
                type, e.getMessage(), e);
        }
    }
    
    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new SerializationException(SerializationOperation.DESERIALIZE, 
                type, e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> toMap(Object object) throws SerializationException {
        try {
            return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException e) {
            throw new SerializationException(SerializationOperation.TO_MAP, 
                object.getClass(), e.getMessage(), e);
        }
    }
    
    @Override
    public <T> T fromMap(Map<String, Object> map, Class<T> type) throws SerializationException {
        try {
            return objectMapper.convertValue(map, type);
        } catch (IllegalArgumentException e) {
            throw new SerializationException(SerializationOperation.FROM_MAP, 
                type, e.getMessage(), e);
        }
    }
    
    @Override
    public SerializerBuilder builder() {
        return new JsonSerializerBuilder(this);
    }
    
    /**
     * Gets the Jackson {@link ObjectMapper} used by this serializer.
     * 
     * @return the object mapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Gets the charset used by this serializer.
     * 
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }
    
    /**
     * Builder for {@link JsonSerializer}.
     */
    public static class JsonSerializerBuilder implements SerializerBuilder {
        
        private final ObjectMapper objectMapper;
        private Charset charset;
        private final Map<String, Object> properties = new HashMap<>();
        
        /**
         * Creates a new builder with default settings.
         */
        public JsonSerializerBuilder() {
            this.objectMapper = new ObjectMapper();
            this.charset = StandardCharsets.UTF_8;
            
            // Configure defaults
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            
            // Register modules for Java 8 date/time types
            objectMapper.findAndRegisterModules();
        }
        
        /**
         * Creates a new builder initialized with settings from the specified serializer.
         * 
         * @param serializer the serializer to copy settings from
         */
        public JsonSerializerBuilder(JsonSerializer serializer) {
            this.objectMapper = serializer.getObjectMapper().copy();
            this.charset = serializer.getCharset();
        }
        
        @Override
        public SerializerBuilder withPrettyPrint(boolean prettyPrint) {
            if (prettyPrint) {
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            } else {
                objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
            }
            return this;
        }
        
        @Override
        public SerializerBuilder withCharset(Charset charset) {
            if (charset == null) {
                throw new IllegalArgumentException("Charset must not be null");
            }
            this.charset = charset;
            return this;
        }
        
        @Override
        public SerializerBuilder withIgnoreUnknownProperties(boolean ignoreUnknown) {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, !ignoreUnknown);
            return this;
        }
        
        @Override
        public SerializerBuilder withDateFormat(DateFormat dateFormat) {
            if (dateFormat == null) {
                throw new IllegalArgumentException("Date format must not be null");
            }
            objectMapper.setDateFormat(dateFormat);
            return this;
        }
        
        @Override
        public SerializerBuilder withDateFormat(String dateFormat) {
            if (dateFormat == null) {
                throw new IllegalArgumentException("Date format must not be null");
            }
            objectMapper.setDateFormat(new SimpleDateFormat(dateFormat));
            return this;
        }
        
        @Override
        public SerializerBuilder withFailOnEmptyBeans(boolean failOnEmptyBeans) {
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, failOnEmptyBeans);
            return this;
        }
        
        @Override
        public SerializerBuilder withIncludeNulls(boolean includeNulls) {
            if (includeNulls) {
                objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            } else {
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }
            return this;
        }
        
        @Override
        public SerializerBuilder withProperty(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("Property key must not be null");
            }
            properties.put(key, value);
            return this;
        }
        
        @Override
        public SerializerBuilder withProperties(Map<String, Object> properties) {
            if (properties == null) {
                throw new IllegalArgumentException("Properties must not be null");
            }
            this.properties.putAll(properties);
            return this;
        }
        
        @Override
        public SerializerBuilder configure(Consumer<SerializerBuilder> configurer) {
            if (configurer == null) {
                throw new IllegalArgumentException("Configurer must not be null");
            }
            configurer.accept(this);
            return this;
        }
        
        @Override
        public Serializer build() {
            return new JsonSerializer(objectMapper, charset);
        }
    }
    
    /**
     * Creates a new builder for {@link JsonSerializer}.
     * 
     * @return a new builder
     */
    public static JsonSerializerBuilder builder() {
        return new JsonSerializerBuilder();
    }
}