/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.monit.unit;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 监控要素队列.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class MonitItemQueue {
	protected final static Logger logger = LoggerFactory.getLogger(MonitItemQueue.class);

	private BlockingQueue<Object> monitQueue;

	private boolean alived = false;

	private int pollBatchSize = 200;

	private int queueCapacity = 81920;

	public MonitItemQueue() {
	}

	public MonitItemQueue(int maxQueueSize, int consumeBlockSize) {
		if (maxQueueSize > 0)
			this.queueCapacity = maxQueueSize;
		if (consumeBlockSize > 0)
			this.pollBatchSize = consumeBlockSize;
	}

	public int getPollBatchSize() {
		return pollBatchSize;
	}

	public void setPollBatchSize(int pollBatchSize) {
		if (pollBatchSize > 0)
			this.pollBatchSize = pollBatchSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		if (queueCapacity > 0)
			this.queueCapacity = queueCapacity;
	}

	public boolean isAlived() {
		return alived;
	}

	private synchronized void activate() {
		if (monitQueue == null)
			monitQueue = new ArrayBlockingQueue(queueCapacity);
		alived = true;
	}

	/**
	 * 抓取消息.
	 * 
	 * @param out
	 * @return -1 队列尚未激活.
	 * @throws InterruptedException
	 */
	public int fetch(List out) throws InterruptedException {
		if (alived) {
			out.clear();
			Object o = monitQueue.take();
			out.add(o);
			return monitQueue.drainTo(out, pollBatchSize - 1) + 1;
		}
		return -1;
	}

	/**
	 * 发送消息.
	 * 
	 * @param item
	 */
	public void insert(Object item) {
		if (!alived) {
			logger.debug("monitQueue is not alive, try to activate .");
			activate();
		}
		try {
			monitQueue.put(item);
		} catch (InterruptedException e) {
			logger.error("put monit item into queue InterruptedException : " + e);
		}
	}

	/**
	 * 清空.
	 */
	public void clear() {
		if (alived && monitQueue != null) {
			monitQueue.clear();
		}
	}
}
