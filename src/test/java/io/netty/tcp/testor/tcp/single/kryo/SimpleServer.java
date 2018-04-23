/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.testor.tcp.single.kryo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.netty.tcp.netty.server.TcpServer;

/**
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class SimpleServer {
	protected final static Logger logger = LoggerFactory.getLogger(SimpleServer.class);

	/**
	 * 
	 */
	public SimpleServer() {
		// TODO 自动生成的构造函数存根
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		MDC.put("TRCODE", "Server");
		TcpServer server = new TcpServer();
		server.setServiceHandler(new SimpleServerHandler());
		server.setPort(8000);
		server.setDebug(true);
		server.start();
	}

}
