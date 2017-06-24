/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package testor.io.netty.digits;

import java.net.Socket;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.netty.tcp.util.ClientSocket;

/**
 * echo client.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class DigitsClient {

	protected final static Logger logger = LoggerFactory.getLogger(DigitsClient.class);

	/**
	 * 
	 */
	public DigitsClient() {
		// TODO 自动生成的构造函数存根
	}

	public static void simuHT() throws Exception {

		byte[] body = "HEARTBEAT".getBytes();
		String l = body.length + "";
		for (int i = 0, j = 8 - l.length(); i < j; i++)
			l = "0" + l;
		byte[] head = l.getBytes();
		
		long a1 = System.currentTimeMillis();		
		Socket client = ClientSocket.connect("127.0.0.1", 8000);		
		ClientSocket.write(client, head, 0, head.length);
		ClientSocket.write(client, body, 0, body.length);
		byte[] resp = new byte[1024];
		ClientSocket.read(client, resp, 0, 10);		
		ClientSocket.close(client);
		long a2 = System.currentTimeMillis();
		
		logger.info((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + new String(resp));
	}
	
	public static void simuSync() throws Exception {

		byte[] body = (new Date()).toLocaleString().getBytes();
		String l = body.length + "";
		for (int i = 0, j = 8 - l.length(); i < j; i++)
			l = "0" + l;
		byte[] head = l.getBytes();
		
		long a1 = System.currentTimeMillis();		
		Socket client = ClientSocket.connect("127.0.0.1", 8000);		
		ClientSocket.write(client, head, 0, head.length);
		ClientSocket.write(client, body, 0, body.length);
		byte[] resp = new byte[1024];
		ClientSocket.read(client, resp, 0, 10);		
		ClientSocket.close(client);
		long a2 = System.currentTimeMillis();
		
		logger.info((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + new String(resp));
	}

	public static void simuMulti() throws Exception {
		for (int i = 0; i < 10; i++) {
			try {
				simuSync();
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void s() {
		String s = Thread.currentThread().getName() + " :" + UUID.randomUUID().toString();
//		logger.info(s);
		logger.debug(s);
	}

	public static void main(String[] args) throws Exception {
		logger.debug("=====================================");
		simuHT();
//		simuSync();
		System.exit(0);
		logger.debug("=====================================");
		
		for (int i = 0; i < 4; i++) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("Child-" + Thread.currentThread().getId());
					MDC.put("TRCODE", "Client");
					
					try {
						simuMulti();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			t.start();
		}
		Thread.sleep(100000);
		logger.debug("=====================================");
		
		// for (int i = 0; i < 2; i++) {
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// simuSync();
		//// s();
		// }
		// }).start();
		// }

		// simu();
		// simuMulti();

		System.exit(0);
	}
}
