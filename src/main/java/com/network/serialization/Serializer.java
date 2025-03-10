package com.network.serialization;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Interface for serializing and deserializing objects.
 * 
 * <p>This interface defines methods for converting objects to and from
 * various formats, such as bytes, strings, input streams, and output streams.
 */
public interface Serializer {
    
    /**
     * Gets the content type for this serializer.
     * 
     * <p>The content type is used in HTTP headers and other contexts
     * to identify the format of the serialized data.
     * 
     * @return the content type
     */
    String getContentType();
    
    /**
     * Serializes an object to bytes.
     * 
     * @param object the object to serialize
     * @return the serialized bytes
     * @throws SerializationException if serialization fails
     */
    byte[] serialize(Object object) throws SerializationException;
    
    /**
     * Serializes an object to a string.
     * 
     * @param object the object to serialize
     * @return the serialized string
     * @throws SerializationException if serialization fails
     */
    String serializeToString(Object object) throws SerializationException;
    
    /**
     * Serializes an object to an output stream.
     * 
     * @param object the object to serialize
     * @param outputStream the output stream to write to
     * @throws SerializationException if serialization fails
     */
    void serialize(Object object, OutputStream outputStream) throws SerializationException;
    
    /**
     * Deserializes bytes to an object.
     * 
     * @param <T> the type to deserialize to
     * @param bytes the bytes to deserialize
     * @param type the class of the type to deserialize to
     * @return the deserialized object
     * @throws SerializationException if deserialization fails
     */
    <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException;
    
    /**
     * Deserializes a string to an object.
     * 
     * @param <T> the type to deserialize to
     * @param string the string to deserialize
     * @param type the class of the type to deserialize to
     * @return the deserialized object
     * @throws SerializationException if deserialization fails
     */
    <T> T deserialize(String string, Class<T> type) throws SerializationException;
    
    /**
     * Deserializes an input stream to an object.
     * 
     * @param <T> the type to deserialize to
     * @param inputStream the input stream to read from
     * @param type the class of the type to deserialize to
     * @return the deserialized object
     * @throws SerializationException if deserialization fails
     */
    <T> T deserialize(InputStream inputStream, Class<T> type) throws SerializationException;
    
    /**
     * Creates a map from an object.
     * 
     * <p>This method converts an object to a map representation, which
     * can be useful for inspecting or manipulating the object's properties.
     * 
     * @param object the object to convert
     * @return a map representation of the object
     * @throws SerializationException if conversion fails
     */
    Map<String, Object> toMap(Object object) throws SerializationException;
    
    /**
     * Creates an object from a map.
     * 
     * <p>This method converts a map to an object of the specified type,
     * which can be useful for creating objects from map representations.
     * 
     * @param <T> the type to create
     * @param map the map to convert
     * @param type the class of the type to create
     * @return the created object
     * @throws SerializationException if conversion fails
     */
    <T> T fromMap(Map<String, Object> map, Class<T> type) throws SerializationException;
    
    /**
     * Gets a builder for this serializer.
     * 
     * <p>The builder allows for configuring serialization options
     * and creating a new serializer instance.
     * 
     * @return a new builder
     */
    SerializerBuilder builder();
}