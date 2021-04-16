/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月14日 上午11:22:26 <br> vc: $Id: $
 */

package io.netty.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.handler.ServiceAppHandler;

/**
 * TODO 请填写注释.
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class TcpApp1Handler implements ServiceAppHandler {
	protected final static Logger logger = LoggerFactory.getLogger(TcpApp1Handler.class);

	@Override
	public Object call(Object request, Channel channel) {
		logger.debug("recv {}", new String((byte[]) request));
		String res = "Response" + System.currentTimeMillis();
		// return res.getBytes();
		return res;
	}

	@Override
	public void onChannelClosed(Channel channel) {
	}

	@Override
	public void onChannelException(Channel channel, Throwable cause) {
	}

}
