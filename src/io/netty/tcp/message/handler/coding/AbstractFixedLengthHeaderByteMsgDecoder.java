/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.message.handler.coding;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.tcp.message.HeadLengthType;
import io.netty.tcp.server.TcpServer;
import io.netty.tcp.util.CommUtil;

/**
 * 固定报文头长度的接收解码, 接收byte[]. <br>
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public abstract class AbstractFixedLengthHeaderByteMsgDecoder extends ByteToMessageDecoder {
	protected final static Logger logger = LoggerFactory.getLogger(AbstractFixedLengthHeaderByteMsgDecoder.class);

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

	/**
	 * 长度头是否包含在输出结果.
	 */
	protected boolean lengthHeaderIncluded = false;

	public AbstractFixedLengthHeaderByteMsgDecoder() {
		resetHeaderLengthSize(-1);
	}

	public AbstractFixedLengthHeaderByteMsgDecoder(HeadLengthType headLengthType) {
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

	public boolean isLengthHeaderIncluded() {
		return lengthHeaderIncluded;
	}

	public void setLengthHeaderIncluded(boolean lengthHeaderIncluded) {
		this.lengthHeaderIncluded = lengthHeaderIncluded;
	}

	abstract public Object decoding(byte[] header, byte[] body);

	abstract public AbstractFixedLengthHeaderByteMsgDecoder clone();

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		int readableLen = in.readableBytes();
		if (readableLen < this.headerLengthSize) {
			logger.trace("decode recv not enough head, expect {}, readableBytes length {}, wait.",
					this.headerLengthSize, readableLen);
			return;
		}
		in.markReaderIndex();

		byte[] head = new byte[this.headerLengthSize];
		in.readBytes(head);

		int dataLength = this.headLengthType.toLength(head);
		if (TcpServer.isDebug() && logger.isTraceEnabled())
			logger.trace("{} decode recv expected body length {}.", this, dataLength);
		
		if (this.isHeaderLengthIncluded())
			dataLength -= this.headerLengthSize;
		if (dataLength <= 0) {
			ctx.close();
			return;
		}

		readableLen = in.readableBytes();
		if (readableLen < dataLength) {
			in.resetReaderIndex();

			if (TcpServer.isDebug() && logger.isTraceEnabled())
				logger.trace("{} decode recv not enough body, expected {}, readableBytes length {}, wait.",this, dataLength,
					readableLen);
			return;
		}

		byte[] body = new byte[dataLength];
		in.readBytes(body);

		Object o = decoding(head, body);
		if (o == null)
			out.add(body);
		else
			out.add(o);
		
		if (TcpServer.isDebug() && logger.isTraceEnabled())
			logger.trace("{} decode recv data over.", this);
		
	}

	@Override
	public String toString() {
		return "AbstractFixedLengthHeaderByteMsgDecoder [headLengthType=" + headLengthType + ", headerLengthSize="
				+ headerLengthSize + ", headerLengthIncluded=" + headerLengthIncluded + ", lengthHeaderIncluded="
				+ lengthHeaderIncluded + "]";
	}

}
