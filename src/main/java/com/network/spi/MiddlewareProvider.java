package com.network.spi;

/**
 * Service Provider Interface (SPI) for middleware providers.
 * <p>
 * This interface allows custom middleware implementations to be registered
 * with the library. Implementations can be registered programmatically
 * or discovered through the Java ServiceLoader mechanism.
 * </p>
 */
public interface MiddlewareProvider {

    /**
     * Gets the name of the middleware this provider supports.
     *
     * @return the middleware name
     */
    String getName();
    
    /**
     * Gets the type of the middleware this provider creates.
     *
     * @return the middleware class
     */
    Class<?> getMiddlewareType();
    
    /**
     * Creates a new middleware instance.
     * <p>
     * The returned object should be castable to the type returned by
     * {@link #getMiddlewareType()}.
     * </p>
     *
     * @param config configuration object for the middleware, can be null
     * @return a new middleware instance
     */
    Object createMiddleware(Object config);
    
    /**
     * Gets the priority of this provider.
     * <p>
     * Higher priority providers will be preferred over lower priority ones
     * when multiple providers support the same middleware name.
     * </p>
     *
     * @return the priority (default is 0)
     */
    default int getPriority() {
        return 0;
    }
}
