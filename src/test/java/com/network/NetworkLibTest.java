package com.network;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.network.api.http.HttpClient;
import com.network.api.http.HttpClientBuilder;
import com.network.api.tcp.TcpClient;
import com.network.api.tcp.TcpClientBuilder;
import com.network.api.udp.UdpClient;
import com.network.api.udp.UdpClientBuilder;
import com.network.api.websocket.WebSocketClient;
import com.network.api.websocket.WebSocketClientBuilder;
import com.network.middleware.SampleMiddleware;
import com.network.spi.MiddlewareProvider;
import com.network.spi.ProtocolProvider;
import com.network.spi.SerializerProvider;

/**
 * Tests for {@link NetworkLib} factory methods.
 */
public class NetworkLibTest {

    @Test
    @DisplayName("Should create HttpClientBuilder")
    public void testCreateHttpClientBuilder() {
        HttpClientBuilder builder = NetworkLib.createHttpClient();
        assertNotNull(builder, "HttpClientBuilder should not be null");
    }

    @Test
    @DisplayName("Should create TcpClientBuilder")
    public void testCreateTcpClientBuilder() {
        TcpClientBuilder builder = NetworkLib.createTcpClient();
        assertNotNull(builder, "TcpClientBuilder should not be null");
    }

    @Test
    @DisplayName("Should create UdpClientBuilder")
    public void testCreateUdpClientBuilder() {
        UdpClientBuilder builder = NetworkLib.createUdpClient();
        assertNotNull(builder, "UdpClientBuilder should not be null");
    }

    @Test
    @DisplayName("Should create WebSocketClientBuilder")
    public void testCreateWebSocketClientBuilder() {
        WebSocketClientBuilder builder = NetworkLib.createWebSocketClient();
        assertNotNull(builder, "WebSocketClientBuilder should not be null");
    }
    
    @Test
    @DisplayName("Should create HttpClient with host and port")
    public void testCreateHttpClientWithHostAndPort() {
        HttpClient client = NetworkLib.createHttpClient("localhost", 8080);
        assertNotNull(client, "HttpClient should not be null");
        assertEquals("http://localhost:8080", client.getBaseUrl().toString());
    }
    
    @Test
    @DisplayName("Should create HttpClient with URL")
    public void testCreateHttpClientWithUrl() {
        HttpClient client = NetworkLib.createHttpClient("http://example.com/api");
        assertNotNull(client, "HttpClient should not be null");
        assertEquals("http://example.com/api", client.getBaseUrl().toString());
    }
    
    @Test
    @DisplayName("Should create TCP client with host and port")
    public void testCreateTcpClientWithHostAndPort() {
        TcpClient client = NetworkLib.createTcpClient("localhost", 9000);
        assertNotNull(client, "TcpClient should not be null");
        // We can't easily test the internal state, but at least we know it doesn't throw
    }
    
    @Test
    @DisplayName("Should create UDP client with host and port")
    public void testCreateUdpClientWithHostAndPort() {
        UdpClient client = NetworkLib.createUdpClient("localhost", 9001);
        assertNotNull(client, "UdpClient should not be null");
        // We can't easily test the internal state, but at least we know it doesn't throw
    }
    
    @Test
    @DisplayName("Should create WebSocket client with URL")
    public void testCreateWebSocketClientWithUrl() {
        WebSocketClient client = NetworkLib.createWebSocketClient("ws://localhost:8080/socket");
        assertNotNull(client, "WebSocketClient should not be null");
        // We can't easily test the internal state, but at least we know it doesn't throw
    }

    @Test
    @DisplayName("Should return library version")
    public void testGetVersion() {
        String version = NetworkLib.getVersion();
        assertNotNull(version, "Version should not be null");
        // Since we're testing locally without proper packaging, it's expected to be "development"
        assertEquals("development", version);
    }

    @Test
    @DisplayName("Should register and retrieve middleware provider")
    public void testRegisterAndGetMiddlewareProvider() {
        // Create a test middleware provider
        MiddlewareProvider provider = new MiddlewareProvider() {
            @Override
            public String getName() {
                return "test-middleware";
            }
            
            @Override
            public Object createMiddleware() {
                return new SampleMiddleware();
            }
        };
        
        // Register the provider
        NetworkLib.registerMiddlewareProvider(provider);
        
        // Retrieve the provider
        MiddlewareProvider retrievedProvider = NetworkLib.getMiddlewareProvider("test-middleware");
        
        // Verify
        assertNotNull(retrievedProvider, "Retrieved provider should not be null");
        assertEquals("test-middleware", retrievedProvider.getName());
    }

    @Test
    @DisplayName("Should get all middleware providers")
    public void testGetAllMiddlewareProviders() {
        // Register test providers
        MiddlewareProvider provider1 = new MiddlewareProvider() {
            @Override
            public String getName() {
                return "test-middleware-1";
            }
            
            @Override
            public Object createMiddleware() {
                return new SampleMiddleware();
            }
        };
        
        MiddlewareProvider provider2 = new MiddlewareProvider() {
            @Override
            public String getName() {
                return "test-middleware-2";
            }
            
            @Override
            public Object createMiddleware() {
                return new SampleMiddleware();
            }
        };
        
        NetworkLib.registerMiddlewareProvider(provider1);
        NetworkLib.registerMiddlewareProvider(provider2);
        
        // Get all providers
        var providers = NetworkLib.getMiddlewareProviders();
        
        // Verify
        assertNotNull(providers, "Providers list should not be null");
        assertTrue(providers.contains(provider1), "Providers should contain provider1");
        assertTrue(providers.contains(provider2), "Providers should contain provider2");
    }

    @Test
    @DisplayName("Should handle null provider registration gracefully")
    public void testRegisterNullProvider() {
        // Shouldn't throw an exception
        NetworkLib.registerSerializerProvider(null);
        NetworkLib.registerProtocolProvider(null);
        NetworkLib.registerMiddlewareProvider(null);
    }

    @Test
    @DisplayName("Should return null for nonexistent provider")
    public void testGetNonexistentProvider() {
        SerializerProvider serializer = NetworkLib.getSerializerProvider("nonexistent");
        ProtocolProvider<?> protocol = NetworkLib.getProtocolProvider("nonexistent");
        MiddlewareProvider middleware = NetworkLib.getMiddlewareProvider("nonexistent");
        
        assertNull(serializer, "Should return null for nonexistent serializer provider");
        assertNull(protocol, "Should return null for nonexistent protocol provider");
        assertNull(middleware, "Should return null for nonexistent middleware provider");
    }
}
