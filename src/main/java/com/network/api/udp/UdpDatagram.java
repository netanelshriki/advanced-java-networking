package com.network.api.udp;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a UDP datagram.
 * 
 * <p>This class encapsulates the data and metadata of a UDP datagram,
 * such as the sender address, data, and timestamp.
 */
public class UdpDatagram {
    
    private final byte[] data;
    private final InetSocketAddress senderAddress;
    private final Instant receiveTime;
    
    /**
     * Creates a new UDP datagram.
     * 
     * @param data the datagram data
     * @param senderAddress the sender address
     */
    public UdpDatagram(byte[] data, InetSocketAddress senderAddress) {
        this(data, senderAddress, Instant.now());
    }
    
    /**
     * Creates a new UDP datagram with a specific receive time.
     * 
     * @param data the datagram data
     * @param senderAddress the sender address
     * @param receiveTime the time the datagram was received
     */
    public UdpDatagram(byte[] data, InetSocketAddress senderAddress, Instant receiveTime) {
        this.data = data != null ? Arrays.copyOf(data, data.length) : new byte[0];
        this.senderAddress = senderAddress;
        this.receiveTime = receiveTime != null ? receiveTime : Instant.now();
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
     * Gets the sender address.
     * 
     * @return the sender address
     */
    public InetSocketAddress getSenderAddress() {
        return senderAddress;
    }
    
    /**
     * Gets the time the datagram was received.
     * 
     * @return the receive time
     */
    public Instant getReceiveTime() {
        return receiveTime;
    }
    
    /**
     * Gets the length of the datagram data.
     * 
     * @return the data length in bytes
     */
    public int getLength() {
        return data.length;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UdpDatagram that = (UdpDatagram) o;
        return Arrays.equals(data, that.data) &&
               Objects.equals(senderAddress, that.senderAddress) &&
               Objects.equals(receiveTime, that.receiveTime);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(senderAddress, receiveTime);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
    
    @Override
    public String toString() {
        return "UdpDatagram{" +
               "dataLength=" + data.length +
               ", senderAddress=" + senderAddress +
               ", receiveTime=" + receiveTime +
               '}';
    }
}