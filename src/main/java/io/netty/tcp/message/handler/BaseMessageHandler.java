/**
 * netty-tcp. <br>
 * Copyright (C) 1999-2017, All rights reserved. <br>
 * <br>
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0. <br>
 */

package io.netty.tcp.message.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgDecoder;

/**
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class BaseMessageHandler {
	protected final static Logger logger = LoggerFactory.getLogger(AbstractFixedLengthHeaderByteMsgDecoder.class);

	public BaseMessageHandler() {
	}

}
