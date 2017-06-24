/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package testor.io.netty.tcp.monit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.tcp.server.ServiceAppHandler;

public class MonitServHandler implements ServiceAppHandler {
	protected final static Logger logger = LoggerFactory.getLogger(MonitServHandler.class);

	private int numb = 0;

	/**
	 * 
	 */
	public MonitServHandler() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.netty.tcp.server.ServiceAppHandler#call(java.lang.
	 * Object, io.netty.channel.Channel)
	 */
	@Override
	public synchronized Object call(Object request, Channel channel) {
		++numb;
		logger.info(numb + "	recved: " + request);
//		return numb + "," + System.currentTimeMillis();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.netty.tcp.server.ServiceAppHandler#exceptionCaught(io.
	 * netty.channel.Channel, java.lang.Throwable)
	 */
	@Override
	public void onChannelException(Channel channel, Throwable cause) {
		logger.info("collector exception, have  received " + numb + " item(s), " + channel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.netty.tcp.server.ServiceAppHandler#onChannelClosed(io.
	 * netty.channel.Channel)
	 */
	@Override
	public void onChannelClosed(Channel channel) {
		logger.info("collector disconnect, have  received " + numb + " item(s), " + channel);
	}

}
