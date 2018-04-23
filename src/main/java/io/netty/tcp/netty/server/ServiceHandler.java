/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.netty.server;

/**
 * 业务处理.
 * @author Lawnstein.Chan 
 * @version $Revision:$
 */
public interface ServiceHandler {
	public Object call(Object request);
}
