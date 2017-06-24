/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgDecoder;
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgEncoder;
import io.netty.tcp.message.handler.coding.impl.NoneHeadByteMsgEncoder;
import io.netty.tcp.util.ExceptionUtil;

/**
 * channel处理.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
@Sharable
public class ServerMessageHandler extends ChannelInboundHandlerAdapter {
	protected final static Logger logger = LoggerFactory.getLogger(TcpServer.class);

	protected static String KEY_HEARTBEAT = "HEARTBEAT";
	protected static String KEY_ALIVE = "ALIVE";
	protected static byte[] KEY_ALIVE_BODY_BYTES = KEY_ALIVE.getBytes();
	
	private AbstractFixedLengthHeaderByteMsgDecoder messageDecoder = null;

	private AbstractFixedLengthHeaderByteMsgEncoder messageEncoder = null;
	
	private ServiceAppHandler serviceHandler;

	private String name;

	private int minServiceThreads = 0;

	private int maxServiceThreads = 0;

	private int threadKeepAliveSeconds = 0;

	private boolean shortConnection = false;

	/**
	 * 升级成Akka？
	 */
	private final static Map<String, ExecutorService> threadPoolMap = new HashMap<String, ExecutorService>();

	public ServerMessageHandler(ServiceAppHandler serviceHandler) {
		super();
		this.serviceHandler = serviceHandler;
	}

	public AbstractFixedLengthHeaderByteMsgDecoder getMessageDecoder() {
		return messageDecoder;
	}

	public void setMessageDecoder(AbstractFixedLengthHeaderByteMsgDecoder messageDecoder) {
		this.messageDecoder = messageDecoder;
	}

	public AbstractFixedLengthHeaderByteMsgEncoder getMessageEncoder() {
		return messageEncoder;
	}

	public void setMessageEncoder(AbstractFixedLengthHeaderByteMsgEncoder messageEncoder) {
		this.messageEncoder = messageEncoder;
	}

	public int getMaxServiceThreads() {
		return maxServiceThreads;
	}

	public void setMaxServiceThreads(int maxServiceThreads) {
		this.maxServiceThreads = maxServiceThreads;
	}

	public boolean isShortConnection() {
		return shortConnection;
	}

	public void setShortConnection(boolean shortConnection) {
		this.shortConnection = shortConnection;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ServiceAppHandler getServiceHandler() {
		return serviceHandler;
	}

	public void setServiceHandler(ServiceAppHandler serviceHandler) {
		this.serviceHandler = serviceHandler;
	}

	public int getMinServiceThreads() {
		return minServiceThreads;
	}

	public void setMinServiceThreads(int minServiceThreads) {
		this.minServiceThreads = minServiceThreads;
	}

	public int getThreadKeepAliveSeconds() {
		return threadKeepAliveSeconds;
	}

	public void setThreadKeepAliveSeconds(int threadKeepAliveSeconds) {
		this.threadKeepAliveSeconds = threadKeepAliveSeconds;
	}

	public ExecutorService getExcutorThread() {
		
		if (threadPoolMap.containsKey(name)) 
			return threadPoolMap.get(name);
		
		ExecutorService excutor = null;
		synchronized (threadPoolMap) {
			if (threadPoolMap.containsKey(name)) 
				excutor = threadPoolMap.get(name);
			else {
//				excutor =  new ThreadPoolExecutor(this.minServiceThreads,
//					this.maxServiceThreads, threadKeepAliveSeconds, TimeUnit.SECONDS,
//					new LinkedBlockingQueue<Runnable>());
				
				excutor = Executors.newFixedThreadPool(maxServiceThreads);
				threadPoolMap.put(name, excutor);
			}
		}
		return excutor;
	}
	
	public static void stopThreadPool(String name) {
		if (threadPoolMap.containsKey(name)) {
			threadPoolMap.get(name).shutdown();
			threadPoolMap.remove(name);
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelRegistered();
		if (TcpServer.isDebug() && logger.isDebugEnabled())
			logger.debug("{} registered.", ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelUnregistered();
		if (TcpServer.isDebug() && logger.isDebugEnabled())
			logger.debug("{} unregistered.", ctx);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelActive();
		if (TcpServer.isDebug() && logger.isDebugEnabled())
			logger.debug("{} active.", ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.serviceHandler.onChannelClosed(ctx.channel());
		ctx.fireChannelInactive();

		if (TcpServer.isDebug() && logger.isDebugEnabled())
			logger.debug("{} inactive.", ctx);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object request) throws Exception {
		if (request == null) {
			if (TcpServer.isDebug() && logger.isDebugEnabled())
				logger.debug("{} request is null, ignore it.", ctx);
			return;
		}
		
		if (!(request instanceof byte[])) {
			if (TcpServer.isDebug() && logger.isDebugEnabled())
				logger.error("{} request is not byte array type ({}), ignore it.", ctx, request.getClass());
			return;
		}
		
		byte[] body = (byte[]) request;
		if (body != null && body.length == KEY_HEARTBEAT.length() && KEY_HEARTBEAT.equals(new String(body))) {
			
			if (this.messageEncoder == null || (this.messageEncoder instanceof NoneHeadByteMsgEncoder)) {
				ctx.write(this.messageDecoder.getHeadLengthType().toBytes(messageDecoder.getHeaderLengthSize(), messageDecoder.isHeaderLengthIncluded(), KEY_ALIVE_BODY_BYTES.length));
			}
			
			if (shortConnection)
				ctx.writeAndFlush(KEY_ALIVE_BODY_BYTES).addListener(ChannelFutureListener.CLOSE);
			else
				ctx.writeAndFlush(KEY_ALIVE_BODY_BYTES);

			if (TcpServer.isDebug() && logger.isDebugEnabled())
				logger.debug("{} HeartBeat alived.", ctx, request.getClass());
			
			return;
		}

		if (TcpServer.isDebug() && logger.isDebugEnabled())
			logger.debug("{} Read from Channel with request message {}", ctx,
					 new String(body));
		
		getExcutorThread().execute(new Runnable() {

			@Override
			public void run() {

				Object response = serviceHandler.call(request, ctx.channel());
				if (response != null) {
					if (shortConnection)
						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					else
						ctx.writeAndFlush(response);

					if (TcpServer.isDebug() && logger.isDebugEnabled())
						logger.debug("{} Write to Channel with response message : {}", ctx, new String((byte[])response));
				} else {
					if (TcpServer.isDebug() && logger.isDebugEnabled())
						logger.debug("{} Write to Channel with none response message", ctx);
				}
			}
		});

	}

//	@Override
//	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//		ctx.fireChannelReadComplete();
//	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.fireExceptionCaught(cause);
		logger.warn("{} server channel exceptionCaught : {}", this, ExceptionUtil.getStackTrace(cause));
		this.serviceHandler.onChannelException(ctx.channel(), cause);
		if (cause != null && !(cause instanceof ReadTimeoutException) && !(cause instanceof WriteTimeoutException)) {
			ctx.close();
			logger.error("close the context {} for the causedException {}", ctx, cause);
		}
	}
	
}