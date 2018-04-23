/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.netty.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.tcp.netty.client.handler.DefaultClientHandler;
import io.netty.tcp.netty.message.HeartBeatMessage;
import io.netty.tcp.netty.message.handler.coding.impl.KyroObjectMsgDecoder;
import io.netty.tcp.netty.message.handler.coding.impl.KyroObjectMsgEncoder;
import io.netty.tcp.util.ExceptionUtil;

/**
 * 客户端通讯调度处理.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class NTcpClient extends AbstractClient {
	protected final static Logger logger = LoggerFactory.getLogger(NTcpClient.class);

	private ChannelInboundHandlerAdapter messageDecoder = null;

	private ChannelOutboundHandlerAdapter messageEncoder = null;

	private DefaultClientHandler clientHandler;

	private EventLoopGroup workerGroup = null;

	private List<Object> responses = new ArrayList<Object>();

	private int threadNumb = 2;

	public NTcpClient() {
	}

	public void setWriteTimeout(int writeTimeout) {
		if (this.heartbeatInervalSec > 0)
			this.writeTimeout = -1;
		else
			this.writeTimeout = writeTimeout;
	}
	
	public void setTimeout(int timeout) {
		super.setTimeout(timeout);
		if (this.writeTimeout <= 0)
			setWriteTimeout(timeout);
	}
	
	public void setHeartbeatInervalSec(int heartbeatInervalSec) {
		this.heartbeatInervalSec = heartbeatInervalSec;
		this.writeTimeout = -1;
	}
	
	public NTcpClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public ChannelInboundHandlerAdapter getMessageDecoder() {
		return messageDecoder;
	}

	public void setMessageDecoder(ChannelInboundHandlerAdapter messageDecoder) {
		this.messageDecoder = messageDecoder;
	}

	public ChannelOutboundHandlerAdapter getMessageEncoder() {
		return messageEncoder;
	}

	public void setMessageEncoder(ChannelOutboundHandlerAdapter messageEncoder) {
		this.messageEncoder = messageEncoder;
	}

	public DefaultClientHandler getClientHandler() {
		return clientHandler;
	}

	public void setClientHandler(DefaultClientHandler clientHandler) {
		this.clientHandler = clientHandler;
		this.clientHandler.setCreator(this);
	}

	public int getThreadNumb() {
		return threadNumb;
	}

	public void setThreadNumb(int threadNumb) {
		if (threadNumb > 0)
			this.threadNumb = threadNumb;
	}

	@Override
	public String toString() {
		return "NTcpClient [messageDecoder=" + messageDecoder + ", messageEncoder=" + messageEncoder
				+ ", clientHandler=" + clientHandler + ", workerGroup=" + workerGroup + ", responses=" + responses
				+ ", threadNumb=" + threadNumb + ", host=" + host + ", port=" + port + ", readTimeout=" + readTimeout
				+ ", writeTimeout=" + writeTimeout + ", connectTimeout=" + connectTimeout + ", heartbeatInervalSec="
				+ heartbeatInervalSec + ", timeout=" + timeout + ", alived=" + alived + "]";
	}

	private NTcpClient getThis() {
		return this;
	}
	
	/**
	 * 连接.
	 * 
	 */
	public void connect() {
		if (alived) {
			if (logger.isTraceEnabled())
				logger.trace("Connection to server {}:{} is alived.", host, port);
			return;
		}
		
		try {
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
				workerGroup = null;
			}
		} catch (Throwable th) {
			logger.warn("close the previous connection resource exception.", th);
		}

		if (logger.isTraceEnabled())
			logger.trace("create connection to server {}:{} ...", host, port);
		synchronized (host) {
			try {
				if (threadNumb > 0)
					workerGroup = new NioEventLoopGroup(threadNumb);
				else
					workerGroup = new NioEventLoopGroup();
				
				Bootstrap b = new Bootstrap();
				b.group(workerGroup);
				b.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.ALLOW_HALF_CLOSURE, true)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
				b.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						if (heartbeatInervalSec > 0) {
							logger.trace("add HeartBeat Handler with heartbeatInervalSec {}", heartbeatInervalSec);
			                ch.pipeline().addLast(new IdleStateHandler(0,heartbeatInervalSec, 0, TimeUnit.SECONDS)); 		
					} else {
							if (readTimeout > 0)
								ch.pipeline().addLast(new ReadTimeoutHandler(readTimeout));
							if (writeTimeout > 0)
								ch.pipeline().addLast(new WriteTimeoutHandler(writeTimeout));
						}
						
						ChannelInboundHandlerAdapter innerMessageDecoder = null;
						ChannelOutboundHandlerAdapter innerMessageEncoder = null;
						if (messageDecoder == null) {
							innerMessageDecoder = new KyroObjectMsgDecoder();
						} else {
							innerMessageDecoder = messageDecoder;
						}
						if (messageEncoder == null || messageDecoder instanceof KyroObjectMsgDecoder) {
							innerMessageEncoder = new KyroObjectMsgEncoder();
						} else {
							innerMessageEncoder = messageEncoder;
						}
						if (innerMessageDecoder != null)
							ch.pipeline().addLast(innerMessageDecoder);
						if (innerMessageEncoder != null)
							ch.pipeline().addLast(innerMessageEncoder);
						
						ch.pipeline().addLast(new ClientPlatHandler(getThis()));
					}
				});
				

				ChannelFuture f = b.connect(host, port).sync();
				int connTimeout = timeout > 0 ? timeout : connectTimeout;
				if (logger.isTraceEnabled())
					logger.trace("Connection request has sended to server {}:{}, wait for connection response, connTimeout {}, success ? {}", host, port,
							connTimeout, f.isSuccess());
				if (f.isSuccess()) {
					logger.debug("Connected channel {} ", f.channel());
				}

				alived = true;
				if (logger.isTraceEnabled())
					logger.trace("Connection to server {}:{} has created.", host, port);
				
			} catch (Throwable th) {
				close();
				throw new RuntimeException("Connection to server " + host + ":" + port + " failed.", th);			
			}

		}
	}


	/**
	 * 连接断开。
	 */
	public void disconnect() {
		if (clientHandler != null) {
			clientHandler.close();
		}
		alived = false;
	}

	/**
	 * 关闭客户端。
	 */
	public void close() {
		disconnect();
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
			workerGroup = null;
		}
	}

	/**
	 * 同步模式：发送请求>接收应答
	 * 
	 * @param request
	 * @return response
	 */
	public Object call(Object request) {
		if (clientHandler == null) {
			setClientHandler(new DefaultClientHandler());
		}
		if (!alived)
			connect();
		return clientHandler.call(request);
	}

	/**
	 * 发送请求.
	 * 
	 * @param request
	 */
	public void send(Object request) {
		if (clientHandler == null) {
			setClientHandler(new DefaultClientHandler());
		}
		if (!alived)
			connect();

		clientHandler.send(request);
	}

	/**
	 * 接收应答(阻塞模式).
	 * 
	 * @param response
	 * @return
	 */
	public Object recv() {
		if (clientHandler == null) {
			setClientHandler(new DefaultClientHandler());
		}
		if (!alived)
			connect();
		return clientHandler.recv();
	}

	private class ClientPlatHandler extends ChannelInboundHandlerAdapter {
		private NTcpClient owner;
		
		public ClientPlatHandler() {
		}

		public ClientPlatHandler(NTcpClient owner) {
			setOwner(owner);
		}

		public NTcpClient getOwner() {
			return owner;
		}


		public void setOwner(NTcpClient owner) {
			this.owner = owner;
		}

		@Override  
	    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {  
	        if (evt instanceof IdleStateEvent) {  
        		//logger.info("{} IdleStateEvent trigged {}.", ctx, evt);
        		
	            IdleStateEvent e = (IdleStateEvent) evt;  
	            switch (e.state()) {  
	                case WRITER_IDLE:
	                	if (owner.getHeartbeatInervalSec() > 0) {
	                		logger.trace("Write Idle for {}s , send ping.", owner.getHeartbeatInervalSec());
	                		owner.send(HeartBeatMessage.KEY_PING);
	                	}
	                   break;

	                default:  
	                    break;  
	            }  
	        }  
	        ctx.fireUserEventTriggered(evt);
	    } 
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			if (logger.isTraceEnabled())
				logger.trace("{} Active Channel {}", this, ctx.channel());
			synchronized (workerGroup) {
				if (clientHandler != null) {
					clientHandler.activateChannel(ctx.channel());
					if (clientHandler.isComplete()) {
						ctx.close();
						disconnect();
					}
				}
				workerGroup.notifyAll();
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			if (logger.isTraceEnabled())
				logger.trace("{} on channelInactive ...", this);
			if (clientHandler != null) {
				clientHandler.onChannelClosed(ctx.channel());
			}
			disconnect();
			ctx.fireChannelInactive();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object response) throws Exception {
			if (logger.isTraceEnabled())
				logger.trace("{} Read from Channel {} with message ({}) {}", this, ctx.channel(),
						(response == null ? "null" : response.getClass()), response);

			if (clientHandler != null) {
				clientHandler.recvResponse(response);
				if (clientHandler.isComplete()) {
					ctx.close();
					disconnect();
				}
			} else {
				responses.add((Object) response);
				logger.info("Received response : {}", (Object) response);
				ctx.close();
				disconnect();
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.fireExceptionCaught(cause);			
			if (clientHandler != null) {
				clientHandler.onChannelException(ctx.channel(), cause);
			}
			if (cause != null) {
				if ((cause instanceof ReadTimeoutException)
					|| (cause instanceof WriteTimeoutException)) {
					if (logger.isTraceEnabled())
					logger.trace("{} client channel exceptionCaught : {}", this, ExceptionUtil.getStackTrace(cause));		
				} else {
					ctx.close();
					disconnect();
					logger.error("{} close the context {} for the causedException {}", this, cause);
				}
			} else {					
				logger.warn("{} client channel exceptionCaught : {}", this, cause);				
			}
			
		}

	}

}