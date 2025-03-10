package com.network.impl.http;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import com.network.api.http.HttpClientBuilder;
import com.network.api.http.HttpClientConfig;
import com.network.api.http.middleware.HttpMiddleware;
import com.network.config.AbstractNetworkConfig;
import com.network.serialization.Serializer;

/**
 * Default implementation of {@link HttpClientConfig}.
 * 
 * <p>This class provides a concrete implementation of the HTTP client configuration
 * interface with reasonable default values.
 */
public class DefaultHttpClientConfig extends AbstractNetworkConfig implements HttpClientConfig {
    
    private final URL baseUrl;
    private final Map<String, String> defaultHeaders;
    private final String defaultContentType;
    private final String defaultAccept;
    private final String userAgent;
    private final boolean followRedirects;
    private final int maxRedirects;
    private final boolean verifySsl;
    private final SSLContext sslContext;
    private final int maxConnectionsPerRoute;
    private final int maxTotalConnections;
    private final Duration connectionTimeToLive;
    private final Serializer serializer;
    private final List<HttpMiddleware> middleware;
    private final String proxyHost;
    private final int proxyPort;
    
    /**
     * Creates a new HTTP client configuration with the specified builder.
     * 
     * @param builder the builder containing the configuration values
     */
    public DefaultHttpClientConfig(Builder builder) {
        super(builder);
        this.baseUrl = builder.baseUrl;
        this.defaultHeaders = Collections.unmodifiableMap(new HashMap<>(builder.defaultHeaders));
        this.defaultContentType = builder.defaultContentType;
        this.defaultAccept = builder.defaultAccept;
        this.userAgent = builder.userAgent;
        this.followRedirects = builder.followRedirects;
        this.maxRedirects = builder.maxRedirects;
        this.verifySsl = builder.verifySsl;
        this.sslContext = builder.sslContext;
        this.maxConnectionsPerRoute = builder.maxConnectionsPerRoute;
        this.maxTotalConnections = builder.maxTotalConnections;
        this.connectionTimeToLive = builder.connectionTimeToLive;
        this.serializer = builder.serializer;
        this.middleware = Collections.unmodifiableList(new ArrayList<>(builder.middleware));
        this.proxyHost = builder.proxyHost;
        this.proxyPort = builder.proxyPort;
    }
    
    @Override
    public Optional<URL> getBaseUrl() {
        return Optional.ofNullable(baseUrl);
    }
    
