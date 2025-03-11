package com.network.annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import com.network.NetworkLib;
import com.network.annotation.http.Body;
import com.network.annotation.http.DELETE;
import com.network.annotation.http.DefaultHeaders;
import com.network.annotation.http.GET;
import com.network.annotation.http.Header;
import com.network.annotation.http.HeaderDef;
import com.network.annotation.http.HttpClient;
import com.network.annotation.http.POST;
import com.network.annotation.http.PUT;
import com.network.annotation.http.PathVariable;
import com.network.annotation.http.RequestParam;
import com.network.annotation.http.Timeout;
import com.network.api.http.HttpResponse;

/**
 * Tests for HTTP client annotations.
 */
public class HttpClientAnnotationTest {
    
    private static ClientAndServer mockServer;
    private static int mockServerPort;
    
    /**
     * Starts the mock server before tests.
     */
    private void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(0);
        mockServerPort = mockServer.getPort();
        new MockServerClient("localhost", mockServerPort);
    }
    
    /**
     * Stops the mock server after tests.
     */
    private void stopMockServer() {
        if (mockServer != null && mockServer.isRunning()) {
            mockServer.stop();
        }
    }
    
    /**
     * Sample API interface using annotations.
     */
    @HttpClient(baseUrl = "http://localhost", connectionTimeout = 5000, readTimeout = 5000)
    @DefaultHeaders({
        @HeaderDef(name = "Content-Type", value = "application/json"),
        @HeaderDef(name = "User-Agent", value = "NetworkLib/Test")
    })
    interface UserApi {
        
        @GET("/users")
        List<User> getAllUsers();
        
        @GET("/users/{id}")
        User getUserById(@PathVariable("id") Long id);
        
        @GET("/users/search")
        List<User> searchUsers(@RequestParam("name") String name, @RequestParam(value = "age", required = false) Integer age);
        
        @POST("/users")
        User createUser(@Body User user);
        
        @PUT("/users/{id}")
        User updateUser(@PathVariable("id") Long id, @Body User user);
        
        @DELETE("/users/{id}")
        void deleteUser(@PathVariable("id") Long id);
        
        @GET("/users/slow")
        @Timeout(1000)
        User getSlowResource();
        
        @GET("/users/headers")
        User getUserWithHeader(@Header("X-Api-Key") String apiKey);
        
        @GET("/users/async/{id}")
        CompletableFuture<User> getUserByIdAsync(@PathVariable("id") Long id);
    }
    
    /**
     * Sample User model for testing.
     */
    public static class User {
        private Long id;
        private String name;
        private int age;
        
        public User() {
        }
        
        public User(Long id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
        
        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', age=" + age + '}';
        }
    }
    
    @Test
    @DisplayName("Should create client from annotated interface")
    public void testCreateClientFromAnnotatedInterface() {
        try {
            startMockServer();
            
            // Override the baseUrl with mock server
            System.setProperty("user.api.baseUrl", "http://localhost:" + mockServerPort);
            
            // Setup mock response for getUserById
            long userId = 1L;
            String userJson = "{\"id\":1,\"name\":\"John Doe\",\"age\":30}";
            
            MockServerClient mockServerClient = new MockServerClient("localhost", mockServerPort);
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/users/" + userId)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("User-Agent", "NetworkLib/Test")
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(userJson)
                );
            
            // Create client from annotated interface
            UserApi userApi = NetworkLib.createClient(UserApi.class);
            assertNotNull(userApi, "Client should not be null");
            
            // Test getUserById method
            User user = userApi.getUserById(userId);
            
            // Verify results
            assertNotNull(user, "User should not be null");
            assertEquals(userId, user.getId());
            assertEquals("John Doe", user.getName());
            assertEquals(30, user.getAge());
            
            // Verify request was made correctly
            mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/users/" + userId)
                    .withHeader("Content-Type", "application/json")
                    .withHeader("User-Agent", "NetworkLib/Test"),
                VerificationTimes.once()
            );
        } finally {
            stopMockServer();
            System.clearProperty("user.api.baseUrl");
        }
    }
    
    @Test
    @DisplayName("Should handle request parameters")
    public void testRequestParameters() {
        try {
            startMockServer();
            
            // Override the baseUrl with mock server
            System.setProperty("user.api.baseUrl", "http://localhost:" + mockServerPort);
            
            // Setup mock response for searchUsers
            String searchName = "John";
            Integer searchAge = 30;
            String usersJson = "[{\"id\":1,\"name\":\"John Smith\",\"age\":30},{\"id\":2,\"name\":\"John Doe\",\"age\":30}]";
            
            MockServerClient mockServerClient = new MockServerClient("localhost", mockServerPort);
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/users/search")
                        .withQueryStringParameter("name", searchName)
                        .withQueryStringParameter("age", searchAge.toString())
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(usersJson)
                );
            
            // Create client from annotated interface
            UserApi userApi = NetworkLib.createClient(UserApi.class);
            
            // Test searchUsers method
            List<User> users = userApi.searchUsers(searchName, searchAge);
            
            // Verify results
            assertNotNull(users, "Users should not be null");
            assertEquals(2, users.size());
            assertEquals("John Smith", users.get(0).getName());
            assertEquals(30, users.get(0).getAge());
            
            // Verify request was made correctly
            mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/users/search")
                    .withQueryStringParameter("name", searchName)
                    .withQueryStringParameter("age", searchAge.toString()),
                VerificationTimes.once()
            );
            
            // Test with optional parameter as null
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/users/search")
                        .withQueryStringParameter("name", searchName)
                        .withoutQueryStringParameter("age")
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("[{\"id\":3,\"name\":\"John Adams\",\"age\":45}]")
                );
            
            List<User> usersWithoutAge = userApi.searchUsers(searchName, null);
            
            // Verify results
            assertNotNull(usersWithoutAge, "Users should not be null");
            assertEquals(1, usersWithoutAge.size());
            assertEquals("John Adams", usersWithoutAge.get(0).getName());
            
            // Verify request was made correctly (without age parameter)
            mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/users/search")
                    .withQueryStringParameter("name", searchName)
                    .withoutQueryStringParameter("age"),
                VerificationTimes.once()
            );
        } finally {
            stopMockServer();
            System.clearProperty("user.api.baseUrl");
        }
    }
    
    @Test
    @DisplayName("Should handle POST with request body")
    public void testPostWithRequestBody() {
        try {
            startMockServer();
            
            // Override the baseUrl with mock server
            System.setProperty("user.api.baseUrl", "http://localhost:" + mockServerPort);
            
            // Create user to post
            User newUser = new User(null, "Jane Doe", 25);
            String requestJson = "{\"name\":\"Jane Doe\",\"age\":25}";
            String responseJson = "{\"id\":3,\"name\":\"Jane Doe\",\"age\":25}";
            
            MockServerClient mockServerClient = new MockServerClient("localhost", mockServerPort);
            mockServerClient
                .when(
                    request()
                        .withMethod("POST")
                        .withPath("/users")
                        .withHeader("Content-Type", "application/json")
                        .withBody(requestJson)
                )
                .respond(
                    response()
                        .withStatusCode(201)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(responseJson)
                );
            
            // Create client from annotated interface
            UserApi userApi = NetworkLib.createClient(UserApi.class);
            
            // Test createUser method
            User createdUser = userApi.createUser(newUser);
            
            // Verify results
            assertNotNull(createdUser, "Created user should not be null");
            assertEquals(3L, createdUser.getId());
            assertEquals("Jane Doe", createdUser.getName());
            assertEquals(25, createdUser.getAge());
            
            // Verify request was made correctly
            mockServerClient.verify(
                request()
                    .withMethod("POST")
                    .withPath("/users")
                    .withHeader("Content-Type", "application/json")
                    .withBody(requestJson),
                VerificationTimes.once()
            );
        } finally {
            stopMockServer();
            System.clearProperty("user.api.baseUrl");
        }
    }
    
    @Test
    @DisplayName("Should handle custom headers")
    public void testCustomHeaders() {
        try {
            startMockServer();
            
            // Override the baseUrl with mock server
            System.setProperty("user.api.baseUrl", "http://localhost:" + mockServerPort);
            
            // Setup mock response
            String apiKey = "test-api-key-123";
            String userJson = "{\"id\":4,\"name\":\"API User\",\"age\":40}";
            
            MockServerClient mockServerClient = new MockServerClient("localhost", mockServerPort);
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/users/headers")
                        .withHeader("X-Api-Key", apiKey)
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(userJson)
                );
            
            // Create client from annotated interface
            UserApi userApi = NetworkLib.createClient(UserApi.class);
            
            // Test getUserWithHeader method
            User user = userApi.getUserWithHeader(apiKey);
            
            // Verify results
            assertNotNull(user, "User should not be null");
            assertEquals(4L, user.getId());
            assertEquals("API User", user.getName());
            assertEquals(40, user.getAge());
            
            // Verify request was made correctly
            mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/users/headers")
                    .withHeader("X-Api-Key", apiKey),
                VerificationTimes.once()
            );
        } finally {
            stopMockServer();
            System.clearProperty("user.api.baseUrl");
        }
    }
    
    @Test
    @DisplayName("Should handle path variables")
    public void testPathVariables() {
        try {
            startMockServer();
            
            // Override the baseUrl with mock server
            System.setProperty("user.api.baseUrl", "http://localhost:" + mockServerPort);
            
            // Setup mock response for updating a user
            long userId = 5L;
            User updatedUser = new User(userId, "Updated User", 50);
            String requestJson = "{\"id\":5,\"name\":\"Updated User\",\"age\":50}";
            String responseJson = "{\"id\":5,\"name\":\"Updated User\",\"age\":50}";
            
            MockServerClient mockServerClient = new MockServerClient("localhost", mockServerPort);
            mockServerClient
                .when(
                    request()
                        .withMethod("PUT")
                        .withPath("/users/" + userId)
                        .withBody(requestJson)
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(responseJson)
                );
            
            // Create client from annotated interface
            UserApi userApi = NetworkLib.createClient(UserApi.class);
            
            // Test updateUser method
            User resultUser = userApi.updateUser(userId, updatedUser);
            
            // Verify results
            assertNotNull(resultUser, "Updated user should not be null");
            assertEquals(userId, resultUser.getId());
            assertEquals("Updated User", resultUser.getName());
            assertEquals(50, resultUser.getAge());
            
            // Verify request was made correctly
            mockServerClient.verify(
                request()
                    .withMethod("PUT")
                    .withPath("/users/" + userId)
                    .withBody(requestJson),
                VerificationTimes.once()
            );
        } finally {
            stopMockServer();
            System.clearProperty("user.api.baseUrl");
        }
    }
    
    @Test
    @DisplayName("Should handle asynchronous requests")
    public void testAsyncRequests() throws Exception {
        try {
            startMockServer();
            
            // Override the baseUrl with mock server
            System.setProperty("user.api.baseUrl", "http://localhost:" + mockServerPort);
            
            // Setup mock response
            long userId = 6L;
            String userJson = "{\"id\":6,\"name\":\"Async User\",\"age\":60}";
            
            MockServerClient mockServerClient = new MockServerClient("localhost", mockServerPort);
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/users/async/" + userId)
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(userJson)
                );
            
            // Create client from annotated interface
            UserApi userApi = NetworkLib.createClient(UserApi.class);
            
            // Test getUserByIdAsync method
            CompletableFuture<User> userFuture = userApi.getUserByIdAsync(userId);
            
            // Wait for the future to complete
            User user = userFuture.get();
            
            // Verify results
            assertNotNull(user, "User should not be null");
            assertEquals(userId, user.getId());
            assertEquals("Async User", user.getName());
            assertEquals(60, user.getAge());
            
            // Verify request was made correctly
            mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/users/async/" + userId),
                VerificationTimes.once()
            );
        } finally {
            stopMockServer();
            System.clearProperty("user.api.baseUrl");
        }
    }
    
    @Test
    @DisplayName("Should handle DELETE requests")
    public void testDeleteRequests() {
        try {
            startMockServer();
            
            // Override the baseUrl with mock server
            System.setProperty("user.api.baseUrl", "http://localhost:" + mockServerPort);
            
            // Setup mock response
            long userId = 7L;
            
            MockServerClient mockServerClient = new MockServerClient("localhost", mockServerPort);
            mockServerClient
                .when(
                    request()
                        .withMethod("DELETE")
                        .withPath("/users/" + userId)
                )
                .respond(
                    response()
                        .withStatusCode(204)
                );
            
            // Create client from annotated interface
            UserApi userApi = NetworkLib.createClient(UserApi.class);
            
            // Test deleteUser method
            userApi.deleteUser(userId);
            
            // Verify request was made correctly
            mockServerClient.verify(
                request()
                    .withMethod("DELETE")
                    .withPath("/users/" + userId),
                VerificationTimes.once()
            );
        } finally {
            stopMockServer();
            System.clearProperty("user.api.baseUrl");
        }
    }
}
