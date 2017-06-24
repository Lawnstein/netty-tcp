/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package testor.io.netty.tcp.monit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.netty.tcp.server.TcpServer;
import testor.io.netty.multi.MultiClient;

/**
 * 监控服务端.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class MonitServer {
	protected final static Logger logger = LoggerFactory.getLogger(MultiClient.class);

	/**
	 * 
	 */
	public MonitServer() {
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		MDC.put("TRCODE", "MonitServer");
		TcpServer server = new TcpServer();
		server.setServiceHandler(new MonitServHandler());
		server.setPort(8100);
		server.start();
	}

}
