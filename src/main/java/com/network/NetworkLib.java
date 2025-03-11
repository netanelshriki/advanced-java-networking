package com.network;

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

/**
 * Factory class for creating network clients.
 * 
 * <p>This class provides static factory methods for creating instances of
 * various network clients, such as HTTP, TCP, UDP, and WebSocket clients.
 */
public final class NetworkLib {
    
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
     * Gets the version of the library.
     * 
     * @return the library version
     */
    public static String getVersion() {
        String version = NetworkLib.class.getPackage().getImplementationVersion();
        return version != null ? version : "development";
    }
}