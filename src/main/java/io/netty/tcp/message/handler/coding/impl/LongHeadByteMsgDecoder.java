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
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgDecoder;

/**
 * 以网络long长度头接收byte[].
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class LongHeadByteMsgDecoder extends AbstractFixedLengthHeaderByteMsgDecoder {
	protected final static Logger logger = LoggerFactory.getLogger(LongHeadByteMsgDecoder.class);

	public LongHeadByteMsgDecoder() {
		super(HeadLengthType.LONG);
	}

	@Override
	public Object decoding(byte[] header, byte[] body) {
		if (this.lengthHeaderIncluded) {
			byte[] b = new byte[(header != null ? header.length : 0) + (body != null ? body.length : 0)];
			if (header != null && header.length > 0)
				System.arraycopy(header, 0, header.length, 0, header.length);
			if (body != null && body.length > 0)
				System.arraycopy(body, 0, body.length, header.length, body.length);
			return b;
		} else
			return body;
	}

	@Override
	public LongHeadByteMsgDecoder clone() {
		LongHeadByteMsgDecoder d = new LongHeadByteMsgDecoder();
		d.setHeaderLengthIncluded(this.isHeaderLengthIncluded());
		d.setLengthHeaderIncluded(this.isHeaderLengthIncluded());
		return d;
	}

	@Override
	public String toString() {
		return "LongHeadByteMsgDecoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize
				+ ", headerLengthIncluded=" + headerLengthIncluded + ", lengthHeaderIncluded=" + lengthHeaderIncluded
				+ "]";
	}
	
	
}
