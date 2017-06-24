/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package testor.io.netty.multi;

import io.netty.tcp.server.TcpServer;

/**
 * TODO 请填写注释.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class MultiServer {

	public MultiServer() {
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		TcpServer server = new TcpServer();
		server.setServiceHandler(new MultiServerHandler());
		server.setPort(8000);
		server.start();
	}

}
