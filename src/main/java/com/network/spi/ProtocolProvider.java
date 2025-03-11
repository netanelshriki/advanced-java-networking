package com.network.spi;

import com.network.api.NetworkClient;

/**
 * Service Provider Interface (SPI) for protocol providers.
 * <p>
 * This interface allows custom protocol implementations to be registered
 * with the library. Implementations can be registered programmatically
 * or discovered through the Java ServiceLoader mechanism.
 * </p>
 * 
 * @param <T> the type of client this provider creates
 */
public interface ProtocolProvider<T extends NetworkClient> {

    /**
     * Gets the name of the protocol this provider supports.
     *
     * @return the protocol name (e.g., "http", "mqtt")
     */
    String getProtocolName();
    
    /**
     * Creates a client configuration builder for this protocol.
     *
     * @return a new configuration builder
     */
    Object createConfigBuilder();
    
    /**
     * Creates a new client instance with the specified configuration.
     *
     * @param config the client configuration
     * @return a new client instance
     */
    T createClient(Object config);
    
    /**
     * Gets the priority of this provider.
     * <p>
     * Higher priority providers will be preferred over lower priority ones
     * when multiple providers support the same protocol.
     * </p>
     *
     * @return the priority (default is 0)
     */
    default int getPriority() {
        return 0;
    }
}
