package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for binding method parameters to path variables in URL templates.
 * <p>
 * When applied to a method parameter, it binds the parameter value to a path variable
 * with the same name or specified by the value attribute.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GET("/users/{id}")
 * User getUser(@PathVariable("id") String userId);
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    /**
     * The name of the path variable to bind to.
     * <p>
     * If not specified, the parameter name will be used.
     * </p>
     * @return the path variable name
     */
    String value() default "";
}
