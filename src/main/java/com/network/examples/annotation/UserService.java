package com.network.examples.annotation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.network.annotation.http.Body;
import com.network.annotation.http.DELETE;
import com.network.annotation.http.DefaultHeaders;
import com.network.annotation.http.GET;
import com.network.annotation.http.HeaderDef;
import com.network.annotation.http.HttpClient;
import com.network.annotation.http.POST;
import com.network.annotation.http.PUT;
import com.network.annotation.http.PathVariable;
import com.network.annotation.http.RequestParam;
import com.network.annotation.http.Timeout;
import com.network.annotation.resilience.Backoff;
import com.network.annotation.resilience.CircuitBreaker;
import com.network.annotation.resilience.RateLimit;
import com.network.annotation.resilience.Retry;

/**
 * Example interface demonstrating annotation-based HTTP client.
 * <p>
 * This interface uses annotations to define HTTP endpoints for interacting with a user service.
 * </p>
 */
@HttpClient(baseUrl = "https://api.example.com", connectionTimeout = 5000, readTimeout = 10000)
@DefaultHeaders({
    @HeaderDef(name = "Accept", value = "application/json"),
    @HeaderDef(name = "User-Agent", value = "AdvancedNetworking/1.0")
})
public interface UserService {

    /**
     * Get all users with optional filtering.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param nameFilter optional name filter
     * @return a list of users
     */
    @GET("/users")
    @Timeout(5000)
    List<User> getUsers(
        @RequestParam("page") int page,
        @RequestParam("size") int size,
        @RequestParam(value = "name", required = false) String nameFilter
    );
    
    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user
     */
    @GET("/users/{id}")
    @CircuitBreaker(failureThreshold = 3, resetTimeout = 30000)
    User getUserById(@PathVariable("id") String id);
    
    /**
     * Create a new user.
     *
     * @param user the user to create
     * @return the created user
     */
    @POST("/users")
    @Retry(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    User createUser(@Body User user);
    
    /**
     * Update an existing user.
     *
     * @param id the user ID
     * @param user the updated user data
     * @return the updated user
     */
    @PUT("/users/{id}")
    User updateUser(
        @PathVariable("id") String id,
        @Body User user
    );
    
    /**
     * Delete a user by ID.
     *
     * @param id the user ID
     * @param authToken the authentication token
     */
    @DELETE("/users/{id}")
    @RateLimit(permits = 10, timeWindowMs = 60000)
    void deleteUser(
        @PathVariable("id") String id,
        @RequestParam("authToken") String authToken
    );
    
    /**
     * Search for users asynchronously.
     *
     * @param query the search query
     * @return a future that completes with the search results
     */
    @GET("/users/search")
    CompletableFuture<List<User>> searchUsersAsync(
        @RequestParam("q") String query
    );
}