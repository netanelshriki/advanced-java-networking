package com.network.impl.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Netty channel handler for TCP clients.
 * 
 * <p>This handler processes inbound channel events and forwards them
 * to the associated TCP client.
 */
public class NettyTcpClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyTcpClientHandler.class);
    
    private final NettyTcpClient client;
    
    /**
     * Creates a new Netty TCP client handler.
     * 
     * @param client the associated TCP client
     */
    public NettyTcpClientHandler(NettyTcpClient client) {
        this.client = client;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel active: {}", ctx.channel().remoteAddress());
        client.onChannelActive();
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel inactive: {}", ctx.channel().remoteAddress());
        client.onChannelInactive();
        super.channelInactive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // Extract bytes from ByteBuf
        byte[] data = new byte[msg.readableBytes()];
        msg.readBytes(data);
        
        // Forward to client
        client.onDataReceived(data);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught in channel handler: {}", cause.getMessage(), cause);
        client.onExceptionCaught(cause);
        ctx.close();
    }
}