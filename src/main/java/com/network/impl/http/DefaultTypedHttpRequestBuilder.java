package com.network.impl.http;

import com.network.api.http.HttpResponse;
import com.network.api.http.TypedHttpRequestBuilder;
import com.network.exception.NetworkException;

/**
 * Default implementation of the {@link TypedHttpRequestBuilder} interface.
 * 
 * @param <T> the response body type
 */
class DefaultTypedHttpRequestBuilder<T> implements TypedHttpRequestBuilder<T> {

    private final DefaultHttpRequestBuilder builder;
    private final Class<T> responseType;
    
    /**
     * Creates a new DefaultTypedHttpRequestBuilder.
     * 
     * @param builder      the HTTP request builder
     * @param responseType the response body type
     */
    DefaultTypedHttpRequestBuilder(DefaultHttpRequestBuilder builder, Class<T> responseType) {
        this.builder = builder;
        this.responseType = responseType;
    }

    @Override
    public T execute() throws NetworkException {
        HttpResponse response = builder.execute();
        
        if (!response.isSuccessful()) {
            throw new NetworkException("Request failed with status code " + response.getStatusCode(), 
                                      new RuntimeException(response.getStatusMessage()));
        }
        
        return response.getBodyAs(responseType);
    }
}
