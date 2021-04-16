/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月14日 下午1:52:08 <br> vc: $Id: $
 */

package io.netty.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.http.server.HttpServer;

/**
 * TODO 请填写注释.
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class HTTPServer1Test {
	protected final static Logger logger = LoggerFactory.getLogger(HTTPServer1Test.class);
	public final static int PORT = 8071;

	public static void main(String[] args) {
		HttpServer ts = new HttpServer();
		ts.setPort(PORT);
		ts.setDaemon(false);
		ts.setServiceHandler(new HttpApp1Handler());
		ts.setDebug(true);
		// ts.setShortConnection(true);
		logger.debug("HttpServer {} starting ...... ", ts);
		try {
			ts.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("HttpServer {} shutdown", ts);
	}
}
