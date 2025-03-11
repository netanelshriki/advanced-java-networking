package com.network.middleware.http;

import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTTP middleware that logs request and response details.
 * 
 * <p>This middleware logs information about HTTP requests and responses, including
 * headers, bodies, timing information, and status codes. The logging level and
 * what details are logged can be configured through the builder.
 */
public class LoggingMiddleware implements HttpMiddleware {

    private static final String REQUEST_TIMESTAMP_ATTR = "logging.requestTimestamp";
    
    private final Logger logger;
    private final LogLevel level;
    private final boolean logHeaders;
    private final boolean logBody;
    private final int maxBodyLogLength;
    private final Set<String> sensitiveHeaders;
    private final Set<String> excludedHeaders;
    
    /**
     * Creates a new LoggingMiddleware with the specified configuration.
     * 
     * @param builder the builder used to create this middleware
     */
    private LoggingMiddleware(Builder builder) {
        this.logger = builder.logger != null ? builder.logger : LoggerFactory.getLogger(LoggingMiddleware.class);
        this.level = builder.level;
        this.logHeaders = builder.logHeaders;
        this.logBody = builder.logBody;
        this.maxBodyLogLength = builder.maxBodyLogLength;
        this.sensitiveHeaders = builder.sensitiveHeaders;
        this.excludedHeaders = builder.excludedHeaders;
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        // Record the start time for measuring duration
        context.setAttribute(REQUEST_TIMESTAMP_ATTR, Instant.now());
        
        // Log the request
        log("Sending request {} {}", context.getRequest().getMethod(), context.getRequest().getUri());
        
        // Log request headers if enabled
        if (logHeaders) {
            Map<String, String> headers = context.getRequest().getHeaders();
            if (!headers.isEmpty()) {
                log("Request headers:");
                logHeaders(headers);
            }
        }
        
        // Log request body if enabled
        if (logBody && context.getRequest().getBody() != null) {
            log("Request body: {}", truncateBody(new String(context.getRequest().getBody())));
        }
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        // Calculate the request duration if the start time was recorded
        String duration = "";
        if (context.hasAttribute(REQUEST_TIMESTAMP_ATTR)) {
            Instant start = (Instant) context.getAttribute(REQUEST_TIMESTAMP_ATTR);
            Duration elapsed = Duration.between(start, Instant.now());
            duration = String.format(" (%s ms)", elapsed.toMillis());
        }
        
        // Log the response
        log("Received response {} {}{}", response.getStatusCode(), response.getStatusMessage(), duration);
        
        // Log response headers if enabled
        if (logHeaders) {
            Map<String, String> headers = response.getHeaders();
            if (!headers.isEmpty()) {
                log("Response headers:");
                logHeaders(headers);
            }
        }
        
        // Log response body if enabled
        if (logBody && response.getBody() != null) {
            log("Response body: {}", truncateBody(response.getBodyAsString()));
        }
    }
    
    /**
     * Logs the message at the configured log level.
     * 
     * @param message the message to log
     * @param args the message arguments
     */
    private void log(String message, Object... args) {
        switch (level) {
            case TRACE:
                logger.trace(message, args);
                break;
            case DEBUG:
                logger.debug(message, args);
                break;
            case INFO:
                logger.info(message, args);
                break;
            case WARN:
                logger.warn(message, args);
                break;
            case ERROR:
                logger.error(message, args);
                break;
        }
    }
    
    /**
     * Logs the headers, filtering out sensitive headers and excluded headers.
     * 
     * @param headers the headers to log
     */
    private void logHeaders(Map<String, String> headers) {
        headers.forEach((name, value) -> {
            if (excludedHeaders.contains(name.toLowerCase())) {
                // Skip excluded headers
                return;
            }
            
            if (sensitiveHeaders.contains(name.toLowerCase())) {
                // Mask sensitive headers
                log("  {}: {}", name, "********");
            } else {
                log("  {}: {}", name, value);
            }
        });
    }
    
