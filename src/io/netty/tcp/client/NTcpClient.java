/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.client;

import java.util.ArrayList;
import java.util.List;

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
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.tcp.client.handler.DefaultClientHandler;
import io.netty.tcp.message.handler.coding.impl.KyroObjectMsgDecoder;
import io.netty.tcp.message.handler.coding.impl.KyroObjectMsgEncoder;
import io.netty.tcp.util.ExceptionUtil;

/**
 * 客户端通讯调度处理.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class NTcpClient {
	protected final static Logger logger = LoggerFactory.getLogger(NTcpClient.class);

	public String host;

	public int port;

	private int readTimeout = 0;
	
	public int DEFAULT_READTIMEOUT = 10;

	private int writeTimeout = 0;

	public int DEFAULT_WRITETIMEOUT = 3;

	private int connectTimeout = 10;

	private int timeout = -1;

	private ChannelInboundHandlerAdapter messageDecoder = null;

	private ChannelOutboundHandlerAdapter messageEncoder = null;

	private DefaultClientHandler clientHandler;

	private EventLoopGroup workerGroup = null;

	private List<Object> responses = new ArrayList<Object>();

	private int threadNumb = 2;

	private boolean isAlive = false;

	public NTcpClient() {
	}

	public NTcpClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(int writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		if (timeout < 0)
			return;
		this.timeout = timeout;
		if (this.readTimeout <= 0)
			this.readTimeout = timeout;
		if (this.writeTimeout <= 0)
			this.writeTimeout = timeout;
		if (this.connectTimeout < 0)
			this.connectTimeout = timeout;
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
		return "NTcpClient [host=" + host + ", port=" + port + ", readTimeout=" + readTimeout + ", writeTimeout="
				+ writeTimeout + ", connectTimeout=" + connectTimeout + ", timeout=" + timeout + ", clientHandler="
				+ clientHandler + ", workerGroup=" + workerGroup + ", responses=" + responses + ", threadNumb="
				+ threadNumb + ", isAlive=" + isAlive + "]";
	}

	/**
	 * 连接.
	 * 
	 * @throws InterruptedException
	 */
	public void connect() throws InterruptedException {
		if (workerGroup == null) {
			synchronized (host) {
				if (threadNumb > 0)
					workerGroup = new NioEventLoopGroup(threadNumb);
				else
					workerGroup = new NioEventLoopGroup();
			}
		}
		synchronized (workerGroup) {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					if (readTimeout > 0)
						ch.pipeline().addLast(new ReadTimeoutHandler(readTimeout));
					if (writeTimeout > 0)
						ch.pipeline().addLast(new WriteTimeoutHandler(writeTimeout));

					if (messageDecoder == null) {
						messageDecoder = new KyroObjectMsgDecoder();
					}
					if (messageEncoder == null || messageDecoder instanceof KyroObjectMsgDecoder) {
						messageEncoder = new KyroObjectMsgEncoder();
					}
					if (messageDecoder != null)
						ch.pipeline().addLast(messageDecoder);
					if (messageEncoder != null)
						ch.pipeline().addLast(messageEncoder);
					
					ch.pipeline().addLast(new ClientPlatHandler());
				}
			});
			b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

			ChannelFuture f = b.connect(host, port).sync();
			int connTimeout = timeout > 0 ? timeout : connectTimeout;
			if (logger.isTraceEnabled())
				logger.trace("Connection request sended to server {}:{}, wait for create, timeout {}", host, port, connTimeout);
			workerGroup.wait(connTimeout * 1000);
			isAlive = true;
			if (logger.isTraceEnabled())
				logger.trace("Connection to server {}:{} has created.", host, port);
		}
	}

	/**
	 * 连接断开。
	 */
	public void disconnect() {
		if (clientHandler != null) {
			clientHandler.close();
		}
		isAlive = false;
	}

	/**
	 * 关闭客户端。
	 */
	public void close() {
		disconnect();
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
			// workerGroup = null;
		}
	}

	/**
	 * 同步模式：发送请求>接收应答
	 * 
	 * @param request
	 * @return response
	 * @throws InterruptedException
	 */
	public Object call(Object request) throws InterruptedException {
		if (clientHandler == null) {
			setClientHandler(new DefaultClientHandler());
		}
		if (!isAlive)
			connect();
		return clientHandler.call(request);
	}

	/**
	 * 发送请求.
	 * 
	 * @param request
	 * @throws InterruptedException
	 */
	public void send(Object request) throws InterruptedException {
		if (clientHandler == null) {
			setClientHandler(new DefaultClientHandler());
		}
		if (!isAlive)
			connect();

		clientHandler.send(request);
	}

	/**
	 * 接收应答(阻塞模式).
	 * 
	 * @param response
	 * @return
	 * @throws InterruptedException
	 */
	public Object recv() throws InterruptedException {
		if (clientHandler == null) {
			setClientHandler(new DefaultClientHandler());
		}
		if (!isAlive)
			connect();
		return clientHandler.recv();
	}

	private class ClientPlatHandler extends ChannelInboundHandlerAdapter {

		public ClientPlatHandler() {
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			if (logger.isTraceEnabled())
				logger.trace("{} Active Channel {}" , this, ctx.channel());
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
				logger.trace("{} Read from Channel {} with message ({}) {}", this, ctx.channel(), (response == null ? "null" : response.getClass()), response);

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
			logger.warn("{} client channel exceptionCaught : {}", this, ExceptionUtil.getStackTrace(cause));
			if (clientHandler != null) {
				clientHandler.onChannelException(ctx.channel(), cause);
			}
			if (cause != null && !(cause instanceof ReadTimeoutException) && !(cause instanceof WriteTimeoutException)) {
				ctx.close();
				disconnect();
				logger.error("close the context {} for the causedException {}" , ctx, cause);
			}
		}

	}

}