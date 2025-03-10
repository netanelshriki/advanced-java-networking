# Network Library Examples

This directory contains examples of how to use the networking library.

## HTTP Client Example

Here's a basic example of using the HTTP client:

```java
import com.network.NetworkLib;
import com.network.api.http.HttpClient;
import com.network.api.http.HttpResponse;
import com.network.api.http.middleware.LoggingMiddleware;
import com.network.serialization.JsonSerializer;

import java.util.Map;

public class HttpExample {
    
    public static void main(String[] args) {
        // Create a client with default settings
        HttpClient client = NetworkLib.createHttpClient()
            .withBaseUrl("https://api.example.com")
            .withTimeout(Duration.ofSeconds(30))
            .withRetry(3, RetryBackoffStrategy.EXPONENTIAL)
            .withSerializer(new JsonSerializer())
            .withMiddleware(LoggingMiddleware.builder()
                .level(LogLevel.INFO)
                .logHeaders(true)
                .build())
            .build();
        
        // Make a GET request
        HttpResponse<User> response = client.request()
            .path("/users/{id}")
            .pathParam("id", "123")
            .header("Authorization", "Bearer token")
            .get()
            .deserializeAs(User.class)
            .execute();
        
        // Access the response data
        if (response.isSuccessful()) {
            User user = response.getBody();
            System.out.println("User name: " + user.getName());
        } else {
            System.err.println("Error: " + response.getStatusCode() + " " + response.getStatusMessage());
        }
        
        // Make a POST request
        User newUser = new User("John", "Doe");
        
        HttpResponse<User> postResponse = client.request()
            .path("/users")
            .contentType("application/json")
            .body(newUser)
            .post()
            .deserializeAs(User.class)
            .execute();
        
        // Process the response
        if (postResponse.isSuccessful()) {
            User createdUser = postResponse.getBody();
            System.out.println("Created user with ID: " + createdUser.getId());
        }
        
        // Make an asynchronous request
        client.requestAsync()
            .path("/users")
            .get()
            .deserializeAs(User[].class)
            .execute()
            .thenAccept(usersResponse -> {
                if (usersResponse.isSuccessful()) {
                    User[] users = usersResponse.getBody();
                    System.out.println("Got " + users.length + " users");
                }
            })
            .exceptionally(ex -> {
                System.err.println("Error: " + ex.getMessage());
                return null;
            });
    }
    
    // Example model class
    public static class User {
        private String id;
        private String name;
        private String email;
        
        public User() {
            // Default constructor for deserialization
        }
        
        public User(String firstName, String lastName) {
            this.name = firstName + " " + lastName;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
```

## TCP Client Example

Here's a basic example of using the TCP client:

```java
import com.network.NetworkLib;
import com.network.api.tcp.TcpClient;
import com.network.api.connection.Connection;

public class TcpExample {
    
    public static void main(String[] args) {
        // Create a TCP client
        TcpClient client = NetworkLib.createTcpClient()
            .withAddress("example.com", 9000)
            .withConnectionTimeout(Duration.ofSeconds(5))
            .withKeepAlive(true)
            .withTcpNoDelay(true)
            .withAutoReconnect(true)
            .build();
        
        // Register event handlers
        client.onConnect(conn -> System.out.println("Connected to server"))
              .onDisconnect(conn -> System.out.println("Disconnected from server"))
              .onError(ex -> System.err.println("Error: " + ex.getMessage()))
              .onDataReceived((conn, data) -> {
                  System.out.println("Received " + data.length + " bytes");
                  // Process received data
                  String message = new String(data, StandardCharsets.UTF_8);
                  System.out.println("Message: " + message);
              });
        
        // Connect to the server
        client.connect();
        
        // Send data
        String message = "Hello, server!";
        client.send(message.getBytes(StandardCharsets.UTF_8));
        
        // Send data and receive response
        byte[] response = client.sendAndReceive(message.getBytes(StandardCharsets.UTF_8));
        System.out.println("Response: " + new String(response, StandardCharsets.UTF_8));
        
        // Disconnect when done
        client.disconnect();
    }
}
```