package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying request timeouts.
 * <p>
 * When applied to a method, it defines the timeout for that specific request.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GET("/users")
 * @Timeout(5000)
 * List<User> getUsers();
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timeout {
    /**
     * The timeout value in milliseconds.
     * @return the timeout value
     */
    long value();
}
