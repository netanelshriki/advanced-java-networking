package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for binding method parameters to query parameters.
 * <p>
 * When applied to a method parameter, it binds the parameter value to a query parameter
 * with the same name or specified by the value attribute.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @GET("/users")
 * List<User> searchUsers(@RequestParam("query") String searchTerm, @RequestParam("page") int page);
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    /**
     * The name of the query parameter to bind to.
     * <p>
     * If not specified, the parameter name will be used.
     * </p>
     * @return the query parameter name
     */
    String value() default "";
    
    /**
     * Whether the parameter is required.
     * <p>
     * If true and the value is null, an exception will be thrown.
     * </p>
     * @return true if the parameter is required, false otherwise
     */
    boolean required() default true;
}
