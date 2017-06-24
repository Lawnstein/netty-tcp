/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.message;

import io.netty.tcp.util.CommUtil;

/**
 * 报文长度头类型.
 * @author Lawnstein.Chan 
 * @version $Revision:$
 */
public enum HeadLengthType {

	
	SHORT("short", CommUtil.SHORT_BYTES_LENGTH), 
	INT("int", CommUtil.INT_BYTES_LENGTH), 
	LONG("long", CommUtil.LONG_BYTES_LENGTH), 
	DIGITS("digits", 0);
	
	public String value;
	
	public int size;
		
	private HeadLengthType(String value, int size) {
		this.value = value;
		this.size = size;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String value() {
		return this.value();
	}
	
	public int size() {
		return this.size;
	}

	public static HeadLengthType valueof(String name) {
		if (name == null || name.length() == 0)
			return null;
		
		name = name.toUpperCase();
		HeadLengthType hlt = null;
		try {
			hlt = HeadLengthType.valueOf(name);
		} catch (java.lang.IllegalArgumentException e) {
		}
		if (hlt == null) {
			if (name.equalsIgnoreCase("short")) {
				return SHORT;
			}
			if (name.equalsIgnoreCase("int")) {
				return INT;
			}
			if (name.equalsIgnoreCase("long")) {
				return LONG;
			}
			if (name.equalsIgnoreCase("digits")) {
				return DIGITS;
			}
		}
		return hlt;
	}
	
	public int toLength(byte[] head) {
		int dataLength = 0;
		switch (this) {
		case SHORT:
			dataLength = CommUtil.bytesToShort(head);
			break;

		case INT:
			dataLength = CommUtil.bytesToInt(head);
			break;

		case LONG:
			dataLength = (int) CommUtil.bytesToLong(head);
			break;

		case DIGITS:
			dataLength = CommUtil.toInt(new String(head));
			break;

		default:
			break;
		}
		return dataLength;
	}
	
	public byte[] toBytes(int headerLengthSize, boolean headerLengthIncluded, int bodyLength) {
		byte[] headBuffer = null;
		int sendLength = bodyLength;
		if (headerLengthSize > 0) {
			if (headerLengthIncluded)
				sendLength += headerLengthSize;

			switch (this) {
			case SHORT:
				headBuffer = CommUtil.shortToBytes((short) sendLength);
				break;

			case INT:
				headBuffer = CommUtil.intToBytes(sendLength);
				break;

			case LONG:
				headBuffer = CommUtil.longToBytes(sendLength);
				break;

			case DIGITS: {
				String s = "0000000000" + sendLength;
				if (s.length() >= headerLengthSize) {
					s = s.substring(s.length() - headerLengthSize, s.length());
				} else {
					for (int i = 0, j = headerLengthSize - s.length(); i < j; i++) {
						s = "0" + s;
					}
				}
				headBuffer = s.getBytes();
			}
				break;

			default:
				break;
			}
		}
		return headBuffer;
	}
}
