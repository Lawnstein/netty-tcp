/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.netty.client.handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.tcp.netty.client.ClientAppHandler;
import io.netty.tcp.netty.client.NTcpClient;

/**
 * 默认自定义客户端处理,实现同步通讯（一问一答）、异步通讯（各问各答，异步调用）.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class DefaultClientHandler implements ClientAppHandler {
	protected final static Logger logger = LoggerFactory.getLogger(DefaultClientHandler.class);

	private BlockingQueue<Object> responseQueue;

	/**
	 * 建立的连接通道.
	 */
	private Channel channel;

	private boolean isAlive = false;

	private NTcpClient creator;

	public DefaultClientHandler() {
		this.responseQueue = new LinkedBlockingQueue();
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.tcp.netty.client.ClientAppHandler#activateChannel(io.netty.
	 * channel.Channel)
	 */
	@Override
	public void activateChannel(Channel channel) {
		this.channel = channel;
		this.isAlive = true;
		logger.trace("Connection channel created ： {}", this.channel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.tcp.netty.client.ClientAppHandler#recvResponse(java.lang.
	 * Object)
	 */
	@Override
	public void recvResponse(Object response) {
		try {
			responseQueue.put(response);
		} catch (InterruptedException e) {
			logger.error("put response to queue InterruptedException : {}", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.netty.tcp.netty.client.ClientAppHandler#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return false;
		// return !this.isAlive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.tcp.netty.client.ClientAppHandler#onChannelClosed(io.netty.
	 * channel.Channel)
	 */
	@Override
	public void onChannelClosed(Channel channel) {
		logger.trace("Channel {} closed .", channel);
		close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.tcp.netty.client.ClientAppHandler#onChannelException(io.
	 * netty.channel.Channel, java.lang.Throwable)
	 */
	@Override
	public void onChannelException(Channel channel, Throwable cause) {
	}

	public void close() {
		this.isAlive = false;
		if (this.channel != null) {
			if (this.channel.isActive()) this.channel.close();
			if (this.channel.isOpen()) this.channel.close();
		}
	}

	@Override
	public void setCreator(NTcpClient creator) {
		this.creator = creator;
	}

	/**
	 * 同步通讯：发送一个请求，阻塞等待应答，收到应答后返回。
	 * 
	 * @param request
	 * @return response
	 */
	public Object call(Object request) {
		if (request != null)
			send(request);
		return recv();
	}

	/**
	 * 发送调用.
	 * 
	 * @param request
	 */
	public void send(Object request) {
		if (request != null) {
			if (channel == null) {
				logger.error("The socket channel not prepared.");
				throw new RuntimeException("The socket channel not prepared.");
			}
			channel.writeAndFlush(request);
		}
	}

	/**
	 * 接收调用.
	 * 
	 * @param response
	 * @throws InterruptedException
	 */
	public Object recv() {
		Object rsp = null;
		try {
			int to = creator.getReadTimeout() <= 0 ? creator.DEFAULT_READTIMEOUT : creator.getReadTimeout();
			rsp = responseQueue.poll(to, TimeUnit.SECONDS);
			if (rsp == null) {
				logger.error("Recv from server timeout({}, {})", creator.getReadTimeout(), TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			logger.error("recv from responseQueue InterruptedException {}", e);
		}
		return rsp;
	}


}
