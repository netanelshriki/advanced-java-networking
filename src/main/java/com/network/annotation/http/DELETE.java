package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for HTTP DELETE methods.
 * <p>
 * When applied to an interface method, it indicates that the method
 * should generate an HTTP DELETE request.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @DELETE("/users/{id}")
 * void deleteUser(@PathVariable("id") String id);
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DELETE {
    /**
     * The path of the resource, relative to the base URL.
     * <p>
     * May contain path parameters in the form {name}.
     * </p>
     * @return the path value
     */
    String value() default "";
}
