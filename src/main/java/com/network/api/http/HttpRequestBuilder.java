package com.network.api.http;

import java.net.URI;
import java.net.URL;
import java.util.Map;

import com.network.api.http.HttpRequest.HttpMethod;
import com.network.exception.NetworkException;
import com.network.serialization.Serializer;

/**
 * Builder for HTTP requests.
 * 
 * <p>This interface defines the methods for building HTTP requests
 * in a fluent manner. It follows the builder pattern to provide
 * a convenient way to construct requests.
 */
public interface HttpRequestBuilder {
    
    /**
     * Sets the HTTP method for the request.
     * 
     * @param method the HTTP method
     * @return this builder
     * @throws IllegalArgumentException if method is null
     */
    HttpRequestBuilder method(HttpMethod method);
    
    /**
     * Sets the URL for the request.
     * 
     * @param url the URL
     * @return this builder
     * @throws IllegalArgumentException if url is null
     */
    HttpRequestBuilder url(URL url);
    
    /**
     * Sets the URI for the request.
     * 
     * @param uri the URI
     * @return this builder
     * @throws IllegalArgumentException if uri is null
     */
    HttpRequestBuilder uri(URI uri);
    
    /**
     * Sets the URL for the request as a string.
     * 
     * @param url the URL as a string
     * @return this builder
     * @throws IllegalArgumentException if url is null or not a valid URL
     */
    HttpRequestBuilder url(String url);
    
    /**
     * Sets the path for the request.
     * 
     * <p>The path is resolved against the client's base URL.
     * 
     * @param path the path
     * @return this builder
     * @throws IllegalArgumentException if path is null
     */
    HttpRequestBuilder path(String path);
    
    /**
     * Adds a query parameter to the request.
     * 
     * @param name the parameter name
     * @param value the parameter value
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    HttpRequestBuilder queryParam(String name, String value);
    
    /**
     * Adds query parameters to the request.
     * 
     * @param params the parameters
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    HttpRequestBuilder queryParams(Map<String, String> params);
    
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
    HttpRequestBuilder pathParam(String name, String value);
    
    /**
     * Adds path parameters to the request.
     * 
     * @param params the parameters
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    HttpRequestBuilder pathParams(Map<String, String> params);
    
    /**
     * Adds a header to the request.
     * 
     * @param name the header name
     * @param value the header value
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    HttpRequestBuilder header(String name, String value);
    
    /**
     * Adds headers to the request.
     * 
     * @param headers the headers
     * @return this builder
     * @throws IllegalArgumentException if headers is null
     */
    HttpRequestBuilder headers(Map<String, String> headers);
    
    /**
     * Sets the content type header for the request.
     * 
     * @param contentType the content type
     * @return this builder
     * @throws IllegalArgumentException if contentType is null
     */
    HttpRequestBuilder contentType(String contentType);
    
    /**
     * Sets the accept header for the request.
     * 
     * @param accept the accept header value
     * @return this builder
     * @throws IllegalArgumentException if accept is null
     */
    HttpRequestBuilder accept(String accept);
    
    /**
     * Sets the body of the request as a string.
     * 
     * @param body the body
     * @return this builder
     */
    HttpRequestBuilder body(String body);
    
    /**
     * Sets the body of the request as bytes.
     * 
     * @param body the body
     * @return this builder
     */
    HttpRequestBuilder body(byte[] body);
    
    /**
     * Sets the body of the request as an object.
     * 
     * <p>The object is serialized using the client's serializer.
     * 
     * @param body the body
     * @return this builder
     */
    HttpRequestBuilder body(Object body);
    
    /**
     * Sets the body of the request as form parameters.
     * 
     * <p>The content type is set to "application/x-www-form-urlencoded".
     * 
     * @param params the form parameters
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    HttpRequestBuilder formParams(Map<String, String> params);
    
    /**
     * Sets the serializer to use for this request.
     * 
     * <p>If not set, the client's default serializer is used.
     * 
     * @param serializer the serializer
     * @return this builder
     * @throws IllegalArgumentException if serializer is null
     */
    HttpRequestBuilder serializer(Serializer serializer);
    
    /**
     * Sets the follow redirects flag.
     * 
     * @param followRedirects true to follow redirects, false to not
     * @return this builder
     */
    HttpRequestBuilder followRedirects(boolean followRedirects);
    
    /**
     * Sets the timeout for this request.
     * 
     * <p>If not set, the client's timeout is used.
     * 
     * @param timeoutMillis the timeout in milliseconds
     * @return this builder
     * @throws IllegalArgumentException if timeoutMillis is negative
     */
    HttpRequestBuilder timeout(int timeoutMillis);
    
    /**
     * Sets an attribute in the request context.
     * 
     * @param key the attribute key
     * @param value the attribute value
     * @return this builder
     * @throws IllegalArgumentException if key is null
     */
    HttpRequestBuilder attribute(String key, Object value);
    
    /**
     * Sets the HTTP GET method for the request.
     * 
     * @return this builder
     */
    default HttpRequestBuilder get() {
        return method(HttpMethod.GET);
    }
    
    /**
     * Sets the HTTP POST method for the request.
     * 
     * @return this builder
     */
    default HttpRequestBuilder post() {
        return method(HttpMethod.POST);
    }
    
    /**
     * Sets the HTTP PUT method for the request.
     * 
     * @return this builder
     */
    default HttpRequestBuilder put() {
        return method(HttpMethod.PUT);
    }
    
    /**
     * Sets the HTTP DELETE method for the request.
     * 
     * @return this builder
     */
    default HttpRequestBuilder delete() {
        return method(HttpMethod.DELETE);
    }
    
    /**
     * Sets the HTTP PATCH method for the request.
     * 
     * @return this builder
     */
    default HttpRequestBuilder patch() {
        return method(HttpMethod.PATCH);
    }
    
    /**
     * Sets the HTTP HEAD method for the request.
     * 
     * @return this builder
     */
    default HttpRequestBuilder head() {
        return method(HttpMethod.HEAD);
    }
    
    /**
     * Sets the HTTP OPTIONS method for the request.
     * 
     * @return this builder
     */
    default HttpRequestBuilder options() {
        return method(HttpMethod.OPTIONS);
    }
    
    /**
     * Configures the deserialization of the response body to the specified type.
     * 
     * @param <T> the type to deserialize to
     * @param type the class of the type to deserialize to
     * @return a specialized builder for typed responses
     * @throws IllegalArgumentException if type is null
     */
    <T> TypedHttpRequestBuilder<T> deserializeAs(Class<T> type);
    
    /**
     * Executes the request and returns the response.
     * 
     * @return the response
     * @throws NetworkException if an error occurs
     * @throws IllegalStateException if the builder is not properly configured
     */
    HttpResponse execute() throws NetworkException;
    
    /**
     * Builds the request without executing it.
     * 
     * @return the built request
     * @throws IllegalStateException if the builder is not properly configured
     */
    HttpRequest build();
}