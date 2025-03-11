package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for HTTP client interfaces.
 * <p>
 * When applied to an interface, it indicates that the interface should be
 * treated as an HTTP client, and methods should be processed according to
 * their HTTP method annotations.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @HttpClient(baseUrl = "https://api.example.com")
 * public interface UserService {
 *     @GET("/users/{id}")
 *     User getUser(@PathVariable("id") String userId);
 * }
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpClient {
    /**
     * The base URL for all requests made by this client.
     * @return the base URL
     */
    String baseUrl() default "";
    
    /**
     * The connection timeout in milliseconds.
     * @return the connection timeout
     */
    long connectionTimeout() default 30000;
    
    /**
     * The read timeout in milliseconds.
     * @return the read timeout
     */
    long readTimeout() default 30000;
    
    /**
     * Whether to follow redirects.
     * @return true if redirects should be followed, false otherwise
     */
    boolean followRedirects() default true;
}
