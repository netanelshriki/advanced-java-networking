package com.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import com.network.api.http.HttpClient;
import com.network.api.http.HttpClientBuilder;
import com.network.api.tcp.TcpClient;
import com.network.api.tcp.TcpClientBuilder;
import com.network.api.udp.UdpClient;
import com.network.api.udp.UdpClientBuilder;
import com.network.api.websocket.WebSocketClient;
import com.network.api.websocket.WebSocketClientBuilder;
import com.network.impl.http.DefaultHttpClientConfig;
import com.network.impl.tcp.DefaultTcpClientConfig;
import com.network.impl.udp.DefaultUdpClientConfig;
import com.network.impl.websocket.DefaultWebSocketClientConfig;
import com.network.proxy.ClientProxyFactory;
import com.network.spi.MiddlewareProvider;
import com.network.spi.ProtocolProvider;
import com.network.spi.SerializerProvider;

/**
 * Factory class for creating network clients.
 * 
 * <p>This class provides static factory methods for creating instances of
 * various network clients, such as HTTP, TCP, UDP, and WebSocket clients.
 */
public final class NetworkLib {
    
    private static final Map<String, SerializerProvider> SERIALIZER_PROVIDERS = new HashMap<>();
    private static final Map<String, ProtocolProvider<?>> PROTOCOL_PROVIDERS = new HashMap<>();
    private static final Map<String, MiddlewareProvider> MIDDLEWARE_PROVIDERS = new HashMap<>();
    
    static {
        // Load providers using ServiceLoader
        loadProviders();
    }
    
    private NetworkLib() {
        // Prevent instantiation
        throw new AssertionError("NetworkLib class should not be instantiated");
    }
    
    /**
     * Creates a new HTTP client builder.
     * 
     * @return a new HTTP client builder
     */
    public static HttpClientBuilder createHttpClient() {
        return DefaultHttpClientConfig.builder();
    }
    
    /**
     * Creates a new TCP client builder.
     * 
     * @return a new TCP client builder
     */
    public static TcpClientBuilder createTcpClient() {
        return DefaultTcpClientConfig.builder();
    }
    
    /**
     * Creates a new UDP client builder.
     * 
     * @return a new UDP client builder
     */
    public static UdpClientBuilder createUdpClient() {
        return DefaultUdpClientConfig.builder();
    }
    
    /**
     * Creates a new WebSocket client builder.
     * 
     * @return a new WebSocket client builder
     */
    public static WebSocketClientBuilder createWebSocketClient() {
        return DefaultWebSocketClientConfig.builder();
    }
    
    /**
     * Creates a new HTTP client with the specified host and port.
     * 
     * @param host the host to connect to
     * @param port the port to connect to
     * @return a new HTTP client
     */
    public static HttpClient createHttpClient(String host, int port) {
        return createHttpClient()
            .withBaseUrl("http://" + host + ":" + port)
            .build();
    }
    
    /**
     * Creates a new HTTP client with the specified URL.
     * 
     * @param url the URL to connect to
     * @return a new HTTP client
     */
    public static HttpClient createHttpClient(String url) {
        return createHttpClient()
            .withBaseUrl(url)
            .build();
    }
    
    /**
     * Creates a new TCP client with the specified host and port.
     * 
     * @param host the host to connect to
     * @param port the port to connect to
     * @return a new TCP client
     */
    public static TcpClient createTcpClient(String host, int port) {
        return createTcpClient()
            .withAddress(host, port)
            .build();
    }
    
    /**
     * Creates a new UDP client with the specified host and port.
     * 
     * @param host the host to connect to
     * @param port the port to connect to
     * @return a new UDP client
     */
    public static UdpClient createUdpClient(String host, int port) {
        return createUdpClient()
            .withAddress(host, port)
            .build();
    }
    
    /**
     * Creates a new WebSocket client with the specified URL.
     * 
     * @param url the URL to connect to
     * @return a new WebSocket client
     */
    public static WebSocketClient createWebSocketClient(String url) {
        return createWebSocketClient()
            .withUrl(url)
            .build();
    }
    
