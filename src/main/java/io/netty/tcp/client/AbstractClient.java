/**
 * netty-tcp. <br>
 * Copyright (C) 1999-2017, All rights reserved. <br>
 * <br>
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0. <br>
 */
package io.netty.tcp.client;

/**
 * 通讯连接客户端.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public abstract class AbstractClient {

	protected String host;

	protected int port;

	protected boolean soLingerOn = false;

	protected int soLingerNum = 0;


	public final static int DEFAULT_READTIMEOUT = 10;
	
	protected int readTimeout = DEFAULT_READTIMEOUT * 1000;

	public final static int DEFAULT_WRITETIMEOUT = 3;
	
	protected int writeTimeout = DEFAULT_WRITETIMEOUT * 1000;

	public final static int DEFAULT_CONNECTIMEOUT = 3;

	protected int connectTimeout = DEFAULT_CONNECTIMEOUT * 1000;

	protected int heartbeatInervalSec = 0;

	protected int timeout = -1;

	protected boolean alived = false;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSoLingerOn() {
		return soLingerOn;
	}

	public void setSoLingerOn(boolean soLingerOn) {
		this.soLingerOn = soLingerOn;
	}

	public int getSoLingerNum() {
		return soLingerNum;
	}

	public void setSoLingerNum(int soLingerNum) {
		this.soLingerNum = soLingerNum;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(int writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		if (timeout < 0)
			return;
		this.timeout = timeout;
		if (this.readTimeout <= 0)
			this.readTimeout = timeout;
		if (this.writeTimeout <= 0)
			this.writeTimeout = timeout;
		if (this.connectTimeout < 0)
			this.connectTimeout = timeout;
	}

	public int getHeartbeatInervalSec() {
		return heartbeatInervalSec;
	}

	public void setHeartbeatInervalSec(int heartbeatInervalSec) {
		this.heartbeatInervalSec = heartbeatInervalSec;
	}

	public boolean isAlived() {
		return alived;
	}

	public void setAlived(boolean alived) {
		this.alived = alived;
	}

	public AbstractClient() {
	}

	abstract public void close();

	abstract public Object call(Object request);

	abstract public void send(Object request);

	abstract public Object recv();
}
