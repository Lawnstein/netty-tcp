/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月21日 下午3:16:54 <br> vc: $Id: $
 */

package io.netty.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 线程池. <br>
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class ThreadPoolServiceExecutor implements ExecutorService {

	private int corePoolSize = Runtime.getRuntime().availableProcessors();

	private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;

	private int queueCapacity = 1000;

	private int keepAliveSeconds = 600;

	private String threadNamePrefix = "thread-pool";

	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

	private boolean allowCoreThreadTimeOut = false;

	private boolean waitForTasksToCompleteOnShutdown = true;

	private int awaitTerminationSeconds = 0;

	private ThreadPoolExecutor pool;

	private boolean actived = false;

	public ThreadPoolServiceExecutor() {
		super();
	}

	/**
	 * @param corePoolSize
	 * @param maxPoolSize
	 * @param threadNamePrefix
	 */
	public ThreadPoolServiceExecutor(int corePoolSize, int maxPoolSize, String threadNamePrefix) {
		super();
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.threadNamePrefix = threadNamePrefix;
	}

	/**
	 * @param corePoolSize
	 * @param maxPoolSize
	 * @param queueCapacity
	 * @param threadNamePrefix
	 */
	public ThreadPoolServiceExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, String threadNamePrefix) {
		super();
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.queueCapacity = queueCapacity;
		this.threadNamePrefix = threadNamePrefix;
	}

	/**
	 * @param corePoolSize
	 * @param maxPoolSize
	 * @param queueCapacity
	 * @param keepAliveSeconds
	 * @param threadNamePrefix
	 */
	public ThreadPoolServiceExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds, String threadNamePrefix) {
		super();
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.queueCapacity = queueCapacity;
		this.keepAliveSeconds = keepAliveSeconds;
		this.threadNamePrefix = threadNamePrefix;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
		if (getMaxPoolSize() < this.corePoolSize) {
			this.maxPoolSize = this.corePoolSize;
		}
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		if (maxPoolSize < getCorePoolSize()) {
			this.maxPoolSize = getCorePoolSize();
		} else {
			this.maxPoolSize = maxPoolSize;
		}
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	public RejectedExecutionHandler getRejectedExecutionHandler() {
		return rejectedExecutionHandler;
	}

	public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler = rejectedExecutionHandler;
	}

	public boolean isAllowCoreThreadTimeOut() {
		return allowCoreThreadTimeOut;
	}

	public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
	}

	public boolean isWaitForTasksToCompleteOnShutdown() {
		return waitForTasksToCompleteOnShutdown;
	}

	public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
		this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
	}

	public int getAwaitTerminationSeconds() {
		return awaitTerminationSeconds;
	}

	public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
		this.awaitTerminationSeconds = awaitTerminationSeconds;
	}

	private void init() {
		if (actived) {
			return;
		}

		synchronized (ThreadPoolServiceExecutor.class) {
			if (actived) {
				return;
			}

			/**
			 * java.util.concurrent.ThreadPoolExecutor.ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, <br>
			 * BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) <br>
			 * <br>
			 * Creates a new ThreadPoolExecutor with the given initial parameters. <br>
			 * <br>
			 * Parameters:<br>
			 * corePoolSize the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set<br>
			 * maximumPoolSize the maximum number of threads to allow in the pool<br>
			 * keepAliveTime when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks
			 * before terminating.<br>
			 * unit the time unit for the keepAliveTime argument<br>
			 * workQueue the queue to use for holding tasks before they are executed. This queue will hold only the Runnable tasks submitted by the execute
			 * method.<br>
			 * threadFactory the factory to use when the executor creates a new thread<br>
			 * handler the handler to use when execution is blocked because the thread bounds and queue capacities are reached<br>
			 * Throws:<br>
			 * IllegalArgumentException - if one of the following holds:<br>
			 * corePoolSize < 0<br>
			 * keepAliveTime < 0<br>
			 * maximumPoolSize <= 0<br>
			 * maximumPoolSize < corePoolSize<br>
			 * NullPointerException - if workQueue or threadFactory or handler is null<br>
			 */
			pool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueCapacity), new NamedThreadFactory(threadNamePrefix), rejectedExecutionHandler);
			actived = true;
		}

	}

	@Override
	public void execute(Runnable tasklet) {
		init();
		pool.execute(tasklet);

	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		if (!actived)
			return true;
		return pool.awaitTermination(timeout, unit);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		init();
		return pool.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		init();
		return pool.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		init();
		return pool.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
	        throws InterruptedException, ExecutionException, TimeoutException {
		init();
		return pool.invokeAny(tasks, timeout, unit);
	}

	@Override
	public boolean isShutdown() {
		if (!actived)
			return true;
		return pool.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		if (!actived)
			return true;
		return pool.isTerminated();
	}

	@Override
	public void shutdown() {
		if (!actived)
			return;
		pool.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		if (!actived)
			return null;
		return pool.shutdownNow();
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		init();
		return pool.submit(task);
	}

	@Override
	public Future<?> submit(Runnable task) {
		init();
		return pool.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		init();
		return pool.submit(task, result);
	}

}
