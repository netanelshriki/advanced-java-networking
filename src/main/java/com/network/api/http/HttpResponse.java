package com.network.api.http;

import java.net.URI;
import java.util.Map;

/**
 * HTTP response returned from an {@link HttpClient}.
 * <p>
 * This interface provides methods for accessing the response status, body, headers,
 * and other information.
 * </p>
 * 
 * @param <T> the type of the response body
 */
public interface HttpResponse<T> {

    /**
     * Gets the status code of the response.
     * 
     * @return the status code
     */
    int getStatusCode();
    
    /**
     * Gets the status message of the response.
     * 
     * @return the status message
     */
    String getStatusMessage();
    
    /**
     * Gets the request that resulted in this response.
     * 
     * @return the request
     */
    HttpRequest getRequest();
    
    /**
     * Gets the URI that was used for the request.
     * 
     * @return the URI
     */
    URI getUri();
    
    /**
     * Gets the URI that was used for the request.
     * 
     * @return the URI
     */
    URI getRequestUri();
    
    /**
     * Checks if the response status code indicates success (2xx).
     * 
     * @return true if the response is successful, false otherwise
     */
    boolean isSuccess();
    
    /**
     * Checks if the response is successful.
     * 
     * @return true if the response is successful, false otherwise
     */
    boolean isSuccessful();
    
    /**
     * Checks if the response status code indicates an error (4xx or 5xx).
     * 
     * @return true if the response is an error, false otherwise
     */
    boolean isError();
    
    /**
     * Gets the body of the response as a byte array.
     * 
     * @return the body, or null if there is no body
     */
    byte[] getBody();
    
    /**
     * Gets the typed body of the response.
     * 
     * @return the typed body
     */
    T getBodyAs();
    
    /**
     * Gets the body of the response as a string, using UTF-8 encoding.
     * 
     * @return the body as a string, or empty string if there is no body
     */
    String getBodyAsString();
    
    /**
     * Deserializes the body to the specified type.
     * 
     * @param <R> the target type
     * @param type the class of the target type
     * @return the deserialized body
     */
    <R> R getBodyAs(Class<R> type);
    
    /**
     * Gets a header value.
     * 
     * @param name the header name
     * @return the header value, or empty if not present
     */
    String getHeader(String name);
    
    /**
     * Gets all headers.
     * 
     * @return the headers
     */
    Map<String, String> getHeaders();
    
    /**
     * Gets the Content-Type header.
     * 
     * @return the content type, or empty if not present
     */
    String getContentType();
    
    /**
     * Throws an exception if the response is not successful.
     * 
     * @return this response
     * @throws HttpResponseException if the response is not successful
     */
    HttpResponse<T> throwIfNotSuccessful() throws HttpResponseException;
}