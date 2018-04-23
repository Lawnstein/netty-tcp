/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用工具.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class CommUtil {
	protected final static Logger logger = LoggerFactory.getLogger(CommUtil.class);

	public static final int SHORT_BYTES_LENGTH = 2;
	public static final int INT_BYTES_LENGTH = 4;
	public static final int LONG_BYTES_LENGTH = 8;

	// 以下 是整型数 和 网络字节序的 byte[] 数组之间的转换
	public static byte[] longToBytes(long n) {
		byte[] b = new byte[8];
		b[7] = (byte) (n & 0xff);
		b[6] = (byte) (n >> 8 & 0xff);
		b[5] = (byte) (n >> 16 & 0xff);
		b[4] = (byte) (n >> 24 & 0xff);
		b[3] = (byte) (n >> 32 & 0xff);
		b[2] = (byte) (n >> 40 & 0xff);
		b[1] = (byte) (n >> 48 & 0xff);
		b[0] = (byte) (n >> 56 & 0xff);
		return b;
	}

	public static void longToBytes(long n, byte[] array, int offset) {
		array[7 + offset] = (byte) (n & 0xff);
		array[6 + offset] = (byte) (n >> 8 & 0xff);
		array[5 + offset] = (byte) (n >> 16 & 0xff);
		array[4 + offset] = (byte) (n >> 24 & 0xff);
		array[3 + offset] = (byte) (n >> 32 & 0xff);
		array[2 + offset] = (byte) (n >> 40 & 0xff);
		array[1 + offset] = (byte) (n >> 48 & 0xff);
		array[0 + offset] = (byte) (n >> 56 & 0xff);
	}

	public static long bytesToLong(byte[] array) {
		return ((((long) array[0] & 0xff) << 56) | (((long) array[1] & 0xff) << 48) | (((long) array[2] & 0xff) << 40)
				| (((long) array[3] & 0xff) << 32) | (((long) array[4] & 0xff) << 24) | (((long) array[5] & 0xff) << 16)
				| (((long) array[6] & 0xff) << 8) | (((long) array[7] & 0xff) << 0));
	}

	public static long bytesToLong(byte[] array, int offset) {
		return ((((long) array[offset + 0] & 0xff) << 56) | (((long) array[offset + 1] & 0xff) << 48)
				| (((long) array[offset + 2] & 0xff) << 40) | (((long) array[offset + 3] & 0xff) << 32)
				| (((long) array[offset + 4] & 0xff) << 24) | (((long) array[offset + 5] & 0xff) << 16)
				| (((long) array[offset + 6] & 0xff) << 8) | (((long) array[offset + 7] & 0xff) << 0));
	}

	public static byte[] intToBytes(int n) {
		byte[] b = new byte[4];
		b[3] = (byte) (n & 0xff);
		b[2] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n >> 16 & 0xff);
		b[0] = (byte) (n >> 24 & 0xff);
		return b;
	}

	public static void intToBytes(int n, byte[] array, int offset) {
		array[3 + offset] = (byte) (n & 0xff);
		array[2 + offset] = (byte) (n >> 8 & 0xff);
		array[1 + offset] = (byte) (n >> 16 & 0xff);
		array[offset] = (byte) (n >> 24 & 0xff);
	}

	public static int bytesToInt(byte b[]) {
		return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
	}

	public static int bytesToInt(byte b[], int offset) {
		return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16
				| (b[offset] & 0xff) << 24;
	}

	public static byte[] uintToBytes(long n) {
		byte[] b = new byte[4];
		b[3] = (byte) (n & 0xff);
		b[2] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n >> 16 & 0xff);
		b[0] = (byte) (n >> 24 & 0xff);

		return b;
	}

	public static void uintToBytes(long n, byte[] array, int offset) {
		array[3 + offset] = (byte) (n);
		array[2 + offset] = (byte) (n >> 8 & 0xff);
		array[1 + offset] = (byte) (n >> 16 & 0xff);
		array[offset] = (byte) (n >> 24 & 0xff);
	}

	public static long bytesToUint(byte[] array) {
		return ((long) (array[3] & 0xff)) | ((long) (array[2] & 0xff)) << 8 | ((long) (array[1] & 0xff)) << 16
				| ((long) (array[0] & 0xff)) << 24;
	}

	public static long bytesToUint(byte[] array, int offset) {
		return ((long) (array[offset + 3] & 0xff)) | ((long) (array[offset + 2] & 0xff)) << 8
				| ((long) (array[offset + 1] & 0xff)) << 16 | ((long) (array[offset] & 0xff)) << 24;
	}

	public static byte[] shortToBytes(short n) {
		byte[] b = new byte[2];
		b[1] = (byte) (n & 0xff);
		b[0] = (byte) ((n >> 8) & 0xff);
		return b;
	}

	public static void shortToBytes(short n, byte[] array, int offset) {
		array[offset + 1] = (byte) (n & 0xff);
		array[offset] = (byte) ((n >> 8) & 0xff);
	}

	public static short bytesToShort(byte[] b) {
		return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
	}

	public static short bytesToShort(byte[] b, int offset) {
		return (short) (b[offset + 1] & 0xff | (b[offset] & 0xff) << 8);
	}

	public static byte[] ushortToBytes(int n) {
		byte[] b = new byte[2];
		b[1] = (byte) (n & 0xff);
		b[0] = (byte) ((n >> 8) & 0xff);
		return b;
	}

	public static void ushortToBytes(int n, byte[] array, int offset) {
		array[offset + 1] = (byte) (n & 0xff);
		array[offset] = (byte) ((n >> 8) & 0xff);
	}

	public static int bytesToUshort(byte b[]) {
		return b[1] & 0xff | (b[0] & 0xff) << 8;
	}

	public static int bytesToUshort(byte b[], int offset) {
		return b[offset + 1] & 0xff | (b[offset] & 0xff) << 8;
	}

	public static byte[] ubyteToBytes(int n) {
		byte[] b = new byte[1];
		b[0] = (byte) (n & 0xff);
		return b;
	}

	public static void ubyteToBytes(int n, byte[] array, int offset) {
		array[0] = (byte) (n & 0xff);
	}

	public static int bytesToUbyte(byte[] array) {
		return array[0] & 0xff;
	}

	public static int bytesToUbyte(byte[] array, int offset) {
		return array[offset] & 0xff;
	}

	/**
	 * 组成字符串方法
	 * 
	 * @param args
	 *            变参
	 * @return String
	 */
	public static BigDecimal toBigDecimal(Object value) {
		if (value == null)
			return null;
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		} else if (value instanceof Double) {
			String s = String.format("%f", (Double) value);
			return new BigDecimal(s);
		} else if (value instanceof Float) {
			String s = String.format("%f", (Float) value);
			return new BigDecimal(s);
		} else if (value instanceof Long) {
			String s = String.format("%d", (Long) value);
			return new BigDecimal(s);
		} else if (value instanceof Integer) {
			String s = String.format("%d", (Integer) value);
			return new BigDecimal(s);
		} else if (value instanceof String) {
			return new BigDecimal((String) value);
		} else {
			return new BigDecimal(value.toString());
		}
	}

	/**
	 * 转换成Integer
	 * 
	 * @param args
	 *            value
	 * @return Integer
	 */
	public static Integer toInteger(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return (Integer) value;
		} else {
			try {
				return new Integer(value.toString());
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * BigDecimal转换成Long, 如果BigDecimal包含有效小数位，则会报错。
	 * 
	 * @param args
	 *            value
	 * @return Long
	 */
	public static Long toLong(BigDecimal value) {
		if (value == null) {
			return null;
		}
		return value.longValueExact();
	}

	/**
	 * 转换成Long
	 * 
	 * @param args
	 *            value
	 * @return Long
	 */
	public static Long toLong(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Long) {
			return (Long) value;
		} else {
			try {
				return new Long(value.toString());
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * 转换成Boolean
	 * 
	 * @param args
	 *            value
	 * @return boolean
	 */
	public final static boolean toBoolean(String str) {
		return toBoolean(str, false);
	}

	/**
	 * 转换成Boolean,为空时赋值缺省值.
	 * 
	 * @param args
	 *            value
	 * @return boolean
	 */
	public final static boolean toBoolean(String str, boolean defaultValue) {
		if (str == null || str.length() == 0) {
			return defaultValue;
		} else
			return new Boolean(str.trim()).booleanValue();

	}

	/**
	 * 转换成int.
	 * 
	 * @param args
	 *            str
	 * @return int
	 */
	public static final int toInt(String str) {
		return toInt(str, 0);
	}

	/**
	 * 转换成int,为空则赋缺省值.
	 * 
	 * @param args
	 *            str
	 * @param args
	 *            defaultValue
	 * @return int
	 */
	public static final int toInt(String str, int defaultValue) {
		if (str == null || str.length() == 0) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(str.trim());
		} catch (Throwable t) {
			return defaultValue;
		}
	}

	/**
	 * Array的toString方法
	 * 
	 * @param array
	 * @return String
	 */
	public static String toString(Object[] array) {
		if (array == null)
			return null;
		String out = array.getClass().getSimpleName() + " [";
		for (int ii = 0; ii < array.length; ii++) {
			out += array[ii];
			if (ii + 1 < array.length)
				out += ",";
		}
		out += "]";
		return out;
	}

	/**
	 * Map的toString方法
	 * 
	 * @param map
	 * @return String
	 */
	public static String toString(Map<?, ?> map) {
		if (map == null)
			return null;
		String out = map.getClass().getSimpleName() + " {";
		Iterator<?> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object value = map.get(key);
			out += key + "=" + value;
			if (iter.hasNext())
				out += ",";
		}
		out += "}";
		return out;
	}

	/**
	 * List的toString方法
	 * 
	 * @param map
	 * @return String
	 */
	public static String toString(List list) {
		if (list == null)
			return null;
		String out = list.getClass().getSimpleName() + " [";
		for (int ii = 0; ii < list.size(); ii++) {
			out += list.get(ii);
			if (ii + 1 < list.size())
				out += ",";
		}
		out += "]";
		return out;
	}

	/**
	 * Set的toString方法
	 * 
	 * @param set
	 * @return String
	 */
	public static String toString(Set set) {
		if (set == null)
			return null;
		String out = set.getClass().getSimpleName() + " [";
		int ii = 0;
		for (Object o : set) {
			out += o;
			if (++ii + 1 < set.size())
				out += ",";
		}
		out += "]";
		return out;
	}

	public static List toList(Object[] array) {
		if (array == null)
			return null;
		return Arrays.asList(array);
	}

	public static <T> T[] toArray(List<T> list, Class<? extends T> newType) {
		if (list == null || list.size() == 0)
			return null;
		return (T[]) list.toArray((T[]) Array.newInstance(newType, list.size()));
	}

	public static byte[] toByteArray(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException e) {
			logger.error("toByteArray IOException {}", ExceptionUtil.getStackTrace(e));
		}
		return bytes;
	}

	/**
	 * 数组转对象
	 * 
	 * @param bytes
	 * @return
	 */
	public static Object toObject(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException e) {
			logger.error("Byte array toObject IOException {}", ExceptionUtil.getStackTrace(e));
		} catch (ClassNotFoundException e) {
			logger.error("Byte array toObject ClassNotFoundException {}", ExceptionUtil.getStackTrace(e));
		}
		return obj;
	}

	public static boolean isNumeric(String str) {
		String reg = "^[-\\+]?[\\d]*$";
		return str.matches(reg);
	}

	public static boolean isDecimal(String str) {
		String reg = "^[-\\+]?[.\\d]*$";
		return str.matches(reg);
	}

}
