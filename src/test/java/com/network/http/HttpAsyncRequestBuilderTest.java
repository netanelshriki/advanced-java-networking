package com.network.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.verify.VerificationTimes;

import com.network.NetworkLib;
import com.network.api.http.HttpAsyncRequestBuilder;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpResponse;
import com.network.exception.NetworkException;

/**
 * Tests for {@link com.network.api.http.HttpAsyncRequestBuilder} and its default implementation.
 */
public class HttpAsyncRequestBuilderTest extends AbstractHttpClientTest {

    @Override
    protected HttpClient createHttpClient() {
        return NetworkLib.createHttpClient(getMockServerBaseUrl());
    }

    @Test
    @DisplayName("Should execute async GET request and get response")
    public void testAsyncGetRequest() throws Exception {
        // Setup mock response
        setupGetRequest("/async-test", 200, "application/json", "{\"result\":\"success\"}");

        // Execute async request
        CompletableFuture<HttpResponse> future = httpClient.requestAsync()
                .get()
                .path("/async-test")
                .execute();

        // Wait for response
        HttpResponse response = future.get(5, TimeUnit.SECONDS);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"result\":\"success\"}", response.getBodyAsString());
        assertEquals("application/json", response.getHeader("Content-Type"));

        // Verify request was made
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/async-test"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should execute async POST request with body")
    public void testAsyncPostRequestWithBody() throws Exception {
        String requestBody = "Async POST request body";
        
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/async-post")
                    .withBody(requestBody)
            )
            .respond(
                response()
                    .withStatusCode(201)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Created via async request")
            );

        // Execute async request
        CompletableFuture<HttpResponse> future = httpClient.requestAsync()
                .post()
                .path("/async-post")
                .body(requestBody.getBytes(StandardCharsets.UTF_8))
                .execute();

        // Wait for response
        HttpResponse response = future.get(5, TimeUnit.SECONDS);

        // Verify response
        assertEquals(201, response.getStatusCode());
        assertEquals("Created via async request", response.getBodyAsString());
        assertEquals("text/plain", response.getHeader("Content-Type"));

        // Verify request was made with correct body
        mockServerClient.verify(
                request()
                    .withMethod("POST")
                    .withPath("/async-post")
                    .withBody(requestBody),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should handle async request timeout")
    public void testAsyncRequestTimeout() {
        // Setup mock response with delay longer than timeout
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/async-timeout")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withDelay(TimeUnit.SECONDS, 3)
                    .withBody("This should time out")
            );

        // Execute async request with short timeout
        CompletableFuture<HttpResponse> future = httpClient.requestAsync()
                .get()
                .path("/async-timeout")
                .timeout(Duration.ofMillis(100))
                .execute();

        // Verify timeout exception
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get(5, TimeUnit.SECONDS);
        });
        
        // The cause should be either directly a timeout exception or a wrapped one
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof NetworkException, "Expected NetworkException but got " + cause.getClass().getName());
        
        Throwable rootCause = cause.getCause();
        assertTrue(rootCause instanceof java.net.http.HttpTimeoutException 
                || rootCause.toString().contains("timeout"), "Expected timeout exception but got " + rootCause);
    }

    @Test
    @DisplayName("Should execute multiple async requests in parallel")
    public void testMultipleAsyncRequests() throws Exception {
        // Setup first mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/async-first")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withDelay(TimeUnit.MILLISECONDS, 200)
                    .withBody("First async response")
            );

        // Setup second mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/async-second")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withDelay(TimeUnit.MILLISECONDS, 100)
                    .withBody("Second async response")
            );

        // Execute first async request
        CompletableFuture<HttpResponse> future1 = httpClient.requestAsync()
                .get()
                .path("/async-first")
                .execute();

        // Execute second async request
        CompletableFuture<HttpResponse> future2 = httpClient.requestAsync()
                .get()
                .path("/async-second")
                .execute();

        // Wait for both responses
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2);
        allFutures.get(5, TimeUnit.SECONDS);

        // Verify responses
        HttpResponse response1 = future1.get();
        HttpResponse response2 = future2.get();

        assertEquals(200, response1.getStatusCode());
        assertEquals("First async response", response1.getBodyAsString());

        assertEquals(200, response2.getStatusCode());
        assertEquals("Second async response", response2.getBodyAsString());

        // Verify both requests were made
        mockServerClient.verify(
                request().withPath("/async-first"),
                VerificationTimes.once()
        );
        
        mockServerClient.verify(
                request().withPath("/async-second"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should chain async request callbacks")
    public void testAsyncRequestCallbacks() throws Exception {
        // Setup mock responses for two sequential requests
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/async-first-step")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":123}")
            );

        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/async-second-step")
                    .withQueryStringParameter("id", "123")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"result\":\"success\",\"id\":123}")
            );

        // Execute first request and chain to second
        CompletableFuture<HttpResponse> chainedFuture = httpClient.requestAsync()
                .get()
                .path("/async-first-step")
                .execute()
                .thenCompose(firstResponse -> {
                    // Extract ID from first response (simplified parsing)
                    String responseBody = firstResponse.getBodyAsString();
                    String id = responseBody.substring(responseBody.indexOf(":") + 1, responseBody.indexOf("}"));
                    
                    // Use ID in second request
                    return httpClient.requestAsync()
                            .get()
                            .path("/async-second-step")
                            .queryParam("id", id)
                            .execute();
                });

        // Wait for final response
        HttpResponse finalResponse = chainedFuture.get(5, TimeUnit.SECONDS);

        // Verify final response
        assertEquals(200, finalResponse.getStatusCode());
        assertEquals("{\"result\":\"success\",\"id\":123}", finalResponse.getBodyAsString());

        // Verify both requests were made
        mockServerClient.verify(
                request().withPath("/async-first-step"),
                VerificationTimes.once()
        );
        
        mockServerClient.verify(
                request()
                    .withPath("/async-second-step")
                    .withQueryStringParameter("id", "123"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should handle server error in async request")
    public void testAsyncServerError() throws Exception {
        // Setup error response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/async-error")
            )
            .respond(
                response()
                    .withStatusCode(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal server error\"}")
            );

        // Execute async request
        CompletableFuture<HttpResponse> future = httpClient.requestAsync()
                .get()
                .path("/async-error")
                .execute();

        // Wait for response
        HttpResponse response = future.get(5, TimeUnit.SECONDS);

        // Verify error response
        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\":\"Internal server error\"}", response.getBodyAsString());
        assertTrue(response.isError());

        // Verify request was made
        mockServerClient.verify(
                request().withPath("/async-error"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should handle connection error in async request")
    public void testAsyncConnectionError() {
        // Create client with invalid port to force connection error
        HttpClient badClient = NetworkLib.createHttpClient("http://localhost:1");
        
        try {
            // Execute async request to non-existent server
            CompletableFuture<HttpResponse> future = badClient.requestAsync()
                    .get()
                    .path("/anything")
                    .execute();

            // Should fail with network error
            ExecutionException exception = assertThrows(ExecutionException.class, () -> {
                future.get(5, TimeUnit.SECONDS);
            });
            
            // Verify exception chain
            assertTrue(exception.getCause() instanceof NetworkException);
        } finally {
            badClient.close();
        }
    }

    @Test
    @DisplayName("Should transform async response with thenApply")
    public void testAsyncResponseTransformation() throws Exception {
        // Setup mock response
        setupGetRequest("/async-transform", 200, "application/json", "{\"value\":42}");

        // Execute async request with transformation
        CompletableFuture<Integer> transformedFuture = httpClient.requestAsync()
                .get()
                .path("/async-transform")
                .execute()
                .thenApply(response -> {
                    // Extract numeric value from JSON (simplified parsing)
                    String body = response.getBodyAsString();
                    String valueStr = body.substring(body.indexOf(":") + 1, body.indexOf("}"));
                    return Integer.parseInt(valueStr);
                });

        // Wait for transformed result
        Integer value = transformedFuture.get(5, TimeUnit.SECONDS);

        // Verify transformed value
        assertEquals(42, value);

        // Verify request was made
        mockServerClient.verify(
                request().withPath("/async-transform"),
                VerificationTimes.once()
        );
    }
}
