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
import io.netty.tcp.netty.message.handler.coding.AbstractFixedLengthHeaderByteMsgDecoder;
import io.netty.tcp.serialiaztion.KryoObjectSerializer;

/**
 * Kyro接收解码.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class KyroObjectMsgDecoder extends AbstractFixedLengthHeaderByteMsgDecoder {
	protected final static Logger logger = LoggerFactory.getLogger(KyroObjectMsgDecoder.class);

	public KyroObjectMsgDecoder() {
		super(HeadLengthType.INT);
	}

	@Override
	public Object decoding(byte[] header, byte[] source) {
		return KryoObjectSerializer.deserializing(source);
	}

	@Override
	public KyroObjectMsgDecoder clone() {
		KyroObjectMsgDecoder d = new KyroObjectMsgDecoder();
		d.setHeaderLengthIncluded(this.isHeaderLengthIncluded());
		d.setLengthHeaderIncluded(this.isHeaderLengthIncluded());
		return d;
	}

	@Override
	public String toString() {
		return "KyroObjectMsgDecoder [headLengthType=" + headLengthType + ", headerLengthSize=" + headerLengthSize
				+ ", headerLengthIncluded=" + headerLengthIncluded + ", lengthHeaderIncluded=" + lengthHeaderIncluded
				+ "]";
	}

}
