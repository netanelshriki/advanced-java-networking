package com.network.impl.websocket;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.api.websocket.WebSocketClient;
import com.network.api.websocket.WebSocketCloseCode;
import com.network.api.websocket.WebSocketMessage;
import com.network.api.websocket.WebSocketMessageType;
import com.network.exception.NetworkException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Default implementation of the {@link WebSocketClient} interface using
 * the Java 11 {@link java.net.http.WebSocket} API.
 */
public class DefaultWebSocketClient implements WebSocketClient {

    private final URI uri;
    private final Duration connectTimeout;
    private final Map<String, String> headers;
    private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final List<BiConsumer<Connection, WebSocketMessage>> messageCallbacks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    
    private WebSocketConnection connection;
    private WebSocket webSocket;
    private final ExecutorService executor;
    
    /**
     * Creates a new DefaultWebSocketClient with the specified configuration.
     *
     * @param config the WebSocket client configuration
     */
    DefaultWebSocketClient(DefaultWebSocketClientConfig config) {
        this.uri = config.getUri();
        this.connectTimeout = config.getConnectTimeout();
        this.headers = new ConcurrentHashMap<>(config.getHeaders());
        this.executor = config.getExecutor() != null 
                ? config.getExecutor() 
                : Executors.newSingleThreadExecutor(r -> {
                    Thread thread = new Thread(r, "websocket-client-" + uri.getHost());
                    thread.setDaemon(true);
                    return thread;
                });
    }

