package com.network.api.http;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.network.api.http.middleware.HttpMiddleware;
import com.network.config.NetworkConfig;
import com.network.serialization.Serializer;

/**
 * Configuration for HTTP clients.
 * 
 * <p>This interface defines the configuration properties specific to HTTP clients.
 */
public interface HttpClientConfig extends NetworkConfig {
    
    /**
     * Gets the base URL for the client.
     * 
     * @return an Optional containing the base URL, or empty if not set
     */
    Optional<URL> getBaseUrl();
    
    /**
     * Gets the default headers that will be sent with every request.
     * 
     * @return the default headers
     */
    Map<String, String> getDefaultHeaders();
    
    /**
     * Gets the default content type for requests.
     * 
     * @return an Optional containing the default content type, or empty if not set
     */
    Optional<String> getDefaultContentType();
    
    /**
     * Gets the default accept header for requests.
     * 
     * @return an Optional containing the default accept header, or empty if not set
     */
    Optional<String> getDefaultAccept();
    
    /**
     * Gets the user agent header for requests.
     * 
     * @return an Optional containing the user agent, or empty if not set
     */
    Optional<String> getUserAgent();
    
    /**
     * Gets whether to follow redirects.
     * 
     * @return true if redirects should be followed, false otherwise
     */
    boolean isFollowRedirects();
    
    /**
     * Gets the maximum number of redirects to follow.
     * 
     * @return the maximum number of redirects
     */
    int getMaxRedirects();
    
    /**
     * Gets whether to verify SSL certificates.
     * 
     * @return true if SSL certificates should be verified, false otherwise
     */
    boolean isVerifySsl();
    
    /**
     * Gets the SSL context to use for secure connections.
     * 
     * @return an Optional containing the SSL context, or empty if not set
     */
    Optional<SSLContext> getSslContext();
    
    /**
     * Gets the maximum number of connections per route.
     * 
     * @return the maximum number of connections per route
     */
    int getMaxConnectionsPerRoute();
    
    /**
     * Gets the maximum total number of connections.
     * 
     * @return the maximum total number of connections
     */
    int getMaxTotalConnections();
    
    /**
     * Gets the connection time to live.
     * 
     * @return the connection time to live
     */
    Duration getConnectionTimeToLive();
    
    /**
     * Gets the default serializer for requests and responses.
     * 
     * @return an Optional containing the serializer, or empty if not set
     */
    Optional<Serializer> getSerializer();
    
    /**
     * Gets the middleware for the client.
     * 
     * <p>Middleware will be executed in the order they appear in the list.
     * 
     * @return the list of middleware
     */
    List<HttpMiddleware> getMiddleware();
    
    /**
     * Gets the proxy host.
     * 
     * @return an Optional containing the proxy host, or empty if not set
     */
    Optional<String> getProxyHost();
    
    /**
     * Gets the proxy port.
     * 
     * @return the proxy port, or -1 if not set
     */
    int getProxyPort();
    
    /**
     * Creates a builder pre-configured with the values from this configuration.
     * 
     * @return a new builder
     */
    @Override
    HttpClientBuilder toBuilder();
}