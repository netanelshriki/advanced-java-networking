package com.network.examples;

import com.network.NetworkLib;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.api.http.HttpRequestContext;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Example of using the HTTP client.
 */
public class HttpClientExample {

    public static void main(String[] args) {
        // Create HTTP client
        HttpClient client = NetworkLib.createHttpClient()
                .withBaseUrl("https://jsonplaceholder.typicode.com")
                .withTimeout(Duration.ofSeconds(30))
                .withHeader("User-Agent", "JavaNetworkLib/1.0")
                .withMiddleware(new LoggingMiddleware())
                .build();
        
        // Make synchronous request
        try {
            System.out.println("Making synchronous request...");
            
            HttpResponse response = client.request()
                    .path("/posts/1")
                    .get()
                    .execute();
            
            if (response.isSuccessful()) {
                System.out.println("Response: " + response.getBodyAsString());
            } else {
                System.err.println("Error: " + response.getStatusCode() + " " + response.getStatusMessage());
            }
            
            // Make asynchronous request
            System.out.println("\nMaking asynchronous request...");
            
            CompletableFuture<HttpResponse> future = client.requestAsync()
                    .path("/users/1")
                    .get()
                    .execute();
            
            future.thenAccept(userResponse -> {
                if (userResponse.isSuccessful()) {
                    System.out.println("User response: " + userResponse.getBodyAsString());
                } else {
                    System.err.println("Error: " + userResponse.getStatusCode() + " " + userResponse.getStatusMessage());
                }
            }).join(); // Wait for completion in this example
            
            // Make POST request
            System.out.println("\nMaking POST request...");
            
            String postData = "{\"title\":\"foo\",\"body\":\"bar\",\"userId\":1}";
            
            HttpResponse postResponse = client.request()
                    .path("/posts")
                    .contentType("application/json")
                    .body(postData)
                    .post()
                    .execute();
            
            if (postResponse.isSuccessful()) {
                System.out.println("POST response: " + postResponse.getBodyAsString());
            } else {
                System.err.println("Error: " + postResponse.getStatusCode() + " " + postResponse.getStatusMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Close client
            client.close();
        }
    }
    
    /**
     * Simple middleware for logging HTTP requests and responses.
     */
    private static class LoggingMiddleware implements HttpMiddleware {
        
        @Override
        public void beforeRequest(HttpRequestContext context) {
            System.out.println("Sending request: " + context.getRequest().getMethod() + " " + context.getRequest().getUri());
        }
        
        @Override
        public void afterResponse(HttpRequestContext context, HttpResponse response) {
            System.out.println("Received response: " + response.getStatusCode() + " " + response.getStatusMessage());
        }
    }
}
