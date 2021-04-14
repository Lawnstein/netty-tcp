/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月14日 上午10:55:54 <br> vc: $Id: $
 */

package io.netty.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.message.handler.coding.impl.DigitsHeadByteMsgDecoder;
import io.netty.tcp.message.handler.coding.impl.DigitsHeadByteMsgEncoder;
import io.netty.tcp.server.TcpServer;

/**
 * TcpServer服务器.
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class TcpServer1Test {

	protected final static Logger logger = LoggerFactory.getLogger(TcpServer1Test.class);
	public final static int PORT = 8001;

	public static void main(String[] args) {
		DigitsHeadByteMsgDecoder decoder = new DigitsHeadByteMsgDecoder(8);
		DigitsHeadByteMsgEncoder encoder = new DigitsHeadByteMsgEncoder(8);
		TcpServer ts = new TcpServer();
		ts.setPort(PORT);
		ts.setDaemon(false);
		ts.setMessageDecoder(decoder);
		ts.setMessageEncoder(encoder);
		ts.setServiceHandler(new TcpApp1Handler());
		ts.setDebug(true);
		logger.debug("TcpServer {} starting ...... ", ts);
		try {
			ts.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("TcpServer {} shutdown", ts);
	}
}
