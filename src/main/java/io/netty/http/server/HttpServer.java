/**
 * netty-tcp. Copyright (C) 1999-2017, All rights reserved. This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */

package io.netty.http.server;

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
import io.netty.handler.ServiceAppHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
// import io.netty.tcp.server.HttpMessageHandler;
import io.netty.tcp.util.CommUtil;
import io.netty.util.concurrent.Future;

/**
 * HTTP服务器.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class HttpServer {

	protected final static Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private static String DEFAULT_NAME = "HttpServer";

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

	private boolean daemon = true;

	private boolean shutdownGracefully = false;

	// private AbstractFixedLengthHeaderByteMsgDecoder messageDecoder = null;
	//
	// private AbstractFixedLengthHeaderByteMsgEncoder messageEncoder = null;

	private ServiceAppHandler serviceHandler = null;

	private EventLoopGroup bossGroup = null;

	private EventLoopGroup workerGroup = null;

	// private HttpMessageHandler serverMessageHandler = null;

	private Thread listenThrd = null;

	private static String PROP_DEBUG = "http.netty.debug";
	private static String PROP_SHORTCONNECTION = "http.netty.shortconnection";
	private static String PROP_SHUTDOWNGRACEFULLY = "http.netty.shutdowngracefully";
	private static String PROP_BACKLOG = "http.netty.backlog";
	private static String PROP_BOSSTHREDS = "http.netty.bossthreads";
	private static String PROP_NIOTHREDS = "http.netty.niothreads";
	private static String PROP_SERVICETHREDS = "http.netty.servicethreads";

	private static boolean debug = false;

	public HttpServer() {
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

	// public AbstractFixedLengthHeaderByteMsgDecoder getMessageDecoder() {
	// return messageDecoder;
	// }
	//
	// public void setMessageDecoder(AbstractFixedLengthHeaderByteMsgDecoder messageDecoder) {
	// this.messageDecoder = messageDecoder;
	// }
	//
	// public AbstractFixedLengthHeaderByteMsgEncoder getMessageEncoder() {
	// return messageEncoder;
	// }
	//
	// public void setMessageEncoder(AbstractFixedLengthHeaderByteMsgEncoder messageEncoder) {
	// this.messageEncoder = messageEncoder;
	// }

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
		HttpServer.debug = debug;
	}

	public HttpMessageHandler createMessageHandler() {
		HttpMessageHandler handler = new HttpMessageHandler(serviceHandler);
		handler.setName(getName() + "-" + port);
		handler.setDebug(debug);
		handler.setMinServiceThreads(getMinServiceThreads());
		handler.setMaxServiceThreads(getMaxServiceThreads());
		handler.setThreadKeepAliveSeconds(getThreadKeepAliveSeconds());
		handler.setShortConnection(isShortConnection());
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
			this.backlog = CommUtil.toInt(System.getProperty(PROP_BACKLOG), 1024);
		if (null != System.getProperty(PROP_BOSSTHREDS))
			this.maxBossThreads = CommUtil.toInt(System.getProperty(PROP_BOSSTHREDS), 1);
		if (null != System.getProperty(PROP_NIOTHREDS))
			this.maxNioThreads = CommUtil.toInt(System.getProperty(PROP_NIOTHREDS), Runtime.getRuntime().availableProcessors() * 2);
		if (null != System.getProperty(PROP_SERVICETHREDS))
			this.maxServiceThreads = CommUtil.toInt(System.getProperty(PROP_SERVICETHREDS), Runtime.getRuntime().availableProcessors());
	}

	public void bind(int port) throws Exception {
		initEnvProps();
		// if (messageDecoder == null) {
		// messageDecoder = new KyroObjectMsgDecoder();
		// }
		// if (messageEncoder == null && messageDecoder instanceof KyroObjectMsgDecoder) {
		// messageEncoder = new KyroObjectMsgEncoder();
		// }
		// if (messageDecoder == null) {
		// logger.error("invalid config, no decoder configured.");
		// throw new RuntimeException("start HttpServer({},{}) failed, no decoder configured.");
		// }
		// if (messageEncoder == null) {
		// logger.error("invalid config, no encoder configured.");
		// throw new RuntimeException("start HttpServer({},{}) failed, no encoder configured.");
		// }

		if (logger.isDebugEnabled()) {
			logger.debug("HttpServer.name : {}", name);
			logger.debug("HttpServer.port : {}", port);
			logger.debug("HttpServer.readTimeout : {}", readTimeout);
			logger.debug("HttpServer.writeTimeout : {}", writeTimeout);
			logger.debug("HttpServer.SO_BACKLOG : {}", backlog);
			logger.debug("HttpServer.maxNioThreads : {}", maxNioThreads);
			logger.debug("HttpServer.maxServiceThreads : {}", maxServiceThreads);
			// logger.debug("HttpServer.messageDecoder : {}", messageDecoder);
			// logger.debug("HttpServer.messageEncoder : {}", messageEncoder);
		}

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
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_REUSEADDR, true)
			        .option(ChannelOption.SO_BACKLOG, backlog).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
			        .option(ChannelOption.ALLOW_HALF_CLOSURE, true).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).childHandler(new ChannelInitializer<SocketChannel>() {
				        @Override
				        public void initChannel(SocketChannel ch) throws Exception {
					        if (readTimeout > 0)
						        ch.pipeline().addLast(new ReadTimeoutHandler(readTimeout));
					        if (writeTimeout > 0)
						        ch.pipeline().addLast(new WriteTimeoutHandler(writeTimeout));

					        // ch.pipeline().addLast(messageDecoder.clone());
					        // ch.pipeline().addLast(messageEncoder.clone());
					        // ch.pipeline().addLast(createServiceHandler());

//					        // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
//					        ch.pipeline().addLast(new HttpResponseEncoder());
//					        // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
//					        ch.pipeline().addLast(new HttpRequestDecoder());
					        
					     // 解码成HttpRequest
					        ch.pipeline().addLast(new HttpServerCodec());

			                // 解码成FullHttpRequest
					        ch.pipeline().addLast(new HttpObjectAggregator(65536));

			                // 添加WebSocket解编码
					        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/"));
					        
					        ch.pipeline().addLast(createMessageHandler());
				        }
			        });

			logger.debug("try to bind port {} for {} ...", port, name);
			ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly().addListener(future -> {
				logger.info("Http Server '{}' on port {} started.", name, port);
			});
			channelFuture.channel().closeFuture().sync().addListener(future -> {
				logger.info("Http Server '{}' on port {} shutdown ......", name, port);
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
					logger.error("{} started on port {} Exception : {}", name, port, e);
					e.printStackTrace();
				}
			}

		});
		if (isDaemon()) {
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
				HttpMessageHandler.stopThreadPool(getName());
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
			} catch (Throwable th) {
				logger.warn("{} stop error.", name, th);
			}
		}
	}
}
