package com.network.api.udp;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a UDP datagram.
 * 
 * <p>This class encapsulates the data and metadata of a UDP datagram,
 * such as the source and destination addresses, the data, and the
 * timestamp.
 */
public class UdpDatagram {
    
    private final byte[] data;
    private final InetSocketAddress sourceAddress;
    private final InetSocketAddress destinationAddress;
    private final Instant timestamp;
    
    /**
     * Creates a new UDP datagram.
     * 
     * @param data the datagram data
     * @param sourceAddress the source address
     * @param destinationAddress the destination address
     */
    public UdpDatagram(byte[] data, InetSocketAddress sourceAddress, InetSocketAddress destinationAddress) {
        this(data, sourceAddress, destinationAddress, Instant.now());
    }
    
    /**
     * Creates a new UDP datagram.
     * 
     * @param data the datagram data
     * @param sourceAddress the source address
     * @param destinationAddress the destination address
     * @param timestamp the timestamp
     */
    public UdpDatagram(byte[] data, InetSocketAddress sourceAddress, InetSocketAddress destinationAddress, Instant timestamp) {
        this.data = data != null ? Arrays.copyOf(data, data.length) : new byte[0];
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }
    
    /**
     * Gets the datagram data.
     * 
     * @return the data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
    
    /**
     * Gets the source address.
     * 
     * @return the source address
     */
    public InetSocketAddress getSourceAddress() {
        return sourceAddress;
    }
    
    /**
     * Gets the destination address.
     * 
     * @return the destination address
     */
    public InetSocketAddress getDestinationAddress() {
        return destinationAddress;
    }
    
    /**
     * Gets the timestamp when this datagram was created.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the size of the datagram data.
     * 
     * @return the data size in bytes
     */
    public int getSize() {
        return data.length;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UdpDatagram that = (UdpDatagram) o;
        return Arrays.equals(data, that.data) &&
               Objects.equals(sourceAddress, that.sourceAddress) &&
               Objects.equals(destinationAddress, that.destinationAddress) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(sourceAddress, destinationAddress, timestamp);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
    
    @Override
    public String toString() {
        return "UdpDatagram{" +
               "size=" + data.length +
               ", source=" + sourceAddress +
               ", destination=" + destinationAddress +
               ", timestamp=" + timestamp +
               '}';
    }
    
    /**
     * Creates a builder for {@link UdpDatagram}.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for {@link UdpDatagram}.
     */
    public static class Builder {
        private byte[] data;
        private InetSocketAddress sourceAddress;
        private InetSocketAddress destinationAddress;
        private Instant timestamp;
        
        /**
         * Sets the datagram data.
         * 
         * @param data the data
         * @return this builder
         */
        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }
        
        /**
         * Sets the source address.
         * 
         * @param sourceAddress the source address
         * @return this builder
         */
        public Builder sourceAddress(InetSocketAddress sourceAddress) {
            this.sourceAddress = sourceAddress;
            return this;
        }
        
        /**
         * Sets the destination address.
         * 
         * @param destinationAddress the destination address
         * @return this builder
         */
        public Builder destinationAddress(InetSocketAddress destinationAddress) {
            this.destinationAddress = destinationAddress;
            return this;
        }
        
        /**
         * Sets the timestamp.
         * 
         * @param timestamp the timestamp
         * @return this builder
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        /**
         * Builds the UDP datagram.
         * 
         * @return the built datagram
         */
        public UdpDatagram build() {
            return new UdpDatagram(data, sourceAddress, destinationAddress, timestamp);
        }
    }
}