    @Override
    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }
    
    @Override
    public Optional<String> getDefaultContentType() {
        return Optional.ofNullable(defaultContentType);
    }
    
    @Override
    public Optional<String> getDefaultAccept() {
        return Optional.ofNullable(defaultAccept);
    }
    
    @Override
    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }
    
    @Override
    public boolean isFollowRedirects() {
        return followRedirects;
    }
    
    @Override
    public int getMaxRedirects() {
        return maxRedirects;
    }
    
    @Override
    public boolean isVerifySsl() {
        return verifySsl;
    }
    
    @Override
    public Optional<SSLContext> getSslContext() {
        return Optional.ofNullable(sslContext);
    }
    
    @Override
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }
    
    @Override
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }
    
    @Override
    public Duration getConnectionTimeToLive() {
        return connectionTimeToLive;
    }
    
    @Override
    public Optional<Serializer> getSerializer() {
        return Optional.ofNullable(serializer);
    }
    
    @Override
    public List<HttpMiddleware> getMiddleware() {
        return middleware;
    }
    
    @Override
    public Optional<String> getProxyHost() {
        return Optional.ofNullable(proxyHost);
    }
    
    @Override
    public int getProxyPort() {
        return proxyPort;
    }
    
    @Override
    public HttpClientBuilder toBuilder() {
        return new Builder(this);
    }
    
    /**
     * Builder for {@link DefaultHttpClientConfig}.
     */
    public static class Builder extends AbstractNetworkConfigBuilder<Builder, HttpClientConfig>
            implements HttpClientBuilder {
        
        private URL baseUrl;
        private final Map<String, String> defaultHeaders = new HashMap<>();
        private String defaultContentType;
        private String defaultAccept;
        private String userAgent = "NetworkLib HTTP Client/1.0";
        private boolean followRedirects = true;
        private int maxRedirects = 5;
        private boolean verifySsl = true;
        private SSLContext sslContext;
        private int maxConnectionsPerRoute = 10;
        private int maxTotalConnections = 100;
        private Duration connectionTimeToLive = Duration.ofMinutes(5);
        private Serializer serializer;
        private final List<HttpMiddleware> middleware = new ArrayList<>();
        private String proxyHost;
        private int proxyPort = -1;
        
        /**
         * Creates a new builder with default values.
         */
        public Builder() {
            // Use default values
        }
        
        /**
         * Creates a new builder initialized with values from the specified configuration.
         * 
         * @param config the configuration to copy values from
         */
        public Builder(HttpClientConfig config) {
            super.connectionTimeout = config.getConnectionTimeout();
            super.readTimeout = config.getReadTimeout();
            super.writeTimeout = config.getWriteTimeout();
            super.idleTimeout = config.getIdleTimeout();
            super.maxRetryAttempts = config.getMaxRetryAttempts();
            super.retryBackoffStrategy = config.getRetryBackoffStrategy();
            super.keepAliveEnabled = config.isKeepAliveEnabled();
            super.keepAliveInterval = config.getKeepAliveInterval();
            super.bufferSize = config.getBufferSize();
            super.properties.putAll(config.getProperties());
            
            config.getBaseUrl().ifPresent(url -> this.baseUrl = url);
            this.defaultHeaders.putAll(config.getDefaultHeaders());
            config.getDefaultContentType().ifPresent(ct -> this.defaultContentType = ct);
            config.getDefaultAccept().ifPresent(a -> this.defaultAccept = a);
            config.getUserAgent().ifPresent(ua -> this.userAgent = ua);
            this.followRedirects = config.isFollowRedirects();
            this.maxRedirects = config.getMaxRedirects();
            this.verifySsl = config.isVerifySsl();
            config.getSslContext().ifPresent(sc -> this.sslContext = sc);
            this.maxConnectionsPerRoute = config.getMaxConnectionsPerRoute();
            this.maxTotalConnections = config.getMaxTotalConnections();
            this.connectionTimeToLive = config.getConnectionTimeToLive();
            config.getSerializer().ifPresent(s -> this.serializer = s);
            this.middleware.addAll(config.getMiddleware());
            config.getProxyHost().ifPresent(h -> this.proxyHost = h);
            this.proxyPort = config.getProxyPort();
        }
        
        @Override
        public Builder withBaseUrl(URL baseUrl) {
            if (baseUrl == null) {
                throw new IllegalArgumentException("Base URL must not be null");
            }
            this.baseUrl = baseUrl;
            return this;
        }
        
        @Override
        public Builder withBaseUrl(String baseUrl) {
            if (baseUrl == null) {
                throw new IllegalArgumentException("Base URL must not be null");
            }
            try {
                this.baseUrl = new URL(baseUrl);
                return this;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URL: " + baseUrl, e);
            }
        }
        
        @Override
        public Builder withDefaultHeader(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("Header name must not be null");
            }
            if (value == null) {
                defaultHeaders.remove(name);
            } else {
                defaultHeaders.put(name, value);
            }
            return this;
        }
        
        @Override
        public Builder withDefaultContentType(String contentType) {
            if (contentType == null) {
                throw new IllegalArgumentException("Content type must not be null");
            }
            this.defaultContentType = contentType;
            return this;
        }
        
        @Override
        public Builder withDefaultAccept(String accept) {
            if (accept == null) {
                throw new IllegalArgumentException("Accept header must not be null");
            }
            this.defaultAccept = accept;
            return this;
        }
        
        @Override
        public Builder withUserAgent(String userAgent) {
            if (userAgent == null) {
                throw new IllegalArgumentException("User agent must not be null");
            }
            this.userAgent = userAgent;
            return this;
        }
        
        @Override
        public Builder withFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }
        
        @Override
        public Builder withMaxRedirects(int maxRedirects) {
            if (maxRedirects < 0) {
                throw new IllegalArgumentException("Max redirects must not be negative");
            }
            this.maxRedirects = maxRedirects;
            return this;
        }
        
        @Override
        public Builder withVerifySsl(boolean verify) {
            this.verifySsl = verify;
            return this;
        }
        
        @Override
        public Builder withSslContext(SSLContext sslContext) {
            if (sslContext == null) {
                throw new IllegalArgumentException("SSL context must not be null");
            }
            this.sslContext = sslContext;
            return this;
        }
        
        @Override
        public Builder withTrustManagerFactory(javax.net.ssl.TrustManagerFactory trustManagerFactory) {
            if (trustManagerFactory == null) {
                throw new IllegalArgumentException("Trust manager factory must not be null");
            }
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustManagerFactory.getTrustManagers(), null);
                this.sslContext = context;
                return this;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create SSL context", e);
            }
        }
        
        @Override
        public Builder withTrustStore(java.security.KeyStore trustStore) {
            if (trustStore == null) {
                throw new IllegalArgumentException("Trust store must not be null");
            }
            try {
                javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(
                    javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);
                return withTrustManagerFactory(tmf);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create trust manager factory", e);
            }
        }
        
        @Override
        public Builder withMaxConnectionsPerRoute(int maxConnections) {
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("Max connections per route must be positive");
            }
            this.maxConnectionsPerRoute = maxConnections;
            return this;
        }
        
        @Override
        public Builder withMaxTotalConnections(int maxConnections) {
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("Max total connections must be positive");
            }
            this.maxTotalConnections = maxConnections;
            return this;
        }
        
        @Override
        public Builder withConnectionTimeToLive(Duration ttl) {
            if (ttl == null || ttl.isNegative()) {
                throw new IllegalArgumentException("Connection time to live must not be null or negative");
            }
            this.connectionTimeToLive = ttl;
            return this;
        }
        
        @Override
        public Builder withSerializer(Serializer serializer) {
            if (serializer == null) {
                throw new IllegalArgumentException("Serializer must not be null");
            }
            this.serializer = serializer;
            return this;
        }
        
        @Override
        public Builder withMiddleware(HttpMiddleware middleware) {
            if (middleware == null) {
                throw new IllegalArgumentException("Middleware must not be null");
            }
            this.middleware.add(middleware);
            // Sort middleware by order
            Collections.sort(this.middleware, (m1, m2) -> Integer.compare(m1.getOrder(), m2.getOrder()));
            return this;
        }
        
        @Override
        public Builder withProxy(String host, int port) {
            if (host == null) {
                throw new IllegalArgumentException("Proxy host must not be null");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Proxy port must be between 1 and 65535");
            }
            this.proxyHost = host;
            this.proxyPort = port;
            return this;
        }
        
        @Override
        public Builder configure(Consumer<HttpClientBuilder> configurer) {
            if (configurer == null) {
                throw new IllegalArgumentException("Configurer must not be null");
            }
            configurer.accept(this);
            return this;
        }
        
        @Override
        public HttpClientConfig build() {
            return new DefaultHttpClientConfig(this);
        }
    }
    
    /**
     * Creates a new builder for {@link DefaultHttpClientConfig}.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
}