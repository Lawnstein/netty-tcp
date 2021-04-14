/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月14日 上午11:22:26 <br> vc: $Id: $
 */

package io.netty.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.http.server.HttpAppHandler;

/**
 * TODO 请填写注释.
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class HttpApp1Handler extends HttpAppHandler {
	protected final static Logger logger = LoggerFactory.getLogger(HttpApp1Handler.class);

	@Override
	public Object doCall(FullHttpRequest request) {
		Map<String, Object> getMap = getGetParameters(request);
		Map<String, Object> formMap = getFormParameters(request);
		String bodyStr = getBodyString(request);
		logger.debug("getMap: {}", getMap);
		logger.debug("formMap: {}", formMap);
		logger.debug("bodyStr: {}", bodyStr);

		String responseStr = null;
		if (isJsonContentType(request)) {
			responseStr = "{ \"timeStamp\" : " + System.currentTimeMillis() + "}";
		} else if (isXmlContentType(request)) {
			responseStr = "<timeStamp>" + System.currentTimeMillis() + "</timeStamp>";
		} else {
			responseStr = "" + System.currentTimeMillis();
		}
		return responseStr;
	}

}
