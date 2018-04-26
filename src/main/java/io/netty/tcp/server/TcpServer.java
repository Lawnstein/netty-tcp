/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgDecoder;
import io.netty.tcp.message.handler.coding.AbstractFixedLengthHeaderByteMsgEncoder;
import io.netty.tcp.message.handler.coding.impl.KyroObjectMsgDecoder;
import io.netty.tcp.message.handler.coding.impl.KyroObjectMsgEncoder;
import io.netty.tcp.util.CommUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

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

	private static int DEFAULT_READTIMEOUT = 10;

	private int writeTimeout = 0;

	private static int DEFAULT_WRITETIMEOUT = 3;

	private int timeout = 0;

	private int backlog = 10240;

	private int maxBossThreads = 0;

	private int maxNioThreads = 0;

	private int minServiceThreads = 0;

	private int maxServiceThreads = Runtime.getRuntime().availableProcessors() * 2;

	private int threadKeepAliveSeconds = 0;

	private boolean shortConnection = false;

	private AbstractFixedLengthHeaderByteMsgDecoder messageDecoder = null;

	private AbstractFixedLengthHeaderByteMsgEncoder messageEncoder = null;

	private ServiceAppHandler serviceHandler = null;

	private EventLoopGroup bossGroup = null;

	private EventLoopGroup workerGroup = null;

	private ServerMessageHandler serverMessageHandler = null;

	private static String PROP_DEBUG = "ccbs.tcp.netty.debug";
	private static String PROP_SHORTCONNECTION = "ccbs.tcp.netty.shortconnection";
	private static String PROP_BACKLOG = "ccbs.tcp.netty.backlog";
	private static String PROP_BOSSTHREDS = "ccbs.tcp.netty.bossthreads";
	private static String PROP_NIOTHREDS = "ccbs.tcp.netty.niothreads";
	private static String PROP_SERVICETHREDS = "ccbs.tcp.netty.servicethreads";

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

	public ServerMessageHandler createServiceHandler() {
//		if (serverMessageHandler != null)
//			return serverMessageHandler;

		ServerMessageHandler handler = new ServerMessageHandler(serviceHandler);
		handler.setName(getName() + "-" + port);
		handler.setMinServiceThreads(getMinServiceThreads());
		handler.setMaxServiceThreads(getMaxServiceThreads());
		handler.setThreadKeepAliveSeconds(getThreadKeepAliveSeconds());
		handler.setShortConnection(isShortConnection());
		handler.setMessageDecoder(this.messageDecoder);
		handler.setMessageEncoder(this.messageEncoder);
//		serverMessageHandler = handler;
		return handler;
	}

	public void initEnvProps() {
		if (!this.debug) {
			if (null != System.getProperty(PROP_DEBUG))
				this.debug = System.getProperty(PROP_DEBUG).equalsIgnoreCase("true") ? true : false;
		}
		if (null != System.getProperty(PROP_SHORTCONNECTION))
			this.shortConnection = System.getProperty(PROP_SHORTCONNECTION).equalsIgnoreCase("true");
		if (null != System.getProperty(PROP_BACKLOG))
			this.backlog = CommUtil.toInt(System.getProperty(PROP_BACKLOG), 1024);
		if (null != System.getProperty(PROP_BOSSTHREDS))
			this.maxBossThreads = CommUtil.toInt(System.getProperty(PROP_BOSSTHREDS), 1);
		if (null != System.getProperty(PROP_NIOTHREDS))
			this.maxNioThreads = CommUtil.toInt(System.getProperty(PROP_NIOTHREDS),
					Runtime.getRuntime().availableProcessors() * 2);
		if (null != System.getProperty(PROP_SERVICETHREDS))
			this.maxServiceThreads = CommUtil.toInt(System.getProperty(PROP_SERVICETHREDS),
					Runtime.getRuntime().availableProcessors());
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
			throw new RuntimeException("start TcpServer({},{}) failed, no decoder configured.");
		}
		if (messageEncoder == null) {
			logger.error("invalid config, no encoder configured.");
			throw new RuntimeException("start TcpServer({},{}) failed, no encoder configured.");
		}

		logger.debug("TcpServer.name : {}", name);
		logger.debug("TcpServer.port : {}", port);
		logger.debug("TcpServer.readTimeout : {}", readTimeout);
		logger.debug("TcpServer.writeTimeout : {}", writeTimeout);
		logger.debug("TcpServer.SO_BACKLOG : {}", backlog);
		logger.debug("TcpServer.maxNioThreads : {}", maxNioThreads);
		logger.debug("TcpServer.maxServiceThreads : {}", maxServiceThreads);
		logger.debug("TcpServer.messageDecoder : {}", messageDecoder);
		logger.debug("TcpServer.messageEncoder : {}", messageEncoder);

		if (maxBossThreads > 0)
			bossGroup = new NioEventLoopGroup(maxBossThreads);
		else
			bossGroup = new NioEventLoopGroup();
		if (maxNioThreads > 0)
			workerGroup = new NioEventLoopGroup(maxNioThreads);
		else
			workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_BACKLOG, backlog)
					.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.ALLOW_HALF_CLOSURE, true)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							if (readTimeout > 0)
								ch.pipeline().addLast(new ReadTimeoutHandler(readTimeout));
							if (writeTimeout > 0)
								ch.pipeline().addLast(new WriteTimeoutHandler(writeTimeout));

							ch.pipeline().addLast(messageDecoder.clone());
							ch.pipeline().addLast(messageEncoder.clone());
							ch.pipeline().addLast(createServiceHandler());
						}
					});

			/*
			 * serverBootstrap.option(ChannelOption.SO_REUSEADDR, true).
			 * option(ChannelOption.SO_BACKLOG, backlog)
			 * .option(ChannelOption.SO_KEEPALIVE, true).
			 * option(ChannelOption.TCP_NODELAY, true)
			 * .option(ChannelOption.ALLOW_HALF_CLOSURE, true)
			 * .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			 * .childOption(ChannelOption.ALLOCATOR,
			 * PooledByteBufAllocator.DEFAULT);
			 */

			logger.info("try to bind port {} for {} ...", port, name);
			ChannelFuture f = serverBootstrap.bind(port).sync();
			if (f.isSuccess()) {
				logger.info("{} started on port {}", name, port);
			}
			f.channel().closeFuture().sync();
		} finally {
			stop();
		}
	}

	public void start() throws Exception {

		new Thread(new Runnable() {

			@Override
			public void run() {
				Thread.currentThread().setName(name);
				try {
					bind(port);
				} catch (Exception e) {
					logger.error("{} started on port {} Exception : {}", name, port, e);
					e.printStackTrace();
				}
			}

		}).start();
	}

	public void stop() throws Exception {
		logger.info("{} stopping ...", name);
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		ServerMessageHandler.stopThreadPool(getName());
	}
}
