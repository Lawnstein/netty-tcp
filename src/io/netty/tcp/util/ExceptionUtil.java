/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * exception utils.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class ExceptionUtil {

	/**
	 * @param e
	 * @return
	 */
	public static String getStackTrace(Throwable e) {
		try {
			ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
			e.printStackTrace(new java.io.PrintWriter(buf, true));
			String stack = buf.toString();
			buf.close();
			return stack;
		} catch (IOException e1) {
		}
		return null;
	}

}
