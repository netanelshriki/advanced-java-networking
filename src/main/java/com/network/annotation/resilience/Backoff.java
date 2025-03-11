package com.network.annotation.resilience;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying backoff behavior for retries.
 * <p>
 * When used with {@link Retry}, it defines the backoff strategy between retry attempts.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Backoff {
    /**
     * The initial delay in milliseconds.
     * @return the initial delay
     */
    long delay() default 500;
    
    /**
     * The multiplier to apply to the delay after each retry attempt.
     * @return the multiplier
     */
    double multiplier() default 1.5;
    
    /**
     * The maximum delay in milliseconds.
     * @return the maximum delay
     */
    long maxDelay() default 30000;
    
    /**
     * Whether to use random jitter to avoid thundering herd issues.
     * @return true if jitter should be applied, false otherwise
     */
    boolean jitter() default true;
}
