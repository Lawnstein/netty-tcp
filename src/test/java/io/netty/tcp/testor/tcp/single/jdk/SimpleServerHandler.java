/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.testor.tcp.single.jdk;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.tcp.server.ServiceAppHandler;

/**
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class SimpleServerHandler implements ServiceAppHandler {
	protected final static Logger logger = LoggerFactory.getLogger(SimpleServerHandler.class);

	/**
	 * 
	 */
	public SimpleServerHandler() {
		// TODO 自动生成的构造函数存根
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.csii.ccbs.tcp.netty.server.ServiceAppHandler#call(java.lang.Object,
	 * io.netty.channel.Channel)
	 */
	@Override
	public Object call(Object request, Channel channel) {
		Map map = (Map) request;
		map.put("ServerTimeStamp", System.currentTimeMillis());
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.csii.ccbs.tcp.netty.server.ServiceAppHandler#onChannelClosed(io.netty
	 * .channel.Channel)
	 */
	@Override
	public void onChannelClosed(Channel channel) {
		// TODO 自动生成的方法存根

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.csii.ccbs.tcp.netty.server.ServiceAppHandler#exceptionCaught(io.netty
	 * .channel.Channel, java.lang.Throwable)
	 */
	@Override
	public void onChannelException(Channel channel, Throwable cause) {
		// TODO 自动生成的方法存根

	}

}
