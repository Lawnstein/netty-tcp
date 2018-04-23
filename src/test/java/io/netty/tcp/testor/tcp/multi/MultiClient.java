/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.testor.tcp.multi;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.netty.client.NTcpClient;

/**
 * TODO 请填写注释.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class MultiClient {

	protected final static Logger logger = LoggerFactory.getLogger(MultiClient.class);

	public MultiClient() {
	}

	public static void main(String[] args) throws InterruptedException {
		NTcpClient client = new NTcpClient();
		client.setHost("127.0.0.1");
		client.setPort(8000);
//		client.setReadTimeout(3);
		Map req = new HashMap();
		req.put("seq", 1L);
		req.put("clientStamp", System.currentTimeMillis());
		logger.info("11111111111111==============================");
		client.send(req);
		logger.info("22222222222222==============================");
		Object responses = client.call(req);
		logger.info("==============================");
		logger.info(responses + "");
		logger.info("==============================");
		client.close();
		logger.info("3333333333333==============================");
		client.send(req);
		logger.info("44444444444==============================");
		responses = client.call(req);
		logger.info("-----------------------------------------");
		logger.info(responses + "");
		logger.info("-----------------------------------------");
		for (int i = 0; i < 1000; i++) {
			try {
			responses = client.call(req);
			logger.info("************************************************");
			} catch (Throwable th) {
				th.printStackTrace();
			}
			Thread.sleep(2000);
		}
		System.exit(0);
	}
}
