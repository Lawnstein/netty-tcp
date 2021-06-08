/**
 * netty-tcp. <br>
 * Copyright (C) 1999-2017, All rights reserved. <br>
 * <br>
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0. <br>
 */

package io.netty.tcp.message;

/**
 * 心跳内容定义.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class HeartBeatMessage {

	/**
	 * 心跳发起
	 */
	public static String KEY_HEARTBEAT = "HEARTBEAT";

	/**
	 * 心跳应答
	 */
	public static String KEY_ALIVE = "ALIVE";

	/**
	 * 通道檢測
	 */
	public static String KEY_PING = "PING";

	public HeartBeatMessage() {
	}

}
