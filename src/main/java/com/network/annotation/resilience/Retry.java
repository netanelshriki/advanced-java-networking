package com.network.annotation.resilience;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying retry behavior for a method.
 * <p>
 * When applied to a method, it defines the retry policy for that specific request.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GET("/users")
 * @Retry(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
 * List<User> getUsers();
 * }
 * </pre>
 * </p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    /**
     * The maximum number of retry attempts.
     * @return the maximum number of attempts
     */
    int maxAttempts() default 3;
    
    /**
     * The backoff strategy to use between retries.
     * @return the backoff strategy
     */
    Backoff backoff() default @Backoff;
    
    /**
     * Array of exception types that should trigger a retry.
     * <p>
     * If empty, all exceptions will trigger a retry.
     * </p>
     * @return the exception types
     */
    Class<? extends Throwable>[] retryOn() default {};
    
    /**
     * Array of exception types that should not trigger a retry.
     * <p>
     * If empty, all exceptions specified in retryOn will trigger a retry.
     * </p>
     * @return the exception types
     */
    Class<? extends Throwable>[] noRetryOn() default {};
}
