package com.network.api.http.middleware;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.http.HttpRequest;
import com.network.api.http.HttpResponse;

/**
 * Middleware for logging HTTP requests and responses.
 * 
 * <p>This middleware logs information about requests before they are sent,
 * and responses after they are received. It also measures the time taken
 * to process the request.
 */
public class LoggingMiddleware extends AbstractHttpMiddleware {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingMiddleware.class);
    
    private final LogLevel requestLevel;
    private final LogLevel responseLevel;
    private final boolean logHeaders;
    private final boolean logBody;
    private final int maxBodyLogSize;
    
    /**
     * Creates a new logging middleware with default settings.
     * 
     * <p>By default, logs requests and responses at DEBUG level,
     * includes headers, but doesn't log bodies.
     */
    public LoggingMiddleware() {
        this(LogLevel.DEBUG, LogLevel.DEBUG, true, false, 0);
    }
    
    /**
     * Creates a new logging middleware with the specified log levels.
     * 
     * @param requestLevel the log level for requests
     * @param responseLevel the log level for responses
     */
    public LoggingMiddleware(LogLevel requestLevel, LogLevel responseLevel) {
        this(requestLevel, responseLevel, true, false, 0);
    }
    
    /**
     * Creates a new logging middleware with the specified settings.
     * 
     * @param requestLevel the log level for requests
     * @param responseLevel the log level for responses
     * @param logHeaders whether to log headers
     * @param logBody whether to log request and response bodies
     * @param maxBodyLogSize the maximum size of body to log, or 0 for unlimited
     */
    public LoggingMiddleware(LogLevel requestLevel, LogLevel responseLevel, boolean logHeaders, boolean logBody, int maxBodyLogSize) {
        super("LoggingMiddleware", 50); // High priority to run before other middleware
        this.requestLevel = requestLevel;
        this.responseLevel = responseLevel;
        this.logHeaders = logHeaders;
        this.logBody = logBody;
        this.maxBodyLogSize = maxBodyLogSize;
    }
    
    @Override
    protected HttpRequest preProcess(HttpRequest request) {
        if (isRequestLoggingEnabled()) {
            logRequest(request);
        }
        
        // Store start time in request context
        request.getContext().setAttribute("logging.startTime", Instant.now());
        
        return request;
    }
    
    @Override
    protected HttpResponse postProcess(HttpRequest request, HttpResponse response) {
        if (isResponseLoggingEnabled()) {
            // Calculate duration
            Duration duration = null;
            Instant startTime = request.getContext().getAttribute("logging.startTime", Instant.class).orElse(null);
            if (startTime != null) {
                duration = Duration.between(startTime, Instant.now());
            }
            
            logResponse(request, response, duration);
        }
        
        return response;
    }
    
    private boolean isRequestLoggingEnabled() {
        switch (requestLevel) {
            case TRACE: return logger.isTraceEnabled();
            case DEBUG: return logger.isDebugEnabled();
            case INFO: return logger.isInfoEnabled();
            case WARN: return logger.isWarnEnabled();
            case ERROR: return logger.isErrorEnabled();
            default: return false;
        }
    }
    
    private boolean isResponseLoggingEnabled() {
        switch (responseLevel) {
            case TRACE: return logger.isTraceEnabled();
            case DEBUG: return logger.isDebugEnabled();
            case INFO: return logger.isInfoEnabled();
            case WARN: return logger.isWarnEnabled();
            case ERROR: return logger.isErrorEnabled();
            default: return false;
        }
    }
    
    private void logRequest(HttpRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sending ").append(request.getMethod()).append(" request to ").append(request.getUri());
        
        if (logHeaders && !request.getHeaders().isEmpty()) {
            sb.append("\nHeaders:");
            request.getHeaders().forEach((name, values) -> {
                sb.append("\n  ").append(name).append(": ");
                if (values.size() == 1) {
                    sb.append(values.get(0));
                } else {
                    sb.append(values);
                }
            });
        }
        
        if (logBody && request.hasBody()) {
            sb.append("\nBody: ");
            String body = request.getBodyAsString();
            if (maxBodyLogSize > 0 && body.length() > maxBodyLogSize) {
                sb.append(body, 0, maxBodyLogSize).append("... [truncated, ").append(body.length()).append(" bytes total]");
            } else {
                sb.append(body);
            }
        }
        
        log(requestLevel, sb.toString());
    }
    
    private void logResponse(HttpRequest request, HttpResponse response, Duration duration) {
        StringBuilder sb = new StringBuilder();
        sb.append("Received response ").append(response.getStatusCode()).append(" ").append(response.getStatusMessage())
          .append(" for ").append(request.getMethod()).append(" ").append(request.getUri());
        
        if (duration != null) {
            sb.append(" in ").append(duration.toMillis()).append("ms");
        }
        
        if (logHeaders && !response.getHeaders().isEmpty()) {
            sb.append("\nHeaders:");
            response.getHeaders().forEach((name, values) -> {
                sb.append("\n  ").append(name).append(": ");
                if (values.size() == 1) {
                    sb.append(values.get(0));
                } else {
                    sb.append(values);
                }
            });
        }
        
        if (logBody && response.hasBody()) {
            sb.append("\nBody: ");
            String body = response.getBodyAsString();
            if (maxBodyLogSize > 0 && body.length() > maxBodyLogSize) {
                sb.append(body, 0, maxBodyLogSize).append("... [truncated, ").append(body.length()).append(" bytes total]");
            } else {
                sb.append(body);
            }
        }
        
        log(responseLevel, sb.toString());
    }
    
    private void log(LogLevel level, String message) {
        switch (level) {
            case TRACE:
                logger.trace(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
    }
    
    /**
     * Log levels for the logging middleware.
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * Builder for creating {@link LoggingMiddleware} instances.
     */
    public static class Builder {
        private LogLevel requestLevel = LogLevel.DEBUG;
        private LogLevel responseLevel = LogLevel.DEBUG;
        private boolean logHeaders = true;
        private boolean logBody = false;
        private int maxBodyLogSize = 0;
        
        /**
         * Sets the log level for requests.
         * 
         * @param level the log level
         * @return this builder
         */
        public Builder requestLevel(LogLevel level) {
            this.requestLevel = level;
            return this;
        }
        
        /**
         * Sets the log level for responses.
         * 
         * @param level the log level
         * @return this builder
         */
        public Builder responseLevel(LogLevel level) {
            this.responseLevel = level;
            return this;
        }
        
        /**
         * Sets the log level for both requests and responses.
         * 
         * @param level the log level
         * @return this builder
         */
        public Builder level(LogLevel level) {
            this.requestLevel = level;
            this.responseLevel = level;
            return this;
        }
        
        /**
         * Sets whether to log headers.
         * 
         * @param logHeaders true to log headers, false to not
         * @return this builder
         */
        public Builder logHeaders(boolean logHeaders) {
            this.logHeaders = logHeaders;
            return this;
        }
        
        /**
         * Sets whether to log request and response bodies.
         * 
         * @param logBody true to log bodies, false to not
         * @return this builder
         */
        public Builder logBody(boolean logBody) {
            this.logBody = logBody;
            return this;
        }
        
        /**
         * Sets the maximum size of body to log.
         * 
         * <p>If the body is larger than this size, it will be truncated.
         * Set to 0 for unlimited.
         * 
         * @param maxBodyLogSize the maximum body size to log
         * @return this builder
         */
        public Builder maxBodyLogSize(int maxBodyLogSize) {
            this.maxBodyLogSize = maxBodyLogSize;
            return this;
        }
        
        /**
         * Builds a new logging middleware with the configured settings.
         * 
         * @return the built middleware
         */
        public LoggingMiddleware build() {
            return new LoggingMiddleware(requestLevel, responseLevel, logHeaders, logBody, maxBodyLogSize);
        }
    }
    
    /**
     * Creates a new builder for the logging middleware.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
}