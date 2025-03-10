package com.network.api.metrics;

import java.time.Duration;
import java.time.Instant;

/**
 * Interface for retrieving metrics about a connection.
 * 
 * <p>This interface provides methods for getting various metrics about a network connection,
 * such as bytes sent and received, connection time, latency, etc.
 */
public interface ConnectionMetrics {
    
    /**
     * Gets the total number of bytes sent over this connection.
     * 
     * @return the total bytes sent
     */
    long getTotalBytesSent();
    
    /**
     * Gets the total number of bytes received over this connection.
     * 
     * @return the total bytes received
     */
    long getTotalBytesReceived();
    
    /**
     * Gets the current send throughput in bytes per second.
     * 
     * @return the current send throughput
     */
    double getCurrentSendThroughput();
    
    /**
     * Gets the current receive throughput in bytes per second.
     * 
     * @return the current receive throughput
     */
    double getCurrentReceiveThroughput();
    
    /**
     * Gets the average send throughput in bytes per second since connection was established.
     * 
     * @return the average send throughput
     */
    double getAverageSendThroughput();
    
    /**
     * Gets the average receive throughput in bytes per second since connection was established.
     * 
     * @return the average receive throughput
     */
    double getAverageReceiveThroughput();
    
    /**
     * Gets the total number of messages sent over this connection.
     * 
     * @return the total messages sent
     */
    long getTotalMessagesSent();
    
    /**
     * Gets the total number of messages received over this connection.
     * 
     * @return the total messages received
     */
    long getTotalMessagesReceived();
    
    /**
     * Gets the time when this connection was established.
     * 
     * @return the connection establishment time
     */
    Instant getConnectionTime();
    
    /**
     * Gets the time when this connection was last active (sent or received data).
     * 
     * @return the last activity time
     */
    Instant getLastActivityTime();
    
    /**
     * Gets the duration for which this connection has been active.
     * 
     * @return the connection duration
     */
    Duration getConnectionDuration();
    
    /**
     * Gets the current round-trip time (latency) for this connection.
     * 
     * @return the current round-trip time
     */
    Duration getCurrentRoundTripTime();
    
    /**
     * Gets the average round-trip time (latency) for this connection.
     * 
     * @return the average round-trip time
     */
    Duration getAverageRoundTripTime();
    
    /**
     * Gets the number of errors that have occurred on this connection.
     * 
     * @return the error count
     */
    long getErrorCount();
    
    /**
     * Gets the number of reconnection attempts for this connection.
     * 
     * @return the reconnection count
     */
    long getReconnectionCount();
    
    /**
     * Gets a snapshot of these metrics at the current point in time.
     * 
     * <p>The returned snapshot will not be updated as the metrics change.
     * 
     * @return a snapshot of these metrics
     */
    ConnectionMetricsSnapshot snapshot();
}