package com.network.api.http;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an HTTP request.
 * 
 * <p>This interface defines the properties of an HTTP request, such as
 * the method, URI, headers, and body.
 */
public interface HttpRequest {
    
    /**
     * Gets the HTTP method of this request.
     * 
     * @return the HTTP method
     */
    HttpMethod getMethod();
    
    /**
     * Gets the URI of this request.
     * 
     * @return the URI
     */
    URI getUri();
    
    /**
     * Gets all headers of this request.
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
     * Checks if the request has a body.
     * 
     * @return true if the request has a body, false otherwise
     */
    boolean hasBody();
    
    /**
     * Gets the body of this request as bytes.
     * 
     * @return the body as bytes, or an empty array if no body
     */
    byte[] getBodyAsBytes();
    
    /**
     * Gets the body of this request as a string.
     * 
     * <p>The encoding is determined from the Content-Type header if present,
     * otherwise UTF-8 is used.
     * 
     * @return the body as a string, or an empty string if no body
     */
    String getBodyAsString();
    
    /**
     * Gets the body of this request as an object.
     * 
     * <p>The body is deserialized using the request's content type
     * and the client's serializer.
     * 
     * @param <T> the type to deserialize to
     * @param type the class of the type to deserialize to
     * @return the deserialized body
     * @throws IllegalStateException if the body cannot be deserialized
     */
    <T> T getBodyAs(Class<T> type);
    
    /**
     * Gets the content type of this request.
     * 
     * @return an Optional containing the content type, or empty if not set
     */
    Optional<String> getContentType();
    
    /**
     * Gets the query parameters of this request.
     * 
     * @return the query parameters
     */
    Map<String, List<String>> getQueryParams();
    
    /**
     * Gets the value of the specified query parameter.
     * 
     * <p>If the parameter has multiple values, the first value is returned.
     * 
     * @param name the parameter name
     * @return an Optional containing the parameter value, or empty if not found
     */
    Optional<String> getQueryParam(String name);
    
    /**
     * Gets all values of the specified query parameter.
     * 
     * @param name the parameter name
     * @return a list of parameter values, or an empty list if not found
     */
    List<String> getQueryParamValues(String name);
    
    /**
     * Gets the path parameters of this request.
     * 
     * <p>Path parameters are variables in the URL path, such as {id} in /users/{id}.
     * 
     * @return the path parameters
     */
    Map<String, String> getPathParams();
    
    /**
     * Gets the value of the specified path parameter.
     * 
     * @param name the parameter name
     * @return an Optional containing the parameter value, or empty if not found
     */
    Optional<String> getPathParam(String name);
    
    /**
     * Gets the follow redirects flag.
     * 
     * @return true if redirects should be followed, false otherwise
     */
    boolean isFollowRedirects();
    
    /**
     * Gets the timeout for this request.
     * 
     * <p>If not set, the client's timeout is used.
     * 
     * @return an Optional containing the timeout in milliseconds, or empty if not set
     */
    Optional<Integer> getTimeout();
    
    /**
     * Gets the context associated with this request.
     * 
     * <p>The context can be used to store arbitrary data associated with the request.
     * 
     * @return the request context
     */
    HttpRequestContext getContext();
    
    /**
     * HTTP methods.
     */
    enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE
    }
}