    /**
     * Creates a client implementation from an annotated interface.
     * <p>
     * The interface must be annotated with {@link com.network.annotation.http.HttpClient}
     * and methods must be annotated with HTTP method annotations like
     * {@link com.network.annotation.http.GET}, {@link com.network.annotation.http.POST}, etc.
     * </p>
     * 
     * @param <T> the interface type
     * @param interfaceType the interface class
     * @return a proxy implementation of the interface
     * @throws IllegalArgumentException if the interface is not properly annotated
     */
    public static <T> T createClient(Class<T> interfaceType) {
        return ClientProxyFactory.getInstance().createClient(interfaceType);
    }
    
    /**
     * Creates a client implementation from an annotated interface with custom configuration.
     * <p>
     * This method allows for additional configuration of the client beyond what is
     * specified in the annotations.
     * </p>
     * 
     * @param <T> the interface type
     * @param interfaceType the interface class
     * @param configurer a consumer that can configure the client builder
     * @return a proxy implementation of the interface
     * @throws IllegalArgumentException if the interface is not properly annotated
     */
    public static <T> T createClient(Class<T> interfaceType, Consumer<HttpClientBuilder> configurer) {
        // Not implemented yet - would require changes to ClientProxyFactory
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Registers a serializer provider.
     * 
     * @param provider the provider to register
     */
    public static void registerSerializerProvider(SerializerProvider provider) {
        if (provider != null) {
            SERIALIZER_PROVIDERS.put(provider.getMediaType(), provider);
        }
    }
    
    /**
     * Registers a protocol provider.
     * 
     * @param provider the provider to register
     */
    public static void registerProtocolProvider(ProtocolProvider<?> provider) {
        if (provider != null) {
            PROTOCOL_PROVIDERS.put(provider.getProtocolName(), provider);
        }
    }
    
    /**
     * Registers a middleware provider.
     * 
     * @param provider the provider to register
     */
    public static void registerMiddlewareProvider(MiddlewareProvider provider) {
        if (provider != null) {
            MIDDLEWARE_PROVIDERS.put(provider.getName(), provider);
        }
    }
    
    /**
     * Gets a serializer provider for the given media type.
     * 
     * @param mediaType the media type
     * @return the provider, or null if none is registered for the media type
     */
    public static SerializerProvider getSerializerProvider(String mediaType) {
        return SERIALIZER_PROVIDERS.get(mediaType);
    }
    
    /**
     * Gets all registered serializer providers.
     * 
     * @return a list of all registered providers
     */
    public static List<SerializerProvider> getSerializerProviders() {
        return new ArrayList<>(SERIALIZER_PROVIDERS.values());
    }
    
    /**
     * Gets a protocol provider for the given protocol name.
     * 
     * @param protocolName the protocol name
     * @return the provider, or null if none is registered for the protocol
     */
    public static ProtocolProvider<?> getProtocolProvider(String protocolName) {
        return PROTOCOL_PROVIDERS.get(protocolName);
    }
    
    /**
     * Gets all registered protocol providers.
     * 
     * @return a list of all registered providers
     */
    public static List<ProtocolProvider<?>> getProtocolProviders() {
        return new ArrayList<>(PROTOCOL_PROVIDERS.values());
    }
    
    /**
     * Gets a middleware provider for the given name.
     * 
     * @param name the middleware name
     * @return the provider, or null if none is registered with the name
     */
    public static MiddlewareProvider getMiddlewareProvider(String name) {
        return MIDDLEWARE_PROVIDERS.get(name);
    }
    
    /**
     * Gets all registered middleware providers.
     * 
     * @return a list of all registered providers
     */
    public static List<MiddlewareProvider> getMiddlewareProviders() {
        return new ArrayList<>(MIDDLEWARE_PROVIDERS.values());
    }
    
    /**
     * Loads providers using ServiceLoader.
     */
    private static void loadProviders() {
        // Load serializer providers
        ServiceLoader.load(SerializerProvider.class).forEach(provider -> {
            registerSerializerProvider(provider);
        });
        
        // Load protocol providers
        ServiceLoader.load(ProtocolProvider.class).forEach(provider -> {
            registerProtocolProvider(provider);
        });
        
        // Load middleware providers
        ServiceLoader.load(MiddlewareProvider.class).forEach(provider -> {
            registerMiddlewareProvider(provider);
        });
    }
    
    /**
     * Gets the version of the library.
     * 
     * @return the library version
     */
    public static String getVersion() {
        String version = NetworkLib.class.getPackage().getImplementationVersion();
        return version != null ? version : "development";
    }
}