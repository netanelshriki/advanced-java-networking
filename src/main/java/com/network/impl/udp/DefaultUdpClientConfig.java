package com.network.impl.udp;

import com.network.api.udp.UdpClient;
import com.network.api.udp.UdpClientBuilder;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Default implementation of the UDP client configuration.
 */
public class DefaultUdpClientConfig {

    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final int bufferSize;
    private final Duration timeout;
    private final int maxPacketSize;
    
    /**
     * Creates a new DefaultUdpClientConfig with the specified configuration.
     * 
     * @param builder the builder used to create this configuration
     */
    private DefaultUdpClientConfig(Builder builder) {
        this.remoteAddress = builder.remoteAddress;
        this.localAddress = builder.localAddress;
        this.bufferSize = builder.bufferSize;
        this.timeout = builder.timeout;
        this.maxPacketSize = builder.maxPacketSize;
    }
    
    /**
     * Gets the remote address to connect to.
     * 
     * @return the remote address
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    /**
     * Gets the local address to bind to.
     * 
     * @return the local address
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }
    
    /**
     * Gets the UDP buffer size.
     * 
     * @return the buffer size
     */
    public int getBufferSize() {
        return bufferSize;
    }
    
    /**
     * Gets the socket timeout.
     * 
     * @return the timeout
     */
    public Duration getTimeout() {
        return timeout;
    }
    
    /**
     * Gets the maximum packet size that can be received.
     * 
     * @return the maximum packet size
     */
    public int getMaxPacketSize() {
        return maxPacketSize;
    }
    
    /**
     * Creates a new builder for creating DefaultUdpClientConfig instances.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating {@link DefaultUdpClientConfig} instances.
     */
    public static class Builder implements UdpClientBuilder {
        private InetSocketAddress remoteAddress;
        private InetSocketAddress localAddress;
        private int bufferSize = 8192;
        private Duration timeout;
        private int maxPacketSize = 65507; // Maximum UDP packet size
        
        @Override
        public UdpClient build() {
            if (remoteAddress == null) {
                throw new IllegalStateException("Remote address must be set");
            }
            
            DefaultUdpClientConfig config = new DefaultUdpClientConfig(this);
            return new DefaultUdpClient(config);
        }
        
        @Override
        public UdpClientBuilder withAddress(String host, int port) {
            this.remoteAddress = new InetSocketAddress(host, port);
            return this;
        }
        
        @Override
        public UdpClientBuilder withAddress(InetSocketAddress address) {
            this.remoteAddress = address;
            return this;
        }
        
        /**
         * Sets the local address to bind to.
         * 
         * @param host the local host
         * @param port the local port
         * @return this builder instance
         */
        public Builder withLocalAddress(String host, int port) {
            this.localAddress = new InetSocketAddress(host, port);
            return this;
        }
        
        /**
         * Sets the local address to bind to.
         * 
         * @param address the local address
         * @return this builder instance
         */
        public Builder withLocalAddress(InetSocketAddress address) {
            this.localAddress = address;
            return this;
        }
        
        /**
         * Sets the UDP buffer size.
         * 
         * @param bufferSize the buffer size
         * @return this builder instance
         */
        public Builder withBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }
        
        /**
         * Sets the socket timeout.
         * 
         * @param timeout the timeout
         * @return this builder instance
         */
        public Builder withTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }
        
        /**
         * Sets the maximum packet size that can be received.
         * 
         * @param maxPacketSize the maximum packet size
         * @return this builder instance
         */
        public Builder withMaxPacketSize(int maxPacketSize) {
            this.maxPacketSize = maxPacketSize;
            return this;
        }
    }
}
