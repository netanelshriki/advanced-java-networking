package com.network.annotation.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.network.NetworkLib;

/**
 * Tests for HTTP annotations and client proxy generation.
 */
public class HttpAnnotationsTest {

    private static ClientAndServer mockServer;
    private static MockServerClient mockServerClient;
    private static int mockServerPort;
    
    @BeforeAll
    public static void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(0);
        mockServerPort = mockServer.getPort();
        mockServerClient = new MockServerClient("localhost", mockServerPort);
    }
    
    @AfterAll
    public static void stopMockServer() {
        if (mockServer != null && mockServer.isRunning()) {
            mockServer.stop();
        }
    }
    
    @BeforeEach
    public void resetMockServer() {
        mockServerClient.reset();
    }
    
    @Test
    @DisplayName("Should create client with HttpClient annotation and execute GET request")
    public void testHttpClientAnnotationWithGet() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/users/123")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"id\":123,\"name\":\"John Doe\",\"email\":\"john@example.com\"}")
            );
        
        // Create client proxy from annotated interface
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Execute method
        User user = client.getUserById(123);
        
        // Verify response
        assertNotNull(user);
        assertEquals(123, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        
        // Verify request was made
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/users/123"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should execute POST request with Body annotation")
    public void testPostWithBodyAnnotation() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/api/users")
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}")
            )
            .respond(
                response()
                    .withStatusCode(201)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"id\":456,\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}")
            );
        
        // Create client proxy
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Create user object
        User userToCreate = new User();
        userToCreate.setName("Jane Doe");
        userToCreate.setEmail("jane@example.com");
        
        // Execute method with @Body parameter
        User createdUser = client.createUser(userToCreate);
        
        // Verify response
        assertNotNull(createdUser);
        assertEquals(456, createdUser.getId());
        assertEquals("Jane Doe", createdUser.getName());
        assertEquals("jane@example.com", createdUser.getEmail());
        
        // Verify request was made
        mockServerClient.verify(
                request()
                    .withMethod("POST")
                    .withPath("/api/users")
                    .withHeader("Content-Type", "application/json"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should apply PathVariable annotations")
    public void testPathVariableAnnotation() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/users/123/posts/456")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"id\":456,\"userId\":123,\"title\":\"Sample Post\",\"content\":\"Lorem ipsum\"}")
            );
        
        // Create client proxy
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Execute method with path variables
        Post post = client.getUserPost(123, 456);
        
        // Verify response
        assertNotNull(post);
        assertEquals(456, post.getId());
        assertEquals(123, post.getUserId());
        assertEquals("Sample Post", post.getTitle());
        
        // Verify request was made with correct path
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/users/123/posts/456"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should apply RequestParam annotations")
    public void testRequestParamAnnotation() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/search")
                    .withQueryStringParameter("q", "test")
                    .withQueryStringParameter("page", "1")
                    .withQueryStringParameter("size", "10")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"results\":[{\"id\":1,\"title\":\"Test Result\"}],\"total\":1}")
            );
        
        // Create client proxy
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Execute method with query parameters
        SearchResult result = client.search("test", 1, 10);
        
        // Verify response
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getResults().size());
        assertEquals("Test Result", result.getResults().get(0).getTitle());
        
        // Verify request was made with correct query parameters
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/search")
                    .withQueryStringParameter("q", "test")
                    .withQueryStringParameter("page", "1")
                    .withQueryStringParameter("size", "10"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should apply Header annotations")
    public void testHeaderAnnotation() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/protected-resource")
                    .withHeader("Authorization", "Bearer test-token")
                    .withHeader("X-API-Key", "test-api-key")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"data\":\"Protected content\"}")
            );
        
        // Create client proxy
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Execute method with header parameters
        String content = client.getProtectedResource("Bearer test-token", "test-api-key");
        
        // Verify response
        assertEquals("Protected content", content);
        
        // Verify request was made with correct headers
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/protected-resource")
                    .withHeader("Authorization", "Bearer test-token")
                    .withHeader("X-API-Key", "test-api-key"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should execute PUT requests")
    public void testPutAnnotation() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("PUT")
                    .withPath("/api/users/789")
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":789,\"name\":\"Updated Name\",\"email\":\"updated@example.com\"}")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"id\":789,\"name\":\"Updated Name\",\"email\":\"updated@example.com\"}")
            );
        
        // Create client proxy
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Create user object for update
        User userToUpdate = new User();
        userToUpdate.setId(789);
        userToUpdate.setName("Updated Name");
        userToUpdate.setEmail("updated@example.com");
        
        // Execute method
        User updatedUser = client.updateUser(789, userToUpdate);
        
        // Verify response
        assertNotNull(updatedUser);
        assertEquals(789, updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        
        // Verify request was made
        mockServerClient.verify(
                request()
                    .withMethod("PUT")
                    .withPath("/api/users/789"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should execute DELETE requests")
    public void testDeleteAnnotation() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("DELETE")
                    .withPath("/api/users/321")
            )
            .respond(
                response()
                    .withStatusCode(204)
            );
        
        // Create client proxy
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Execute method
        client.deleteUser(321);
        
        // Verify request was made
        mockServerClient.verify(
                request()
                    .withMethod("DELETE")
                    .withPath("/api/users/321"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should handle async methods")
    public void testAsyncMethods() throws InterruptedException, ExecutionException {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/users/async/123")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"id\":123,\"name\":\"Async User\",\"email\":\"async@example.com\"}")
            );
        
        // Create client proxy
        UserApiClient client = NetworkLib.createClient(UserApiClient.class);
        
        // Execute async method
        CompletableFuture<User> future = client.getUserByIdAsync(123);
        
        // Wait for and verify result
        User user = future.get();
        assertNotNull(user);
        assertEquals(123, user.getId());
        assertEquals("Async User", user.getName());
        assertEquals("async@example.com", user.getEmail());
        
        // Verify request was made
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/users/async/123"),
                VerificationTimes.once()
        );
    }
    
    @Test
    @DisplayName("Should apply default headers from interface annotation")
    public void testDefaultHeadersAnnotation() {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/default-headers-test")
                    .withHeader("User-Agent", "NetworkLib-Test")
                    .withHeader("Accept", "application/json")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"success\":true}")
            );
        
        // Create client proxy
        ClientWithDefaultHeaders client = NetworkLib.createClient(ClientWithDefaultHeaders.class);
        
        // Execute method
        Map<String, Object> result = client.testDefaultHeaders();
        
        // Verify response
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        
        // Verify request was made with default headers
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/default-headers-test")
                    .withHeader("User-Agent", "NetworkLib-Test")
                    .withHeader("Accept", "application/json"),
                VerificationTimes.once()
        );
    }
    
    // Test model classes
    
    public static class User {
        private int id;
        private String name;
        private String email;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class Post {
        private int id;
        private int userId;
        private String title;
        private String content;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class SearchResult {
        private int total;
        private java.util.List<SearchItem> results;
        
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        
        public java.util.List<SearchItem> getResults() { return results; }
        public void setResults(java.util.List<SearchItem> results) { this.results = results; }
    }
    
    public static class SearchItem {
        private int id;
        private String title;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
    
    // Test API interfaces
    
    @HttpClient(baseUrl = "http://localhost")
    public interface UserApiClient {
        
        @GET("/api/users/{id}")
        User getUserById(@PathVariable("id") int userId);
        
        @POST("/api/users")
        User createUser(@Body User user);
        
        @PUT("/api/users/{id}")
        User updateUser(@PathVariable("id") int userId, @Body User user);
        
        @DELETE("/api/users/{id}")
        void deleteUser(@PathVariable("id") int userId);
        
        @GET("/api/users/{userId}/posts/{postId}")
        Post getUserPost(@PathVariable("userId") int userId, @PathVariable("postId") int postId);
        
        @GET("/api/search")
        SearchResult search(@RequestParam("q") String query, 
                           @RequestParam("page") int page, 
                           @RequestParam("size") int size);
        
        @GET("/api/protected-resource")
        String getProtectedResource(@Header("Authorization") String authToken, 
                                  @Header("X-API-Key") String apiKey);
        
        @GET("/api/users/async/{id}")
        CompletableFuture<User> getUserByIdAsync(@PathVariable("id") int userId);
    }
    
    @HttpClient(baseUrl = "http://localhost")
    @DefaultHeaders({
        @HeaderDef(name = "User-Agent", value = "NetworkLib-Test"),
        @HeaderDef(name = "Accept", value = "application/json")
    })
    public interface ClientWithDefaultHeaders {
        
        @GET("/api/default-headers-test")
        Map<String, Object> testDefaultHeaders();
    }
}
