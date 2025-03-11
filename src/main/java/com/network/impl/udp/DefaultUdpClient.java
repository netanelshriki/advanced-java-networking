package com.network.impl.udp;

import com.network.api.connection.Connection;
import com.network.api.connection.ConnectionListener;
import com.network.api.udp.UdpClient;
import com.network.exception.NetworkException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Default implementation of the {@link UdpClient} interface.
 */
public class DefaultUdpClient implements UdpClient {

    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final int bufferSize;
    private final Duration timeout;
    private final int maxPacketSize;
    private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final List<BiConsumer<Connection, byte[]>> dataReceivedCallbacks = new CopyOnWriteArrayList<>();
    
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean listening = new AtomicBoolean(false);
    private ExecutorService executor;
    private DatagramSocket socket;
    private DefaultUdpConnection connection;
    
    /**
     * Creates a new DefaultUdpClient with the specified configuration.
     * 
     * @param config the UDP client configuration
     */
    DefaultUdpClient(DefaultUdpClientConfig config) {
        this.remoteAddress = config.getRemoteAddress();
        this.localAddress = config.getLocalAddress();
        this.bufferSize = config.getBufferSize();
        this.timeout = config.getTimeout();
        this.maxPacketSize = config.getMaxPacketSize();
    }
    
    @Override
    public void connect() throws NetworkException {
        if (connected.get()) {
            return;
        }
        
        try {
            // Create and configure the socket
            if (localAddress != null) {
                socket = new DatagramSocket(localAddress);
            } else {
                socket = new DatagramSocket();
            }
            
            // Apply socket options
            if (timeout != null) {
                socket.setSoTimeout((int) timeout.toMillis());
            }
            
            // Create the connection object
            connection = new DefaultUdpConnection(socket, remoteAddress);
            
            // Start listening for incoming data
            startListening();
            
            // Mark as connected
            connected.set(true);
            
            // Notify listeners
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onConnect(connection);
                } catch (Exception e) {
                    // Ignore exceptions from listeners
                }
            }
        } catch (SocketException e) {
            throw new NetworkException("Failed to create UDP socket", e);
        }
    }
    
    @Override
    public CompletableFuture<Void> connectAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            connect();
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    @Override
    public void disconnect() {
        if (!connected.get()) {
            return;
        }
        
        // Stop listening
        if (listening.get() && executor != null) {
            executor.shutdownNow();
            listening.set(false);
        }
        
        // Close the socket
        if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
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
        
        try {
            disconnect();
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    @Override
    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }
    
    @Override
    public Connection getConnection() {
        return connection;
    }
    
    @Override
    public UdpClient addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
        return this;
    }
    
    @Override
    public boolean removeConnectionListener(ConnectionListener listener) {
        return connectionListeners.remove(listener);
    }
    
    @Override
    public UdpClient onConnect(Consumer<Connection> callback) {
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
    public UdpClient onDisconnect(Consumer<Connection> callback) {
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
    public UdpClient onError(Consumer<Throwable> callback) {
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
    public UdpClient withConnectionTimeout(Duration timeout) {
        if (isConnected()) {
            throw new IllegalStateException("Cannot change connection timeout while connected");
        }
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        
        // Not applicable for UDP as it's connectionless
        return this;
    }
    
    @Override
    public void close() {
        disconnect();
    }
    
    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    @Override
    public InetSocketAddress getLocalAddress() {
        if (socket != null && socket.isBound()) {
            return (InetSocketAddress) socket.getLocalSocketAddress();
        }
        return localAddress;
    }
    
    @Override
    public void send(byte[] data) throws NetworkException {
        if (!isConnected()) {
            throw new NetworkException("Not connected", null);
        }
        
        try {
            DatagramPacket packet = new DatagramPacket(
                    data, 0, data.length, remoteAddress.getAddress(), remoteAddress.getPort());
            socket.send(packet);
        } catch (IOException e) {
            throw new NetworkException("Failed to send UDP packet", e);
        }
    }
    
    @Override
    public CompletableFuture<Void> sendAsync(byte[] data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            send(data);
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    @Override
    public byte[] receive() throws NetworkException {
        if (!isConnected()) {
            throw new NetworkException("Not connected", null);
        }
        
        try {
            byte[] buffer = new byte[maxPacketSize];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            
            // Copy only the received data
            byte[] data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
            
            return data;
        } catch (IOException e) {
            throw new NetworkException("Failed to receive UDP packet", e);
        }
    }
    
    @Override
    public CompletableFuture<byte[]> receiveAsync() {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        
        if (!isConnected()) {
            future.completeExceptionally(new NetworkException("Not connected", null));
            return future;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                byte[] data = receive();
                future.complete(data);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    @Override
    public UdpClient onDataReceived(BiConsumer<Connection, byte[]> callback) {
        dataReceivedCallbacks.add(callback);
        return this;
    }
    
    /**
     * Starts listening for incoming data on a separate thread.
     */
    private void startListening() {
        if (listening.get()) {
            return;
        }
        
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "udp-listener-" + remoteAddress);
            thread.setDaemon(true);
            return thread;
        });
        
        executor.submit(() -> {
            listening.set(true);
            
            while (listening.get() && isConnected()) {
                try {
                    byte[] buffer = new byte[maxPacketSize];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // Copy only the received data
                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                    
                    // Notify callbacks
                    for (BiConsumer<Connection, byte[]> callback : dataReceivedCallbacks) {
                        try {
                            callback.accept(connection, data);
                        } catch (Exception e) {
                            // Ignore exceptions from callbacks
                        }
                    }
                } catch (IOException e) {
                    if (listening.get() && isConnected()) {
                        for (ConnectionListener listener : connectionListeners) {
                            try {
                                listener.onError(e);
                            } catch (Exception ex) {
                                // Ignore exceptions from listeners
                            }
                        }
                    }
                }
            }
            
            listening.set(false);
        });
    }
    
    /**
     * UDP connection implementation.
     */
    private static class DefaultUdpConnection implements Connection {
        private final DatagramSocket socket;
        private final InetSocketAddress remoteAddress;
        
        DefaultUdpConnection(DatagramSocket socket, InetSocketAddress remoteAddress) {
            this.socket = socket;
            this.remoteAddress = remoteAddress;
        }
        
        @Override
        public boolean isConnected() {
            return socket != null && !socket.isClosed();
        }
        
        @Override
        public void close() {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        
        /**
         * Gets the remote address this connection is connected to.
         * 
         * @return the remote address
         */
        public InetSocketAddress getRemoteAddress() {
            return remoteAddress;
        }
        
        /**
         * Gets the local address this connection is bound to.
         * 
         * @return the local address
         */
        public InetSocketAddress getLocalAddress() {
            return (InetSocketAddress) socket.getLocalSocketAddress();
        }
    }
}
