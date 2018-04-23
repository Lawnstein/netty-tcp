/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.netty.message.handler.coding.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.netty.message.HeadLengthType;
import io.netty.tcp.netty.message.handler.coding.AbstractFixedLengthHeaderByteMsgEncoder;
import io.netty.tcp.util.CommUtil;

/**
 * 以网络long长度头发送byte[].
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 * @param <I>
 */
public class LongHeadByteMsgEncoder extends AbstractFixedLengthHeaderByteMsgEncoder {
	protected final static Logger logger = LoggerFactory.getLogger(LongHeadByteMsgEncoder.class);

	public LongHeadByteMsgEncoder() {
		super(HeadLengthType.LONG);
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
	public LongHeadByteMsgEncoder clone() {
		LongHeadByteMsgEncoder e = new LongHeadByteMsgEncoder();
		e.setHeaderLengthIncluded(this.isHeaderLengthIncluded());
		return e;
	}

	@Override
	public String toString() {
		return "LongHeadByteMsgEncoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize
				+ ", headerLengthIncluded=" + headerLengthIncluded + "]";
	}
	
	
}
