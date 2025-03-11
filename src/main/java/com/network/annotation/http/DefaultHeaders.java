package com.network.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying default headers for an HTTP client interface.
 * <p>
 * When applied to an interface, it defines default headers that will be
 * applied to all requests made by the client.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @HttpClient(baseUrl = "https://api.example.com")
 * @DefaultHeaders({
 *     @Header(name = "Accept", value = "application/json"),
 *     @Header(name = "User-Agent", value = "MyClient/1.0")
 * })
 * public interface UserService {
 *     @GET("/users")
 *     List<User> getUsers();
 * }
 * }
 * </pre>
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultHeaders {
    /**
     * The headers to apply to all requests.
     * @return the headers
     */
    HeaderDef[] value();
}
