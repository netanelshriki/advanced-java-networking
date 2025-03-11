package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for binding method parameters to HTTP headers.
 * <p>
 * When applied to a method parameter, it binds the parameter value to an HTTP header
 * with the same name or specified by the value attribute.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GET("/users")
 * List<User> getUsers(@Header("Authorization") String token);
 * }
 * </pre>
 * </p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
    /**
     * The name of the header to bind to.
     * <p>
     * If not specified, the parameter name will be used.
     * </p>
     * @return the header name
     */
    String value() default "";
}
