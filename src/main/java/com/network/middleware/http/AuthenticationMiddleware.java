package com.network.middleware.http;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * HTTP middleware that handles authentication.
 * 
 * <p>This middleware can add various authentication headers to requests, including
 * Basic, Bearer, API key, and custom authentication schemes. It also handles token
 * refresh logic for OAuth-style authentication.
 */
public class AuthenticationMiddleware implements HttpMiddleware {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationMiddleware.class);
    
    private final AuthenticationScheme scheme;
    private final Supplier<String> credentialsSupplier;
    private final Supplier<CompletableFuture<String>> tokenRefreshFunction;
    private final boolean autoRefresh;
    
    private volatile String cachedCredentials;
    private volatile Instant expiresAt;
    
    /**
     * Creates a new AuthenticationMiddleware with the specified configuration.
     * 
     * @param builder the builder used to create this middleware
     */
    private AuthenticationMiddleware(Builder builder) {
        this.scheme = builder.scheme;
        this.credentialsSupplier = builder.credentialsSupplier;
        this.tokenRefreshFunction = builder.tokenRefreshFunction;
        this.autoRefresh = builder.autoRefresh;
        
        // Initialize credentials
        if (credentialsSupplier != null) {
            this.cachedCredentials = credentialsSupplier.get();
        }
        
        if (builder.tokenTtl != null) {
            this.expiresAt = Instant.now().plus(builder.tokenTtl);
        }
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        // Skip authentication for certain requests if needed
        if (shouldSkipAuthentication(context)) {
            return;
        }
        
        // Check if we need to refresh the token
        if (autoRefresh && tokenRefreshFunction != null && isTokenExpired()) {
            try {
                refreshToken().join(); // Wait for token refresh to complete
            } catch (Exception e) {
                LOG.warn("Failed to refresh authentication token", e);
                // Continue with the existing token
            }
        }
        
        // Add the appropriate authentication header
        String headerValue = formatAuthenticationHeader();
        
        // Add header to request if implementation supports it
        if (context.getRequest() instanceof MutableHttpRequest) {
            MutableHttpRequest req = (MutableHttpRequest) context.getRequest();
            req.addHeader("Authorization", headerValue);
        }
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        // Check for authentication failures
        if (isAuthenticationFailure(response)) {
            LOG.debug("Authentication failure detected: {}", response.getStatusCode());
            
            // If we have a refresh function, try to refresh the token
            if (tokenRefreshFunction != null) {
                markTokenExpired();
                
                // Note: We don't actually refresh the token here because that would require
                // retrying the request, which is handled by the RetryMiddleware
            }
        }
    }
    
    /**
     * Refreshes the authentication token asynchronously.
     * 
     * @return a CompletableFuture that completes when the token has been refreshed
     */
    public CompletableFuture<Void> refreshToken() {
        if (tokenRefreshFunction == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        LOG.debug("Refreshing authentication token");
        
        return tokenRefreshFunction.get().thenAccept(newToken -> {
            cachedCredentials = newToken;
            markTokenRefreshed();
            LOG.debug("Authentication token refreshed");
        });
    }
    
    /**
     * Sets the token expiration time based on the TTL.
     * 
     * @param ttl the time-to-live for the token
     */
    public void setTokenTtl(Duration ttl) {
        if (ttl != null) {
            this.expiresAt = Instant.now().plus(ttl);
        }
    }
    
    /**
     * Checks if the authentication token has expired.
     * 
     * @return true if the token has expired, false otherwise
     */
    public boolean isTokenExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Marks the token as expired, forcing a refresh on the next request.
     */
    public void markTokenExpired() {
        this.expiresAt = Instant.now().minusSeconds(1);
    }
    
    /**
     * Marks the token as refreshed, resetting the expiration time.
     */
    private void markTokenRefreshed() {
        if (expiresAt != null) {
            this.expiresAt = Instant.now().plus(Duration.between(
                    expiresAt.minusSeconds(1), expiresAt));
        }
    }
    
    /**
     * Determines if authentication should be skipped for the request.
     * 
     * @param context the request context
     * @return true if authentication should be skipped, false otherwise
     */
    private boolean shouldSkipAuthentication(HttpRequestContext context) {
        // Example: Skip authentication for certain paths
        String path = context.getRequest().getUri().getPath();
        return path != null && (
                path.endsWith("/login") || 
                path.endsWith("/auth") || 
                path.endsWith("/token"));
    }
    
    /**
     * Checks if the response indicates an authentication failure.
     * 
     * @param response the HTTP response
     * @return true if authentication failed, false otherwise
     */
    private boolean isAuthenticationFailure(HttpResponse response) {
        return response.getStatusCode() == 401 || 
               (response.getStatusCode() == 403 && scheme == AuthenticationScheme.BEARER);
    }
    
    /**
     * Formats the authentication header value based on the scheme.
     * 
     * @return the formatted header value
     */
    private String formatAuthenticationHeader() {
        if (cachedCredentials == null) {
            // Fall back to supplier if available
            if (credentialsSupplier != null) {
                cachedCredentials = credentialsSupplier.get();
            }
            
            if (cachedCredentials == null) {
                throw new IllegalStateException("No authentication credentials available");
            }
        }
        
        switch (scheme) {
            case BASIC:
                // Basic authentication requires Base64 encoding
                return "Basic " + cachedCredentials;
                
            case BEARER:
                return "Bearer " + cachedCredentials;
                
            case API_KEY:
                return "ApiKey " + cachedCredentials;
                
            case CUSTOM:
                return cachedCredentials;
                
            default:
                throw new IllegalStateException("Unsupported authentication scheme: " + scheme);
        }
    }
    
    /**
     * Creates a new builder for creating AuthenticationMiddleware instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link AuthenticationMiddleware} instances.
     */
    public static class Builder {
        private AuthenticationScheme scheme = AuthenticationScheme.BEARER;
        private Supplier<String> credentialsSupplier;
        private Supplier<CompletableFuture<String>> tokenRefreshFunction;
        private Duration tokenTtl;
        private boolean autoRefresh = true;
        
        /**
         * Sets the authentication scheme.
         * 
         * @param scheme the authentication scheme
         * @return this builder instance
         */
        public Builder scheme(AuthenticationScheme scheme) {
            this.scheme = scheme;
            return this;
        }
        
        /**
         * Sets the credentials supplier.
         * 
         * <p>This supplier will be called to get the authentication credentials
         * when needed.
         * 
         * @param credentialsSupplier the credentials supplier
         * @return this builder instance
         */
        public Builder credentialsSupplier(Supplier<String> credentialsSupplier) {
            this.credentialsSupplier = credentialsSupplier;
            return this;
        }
        
        /**
         * Sets the token refresh function.
         * 
         * <p>This function will be called to refresh the authentication token
         * when it expires or when authentication fails.
         * 
         * @param tokenRefreshFunction the token refresh function
         * @return this builder instance
         */
        public Builder tokenRefreshFunction(Supplier<CompletableFuture<String>> tokenRefreshFunction) {
            this.tokenRefreshFunction = tokenRefreshFunction;
            return this;
        }
        
        /**
         * Sets the token time-to-live.
         * 
         * <p>This is used to determine when the token expires and needs to be refreshed.
         * 
         * @param tokenTtl the token time-to-live
         * @return this builder instance
         */
        public Builder tokenTtl(Duration tokenTtl) {
            this.tokenTtl = tokenTtl;
            return this;
        }
        
        /**
         * Sets whether to automatically refresh the token when it expires.
         * 
         * @param autoRefresh true to auto-refresh, false otherwise
         * @return this builder instance
         */
        public Builder autoRefresh(boolean autoRefresh) {
            this.autoRefresh = autoRefresh;
            return this;
        }
        
        /**
         * Configures Bearer token authentication.
         * 
         * @param token the bearer token
         * @return this builder instance
         */
        public Builder bearerToken(String token) {
            return scheme(AuthenticationScheme.BEARER)
                    .credentialsSupplier(() -> token);
        }
        
        /**
         * Configures Bearer token authentication with a token supplier.
         * 
         * @param tokenSupplier the token supplier
         * @return this builder instance
         */
        public Builder bearerToken(Supplier<String> tokenSupplier) {
            return scheme(AuthenticationScheme.BEARER)
                    .credentialsSupplier(tokenSupplier);
        }
        
        /**
         * Configures Basic authentication.
         * 
         * @param username the username
         * @param password the password
         * @return this builder instance
         */
        public Builder basicAuth(String username, String password) {
            String credentials = Base64.getEncoder().encodeToString(
                    (username + ":" + password).getBytes());
            
            return scheme(AuthenticationScheme.BASIC)
                    .credentialsSupplier(() -> credentials);
        }
        
        /**
         * Configures API key authentication.
         * 
         * @param apiKey the API key
         * @return this builder instance
         */
        public Builder apiKey(String apiKey) {
            return scheme(AuthenticationScheme.API_KEY)
                    .credentialsSupplier(() -> apiKey);
        }
        
        /**
         * Configures a custom authentication scheme.
         * 
         * @param headerValue the complete header value
         * @return this builder instance
         */
        public Builder custom(String headerValue) {
            return scheme(AuthenticationScheme.CUSTOM)
                    .credentialsSupplier(() -> headerValue);
        }
        
        /**
         * Builds a new {@link AuthenticationMiddleware} instance with the current settings.
         * 
         * @return a new AuthenticationMiddleware instance
         */
        public AuthenticationMiddleware build() {
            Objects.requireNonNull(scheme, "Authentication scheme must be set");
            
            if (credentialsSupplier == null && tokenRefreshFunction == null) {
                throw new IllegalStateException("Either credentialsSupplier or tokenRefreshFunction must be set");
            }
            
            return new AuthenticationMiddleware(this);
        }
    }
    
    /**
     * Enum representing the possible authentication schemes.
     */
    public enum AuthenticationScheme {
        /**
         * Basic authentication (username/password).
         */
        BASIC,
        
        /**
         * Bearer token authentication (OAuth, JWT).
         */
        BEARER,
        
        /**
         * API key authentication.
         */
        API_KEY,
        
        /**
         * Custom authentication scheme.
         */
        CUSTOM
    }
    
    /**
     * Interface for modifiable HTTP requests.
     * 
     * <p>This interface is used internally by the middleware to add headers to the request.
     * Implementations of HTTP request should also implement this interface.
     */
    private interface MutableHttpRequest {
        /**
         * Adds a header to the request.
         * 
         * @param name the header name
         * @param value the header value
         */
        void addHeader(String name, String value);
    }
}
