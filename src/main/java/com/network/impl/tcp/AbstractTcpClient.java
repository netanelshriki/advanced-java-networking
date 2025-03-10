package com.network.impl.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.api.tcp.TcpClient;
import com.network.api.tcp.TcpClientConfig;
import com.network.config.NetworkConfig.RetryBackoffStrategy;
import com.network.exception.ConnectionException;
import com.network.exception.NetworkException;
import com.network.exception.NetworkException.ErrorCode;
import com.network.exception.TimeoutException;
import com.network.serialization.Serializer;

/**
 * Abstract base implementation of {@link TcpClient}.
 * 
 * <p>This class provides common functionality for TCP client implementations,
 * such as connection management, event handling, and reconnection logic.
 * 
 * <p>Concrete implementations need to provide the actual network I/O operations,
 * such as connecting, sending, and receiving data.
 */
public abstract class AbstractTcpClient implements TcpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractTcpClient.class);
    
    protected final String clientId;
    protected final TcpClientConfig config;
    protected final List<ConnectionListener> connectionListeners;
    protected final AtomicBoolean connected;
    protected final AtomicInteger reconnectAttempts;
    protected final ReentrantLock connectionLock;
    protected final ExecutorService executor;
    protected final ScheduledExecutorService scheduler;
    
    protected Connection connection;
    protected ScheduledFuture<?> reconnectFuture;
    
    /**
     * Creates a new TCP client with the specified configuration.
     * 
     * @param config the client configuration
     */
    protected AbstractTcpClient(TcpClientConfig config) {
        this.clientId = UUID.randomUUID().toString();
        this.config = config;
        this.connectionListeners = new CopyOnWriteArrayList<>();
        this.connected = new AtomicBoolean(false);
        this.reconnectAttempts = new AtomicInteger(0);
        this.connectionLock = new ReentrantLock();
        this.executor = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Auto-connect if enabled
        if (config.isAutoConnectEnabled()) {
            try {
                connect();
            } catch (NetworkException e) {
                logger.error("Failed to auto-connect: {}", e.getMessage());
            }
        }
    }
    
    @Override
    public void connect() throws NetworkException {
        connectionLock.lock();
        try {
            if (connected.get()) {
                return;
            }
            
            doConnect();
            connected.set(true);
            reconnectAttempts.set(0);
            
            logger.debug("Connected to {}", config.getRemoteAddress());
        } catch (Exception e) {
            // Handle connection failure
            handleConnectionFailure(e);
            throw translateException(e);
        } finally {
            connectionLock.unlock();
        }
    }
    
    @Override
    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                connect();
            } catch (NetworkException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    @Override
    public void disconnect() {
        connectionLock.lock();
        try {
            if (!connected.get()) {
                return;
            }
            
            // Cancel any pending reconnect
            cancelReconnect();
            
            // Disconnect
            try {
                doDisconnect();
            } catch (Exception e) {
                logger.error("Error during disconnect: {}", e.getMessage(), e);
            } finally {
                connected.set(false);
                connection = null;
                
                logger.debug("Disconnected from {}", config.getRemoteAddress());
            }
        } finally {
            connectionLock.unlock();
        }
    }
    
    @Override
    public CompletableFuture<Void> disconnectAsync() {
        return CompletableFuture.runAsync(this::disconnect, executor);
    }
    
    @Override
    public boolean isConnected() {
        return connected.get() && connection != null && connection.isOpen();
    }
    
    @Override
    public Connection getConnection() {
        return connection;
    }
    
    @Override
    public InetSocketAddress getRemoteAddress() {
        return config.getRemoteAddress();
    }
    
    @Override
    public InetSocketAddress getLocalAddress() {
        return config.getLocalAddress().orElse(null);
    }
    
    @Override
    public void send(byte[] data) throws NetworkException {
        if (!isConnected()) {
            throw new ConnectionException(ErrorCode.CONNECTION_CLOSED, "Not connected");
        }
        
        try {
            connection.send(data);
        } catch (Exception e) {
            handleSendFailure(e, data);
            throw translateException(e);
        }
    }
    
    @Override
    public CompletableFuture<Void> sendAsync(byte[] data) {
        if (!isConnected()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new ConnectionException(ErrorCode.CONNECTION_CLOSED, "Not connected"));
            return future;
        }
        
        return connection.sendAsync(data)
            .exceptionally(e -> {
                handleSendFailure(e, data);
                throw translateException(e);
            });
    }
    
    @Override
    public byte[] sendAndReceive(byte[] data) throws NetworkException {
        if (!isConnected()) {
            throw new ConnectionException(ErrorCode.CONNECTION_CLOSED, "Not connected");
        }
        
        CompletableFuture<byte[]> responseFuture = new CompletableFuture<>();
        
        // Register one-time response handler
        ConnectionListener responseListener = new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                // Not interested in connect events
            }
            
            @Override
            public void onDisconnect(Connection connection, String reason) {
                // Complete exceptionally if disconnected before response
                if (!responseFuture.isDone()) {
                    responseFuture.completeExceptionally(
                        new ConnectionException(ErrorCode.CONNECTION_CLOSED, "Disconnected before response received"));
                }
            }
            
            @Override
            public void onDataReceived(Connection connection, byte[] responseData) {
                // Complete the future with the response
                responseFuture.complete(responseData);
                
                // Remove this listener
                removeConnectionListener(this);
            }
            
            @Override
            public void onDataSent(Connection connection, int size) {
                // Not interested in data sent events
            }
            
            @Override
            public void onError(Connection connection, Throwable throwable) {
                // Complete exceptionally if error occurs
                if (!responseFuture.isDone()) {
                    responseFuture.completeExceptionally(throwable);
                }
            }
        };
        
        addConnectionListener(responseListener);
        
        try {
            // Send the data
            send(data);
            
            // Wait for response with timeout
            try {
                return responseFuture.get(config.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new TimeoutException("sendAndReceive", config.getReadTimeout());
            } catch (Exception e) {
                throw translateException(e);
            }
        } finally {
            // Ensure listener is removed
            removeConnectionListener(responseListener);
        }
    }
    
    @Override
    public CompletableFuture<byte[]> sendAndReceiveAsync(byte[] data) {
        if (!isConnected()) {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(new ConnectionException(ErrorCode.CONNECTION_CLOSED, "Not connected"));
            return future;
        }
        
        CompletableFuture<byte[]> responseFuture = new CompletableFuture<>();
        
        // Register one-time response handler
        ConnectionListener responseListener = new ConnectionListener() {
            @Override
            public void onConnect(Connection connection) {
                // Not interested in connect events
            }
            
            @Override
            public void onDisconnect(Connection connection, String reason) {
                // Complete exceptionally if disconnected before response
                if (!responseFuture.isDone()) {
                    responseFuture.completeExceptionally(
                        new ConnectionException(ErrorCode.CONNECTION_CLOSED, "Disconnected before response received"));
                }
            }
            
            @Override
            public void onDataReceived(Connection connection, byte[] responseData) {
                // Complete the future with the response
                responseFuture.complete(responseData);
                
                // Remove this listener
                removeConnectionListener(this);
            }
            
            @Override
            public void onDataSent(Connection connection, int size) {
                // Not interested in data sent events
            }
            
            @Override
            public void onError(Connection connection, Throwable throwable) {
                // Complete exceptionally if error occurs
                if (!responseFuture.isDone()) {
                    responseFuture.completeExceptionally(throwable);
                }
            }
        };
        
        addConnectionListener(responseListener);
        
        // Set up timeout
        ScheduledFuture<?> timeoutFuture = scheduler.schedule(() -> {
            if (!responseFuture.isDone()) {
                responseFuture.completeExceptionally(
                    new TimeoutException("sendAndReceiveAsync", config.getReadTimeout()));
                removeConnectionListener(responseListener);
            }
        }, config.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS);
        
        // Send the data
        sendAsync(data)
            .exceptionally(e -> {
                // Complete exceptionally if send fails
                if (!responseFuture.isDone()) {
                    responseFuture.completeExceptionally(e);
                    removeConnectionListener(responseListener);
                    timeoutFuture.cancel(false);
                }
                return null;
            });
        
        // Add callback to clean up timeout when future completes
        responseFuture.whenComplete((result, ex) -> {
            timeoutFuture.cancel(false);
        });
        
        return responseFuture;
    }
    
    @Override
    public TcpClient addConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.add(listener);
        }
        return this;
    }
    
    @Override
    public boolean removeConnectionListener(ConnectionListener listener) {
        return connectionListeners.remove(listener);
    }
    
    @Override
    public TcpClient onConnect(Consumer<Connection> callback) {
        if (callback != null) {
            addConnectionListener(new ConnectionListener() {
                @Override
                public void onConnect(Connection connection) {
                    callback.accept(connection);
                }
                
                @Override
                public void onDisconnect(Connection connection, String reason) {
                    // Not interested
                }
                
                @Override
                public void onDataReceived(Connection connection, byte[] data) {
                    // Not interested
                }
                
                @Override
                public void onDataSent(Connection connection, int size) {
                    // Not interested
                }
                
                @Override
                public void onError(Connection connection, Throwable throwable) {
                    // Not interested
                }
            });
        }
        return this;
    }
    
    @Override
    public TcpClient onDisconnect(Consumer<Connection> callback) {
        if (callback != null) {
            addConnectionListener(new ConnectionListener() {
                @Override
                public void onConnect(Connection connection) {
                    // Not interested
                }
                
                @Override
                public void onDisconnect(Connection connection, String reason) {
                    callback.accept(connection);
                }
                
                @Override
                public void onDataReceived(Connection connection, byte[] data) {
                    // Not interested
                }
                
                @Override
                public void onDataSent(Connection connection, int size) {
                    // Not interested
                }
                
                @Override
                public void onError(Connection connection, Throwable throwable) {
                    // Not interested
                }
            });
        }
        return this;
    }
    
    @Override
    public TcpClient onError(Consumer<Throwable> callback) {
        if (callback != null) {
            addConnectionListener(new ConnectionListener() {
                @Override
                public void onConnect(Connection connection) {
                    // Not interested
                }
                
                @Override
                public void onDisconnect(Connection connection, String reason) {
                    // Not interested
                }
                
                @Override
                public void onDataReceived(Connection connection, byte[] data) {
                    // Not interested
                }
                
                @Override
                public void onDataSent(Connection connection, int size) {
                    // Not interested
                }
                
                @Override
                public void onError(Connection connection, Throwable throwable) {
                    callback.accept(throwable);
                }
            });
        }
        return this;
    }
    
    @Override
    public TcpClient onDataReceived(BiConsumer<Connection, byte[]> callback) {
        if (callback != null) {
            addConnectionListener(new ConnectionListener() {
                @Override
                public void onConnect(Connection connection) {
                    // Not interested
                }
                
                @Override
                public void onDisconnect(Connection connection, String reason) {
                    // Not interested
                }
                
                @Override
                public void onDataReceived(Connection connection, byte[] data) {
                    callback.accept(connection, data);
                }
                
                @Override
                public void onDataSent(Connection connection, int size) {
                    // Not interested
                }
                
                @Override
                public void onError(Connection connection, Throwable throwable) {
                    // Not interested
                }
            });
        }
        return this;
    }
    
    @Override
    public TcpClient withConnectionTimeout(Duration timeout) {
        // Already connected, can't change timeout
        if (isConnected()) {
            throw new IllegalStateException("Cannot change timeout while connected");
        }
        
        // Delegate to builder and rebuild
        return config.toBuilder()
            .withConnectionTimeout(timeout)
            .build();
    }
    
    @Override
    public TcpClient withKeepAlive(boolean keepAlive) {
        // Already connected, can't change keep-alive
        if (isConnected()) {
            throw new IllegalStateException("Cannot change keep-alive while connected");
        }
        
        // Delegate to builder and rebuild
        return config.toBuilder()
            .withKeepAlive(keepAlive)
            .build();
    }
    
    @Override
    public TcpClient withTcpNoDelay(boolean tcpNoDelay) {
        // Already connected, can't change TCP no delay
        if (isConnected()) {
            throw new IllegalStateException("Cannot change TCP no delay while connected");
        }
        
        // Delegate to builder and rebuild (not directly exposed in NetworkConfigBuilder)
        TcpClientBuilder builder = config.toBuilder();
        builder.withTcpNoDelay(tcpNoDelay);
        return builder.build();
    }
    
    @Override
    public TcpClient withLinger(int linger) {
        // Already connected, can't change linger
        if (isConnected()) {
            throw new IllegalStateException("Cannot change linger while connected");
        }
        
        // Delegate to builder and rebuild
        TcpClientBuilder builder = config.toBuilder();
        builder.withLinger(linger);
        return builder.build();
    }
    
    @Override
    public TcpClient withReceiveBufferSize(int size) {
        // Already connected, can't change buffer size
        if (isConnected()) {
            throw new IllegalStateException("Cannot change receive buffer size while connected");
        }
        
        // Delegate to builder and rebuild
        TcpClientBuilder builder = config.toBuilder();
        builder.withReceiveBufferSize(size);
        return builder.build();
    }
    
    @Override
    public TcpClient withSendBufferSize(int size) {
        // Already connected, can't change buffer size
        if (isConnected()) {
            throw new IllegalStateException("Cannot change send buffer size while connected");
        }
        
        // Delegate to builder and rebuild
        TcpClientBuilder builder = config.toBuilder();
        builder.withSendBufferSize(size);
        return builder.build();
    }
    
    @Override
    public void close() {
        disconnect();
        
        // Shut down executors
        executor.shutdown();
        scheduler.shutdown();
        
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            scheduler.shutdownNow();
        }
    }
    
    /**
     * Performs the actual connection to the remote endpoint.
     * 
     * <p>This method is called by {@link #connect()} and should be implemented
     * by concrete subclasses to establish the network connection.
     * 
     * @throws IOException if an I/O error occurs
     * @throws Exception if any other error occurs
     */
    protected abstract void doConnect() throws Exception;
    
    /**
     * Performs the actual disconnection from the remote endpoint.
     * 
     * <p>This method is called by {@link #disconnect()} and should be implemented
     * by concrete subclasses to close the network connection.
     * 
     * @throws IOException if an I/O error occurs
     * @throws Exception if any other error occurs
     */
    protected abstract void doDisconnect() throws Exception;
    
    /**
     * Notifies all registered connection listeners of a connect event.
     * 
     * @param connection the connection that was established
     */
    protected void notifyConnect(Connection connection) {
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onConnect(connection);
            } catch (Exception e) {
                logger.error("Error notifying listener of connect event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Notifies all registered connection listeners of a disconnect event.
     * 
     * @param connection the connection that was closed
     * @param reason the reason for disconnection, or null if not available
     */
    protected void notifyDisconnect(Connection connection, String reason) {
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onDisconnect(connection, reason);
            } catch (Exception e) {
                logger.error("Error notifying listener of disconnect event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Notifies all registered connection listeners of a data received event.
     * 
     * @param connection the connection that received data
     * @param data the received data
     */
    protected void notifyDataReceived(Connection connection, byte[] data) {
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onDataReceived(connection, data);
            } catch (Exception e) {
                logger.error("Error notifying listener of data received event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Notifies all registered connection listeners of a data sent event.
     * 
     * @param connection the connection that sent data
     * @param size the size of data sent in bytes
     */
    protected void notifyDataSent(Connection connection, int size) {
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onDataSent(connection, size);
            } catch (Exception e) {
                logger.error("Error notifying listener of data sent event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Notifies all registered connection listeners of an error event.
     * 
     * @param connection the connection where the error occurred
     * @param throwable the error that occurred
     */
    protected void notifyError(Connection connection, Throwable throwable) {
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onError(connection, throwable);
            } catch (Exception e) {
                logger.error("Error notifying listener of error event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Handles a connection failure.
     * 
     * <p>This method is called when a connection attempt fails. It may
     * initiate a reconnect attempt if auto-reconnect is enabled.
     * 
     * @param e the exception that caused the failure
     */
    protected void handleConnectionFailure(Throwable e) {
        logger.error("Connection failure: {}", e.getMessage(), e);
        
        // Notify error
        if (connection != null) {
            notifyError(connection, e);
        }
        
        // Start reconnect if enabled
        if (config.isAutoReconnectEnabled()) {
            scheduleReconnect();
        }
    }
    
    /**
     * Handles a send failure.
     * 
     * <p>This method is called when a send operation fails. It may
     * initiate a reconnect attempt if the connection is lost.
     * 
     * @param e the exception that caused the failure
     * @param data the data that was being sent
     */
    protected void handleSendFailure(Throwable e, byte[] data) {
        logger.error("Send failure: {}", e.getMessage(), e);
        
        // Notify error
        if (connection != null) {
            notifyError(connection, e);
        }
        
        // Check if connection is lost
        if (e instanceof ConnectionException || e.getCause() instanceof ConnectionException) {
            connected.set(false);
            
            // Start reconnect if enabled
            if (config.isAutoReconnectEnabled()) {
                scheduleReconnect();
            }
        }
    }
    
    /**
     * Schedules a reconnect attempt.
     * 
     * <p>This method is called when a connection is lost and auto-reconnect
     * is enabled. It schedules a reconnect attempt after the appropriate
     * backoff delay.
     */
    protected void scheduleReconnect() {
        connectionLock.lock();
        try {
            // Check if already reconnecting
            if (reconnectFuture != null && !reconnectFuture.isDone()) {
                return;
            }
            
            // Check max attempts
            int maxAttempts = config.getMaxReconnectAttempts();
            int attempts = reconnectAttempts.incrementAndGet();
            
            if (maxAttempts > 0 && attempts > maxAttempts) {
                logger.info("Maximum reconnect attempts ({}) reached, giving up", maxAttempts);
                return;
            }
            
            // Calculate backoff delay
            Duration delay = calculateReconnectDelay(attempts);
            
            logger.info("Scheduling reconnect attempt {} in {}ms", attempts, delay.toMillis());
            
            // Schedule reconnect
            reconnectFuture = scheduler.schedule(() -> {
                try {
                    logger.info("Attempting reconnect #{}", attempts);
                    connect();
                    logger.info("Reconnect successful");
                } catch (Exception e) {
                    logger.error("Reconnect attempt failed: {}", e.getMessage());
                    // Schedule next attempt
                    scheduleReconnect();
                }
            }, delay.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            connectionLock.unlock();
        }
    }
    
    /**
     * Cancels any pending reconnect attempt.
     */
    protected void cancelReconnect() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
    }
    
    /**
     * Calculates the backoff delay for a reconnect attempt.
     * 
     * @param attempt the reconnect attempt number (1-based)
     * @return the backoff delay
     */
    protected Duration calculateReconnectDelay(int attempt) {
        Duration initialBackoff = config.getInitialReconnectBackoff();
        Duration maxBackoff = config.getMaxReconnectBackoff();
        RetryBackoffStrategy strategy = config.getReconnectBackoffStrategy();
        
        Duration delay;
        
        switch (strategy) {
            case NONE:
                delay = initialBackoff;
                break;
            case FIXED:
                delay = initialBackoff;
                break;
            case LINEAR:
                delay = initialBackoff.multipliedBy(attempt);
                break;
            case EXPONENTIAL:
                // Use exponential backoff with base 2: initial * 2^(attempt-1)
                delay = initialBackoff.multipliedBy((long) Math.pow(2, attempt - 1));
                break;
            case RANDOM:
                // Random between 50% and 150% of the initial value
                long baseMillis = initialBackoff.toMillis();
                long randomMillis = (long) (baseMillis * (0.5 + Math.random()));
                delay = Duration.ofMillis(randomMillis);
                break;
            default:
                delay = initialBackoff;
                break;
        }
        
        // Cap at max backoff if specified
        if (maxBackoff != null && !maxBackoff.isZero() && delay.compareTo(maxBackoff) > 0) {
            delay = maxBackoff;
        }
        
        return delay;
    }
    
    /**
     * Translates an exception to a {@link NetworkException}.
     * 
     * <p>This method is used to ensure that all exceptions thrown by the client
     * are of type {@link NetworkException}.
     * 
     * @param e the exception to translate
     * @return the translated exception
     */
    protected NetworkException translateException(Throwable e) {
        if (e instanceof NetworkException) {
            return (NetworkException) e;
        }
        
        if (e instanceof IOException) {
            return new ConnectionException(ErrorCode.IO_ERROR, e);
        }
        
        if (e instanceof java.util.concurrent.TimeoutException) {
            return new TimeoutException("operation", config.getConnectionTimeout(), e);
        }
        
        if (e instanceof RuntimeException) {
            return new NetworkException(ErrorCode.UNKNOWN, e);
        }
        
        return new NetworkException(ErrorCode.UNKNOWN, e);
    }
    
    /**
     * Gets the serializer to use for data conversion.
     * 
     * @return the serializer, or null if not available
     */
    protected Serializer getSerializer() {
        return config.getSerializer().orElse(null);
    }
    
    /**
     * Gets the charset to use for string conversions.
     * 
     * @return the charset
     */
    protected Charset getCharset() {
        return config.getCharset();
    }
}