/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月14日 上午11:35:09 <br> vc: $Id: $
 */

package io.netty.tcp;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.util.SocketUtil;

/**
 * TODO 请填写注释.
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class TcpClient1Test {
	protected final static Logger logger = LoggerFactory.getLogger(TcpClient1Test.class);

	public static void main(String[] args) {
		try {
			Socket s = SocketUtil.connect("127.0.0.1", TcpServer1Test.PORT);

			for (int i = 0; i < 10; i++) {
				String body = "Request" + System.currentTimeMillis();
				int hl = body.getBytes().length;
				String hls = "" + hl;
				String packets = "00000000".substring(0, 8 - hls.length()) + hls + body;
				byte[] packetBytes = packets.getBytes();
				logger.debug("try to send {} ", packets);
				SocketUtil.write(s, packetBytes, 0, packetBytes.length);
				logger.debug("sended, try to recv ... ");
				byte[] recvBytes = SocketUtil.readBytes(s, 10);
				logger.debug("recv {}", new String(recvBytes));
			}
			SocketUtil.close(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
