package com.network.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import com.network.NetworkLib;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpMethod;
import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequestBuilder;
import com.network.api.http.HttpResponse;
import com.network.exception.NetworkException;

/**
 * Tests for {@link com.network.impl.http.DefaultHttpClient}.
 */
public class DefaultHttpClientTest extends AbstractHttpClientTest {

    @Override
    protected HttpClient createHttpClient() {
        return NetworkLib.createHttpClient(getMockServerBaseUrl());
    }

    @Test
    @DisplayName("Should successfully send GET request and receive response")
    public void testBasicGetRequest() throws Exception {
        // Setup mock response
        setupGetRequest("/api/test", 200, "application/json", "{\"message\":\"Hello, World!\"}");

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/api/test")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"message\":\"Hello, World!\"}", response.getBodyAsString());
        assertEquals("application/json", response.getHeader("Content-Type"));

        // Verify request was made correctly
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/test"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should send request with headers")
    public void testRequestWithHeaders() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/with-headers")
                    .withHeader("X-Custom-Header", "test-value")
                    .withHeader("Authorization", "Bearer token123")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Headers received")
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/api/with-headers")
                .header("X-Custom-Header", "test-value")
                .header("Authorization", "Bearer token123")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Headers received", response.getBodyAsString());

        // Verify request was made with correct headers
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/with-headers")
                    .withHeader("X-Custom-Header", "test-value")
                    .withHeader("Authorization", "Bearer token123"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should send POST request with JSON body")
    public void testPostRequestWithJsonBody() throws Exception {
        String requestJson = "{\"name\":\"John\",\"age\":30}";
        String responseJson = "{\"id\":1,\"name\":\"John\",\"age\":30}";

        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/api/users")
                    .withHeader("Content-Type", "application/json")
                    .withBody(requestJson)
            )
            .respond(
                response()
                    .withStatusCode(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseJson)
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .post()
                .path("/api/users")
                .header("Content-Type", "application/json")
                .body(requestJson.getBytes(StandardCharsets.UTF_8))
                .execute();

        // Verify response
        assertEquals(201, response.getStatusCode());
        assertEquals(responseJson, response.getBodyAsString());

        // Verify request
        mockServerClient.verify(
                request()
                    .withMethod("POST")
                    .withPath("/api/users")
                    .withHeader("Content-Type", "application/json")
                    .withBody(requestJson),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should handle 404 Not Found response")
    public void testHandleNotFoundResponse() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/nonexistent")
            )
            .respond(
                response()
                    .withStatusCode(404)
                    .withBody("Resource not found")
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/api/nonexistent")
                .execute();

        // Verify response
        assertEquals(404, response.getStatusCode());
        assertEquals("Resource not found", response.getBodyAsString());
    }

    @Test
    @DisplayName("Should send request with query parameters")
    public void testRequestWithQueryParameters() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/search")
                    .withQueryStringParameter("q", "test")
                    .withQueryStringParameter("page", "1")
                    .withQueryStringParameter("limit", "10")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"results\":[{\"name\":\"Test Result\"}]}")
            );

        // Execute request
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "test");
        queryParams.put("page", "1");
        queryParams.put("limit", "10");

        HttpResponse response = httpClient.request()
                .get()
                .path("/api/search")
                .queryParams(queryParams)
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"results\":[{\"name\":\"Test Result\"}]}", response.getBodyAsString());

        // Verify request
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/search")
                    .withQueryStringParameter("q", "test")
                    .withQueryStringParameter("page", "1")
                    .withQueryStringParameter("limit", "10"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should send request with timeout")
    public void testRequestWithTimeout() throws Exception {
        // Setup mock response with delay
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/delayed")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withDelay(TimeUnit.MILLISECONDS, 500)
                    .withBody("Delayed response")
            );

        // Execute request with timeout longer than delay
        HttpResponse response = httpClient.request()
                .get()
                .path("/api/delayed")
                .timeout(Duration.ofSeconds(1))
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Delayed response", response.getBodyAsString());
    }

    @Test
    @DisplayName("Should handle timeout exception")
    public void testHandleTimeoutException() {
        // Setup mock response with a longer delay than the timeout
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/long-delay")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withDelay(TimeUnit.SECONDS, 3)
                    .withBody("This should time out")
            );

        // Execute request with short timeout
        NetworkException exception = assertThrows(NetworkException.class, () -> {
            httpClient.request()
                    .get()
                    .path("/api/long-delay")
                    .timeout(Duration.ofMillis(100))
                    .execute();
        });

        // Verify exception
        assertTrue(exception.getMessage().contains("Failed to send HTTP request"));
        assertTrue(exception.getCause() instanceof java.net.http.HttpTimeoutException 
                || exception.getCause().toString().contains("timeout"));
    }

    @Test
    @DisplayName("Should send asynchronous request successfully")
    public void testAsyncRequest() throws Exception {
        // Setup mock response
        setupGetRequest("/api/async", 200, "text/plain", "Async response");

        // Execute async request
        HttpResponse response = httpClient.requestAsync()
                .get()
                .path("/api/async")
                .execute()
                .get();  // Block and get result

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Async response", response.getBodyAsString());
        assertEquals("text/plain", response.getHeader("Content-Type"));

        // Verify request was made correctly
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/api/async"),
                VerificationTimes.once()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"})
    @DisplayName("Should support different HTTP methods")
    public void testDifferentHttpMethods(String methodName) throws Exception {
        HttpMethod method = HttpMethod.valueOf(methodName);
        
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod(methodName)
                    .withPath("/api/method-test")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Method: " + methodName)
            );

        // Create request builder
        HttpRequestBuilder builder = httpClient.request().path("/api/method-test");
        
        // Set method
        HttpRequest request;
        switch (method) {
            case PUT:
                request = builder.put().build();
                break;
            case DELETE:
                request = builder.delete().build();
                break;
            case PATCH:
                request = builder.patch().build();
                break;
            case HEAD:
                request = builder.head().build();
                break;
            case OPTIONS:
                request = builder.options().build();
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + methodName);
        }

        // Execute request
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        
        // HEAD doesn't return body
        if (method != HttpMethod.HEAD) {
            assertEquals("Method: " + methodName, response.getBodyAsString());
        }

        // Verify request
        mockServerClient.verify(
                request()
                    .withMethod(methodName)
                    .withPath("/api/method-test"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should handle binary response data")
    public void testBinaryResponse() throws Exception {
        byte[] binaryData = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/binary")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/octet-stream")
                    .withBody(binaryData)
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/api/binary")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertArrayEquals(binaryData, response.getBody());
        assertEquals("application/octet-stream", response.getHeader("Content-Type"));
    }

    @Test
    @DisplayName("Should handle multiple HTTP requests")
    public void testMultipleRequests() throws Exception {
        // Setup first request/response
        setupGetRequest("/api/first", 200, "application/json", "{\"id\":1}");
        
        // Setup second request/response
        setupGetRequest("/api/second", 200, "application/json", "{\"id\":2}");

        // Execute first request
        HttpResponse response1 = httpClient.request()
                .get()
                .path("/api/first")
                .execute();

        // Execute second request
        HttpResponse response2 = httpClient.request()
                .get()
                .path("/api/second")
                .execute();

        // Verify responses
        assertEquals(200, response1.getStatusCode());
        assertEquals("{\"id\":1}", response1.getBodyAsString());
        
        assertEquals(200, response2.getStatusCode());
        assertEquals("{\"id\":2}", response2.getBodyAsString());

        // Verify requests
        mockServerClient.verify(
                request().withPath("/api/first"),
                VerificationTimes.once()
        );
        
        mockServerClient.verify(
                request().withPath("/api/second"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should handle redirect responses")
    public void testHandleRedirect() throws Exception {
        // Setup redirect
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/redirect")
            )
            .respond(
                response()
                    .withStatusCode(302)
                    .withHeader("Location", getMockServerBaseUrl() + "/api/target")
            );
        
        // Setup target
        setupGetRequest("/api/target", 200, "text/plain", "Redirect target");

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/api/redirect")
                .execute();

        // Verify response (should be from the target)
        assertEquals(200, response.getStatusCode());
        assertEquals("Redirect target", response.getBodyAsString());

        // Verify both requests were made
        mockServerClient.verify(
                request().withPath("/api/redirect"),
                VerificationTimes.once()
        );
        
        mockServerClient.verify(
                request().withPath("/api/target"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should use default headers")
    public void testDefaultHeaders() throws Exception {
        // Create client with default headers
        HttpClient clientWithDefaults = NetworkLib.createHttpClient()
                .withBaseUrl(getMockServerBaseUrl())
                .withDefaultHeader("User-Agent", "Test-Client/1.0")
                .withDefaultHeader("Accept", "application/json")
                .build();
        
        try {
            // Setup mock response that expects headers
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/api/default-headers")
                        .withHeader("User-Agent", "Test-Client/1.0")
                        .withHeader("Accept", "application/json")
                )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withBody("Headers received")
                );

            // Execute request
            HttpResponse response = clientWithDefaults.request()
                    .get()
                    .path("/api/default-headers")
                    .execute();

            // Verify response
            assertEquals(200, response.getStatusCode());
            assertEquals("Headers received", response.getBodyAsString());

            // Verify request was made with correct headers
            mockServerClient.verify(
                    request()
                        .withMethod("GET")
                        .withPath("/api/default-headers")
                        .withHeader("User-Agent", "Test-Client/1.0")
                        .withHeader("Accept", "application/json"),
                    VerificationTimes.once()
            );
        } finally {
            clientWithDefaults.close();
        }
    }

    @Test
    @DisplayName("Should handle server error responses")
    public void testHandleServerError() throws Exception {
        // Setup error response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/error")
            )
            .respond(
                response()
                    .withStatusCode(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal server error\",\"code\":500}")
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/api/error")
                .execute();

        // Verify response
        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\":\"Internal server error\",\"code\":500}", response.getBodyAsString());
        assertEquals("application/json", response.getHeader("Content-Type"));
        assertTrue(response.isError());
    }
}
