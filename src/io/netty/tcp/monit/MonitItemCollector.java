/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.monit;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.netty.tcp.client.NTcpClient;
import io.netty.tcp.monit.unit.MonitItemQueue;
import io.netty.tcp.util.ExceptionUtil;

/**
 * 监控信息采集器.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class MonitItemCollector {
	protected final static Logger logger = LoggerFactory.getLogger(MonitItemCollector.class);

	private int maxQueueCapacity = -1;

	private int maxPollBatchSize = -1;

	private MonitItemQueue monitQueue = new MonitItemQueue();

	private boolean queueActivated = false;

	private String monitServAddress;

	private int monitServPort;

	private NTcpClient monitConnection = new NTcpClient();;

	public MonitItemCollector() {
	}

	public int getMaxQueueCapacity() {
		return maxQueueCapacity;
	}

	public void setMaxQueueCapacity(int maxQueueCapacity) {
		this.maxQueueCapacity = maxQueueCapacity;
		monitQueue.setQueueCapacity(maxQueueCapacity);
	}

	public int getMaxPollBatchSize() {
		return maxPollBatchSize;
	}

	public void setMaxPollBatchSize(int maxPollBatchSize) {
		this.maxPollBatchSize = maxPollBatchSize;
		monitQueue.setPollBatchSize(maxPollBatchSize);
	}

	public MonitItemQueue getMonitQueue() {
		return monitQueue;
	}

	public void setMonitQueue(MonitItemQueue monitQueue) {
		this.monitQueue = monitQueue;
	}

	public String getMonitServAddress() {
		return monitServAddress;
	}

	public void setMonitServAddress(String monitServAddress) {
		this.monitServAddress = monitServAddress;
	}

	public int getMonitServPort() {
		return monitServPort;
	}

	public void setMonitServPort(int monitServPort) {
		this.monitServPort = monitServPort;
	}

	/**
	 * 推送监控要素信息。
	 * 
	 * @param monitItem
	 */
	public void push(Object monitItem) {
		monitQueue.insert(monitItem);
		if (!queueActivated) {
			synchronized (monitConnection) {
				if (!queueActivated) {
					open();
				}
			}
		}
	}

	/**
	 * 开启采集.
	 */
	public void open() {
		synchronized (monitServAddress) {
			queueActivated = true;
			monitConnection = new NTcpClient();
			monitConnection.setHost(monitServAddress);
			monitConnection.setPort(monitServPort);
			// monitConnection.connect();

			new Thread(new Runnable() {

				@Override
				public void run() {
					Thread.currentThread().setName("monitThread");
					logger.debug("try to start send monit item to server " + monitConnection);
					sending();
				}

			}).start();
		}
	}

	/**
	 * 关闭采集.
	 */
	public void close() {
		monitQueue.clear();
		monitConnection.close();
		monitConnection = null;
		queueActivated = false;
	}

	private void sending() {
		List<Object> sndItemList = new ArrayList<Object>();
		int numb = -1;
		while (true) {
			try {
				numb = monitQueue.fetch(sndItemList);
				logger.debug("fetch " + numb + " monitItem(s) to send to " + monitServAddress + ":" + monitServPort);
			} catch (InterruptedException e1) {
			}

			if (numb < 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				int z = 1;
				try {
					for (Object item : sndItemList) {
						logger.trace(z++ + "/" + numb + "	monitItem will send : " + item);
						monitConnection.send(item);
						// Object r = monitConnection.call(item);
						// logger.trace(z++ + "/" + numb + " response " + r);
					}
				} catch (Throwable th) {
					logger.error("send " + numb + " monitItem(s) to " + monitServAddress + ":" + monitServPort
							+ " failed : " + ExceptionUtil.getStackTrace(th));
				}
			}
		}
	}

}
