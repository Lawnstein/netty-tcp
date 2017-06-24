/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.server;

/**
 * business call.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public interface ServiceHandler {
	public Object call(Object request);
}
