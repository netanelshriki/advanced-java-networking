package com.network.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.network.api.http.HttpClient;

/**
 * Base class for HTTP client tests using MockServer.
 */
public abstract class AbstractHttpClientTest {
    
    protected static ClientAndServer mockServer;
    protected static MockServerClient mockServerClient;
    protected static int mockServerPort;
    protected HttpClient httpClient;
    
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
    public void setUp() {
        // Reset expectations before each test
        mockServerClient.reset();
        
        // Create a new HTTP client for each test
        httpClient = createHttpClient();
    }
    
    @AfterEach
    public void tearDown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
    
    /**
     * Creates an HTTP client for testing.
     * 
     * @return the HTTP client
     */
    protected abstract HttpClient createHttpClient();
    
    /**
     * Sets up a basic GET request expectation.
     * 
     * @param path        the request path
     * @param statusCode  the response status code
     * @param contentType the response content type
     * @param body        the response body
     */
    protected void setupGetRequest(String path, int statusCode, String contentType, String body) {
        mockServerClient
            .when(
                HttpRequest.request()
                    .withMethod("GET")
                    .withPath(path)
            )
            .respond(
                HttpResponse.response()
                    .withStatusCode(statusCode)
                    .withHeader("Content-Type", contentType)
                    .withBody(body)
            );
    }
    
    /**
     * Sets up a basic POST request expectation.
     * 
     * @param path        the request path
     * @param requestBody the expected request body
     * @param statusCode  the response status code
     * @param contentType the response content type
     * @param responseBody the response body
     */
    protected void setupPostRequest(String path, String requestBody, int statusCode, String contentType, String responseBody) {
        mockServerClient
            .when(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath(path)
                    .withBody(requestBody)
            )
            .respond(
                HttpResponse.response()
                    .withStatusCode(statusCode)
                    .withHeader("Content-Type", contentType)
                    .withBody(responseBody)
            );
    }
    
    /**
     * Gets the base URL for the mock server.
     * 
     * @return the mock server base URL
     */
    protected String getMockServerBaseUrl() {
        return "http://localhost:" + mockServerPort;
    }
}
