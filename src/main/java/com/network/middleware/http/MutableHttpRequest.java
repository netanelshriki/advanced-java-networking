package com.network.middleware.http;

/**
 * Interface for HTTP requests that can have their headers modified.
 * 
 * <p>This interface is used by middleware to add headers to requests.
 * All HTTP request implementations should implement this interface.
 */
public interface MutableHttpRequest {
    
    /**
     * Adds a header to the request.
     * 
     * @param name the header name
     * @param value the header value
     */
    void addHeader(String name, String value);
}
