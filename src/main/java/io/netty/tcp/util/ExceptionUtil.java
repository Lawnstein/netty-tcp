/**
 * netty-tcp. <br>
 * Copyright (C) 1999-2017, All rights reserved. <br>
 * <br>
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0. <br>
 */

package io.netty.tcp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 异常类工具.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class ExceptionUtil {

	/**
	 * 获取异常堆栈信息。
	 * 
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
