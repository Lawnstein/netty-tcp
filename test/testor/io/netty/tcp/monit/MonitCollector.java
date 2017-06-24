/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package testor.io.netty.tcp.monit;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.netty.tcp.monit.MonitItemCollector;

/**
 * 监控客户端采集.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class MonitCollector {

	protected final static Logger logger = LoggerFactory.getLogger(MonitCollector.class);

	protected final static Random r = new Random();

	public MonitCollector() {
	}

	public static void collect(MonitItemCollector client) throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			Map m = new HashMap();
			m.put("seq", i);
			m.put("threadName", Thread.currentThread().getName());
			int t = r.nextInt(20);
			for (int k = 0; k < t; k++) {
				m.put("clientStamp-" + k, UUID.randomUUID().toString());				
			}
			client.push(m);
			if (i > 0 && i % 100 == 0)
				Thread.sleep(r.nextInt(100) * r.nextInt(100));
		}
	}

	public static void forkTask(final MonitItemCollector client) throws InterruptedException {
		for (int j = 0; j < 10; j++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						collect(client);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}).start();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		MDC.put("TRCODE", "MonitClient");

		final MonitItemCollector collector = new MonitItemCollector();
		collector.setMonitServAddress("127.0.0.1");
		collector.setMonitServPort(8100);
		collector.open();
		// Thread.sleep(1000);
		logger.info("MonitItemCollector opened, try to new thread to collect...");

		while (true) {
			forkTask(collector);
			int s = 100 * r.nextInt(500);
			logger.info("suspend for " + (s / 1000) + " seconds.");
			Thread.sleep(s);
		}
	}
}
