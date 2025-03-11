package com.network.middleware.http;

/**
 * Interface for HTTP responses that can have their headers modified.
 * 
 * <p>This interface is used by middleware to add headers to responses.
 * All HTTP response implementations should implement this interface.
 */
public interface MutableHttpResponse {
    
    /**
     * Adds a header to the response.
     * 
     * @param name the header name
     * @param value the header value
     */
    void addHeader(String name, String value);
}
