package com.network.api.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * An immutable snapshot of connection metrics at a specific point in time.
 * 
 * <p>This class implements the {@link ConnectionMetrics} interface
 * and provides a snapshot of the metrics that will not change as the
 * actual metrics of the connection change.
 */
public class ConnectionMetricsSnapshot implements ConnectionMetrics {
    
    private final long totalBytesSent;
    private final long totalBytesReceived;
    private final double currentSendThroughput;
    private final double currentReceiveThroughput;
    private final double averageSendThroughput;
    private final double averageReceiveThroughput;
    private final long totalMessagesSent;
    private final long totalMessagesReceived;
    private final Instant connectionTime;
    private final Instant lastActivityTime;
    private final Duration connectionDuration;
    private final Duration currentRoundTripTime;
    private final Duration averageRoundTripTime;
    private final long errorCount;
    private final long reconnectionCount;
    private final Instant snapshotTime;
    
    /**
     * Creates a new metrics snapshot.
     * 
     * @param metrics the metrics to snapshot
     */
    public ConnectionMetricsSnapshot(ConnectionMetrics metrics) {
        this.totalBytesSent = metrics.getTotalBytesSent();
        this.totalBytesReceived = metrics.getTotalBytesReceived();
        this.currentSendThroughput = metrics.getCurrentSendThroughput();
        this.currentReceiveThroughput = metrics.getCurrentReceiveThroughput();
        this.averageSendThroughput = metrics.getAverageSendThroughput();
        this.averageReceiveThroughput = metrics.getAverageReceiveThroughput();
        this.totalMessagesSent = metrics.getTotalMessagesSent();
        this.totalMessagesReceived = metrics.getTotalMessagesReceived();
        this.connectionTime = metrics.getConnectionTime();
        this.lastActivityTime = metrics.getLastActivityTime();
        this.connectionDuration = metrics.getConnectionDuration();
        this.currentRoundTripTime = metrics.getCurrentRoundTripTime();
        this.averageRoundTripTime = metrics.getAverageRoundTripTime();
        this.errorCount = metrics.getErrorCount();
        this.reconnectionCount = metrics.getReconnectionCount();
        this.snapshotTime = Instant.now();
    }
    
    /**
     * Builder constructor for creating snapshots directly.
     */
    private ConnectionMetricsSnapshot(Builder builder) {
        this.totalBytesSent = builder.totalBytesSent;
        this.totalBytesReceived = builder.totalBytesReceived;
        this.currentSendThroughput = builder.currentSendThroughput;
        this.currentReceiveThroughput = builder.currentReceiveThroughput;
        this.averageSendThroughput = builder.averageSendThroughput;
        this.averageReceiveThroughput = builder.averageReceiveThroughput;
        this.totalMessagesSent = builder.totalMessagesSent;
        this.totalMessagesReceived = builder.totalMessagesReceived;
        this.connectionTime = builder.connectionTime;
        this.lastActivityTime = builder.lastActivityTime;
        this.connectionDuration = builder.connectionDuration;
        this.currentRoundTripTime = builder.currentRoundTripTime;
        this.averageRoundTripTime = builder.averageRoundTripTime;
        this.errorCount = builder.errorCount;
        this.reconnectionCount = builder.reconnectionCount;
        this.snapshotTime = Instant.now();
    }
    
