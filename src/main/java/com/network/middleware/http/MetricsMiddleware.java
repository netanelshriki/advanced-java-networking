package com.network.middleware.http;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.network.api.http.HttpRequestContext;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.HttpMiddleware;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * HTTP middleware that collects metrics for requests and responses.
 * 
 * <p>This middleware collects various metrics using the Dropwizard Metrics library,
 * including request rates, response times, and status code distributions. Metrics
 * are grouped by URI patterns and HTTP methods.
 * 
 * <p>This middleware can be configured with custom metric namers to control how
 * metrics are named and grouped.
 */
public class MetricsMiddleware implements HttpMiddleware {

    private static final String REQUEST_TIMER_ATTR = "metrics.requestTimer";
    private static final String REQUEST_START_TIME_ATTR = "metrics.requestStartTime";
    
    private final MetricRegistry registry;
    private final Function<HttpRequestContext, String> uriGrouper;
    private final String metricPrefix;
    private final Map<Integer, Counter> statusCounters = new HashMap<>();
    private final Map<String, Timer> requestTimers = new HashMap<>();
    private final Map<String, Meter> requestMeters = new HashMap<>();
    private final Map<String, Histogram> requestSizeHistograms = new HashMap<>();
    private final Map<String, Histogram> responseSizeHistograms = new HashMap<>();
    
    /**
     * Creates a new MetricsMiddleware with the specified registry and configuration.
     * 
     * @param builder the builder used to create this middleware
     */
    private MetricsMiddleware(Builder builder) {
        this.registry = builder.registry;
        this.uriGrouper = builder.uriGrouper;
        this.metricPrefix = builder.metricPrefix;
        
        // Create common status code counters
        for (int status : new int[]{200, 201, 204, 301, 302, 304, 400, 401, 403, 404, 500, 503}) {
            statusCounters.put(status, registry.counter(metricPrefix + ".status." + status));
        }
    }
    
    @Override
    public void beforeRequest(HttpRequestContext context) {
        // Start timing the request
        Timer.Context timerContext = getRequestTimer(context).time();
        context.setAttribute(REQUEST_TIMER_ATTR, timerContext);
        context.setAttribute(REQUEST_START_TIME_ATTR, Instant.now());
        
        // Record request size
        if (context.getRequest().getBody() != null) {
            getRequestSizeHistogram(context).update(context.getRequest().getBody().length);
        }
        
        // Record request count
        getRequestMeter(context).mark();
    }
    
