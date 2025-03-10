package com.network.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.network.serialization.SerializationException.SerializationOperation;
import com.network.serialization.SerializerBuilder.FieldVisibility;
import com.network.serialization.SerializerBuilder.NamingStrategy;

/**
 * JSON implementation of the {@link Serializer} interface.
 * 
 * <p>This class uses Jackson as the underlying JSON processor.
 */
public class JsonSerializer implements Serializer {
    
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    
    private final ObjectMapper objectMapper;
    private final Charset charset;
    private final String contentType;
    
    /**
     * Creates a new JSON serializer with default settings.
     */
    public JsonSerializer() {
        this(new Builder());
    }
    
    /**
     * Creates a new JSON serializer with the specified builder.
     * 
     * @param builder the builder containing the configuration values
     */
    private JsonSerializer(Builder builder) {
        this.objectMapper = createObjectMapper(builder);
        this.charset = builder.charset;
        this.contentType = DEFAULT_CONTENT_TYPE + "; charset=" + charset.name().toLowerCase();
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(SerializationOperation.SERIALIZE, 
                    object != null ? object.getClass() : null, e);
        }
    }
    
    @Override
    public String serializeToString(Object object) throws SerializationException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(SerializationOperation.SERIALIZE, 
                    object != null ? object.getClass() : null, e);
        }
    }
    
    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        try {
            objectMapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new SerializationException(SerializationOperation.SERIALIZE, 
                    object != null ? object.getClass() : null, e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SerializationException(SerializationOperation.DESERIALIZE, type, e);
        }
    }
    
    @Override
    public <T> T deserialize(String string, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(string, type);
        } catch (IOException e) {
            throw new SerializationException(SerializationOperation.DESERIALIZE, type, e);
        }
    }
    
    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new SerializationException(SerializationOperation.DESERIALIZE, type, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> toMap(Object object) throws SerializationException {
        try {
            if (object instanceof Map) {
                return (Map<String, Object>) object;
            }
            return objectMapper.convertValue(object, Map.class);
        } catch (Exception e) {
            throw new SerializationException(SerializationOperation.TO_MAP, 
                    object != null ? object.getClass() : null, e);
        }
    }
    
    @Override
    public <T> T fromMap(Map<String, Object> map, Class<T> type) throws SerializationException {
        try {
            return objectMapper.convertValue(map, type);
        } catch (Exception e) {
            throw new SerializationException(SerializationOperation.FROM_MAP, type, e);
        }
    }
    
    @Override
    public Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a Jackson ObjectMapper with the specified configuration.
     * 
     * @param builder the builder containing the configuration values
     * @return the configured ObjectMapper
     */
    private ObjectMapper createObjectMapper(Builder builder) {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());
        
        // Date format
        if (builder.dateFormat != null) {
            mapper.setDateFormat(new SimpleDateFormat(builder.dateFormat));
        }
        
        // Pretty print
        if (builder.prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        
        // Null handling
        if (!builder.serializeNulls) {
            mapper.setSerializationInclusion(Include.NON_NULL);
        }
        
        // Naming strategy
        if (builder.namingStrategy != null) {
            PropertyNamingStrategy jacksonStrategy;
            switch (builder.namingStrategy) {
                case CAMEL_CASE:
                    jacksonStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE;
                    break;
                case PASCAL_CASE:
                    jacksonStrategy = PropertyNamingStrategies.UPPER_CAMEL_CASE;
                    break;
                case SNAKE_CASE:
                    jacksonStrategy = PropertyNamingStrategies.SNAKE_CASE;
                    break;
                case KEBAB_CASE:
                    jacksonStrategy = PropertyNamingStrategies.KEBAB_CASE;
                    break;
                case UPPER_SNAKE_CASE:
                    jacksonStrategy = PropertyNamingStrategies.UPPER_SNAKE_CASE;
                    break;
                case IDENTITY:
                default:
                    jacksonStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE;
                    break;
            }
            mapper.setPropertyNamingStrategy(jacksonStrategy);
        }
        
        // Field visibility
        if (builder.fieldVisibility != null) {
            Visibility jacksonVisibility;
            switch (builder.fieldVisibility) {
                case PUBLIC:
                    jacksonVisibility = Visibility.PUBLIC_ONLY;
                    break;
                case PROTECTED:
                    jacksonVisibility = Visibility.PROTECTED_AND_PUBLIC;
                    break;
                case PACKAGE:
                    jacksonVisibility = Visibility.ANY;
                    break;
                case PRIVATE:
                    jacksonVisibility = Visibility.ANY;
                    break;
                case ANNOTATED_ONLY:
                    jacksonVisibility = Visibility.NONE;
                    break;
                case NONE:
                    jacksonVisibility = Visibility.NONE;
                    break;
                default:
                    jacksonVisibility = Visibility.PUBLIC_ONLY;
                    break;
            }
            mapper.setVisibility(PropertyAccessor.FIELD, jacksonVisibility);
        }
        
        // Other configuration
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        return mapper;
    }
    
    /**
     * Builder for {@link JsonSerializer}.
     */
    public static class Builder implements SerializerBuilder<Builder> {
        
        private Charset charset = StandardCharsets.UTF_8;
        private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        private boolean prettyPrint = false;
        private boolean serializeNulls = false;
        private boolean usePropertyName = false;
        private NamingStrategy namingStrategy = NamingStrategy.CAMEL_CASE;
        private FieldVisibility fieldVisibility = FieldVisibility.PUBLIC;
        private final Map<String, Object> properties = new HashMap<>();
        
        @Override
        public Builder withCharset(Charset charset) {
            if (charset == null) {
                throw new IllegalArgumentException("Charset must not be null");
            }
            this.charset = charset;
            return this;
        }
        
        @Override
        public Builder withDateFormat(String pattern) {
            if (pattern == null) {
                throw new IllegalArgumentException("Date format pattern must not be null");
            }
            try {
                new SimpleDateFormat(pattern);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid date format pattern: " + pattern, e);
            }
            this.dateFormat = pattern;
            return this;
        }
        
        @Override
        public Builder withPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }
        
        @Override
        public Builder withSerializeNulls(boolean serializeNulls) {
            this.serializeNulls = serializeNulls;
            return this;
        }
        
        @Override
        public Builder withUsePropertyName(boolean usePropertyName) {
            this.usePropertyName = usePropertyName;
            if (usePropertyName) {
                this.namingStrategy = NamingStrategy.IDENTITY;
            }
            return this;
        }
        
        @Override
        public Builder withNamingStrategy(NamingStrategy strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Naming strategy must not be null");
            }
            this.namingStrategy = strategy;
            return this;
        }
        
        @Override
        public Builder withFieldVisibility(FieldVisibility visibility) {
            if (visibility == null) {
                throw new IllegalArgumentException("Field visibility must not be null");
            }
            this.fieldVisibility = visibility;
            return this;
        }
        
        @Override
        public Builder withProperty(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("Property key must not be null");
            }
            properties.put(key, value);
            return this;
        }
        
        @Override
        public Builder configure(Consumer<Builder> configurer) {
            if (configurer == null) {
                throw new IllegalArgumentException("Configurer must not be null");
            }
            configurer.accept(this);
            return this;
        }
        
        @Override
        public Serializer build() {
            return new JsonSerializer(this);
        }
    }
    
    /**
     * Creates a new JSON serializer with default settings.
     * 
     * @return a new JSON serializer
     */
    public static JsonSerializer create() {
        return new JsonSerializer();
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