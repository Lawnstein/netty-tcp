/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package testor.io.netty.single;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.netty.tcp.client.TcpClient;

/**
 * TODO 请填写注释.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class SimpleClient {

	protected final static Logger logger = LoggerFactory.getLogger(SimpleClient.class);

	/**
	 * 
	 */
	public SimpleClient() {
		// TODO 自动生成的构造函数存根
	}

	public static void simuSync() {

		TcpClient client = new TcpClient();
		client.setHost("127.0.0.1");
		client.setPort(8000);
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
		Object response = client.call(req);
		long a2 = System.currentTimeMillis();
		logger.info((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + response);
		logger.debug((a2 - a1) + " " + Thread.currentThread().getName() + " Response:" + response);
		if ((a2 - a1) > 1000) {
			System.err.println(Thread.currentThread().getName() + " timeout : " + (a2 - a1));
		}
	}

	public static void simuAsync() {

		TcpClient client = new TcpClient();
		client.setHost("127.0.0.1");
		client.setPort(8000);
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
		simuAsync();
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
