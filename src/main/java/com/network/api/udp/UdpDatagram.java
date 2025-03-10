package com.network.api.udp;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a UDP datagram.
 * 
 * <p>This class encapsulates the data and addressing information of a UDP datagram.
 */
public class UdpDatagram {
    
    private final byte[] data;
    private final InetSocketAddress senderAddress;
    private final long receiveTime;
    
    /**
     * Creates a new UDP datagram.
     * 
     * @param data the datagram data
     * @param senderAddress the sender's address
     */
    public UdpDatagram(byte[] data, InetSocketAddress senderAddress) {
        this(data, senderAddress, System.currentTimeMillis());
    }
    
    /**
     * Creates a new UDP datagram with the specified receive time.
     * 
     * @param data the datagram data
     * @param senderAddress the sender's address
     * @param receiveTime the time when the datagram was received, in milliseconds since epoch
     */
    public UdpDatagram(byte[] data, InetSocketAddress senderAddress, long receiveTime) {
        this.data = Arrays.copyOf(data, data.length);
        this.senderAddress = senderAddress;
        this.receiveTime = receiveTime;
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
     * Gets the sender's address.
     * 
     * @return the sender's address
     */
    public InetSocketAddress getSenderAddress() {
        return senderAddress;
    }
    
    /**
     * Gets the time when the datagram was received.
     * 
     * @return the receive time, in milliseconds since epoch
     */
    public long getReceiveTime() {
        return receiveTime;
    }
    
    /**
     * Gets the data length.
     * 
     * @return the data length in bytes
     */
    public int getLength() {
        return data.length;
    }
    
    /**
     * Gets the datagram data as a string.
     * 
     * <p>The data is decoded using the UTF-8 charset.
     * 
     * @return the data as a string
     */
    public String getDataAsString() {
        return new String(data);
    }
    
    /**
     * Gets the datagram data as a string using the specified charset.
     * 
     * @param charsetName the charset name
     * @return the data as a string
     * @throws java.nio.charset.UnsupportedCharsetException if the charset is not supported
     */
    public String getDataAsString(String charsetName) {
        return new String(data, java.nio.charset.Charset.forName(charsetName));
    }
    
    /**
     * Gets the datagram data as a string using the specified charset.
     * 
     * @param charset the charset
     * @return the data as a string
     */
    public String getDataAsString(java.nio.charset.Charset charset) {
        return new String(data, charset);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UdpDatagram that = (UdpDatagram) o;
        return receiveTime == that.receiveTime &&
               Arrays.equals(data, that.data) &&
               Objects.equals(senderAddress, that.senderAddress);
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
               "length=" + data.length +
               ", senderAddress=" + senderAddress +
               ", receiveTime=" + receiveTime +
               '}';
    }
}