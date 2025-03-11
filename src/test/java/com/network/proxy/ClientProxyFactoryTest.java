package com.network.proxy;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.network.NetworkLib;
import com.network.annotation.http.DefaultHeaders;
import com.network.annotation.http.GET;
import com.network.annotation.http.HeaderDef;
import com.network.annotation.http.HttpClient;
import com.network.annotation.http.POST;
import com.network.annotation.http.PathVariable;
import com.network.annotation.http.RequestParam;

/**
 * Tests for {@link ClientProxyFactory}.
 */
public class ClientProxyFactoryTest {

    @Test
    @DisplayName("Should create proxy for valid annotated interface")
    public void testCreateClientForValidInterface() {
        // Get factory instance
        ClientProxyFactory factory = ClientProxyFactory.getInstance();
        
        // Create client proxy
        TestApi client = factory.createClient(TestApi.class);
        
        // Verify proxy was created
        assertNotNull(client);
        assertTrue(Proxy.isProxyClass(client.getClass()));
        assertTrue(Proxy.getInvocationHandler(client) instanceof HttpClientInvocationHandler);
    }
    
    @Test
    @DisplayName("Should create client through NetworkLib")
    public void testCreateClientThroughNetworkLib() {
        // Create client proxy
        TestApi client = NetworkLib.createClient(TestApi.class);
        
        // Verify proxy was created
        assertNotNull(client);
        assertTrue(Proxy.isProxyClass(client.getClass()));
        assertTrue(Proxy.getInvocationHandler(client) instanceof HttpClientInvocationHandler);
    }
    
    @Test
    @DisplayName("Should throw exception for non-interface type")
    public void testCreateClientWithNonInterface() {
        // Get factory instance
        ClientProxyFactory factory = ClientProxyFactory.getInstance();
        
        // Try to create client from a class
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.createClient(NonInterfaceClass.class);
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("must be an interface"));
    }
    
    @Test
    @DisplayName("Should throw exception for interface without @HttpClient annotation")
    public void testCreateClientWithoutHttpClientAnnotation() {
        // Get factory instance
        ClientProxyFactory factory = ClientProxyFactory.getInstance();
        
        // Try to create client from interface without @HttpClient
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.createClient(NonAnnotatedInterface.class);
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("must be annotated with @HttpClient"));
    }
    
    @Test
    @DisplayName("Should handle methods without HTTP method annotations")
    public void testMethodWithoutHttpMethodAnnotation() {
        // Create client proxy
        TestApiWithInvalidMethod client = NetworkLib.createClient(TestApiWithInvalidMethod.class);
        
        // Try to call method without HTTP annotation
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            client.methodWithoutHttpAnnotation();
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("HTTP method annotation"));
    }
    
    @Test
    @DisplayName("Should apply configuration from @HttpClient annotation")
    public void testHttpClientAnnotationConfiguration() {
        // Create client proxy for interface with specific configuration
        ConfiguredApi client = NetworkLib.createClient(ConfiguredApi.class);
        
        // Verify proxy was created
        assertNotNull(client);
        
        // Note: It's hard to test the actual configuration without exposing internals
        // or making real requests, but at least we can verify it doesn't throw
    }
    
    @Test
    @DisplayName("Should apply headers from @DefaultHeaders annotation")
    public void testDefaultHeadersAnnotation() {
        // Create client proxy with default headers
        ApiWithDefaultHeaders client = NetworkLib.createClient(ApiWithDefaultHeaders.class);
        
        // Verify proxy was created
        assertNotNull(client);
        
        // Note: Again, hard to test without exposing internals or making real requests
    }
    
    @Test
    @DisplayName("Should use singleton pattern for factory")
    public void testSingletonFactoryInstance() {
        // Get factory instance twice
        ClientProxyFactory instance1 = ClientProxyFactory.getInstance();
        ClientProxyFactory instance2 = ClientProxyFactory.getInstance();
        
        // Verify same instance
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }
    
    // Test classes and interfaces
    
    public static class NonInterfaceClass {
        public void someMethod() {
            // This is not an interface
        }
    }
    
    public interface NonAnnotatedInterface {
        String someMethod();
    }
    
    @HttpClient(baseUrl = "http://localhost:8080/api")
    public interface TestApi {
        
        @GET("/test/{id}")
        String testGet(@PathVariable("id") String id);
        
        @POST("/test")
        void testPost(String body);
        
        @GET("/async")
        CompletableFuture<String> testAsync();
    }
    
    @HttpClient(baseUrl = "http://localhost:8080/api")
    public interface TestApiWithInvalidMethod {
        
        @GET("/valid")
        String validMethod();
        
        // No HTTP method annotation
        String methodWithoutHttpAnnotation();
    }
    
    @HttpClient(
        baseUrl = "http://localhost:8080/api",
        connectionTimeout = 5000,
        readTimeout = 10000,
        followRedirects = true
    )
    public interface ConfiguredApi {
        
        @GET("/configured")
        String testConfigured();
    }
    
    @HttpClient(baseUrl = "http://localhost:8080/api")
    @DefaultHeaders({
        @HeaderDef(name = "User-Agent", value = "TestClient/1.0"),
        @HeaderDef(name = "Accept", value = "application/json")
    })
    public interface ApiWithDefaultHeaders {
        
        @GET("/headers-test")
        String testWithDefaultHeaders();
    }
}
