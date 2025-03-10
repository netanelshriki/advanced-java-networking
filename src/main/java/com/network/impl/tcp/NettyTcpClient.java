package com.network.impl.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.network.api.connection.Connection;
import com.network.api.tcp.TcpClientConfig;
import com.network.exception.ConnectionException;
import com.network.exception.NetworkException.ErrorCode;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Netty-based implementation of the TCP client.
 * 
 * <p>This class uses Netty as the underlying network framework to provide
 * high-performance TCP client functionality.
 */
public class NettyTcpClient extends AbstractTcpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyTcpClient.class);
    
    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;
    private final NettyTcpClientHandler handler;
    
    private Channel channel;
    
    /**
     * Creates a new Netty TCP client with the specified configuration.
     * 
     * @param config the client configuration
     */
    public NettyTcpClient(TcpClientConfig config) {
        super(config);
        
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.handler = new NettyTcpClientHandler(this);
        
        initBootstrap();
    }
    
    /**
     * Initializes the Netty bootstrap with the client configuration.
     */
    private void initBootstrap() {
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, config.isKeepAliveEnabled())
            .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
            .option(ChannelOption.SO_LINGER, config.getLinger())
            .option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
            .option(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getConnectionTimeout().toMillis())
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(handler);
                }
            });
        
        // Set local address if configured
        config.getLocalAddress().ifPresent(localAddress -> {
            bootstrap.localAddress(localAddress);
        });
    }
    
    @Override
    protected void doConnect() throws Exception {
        if (channel != null && channel.isOpen()) {
            return;
        }
        
        InetSocketAddress remoteAddress = config.getRemoteAddress();
        
        try {
            // Establish connection
            ChannelFuture future = bootstrap.connect(remoteAddress).sync();
            if (!future.isSuccess()) {
                throw new ConnectionException(ErrorCode.CONNECTION_ERROR, 
                    remoteAddress, null, null, future.cause());
            }
            
            channel = future.channel();
            
            // Create connection wrapper
            connection = new NettyTcpConnection(channel);
            
            // Notify listeners
            notifyConnect(connection);
            
            logger.debug("Connected to {}", remoteAddress);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException(ErrorCode.CONNECTION_TIMEOUT, 
                remoteAddress, null, null, "Connection interrupted");
        }
    }
    
    @Override
    protected void doDisconnect() throws Exception {
        if (channel != null) {
            try {
                Connection oldConnection = connection;
                
                // Close the channel
                channel.close().sync();
                channel = null;
                
                // Notify listeners
                if (oldConnection != null) {
                    notifyDisconnect(oldConnection, "Client disconnected");
                }
                
                logger.debug("Disconnected from {}", config.getRemoteAddress());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Disconnection interrupted", e);
            }
        }
    }
    
    @Override
    public void close() {
        super.close();
        
        // Shut down event loop group
        if (eventLoopGroup != null && !eventLoopGroup.isShutdown()) {
            try {
                eventLoopGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while shutting down event loop group", e);
            }
        }
    }
    
    /**
     * Gets the Netty channel associated with this client.
     * 
     * @return the Netty channel, or null if not connected
     */
    public Channel getChannel() {
        return channel;
    }
    
    /**
     * Called by the handler when data is received.
     * 
     * @param data the received data
     */
    void onDataReceived(byte[] data) {
        if (connection instanceof NettyTcpConnection) {
            ((NettyTcpConnection) connection).handleDataReceived(data);
        }
        notifyDataReceived(connection, data);
    }
    
    /**
     * Called by the handler when the channel is active (connected).
     */
    void onChannelActive() {
        // Nothing to do here, connection is already established in doConnect
    }
    
    /**
     * Called by the handler when the channel is inactive (disconnected).
     */
    void onChannelInactive() {
        if (connected.compareAndSet(true, false)) {
            Connection oldConnection = connection;
            connection = null;
            
            // Notify listeners
            if (oldConnection != null) {
                notifyDisconnect(oldConnection, "Connection closed by remote host");
            }
            
            // Start reconnect if enabled
            if (config.isAutoReconnectEnabled()) {
                scheduleReconnect();
            }
        }
    }
    
    /**
     * Called by the handler when an exception occurs.
     * 
     * @param cause the exception
     */
    void onExceptionCaught(Throwable cause) {
        logger.error("Exception caught: {}", cause.getMessage(), cause);
        
        // Notify listeners
        if (connection != null) {
            notifyError(connection, cause);
        }
    }
}