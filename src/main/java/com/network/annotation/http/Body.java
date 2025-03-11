package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for binding method parameters to the request body.
 * <p>
 * When applied to a method parameter, it indicates that the parameter value
 * should be serialized and sent as the request body.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @POST("/users")
 * User createUser(@Body User user);
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {
}