    /**
     * Gets the time when this snapshot was taken.
     * 
     * @return the snapshot time
     */
    public Instant getSnapshotTime() {
        return snapshotTime;
    }
    
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
        return currentSendThroughput;
    }
    
    @Override
    public double getCurrentReceiveThroughput() {
        return currentReceiveThroughput;
    }
    
    @Override
    public double getAverageSendThroughput() {
        return averageSendThroughput;
    }
    
    @Override
    public double getAverageReceiveThroughput() {
        return averageReceiveThroughput;
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
        return connectionTime;
    }
    
    @Override
    public Instant getLastActivityTime() {
        return lastActivityTime;
    }
    
    @Override
    public Duration getConnectionDuration() {
        return connectionDuration;
    }
    
    @Override
    public Duration getCurrentRoundTripTime() {
        return currentRoundTripTime;
    }
    
    @Override
    public Duration getAverageRoundTripTime() {
        return averageRoundTripTime;
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
        return this; // Already a snapshot
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionMetricsSnapshot that = (ConnectionMetricsSnapshot) o;
        return totalBytesSent == that.totalBytesSent &&
               totalBytesReceived == that.totalBytesReceived &&
               Double.compare(that.currentSendThroughput, currentSendThroughput) == 0 &&
               Double.compare(that.currentReceiveThroughput, currentReceiveThroughput) == 0 &&
               Double.compare(that.averageSendThroughput, averageSendThroughput) == 0 &&
               Double.compare(that.averageReceiveThroughput, averageReceiveThroughput) == 0 &&
               totalMessagesSent == that.totalMessagesSent &&
               totalMessagesReceived == that.totalMessagesReceived &&
               errorCount == that.errorCount &&
               reconnectionCount == that.reconnectionCount &&
               Objects.equals(connectionTime, that.connectionTime) &&
               Objects.equals(lastActivityTime, that.lastActivityTime) &&
               Objects.equals(connectionDuration, that.connectionDuration) &&
               Objects.equals(currentRoundTripTime, that.currentRoundTripTime) &&
               Objects.equals(averageRoundTripTime, that.averageRoundTripTime) &&
               Objects.equals(snapshotTime, that.snapshotTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(
            totalBytesSent, totalBytesReceived,
            currentSendThroughput, currentReceiveThroughput,
            averageSendThroughput, averageReceiveThroughput,
            totalMessagesSent, totalMessagesReceived,
            connectionTime, lastActivityTime, connectionDuration,
            currentRoundTripTime, averageRoundTripTime,
            errorCount, reconnectionCount, snapshotTime
        );
    }
    
    @Override
    public String toString() {
        return "ConnectionMetricsSnapshot{" +
               "totalBytesSent=" + totalBytesSent +
               ", totalBytesReceived=" + totalBytesReceived +
               ", currentSendThroughput=" + currentSendThroughput +
               ", currentReceiveThroughput=" + currentReceiveThroughput +
               ", averageSendThroughput=" + averageSendThroughput +
               ", averageReceiveThroughput=" + averageReceiveThroughput +
               ", totalMessagesSent=" + totalMessagesSent +
               ", totalMessagesReceived=" + totalMessagesReceived +
               ", connectionTime=" + connectionTime +
               ", lastActivityTime=" + lastActivityTime +
               ", connectionDuration=" + connectionDuration +
               ", currentRoundTripTime=" + currentRoundTripTime +
               ", averageRoundTripTime=" + averageRoundTripTime +
               ", errorCount=" + errorCount +
               ", reconnectionCount=" + reconnectionCount +
               ", snapshotTime=" + snapshotTime +
               '}';
    }
    
    /**
     * Returns a builder for creating {@link ConnectionMetricsSnapshot} instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for {@link ConnectionMetricsSnapshot}.
     */
    public static class Builder {
        private long totalBytesSent;
        private long totalBytesReceived;
        private double currentSendThroughput;
        private double currentReceiveThroughput;
        private double averageSendThroughput;
        private double averageReceiveThroughput;
        private long totalMessagesSent;
        private long totalMessagesReceived;
        private Instant connectionTime;
        private Instant lastActivityTime;
        private Duration connectionDuration;
        private Duration currentRoundTripTime;
        private Duration averageRoundTripTime;
        private long errorCount;
        private long reconnectionCount;
        
        private Builder() {
            // Default values
            this.connectionTime = Instant.now();
            this.lastActivityTime = Instant.now();
            this.connectionDuration = Duration.ZERO;
            this.currentRoundTripTime = Duration.ZERO;
            this.averageRoundTripTime = Duration.ZERO;
        }
        
        public Builder totalBytesSent(long totalBytesSent) {
            this.totalBytesSent = totalBytesSent;
            return this;
        }
        
        public Builder totalBytesReceived(long totalBytesReceived) {
            this.totalBytesReceived = totalBytesReceived;
            return this;
        }
        
        public Builder currentSendThroughput(double currentSendThroughput) {
            this.currentSendThroughput = currentSendThroughput;
            return this;
        }
        
        public Builder currentReceiveThroughput(double currentReceiveThroughput) {
            this.currentReceiveThroughput = currentReceiveThroughput;
            return this;
        }
        
        public Builder averageSendThroughput(double averageSendThroughput) {
            this.averageSendThroughput = averageSendThroughput;
            return this;
        }
        
        public Builder averageReceiveThroughput(double averageReceiveThroughput) {
            this.averageReceiveThroughput = averageReceiveThroughput;
            return this;
        }
        
        public Builder totalMessagesSent(long totalMessagesSent) {
            this.totalMessagesSent = totalMessagesSent;
            return this;
        }
        
        public Builder totalMessagesReceived(long totalMessagesReceived) {
            this.totalMessagesReceived = totalMessagesReceived;
            return this;
        }
        
        public Builder connectionTime(Instant connectionTime) {
            this.connectionTime = connectionTime;
            return this;
        }
        
        public Builder lastActivityTime(Instant lastActivityTime) {
            this.lastActivityTime = lastActivityTime;
            return this;
        }
        
        public Builder connectionDuration(Duration connectionDuration) {
            this.connectionDuration = connectionDuration;
            return this;
        }
        
        public Builder currentRoundTripTime(Duration currentRoundTripTime) {
            this.currentRoundTripTime = currentRoundTripTime;
            return this;
        }
        
        public Builder averageRoundTripTime(Duration averageRoundTripTime) {
            this.averageRoundTripTime = averageRoundTripTime;
            return this;
        }
        
        public Builder errorCount(long errorCount) {
            this.errorCount = errorCount;
            return this;
        }
        
        public Builder reconnectionCount(long reconnectionCount) {
            this.reconnectionCount = reconnectionCount;
            return this;
        }
        
        public ConnectionMetricsSnapshot build() {
            return new ConnectionMetricsSnapshot(this);
        }
    }
}