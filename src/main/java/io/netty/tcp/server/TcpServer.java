/**
 * netty-tcp. Copyright (C) 1999-2017, All rights reserved. This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */

package io.netty.tcp.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.executor.NamedThreadFactory;
import io.netty.handler.ServiceAppHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgDecoder;
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgEncoder;
import io.netty.tcp.message.handler.coding.impl.KyroObjectMsgDecoder;
import io.netty.tcp.message.handler.coding.impl.KyroObjectMsgEncoder;
import io.netty.tcp.util.CommUtil;
import io.netty.util.concurrent.Future;

/**
 * TCP服务器.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class TcpServer {

	protected final static Logger logger = LoggerFactory.getLogger(TcpServer.class);

	private static String DEFAULT_NAME = "TcpServer";

	private String name = DEFAULT_NAME;

	private int port = 80;

	private int readTimeout = 0;

	private static int DEFAULT_READTIMEOUT = 30;

	private int writeTimeout = 0;

	private static int DEFAULT_WRITETIMEOUT = 3;

	private int timeout = 0;

	private int backlog = 10240;

	private int maxBossThreads = 2;

	private int maxNioThreads = Runtime.getRuntime().availableProcessors();

	private int minServiceThreads = 0;

	private int maxServiceThreads = 0;

	private int threadKeepAliveSeconds = 0;

	/**
	 * 设置了serviceThreadPool后，上述minServiceThreads、maxServiceThreads无效:无需自动创建了.<br>
	 * 当minServiceThreads、maxServiceThreads、serviceThreadPool均不配置时，则不再开启服务线程池，而采用Nio线程池.<br>
	 */
	private ExecutorService serviceThreadPool;
	
	private boolean shortConnection = false;

	private boolean daemon = true;

	private boolean shutdownGracefully = false;

	private AbstractFixedLengthHeaderByteMsgDecoder messageDecoder = null;

	private AbstractFixedLengthHeaderByteMsgEncoder messageEncoder = null;

	private ServiceAppHandler serviceHandler = null;

	private EventLoopGroup bossGroup = null;

	private EventLoopGroup workerGroup = null;

	private Thread listenThrd = null;

	private static String PROP_DEBUG = "tcp.netty.debug";
	private static String PROP_SHORTCONNECTION = "tcp.netty.shortconnection";
	private static String PROP_SHUTDOWNGRACEFULLY = "tcp.netty.shutdowngracefully";
	private static String PROP_BACKLOG = "tcp.netty.backlog";
	private static String PROP_BOSSTHREDS = "tcp.netty.bossthreads";
	private static String PROP_NIOTHREDS = "tcp.netty.niothreads";
	private static String PROP_SERVICETHREDS = "tcp.netty.servicethreads";

	private static boolean debug = false;

	public TcpServer() {
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(int writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		if (timeout < 0)
			return;
		this.timeout = timeout;
		if (this.readTimeout <= 0)
			this.readTimeout = timeout;
		if (this.writeTimeout <= 0)
			this.writeTimeout = timeout;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getMaxBossThreads() {
		return maxBossThreads;
	}

	public void setMaxBossThreads(int maxBossThreads) {
		if (maxBossThreads >= 0)
			this.maxBossThreads = maxBossThreads;
	}

	public int getMaxNioThreads() {
		return maxNioThreads;
	}

	public void setMaxNioThreads(int maxNioThreads) {
		if (maxNioThreads >= 0)
			this.maxNioThreads = maxNioThreads;
	}

	public int getMinServiceThreads() {
		return minServiceThreads;
	}

	public void setMinServiceThreads(int minServiceThreads) {
		if (minServiceThreads >= 0)
			this.minServiceThreads = minServiceThreads;
	}

	public int getMaxServiceThreads() {
		return maxServiceThreads;
	}

	public void setMaxServiceThreads(int maxServiceThreads) {
		if (maxServiceThreads >= 0)
			this.maxServiceThreads = maxServiceThreads;
	}

	public int getThreadKeepAliveSeconds() {
		return threadKeepAliveSeconds;
	}

	public void setThreadKeepAliveSeconds(int threadKeepAliveSeconds) {
		this.threadKeepAliveSeconds = threadKeepAliveSeconds;
	}

	public ExecutorService getServiceThreadPool() {
		return serviceThreadPool;
	}

	public void setServiceThreadPool(ExecutorService serviceThreadPool) {
		this.serviceThreadPool = serviceThreadPool;
	}
	
	public int getWorkerNumb() {
		return maxServiceThreads;
	}

	public void setWorkerNumb(int maxServiceThreads) {
		this.maxServiceThreads = maxServiceThreads;
	}

	public boolean isShortConnection() {
		return shortConnection;
	}

	public void setShortConnection(boolean shortConnection) {
		this.shortConnection = shortConnection;
	}

	public boolean isDaemon() {
		return daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public boolean isShutdownGracefully() {
		return shutdownGracefully;
	}

	public void setShutdownGracefully(boolean shutdownGracefully) {
		this.shutdownGracefully = shutdownGracefully;
	}

	public AbstractFixedLengthHeaderByteMsgDecoder getMessageDecoder() {
		return messageDecoder;
	}

	public void setMessageDecoder(AbstractFixedLengthHeaderByteMsgDecoder messageDecoder) {
		this.messageDecoder = messageDecoder;
	}

	public AbstractFixedLengthHeaderByteMsgEncoder getMessageEncoder() {
		return messageEncoder;
	}

	public void setMessageEncoder(AbstractFixedLengthHeaderByteMsgEncoder messageEncoder) {
		this.messageEncoder = messageEncoder;
	}

	public ServiceAppHandler getServiceHandler() {
		return serviceHandler;
	}

	public void setServiceHandler(ServiceAppHandler serviceHandler) {
		this.serviceHandler = serviceHandler;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		TcpServer.debug = debug;
	}

	public TcpMessageHandler createMessageHandler() {
		TcpMessageHandler handler = new TcpMessageHandler(getServiceHandler());
		handler.setName(getName() + "-" + getPort());
		handler.setDebug(debug);
		handler.setMinServiceThreads(getMinServiceThreads());
		handler.setMaxServiceThreads(getMaxServiceThreads());
		handler.setServiceThreadPool(getServiceThreadPool());
		handler.setThreadKeepAliveSeconds(getThreadKeepAliveSeconds());
		handler.setShortConnection(isShortConnection());
		handler.setMessageDecoder(getMessageDecoder());
		handler.setMessageEncoder(getMessageEncoder());
		return handler;
	}

	public void initEnvProps() {
		if (!this.debug) {
			if (null != System.getProperty(PROP_DEBUG))
				this.debug = System.getProperty(PROP_DEBUG).equalsIgnoreCase("true") ? true : false;
		}
		if (null != System.getProperty(PROP_SHORTCONNECTION))
			this.shortConnection = System.getProperty(PROP_SHORTCONNECTION).equalsIgnoreCase("true");
		if (null != System.getProperty(PROP_SHUTDOWNGRACEFULLY))
			this.shutdownGracefully = System.getProperty(PROP_SHUTDOWNGRACEFULLY).equalsIgnoreCase("true");
		if (null != System.getProperty(PROP_BACKLOG))
			this.backlog = CommUtil.toInt(System.getProperty(PROP_BACKLOG), 10240);
		if (null != System.getProperty(PROP_BOSSTHREDS))
			this.maxBossThreads = CommUtil.toInt(System.getProperty(PROP_BOSSTHREDS), 1);
		if (null != System.getProperty(PROP_NIOTHREDS))
			this.maxNioThreads = CommUtil.toInt(System.getProperty(PROP_NIOTHREDS), Runtime.getRuntime().availableProcessors());
		if (null != System.getProperty(PROP_SERVICETHREDS))
			this.maxServiceThreads = CommUtil.toInt(System.getProperty(PROP_SERVICETHREDS), Runtime.getRuntime().availableProcessors() * 2);
	}

	public void bind(int port) throws Exception {
		initEnvProps();
		if (messageDecoder == null) {
			messageDecoder = new KyroObjectMsgDecoder();
		}
		if (messageEncoder == null && messageDecoder instanceof KyroObjectMsgDecoder) {
			messageEncoder = new KyroObjectMsgEncoder();
		}
		if (messageDecoder == null) {
			logger.error("invalid config, no decoder configured.");
			throw new RuntimeException("start TcpServer(" + name + ", " + port + ") failed, no decoder configured.");
		}
		if (messageEncoder == null) {
			logger.error("invalid config, no encoder configured.");
			throw new RuntimeException("start TcpServer(" + name + ", " + port + ") failed, no encoder configured.");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("TcpServer.name : {}", name);
			logger.debug("TcpServer.port : {}", port);
			logger.debug("TcpServer.timeout : {}", timeout);
			logger.debug("TcpServer.readTimeout : {}", readTimeout);
			logger.debug("TcpServer.writeTimeout : {}", writeTimeout);
			logger.debug("TcpServer.so_backlog : {}", backlog);
			logger.debug("TcpServer.maxBossThreads : {} {}", maxBossThreads, maxBossThreads == 0 ? "(will use default setting)" : "");
			logger.debug("TcpServer.maxNioThreads : {} {}", maxNioThreads, maxNioThreads == 0 ? "(will use default setting)" : "");
			logger.debug("TcpServer.maxServiceThreads : {}", maxServiceThreads);
			logger.debug("TcpServer.messageDecoder : {}", messageDecoder);
			logger.debug("TcpServer.messageEncoder : {}", messageEncoder);
		}

		//EventLoopGroup默认的线程数是CPU核数的二倍
		if (maxBossThreads > 0) {
			bossGroup = new NioEventLoopGroup(maxBossThreads, new NamedThreadFactory(name+"-boss"));
		} else {
			bossGroup = new NioEventLoopGroup();
		}
		if (maxNioThreads > 0) {
			workerGroup = new NioEventLoopGroup(maxNioThreads, new NamedThreadFactory(name+"-worker"));
		} else {
			workerGroup = new NioEventLoopGroup();
		}
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).
				channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_REUSEADDR, true)	// 端口复用
				.option(ChannelOption.SO_BACKLOG, backlog)	//最大并发连接数
				.option(ChannelOption.SO_KEEPALIVE, true)	//是否保持长连接,可发送keep-alive包
				.option(ChannelOption.TCP_NODELAY, true)	//是否允许延迟组包发送
				.option(ChannelOption.SO_RCVBUF, 256 * 1024)	//设置接收缓冲区大小
				.option(ChannelOption.SO_SNDBUF, 256 * 1024)	//设置发送缓冲区大小
				.option(ChannelOption.ALLOW_HALF_CLOSURE, true)	//是否允许半关闭状态,主要用于server可继续发送数据,client不能发送数据
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)	//零拷贝
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)	//零拷贝
				.childHandler(new ChannelInitializer<SocketChannel>() {
				        @Override
				        public void initChannel(SocketChannel ch) throws Exception {
				        	if (isDebug() && logger.isDebugEnabled()) {
				        			logger.debug("{} accepted.", ch);
				        	}
				        	
					        if (readTimeout > 0)
						        ch.pipeline().addLast(new ReadTimeoutHandler(readTimeout));
					        if (writeTimeout > 0)
						        ch.pipeline().addLast(new WriteTimeoutHandler(writeTimeout));

					        ch.pipeline().addLast(messageDecoder.clone());
					        ch.pipeline().addLast(messageEncoder.clone());
					        ch.pipeline().addLast(createMessageHandler());
				        }
			        });

			logger.debug("try to bind port {} for {} ...", port, name);
			ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly().addListener(future -> {
				logger.info("Tcp Server '{}' on port {} started.", name, port);
			});
			channelFuture.channel().closeFuture().syncUninterruptibly().addListener(future -> {
				logger.info("Tcp Server '{}' on port {} shutdown ......", name, port);
			});
		} finally {
			stop();
		}
	}

	public void start() throws Exception {
		listenThrd = new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName(name);
				try {
					bind(port);
				} catch (Exception e) {
					logger.error("{} bind on port {} failed.", name, port, e);
				}
			}
		});
		if (daemon) {
			listenThrd.setDaemon(true);
		}
		listenThrd.start();
	}

	public void stop() throws Exception {
		logger.info("{} stopping ...", name);

		synchronized (name) {

			if ((bossGroup == null || bossGroup.isShutdown()) && (workerGroup == null || workerGroup.isShutdown())) {
				return;
			}

			try {
				Future bf = null;
				Future wf = null;
				if (shutdownGracefully) {
					bf = bossGroup.shutdownGracefully();
					wf = workerGroup.shutdownGracefully();
				} else {
					bf = bossGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
					wf = workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
				}
				TcpMessageHandler.stopThreadPool(getName());
				if (logger.isTraceEnabled())
					logger.trace("bf.isDone={}, bf.isSuccess={}, bf.shutdown={}, wf.isDone={}, wf.isSuccess={}, wf.shutdown={}", bf != null ? bf
					        .isDone() : null, bf != null ? bf.isSuccess() : null, bossGroup
					                .isShutdown(), wf != null ? wf.isDone() : null, wf != null ? wf.isSuccess() : null, workerGroup.isShutdown());

				if (!bossGroup.isShutdown() || !workerGroup.isShutdown()) {
					for (int i = 0; i < 10; i++) {
						Thread.sleep(1000);
						if (!bossGroup.isShutdown() || !workerGroup.isShutdown()) {
							if (logger.isTraceEnabled())
								logger.trace("bf.isDone={}, bf.isSuccess={}, bf.shutdown={}, wf.isDone={}, wf.isSuccess={}, wf.shutdown={}", bf != null ? bf
								        .isDone() : null, bf != null ? bf.isSuccess() : null, bossGroup
								                .isShutdown(), wf != null ? wf.isDone() : null, wf != null ? wf.isSuccess() : null, workerGroup.isShutdown());
							continue;
						}
						break;
					}
					if (logger.isTraceEnabled()) {
						logger.trace("bf.isDone={}, bf.isSuccess={}, bf.shutdown={}, wf.isDone={}, wf.isSuccess={}, wf.shutdown={}", bf != null ? bf
						        .isDone() : null, bf != null ? bf.isSuccess() : null, bossGroup
						                .isShutdown(), wf != null ? wf.isDone() : null, wf != null ? wf.isSuccess() : null, workerGroup.isShutdown());
					}
				}

				if (listenThrd != null) {
					listenThrd.interrupt();
				}

			} catch (Throwable th) {
				logger.warn("{} stop error.", name, th);
			}

		}
	}
}
