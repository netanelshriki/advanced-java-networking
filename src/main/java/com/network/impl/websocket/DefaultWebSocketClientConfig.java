package com.network.impl.websocket;

import com.network.api.websocket.WebSocketClient;
import com.network.api.websocket.WebSocketClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Default implementation of WebSocket client configuration.
 */
public class DefaultWebSocketClientConfig {

    private final URI uri;
    private final Duration connectTimeout;
    private final Map<String, String> headers;
    private final ExecutorService executor;
    
    /**
     * Creates a new DefaultWebSocketClientConfig with the specified configuration.
     * 
     * @param builder the builder used to create this configuration
     */
    private DefaultWebSocketClientConfig(Builder builder) {
        this.uri = builder.uri;
        this.connectTimeout = builder.connectTimeout;
        this.headers = new HashMap<>(builder.headers);
        this.executor = builder.executor;
    }
    
    /**
     * Gets the WebSocket URI.
     * 
     * @return the URI
     */
    public URI getUri() {
        return uri;
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
     * Gets the WebSocket headers.
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    /**
     * Gets the executor service.
     * 
     * @return the executor service
     */
    public ExecutorService getExecutor() {
        return executor;
    }
    
    /**
     * Creates a new builder for creating DefaultWebSocketClientConfig instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link DefaultWebSocketClientConfig} instances.
     */
    public static class Builder implements WebSocketClientBuilder {
        private URI uri;
        private Duration connectTimeout;
        private final Map<String, String> headers = new HashMap<>();
        private ExecutorService executor;
        
        @Override
        public WebSocketClient build() {
            if (uri == null) {
                throw new IllegalStateException("URI must be set");
            }
            
            DefaultWebSocketClientConfig config = new DefaultWebSocketClientConfig(this);
            return new DefaultWebSocketClient(config);
        }
        
        @Override
        public WebSocketClientBuilder withUrl(String url) {
            try {
                this.uri = new URI(url);
                return this;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI: " + url, e);
            }
        }
        
        @Override
        public WebSocketClientBuilder withUri(URI uri) {
            this.uri = uri;
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
        
        @Override
        public WebSocketClientBuilder withHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }
        
        @Override
        public WebSocketClientBuilder withHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }
        
        /**
         * Sets the executor service.
         * 
         * @param executor the executor service
         * @return this builder instance
         */
        public Builder withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }
    }
}
