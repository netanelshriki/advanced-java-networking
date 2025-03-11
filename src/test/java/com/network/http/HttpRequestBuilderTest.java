package com.network.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.verify.VerificationTimes;

import com.network.NetworkLib;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpMethod;
import com.network.api.http.HttpRequest;
import com.network.api.http.HttpRequestBuilder;
import com.network.api.http.HttpResponse;

/**
 * Tests for {@link com.network.api.http.HttpRequestBuilder} and its default implementation.
 */
public class HttpRequestBuilderTest extends AbstractHttpClientTest {

    @Override
    protected HttpClient createHttpClient() {
        return NetworkLib.createHttpClient(getMockServerBaseUrl());
    }

    @Test
    @DisplayName("Should build GET request with path")
    public void testBuildGetRequestWithPath() throws Exception {
        // Setup mock response
        setupGetRequest("/test", 200, "text/plain", "Test response");

        // Create request using builder
        HttpRequest request = httpClient.request()
                .get()
                .path("/test")
                .build();

        // Verify request properties
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(URI.create(getMockServerBaseUrl() + "/test"), request.getUri());
        assertNull(request.getBody());

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Test response", response.getBodyAsString());

        // Verify request was sent
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/test"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should build POST request with body")
    public void testBuildPostRequestWithBody() throws Exception {
        String requestBody = "This is the request body";
        
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/post-test")
                    .withBody(requestBody)
            )
            .respond(
                response()
                    .withStatusCode(201)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Created")
            );

        // Create request using builder
        HttpRequest request = httpClient.request()
                .post()
                .path("/post-test")
                .body(requestBody.getBytes(StandardCharsets.UTF_8))
                .build();

        // Verify request properties
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals(URI.create(getMockServerBaseUrl() + "/post-test"), request.getUri());
        assertNotNull(request.getBody());

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(201, response.getStatusCode());
        assertEquals("Created", response.getBodyAsString());

        // Verify request was sent with correct body
        mockServerClient.verify(
                request()
                    .withMethod("POST")
                    .withPath("/post-test")
                    .withBody(requestBody),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should build request with headers")
    public void testBuildRequestWithHeaders() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/header-test")
                    .withHeader("X-Test-Header", "test-value")
                    .withHeader("Authorization", "Bearer token123")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Headers received")
            );

        // Create request using builder
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test-Header", "test-value");
        headers.put("Authorization", "Bearer token123");

        HttpRequest request = httpClient.request()
                .get()
                .path("/header-test")
                .headers(headers)
                .build();

        // Verify request properties
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(URI.create(getMockServerBaseUrl() + "/header-test"), request.getUri());
        assertEquals("test-value", request.getHeaders().get("X-Test-Header"));
        assertEquals("Bearer token123", request.getHeaders().get("Authorization"));

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Headers received", response.getBodyAsString());

        // Verify request was sent with correct headers
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/header-test")
                    .withHeader("X-Test-Header", "test-value")
                    .withHeader("Authorization", "Bearer token123"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should build request with individual header")
    public void testBuildRequestWithIndividualHeader() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/single-header-test")
                    .withHeader("X-Single-Header", "single-value")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Single header received")
            );

        // Create request using builder
        HttpRequest request = httpClient.request()
                .get()
                .path("/single-header-test")
                .header("X-Single-Header", "single-value")
                .build();

        // Verify request properties
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(URI.create(getMockServerBaseUrl() + "/single-header-test"), request.getUri());
        assertEquals("single-value", request.getHeaders().get("X-Single-Header"));

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Single header received", response.getBodyAsString());

        // Verify request was sent with correct header
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/single-header-test")
                    .withHeader("X-Single-Header", "single-value"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should build request with query parameters")
    public void testBuildRequestWithQueryParameters() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/query-test")
                    .withQueryStringParameter("param1", "value1")
                    .withQueryStringParameter("param2", "value2")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Query params received")
            );

        // Create request using builder
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("param1", "value1");
        queryParams.put("param2", "value2");

