package com.network.impl.http;

import com.network.api.http.HttpClientBuilder;
import com.network.api.http.HttpClientConfig;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.serialization.JsonSerializer;
import com.network.serialization.Serializer;

import java.net.ProxySelector;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Default implementation of the {@link HttpClientConfig} interface.
 */
public class DefaultHttpClientConfig implements HttpClientConfig {

    private final URL baseUrl;
    private final Map<String, String> defaultHeaders;
    private final List<HttpMiddleware> middlewares;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final boolean followRedirects;
    private final ProxySelector proxy;
    private final Executor executor;
    private final Serializer serializer;
    
    /**
     * Creates a new instance of DefaultHttpClientConfig.
     * 
     * @param builder the builder used to create this configuration
     */
    DefaultHttpClientConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.defaultHeaders = new ConcurrentHashMap<>(builder.defaultHeaders);
        this.middlewares = new ArrayList<>(builder.middlewares);
        this.connectTimeout = builder.connectTimeout;
        this.requestTimeout = builder.requestTimeout;
        this.followRedirects = builder.followRedirects;
        this.proxy = builder.proxy;
        this.executor = builder.executor;
        this.serializer = builder.serializer;
    }
    
    @Override
    public URL getBaseUrl() {
        return baseUrl;
    }
    
    @Override
    public Map<String, String> getDefaultHeaders() {
        return Collections.unmodifiableMap(defaultHeaders);
    }
    
    /**
     * Gets the list of middlewares.
     * 
     * @return the list of middlewares
     */
    public List<HttpMiddleware> getMiddlewares() {
        return Collections.unmodifiableList(middlewares);
    }
    
    /**
     * Gets the connection timeout.
     * 
     * @return the connection timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }
    
    /**
     * Gets the request timeout.
     * 
     * @return the request timeout
     */
    public Duration getRequestTimeout() {
        return requestTimeout;
    }
    
    /**
     * Checks if redirects should be followed.
     * 
     * @return true if redirects should be followed, false otherwise
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }
    
    /**
     * Gets the proxy selector.
     * 
     * @return the proxy selector
     */
    public ProxySelector getProxy() {
        return proxy;
    }
    
    /**
     * Gets the executor service used for asynchronous operations.
     * 
     * @return the executor service
     */
    public Executor getExecutor() {
        return executor;
    }
    
    /**
     * Gets the serializer.
     * 
     * @return the serializer
     */
    public Serializer getSerializer() {
        return serializer;
    }
    
    /**
     * Creates a new builder instance for creating HttpClientConfig objects.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link DefaultHttpClientConfig} instances.
     */
    public static class Builder implements HttpClientBuilder {
        private URL baseUrl;
        private final Map<String, String> defaultHeaders = new ConcurrentHashMap<>();
        private final List<HttpMiddleware> middlewares = new ArrayList<>();
        private Duration connectTimeout;
        private Duration requestTimeout;
        private boolean followRedirects = true;
        private ProxySelector proxy;
        private Executor executor;
        private Serializer serializer = new JsonSerializer();
        
        /**
         * Builds a new {@link DefaultHttpClientConfig} instance with the current settings.
         * 
         * @return a new DefaultHttpClientConfig instance
         */
        @Override
        public DefaultHttpClient build() {
            DefaultHttpClientConfig config = new DefaultHttpClientConfig(this);
            return new DefaultHttpClient(config);
        }
        
        @Override
        public HttpClientBuilder withBaseUrl(String baseUrl) {
            try {
                this.baseUrl = new URL(baseUrl);
                return this;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URL: " + baseUrl, e);
            }
        }
        
        @Override
        public HttpClientBuilder withBaseUrl(URL baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        @Override
        public HttpClientBuilder withHeader(String name, String value) {
            this.defaultHeaders.put(name, value);
            return this;
        }
        
        @Override
        public HttpClientBuilder withHeaders(Map<String, String> headers) {
            this.defaultHeaders.putAll(headers);
            return this;
        }
        
        @Override
        public HttpClientBuilder withTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            this.requestTimeout = timeout;
            return this;
        }
        
        /**
         * Sets the connection timeout.
         * 
         * @param timeout the connection timeout
         * @return this builder instance
         */
        public Builder withConnectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }
        
        /**
         * Sets the request timeout.
         * 
         * @param timeout the request timeout
         * @return this builder instance
         */
        public Builder withRequestTimeout(Duration timeout) {
            this.requestTimeout = timeout;
            return this;
        }
        
        /**
         * Sets whether to follow redirects.
         * 
         * @param followRedirects true to follow redirects, false otherwise
         * @return this builder instance
         */
        public Builder withFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }
        
        /**
         * Sets the proxy selector.
         * 
         * @param proxy the proxy selector
         * @return this builder instance
         */
        public Builder withProxy(ProxySelector proxy) {
            this.proxy = proxy;
            return this;
        }
        
        /**
         * Sets the executor service.
         * 
         * @param executor the executor service
         * @return this builder instance
         */
        public Builder withExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }
        
        /**
         * Sets the serializer.
         * 
         * @param serializer the serializer
         * @return this builder instance
         */
        public Builder withSerializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }
        
        /**
         * Adds a middleware to the client.
         * 
         * @param middleware the middleware to add
         * @return this builder instance
         */
        public Builder withMiddleware(HttpMiddleware middleware) {
            this.middlewares.add(middleware);
            return this;
        }
        
        /**
         * Adds multiple middlewares to the client.
         * 
         * @param middlewares the middlewares to add
         * @return this builder instance
         */
        public Builder withMiddlewares(List<HttpMiddleware> middlewares) {
            this.middlewares.addAll(middlewares);
            return this;
        }
    }
}
