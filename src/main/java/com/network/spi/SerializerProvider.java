package com.network.spi;

import java.lang.reflect.Type;

/**
 * Service Provider Interface (SPI) for serialization providers.
 * <p>
 * This interface allows custom serializers to be registered with the library.
 * Implementations can be registered programmatically or discovered through
 * the Java ServiceLoader mechanism.
 * </p>
 */
public interface SerializerProvider {

    /**
     * Checks if this provider supports the given media type.
     *
     * @param mediaType the media type to check
     * @return true if the provider supports the media type, false otherwise
     */
    boolean supportsMediaType(String mediaType);
    
    /**
     * Gets the primary media type produced by this serializer.
     * 
     * @return the primary media type (e.g., "application/json")
     */
    String getMediaType();
    
    /**
     * Deserializes data from the given byte array to the specified type.
     *
     * @param <T>  the target type
     * @param data the data to deserialize
     * @param type the type to deserialize to
     * @return the deserialized object
     * @throws Exception if deserialization fails
     */
    <T> T deserialize(byte[] data, Class<T> type) throws Exception;
    
    /**
     * Deserializes data from the given byte array to the specified generic type.
     * <p>
     * This method is useful for deserializing generic types like List&lt;User&gt;.
     * </p>
     *
     * @param <T>  the target type
     * @param data the data to deserialize
     * @param type the generic type to deserialize to
     * @return the deserialized object
     * @throws Exception if deserialization fails
     */
    <T> T deserialize(byte[] data, Type type) throws Exception;
    
    /**
     * Serializes the given object to a byte array.
     *
     * @param obj the object to serialize
     * @return the serialized data
     * @throws Exception if serialization fails
     */
    byte[] serialize(Object obj) throws Exception;
    
    /**
     * Gets the priority of this provider.
     * <p>
     * Higher priority providers will be preferred over lower priority ones
     * when multiple providers support the same media type.
     * </p>
     *
     * @return the priority (default is 0)
     */
    default int getPriority() {
        return 0;
    }
}
