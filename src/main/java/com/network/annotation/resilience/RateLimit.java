package com.network.annotation.resilience;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying rate limiting to a method.
 * <p>
 * When applied to a method, it defines the rate limiting policy for that specific request.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GET("/users")
 * @RateLimit(permits = 100, timeUnit = TimeUnit.MINUTES)
 * List<User> getUsers();
 * }
 * </pre>
 * </p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * The number of permitted requests.
     * @return the number of permits
     */
    long permits() default 100;
    
    /**
     * The time window in milliseconds.
     * @return the time window
     */
    long timeWindowMs() default 60000; // 1 minute
    
    /**
     * The behavior when the rate limit is exceeded.
     * @return the rate limit behavior
     */
    RateLimitBehavior behavior() default RateLimitBehavior.THROW_EXCEPTION;
    
    /**
     * Enum for specifying the behavior when a rate limit is exceeded.
     */
    enum RateLimitBehavior {
        /**
         * Throw an exception when the rate limit is exceeded.
         */
        THROW_EXCEPTION,
        
        /**
         * Block until a permit becomes available.
         */
        BLOCK_UNTIL_AVAILABLE
    }
}
