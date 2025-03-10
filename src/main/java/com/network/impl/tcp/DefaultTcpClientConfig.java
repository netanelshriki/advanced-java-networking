package com.network.impl.tcp;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import com.network.api.tcp.TcpClientBuilder;
import com.network.api.tcp.TcpClientConfig;
import com.network.config.AbstractNetworkConfig;
import com.network.serialization.Serializer;

/**
 * Default implementation of {@link TcpClientConfig}.
 * 
 * <p>This class provides a concrete implementation of the TCP client configuration
 * interface with reasonable default values.
 */
public class DefaultTcpClientConfig extends AbstractNetworkConfig implements TcpClientConfig {
    
    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final boolean keepAliveEnabled;
    private final boolean tcpNoDelay;
    private final int linger;
    private final int receiveBufferSize;
    private final int sendBufferSize;
    private final boolean autoReconnectEnabled;
    private final Duration initialReconnectBackoff;
    private final Duration maxReconnectBackoff;
    private final RetryBackoffStrategy reconnectBackoffStrategy;
    private final int maxReconnectAttempts;
    private final boolean autoConnectEnabled;
    private final Serializer serializer;
    private final Charset charset;
    
    /**
     * Creates a new TCP client configuration with the specified builder.
     * 
     * @param builder the builder containing the configuration values
     */
    public DefaultTcpClientConfig(Builder builder) {
        super(builder);
        this.remoteAddress = builder.remoteAddress;
        this.localAddress = builder.localAddress;
        this.keepAliveEnabled = builder.keepAliveEnabled;
        this.tcpNoDelay = builder.tcpNoDelay;
        this.linger = builder.linger;
        this.receiveBufferSize = builder.receiveBufferSize;
        this.sendBufferSize = builder.sendBufferSize;
        this.autoReconnectEnabled = builder.autoReconnectEnabled;
        this.initialReconnectBackoff = builder.initialReconnectBackoff;
        this.maxReconnectBackoff = builder.maxReconnectBackoff;
        this.reconnectBackoffStrategy = builder.reconnectBackoffStrategy;
        this.maxReconnectAttempts = builder.maxReconnectAttempts;
        this.autoConnectEnabled = builder.autoConnectEnabled;
        this.serializer = builder.serializer;
        this.charset = builder.charset;
    }
    
    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    @Override
    public Optional<InetSocketAddress> getLocalAddress() {
        return Optional.ofNullable(localAddress);
    }
    
    @Override
    public boolean isKeepAliveEnabled() {
        return keepAliveEnabled;
    }
    
