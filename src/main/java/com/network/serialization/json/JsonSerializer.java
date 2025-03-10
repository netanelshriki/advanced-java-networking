package com.network.serialization.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.network.serialization.SerializationException;
import com.network.serialization.SerializationException.Direction;
import com.network.serialization.Serializer;
import com.network.serialization.SerializerBuilder;

/**
 * JSON implementation of the {@link Serializer} interface.
 * 
 * <p>This class uses Jackson to serialize and deserialize objects to and from JSON.
 */
public class JsonSerializer implements Serializer {
    
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    
    private final ObjectMapper objectMapper;
    private final Charset charset;
    
    /**
     * Creates a new JSON serializer with default settings.
     */
    public JsonSerializer() {
        this(createDefaultObjectMapper(), StandardCharsets.UTF_8);
    }
    
    /**
     * Creates a new JSON serializer with the specified object mapper and charset.
     * 
     * @param objectMapper the object mapper to use
     * @param charset the charset to use for string conversion
     */
    public JsonSerializer(ObjectMapper objectMapper, Charset charset) {
        this.objectMapper = objectMapper;
        this.charset = charset;
    }
    
    @Override
    public String getContentType() {
        return DEFAULT_CONTENT_TYPE;
    }
    
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object to JSON", e, 
                object != null ? object.getClass() : null, Direction.SERIALIZE);
        }
    }
    
    @Override
    public String serializeToString(Object object) throws SerializationException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object to JSON string", e, 
                object != null ? object.getClass() : null, Direction.SERIALIZE);
        }
    }
    
    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        try {
            objectMapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize object to JSON output stream", e, 
                object != null ? object.getClass() : null, Direction.SERIALIZE);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize JSON to object", e, 
                type, Direction.DESERIALIZE);
        }
    }
    
    @Override
    public <T> T deserialize(String string, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(string, type);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize JSON string to object", e, 
                type, Direction.DESERIALIZE);
        }
    }
    
    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> type) throws SerializationException {
        try {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize JSON input stream to object", e, 
                type, Direction.DESERIALIZE);
        }
    }
    
    @Override
    public Map<String, Object> toMap(Object object) throws SerializationException {
        try {
            if (object == null) {
                return Collections.emptyMap();
            }
            
            return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException e) {
            throw new SerializationException("Failed to convert object to map", e, 
                object != null ? object.getClass() : null, Direction.SERIALIZE);
        }
    }
    
    @Override
    public <T> T fromMap(Map<String, Object> map, Class<T> type) throws SerializationException {
        try {
            if (map == null) {
                return null;
            }
            
            return objectMapper.convertValue(map, type);
        } catch (IllegalArgumentException e) {
            throw new SerializationException("Failed to convert map to object", e, 
                type, Direction.DESERIALIZE);
        }
    }
    
    @Override
    public SerializerBuilder builder() {
        return new JsonSerializerBuilder(this);
    }
    
    /**
     * Gets the object mapper used by this serializer.
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
     * Creates a default object mapper for JSON serialization.
     * 
     * @return a new object mapper
     */
    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure default settings
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        mapper.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        
        return mapper;
    }
    
    /**
     * Builder for {@link JsonSerializer}.
     */
    public static class JsonSerializerBuilder implements SerializerBuilder {
        
        private final ObjectMapper baseMapper;
        private boolean prettyPrint = false;
        private Charset charset = StandardCharsets.UTF_8;
        private DateFormat dateFormat = null;
        private boolean includeNulls = true;
        private boolean writeDatesAsTimestamp = false;
        private boolean caseSensitiveFields = true;
        private boolean failOnUnknownProperties = false;
        private Set<String> includeFields = null;
        private Set<String> excludeFields = null;
        
        /**
         * Creates a new builder with the specified base serializer.
         * 
         * @param baseSerializer the base serializer to copy settings from
         */
        public JsonSerializerBuilder(JsonSerializer baseSerializer) {
            this.baseMapper = baseSerializer.getObjectMapper();
            this.charset = baseSerializer.getCharset();
        }
        
        @Override
        public SerializerBuilder withPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
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
        public SerializerBuilder withDateFormat(DateFormat dateFormat) {
            if (dateFormat == null) {
                throw new IllegalArgumentException("Date format must not be null");
            }
            this.dateFormat = dateFormat;
            return this;
        }
        
        @Override
        public SerializerBuilder withDateFormat(String pattern) {
            if (pattern == null) {
                throw new IllegalArgumentException("Date format pattern must not be null");
            }
            this.dateFormat = new SimpleDateFormat(pattern);
            return this;
        }
        
        @Override
        public SerializerBuilder withIncludeNulls(boolean includeNulls) {
            this.includeNulls = includeNulls;
            return this;
        }
        
        @Override
        public SerializerBuilder withWriteDatesAsTimestamp(boolean asTimestamp) {
            this.writeDatesAsTimestamp = asTimestamp;
            return this;
        }
        
        @Override
        public SerializerBuilder withCaseSensitiveFields(boolean caseSensitive) {
            this.caseSensitiveFields = caseSensitive;
            return this;
        }
        
        @Override
        public SerializerBuilder withFailOnUnknownProperties(boolean failOnUnknown) {
            this.failOnUnknownProperties = failOnUnknown;
            return this;
        }
        
        @Override
        public SerializerBuilder withIncludeFields(Set<String> fields) {
            if (fields == null) {
                throw new IllegalArgumentException("Fields must not be null");
            }
            this.includeFields = new HashSet<>(fields);
            return this;
        }
        
        @Override
        public SerializerBuilder withExcludeFields(Set<String> fields) {
            if (fields == null) {
                throw new IllegalArgumentException("Fields must not be null");
            }
            this.excludeFields = new HashSet<>(fields);
            return this;
        }
        
        @Override
        public SerializerBuilder configure(java.util.function.Consumer<SerializerBuilder> configurer) {
            if (configurer == null) {
                throw new IllegalArgumentException("Configurer must not be null");
            }
            configurer.accept(this);
            return this;
        }
        
        @Override
        public Serializer build() {
            ObjectMapper mapper = baseMapper.copy();
            
            // Apply configuration
            if (prettyPrint) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            } else {
                mapper.disable(SerializationFeature.INDENT_OUTPUT);
            }
            
            if (dateFormat != null) {
                mapper.setDateFormat(dateFormat);
            }
            
            if (includeNulls) {
                mapper.setSerializationInclusion(Include.ALWAYS);
            } else {
                mapper.setSerializationInclusion(Include.NON_NULL);
            }
            
            if (writeDatesAsTimestamp) {
                mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            } else {
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
            
            if (caseSensitiveFields) {
                mapper.enable(MapperFeature.CASE_SENSITIVE_PROPERTIES);
            } else {
                mapper.disable(MapperFeature.CASE_SENSITIVE_PROPERTIES);
            }
            
            if (failOnUnknownProperties) {
                mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            } else {
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
            
            // Apply field filtering through a filter provider if needed
            if (includeFields != null || excludeFields != null) {
                // This would be implemented using Jackson's filter providers
                // but for simplicity, we'll skip this in this example
            }
            
            return new JsonSerializer(mapper, charset);
        }
    }
}