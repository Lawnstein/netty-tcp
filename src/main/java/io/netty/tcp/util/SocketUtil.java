/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Socket通讯.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class SocketUtil {
	protected final static Logger logger = LoggerFactory.getLogger(SocketUtil.class);
	public static final int GRANULARITY = 5;

	public SocketUtil() {
	}

	/**
	 * 创建socket连接
	 * 
	 * @param address
	 *            地址
	 * @param port
	 *            端口
	 * @return
	 * @throws Exception
	 */
	public static Socket connect(String address, int port) throws Exception {
		return connect(address, port, false, 0, 30000, 30000);
	}

	/**
	 * 创建socket连接
	 * 
	 * @param address
	 *            地址
	 * @param port
	 *            端口
	 * @param connectTimeout
	 *            创建连接等待超时时间毫秒数.
	 * @param readTimeout
	 *            读等待超时时间毫秒数.
	 * @return
	 * @throws Exception
	 */
	public static Socket connect(String address, int port, int connectTimeout, int readTimeout) throws Exception {
		return connect(address, port, false, 0, connectTimeout, readTimeout);
	}

	/**
	 * 创建socket连接
	 * 
	 * @param address
	 *            地址
	 * @param port
	 *            端口
	 * @param soLingerOn
	 *            套接字关闭时是否等待发送完再关闭连接并返回：是否等待发送完毕.
	 * @param soLingerNum
	 *            等待发送完的等待秒数.
	 * @param readTimeout
	 *            读等待超时时间毫秒数.
	 * @return
	 * @throws Exception
	 */
	public static Socket connect(String address, int port, boolean soLingerOn, int soLingerNum, int readTimeout)
			throws Exception {
		Socket socket = new Socket(address, port);
		socket.setSoLinger(soLingerOn, soLingerNum);
		socket.setSoTimeout(readTimeout);
		return socket;
	}

	/**
	 * 创建socket连接
	 * 
	 * @param address
	 *            地址
	 * @param port
	 *            端口
	 * @param soLingerOn
	 *            套接字关闭时是否等待发送完再关闭连接并返回：是否等待发送完毕.
	 * @param soLingerNum
	 *            等待发送完的等待秒数.
	 * @param connectTimeout
	 *            创建连接等待超时时间毫秒数.
	 * @param readTimeout
	 *            读等待超时时间毫秒数.
	 * @return
	 * @throws Exception
	 */
	public static Socket connect(String address, int port, boolean soLingerOn, int soLingerNum, int connectTimeout,
			int readTimeout) throws Exception {
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(address, port), connectTimeout);
			socket.setSoLinger(soLingerOn, soLingerNum);
			socket.setSoTimeout(readTimeout);
		} catch (Exception e) {
			if (e instanceof java.net.SocketTimeoutException) {
				if (e.getLocalizedMessage().contains("connect timed out")) {
					throw new java.net.ConnectException("connect timed out");
				}
			}
			throw e;
		}
		return socket;
	}

	/**
	 * 关闭连接
	 * 
	 * @param socket
	 */
	public static void close(Socket socket) {
		if (socket != null && !socket.isClosed()) {
			try {
				socket.shutdownOutput();
				socket.shutdownInput();
				socket.close();
			} catch (IOException e) {
				logger.error("close socket {} IOException ", socket, e);
			}
		}
	}

	/**
	 * 一次性读取一组数据。
	 * 
	 * @param connect
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static byte[] readBytes(Socket connect, int timeout) throws Exception {
		byte[] bytes = null;
		try {
			int available = 0;
			int leftime = (timeout < 0 ? 10 : timeout) * 1000;
			InputStream ins = connect.getInputStream();
			while (leftime > 0) {
				if ((available = ins.available()) > 0) {
					bytes = new byte[available];
					ins.read(bytes, 0, available);
					return bytes;
				}

				Thread.sleep(GRANULARITY);
				leftime -= GRANULARITY;
			}
			if (leftime <= 0) {
				throw new RuntimeException("read timeout " + timeout + " second(s).");
			}
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		}
		return bytes;
	}

	/**
	 * 不指定字节数，读到多少算多少。（该情况下会有tcp粘包现象）
	 * 
	 * @param connect
	 * @param bytes
	 * @param pos
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static int read(Socket connect, byte[] bytes, int pos, int timeout) throws Exception {
		try {
			int available = 0;
			int rsize = 0;
			int leftime = (timeout < 0 ? 10 : timeout) * 1000;
			InputStream ins = connect.getInputStream();
			while (leftime > 0) {
				if ((available = ins.available()) > 0) {
					if (available > bytes.length - pos)
						rsize = bytes.length - pos;
					else
						rsize = available;

					int r = ins.read(bytes, pos, rsize);
					return r;
				}

				Thread.sleep(GRANULARITY);
				leftime -= GRANULARITY;
			}
			if (leftime <= 0) {
				throw new RuntimeException("read timeout " + timeout + " second(s).");
			}
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		}
		return -1;
	}

	/**
	 * 指定读取字节数、超时时间
	 * 
	 * @param connect
	 * @param bytes
	 * @param pos
	 * @param size
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static int read(Socket connect, byte[] bytes, int pos, int size, int timeout) throws Exception {
		if (size == 0)
			return 0;
		if (bytes.length < size) {
			throw new RuntimeException("not enough byte array size " + bytes.length + " for expected size " + size);
		}
		try {
			int cpos = 0;
			int available = 0;
			int rsize = 0;
			int leftsize = size;
			int leftime = (timeout <= 0 ? 10 : timeout) * 1000;
			InputStream ins = connect.getInputStream();
			while (leftime > 0) {
				if ((available = ins.available()) > 0) {
					rsize = leftsize;
					if (leftsize > available)
						rsize = available;
					int r = ins.read(bytes, pos + cpos, rsize);
					if (r < 0) {
						throw new RuntimeException("read bytes exception: available " + available + ", readed " + r);
					}
					cpos += r;
					leftsize -= r;
					logger.trace("read {} byte(s), total read {} byte(s), left {} byte(s).", r, cpos, leftsize);
					if (leftsize <= 0)
						return cpos;
				}

				Thread.sleep(GRANULARITY);
				leftime -= GRANULARITY;
			}
			if (leftime <= 0) {
				throw new RuntimeException("read " + size + " bytes timeout " + timeout + " seconds.");
			}
			return cpos;
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		}
	}

	public static boolean write(Socket connect, byte[] bytes, int pos, int size) throws Exception {
		try {
			OutputStream ous = connect.getOutputStream();
			ous.write(bytes, pos, size);
			ous.flush();
			logger.trace("write {} byte(s).", size);
			return true;
		} catch (IOException e) {
			throw e;
		}
	}

}
