package com.network.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.verify.VerificationTimes;

import com.network.NetworkLib;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;

/**
 * Tests for {@link com.network.api.http.HttpResponse} and its default implementation.
 */
public class HttpResponseTest extends AbstractHttpClientTest {

    @Override
    protected HttpClient createHttpClient() {
        return NetworkLib.createHttpClient(getMockServerBaseUrl());
    }

    @Test
    @DisplayName("Should parse response body as string")
    public void testGetBodyAsString() throws Exception {
        // Setup mock response
        String responseBody = "This is a test response body";
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/string-body")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody(responseBody)
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/string-body")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals(responseBody, response.getBodyAsString());
        assertArrayEquals(responseBody.getBytes(StandardCharsets.UTF_8), response.getBody());
    }

    @Test
    @DisplayName("Should handle JSON response")
    public void testJsonResponse() throws Exception {
        // Setup mock response
        String jsonBody = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/json")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonBody)
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/json")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals(jsonBody, response.getBodyAsString());
        assertEquals("application/json", response.getHeader("Content-Type"));
    }

    @Test
    @DisplayName("Should handle empty response body")
    public void testEmptyResponseBody() throws Exception {
        // Setup mock response with empty body
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/empty")
            )
            .respond(
                response()
                    .withStatusCode(204)
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/empty")
                .execute();

        // Verify response
        assertEquals(204, response.getStatusCode());
        
        // Empty body could be null or empty byte array
        if (response.getBody() != null) {
            assertEquals(0, response.getBody().length);
        }
        assertEquals("", response.getBodyAsString());
    }

    @Test
    @DisplayName("Should get response headers")
    public void testGetResponseHeaders() throws Exception {
        // Setup mock response with multiple headers
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/headers")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "text/plain")
                    .withHeader("X-Rate-Limit", "100")
                    .withHeader("X-Rate-Limit-Remaining", "95")
                    .withHeader("Server", "MockServer")
                    .withBody("Headers test")
            );

        // Execute request
        HttpResponse response = httpClient.request()
                .get()
                .path("/headers")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Headers test", response.getBodyAsString());
        
        // Check individual headers
        assertEquals("text/plain", response.getHeader("Content-Type"));
        assertEquals("100", response.getHeader("X-Rate-Limit"));
        assertEquals("95", response.getHeader("X-Rate-Limit-Remaining"));
        assertEquals("MockServer", response.getHeader("Server"));
        
        // Check case insensitivity
        assertEquals("text/plain", response.getHeader("content-type"));
        
        // Check all headers map
        Map<String, String> headers = response.getHeaders();
        assertNotNull(headers);
        assertTrue(headers.containsKey("Content-Type"));
        assertTrue(headers.containsKey("X-Rate-Limit"));
        assertTrue(headers.containsKey("X-Rate-Limit-Remaining"));
        assertTrue(headers.containsKey("Server"));
    }

    @Test
    @DisplayName("Should handle binary response body")
    public void testBinaryResponseBody() throws Exception {
        // Setup binary mock response
        byte[] binaryData = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/binary")
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
                .path("/binary")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("application/octet-stream", response.getHeader("Content-Type"));
        assertArrayEquals(binaryData, response.getBody());
    }

    @Test
    @DisplayName("Should check if response is success")
    public void testIsSuccess() throws Exception {
        // Test various status codes
        int[] successCodes = {200, 201, 202, 203, 204, 205, 206};
        int[] errorCodes = {400, 401, 403, 404, 500, 501, 502, 503};
        
        for (int code : successCodes) {
            // Setup mock response
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/status/" + code)
                )
                .respond(
                    response()
                        .withStatusCode(code)
                        .withBody("Status " + code)
                );

            // Execute request
            HttpResponse response = httpClient.request()
                    .get()
                    .path("/status/" + code)
                    .execute();

            // Verify response
            assertEquals(code, response.getStatusCode());
            assertTrue(response.isSuccess(), "Status code " + code + " should be considered success");
            assertFalse(response.isError(), "Status code " + code + " should not be considered error");
        }
        
        for (int code : errorCodes) {
            // Setup mock response
            mockServerClient
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/status/" + code)
                )
                .respond(
                    response()
                        .withStatusCode(code)
                        .withBody("Error " + code)
                );

            // Execute request
            HttpResponse response = httpClient.request()
                    .get()
                    .path("/status/" + code)
                    .execute();

            // Verify response
            assertEquals(code, response.getStatusCode());
            assertFalse(response.isSuccess(), "Status code " + code + " should not be considered success");
            assertTrue(response.isError(), "Status code " + code + " should be considered error");
        }
    }

    @Test
    @DisplayName("Should get original request from response")
    public void testGetOriginalRequest() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/original-request")
                    .withQueryStringParameter("param", "value")
                    .withHeader("X-Test", "test-value")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Original request test")
            );

        // Create request with specific properties
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test", "test-value");
        
        HttpRequest originalRequest = httpClient.request()
                .get()
                .path("/original-request")
                .queryParam("param", "value")
                .headers(headers)
                .build();

        // Execute request
        HttpResponse response = httpClient.send(originalRequest);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Original request test", response.getBodyAsString());
        
        // Verify original request is preserved in response
        HttpRequest requestFromResponse = response.getRequest();
        assertNotNull(requestFromResponse);
        assertEquals(originalRequest.getMethod(), requestFromResponse.getMethod());
        assertEquals(originalRequest.getUri(), requestFromResponse.getUri());
        assertEquals("test-value", requestFromResponse.getHeaders().get("X-Test"));
    }
}
