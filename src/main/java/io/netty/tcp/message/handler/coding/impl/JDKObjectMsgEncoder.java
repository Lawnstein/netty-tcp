/**
 * netty-tcp. <br>
 * Copyright (C) 1999-2017, All rights reserved. <br>
 * <br>
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0. <br>
 */

package io.netty.tcp.message.handler.coding.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.message.HeadLengthType;
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgEncoder;
import io.netty.tcp.serialiaztion.JDKObjectSerializer;

/**
 * JDK编码发送.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 * @param <I>
 */
public class JDKObjectMsgEncoder extends AbstractFixedLengthHeaderByteMsgEncoder {
	protected final static Logger logger = LoggerFactory.getLogger(JDKObjectMsgEncoder.class);

	public JDKObjectMsgEncoder() {
		super(HeadLengthType.INT);
	}

	@Override
	public byte[] encoding(Object source) {
		return JDKObjectSerializer.serializing(source);
	}

	@Override
	public JDKObjectMsgEncoder clone() {
		JDKObjectMsgEncoder e = new JDKObjectMsgEncoder();
		e.setHeaderLengthIncluded(this.isHeaderLengthIncluded());
		return e;
	}

	@Override
	public String toString() {
		return "JDKObjectMsgDecoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize + ", headerLengthIncluded=" + headerLengthIncluded + "]";
	}

}
