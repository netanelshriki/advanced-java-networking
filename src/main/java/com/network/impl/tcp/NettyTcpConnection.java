package com.network.impl.tcp;

import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.connection.Connection;
import com.network.api.metrics.ConnectionMetrics;
import com.network.api.metrics.ConnectionMetricsSnapshot;
import com.network.exception.ConnectionException;
import com.network.exception.NetworkException.ErrorCode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Netty-based implementation of the {@link Connection} interface.
 * 
 * <p>This class wraps a Netty {@link Channel} and implements the
 * connection interface methods using Netty's API.
 */
public class NettyTcpConnection implements Connection {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyTcpConnection.class);
    
    private final String id;
    private final Channel channel;
    private final Instant creationTime;
    private final Map<String, Object> attributes;
    private final AtomicBoolean open;
    private final NettyTcpConnectionMetrics metrics;
    
    private Instant lastActivityTime;
    private Duration idleTimeout;
    private boolean keepAlive;
    
    /**
     * Creates a new Netty TCP connection.
     * 
     * @param channel the Netty channel
     */
    public NettyTcpConnection(Channel channel) {
        this.id = UUID.randomUUID().toString();
        this.channel = channel;
        this.creationTime = Instant.now();
        this.lastActivityTime = Instant.now();
        this.attributes = new ConcurrentHashMap<>();
        this.open = new AtomicBoolean(true);
        this.metrics = new NettyTcpConnectionMetrics();
        this.idleTimeout = Duration.ofMinutes(5);
        this.keepAlive = true;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }
    
    @Override
    public SocketAddress getLocalAddress() {
        return channel.localAddress();
    }
    
    @Override
    public boolean isOpen() {
        return open.get() && channel.isOpen() && channel.isActive();
    }
    
    @Override
    public Instant getCreationTime() {
        return creationTime;
    }
    
    @Override
    public Instant getLastActivityTime() {
        return lastActivityTime;
    }
    
    @Override
    public void send(byte[] data) throws ConnectionException {
        checkOpen();
        
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        try {
            channel.writeAndFlush(buffer).sync();
            updateLastActivityTime();
            metrics.incrementBytesSent(data.length);
            metrics.incrementMessagesSent();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException(ErrorCode.SEND_ERROR, getRemoteAddress(), getLocalAddress(), getId(), e);
        } catch (Exception e) {
            throw new ConnectionException(ErrorCode.SEND_ERROR, getRemoteAddress(), getLocalAddress(), getId(), e);
        }
    }
    
    @Override
    public CompletableFuture<Void> sendAsync(byte[] data) {
        checkOpen();
        
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        channel.writeAndFlush(buffer).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    updateLastActivityTime();
                    metrics.incrementBytesSent(data.length);
                    metrics.incrementMessagesSent();
                    future.complete(null);
                } else {
                    future.completeExceptionally(new ConnectionException(
                        ErrorCode.SEND_ERROR, getRemoteAddress(), getLocalAddress(), getId(), f.cause()));
                }
            }
        });
        
        return future;
    }
    
    @Override
    public void close() {
        if (open.compareAndSet(true, false)) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while closing channel", e);
            }
        }
    }
    
    @Override
    public CompletableFuture<Void> closeAsync() {
        if (open.compareAndSet(true, false)) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            channel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(f.cause());
                    }
                }
            });
            
            return future;
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
    
    @Override
    public ConnectionMetrics getMetrics() {
        return metrics;
    }
    
    @Override
    public Connection setAttribute(String key, Object value) {
        if (key != null) {
            if (value != null) {
                attributes.put(key, value);
            } else {
                attributes.remove(key);
            }
        }
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        if (key == null || type == null) {
            return Optional.empty();
        }
        
        Object value = attributes.get(key);
        if (value == null) {
            return Optional.empty();
        }
        
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
    
    @Override
    public Connection withIdleTimeout(Duration timeout) {
        if (timeout == null || timeout.isNegative()) {
            throw new IllegalArgumentException("Idle timeout must not be null or negative");
        }
        this.idleTimeout = timeout;
        return this;
    }
    
    @Override
    public Connection withKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        if (isOpen()) {
            try {
                channel.config().setKeepAlive(keepAlive);
            } catch (Exception e) {
                logger.warn("Failed to set keep-alive: {}", e.getMessage(), e);
            }
        }
        return this;
    }
    
    @Override
    public Protocol getProtocol() {
        return Protocol.TCP;
    }
    
    /**
     * Gets the Netty channel associated with this connection.
     * 
     * @return the Netty channel
     */
    public Channel getChannel() {
        return channel;
    }
    
    /**
     * Updates the last activity time to the current time.
     */
    public void updateLastActivityTime() {
        this.lastActivityTime = Instant.now();
    }
    
    /**
     * Handles data received from the channel.
     * 
     * @param data the received data
     */
    public void handleDataReceived(byte[] data) {
        updateLastActivityTime();
        metrics.incrementBytesReceived(data.length);
        metrics.incrementMessagesReceived();
    }
    
    /**
     * Checks if the connection is open, and throws an exception if not.
     * 
     * @throws ConnectionException if the connection is not open
     */
    private void checkOpen() throws ConnectionException {
        if (!isOpen()) {
            throw new ConnectionException(ErrorCode.CONNECTION_CLOSED, 
                getRemoteAddress(), getLocalAddress(), getId(), "Connection is closed");
        }
    }
    
    /**
     * Metrics implementation for Netty TCP connections.
     */
    private class NettyTcpConnectionMetrics implements ConnectionMetrics {
        
        private long totalBytesSent;
        private long totalBytesReceived;
        private long totalMessagesSent;
        private long totalMessagesReceived;
        private long errorCount;
        private long reconnectionCount;
        
        private final Map<Long, Double> sendThroughputSamples = new HashMap<>();
        private final Map<Long, Double> receiveThroughputSamples = new HashMap<>();
        private final Map<Long, Duration> roundTripTimeSamples = new HashMap<>();
        
        @Override
        public long getTotalBytesSent() {
            return totalBytesSent;
        }
        
        @Override
        public long getTotalBytesReceived() {
            return totalBytesReceived;
        }
        
        @Override
        public double getCurrentSendThroughput() {
            // Calculate throughput over last 5 seconds
            long now = System.currentTimeMillis();
            long cutoff = now - 5000;
            
            double sum = 0;
            int count = 0;
            
            synchronized (sendThroughputSamples) {
                for (Map.Entry<Long, Double> entry : sendThroughputSamples.entrySet()) {
                    if (entry.getKey() >= cutoff) {
                        sum += entry.getValue();
                        count++;
                    }
                }
                
                // Clean up old samples
                sendThroughputSamples.entrySet().removeIf(entry -> entry.getKey() < cutoff);
            }
            
            return count > 0 ? sum / count : 0;
        }
        
        @Override
        public double getCurrentReceiveThroughput() {
            // Calculate throughput over last 5 seconds
            long now = System.currentTimeMillis();
            long cutoff = now - 5000;
            
            double sum = 0;
            int count = 0;
            
            synchronized (receiveThroughputSamples) {
                for (Map.Entry<Long, Double> entry : receiveThroughputSamples.entrySet()) {
                    if (entry.getKey() >= cutoff) {
                        sum += entry.getValue();
                        count++;
                    }
                }
                
                // Clean up old samples
                receiveThroughputSamples.entrySet().removeIf(entry -> entry.getKey() < cutoff);
            }
            
            return count > 0 ? sum / count : 0;
        }
        
        @Override
        public double getAverageSendThroughput() {
            if (totalBytesSent == 0) {
                return 0;
            }
            
            Duration duration = getConnectionDuration();
            double seconds = duration.toMillis() / 1000.0;
            if (seconds < 0.001) {
                return 0;
            }
            
            return totalBytesSent / seconds;
        }
        
        @Override
        public double getAverageReceiveThroughput() {
            if (totalBytesReceived == 0) {
                return 0;
            }
            
            Duration duration = getConnectionDuration();
            double seconds = duration.toMillis() / 1000.0;
            if (seconds < 0.001) {
                return 0;
            }
            
            return totalBytesReceived / seconds;
        }
        
        @Override
        public long getTotalMessagesSent() {
            return totalMessagesSent;
        }
        
        @Override
        public long getTotalMessagesReceived() {
            return totalMessagesReceived;
        }
        
        @Override
        public Instant getConnectionTime() {
            return creationTime;
        }
        
        @Override
        public Instant getLastActivityTime() {
            return lastActivityTime;
        }
        
        @Override
        public Duration getConnectionDuration() {
            return Duration.between(creationTime, Instant.now());
        }
        
        @Override
        public Duration getCurrentRoundTripTime() {
            // Get latest RTT sample
            synchronized (roundTripTimeSamples) {
                if (roundTripTimeSamples.isEmpty()) {
                    return Duration.ZERO;
                }
                
                long maxTime = roundTripTimeSamples.keySet().stream()
                    .max(Long::compare)
                    .orElse(0L);
                
                return roundTripTimeSamples.get(maxTime);
            }
        }
        
        @Override
        public Duration getAverageRoundTripTime() {
            synchronized (roundTripTimeSamples) {
                if (roundTripTimeSamples.isEmpty()) {
                    return Duration.ZERO;
                }
                
                long sum = 0;
                for (Duration rtt : roundTripTimeSamples.values()) {
                    sum += rtt.toMillis();
                }
                
                return Duration.ofMillis(sum / roundTripTimeSamples.size());
            }
        }
        
        @Override
        public long getErrorCount() {
            return errorCount;
        }
        
        @Override
        public long getReconnectionCount() {
            return reconnectionCount;
        }
        
        @Override
        public ConnectionMetricsSnapshot snapshot() {
            return new ConnectionMetricsSnapshot(this);
        }
        
        /**
         * Increments the total bytes sent counter.
         * 
         * @param bytes the number of bytes sent
         */
        public void incrementBytesSent(long bytes) {
            this.totalBytesSent += bytes;
            
            // Add throughput sample
            long now = System.currentTimeMillis();
            synchronized (sendThroughputSamples) {
                sendThroughputSamples.put(now, (double) bytes);
            }
        }
        
        /**
         * Increments the total bytes received counter.
         * 
         * @param bytes the number of bytes received
         */
        public void incrementBytesReceived(long bytes) {
            this.totalBytesReceived += bytes;
            
            // Add throughput sample
            long now = System.currentTimeMillis();
            synchronized (receiveThroughputSamples) {
                receiveThroughputSamples.put(now, (double) bytes);
            }
        }
        
        /**
         * Increments the total messages sent counter.
         */
        public void incrementMessagesSent() {
            this.totalMessagesSent++;
        }
        
        /**
         * Increments the total messages received counter.
         */
        public void incrementMessagesReceived() {
            this.totalMessagesReceived++;
        }
        
        /**
         * Increments the error count.
         */
        public void incrementErrorCount() {
            this.errorCount++;
        }
        
        /**
         * Increments the reconnection count.
         */
        public void incrementReconnectionCount() {
            this.reconnectionCount++;
        }
        
        /**
         * Adds a round trip time sample.
         * 
         * @param rtt the round trip time
         */
        public void addRoundTripTimeSample(Duration rtt) {
            if (rtt != null && !rtt.isNegative()) {
                long now = System.currentTimeMillis();
                synchronized (roundTripTimeSamples) {
                    roundTripTimeSamples.put(now, rtt);
                    
                    // Limit sample size
                    if (roundTripTimeSamples.size() > 100) {
                        long oldest = roundTripTimeSamples.keySet().stream()
                            .min(Long::compare)
                            .orElse(0L);
                        roundTripTimeSamples.remove(oldest);
                    }
                }
            }
        }
    }
}