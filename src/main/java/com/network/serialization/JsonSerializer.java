package com.network.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.network.serialization.SerializationException.SerializationOperation;

/**
 * JSON implementation of the {@link Serializer} interface using Jackson.
 * 
 * <p>This class provides methods for serializing and deserializing objects to and from JSON
 * using the Jackson library.
 */
public class JsonSerializer implements Serializer {
    
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    
    private final ObjectMapper objectMapper;
    private final Charset charset;
    private final String contentType;
    private final Map<String, Object> properties;
    
    /**
     * Creates a new JSON serializer with default settings.
     */
    public JsonSerializer() {
        this(new Builder());
    }
    
    /**
     * Creates a new JSON serializer with the specified builder.
     * 
     * @param builder the builder containing the serializer settings
     */
    private JsonSerializer(Builder builder) {
        this.objectMapper = createObjectMapper(builder);
        this.charset = builder.charset;
        this.contentType = DEFAULT_CONTENT_TYPE + "; charset=" + charset.name().toLowerCase();
        this.properties = new HashMap<>(builder.properties);
    }
    
    /**
     * Creates and configures an ObjectMapper based on the builder settings.
     * 
     * @param builder the builder containing the serializer settings
     * @return the configured ObjectMapper
     */
    private ObjectMapper createObjectMapper(Builder builder) {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure feature flags
        if (builder.prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        
        if (!builder.includeNulls) {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, builder.failOnUnknownProperties);
        
        // Configure property access
        if (builder.useFields) {
            mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
        }
        
        // Configure property naming
        if (builder.useSnakeCase) {
            mapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE);
        }
        
        // Configure date format
        if (builder.dateFormat != null) {
            mapper.setDateFormat(new java.text.SimpleDateFormat(builder.dateFormat));
        }
        
        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(
                SerializationOperation.SERIALIZE, 
                object.getClass(), 
                "JSON", 
                e);
        }
    }
    
    @Override
    public String serializeToString(Object object) throws SerializationException {
        if (object == null) {
            return "";
        }
        
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(
                SerializationOperation.SERIALIZE, 
                object.getClass(), 
                "JSON", 
                e);
        }
    }
    
    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        if (object == null || outputStream == null) {
            return;
        }
        
        try {
            objectMapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new SerializationException(
                SerializationOperation.SERIALIZE, 
                object.getClass(), 
                "JSON", 
                e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        if (bytes == null || bytes.length == 0 || type == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SerializationException(
                SerializationOperation.DESERIALIZE, 
                type, 
                "JSON", 
                e);
        }
    }
    
    @Override
    public <T> T deserialize(String string, Class<T> type) throws SerializationException {
        if (string == null || string.isEmpty() || type == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(string, type);
        } catch (IOException e) {
            throw new SerializationException(
                SerializationOperation.DESERIALIZE, 
                type, 
                "JSON", 
                e);
        }
    }
    
    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) throws SerializationException {
        if (inputStream == null || type == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new SerializationException(
                SerializationOperation.DESERIALIZE, 
                type, 
                "JSON", 
                e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(Object object) throws SerializationException {
        if (object == null) {
            return new HashMap<>();
        }
        
        try {
            if (object instanceof Map) {
                return (Map<String, Object>) object;
            }
            
            return objectMapper.convertValue(object, Map.class);
        } catch (Exception e) {
            throw new SerializationException(
                SerializationOperation.OBJECT_TO_MAP, 
                object.getClass(), 
                "JSON", 
                e);
        }
    }
    
    @Override
    public <T> T fromMap(Map<String, Object> map, Class<T> type) throws SerializationException {
        if (map == null || type == null) {
            return null;
        }
        
        try {
            return objectMapper.convertValue(map, type);
        } catch (Exception e) {
            throw new SerializationException(
                SerializationOperation.MAP_TO_OBJECT, 
                type, 
                "JSON", 
                e);
        }
    }
    
    @Override
    public SerializerBuilder builder() {
        return new Builder();
    }
    
    /**
     * Gets the ObjectMapper used by this serializer.
     * 
     * @return the ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Builder for {@link JsonSerializer}.
     */
    public static class Builder implements SerializerBuilder {
        
        private boolean prettyPrint = false;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean includeNulls = false;
        private boolean useFields = false;
        private boolean useSnakeCase = false;
        private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        private Set<Class<?>> registeredClasses = new HashSet<>();
        private boolean failOnUnknownProperties = false;
        private Map<String, Object> properties = new HashMap<>();
        
        /**
         * Creates a new builder with default settings.
         */
        public Builder() {
            // Use default values
        }
        
        @Override
        public Builder withPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
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
        public Builder withIncludeNulls(boolean includeNulls) {
            this.includeNulls = includeNulls;
            return this;
        }
        
        @Override
        public Builder withUseFields(boolean useFields) {
            this.useFields = useFields;
            return this;
        }
        
        @Override
        public Builder withSnakeCase(boolean useSnakeCase) {
            this.useSnakeCase = useSnakeCase;
            return this;
        }
        
        @Override
        public Builder withDateFormat(String dateFormat) {
            if (dateFormat == null) {
                throw new IllegalArgumentException("Date format must not be null");
            }
            this.dateFormat = dateFormat;
            return this;
        }
        
        @Override
        public Builder registerClass(Class<?> clazz) {
            if (clazz == null) {
                throw new IllegalArgumentException("Class must not be null");
            }
            this.registeredClasses.add(clazz);
            return this;
        }
        
        @Override
        public Builder registerClasses(Set<Class<?>> classes) {
            if (classes == null) {
                throw new IllegalArgumentException("Classes must not be null");
            }
            this.registeredClasses = new HashSet<>(classes);
            return this;
        }
        
        @Override
        public Builder withFailOnUnknownProperties(boolean failOnUnknown) {
            this.failOnUnknownProperties = failOnUnknown;
            return this;
        }
        
        @Override
        public Builder withProperty(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("Property key must not be null");
            }
            this.properties.put(key, value);
            return this;
        }
        
        @Override
        public JsonSerializer build() {
            return new JsonSerializer(this);
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