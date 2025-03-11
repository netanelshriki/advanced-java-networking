package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for HTTP PUT methods.
 * <p>
 * When applied to an interface method, it indicates that the method
 * should generate an HTTP PUT request.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @PUT("/users/{id}")
 * User updateUser(@PathVariable("id") String id, @Body User user);
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PUT {
    /**
     * The path of the resource, relative to the base URL.
     * <p>
     * May contain path parameters in the form {name}.
     * </p>
     * @return the path value
     */
    String value() default "";
}
