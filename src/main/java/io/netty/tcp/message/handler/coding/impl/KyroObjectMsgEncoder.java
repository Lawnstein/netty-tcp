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
import io.netty.tcp.serialiaztion.KryoObjectSerializer;

/**
 * Kyro编码发送.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 * @param <I>
 */
public class KyroObjectMsgEncoder extends AbstractFixedLengthHeaderByteMsgEncoder {
	protected final static Logger logger = LoggerFactory.getLogger(KyroObjectMsgEncoder.class);

	public KyroObjectMsgEncoder() {
		super(HeadLengthType.INT);
	}

	@Override
	public byte[] encoding(Object source) {
		return KryoObjectSerializer.serializing(source);
	}

	@Override
	public KyroObjectMsgEncoder clone() {
		KyroObjectMsgEncoder e = new KyroObjectMsgEncoder();		
		e.setHeaderLengthIncluded(this.isHeaderLengthIncluded());
		return e;
	}

	@Override
	public String toString() {
		return "KyroObjectMsgEncoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize
				+ ", headerLengthIncluded=" + headerLengthIncluded + "]";
	}

}