        HttpRequest request = httpClient.request()
                .get()
                .path("/query-test")
                .queryParams(queryParams)
                .build();

        // Verify request properties
        assertEquals(HttpMethod.GET, request.getMethod());
        assertTrue(request.getUri().toString().contains("param1=value1"));
        assertTrue(request.getUri().toString().contains("param2=value2"));

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Query params received", response.getBodyAsString());

        // Verify request was sent with correct query parameters
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/query-test")
                    .withQueryStringParameter("param1", "value1")
                    .withQueryStringParameter("param2", "value2"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should build request with individual query parameter")
    public void testBuildRequestWithIndividualQueryParameter() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/single-query-test")
                    .withQueryStringParameter("singleParam", "singleValue")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Single query param received")
            );

        // Create request using builder
        HttpRequest request = httpClient.request()
                .get()
                .path("/single-query-test")
                .queryParam("singleParam", "singleValue")
                .build();

        // Verify request properties
        assertEquals(HttpMethod.GET, request.getMethod());
        assertTrue(request.getUri().toString().contains("singleParam=singleValue"));

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Single query param received", response.getBodyAsString());

        // Verify request was sent with correct query parameter
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/single-query-test")
                    .withQueryStringParameter("singleParam", "singleValue"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should build request with timeout")
    public void testBuildRequestWithTimeout() throws Exception {
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/timeout-test")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("Timeout test")
            );

        // Create request using builder
        Duration timeout = Duration.ofSeconds(2);
        HttpRequest request = httpClient.request()
                .get()
                .path("/timeout-test")
                .timeout(timeout)
                .build();

        // Verify request properties
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(timeout, request.getTimeout());

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Timeout test", response.getBodyAsString());
    }

    @Test
    @DisplayName("Should build PUT request with body")
    public void testBuildPutRequestWithBody() throws Exception {
        String requestBody = "PUT request body";
        
        // Setup mock response
        mockServerClient
            .when(
                request()
                    .withMethod("PUT")
                    .withPath("/put-test")
                    .withBody(requestBody)
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("PUT successful")
            );

        // Create request using builder
        HttpRequest request = httpClient.request()
                .put()
                .path("/put-test")
                .body(requestBody.getBytes(StandardCharsets.UTF_8))
                .build();

        // Verify request properties
        assertEquals(HttpMethod.PUT, request.getMethod());
        assertEquals(URI.create(getMockServerBaseUrl() + "/put-test"), request.getUri());
        assertNotNull(request.getBody());

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("PUT successful", response.getBodyAsString());

        // Verify request was sent with correct body
        mockServerClient.verify(
                request()
                    .withMethod("PUT")
                    .withPath("/put-test")
                    .withBody(requestBody),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should create request with direct URL")
    public void testCreateRequestWithDirectUrl() throws Exception {
        // Setup mock response
        setupGetRequest("/direct-url", 200, "text/plain", "Direct URL test");

        String fullUrl = getMockServerBaseUrl() + "/direct-url";
        
        // Create request using builder with direct URL
        HttpRequest request = httpClient.request()
                .get()
                .url(fullUrl)
                .build();

        // Verify request properties
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(URI.create(fullUrl), request.getUri());

        // Execute the request to verify it works
        HttpResponse response = httpClient.send(request);

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Direct URL test", response.getBodyAsString());

        // Verify request was sent
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/direct-url"),
                VerificationTimes.once()
        );
    }

    @Test
    @DisplayName("Should create a simple request and execute it directly")
    public void testSimpleRequestAndExecute() throws Exception {
        // Setup mock response
        setupGetRequest("/simple", 200, "text/plain", "Simple request");

        // Create and execute request in one chain
        HttpResponse response = httpClient.request()
                .get()
                .path("/simple")
                .execute();

        // Verify response
        assertEquals(200, response.getStatusCode());
        assertEquals("Simple request", response.getBodyAsString());

        // Verify request was sent
        mockServerClient.verify(
                request()
                    .withMethod("GET")
                    .withPath("/simple"),
                VerificationTimes.once()
        );
    }
}
