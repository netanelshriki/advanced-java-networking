package com.network.api.http;

import java.net.URI;

import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;

/**
 * Exception thrown when an HTTP response has an error status code.
 * 
 * <p>This exception provides access to the underlying HTTP response
 * that caused the error, allowing for detailed error inspection.
 */
public class HttpResponseException extends NetworkException {
    
    private static final long serialVersionUID = 1L;
    
    private final HttpResponse<?> response;
    
    /**
     * Creates a new HTTP response exception with the specified response.
     * 
     * @param response the HTTP response that caused the error
     */
    public HttpResponseException(HttpResponse<?> response) {
        super(buildMessage(response), determineErrorCode(response.getStatusCode()));
        this.response = response;
    }
    
    /**
     * Creates a new HTTP response exception with the specified response and custom message.
     * 
     * @param response the HTTP response that caused the error
     * @param message the detail message
     */
    public HttpResponseException(HttpResponse<?> response, String message) {
        super(determineErrorCode(response.getStatusCode()), message);
        this.response = response;
    }
    
    /**
     * Creates a new HTTP response exception with the specified response and cause.
     * 
     * @param response the HTTP response that caused the error
     * @param cause the cause of this exception
     */
    public HttpResponseException(HttpResponse<?> response, Throwable cause) {
        super(determineErrorCode(response.getStatusCode()), buildMessage(response), cause);
        this.response = response;
    }
    
    /**
     * Creates a new HTTP response exception with the specified response, custom message, and cause.
     * 
     * @param response the HTTP response that caused the error
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public HttpResponseException(HttpResponse<?> response, String message, Throwable cause) {
        super(determineErrorCode(response.getStatusCode()), message, cause);
        this.response = response;
    }
    
    /**
     * Gets the HTTP response that caused this exception.
     * 
     * @return the HTTP response
     */
    public HttpResponse<?> getResponse() {
        return response;
    }
    
    /**
     * Gets the status code of the HTTP response.
     * 
     * @return the status code
     */
    public int getStatusCode() {
        return response.getStatusCode();
    }
    
    /**
     * Gets the status message of the HTTP response.
     * 
     * @return the status message
     */
    public String getStatusMessage() {
        return response.getStatusMessage();
    }
    
    /**
     * Gets the URI of the request that produced the HTTP response.
     * 
     * @return the request URI
     */
    public URI getRequestUri() {
        return response.getRequestUri();
    }
    
    /**
     * Gets the body of the HTTP response as a string.
     * 
     * @return the body as a string, or an empty string if no body
     */
    public String getResponseBody() {
        return response.getBodyAsString();
    }
    
    /**
     * Builds a standard error message from an HTTP response.
     * 
     * @param response the HTTP response
     * @return the error message
     */
    private static String buildMessage(HttpResponse<?> response) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP request failed: ")
          .append(response.getStatusCode())
          .append(" ")
          .append(response.getStatusMessage());
        
        URI uri = response.getRequestUri();
        if (uri != null) {
            sb.append(" for ")
              .append(response.getRequest().getMethod())
              .append(" ")
              .append(uri);
        }
        
        return sb.toString();
    }
    
    /**
     * Determines the appropriate error code for an HTTP status code.
     * 
     * @param statusCode the HTTP status code
     * @return the corresponding error code
     */
    private static ErrorCode determineErrorCode(int statusCode) {
        if (statusCode >= 400 && statusCode < 500) {
            return ErrorCode.PROTOCOL_ERROR;
        } else if (statusCode >= 500) {
            return ErrorCode.INVALID_RESPONSE;
        } else {
            return ErrorCode.UNKNOWN;
        }
    }
}