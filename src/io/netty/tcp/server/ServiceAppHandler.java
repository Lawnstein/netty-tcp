/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.server;

import io.netty.channel.Channel;

/**
 * 服务端业务处理.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public interface ServiceAppHandler {
	/**
	 * 如果需要在Handler中自己发送Response时，就直接返回null;
	 * @param request 接收到客户端的请求对象
	 * @param channel 通讯句柄
	 * @return
	 */
	public Object call(Object request, Channel channel);

	/**
	 * 连接通道关闭时应用处理.
	 * 
	 * @param channel
	 *            通讯句柄
	 */
	public void onChannelClosed(Channel channel);
	
	/**
	 * 连接通道发生异常时应用的处理.<br>
	 * 平台层会直接中断连接。<br>
	 * 
	 * @param channel
	 *            通讯句柄
	 * @param cause
	 *            异常
	 */
	public void onChannelException(Channel channel, Throwable cause);
}
