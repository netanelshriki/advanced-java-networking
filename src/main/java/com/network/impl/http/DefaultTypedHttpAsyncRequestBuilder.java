package com.network.impl.http;

import com.network.api.http.HttpResponse;
import com.network.api.http.TypedHttpAsyncRequestBuilder;
import com.network.exception.NetworkException;

import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of the {@link TypedHttpAsyncRequestBuilder} interface.
 * 
 * @param <T> the response body type
 */
class DefaultTypedHttpAsyncRequestBuilder<T> implements TypedHttpAsyncRequestBuilder<T> {

    private final DefaultHttpAsyncRequestBuilder builder;
    private final Class<T> responseType;
    
    /**
     * Creates a new DefaultTypedHttpAsyncRequestBuilder.
     * 
     * @param builder      the HTTP async request builder
     * @param responseType the response body type
     */
    DefaultTypedHttpAsyncRequestBuilder(DefaultHttpAsyncRequestBuilder builder, Class<T> responseType) {
        this.builder = builder;
        this.responseType = responseType;
    }

    @Override
    public CompletableFuture<T> execute() {
        return builder.execute().thenApply(response -> {
            if (!response.isSuccessful()) {
                throw new NetworkException("Request failed with status code " + response.getStatusCode(), 
                                          new RuntimeException(response.getStatusMessage()));
            }
            
            return response.getBodyAs(responseType);
        });
    }
    
    @Override
    public CompletableFuture<HttpResponse<T>> executeWithResponse() {
        return builder.execute().thenApply(response -> {
            T body = null;
            if (response.isSuccessful() && response.getBody() != null && response.getBody().length > 0) {
                body = response.getBodyAs(responseType);
            }
            
            return new TypedHttpResponse<>(response, body);
        });
    }
    
    /**
     * HTTP response with a typed body.
     * 
     * @param <T> the body type
     */
    private static class TypedHttpResponse<T> implements HttpResponse<T> {
        private final HttpResponse delegate;
        private final T body;
        
        TypedHttpResponse(HttpResponse delegate, T body) {
            this.delegate = delegate;
            this.body = body;
        }

        @Override
        public int getStatusCode() {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusMessage() {
            return delegate.getStatusMessage();
        }

        @Override
        public boolean isSuccessful() {
            return delegate.isSuccessful();
        }

        @Override
        public byte[] getBody() {
            return delegate.getBody();
        }

        @Override
        public String getBodyAsString() {
            return delegate.getBodyAsString();
        }

        @Override
        public T getBodyAs(Class<T> type) {
            if (!type.isAssignableFrom(body.getClass())) {
                throw new IllegalArgumentException("Cannot convert body to " + type.getName());
            }
            return body;
        }
        
        @Override
        public T getBody() {
            return body;
        }

        @Override
        public java.util.Map<String, String> getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public String getHeader(String name) {
            return delegate.getHeader(name);
        }

        @Override
        public String getContentType() {
            return delegate.getContentType();
        }

        @Override
        public java.net.URI getUri() {
            return delegate.getUri();
        }

        @Override
        public com.network.api.http.HttpRequest getRequest() {
            return delegate.getRequest();
        }

        @Override
        public HttpResponse<T> throwIfNotSuccessful() throws com.network.api.http.HttpResponseException {
            if (!isSuccessful()) {
                throw new com.network.api.http.HttpResponseException(delegate);
            }
            return this;
        }
    }
}
