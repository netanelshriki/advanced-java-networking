package com.network.annotation.resilience;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import com.network.NetworkLib;
import com.network.annotation.http.GET;
import com.network.annotation.http.HttpClient;
import com.network.annotation.http.PathVariable;
import com.network.exception.NetworkException;

/**
 * Tests for resilience annotations in the networking library.
 */
public class ResilienceAnnotationsTest {

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
    @DisplayName("Should retry failed requests based on @Retry annotation")
    public void testRetryAnnotation() {
        // Setup mock response to fail twice, then succeed
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/retry-test"),
                org.mockserver.matchers.Times.exactly(2)
            )
            .respond(
                response()
                    .withStatusCode(500)
                    .withBody("Server Error")
            );
        
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/retry-test")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"status\":\"success\",\"attempt\":3}")
            );
        
        // Create client proxy
        ResilienceTestClient client = NetworkLib.createClient(ResilienceTestClient.class);
        
        // Execute method with retry
        RetryResponse response = client.testRetry();
        
        // Verify response
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(3, response.getAttempt());
        
        // Verify request was made 3 times (2 failures + 1 success)
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/retry-test"),
                VerificationTimes.exactly(3)
        );
    }
    
    @Test
    @DisplayName("Should implement backoff delay between retries")
    public void testBackoffAnnotation() {
        // Setup mock to track timing between requests
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/backoff-test"),
                org.mockserver.matchers.Times.exactly(3)
            )
            .respond(
                response()
                    .withStatusCode(500)
                    .withBody("Server Error")
            );
        
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/backoff-test")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"status\":\"success\",\"attempt\":4}")
            );
        
        // Create client proxy
        ResilienceTestClient client = NetworkLib.createClient(ResilienceTestClient.class);
        
        // Record start time
        long startTime = System.currentTimeMillis();
        
        // Execute method with backoff
        BackoffResponse response = client.testBackoff();
        
        // Record end time
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify response
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(4, response.getAttempt());
        
        // Verify timing - with exponential backoff should be at least 1500ms (100 + 200 + 400 ms base delays)
        // Adding some buffer for processing time
        assertTrue(duration >= 600, "Expected at least 600ms for backoff, but was: " + duration + "ms");
        
        // Verify request was made 4 times (3 failures + 1 success)
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/backoff-test"),
                VerificationTimes.exactly(4)
        );
    }
    
    @Test
    @DisplayName("Should implement circuit breaker to stop requests after failures")
    public void testCircuitBreakerAnnotation() {
        // Setup mock to always fail
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/circuit-test")
            )
            .respond(
                response()
                    .withStatusCode(500)
                    .withBody("Server Error")
            );
        
        // Create client proxy
        ResilienceTestClient client = NetworkLib.createClient(ResilienceTestClient.class);
        
        // Execute method multiple times to trigger circuit breaker
        Exception exception = null;
        
        // First execution should just fail normally
        try {
            client.testCircuitBreaker("test");
        } catch (Exception e) {
            exception = e;
        }
        
        assertNotNull(exception);
        assertTrue(exception instanceof NetworkException);
        
        // Reset for second attempt
        exception = null;
        
        // Execute again to approach failure threshold
        try {
            client.testCircuitBreaker("test");
        } catch (Exception e) {
            exception = e;
        }
        
        assertNotNull(exception);
        assertTrue(exception instanceof NetworkException);
        
        // Reset for third attempt
        exception = null;
        
        // Execute again to exceed failure threshold - circuit should open
        try {
            client.testCircuitBreaker("test");
        } catch (Exception e) {
            exception = e;
        }
        
        // Circuit should be open now and immediately reject the request
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("circuit") || 
                   exception.getMessage().contains("breaker"), 
                   "Expected circuit breaker exception but got: " + exception.getMessage());
        
        // Verify request was made at most 3 times (the failureThreshold)
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/circuit-test"),
                VerificationTimes.atMost(3)
        );
    }
    
    @Test
    @DisplayName("Should implement rate limiting for requests")
    public void testRateLimitAnnotation() {
        // Setup mock to return success
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/rate-limit-test")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"status\":\"success\"}")
            );
        
        // Create client proxy
        ResilienceTestClient client = NetworkLib.createClient(ResilienceTestClient.class);
        
        // Execute method multiple times quickly
        for (int i = 0; i < 2; i++) { // Should succeed for first 2 calls within 1 second
            RateLimitResponse response = client.testRateLimit();
            assertNotNull(response);
            assertEquals("success", response.getStatus());
        }
        
        // Third call should be rate limited or delayed
        Exception exception = null;
        try {
            client.testRateLimit(); // Third call within 1 second
        } catch (Exception e) {
            exception = e;
        }
        
        // Rate limit should either throw an exception or delay the request
        if (exception != null) {
            assertTrue(exception.getMessage().contains("rate") || 
                       exception.getMessage().contains("limit"), 
                       "Expected rate limit exception but got: " + exception.getMessage());
        } else {
            // If no exception, verify the delay (this is implementation-dependent)
            // Rate limit for this test is 2 requests per second, so a third request
            // without delay would violate this
            long start = System.currentTimeMillis();
            client.testRateLimit(); // Try a fourth call
            long duration = System.currentTimeMillis() - start;
            
            // Should be delayed by around 500ms to meet the rate limit
            assertTrue(duration >= 400, "Expected delay for rate limiting, but was: " + duration + "ms");
        }
        
        // Verify request count based on whether exceptions were thrown
        if (exception != null) {
            mockServerClient.verify(
                    request()
                        .withMethod("GET")
                        .withPath("/api/rate-limit-test"),
                    VerificationTimes.exactly(2) // Only the first 2 succeeded
            );
        } else {
            mockServerClient.verify(
                    request()
                        .withMethod("GET")
                        .withPath("/api/rate-limit-test"),
                    VerificationTimes.exactly(4) // All 4 calls succeeded, but were rate-limited
            );
        }
    }
    
    // Test model classes
    
    public static class RetryResponse {
        private String status;
        private int attempt;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getAttempt() { return attempt; }
        public void setAttempt(int attempt) { this.attempt = attempt; }
    }
    
    public static class BackoffResponse {
        private String status;
        private int attempt;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getAttempt() { return attempt; }
        public void setAttempt(int attempt) { this.attempt = attempt; }
    }
    
    public static class CircuitBreakerResponse {
        private String status;
        private String data;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }
    
    public static class RateLimitResponse {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // Test client interface
    
    @HttpClient(baseUrl = "http://localhost")
    public interface ResilienceTestClient {
        
        @GET("/api/retry-test")
        @Retry(maxAttempts = 3, retryOnStatusCodes = {500, 503})
        RetryResponse testRetry();
        
        @GET("/api/backoff-test")
        @Retry(maxAttempts = 4, retryOnStatusCodes = {500})
        @Backoff(delay = 100, maxDelay = 1000, multiplier = 2.0)
        BackoffResponse testBackoff();
        
        @GET("/api/circuit-test")
        @CircuitBreaker(failureThreshold = 3, resetTimeout = 5000)
        CircuitBreakerResponse testCircuitBreaker(@PathVariable("param") String param);
        
        @GET("/api/rate-limit-test")
        @RateLimit(limit = 2, timeWindow = 1, timeUnit = TimeUnit.SECONDS)
        RateLimitResponse testRateLimit();
    }
}
