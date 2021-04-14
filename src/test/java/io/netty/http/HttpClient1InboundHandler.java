/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月14日 下午11:51:35 <br> vc: $Id: $
 */

package io.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;

public class HttpClient1InboundHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaders.Names.CONTENT_TYPE));
		}
		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			ByteBuf buf = content.content();
			System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
			buf.release();
		}
	}
}