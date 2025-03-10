package com.network.serialization.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.network.serialization.SerializationException;
import com.network.serialization.SerializationException.Direction;
import com.network.serialization.Serializer;
import com.network.serialization.SerializerBuilder;

/**
 * JSON serializer implemented using Jackson.
 * 
 * <p>This class provides serialization and deserialization of objects to and from
 * JSON format using the Jackson library.
 */
public class JacksonJsonSerializer implements Serializer {
    
    private static final Logger logger = LoggerFactory.getLogger(JacksonJsonSerializer.class);
    
    private static final String CONTENT_TYPE = "application/json";
    
    private final ObjectMapper mapper;
    private final Charset charset;
    
    /**
     * Creates a new JSON serializer with the default configuration.
     */
    public JacksonJsonSerializer() {
        this(new Builder());
    }
    
    /**
     * Creates a new JSON serializer with the specified configuration.
     * 
     * @param builder the builder containing the configuration
     */
    protected JacksonJsonSerializer(Builder builder) {
        this.charset = builder.charset;
        this.mapper = createMapper(builder);
    }
    
    /**
     * Creates and configures the Jackson ObjectMapper.
     * 
     * @param builder the builder containing the configuration
     * @return the configured ObjectMapper
     */
    private ObjectMapper createMapper(Builder builder) {
        ObjectMapper mapper = JsonMapper.builder().build();
        
        // Configure features
        if (builder.prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        
        if (builder.ignoreUnknownProperties) {
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        } else {
            mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
        
        if (builder.failOnEmptyBeans) {
            mapper.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        } else {
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        }
        
        if (builder.includeNulls) {
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        } else {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        
        // Configure property naming
        if (builder.useSnakeCase) {
            mapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
        }
        
        // Configure type info
        if (builder.includeTypeInfo) {
            mapper.enableDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        }
        
        // Add modules
        mapper.registerModule(new JavaTimeModule());
        
        // Use field access by default (less restrictive than properties)
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        return mapper;
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
            throw new SerializationException(Direction.SERIALIZE, object.getClass(), e);
        }
    }
    
    @Override
    public String serializeToString(Object object) throws SerializationException {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(Direction.SERIALIZE, object.getClass(), e);
        }
    }
    
    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        try {
            mapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new SerializationException(Direction.SERIALIZE, object.getClass(), e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        try {
            return mapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SerializationException(Direction.DESERIALIZE, type, e);
        }
    }
    
    @Override
    public <T> T deserialize(String string, Class<T> type) throws SerializationException {
        try {
            return mapper.readValue(string, type);
        } catch (IOException e) {
            throw new SerializationException(Direction.DESERIALIZE, type, e);
        }
    }
    
    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) throws SerializationException {
        try {
            return mapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new SerializationException(Direction.DESERIALIZE, type, e);
        }
    }
    
    @Override
    public Map<String, Object> toMap(Object object) throws SerializationException {
        try {
            return mapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new SerializationException(Direction.SERIALIZE, object.getClass(), e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromMap(Map<String, Object> map, Class<T> type) throws SerializationException {
        try {
            return mapper.convertValue(map, type);
        } catch (Exception e) {
            throw new SerializationException(Direction.DESERIALIZE, type, e);
        }
    }
    
    @Override
    public Builder builder() {
        return new Builder(this);
    }
    
    /**
     * Gets the underlying Jackson ObjectMapper.
     * 
     * @return the ObjectMapper
     */
    public ObjectMapper getMapper() {
        return mapper;
    }
    
    /**
     * Builder for {@link JacksonJsonSerializer}.
     */
    public static class Builder implements SerializerBuilder {
        
        private boolean prettyPrint = false;
        private Charset charset = StandardCharsets.UTF_8;
        private boolean ignoreUnknownProperties = true;
        private boolean useSnakeCase = false;
        private boolean includeNulls = false;
        private boolean failOnEmptyBeans = false;
        private boolean includeTypeInfo = false;
        private final Map<String, Object> properties = new HashMap<>();
        
        /**
         * Creates a new builder with default values.
         */
        public Builder() {
            // Use default values
        }
        
        /**
         * Creates a new builder initialized with values from the specified serializer.
         * 
         * @param serializer the serializer to copy values from
         */
        public Builder(JacksonJsonSerializer serializer) {
            this.charset = serializer.charset;
            
            // Extract configuration from the mapper
            ObjectMapper mapper = serializer.getMapper();
            this.prettyPrint = mapper.isEnabled(SerializationFeature.INDENT_OUTPUT);
            this.ignoreUnknownProperties = !mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            this.useSnakeCase = mapper.getPropertyNamingStrategy() instanceof 
                com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
            this.includeNulls = mapper.getSerializationInclusion() == JsonInclude.Include.ALWAYS;
            this.failOnEmptyBeans = mapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            this.includeTypeInfo = mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
        public Builder withIgnoreUnknownProperties(boolean ignoreUnknown) {
            this.ignoreUnknownProperties = ignoreUnknown;
            return this;
        }
        
        @Override
        public Builder withSnakeCase(boolean useSnakeCase) {
            this.useSnakeCase = useSnakeCase;
            return this;
        }
        
        @Override
        public Builder withIncludeNulls(boolean includeNulls) {
            this.includeNulls = includeNulls;
            return this;
        }
        
        @Override
        public Builder withFailOnEmptyBeans(boolean failOnEmptyBeans) {
            this.failOnEmptyBeans = failOnEmptyBeans;
            return this;
        }
        
        @Override
        public Builder withIncludeTypeInfo(boolean includeTypeInfo) {
            this.includeTypeInfo = includeTypeInfo;
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
        public Builder configure(java.util.function.Consumer<SerializerBuilder> configurer) {
            if (configurer == null) {
                throw new IllegalArgumentException("Configurer must not be null");
            }
            configurer.accept(this);
            return this;
        }
        
        @Override
        public JacksonJsonSerializer build() {
            return new JacksonJsonSerializer(this);
        }
    }
    
    /**
     * Static factory method for creating a JSON serializer builder.
     * 
     * @return a new JSON serializer builder
     */
    public static Builder builder() {
        return new Builder();
    }
}