    /**
     * Truncates the body to the maximum log length if necessary.
     * 
     * @param body the body to truncate
     * @return the truncated body
     */
    private String truncateBody(String body) {
        if (body == null) {
            return null;
        }
        
        if (body.length() <= maxBodyLogLength) {
            return body;
        }
        
        return body.substring(0, maxBodyLogLength) + "... [truncated " + (body.length() - maxBodyLogLength) + " chars]";
    }
    
    /**
     * Creates a new builder for creating LoggingMiddleware instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link LoggingMiddleware} instances.
     */
    public static class Builder {
        private Logger logger;
        private LogLevel level = LogLevel.DEBUG;
        private boolean logHeaders = true;
        private boolean logBody = false;
        private int maxBodyLogLength = 1000;
        private final Set<String> sensitiveHeaders = new HashSet<>();
        private final Set<String> excludedHeaders = new HashSet<>();
        
        /**
         * Creates a new Builder with default values.
         */
        public Builder() {
            // Add default sensitive headers
            sensitiveHeaders.add("authorization");
            sensitiveHeaders.add("proxy-authorization");
            sensitiveHeaders.add("cookie");
            sensitiveHeaders.add("set-cookie");
            
            // No default excluded headers
        }
        
        /**
         * Sets the logger to use.
         * 
         * @param logger the logger
         * @return this builder instance
         */
        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }
        
        /**
         * Sets the log level to use.
         * 
         * @param level the log level
         * @return this builder instance
         */
        public Builder level(LogLevel level) {
            this.level = level;
            return this;
        }
        
        /**
         * Sets whether to log headers.
         * 
         * @param logHeaders true to log headers, false otherwise
         * @return this builder instance
         */
        public Builder logHeaders(boolean logHeaders) {
            this.logHeaders = logHeaders;
            return this;
        }
        
        /**
         * Sets whether to log request and response bodies.
         * 
         * @param logBody true to log bodies, false otherwise
         * @return this builder instance
         */
        public Builder logBody(boolean logBody) {
            this.logBody = logBody;
            return this;
        }
        
        /**
         * Sets the maximum body length to log.
         * 
         * <p>Bodies longer than this will be truncated.
         * 
         * @param maxBodyLogLength the maximum body length
         * @return this builder instance
         */
        public Builder maxBodyLogLength(int maxBodyLogLength) {
            this.maxBodyLogLength = maxBodyLogLength;
            return this;
        }
        
        /**
         * Adds a sensitive header.
         * 
         * <p>Sensitive headers will have their values masked in the logs.
         * 
         * @param header the header name
         * @return this builder instance
         */
        public Builder addSensitiveHeader(String header) {
            sensitiveHeaders.add(header.toLowerCase());
            return this;
        }
        
        /**
         * Sets the sensitive headers.
         * 
         * <p>Sensitive headers will have their values masked in the logs.
         * 
         * @param headers the header names
         * @return this builder instance
         */
        public Builder sensitiveHeaders(String... headers) {
            sensitiveHeaders.clear();
            for (String header : headers) {
                sensitiveHeaders.add(header.toLowerCase());
            }
            return this;
        }
        
        /**
         * Adds an excluded header.
         * 
         * <p>Excluded headers will not be logged at all.
         * 
         * @param header the header name
         * @return this builder instance
         */
        public Builder addExcludedHeader(String header) {
            excludedHeaders.add(header.toLowerCase());
            return this;
        }
        
        /**
         * Sets the excluded headers.
         * 
         * <p>Excluded headers will not be logged at all.
         * 
         * @param headers the header names
         * @return this builder instance
         */
        public Builder excludedHeaders(String... headers) {
            excludedHeaders.clear();
            for (String header : headers) {
                excludedHeaders.add(header.toLowerCase());
            }
            return this;
        }
        
        /**
         * Builds a new {@link LoggingMiddleware} instance with the current settings.
         * 
         * @return a new LoggingMiddleware instance
         */
        public LoggingMiddleware build() {
            return new LoggingMiddleware(this);
        }
    }
    
    /**
     * Enum representing the possible log levels.
     */
    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
}