    @Override
    public void connect() throws NetworkException {
        if (connected.get()) {
            return;
        }
        
        try {
            // Create HTTP client for WebSocket
            HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                    .executor(executor);
            
            if (connectTimeout != null) {
                httpClientBuilder.connectTimeout(connectTimeout);
            }
            
            HttpClient httpClient = httpClientBuilder.build();
            
            // Build WebSocket
            WebSocket.Builder webSocketBuilder = httpClient.newWebSocketBuilder();
            
            // Add headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                webSocketBuilder.header(header.getKey(), header.getValue());
            }
            
            // Create listener
            WebSocketListener listener = new WebSocketListener();
            
            // Connect to WebSocket
            CompletableFuture<WebSocket> future = webSocketBuilder.buildAsync(uri, listener);
            webSocket = future.join();
            
            // Create connection object
            connection = new WebSocketConnection(webSocket);
            
            // Mark as connected
            connected.set(true);
            
            // Notify listeners
            for (ConnectionListener connectionListener : connectionListeners) {
                try {
                    connectionListener.onConnect(connection);
                } catch (Exception e) {
                    // Ignore exceptions from listeners
                }
            }
        } catch (Exception e) {
            throw new NetworkException("Failed to connect to WebSocket", e);
        }
    }

    @Override
    public CompletableFuture<Void> connectAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        executor.submit(() -> {
            try {
                connect();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }

    @Override
    public void disconnect() {
        if (!connected.get()) {
            return;
        }
        
        if (webSocket != null) {
            webSocket.sendClose(WebSocketCloseCode.NORMAL_CLOSURE.getCode(), "Client closing connection");
            webSocket = null;
        }
        
        // Mark as disconnected
        connected.set(false);
        
        // Notify listeners
        Connection connectionCopy = connection;
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onDisconnect(connectionCopy);
            } catch (Exception e) {
                // Ignore exceptions from listeners
            }
        }
        
        connection = null;
    }

    @Override
    public CompletableFuture<Void> disconnectAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        executor.submit(() -> {
            try {
                disconnect();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }

    @Override
    public boolean isConnected() {
        return connected.get() && webSocket != null;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public WebSocketClient addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
        return this;
    }

    @Override
    public boolean removeConnectionListener(ConnectionListener listener) {
        return connectionListeners.remove(listener);
    }

    @Override
    public WebSocketClient onConnect(Consumer<Connection> callback) {
        return addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                callback.accept(connection);
            }
            
            @Override
            public void onDisconnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onError(Throwable throwable) {
                // Do nothing
            }
        });
    }

    @Override
    public WebSocketClient onDisconnect(Consumer<Connection> callback) {
        return addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onDisconnect(Connection connection) {
                callback.accept(connection);
            }
            
            @Override
            public void onError(Throwable throwable) {
                // Do nothing
            }
        });
    }

    @Override
    public WebSocketClient onError(Consumer<Throwable> callback) {
        return addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onDisconnect(Connection connection) {
                // Do nothing
            }
            
            @Override
            public void onError(Throwable throwable) {
                callback.accept(throwable);
            }
        });
    }

    @Override
    public NetworkClient withConnectionTimeout(Duration timeout) {
        if (isConnected()) {
            throw new IllegalStateException("Cannot change connection timeout while connected");
        }
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        
        // Not applicable after construction
        return this;
    }

    @Override
    public void close() {
        disconnect();
        executor.shutdown();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public void send(String message) throws NetworkException {
        if (!isConnected()) {
            throw new NetworkException("Not connected", null);
        }
        
        try {
            webSocket.sendText(message, true);
        } catch (Exception e) {
            throw new NetworkException("Failed to send message", e);
        }
    }

    @Override
    public void send(byte[] message) throws NetworkException {
        if (!isConnected()) {
            throw new NetworkException("Not connected", null);
        }
        
        try {
            webSocket.sendBinary(ByteBuffer.wrap(message), true);
        } catch (Exception e) {
            throw new NetworkException("Failed to send message", e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        executor.submit(() -> {
            try {
                send(message);
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }

    @Override
    public CompletableFuture<Void> sendAsync(byte[] message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        executor.submit(() -> {
            try {
                send(message);
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }

    @Override
    public WebSocketClient onMessage(BiConsumer<Connection, WebSocketMessage> callback) {
        messageCallbacks.add(callback);
        return this;
    }

    @Override
    public WebSocketClient onTextMessage(BiConsumer<Connection, String> callback) {
        return onMessage((conn, message) -> {
            if (message.getType() == WebSocketMessageType.TEXT) {
                callback.accept(conn, message.getTextContent());
            }
        });
    }

    @Override
    public WebSocketClient onBinaryMessage(BiConsumer<Connection, byte[]> callback) {
        return onMessage((conn, message) -> {
            if (message.getType() == WebSocketMessageType.BINARY) {
                callback.accept(conn, message.getBinaryContent());
            }
        });
    }
    
    /**
     * WebSocket connection implementation.
     */
    private static class WebSocketConnection implements Connection {
        private final WebSocket webSocket;
        
        WebSocketConnection(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        @Override
        public boolean isConnected() {
            return webSocket != null;
        }

        @Override
        public void close() {
            if (webSocket != null) {
                webSocket.sendClose(WebSocketCloseCode.NORMAL_CLOSURE.getCode(), "Client closing connection");
            }
        }
    }
    
    /**
     * WebSocket listener implementation.
     */
    private class WebSocketListener implements WebSocket.Listener {
        private StringBuilder textBuilder;
        private ByteBuffer binaryBuffer;
        
        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
        }
        
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            if (textBuilder == null) {
                textBuilder = new StringBuilder();
            }
            
            textBuilder.append(data);
            
            if (last) {
                String message = textBuilder.toString();
                textBuilder = null;
                
                WebSocketMessage wsMessage = new DefaultWebSocketMessage(
                        WebSocketMessageType.TEXT, message, null);
                
                // Notify callbacks
                for (BiConsumer<Connection, WebSocketMessage> callback : messageCallbacks) {
                    try {
                        callback.accept(connection, wsMessage);
                    } catch (Exception e) {
                        // Ignore exceptions from callbacks
                    }
                }
            }
            
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
        
        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            if (binaryBuffer == null) {
                // Create a new buffer with the same capacity
                binaryBuffer = ByteBuffer.allocate(data.remaining());
            } else {
                // Expand the buffer if needed
                if (binaryBuffer.remaining() < data.remaining()) {
                    ByteBuffer temp = ByteBuffer.allocate(binaryBuffer.capacity() + data.remaining());
                    binaryBuffer.flip();
                    temp.put(binaryBuffer);
                    binaryBuffer = temp;
                }
            }
            
            binaryBuffer.put(data);
            
            if (last) {
                binaryBuffer.flip();
                byte[] message = new byte[binaryBuffer.remaining()];
                binaryBuffer.get(message);
                binaryBuffer = null;
                
                WebSocketMessage wsMessage = new DefaultWebSocketMessage(
                        WebSocketMessageType.BINARY, null, message);
                
                // Notify callbacks
                for (BiConsumer<Connection, WebSocketMessage> callback : messageCallbacks) {
                    try {
                        callback.accept(connection, wsMessage);
                    } catch (Exception e) {
                        // Ignore exceptions from callbacks
                    }
                }
            }
            
            return WebSocket.Listener.super.onBinary(webSocket, data, last);
        }
        
        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            return WebSocket.Listener.super.onPing(webSocket, message);
        }
        
        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            return WebSocket.Listener.super.onPong(webSocket, message);
        }
        
        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            if (connected.get()) {
                connected.set(false);
                
                // Notify listeners
                for (ConnectionListener listener : connectionListeners) {
                    try {
                        listener.onDisconnect(connection);
                    } catch (Exception e) {
                        // Ignore exceptions from listeners
                    }
                }
            }
            
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
        
        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            // Notify listeners
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onError(error);
                } catch (Exception e) {
                    // Ignore exceptions from listeners
                }
            }
            
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}
