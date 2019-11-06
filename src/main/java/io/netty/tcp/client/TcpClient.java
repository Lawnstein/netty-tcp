/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.client;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.message.HeadLengthType;
import io.netty.tcp.serialiaztion.KryoObjectSerializer;
import io.netty.tcp.serialiaztion.ObjectSerializable;
import io.netty.tcp.socket.ClientSocket;

/**
 * 客户端通讯调度处理.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class TcpClient extends AbstractClient {
	protected final static Logger logger = LoggerFactory.getLogger(TcpClient.class);

	private String rhost = null;

	private Socket sock = null;

	private HeadLengthType headLengthType = HeadLengthType.INT;

	private int headLengthSize = HeadLengthType.INT.size();

	private boolean headLengthIncluded = false;

	private ObjectSerializable objectSerializable = null;

	public TcpClient() {
	}

	public TcpClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public HeadLengthType getHeadLengthType() {
		return headLengthType;
	}

	public void setHeadLengthType(HeadLengthType headLengthType) {
		this.headLengthType = headLengthType;
		if (!headLengthType.equals(HeadLengthType.DIGITS))
			this.headLengthSize = headLengthType.size();
	}

	public int getHeadLengthSize() {
		return headLengthSize;
	}

	public void setHeadLengthSize(int headLengthSize) {
		this.headLengthSize = headLengthSize;
	}

	public boolean isHeadLengthIncluded() {
		return headLengthIncluded;
	}

	public void setHeadLengthIncluded(boolean headLengthIncluded) {
		this.headLengthIncluded = headLengthIncluded;
	}

	public ObjectSerializable getObjectSerializable() {
		return objectSerializable;
	}

	public void setObjectSerializable(ObjectSerializable objectSerializable) {
		this.objectSerializable = objectSerializable;
	}

	/**
	 * 创建连接（内部触发调用）.
	 */
	private void connect() {
		rhost = host + ":" + port;
		if (sock != null) {
			try {
				sock.shutdownInput();
				sock.shutdownOutput();
				sock.close();
			} catch (IOException e) {
			}
			sock = null;
		}

		try {
			sock = ClientSocket.connect(host, port, connectTimeout, readTimeout);
			alived = true;
		} catch (Exception e) {
			sock = null;
			logger.error("connect to {} failed: {}", rhost, e);
			throw new RuntimeException("connect to " + rhost + " failed", e);
		}

	}

	/**
	 * 关闭连接.
	 */
	public void disconnect() {
		if (sock != null) {
			try {
				sock.shutdownInput();
				sock.shutdownOutput();
				sock.close();
			} catch (IOException e) {
			}
			sock = null;
		}
		alived = false;
	}

	/**
	 * 同步调用-一请求一应答。
	 * 
	 * @param request
	 *            请求对象（Map，List，Bean等）
	 * @return response 应答对象（Map，List，Bean等）
	 */
	public Object call(Object request) {
		Object response = null;
		connect();
		logger.trace("connect to {} over.", rhost);

		/**
		 * send request message.
		 */
		byte[] out = null;
		try {
			if (this.objectSerializable != null)
				out = objectSerializable.serialize(request);
			else
				out = KryoObjectSerializer.serializing(request);
			int osz = out == null ? 0 : out.length;
			ClientSocket.write(sock,
					this.headLengthType.toBytes(this.headLengthSize, isHeadLengthIncluded(), out.length), 0,
					this.headLengthSize);
			ClientSocket.write(sock, out, 0, osz);
			logger.trace("send request to {} over. Serialized request size {}", rhost, osz);
		} catch (Exception e) {
			logger.error("send request to {} failed:{}", rhost, e);
			disconnect();
			throw new RuntimeException("send request to " + rhost + " failed", e);
		}
		logger.trace("sended request " + request);

		/**
		 * receive response message.
		 */
		try {
			byte[] le = new byte[this.headLengthSize];
			int r = ClientSocket.read(sock, le, 0, headLengthSize,
					readTimeout == 0 ? DEFAULT_READTIMEOUT : readTimeout);
			int isz = this.headLengthType.toLength(le);
			logger.trace("recv response from {} expected size {}({})", rhost, isz, r);
			byte[] ds = new byte[isz];
			r = ClientSocket.read(sock, ds, 0, isz, readTimeout == 0 ? DEFAULT_READTIMEOUT : readTimeout);
			logger.trace("recv response from {} over. Serialized response size {}({})", rhost, isz, r);
			if (this.objectSerializable != null)
				response = objectSerializable.deserialize(ds);
			else
				response = KryoObjectSerializer.deserializing(ds);
		} catch (Exception e) {
			logger.error("recv response from {} failed: {}", rhost, e);
			disconnect();
			throw new RuntimeException("recv response from " + rhost + " failed", e);
		}
		logger.trace("received response {}", response);

		/**
		 * close the socket normal.
		 */
		disconnect();
		logger.trace("disconnect to {} over.", rhost);
		return response;
	}

	/**
	 * 异步调用-发送.
	 * 
	 * @param request
	 *            请求对象（Map，List，Bean等）
	 * @return void
	 */
	public void send(final Object request) {
		if (sock == null)
			connect();

		/**
		 * send request message.
		 */
		byte[] out = null;
		try {
			if (this.objectSerializable != null)
				out = objectSerializable.serialize(request);
			else
				out = KryoObjectSerializer.serializing(request);
			int osz = out == null ? 0 : out.length;
			ClientSocket.write(sock,
					this.headLengthType.toBytes(this.headLengthSize, isHeadLengthIncluded(), out.length), 0,
					this.headLengthSize);
			ClientSocket.write(sock, out, 0, osz);
			logger.trace("send request to {} over. Serialized request size {}", rhost, osz);
		} catch (Throwable e) {
			logger.error("send request to {} failed:{}", rhost, e);
			disconnect();
			throw new RuntimeException("send request to " + rhost + " failed", e);
		}

		logger.trace("sended request {}", request);
	}

	/**
	 * 异步调用-接收.
	 * 
	 * @return response 应答对象（Map，List，Bean等）
	 */
	public Object recv() {
		if (sock == null)
			connect();

		/**
		 * receive response message.
		 */
		Object response = null;
		try {
			byte[] le = new byte[this.headLengthSize];
			int r = ClientSocket.read(sock, le, 0, headLengthSize,
					readTimeout == 0 ? DEFAULT_READTIMEOUT : readTimeout);
			int isz = this.headLengthType.toLength(le);
			logger.trace("recv response from {} expected size {}({})", rhost, isz, r);
			byte[] ds = new byte[isz];
			r = ClientSocket.read(sock, ds, 0, isz, readTimeout == 0 ? DEFAULT_READTIMEOUT : readTimeout);
			logger.trace("recv response from {} over. Serialized response size {}({})", rhost, isz, r);
			if (this.objectSerializable != null)
				response = objectSerializable.deserialize(ds);
			else
				response = KryoObjectSerializer.deserializing(ds);
		} catch (Throwable e) {
			logger.error("recv response from {} failed: {}", rhost, e);
			disconnect();
			throw new RuntimeException("recv response from " + rhost + " failed", e);
		}

		logger.trace("received response {}", response);
		return response;
	}

	public void close() {
		disconnect();
	}

	@Override
	public String toString() {
		return "TcpClient [rhost=" + rhost + ", sock=" + sock + ", headLengthType=" + headLengthType
				+ ", headLengthSize=" + headLengthSize + ", headLengthIncluded=" + headLengthIncluded
				+ ", objectSerializable=" + objectSerializable + ", host=" + host + ", port=" + port + ", readTimeout="
				+ readTimeout + ", writeTimeout=" + writeTimeout + ", connectTimeout=" + connectTimeout + ", timeout="
				+ timeout + "]";
	}

}