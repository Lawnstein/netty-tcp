/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.netty.client;

import io.netty.channel.Channel;

/**
 * 客户端长连接下多次交互场景时的应用交互处理.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public interface ClientAppHandler {

	/**
	 * 连接激活，一般供实现者存储该Channel，然后自行发送request。
	 * 
	 * @param channel
	 */
	public void activateChannel(Channel channel);

	/**
	 * 连接激活，一般供实现者存储该Channel，然后自行发送request。
	 * 
	 * @param channel
	 */

	/**
	 * 接收到服务端应答。
	 * 
	 * @param response
	 */
	public void recvResponse(Object response);

	/**
	 * 交互是否结束。<br>
	 * 在客户端交互过程中，如果需要平台来关闭连接，则需要该方法返回true。<br>
	 * 也可以在call方法中自行关闭channel.close();<br>
	 * 
	 * @return
	 */
	public boolean isComplete();

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
	
	/**
	 * 设置创建者(通讯时会从creator获取一些超时等通讯信息).
	 * @param creator
	 */
	public void setCreator(NTcpClient creator);
}
