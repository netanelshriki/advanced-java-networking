# Class Hierarchy Documentation

This document provides a detailed explanation of the class hierarchy in the Advanced Java Networking Library.

## Table of Contents

- [Overview](#overview)
- [Core Interfaces](#core-interfaces)
- [Protocol-Specific Interfaces](#protocol-specific-interfaces)
- [Abstract Implementations](#abstract-implementations)
- [Concrete Implementations](#concrete-implementations)
- [Supporting Classes](#supporting-classes)
- [Middleware Hierarchy](#middleware-hierarchy)

## Overview

![Class Hierarchy Diagram](class-hierarchy.svg)

The class hierarchy of the Advanced Java Networking Library follows an interface-first design approach, with clear separation between contracts (interfaces) and implementations. This approach facilitates:

- Clean separation of concerns
- Easier testing through interface-based mocking
- Flexibility to switch implementations
- Adherence to SOLID principles

## Core Interfaces

The foundation of the class hierarchy is built upon these core interfaces:

### `NetworkClient`

```java
public interface NetworkClient extends Closeable {
    void connect() throws NetworkException;
    CompletableFuture<Void> connectAsync();
    void disconnect();
    CompletableFuture<Void> disconnectAsync();
    boolean isConnected();
    Connection getConnection();
    NetworkClient addConnectionListener(ConnectionListener listener);
    boolean removeConnectionListener(ConnectionListener listener);
    NetworkClient onConnect(Consumer<Connection> callback);
    NetworkClient onDisconnect(Consumer<Connection> callback);
    NetworkClient onError(Consumer<Throwable> callback);
    NetworkClient withConnectionTimeout(Duration timeout);
    @Override
    void close();
}
```

This interface defines the common lifecycle methods and event handling for all network clients regardless of protocol.

### `Connection`

```java
public interface Connection {
    String getId();
    InetSocketAddress getLocalAddress();
    InetSocketAddress getRemoteAddress();
    boolean isOpen();
    Instant getCreationTime();
    Instant getLastActivityTime();
    Map<String, Object> getAttributes();
    <T> T getAttribute(String name);
    <T> T setAttribute(String name, T value);
    <T> T removeAttribute(String name);
    void close();
}
```

Represents an established connection to a remote endpoint, providing information about the connection and a way to store connection-specific metadata.

### `ConnectionListener`

```java
public interface ConnectionListener {
    void onConnect(Connection connection);
    void onDisconnect(Connection connection);
    void onError(Connection connection, Throwable throwable);
}
```

Event-based interface for connection lifecycle notifications.

## Protocol-Specific Interfaces

Each supported protocol has its own set of interfaces extending the core interfaces:

### HTTP

```java
public interface HttpClient extends NetworkClient {
    URL getBaseUrl();
    Map<String, String> getDefaultHeaders();
    HttpRequestBuilder request();
    HttpAsyncRequestBuilder requestAsync();
    HttpResponse send(HttpRequest request) throws NetworkException;
    CompletableFuture<HttpResponse> sendAsync(HttpRequest request);
}

public interface HttpRequest {
    HttpMethod getMethod();
    URL getUrl();
    Map<String, List<String>> getHeaders();
    byte[] getBody();
    HttpClient getClient();
}

public interface HttpResponse {
    int getStatusCode();
    Map<String, List<String>> getHeaders();
    byte[] getBody();
    <T> T getBodyAs(Class<T> type) throws SerializationException;
    HttpRequest getRequest();
}
```

### TCP

```java
public interface TcpClient extends NetworkClient {
    TcpClient send(byte[] data);
    CompletableFuture<Void> sendAsync(byte[] data);
    TcpSendOperation send(byte[] data, SendOptions options);
    CompletableFuture<TcpSendOperation> sendAsync(byte[] data, SendOptions options);
    TcpClient onData(Consumer<byte[]> callback);
}
```

### UDP

```java
public interface UdpClient extends NetworkClient {
    UdpClient send(byte[] data);
    CompletableFuture<Void> sendAsync(byte[] data);
    UdpSendOperation send(byte[] data, SendOptions options);
    CompletableFuture<UdpSendOperation> sendAsync(byte[] data, SendOptions options);
    UdpClient onData(Consumer<DatagramPacket> callback);
}
```

### WebSocket

```java
public interface WebSocketClient extends NetworkClient {
    WebSocketClient sendText(String message);
    CompletableFuture<Void> sendTextAsync(String message);
    WebSocketClient sendBinary(byte[] data);
    CompletableFuture<Void> sendBinaryAsync(byte[] data);
    WebSocketClient onMessage(Consumer<WebSocketMessage> callback);
    WebSocketClient onClose(BiConsumer<Integer, String> callback);
}
```

## Abstract Implementations

The library provides abstract base classes for common functionality:

### `AbstractNetworkClient`

```java
public abstract class AbstractNetworkClient implements NetworkClient {
    // Common implementation of NetworkClient methods
    // Template methods for protocol-specific behavior
}
```

### Protocol-Specific Abstract Classes

```java
public abstract class AbstractHttpClient extends AbstractNetworkClient implements HttpClient {
    // Common HTTP client implementation
    // Template methods for HTTP-specific operations
}

public abstract class AbstractTcpClient extends AbstractNetworkClient implements TcpClient {
    // Common TCP client implementation
    // Template methods for TCP-specific operations
}
```

## Concrete Implementations

The concrete implementations provide the actual network I/O operations:

### HTTP

```java
public class DefaultHttpClient extends AbstractHttpClient {
    // Implementation using Java's HttpClient
}

public class JettyHttpClient extends AbstractHttpClient {
    // Alternative implementation using Jetty client
}
```

### TCP

```java
public class NettyTcpClient extends AbstractTcpClient {
    // Implementation using Netty
}

public class NioTcpClient extends AbstractTcpClient {
    // Implementation using Java NIO
}
```

### UDP

```java
public class DefaultUdpClient extends AbstractUdpClient {
    // Implementation using Java DatagramSocket
}

public class NettyUdpClient extends AbstractUdpClient {
    // Implementation using Netty
}
```

### WebSocket

```java
public class NettyWebSocketClient extends AbstractWebSocketClient {
    // Implementation using Netty
}

public class StandardWebSocketClient extends AbstractWebSocketClient {
    // Implementation using Java standard API
}
```

## Supporting Classes

### Builders

Each client type has corresponding builder classes:

```java
public interface HttpClientBuilder {
    HttpClientBuilder withBaseUrl(String baseUrl);
    HttpClientBuilder withDefaultHeader(String name, String value);
    HttpClientBuilder withTimeout(Duration timeout);
    HttpClientBuilder withMaxConnections(int maxConnections);
    HttpClientBuilder withMiddleware(HttpMiddleware middleware);
    HttpClient build();
}
```

### Configuration

Configuration classes for each client type:

```java
public interface HttpClientConfig {
    URL getBaseUrl();
    Duration getTimeout();
    int getMaxConnections();
    Map<String, String> getDefaultHeaders();
    List<HttpMiddleware> getMiddleware();
}
```

### Factory

The `NetworkLib` class serves as a factory for all client types:

```java
public final class NetworkLib {
    public static HttpClientBuilder createHttpClient() { /* ... */ }
    public static TcpClientBuilder createTcpClient() { /* ... */ }
    public static UdpClientBuilder createUdpClient() { /* ... */ }
    public static WebSocketClientBuilder createWebSocketClient() { /* ... */ }
}
```

## Middleware Hierarchy

The middleware components follow their own hierarchy:

### Core Middleware Interfaces

```java
public interface Middleware {
    String getName();
    Map<String, Object> getMetadata();
}

public interface HttpMiddleware extends Middleware {
    HttpResponse apply(HttpRequest request, HttpRequestFunction next);
}
```

### Common Middleware Implementations

```java
public class RetryMiddleware implements HttpMiddleware {
    // Implementation of retry logic
}

public class CircuitBreakerMiddleware implements HttpMiddleware {
    // Implementation of circuit breaker pattern
}

public class RateLimitMiddleware implements HttpMiddleware {
    // Implementation of rate limiting
}

public class LoggingMiddleware implements HttpMiddleware {
    // Implementation of request/response logging
}
```

These middleware components can be composed to create sophisticated request/response processing pipelines.