    @Override
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }
    
    @Override
    public int getLinger() {
        return linger;
    }
    
    @Override
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }
    
    @Override
    public int getSendBufferSize() {
        return sendBufferSize;
    }
    
    @Override
    public boolean isAutoReconnectEnabled() {
        return autoReconnectEnabled;
    }
    
    @Override
    public Duration getInitialReconnectBackoff() {
        return initialReconnectBackoff;
    }
    
    @Override
    public Duration getMaxReconnectBackoff() {
        return maxReconnectBackoff;
    }
    
    @Override
    public RetryBackoffStrategy getReconnectBackoffStrategy() {
        return reconnectBackoffStrategy;
    }
    
    @Override
    public int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }
    
    @Override
    public boolean isAutoConnectEnabled() {
        return autoConnectEnabled;
    }
    
    @Override
    public Optional<Serializer> getSerializer() {
        return Optional.ofNullable(serializer);
    }
    
    @Override
    public Charset getCharset() {
        return charset;
    }
    
    @Override
    public TcpClientBuilder toBuilder() {
        return new Builder(this);
    }
    
    /**
     * Builder for {@link DefaultTcpClientConfig}.
     */
    public static class Builder extends AbstractNetworkConfigBuilder<Builder, TcpClientConfig> 
            implements TcpClientBuilder {
        
        private InetSocketAddress remoteAddress;
        private InetSocketAddress localAddress;
        private boolean keepAliveEnabled = true;
        private boolean tcpNoDelay = true;
        private int linger = 0;
        private int receiveBufferSize = 8192;
        private int sendBufferSize = 8192;
        private boolean autoReconnectEnabled = true;
        private Duration initialReconnectBackoff = Duration.ofMillis(100);
        private Duration maxReconnectBackoff = Duration.ofSeconds(30);
        private RetryBackoffStrategy reconnectBackoffStrategy = RetryBackoffStrategy.EXPONENTIAL;
        private int maxReconnectAttempts = 0; // 0 = unlimited
        private boolean autoConnectEnabled = false;
        private Serializer serializer;
        private Charset charset = StandardCharsets.UTF_8;
        
        /**
         * Creates a new builder with default values.
         */
        public Builder() {
            // Use default values
        }
        
        /**
         * Creates a new builder initialized with values from the specified configuration.
         * 
         * @param config the configuration to copy values from
         */
        public Builder(TcpClientConfig config) {
            super.connectionTimeout = config.getConnectionTimeout();
            super.readTimeout = config.getReadTimeout();
            super.writeTimeout = config.getWriteTimeout();
            super.idleTimeout = config.getIdleTimeout();
            super.maxRetryAttempts = config.getMaxRetryAttempts();
            super.retryBackoffStrategy = config.getRetryBackoffStrategy();
            super.keepAliveEnabled = config.isKeepAliveEnabled();
            super.keepAliveInterval = config.getKeepAliveInterval();
            super.bufferSize = config.getBufferSize();
            super.properties.putAll(config.getProperties());
            
            this.remoteAddress = config.getRemoteAddress();
            this.localAddress = config.getLocalAddress().orElse(null);
            this.keepAliveEnabled = config.isKeepAliveEnabled();
            this.tcpNoDelay = config.isTcpNoDelay();
            this.linger = config.getLinger();
            this.receiveBufferSize = config.getReceiveBufferSize();
            this.sendBufferSize = config.getSendBufferSize();
            this.autoReconnectEnabled = config.isAutoReconnectEnabled();
            this.initialReconnectBackoff = config.getInitialReconnectBackoff();
            this.maxReconnectBackoff = config.getMaxReconnectBackoff();
            this.reconnectBackoffStrategy = config.getReconnectBackoffStrategy();
            this.maxReconnectAttempts = config.getMaxReconnectAttempts();
            this.autoConnectEnabled = config.isAutoConnectEnabled();
            this.serializer = config.getSerializer().orElse(null);
            this.charset = config.getCharset();
        }
        
        @Override
        public Builder withAddress(String host, int port) {
            if (host == null) {
                throw new IllegalArgumentException("Host must not be null");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            this.remoteAddress = new InetSocketAddress(host, port);
            return this;
        }
        
        @Override
        public Builder withAddress(InetSocketAddress address) {
            if (address == null) {
                throw new IllegalArgumentException("Address must not be null");
            }
            this.remoteAddress = address;
            return this;
        }
        
        @Override
        public Builder withLocalAddress(String host, int port) {
            if (host == null) {
                throw new IllegalArgumentException("Host must not be null");
            }
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 0 and 65535");
            }
            this.localAddress = new InetSocketAddress(host, port);
            return this;
        }
        
        @Override
        public Builder withLocalAddress(InetSocketAddress address) {
            if (address == null) {
                throw new IllegalArgumentException("Address must not be null");
            }
            this.localAddress = address;
            return this;
        }
        
        @Override
        public Builder withKeepAlive(boolean keepAlive) {
            this.keepAliveEnabled = keepAlive;
            super.withKeepAlive(keepAlive);
            return this;
        }
        
        @Override
        public Builder withTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }
        
        @Override
        public Builder withLinger(int linger) {
            if (linger < 0) {
                throw new IllegalArgumentException("Linger must not be negative");
            }
            this.linger = linger;
            return this;
        }
        
        @Override
        public Builder withReceiveBufferSize(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Receive buffer size must be positive");
            }
            this.receiveBufferSize = size;
            return this;
        }
        
        @Override
        public Builder withSendBufferSize(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Send buffer size must be positive");
            }
            this.sendBufferSize = size;
            return this;
        }
        
        @Override
        public Builder withAutoReconnect(boolean autoReconnect) {
            this.autoReconnectEnabled = autoReconnect;
            return this;
        }
        
        @Override
        public Builder withReconnectBackoff(Duration initialBackoff, Duration maxBackoff, RetryBackoffStrategy strategy) {
            if (initialBackoff == null || initialBackoff.isNegative()) {
                throw new IllegalArgumentException("Initial backoff must not be null or negative");
            }
            if (maxBackoff == null || maxBackoff.isNegative()) {
                throw new IllegalArgumentException("Max backoff must not be null or negative");
            }
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy must not be null");
            }
            
            this.initialReconnectBackoff = initialBackoff;
            this.maxReconnectBackoff = maxBackoff;
            this.reconnectBackoffStrategy = strategy;
            return this;
        }
        
        @Override
        public Builder withMaxReconnectAttempts(int maxAttempts) {
            if (maxAttempts < 0) {
                throw new IllegalArgumentException("Max reconnect attempts must not be negative");
            }
            this.maxReconnectAttempts = maxAttempts;
            return this;
        }
        
        @Override
        public Builder withAutoConnect(boolean autoConnect) {
            this.autoConnectEnabled = autoConnect;
            return this;
        }
        
        @Override
        public Builder withSerializer(Serializer serializer) {
            if (serializer == null) {
                throw new IllegalArgumentException("Serializer must not be null");
            }
            this.serializer = serializer;
            return this;
        }
        
        @Override
        public Builder withCharset(Charset charset) {
            if (charset == null) {
                throw new IllegalArgumentException("Charset must not be null");
            }
            this.charset = charset;
            return this;
        }
        
        @Override
        public Builder configure(java.util.function.Consumer<TcpClientBuilder> configurer) {
            if (configurer == null) {
                throw new IllegalArgumentException("Configurer must not be null");
            }
            configurer.accept(this);
            return this;
        }
        
        @Override
        public TcpClientConfig build() {
            if (remoteAddress == null) {
                throw new IllegalStateException("Remote address must be set");
            }
            
            return new DefaultTcpClientConfig(this);
        }
        
        @Override
        public TcpClient build() {
            return new NettyTcpClient(build());
        }
    }
    
    /**
     * Creates a new builder for {@link DefaultTcpClientConfig}.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
}