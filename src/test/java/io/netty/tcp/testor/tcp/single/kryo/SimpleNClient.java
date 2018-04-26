/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.testor.tcp.single.kryo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.netty.tcp.client.NTcpClient;

/**
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class SimpleNClient {

	protected final static Logger logger = LoggerFactory.getLogger(SimpleNClient.class);


	public SimpleNClient() {
	}

	public static void simuSync() {

		Map req = new HashMap();
		String d = (new Date()).toLocaleString();
		req.put("date", d);
		req.put("time", System.currentTimeMillis());
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < 5000; i++) {
			ids.add(UUID.randomUUID().toString());
		}
		req.put("udis", ids);

		Car c = new Car();
		c.setName("Dazhong");
		c.setPrice(180000);
		c.setSpeed(1500);
		c.setBrand("good");
		req.put("car", c);

		NTcpClient client = new NTcpClient();
		client.setHost("127.0.0.1");
		client.setPort(8000);
		client.setHeartbeatInervalSec(3);		
		//client.setReadTimeout(30);

		Random r = new Random();
		for (int i = 0; i < 1000; i++) {
			req.put("time", System.currentTimeMillis());
			logger.info( Thread.currentThread().getName() + " Request :" + req);
			long a1 = System.currentTimeMillis();
			Object response = null;
			try {
				response= client.call(req);
			} catch (Throwable th) {
				logger.error("call failed.",th);
			}
			long a2 = System.currentTimeMillis();
			logger.info((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + response);
			logger.debug((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + response);
			if ((a2 - a1) > 1000) {
				System.err.println(Thread.currentThread().getName() + " timeout : " + (a2 - a1));
			}
	
			try {
				Thread.sleep(1000 * r.nextInt(9));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		client.close();
		
	}

	public static void simuAsync() {

		NTcpClient client = new NTcpClient();
		client.setHost("127.0.0.1");
		client.setPort(8000);
		client.setHeartbeatInervalSec(1);		
		client.setReadTimeout(30);
		
		
		Map req = new HashMap();
		String d = (new Date()).toLocaleString();
		req.put("date", d);
		req.put("time", System.currentTimeMillis());
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < 5000; i++) {
			ids.add(UUID.randomUUID().toString());
		}
		req.put("udis", ids);

		Car c = new Car();
		c.setName("Dazhong");
		c.setPrice(180000);
		c.setSpeed(1500);
		c.setBrand("good");
		req.put("car", c);

		long a1 = System.currentTimeMillis();
		client.send(req);		
		Object response = client.recv();
		client.disconnect();
		long a2 = System.currentTimeMillis();
		logger.info((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + response);
		logger.debug((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + response);
		if ((a2 - a1) > 1000) {
			System.err.println(Thread.currentThread().getName() + " timeout : " + (a2 - a1));
		}
		

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void simuMulti() {
		for (int i = 0; i < 10; i++) {
			try {
				simuSync();
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < 10; i++) {
			try {
				simuAsync();
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

	public static void main(String[] args) throws InterruptedException {
		simuSync();
		System.exit(0);
		
		logger.debug("=====================================");
		for (int i = 0; i < 4; i++) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("Child-" + Thread.currentThread().getId());
					MDC.put("TRCODE", "Client");
					
//					 s();
//					 simu();
					simuMulti();
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
