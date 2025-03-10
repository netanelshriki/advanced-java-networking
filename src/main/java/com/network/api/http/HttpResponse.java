package com.network.api.http;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an HTTP response.
 * 
 * <p>This interface defines the properties of an HTTP response, such as
 * the status code, headers, and body.
 * 
 * @param <T> the type of the deserialized body, or Void if not deserialized
 */
public interface HttpResponse<T> {
    
    /**
     * Gets the status code of this response.
     * 
     * @return the status code
     */
    int getStatusCode();
    
    /**
     * Gets the status message of this response.
     * 
     * @return the status message
     */
    String getStatusMessage();
    
    /**
     * Gets the URI of the request that produced this response.
     * 
     * @return the request URI
     */
    URI getRequestUri();
    
    /**
     * Gets all headers of this response.
     * 
     * @return the headers
     */
    Map<String, List<String>> getHeaders();
    
    /**
     * Gets the value of the specified header.
     * 
     * <p>If the header has multiple values, the first value is returned.
     * 
     * @param name the header name (case-insensitive)
     * @return an Optional containing the header value, or empty if not found
     */
    Optional<String> getHeader(String name);
    
    /**
     * Gets all values of the specified header.
     * 
     * @param name the header name (case-insensitive)
     * @return a list of header values, or an empty list if not found
     */
    List<String> getHeaderValues(String name);
    
    /**
     * Checks if the response has a body.
     * 
     * @return true if the response has a body, false otherwise
     */
    boolean hasBody();
    
    /**
     * Gets the body of this response as bytes.
     * 
     * @return the body as bytes, or an empty array if no body
     */
    byte[] getBodyAsBytes();
    
    /**
     * Gets the body of this response as a string.
     * 
     * <p>The encoding is determined from the Content-Type header if present,
     * otherwise UTF-8 is used.
     * 
     * @return the body as a string, or an empty string if no body
     */
    String getBodyAsString();
    
    /**
     * Gets the deserialized body of this response.
     * 
     * <p>If this response was not configured for deserialization,
     * or if the body could not be deserialized, this method will return null.
     * 
     * @return the deserialized body, or null if not deserialized
     */
    T getBody();
    
    /**
     * Gets the body of this response deserialized as the specified type.
     * 
     * <p>This method deserializes the body using the response's content type
     * and the client's serializer.
     * 
     * @param <U> the type to deserialize to
     * @param type the class of the type to deserialize to
     * @return the deserialized body
     * @throws IllegalStateException if the body cannot be deserialized
     */
    <U> U getBodyAs(Class<U> type);
    
    /**
     * Gets the content type of this response.
     * 
     * @return an Optional containing the content type, or empty if not set
     */
    Optional<String> getContentType();
    
    /**
     * Gets the time it took to receive the response.
     * 
     * @return the response time
     */
    Duration getResponseTime();
    
    /**
     * Gets the time of the first byte received.
     * 
     * <p>This can be used to calculate time to first byte.
     * 
     * @return the time to first byte
     */
    Duration getTimeToFirstByte();
    
    /**
     * Gets the request that produced this response.
     * 
     * @return the request
     */
    HttpRequest getRequest();
    
    /**
     * Checks if this response has a successful status code (2xx).
     * 
     * @return true if the status code is in the 2xx range, false otherwise
     */
    default boolean isSuccessful() {
        int statusCode = getStatusCode();
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Checks if this response has a redirection status code (3xx).
     * 
     * @return true if the status code is in the 3xx range, false otherwise
     */
    default boolean isRedirection() {
        int statusCode = getStatusCode();
        return statusCode >= 300 && statusCode < 400;
    }
    
    /**
     * Checks if this response has a client error status code (4xx).
     * 
     * @return true if the status code is in the 4xx range, false otherwise
     */
    default boolean isClientError() {
        int statusCode = getStatusCode();
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * Checks if this response has a server error status code (5xx).
     * 
     * @return true if the status code is in the 5xx range, false otherwise
     */
    default boolean isServerError() {
        int statusCode = getStatusCode();
        return statusCode >= 500 && statusCode < 600;
    }
    
    /**
     * Checks if this response has an error status code (4xx or 5xx).
     * 
     * @return true if the status code is in the 4xx or 5xx range, false otherwise
     */
    default boolean isError() {
        return isClientError() || isServerError();
    }
    
    /**
     * Gets a raw, untyped version of this response.
     * 
     * @return an untyped version of this response
     */
    HttpResponse<Void> asUntyped();
    
    /**
     * Throws an exception if this response has an error status code.
     * 
     * @return this response
     * @throws HttpResponseException if the status code is in the 4xx or 5xx range
     */
    HttpResponse<T> throwOnError() throws HttpResponseException;
    
    /**
     * Throws an exception if this response has a status code that does not match the expected code.
     * 
     * @param expectedStatusCode the expected status code
     * @return this response
     * @throws HttpResponseException if the status code does not match the expected code
     */
    HttpResponse<T> assertStatusCode(int expectedStatusCode) throws HttpResponseException;
    
    /**
     * Creates a typed view of this response with the body deserialized to the specified type.
     * 
     * @param <U> the type to deserialize to
     * @param type the class of the type to deserialize to
     * @return a typed view of this response
     * @throws IllegalStateException if the body cannot be deserialized
     */
    <U> HttpResponse<U> as(Class<U> type);
}