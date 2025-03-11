package com.network.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.network.annotation.http.HttpClient;
import com.network.api.http.HttpClientBuilder;
import com.network.impl.http.DefaultHttpClientConfig;

/**
 * Factory for creating dynamic proxies for annotation-based clients.
 * <p>
 * This class is responsible for creating client implementations from
 * annotated interfaces. It uses Java's dynamic proxy mechanism to
 * create client implementations at runtime.
 * </p>
 */
public class ClientProxyFactory {

    private static final ClientProxyFactory INSTANCE = new ClientProxyFactory();
    
    // Cache of method handlers by interface and method
    private final Map<Class<?>, Map<Method, MethodHandler>> methodHandlers = new ConcurrentHashMap<>();
    
    /**
     * Gets the singleton instance of the factory.
     *
     * @return the factory instance
     */
    public static ClientProxyFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Creates a client implementation from an annotated interface.
     *
     * @param <T>           the interface type
     * @param interfaceType the interface class
     * @return a proxy implementation of the interface
     * @throws IllegalArgumentException if the interface is not properly annotated
     */
    @SuppressWarnings("unchecked")
    public <T> T createClient(Class<T> interfaceType) {
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException("Type must be an interface: " + interfaceType.getName());
        }
        
        HttpClient clientAnnotation = interfaceType.getAnnotation(HttpClient.class);
        if (clientAnnotation == null) {
            throw new IllegalArgumentException("Interface must be annotated with @HttpClient: " + interfaceType.getName());
        }
        
        // Create HTTP client based on annotation
        com.network.api.http.HttpClient httpClient = createHttpClient(interfaceType, clientAnnotation);
        
        // Create invocation handler
        InvocationHandler handler = new HttpClientInvocationHandler(interfaceType, httpClient);
        
        // Create proxy
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler);
    }
    
    /**
     * Creates an HTTP client from annotations on the interface.
     *
     * @param interfaceType     the interface class
     * @param clientAnnotation the client annotation
     * @return a configured HTTP client
     */
    private com.network.api.http.HttpClient createHttpClient(Class<?> interfaceType, HttpClient clientAnnotation) {
        HttpClientBuilder builder = DefaultHttpClientConfig.builder();
        
        // Apply base URL
        if (!clientAnnotation.baseUrl().isEmpty()) {
            builder.withBaseUrl(clientAnnotation.baseUrl());
        }
        
        // Apply timeouts
        builder.withConnectionTimeout(java.time.Duration.ofMillis(clientAnnotation.connectionTimeout()));
        builder.withReadTimeout(java.time.Duration.ofMillis(clientAnnotation.readTimeout()));
        
        // Apply follow redirects setting
        builder.withFollowRedirects(clientAnnotation.followRedirects());
        
        // Process default headers
        com.network.annotation.http.DefaultHeaders headersAnnotation = 
                interfaceType.getAnnotation(com.network.annotation.http.DefaultHeaders.class);
        
        if (headersAnnotation != null) {
            for (com.network.annotation.http.HeaderDef header : headersAnnotation.value()) {
                builder.withDefaultHeader(header.name(), header.value());
            }
        }
        
        // Build and return the client
        return builder.build();
    }
}
