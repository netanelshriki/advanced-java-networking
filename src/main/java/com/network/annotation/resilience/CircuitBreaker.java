package com.network.annotation.resilience;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying a circuit breaker pattern to a method.
 * <p>
 * When applied to a method, it defines the circuit breaker policy for that specific request.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GET("/users")
 * @CircuitBreaker(failureThreshold = 5, resetTimeout = 30000)
 * List<User> getUsers();
 * }
 * </pre>
 * </p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CircuitBreaker {
    /**
     * The number of failures that must occur before the circuit opens.
     * @return the failure threshold
     */
    int failureThreshold() default 5;
    
    /**
     * The time in milliseconds after which the circuit will transition from open to half-open.
     * @return the reset timeout
     */
    long resetTimeout() default 30000;
    
    /**
     * The number of successful executions required to close the circuit when in half-open state.
     * @return the success threshold
     */
    int successThreshold() default 1;
    
    /**
     * Array of exception types that should be considered as failures.
     * <p>
     * If empty, all exceptions will be considered as failures.
     * </p>
     * @return the exception types
     */
    Class<? extends Throwable>[] failOn() default {};
    
    /**
     * Array of exception types that should not be considered as failures.
     * <p>
     * If empty, all exceptions specified in failOn will be considered as failures.
     * </p>
     * @return the exception types
     */
    Class<? extends Throwable>[] ignoreOn() default {};
}