    @Override
    public void afterResponse(HttpRequestContext context, HttpResponse response) {
        // Stop request timer and record duration
        if (context.hasAttribute(REQUEST_TIMER_ATTR)) {
            Timer.Context timerContext = (Timer.Context) context.getAttribute(REQUEST_TIMER_ATTR);
            timerContext.stop();
        }
        
        // Record response size
        if (response.getBody() != null) {
            getResponseSizeHistogram(context).update(response.getBody().length);
        }
        
        // Record status code
        Counter statusCounter = statusCounters.get(response.getStatusCode());
        if (statusCounter != null) {
            statusCounter.inc();
        } else {
            // Handle other status codes by group
            int statusGroup = response.getStatusCode() / 100;
            registry.counter(metricPrefix + ".status." + statusGroup + "xx").inc();
        }
        
        // Record specific metrics for different status code ranges
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            registry.meter(metricPrefix + ".status.success").mark();
        } else if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
            registry.meter(metricPrefix + ".status.client-error").mark();
        } else if (response.getStatusCode() >= 500) {
            registry.meter(metricPrefix + ".status.server-error").mark();
        }
        
        // Record specific outcome for common scenarios
        if (response.getStatusCode() == 429) {
            registry.meter(metricPrefix + ".rate-limited").mark();
        } else if (response.getStatusCode() == 408 || response.getStatusCode() == 504) {
            registry.meter(metricPrefix + ".timeout").mark();
        }
    }
    
    /**
     * Gets or creates the timer for the request.
     * 
     * @param context the request context
     * @return the timer
     */
    private Timer getRequestTimer(HttpRequestContext context) {
        String key = getMetricKey(context);
        return requestTimers.computeIfAbsent(key, k -> registry.timer(metricPrefix + ".request-time." + k));
    }
    
    /**
     * Gets or creates the meter for the request.
     * 
     * @param context the request context
     * @return the meter
     */
    private Meter getRequestMeter(HttpRequestContext context) {
        String key = getMetricKey(context);
        return requestMeters.computeIfAbsent(key, k -> registry.meter(metricPrefix + ".request-rate." + k));
    }
    
    /**
     * Gets or creates the histogram for request size.
     * 
     * @param context the request context
     * @return the histogram
     */
    private Histogram getRequestSizeHistogram(HttpRequestContext context) {
        String key = getMetricKey(context);
        return requestSizeHistograms.computeIfAbsent(key, k -> registry.histogram(metricPrefix + ".request-size." + k));
    }
    
    /**
     * Gets or creates the histogram for response size.
     * 
     * @param context the request context
     * @return the histogram
     */
    private Histogram getResponseSizeHistogram(HttpRequestContext context) {
        String key = getMetricKey(context);
        return responseSizeHistograms.computeIfAbsent(key, k -> registry.histogram(metricPrefix + ".response-size." + k));
    }
    
    /**
     * Gets the metric key for the request.
     * 
     * @param context the request context
     * @return the metric key
     */
    private String getMetricKey(HttpRequestContext context) {
        if (uriGrouper != null) {
            return context.getRequest().getMethod() + "." + uriGrouper.apply(context);
        } else {
            return context.getRequest().getMethod().toString();
        }
    }
    
    /**
     * Creates a new builder for creating MetricsMiddleware instances.
     * 
     * @param registry the metric registry to use
     * @return a new builder
     */
    public static Builder builder(MetricRegistry registry) {
        return new Builder(registry);
    }
    
    /**
     * Builder for creating {@link MetricsMiddleware} instances.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Function<HttpRequestContext, String> uriGrouper;
        private String metricPrefix = "http";
        
        /**
         * Creates a new Builder with the specified metric registry.
         * 
         * @param registry the metric registry
         */
        public Builder(MetricRegistry registry) {
            this.registry = registry;
        }
        
        /**
         * Sets a function to group URIs for metrics.
         * 
         * <p>This function will be called for each request to determine the URI group
         * for metrics. If not set, metrics will be grouped by HTTP method only.
         * 
         * @param uriGrouper the URI grouper function
         * @return this builder instance
         */
        public Builder uriGrouper(Function<HttpRequestContext, String> uriGrouper) {
            this.uriGrouper = uriGrouper;
            return this;
        }
        
        /**
         * Sets a regex pattern to group URIs for metrics.
         * 
         * <p>URIs matching the pattern will be grouped using the first capturing group.
         * For example, with pattern {@code "/users/(\\d+)"}, requests to {@code "/users/123"}
         * and {@code "/users/456"} will be grouped as {@code "/users/{id}"}.
         * 
         * @param pattern the regex pattern
         * @param replacement the replacement string
         * @return this builder instance
         */
        public Builder uriPattern(Pattern pattern, String replacement) {
            return uriGrouper(context -> {
                String uri = context.getRequest().getUri().getPath();
                return pattern.matcher(uri).replaceAll(replacement);
            });
        }
        
        /**
         * Sets the metric prefix.
         * 
         * <p>All metrics will be prefixed with this value.
         * 
         * @param metricPrefix the metric prefix
         * @return this builder instance
         */
        public Builder metricPrefix(String metricPrefix) {
            this.metricPrefix = metricPrefix;
            return this;
        }
        
        /**
         * Builds a new {@link MetricsMiddleware} instance with the current settings.
         * 
         * @return a new MetricsMiddleware instance
         */
        public MetricsMiddleware build() {
            return new MetricsMiddleware(this);
        }
    }
}
