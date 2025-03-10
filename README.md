# Advanced Java Networking Library

An enterprise-grade Java networking library providing robust, scalable, and feature-rich networking capabilities with a fluent API design.

## Overview

This library implements modern design patterns and best practices to offer a comprehensive networking solution for Java applications. It supports multiple protocols (HTTP, TCP, UDP, WebSocket) with a consistent API pattern, advanced features like circuit breaking, rate limiting, and extensive monitoring capabilities.

## Features

- **Multi-protocol support**: HTTP, WebSockets, TCP, and UDP
- **Fluent API design**: Intuitive builder patterns for easy configuration
- **Resilience patterns**: Circuit breakers, retry mechanisms, timeouts, backoff strategies
- **Async operations**: Non-blocking I/O with CompletableFuture
- **Middleware architecture**: Extensible request/response processing pipeline
- **Advanced monitoring**: Metrics, logging, tracing integration
- **Protocol adapters**: Seamless protocol switching
- **Secure by default**: TLS/SSL integration, robust authentication
- **Efficient resource management**: Connection pooling, keepalives
- **Serialization framework**: Pluggable formats (JSON, Protocol Buffers, etc.)

## Design Principles

- SOLID principles
- Composition over inheritance
- Interface-based design
- Immutability where appropriate
- Consistent error handling
- Comprehensive testing

## Getting Started

Add the library to your Maven project:

```xml
<dependency>
    <groupId>com.network</groupId>
    <artifactId>advanced-networking</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Examples

### HTTP Client Example

```java
// Create a client with default settings
HttpClient client = NetworkLib.createHttpClient()
    .withBaseUrl("https://api.example.com")
    .withTimeout(Duration.ofSeconds(30))
    .withRetry(RetryPolicy.builder()
        .maxAttempts(3)
        .exponentialBackoff(Duration.ofMillis(100))
        .build())
    .build();

// Make a request
HttpResponse<User> response = client.request()
    .path("/users/{id}")
    .pathParam("id", "123")
    .header("Authorization", "Bearer token")
    .get()
    .deserializeAs(User.class)
    .execute();

// Or with async API
CompletableFuture<HttpResponse<User>> future = client.requestAsync()
    .path("/users")
    .post()
    .body(new User("John", "Doe"))
    .deserializeAs(User.class)
    .execute();
```

### TCP Socket Example

```java
// Create a TCP client
TcpClient client = NetworkLib.createTcpClient()
    .withAddress("example.com", 9000)
    .withConnectionTimeout(Duration.ofSeconds(5))
    .withKeepAlive(true)
    .build();

// Send data and receive response
byte[] response = client.send(data)
    .expectReply()
    .withTimeout(Duration.ofSeconds(10))
    .execute();

// Register event handlers
client.onConnect(conn -> logger.info("Connected to server"))
    .onDisconnect(reason -> logger.warn("Disconnected: {}", reason))
    .onError(ex -> logger.error("Error occurred", ex));
```

## Architecture

The library is designed with a layered architecture:

1. **Core API Layer**: Interfaces defining the networking contracts
2. **Protocol Implementations**: Concrete implementations for each protocol
3. **Middleware Layer**: Pluggable components for cross-cutting concerns
4. **Utility Layer**: Support classes and helpers

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.