/**
 * netty-tcp. <br>
 * Copyright (C) 1999-2017, All rights reserved. <br>
 * <br>
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0. <br>
 */

package io.netty.tcp.serialiaztion;

/**
 * 序列化机制.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public interface ObjectSerializable {

	/**
	 * 序列为byte[].
	 * 
	 * @param t
	 * @return
	 */
	public byte[] serialize(Object t);

	/**
	 * 反序列化为对象.
	 * 
	 * @param bytes
	 * @return
	 */
	public <T> T deserialize(byte[] bytes);

}
