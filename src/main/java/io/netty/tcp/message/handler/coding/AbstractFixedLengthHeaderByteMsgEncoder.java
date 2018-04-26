/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.message.handler.coding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.tcp.message.HeadLengthType;
import io.netty.tcp.server.TcpServer;
import io.netty.tcp.util.CommUtil;

/**
 * 固定报文头长度的编码发送.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 * @param <I>
 */
public abstract class AbstractFixedLengthHeaderByteMsgEncoder extends MessageToByteEncoder {
	protected final static Logger logger = LoggerFactory.getLogger(AbstractFixedLengthHeaderByteMsgEncoder.class);

	/**
	 * 报文长度头类型.
	 */
	protected HeadLengthType headLengthType = HeadLengthType.INT;

	/**
	 * 报文长度头大小.
	 */
	protected int headerLengthSize = -1;

	/**
	 * 报文头大小也包含在该长度头的值内.
	 */
	protected boolean headerLengthIncluded = false;

	public AbstractFixedLengthHeaderByteMsgEncoder() {
		resetHeaderLengthSize(-1);
	}

	public AbstractFixedLengthHeaderByteMsgEncoder(HeadLengthType headLengthType) {
		this.headLengthType = headLengthType;
		resetHeaderLengthSize(-1);
	}

	private void resetHeaderLengthSize(int headerLengthSize) {
		switch (this.headLengthType) {
		case SHORT:
			this.headerLengthSize = CommUtil.SHORT_BYTES_LENGTH;
			break;

		case INT:
			this.headerLengthSize = CommUtil.INT_BYTES_LENGTH;
			break;

		case LONG:
			this.headerLengthSize = CommUtil.LONG_BYTES_LENGTH;
			break;

		default:
			this.headerLengthSize = headerLengthSize;
			break;
		}
	}

	public HeadLengthType getHeadLengthType() {
		return headLengthType;
	}

	public void setHeadLengthType(HeadLengthType headLengthType) {
		this.headLengthType = headLengthType;
		resetHeaderLengthSize(this.headerLengthSize);
	}

	public int getHeaderLengthSize() {
		return headerLengthSize;
	}

	public void setHeaderLengthSize(int headerLengthSize) {
		resetHeaderLengthSize(headerLengthSize);
	}

	public boolean isHeaderLengthIncluded() {
		return headerLengthIncluded;
	}

	public void setHeaderLengthIncluded(boolean headerLengthIncluded) {
		this.headerLengthIncluded = headerLengthIncluded;
	}

	abstract public byte[] encoding(Object source);

	abstract public AbstractFixedLengthHeaderByteMsgEncoder clone();

//	public static byte[] getHeadBytes(HeadLengthType headLengthType, int headerLengthSize, boolean headerLengthIncluded,
//			byte[] body) {
//		return headLengthType.toBytes(headerLengthSize, headerLengthIncluded, body == null ? 0 : body.length);
//	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		if (msg == null)
			return;

		byte[] body = null;
		if (msg instanceof byte[]) {
			body = (byte[]) msg;
		} else {
			body = encoding(msg);
		}

		byte[] head = headLengthType.toBytes(headerLengthSize, headerLengthIncluded, body == null ? 0 : body.length);
		if (head != null && head.length > 0) {
			out.writeBytes(head);

			if (TcpServer.isDebug() && logger.isTraceEnabled())
				logger.trace("{} encode send head with {} length over.", this, head.length);
		}

		if (body != null && body.length > 0)
			out.writeBytes(body);

		if (TcpServer.isDebug() && logger.isTraceEnabled())
			logger.trace("{} encode send body with {} length over.", this, body.length);

	}

	@Override
	public String toString() {
		return "AbstractFixedLengthHeaderByteMsgEncoder [headLengthType=" + headLengthType + ", headerLengthSize="
				+ headerLengthSize + ", headerLengthIncluded=" + headerLengthIncluded + "]";
	}

}
