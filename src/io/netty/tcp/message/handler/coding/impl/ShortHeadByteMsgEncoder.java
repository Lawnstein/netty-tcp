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
 * 以网络int长度头发送byte[].
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 * @param <I>
 */
public class ShortHeadByteMsgEncoder extends AbstractFixedLengthHeaderByteMsgEncoder {
	protected final static Logger logger = LoggerFactory.getLogger(ShortHeadByteMsgEncoder.class);

	public ShortHeadByteMsgEncoder() {
		super(HeadLengthType.SHORT);
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
	public ShortHeadByteMsgEncoder clone() {
		ShortHeadByteMsgEncoder e = new ShortHeadByteMsgEncoder();
		e.setHeaderLengthIncluded(this.isHeaderLengthIncluded());
		return e;
	}

	@Override
	public String toString() {
		return "ShortHeadByteMsgEncoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize
				+ ", headerLengthIncluded=" + headerLengthIncluded + "]";
	}
	
	
}
