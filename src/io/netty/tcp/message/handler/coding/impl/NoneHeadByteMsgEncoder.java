/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.message.handler.coding.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.message.HeadLengthType;
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgEncoder;
import io.netty.tcp.util.CommUtil;

/**
 * 无需处理长度头，直接编码发送.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 * @param <I>
 */
public class NoneHeadByteMsgEncoder extends AbstractFixedLengthHeaderByteMsgEncoder {
	protected final static Logger logger = LoggerFactory.getLogger(NoneHeadByteMsgEncoder.class);

	public NoneHeadByteMsgEncoder() {
		super(HeadLengthType.DIGITS);
		this.headerLengthSize = 0;
	}

	@Override
	public byte[] encoding(Object source) {
		if (source == null)
			return null;
		if (source instanceof byte[])
			return (byte[]) source;
		return (byte[]) CommUtil.toByteArray(source);
	}
	

	@Override
	public NoneHeadByteMsgEncoder clone() {
		NoneHeadByteMsgEncoder e = new NoneHeadByteMsgEncoder();
		return e;
	}

	@Override
	public String toString() {
		return "NoneHeadByteMsgEncoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize
				+ ", headerLengthIncluded=" + headerLengthIncluded + "]";
	}
	
	
}
