/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月15日 上午11:59:38 <br> vc: $Id: $
 */

package io.netty.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.http.client.HttpClient;

/**
 * TODO 请填写注释.
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class HttpClient1Test {
	protected final static Logger logger = LoggerFactory.getLogger(HttpClient1Test.class);

	public static void main(String[] args) {
		String url = "http://127.0.0.1:" + HTTPServer1Test.PORT ;//+ "/#";
		String result = HttpClient.doGet(url);
		logger.debug("doGet:" + result);
		
		url = "http://127.0.0.1:" + HTTPServer1Test.PORT ;//+ "/45001";
		
		result = HttpClient.doPost(url, "text/json", "{ \"kind\" : \"1\"}");
		logger.debug("doPost:" + result);

		result = HttpClient.doPost(url, "text/xml", "<hello>lawn</hello>");
		logger.debug("doPost:" + result);
	}
}
