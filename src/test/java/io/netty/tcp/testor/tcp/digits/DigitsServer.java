/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.testor.tcp.digits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.netty.tcp.netty.message.handler.coding.impl.DigitsHeadByteMsgDecoder;
import io.netty.tcp.netty.message.handler.coding.impl.DigitsHeadByteMsgEncoder;
import io.netty.tcp.netty.server.TcpServer;

/**
 * echo server.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class DigitsServer {
	protected final static Logger logger = LoggerFactory.getLogger(DigitsServer.class);

	public DigitsServer() {
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		MDC.put("TRCODE", "Server");
		TcpServer server = new TcpServer();
		server.setMessageDecoder(new DigitsHeadByteMsgDecoder(8));
		server.setMessageEncoder(new DigitsHeadByteMsgEncoder(8));
		server.setServiceHandler(new DigitsServerHandler());
		server.setPort(8000);
//		server.setShortConnection(true);
		server.start();
	}

}
