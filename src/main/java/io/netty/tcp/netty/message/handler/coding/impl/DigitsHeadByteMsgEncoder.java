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
 * 以指定长度的长度数据串头编码发送.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 * @param <I>
 */
public class DigitsHeadByteMsgEncoder extends AbstractFixedLengthHeaderByteMsgEncoder {
	protected final static Logger logger = LoggerFactory.getLogger(DigitsHeadByteMsgEncoder.class);

	public DigitsHeadByteMsgEncoder() {
		super(HeadLengthType.DIGITS);
	}

	public DigitsHeadByteMsgEncoder(int headerLengthSize) {
		super(HeadLengthType.DIGITS);
		this.setHeaderLengthSize(headerLengthSize);
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
	public DigitsHeadByteMsgEncoder clone() {
		DigitsHeadByteMsgEncoder e = new DigitsHeadByteMsgEncoder();
		e.setHeaderLengthSize(this.getHeaderLengthSize());
		e.setHeaderLengthIncluded(this.isHeaderLengthIncluded());
		return e;
	}

	@Override
	public String toString() {
		return "DigitsHeadByteMsgEncoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize
				+ ", headerLengthIncluded=" + headerLengthIncluded + "]";
	}

}
