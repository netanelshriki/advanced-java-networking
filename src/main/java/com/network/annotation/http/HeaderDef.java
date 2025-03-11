package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a header to be used with {@link DefaultHeaders}.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @DefaultHeaders({
 *     @HeaderDef(name = "Accept", value = "application/json"),
 *     @HeaderDef(name = "User-Agent", value = "MyClient/1.0")
 * })
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HeaderDef {
    /**
     * The name of the header.
     * @return the header name
     */
    String name();
    
    /**
     * The value of the header.
     * @return the header value
     */
    String value();
}
