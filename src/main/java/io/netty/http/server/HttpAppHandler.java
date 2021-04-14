/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月14日 下午4:18:27 <br> vc: $Id: $
 */

package io.netty.http.server;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.ServiceAppHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * HTTP的应用报文处理..
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public abstract class HttpAppHandler implements ServiceAppHandler {

	protected Charset getContentEncoding(FullHttpRequest request) {
		String contentEncoding = request.headers().get(CONTENT_ENCODING);
		if (contentEncoding == null) {
			return io.netty.util.CharsetUtil.UTF_8;
		}
		return Charset.forName(contentEncoding);
	}

	protected String getContentType(FullHttpRequest request) {
		return request.headers().get(CONTENT_TYPE);
	}

	/**
	 * "x-www-form-urlencoded" <br>
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isFormContentType(FullHttpRequest request) {
		String contentType = getContentType(request);
		return contentType == null ? false : contentType.toLowerCase().contains("form");
	}

	/**
	 * text/json <br>
	 * application/json <br>
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isJsonContentType(FullHttpRequest request) {
		String contentType = getContentType(request);
		return contentType == null ? false : contentType.toLowerCase().contains("/json");
	}

	/**
	 * text/xml <br>
	 * application/xml <br>
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isXmlContentType(FullHttpRequest request) {
		String contentType = getContentType(request);
		return contentType == null ? false : contentType.toLowerCase().contains("/xml");
	}

	/**
	 * text/plain <br>
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isPlainContentType(FullHttpRequest request) {
		String contentType = getContentType(request);
		return contentType == null ? false : contentType.toLowerCase().contains("/plain");
	}

	/**
	 * text/* <br>
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isTextContentType(FullHttpRequest request) {
		String contentType = getContentType(request);
		return contentType == null ? false : contentType.toLowerCase().contains("text/");
	}

	/**
	 * 是否未指定Content-Type
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isNoneContentType(FullHttpRequest request) {
		String contentType = getContentType(request);
		return contentType == null;
	}

	protected Map<String, Object> getGetParameters(FullHttpRequest request) {
		// if (request.method() != HttpMethod.GET) {
		// return null;
		// }

		Map<String, Object> params = new HashMap<String, Object>();
		QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
		Map<String, List<String>> paramList = decoder.parameters();
		for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
			params.put(entry.getKey(), entry.getValue().get(0));
		}
		return params;
	}

	protected Map<String, Object> getFormParameters(FullHttpRequest request) {
		// if (request.method() != HttpMethod.POST) {
		// return null;
		// }
		if (!isFormContentType(request)) {
			return null;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
		List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
		for (InterfaceHttpData data : postData) {
			if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
				MemoryAttribute attribute = (MemoryAttribute) data;
				params.put(attribute.getName(), attribute.getValue());
			}
		}
		return params;
	}

	/**
	 * 获取Body为String字符串
	 * 
	 * @param request
	 * @return
	 */
	protected String getBodyString(FullHttpRequest request) {
		// if (request.method() != HttpMethod.POST) {
		// return null;
		// }
		if (isNoneContentType(request) || isTextContentType(request) || isPlainContentType(request) || isJsonContentType(request) || isXmlContentType(request)) {
			return request.content().toString(getContentEncoding(request));
		}
		return null;
	}

	/**
	 * 执行应用逻辑调用
	 * 
	 * @param request
	 * @return
	 */
	public abstract Object doCall(FullHttpRequest request);

	@Override
	public Object call(Object request, Channel channel) {
		if (!(request instanceof FullHttpRequest)) {
			throw new RuntimeException("Illegal request type, FullHttpRequest expected.");
		}

		return doCall((FullHttpRequest) request);
	}

	@Override
	public void onChannelClosed(Channel channel) {
	}

	@Override
	public void onChannelException(Channel channel, Throwable cause) {
	}

}
