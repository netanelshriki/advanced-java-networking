package com.network.api.http;

import java.net.URL;
import java.security.KeyStore;
import java.time.Duration;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.network.api.http.middleware.HttpMiddleware;
import com.network.config.NetworkConfigBuilder;
import com.network.serialization.Serializer;

/**
 * Builder for HTTP clients.
 * 
 * <p>This interface defines the methods for building HTTP clients with
 * various configuration options.
 */
public interface HttpClientBuilder extends NetworkConfigBuilder<HttpClientBuilder, HttpClientConfig> {
    
    /**
     * Sets the base URL for the client.
     * 
     * <p>All relative URLs will be resolved against this base URL.
     * 
     * @param baseUrl the base URL
     * @return this builder
     * @throws IllegalArgumentException if baseUrl is null
     */
    HttpClientBuilder withBaseUrl(URL baseUrl);
    
    /**
     * Sets the base URL for the client.
     * 
     * <p>All relative URLs will be resolved against this base URL.
     * 
     * @param baseUrl the base URL as a string
     * @return this builder
     * @throws IllegalArgumentException if baseUrl is null or not a valid URL
     */
    HttpClientBuilder withBaseUrl(String baseUrl);
    
    /**
     * Sets a default header that will be sent with every request.
     * 
     * @param name the header name
     * @param value the header value
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    HttpClientBuilder withDefaultHeader(String name, String value);
    
    /**
     * Sets the default content type for requests.
     * 
     * @param contentType the content type
     * @return this builder
     * @throws IllegalArgumentException if contentType is null
     */
    HttpClientBuilder withDefaultContentType(String contentType);
    
    /**
     * Sets the default accept header for requests.
     * 
     * @param accept the accept header value
     * @return this builder
     * @throws IllegalArgumentException if accept is null
     */
    HttpClientBuilder withDefaultAccept(String accept);
    
    /**
     * Sets the user agent header for requests.
     * 
     * @param userAgent the user agent
     * @return this builder
     * @throws IllegalArgumentException if userAgent is null
     */
    HttpClientBuilder withUserAgent(String userAgent);
    
    /**
     * Sets the follow redirects flag.
     * 
     * @param followRedirects true to follow redirects, false to not
     * @return this builder
     */
    HttpClientBuilder withFollowRedirects(boolean followRedirects);
    
    /**
     * Sets the maximum number of redirects to follow.
     * 
     * @param maxRedirects the maximum number of redirects
     * @return this builder
     * @throws IllegalArgumentException if maxRedirects is negative
     */
    HttpClientBuilder withMaxRedirects(int maxRedirects);
    
    /**
     * Sets whether to verify SSL certificates.
     * 
     * @param verify true to verify, false to not
     * @return this builder
     */
    HttpClientBuilder withVerifySsl(boolean verify);
    
    /**
     * Sets the SSL context to use for secure connections.
     * 
     * @param sslContext the SSL context
     * @return this builder
     * @throws IllegalArgumentException if sslContext is null
     */
    HttpClientBuilder withSslContext(SSLContext sslContext);
    
    /**
     * Sets the trust manager factory to use for secure connections.
     * 
     * @param trustManagerFactory the trust manager factory
     * @return this builder
     * @throws IllegalArgumentException if trustManagerFactory is null
     */
    HttpClientBuilder withTrustManagerFactory(TrustManagerFactory trustManagerFactory);
    
    /**
     * Sets the trust store to use for secure connections.
     * 
     * @param trustStore the trust store
     * @return this builder
     * @throws IllegalArgumentException if trustStore is null
     */
    HttpClientBuilder withTrustStore(KeyStore trustStore);
    
    /**
     * Sets the maximum number of connections per route.
     * 
     * @param maxConnections the maximum number of connections
     * @return this builder
     * @throws IllegalArgumentException if maxConnections is not positive
     */
    HttpClientBuilder withMaxConnectionsPerRoute(int maxConnections);
    
    /**
     * Sets the maximum total number of connections.
     * 
     * @param maxConnections the maximum number of connections
     * @return this builder
     * @throws IllegalArgumentException if maxConnections is not positive
     */
    HttpClientBuilder withMaxTotalConnections(int maxConnections);
    
    /**
     * Sets the connection time to live.
     * 
     * @param ttl the time to live
     * @return this builder
     * @throws IllegalArgumentException if ttl is negative
     */
    HttpClientBuilder withConnectionTimeToLive(Duration ttl);
    
    /**
     * Sets the default serializer for requests and responses.
     * 
     * @param serializer the serializer
     * @return this builder
     * @throws IllegalArgumentException if serializer is null
     */
    HttpClientBuilder withSerializer(Serializer serializer);
    
    /**
     * Adds middleware to the client.
     * 
     * <p>Middleware will be executed in the order they are added.
     * 
     * @param middleware the middleware to add
     * @return this builder
     * @throws IllegalArgumentException if middleware is null
     */
    HttpClientBuilder withMiddleware(HttpMiddleware middleware);
    
    /**
     * Sets the proxy to use for connections.
     * 
     * @param host the proxy host
     * @param port the proxy port
     * @return this builder
     * @throws IllegalArgumentException if host is null or port is not valid
     */
    HttpClientBuilder withProxy(String host, int port);
    
    /**
     * Configures the client using the given consumer.
     * 
     * <p>This method allows for more complex configuration that may require
     * multiple builder calls.
     * 
     * @param configurer the configurer
     * @return this builder
     * @throws IllegalArgumentException if configurer is null
     */
    HttpClientBuilder configure(Consumer<HttpClientBuilder> configurer);
    
    /**
     * Builds the HTTP client.
     * 
     * @return the built HTTP client
     * @throws IllegalStateException if the builder is not properly configured
     */
    HttpClient build();
}