package com.network.api.http;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.network.api.http.HttpRequest.HttpMethod;
import com.network.serialization.Serializer;

/**
 * Specialized builder for asynchronous HTTP requests that deserialize responses to a specific type.
 * 
 * <p>This interface extends {@link HttpAsyncRequestBuilder} to provide type-safe response handling.
 * 
 * @param <T> the type to deserialize responses to
 */
public interface TypedHttpAsyncRequestBuilder<T> {
    
    /**
     * Sets the HTTP method for the request.
     * 
     * @param method the HTTP method
     * @return this builder
     * @throws IllegalArgumentException if method is null
     */
    TypedHttpAsyncRequestBuilder<T> method(HttpMethod method);
    
    /**
     * Sets the URL for the request.
     * 
     * @param url the URL
     * @return this builder
     * @throws IllegalArgumentException if url is null
     */
    TypedHttpAsyncRequestBuilder<T> url(URL url);
    
    /**
     * Sets the URI for the request.
     * 
     * @param uri the URI
     * @return this builder
     * @throws IllegalArgumentException if uri is null
     */
    TypedHttpAsyncRequestBuilder<T> uri(URI uri);
    
    /**
     * Sets the URL for the request as a string.
     * 
     * @param url the URL as a string
     * @return this builder
     * @throws IllegalArgumentException if url is null or not a valid URL
     */
    TypedHttpAsyncRequestBuilder<T> url(String url);
    
    /**
     * Sets the path for the request.
     * 
     * <p>The path is resolved against the client's base URL.
     * 
     * @param path the path
     * @return this builder
     * @throws IllegalArgumentException if path is null
     */
    TypedHttpAsyncRequestBuilder<T> path(String path);
    
    /**
     * Adds a query parameter to the request.
     * 
     * @param name the parameter name
     * @param value the parameter value
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    TypedHttpAsyncRequestBuilder<T> queryParam(String name, String value);
    
    /**
     * Adds query parameters to the request.
     * 
     * @param params the parameters
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    TypedHttpAsyncRequestBuilder<T> queryParams(Map<String, String> params);
    
    /**
     * Adds a path parameter to the request.
     * 
     * <p>Path parameters are replaced in the URL path, for example
     * given the path "/users/{id}", the path parameter "id" with value "123"
     * would result in the path "/users/123".
     * 
     * @param name the parameter name
     * @param value the parameter value
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    TypedHttpAsyncRequestBuilder<T> pathParam(String name, String value);
    
    /**
     * Adds path parameters to the request.
     * 
     * @param params the parameters
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    TypedHttpAsyncRequestBuilder<T> pathParams(Map<String, String> params);
    
    /**
     * Adds a header to the request.
     * 
     * @param name the header name
     * @param value the header value
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    TypedHttpAsyncRequestBuilder<T> header(String name, String value);
    
    /**
     * Adds headers to the request.
     * 
     * @param headers the headers
     * @return this builder
     * @throws IllegalArgumentException if headers is null
     */
    TypedHttpAsyncRequestBuilder<T> headers(Map<String, String> headers);
    
    /**
     * Sets the content type header for the request.
     * 
     * @param contentType the content type
     * @return this builder
     * @throws IllegalArgumentException if contentType is null
     */
    TypedHttpAsyncRequestBuilder<T> contentType(String contentType);
    
    /**
     * Sets the accept header for the request.
     * 
     * @param accept the accept header value
     * @return this builder
     * @throws IllegalArgumentException if accept is null
     */
    TypedHttpAsyncRequestBuilder<T> accept(String accept);
    
    /**
     * Sets the body of the request as a string.
     * 
     * @param body the body
     * @return this builder
     */
    TypedHttpAsyncRequestBuilder<T> body(String body);
    
    /**
     * Sets the body of the request as bytes.
     * 
     * @param body the body
     * @return this builder
     */
    TypedHttpAsyncRequestBuilder<T> body(byte[] body);
    
    /**
     * Sets the body of the request as an object.
     * 
     * <p>The object is serialized using the client's serializer.
     * 
     * @param body the body
     * @return this builder
     */
    TypedHttpAsyncRequestBuilder<T> body(Object body);
    
    /**
     * Sets the body of the request as form parameters.
     * 
     * <p>The content type is set to "application/x-www-form-urlencoded".
     * 
     * @param params the form parameters
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    TypedHttpAsyncRequestBuilder<T> formParams(Map<String, String> params);
    
    /**
     * Sets the serializer to use for this request.
     * 
     * <p>If not set, the client's default serializer is used.
     * 
     * @param serializer the serializer
     * @return this builder
     * @throws IllegalArgumentException if serializer is null
     */
    TypedHttpAsyncRequestBuilder<T> serializer(Serializer serializer);
    
    /**
     * Sets the follow redirects flag.
     * 
     * @param followRedirects true to follow redirects, false to not
     * @return this builder
     */
    TypedHttpAsyncRequestBuilder<T> followRedirects(boolean followRedirects);
    
    /**
     * Sets the timeout for this request.
     * 
     * <p>If not set, the client's timeout is used.
     * 
     * @param timeoutMillis the timeout in milliseconds
     * @return this builder
     * @throws IllegalArgumentException if timeoutMillis is negative
     */
    TypedHttpAsyncRequestBuilder<T> timeout(int timeoutMillis);
    
    /**
     * Sets an attribute in the request context.
     * 
     * @param key the attribute key
     * @param value the attribute value
     * @return this builder
     * @throws IllegalArgumentException if key is null
     */
    TypedHttpAsyncRequestBuilder<T> attribute(String key, Object value);
    
    /**
     * Sets the HTTP GET method for the request.
     * 
     * @return this builder
     */
    default TypedHttpAsyncRequestBuilder<T> get() {
        return method(HttpMethod.GET);
    }
    
    /**
     * Sets the HTTP POST method for the request.
     * 
     * @return this builder
     */
    default TypedHttpAsyncRequestBuilder<T> post() {
        return method(HttpMethod.POST);
    }
    
    /**
     * Sets the HTTP PUT method for the request.
     * 
     * @return this builder
     */
    default TypedHttpAsyncRequestBuilder<T> put() {
        return method(HttpMethod.PUT);
    }
    
    /**
     * Sets the HTTP DELETE method for the request.
     * 
     * @return this builder
     */
    default TypedHttpAsyncRequestBuilder<T> delete() {
        return method(HttpMethod.DELETE);
    }
    
    /**
     * Sets the HTTP PATCH method for the request.
     * 
     * @return this builder
     */
    default TypedHttpAsyncRequestBuilder<T> patch() {
        return method(HttpMethod.PATCH);
    }
    
    /**
     * Sets the HTTP HEAD method for the request.
     * 
     * @return this builder
     */
    default TypedHttpAsyncRequestBuilder<T> head() {
        return method(HttpMethod.HEAD);
    }
    
    /**
     * Sets the HTTP OPTIONS method for the request.
     * 
     * @return this builder
     */
    default TypedHttpAsyncRequestBuilder<T> options() {
        return method(HttpMethod.OPTIONS);
    }
    
    /**
     * Executes the request asynchronously and returns a CompletableFuture that completes
     * with the typed response.
     * 
     * @return a CompletableFuture that completes with the typed response
     * @throws IllegalStateException if the builder is not properly configured
     */
    CompletableFuture<HttpResponse<T>> execute();
    
    /**
     * Builds the request without executing it.
     * 
     * @return the built request
     * @throws IllegalStateException if the builder is not properly configured
     */
    HttpRequest build();
}