/**
 * netty-tcp. Copyright (C) 1999-2017, All rights reserved. This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */

package io.netty.http.server;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ServiceAppHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.tcp.message.HeartBeatMessage;
import io.netty.tcp.server.NamedThreadFactory;
//import io.netty.tcp.server.ServiceAppHandler;
import io.netty.tcp.util.ExceptionUtil;

/**
 * http channel处理.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
@Sharable
public class HttpMessageHandler extends ChannelInboundHandlerAdapter {
	protected final static Logger logger = LoggerFactory.getLogger(HttpMessageHandler.class);

	protected static String KEY_PING = HeartBeatMessage.KEY_PING;
	protected static String KEY_HEARTBEAT = HeartBeatMessage.KEY_HEARTBEAT;
	protected static String KEY_ALIVE = HeartBeatMessage.KEY_ALIVE;
	// protected static byte[] KEY_ALIVE_BODY_BYTES = KEY_ALIVE.getBytes();

	// private AbstractFixedLengthHeaderByteMsgDecoder messageDecoder = null;
	//
	// private AbstractFixedLengthHeaderByteMsgEncoder messageEncoder = null;

	private ServiceAppHandler serviceHandler;

	private String name;

	private boolean debug;

	private int minServiceThreads = 0;

	private int maxServiceThreads = 0;

	private int threadKeepAliveSeconds = 0;

	private boolean shortConnection = false;

	/**
	 * 升级成Akka？
	 */
	private final static Map<String, ExecutorService> threadPoolMap = new HashMap<String, ExecutorService>();

	// private HttpRequest request;
	// private StringBuilder buffer = new StringBuilder();
	// private String url = "";
	// private String uri = "";
	// private StringBuilder respone;

	public HttpMessageHandler(ServiceAppHandler serviceHandler) {
		super();
		this.serviceHandler = serviceHandler;
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

	public int getMaxServiceThreads() {
		return maxServiceThreads;
	}

	public void setMaxServiceThreads(int maxServiceThreads) {
		this.maxServiceThreads = maxServiceThreads;
	}

	public boolean isShortConnection() {
		return shortConnection;
	}

	public void setShortConnection(boolean shortConnection) {
		this.shortConnection = shortConnection;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public ServiceAppHandler getServiceHandler() {
		return serviceHandler;
	}

	public void setServiceHandler(ServiceAppHandler serviceHandler) {
		this.serviceHandler = serviceHandler;
	}

	public int getMinServiceThreads() {
		return minServiceThreads;
	}

	public void setMinServiceThreads(int minServiceThreads) {
		this.minServiceThreads = minServiceThreads;
	}

	public int getThreadKeepAliveSeconds() {
		return threadKeepAliveSeconds;
	}

	public void setThreadKeepAliveSeconds(int threadKeepAliveSeconds) {
		this.threadKeepAliveSeconds = threadKeepAliveSeconds;
	}

	public ExecutorService getExcutorThread() {

		if (threadPoolMap.containsKey(name))
			return threadPoolMap.get(name);

		ExecutorService excutor = null;
		synchronized (threadPoolMap) {
			if (threadPoolMap.containsKey(name))
				excutor = threadPoolMap.get(name);
			else {
				// excutor = new ThreadPoolExecutor(this.minServiceThreads,
				// this.maxServiceThreads, threadKeepAliveSeconds,
				// TimeUnit.SECONDS,
				// new LinkedBlockingQueue<Runnable>());

				// excutor = Executors.newFixedThreadPool(maxServiceThreads);
				excutor = Executors.newFixedThreadPool(maxServiceThreads, new NamedThreadFactory(name));
				threadPoolMap.put(name, excutor);
			}
		}
		return excutor;
	}

	public static void stopThreadPool(String name) {
		if (threadPoolMap.containsKey(name)) {
			try {
				threadPoolMap.get(name).shutdownNow();
				threadPoolMap.get(name).shutdown();
			} catch (Throwable th) {
				logger.warn("ThreadPool {} shutdown Exception.", name, th);
			}
			threadPoolMap.remove(name);
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelRegistered();
		if (isDebug() && logger.isDebugEnabled())
			logger.debug("{} registered.", ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelUnregistered();
		if (isDebug() && logger.isDebugEnabled())
			logger.debug("{} unregistered.", ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelActive();
		if (isDebug() && logger.isDebugEnabled())
			logger.debug("{} active.", ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.serviceHandler.onChannelClosed(ctx.channel());
		ctx.fireChannelInactive();

		if (isDebug() && logger.isDebugEnabled())
			logger.debug("{} inactive.", ctx);
	}

	// private boolean checkHeartBeat(final ChannelHandlerContext ctx, final Object request) {
	// if (!(request instanceof String))
	// return false;
	//
	// if (KEY_HEARTBEAT.equals((String) request) || (request instanceof byte[] && ((byte[]) request).length == KEY_HEARTBEAT.length() && KEY_HEARTBEAT
	// .equals(new String((byte[]) request)))) {
	//
	// if (this.messageEncoder == null || (this.messageEncoder instanceof NoneHeadByteMsgEncoder)) {
	// ctx.write(this.messageDecoder.getHeadLengthType()
	// .toBytes(messageDecoder.getHeaderLengthSize(), messageDecoder.isHeaderLengthIncluded(), KEY_ALIVE_BODY_BYTES.length));
	// }
	//
	// if (shortConnection) {
	// if (request instanceof String)
	// ctx.writeAndFlush(KEY_ALIVE).addListener(ChannelFutureListener.CLOSE);
	// else
	// ctx.writeAndFlush(KEY_ALIVE_BODY_BYTES).addListener(ChannelFutureListener.CLOSE);
	// } else {
	// if (request instanceof String)
	// ctx.writeAndFlush(KEY_ALIVE);
	// else
	// ctx.writeAndFlush(KEY_ALIVE_BODY_BYTES);
	// }
	//
	// if (isDebug() && logger.isDebugEnabled())
	// logger.debug("{} HeartBeat alived, is short connection ? {}.", ctx, shortConnection);
	//
	// return true;
	// } else if (KEY_PING.equals((String) request) || (request instanceof byte[] && ((byte[]) request).length == KEY_PING.length() && KEY_PING
	// .equals(new String((byte[]) request)))) {
	//
	// if (isDebug() && logger.isDebugEnabled())
	// logger.debug("{} ping ok, is short connection ? {}.", ctx, shortConnection);
	//
	// return true;
	// }
	//
	// return false;
	// }

	// private static void notify100Continue(ChannelHandlerContext ctx) {
	// FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
	// ctx.write(response);
	// }

	private void errorResponse(final ChannelHandlerContext ctx, final Throwable thr) {
		byte[] message = null;
		try {
			message = thr.getMessage().getBytes(io.netty.util.CharsetUtil.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			message = thr.getMessage().getBytes();
		}

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(message));
		response.headers().set(CONTENT_TYPE, "text/plain;charset=UTF-8;");
		response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

		if (shortConnection)
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		else
			ctx.writeAndFlush(response);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object message) throws Exception {
		if (message == null) {
			if (isDebug() && logger.isDebugEnabled())
				logger.debug("{} request is null, ignore it.", ctx);
			return;
		}

		if (isDebug() && logger.isDebugEnabled())
			logger.debug("{} read request ({}){}", ctx, message == null ? "nvl" : message.getClass());

		try {
			if (serviceHandler == null) {
				throw new RuntimeException("No appServiceHandler assigned.");
			}

			if (!(message instanceof FullHttpRequest)) {
				throw new RuntimeException("Readed unsupported message, FullHttpRequest expected.");
			}

			final FullHttpRequest request = (FullHttpRequest) message;
			final HttpVersion protocalVersion = request.protocolVersion() == null ? HTTP_1_1 : request.protocolVersion();
			final String contentType = request.headers().get(CONTENT_TYPE) == null ? "text/plain" : request.headers().get(CONTENT_TYPE);
			final String contentEncoding = request.headers().get(CONTENT_ENCODING) == null ? io.netty.util.CharsetUtil.UTF_8.name() : request.headers()
			        .get(CONTENT_ENCODING);

			/**
			 * heartbeat or ping
			 */
			if (KEY_PING.equalsIgnoreCase(request.uri()) || KEY_HEARTBEAT.equalsIgnoreCase(request.uri())) {
				String content = KEY_HEARTBEAT.equalsIgnoreCase(request.uri()) ? KEY_ALIVE : "";
				FullHttpResponse response = new DefaultFullHttpResponse(protocalVersion, OK, Unpooled.wrappedBuffer(KEY_ALIVE.getBytes(contentEncoding)));
				response.headers().set(CONTENT_TYPE, contentType);
				response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

				if (shortConnection)
					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				else
					ctx.writeAndFlush(response);

				return;
			}

			getExcutorThread().execute(new Runnable() {
				public void run() {

					try {

						if (isDebug() && logger.isDebugEnabled()) {
							logger.debug("{} proccess in thread {} ", ctx, Thread.currentThread());
							logger.debug("FullHttpRequest.protocolVersion={}", request.protocolVersion());
							logger.debug("FullHttpRequest.method={}", request.method());
							logger.debug("FullHttpRequest.uri={}", request.uri());
							logger.debug("FullHttpRequest.headers={}", request.headers());
							logger.debug("FullHttpRequest.refCnt={}", request.refCnt());
							logger.debug("FullHttpRequest.content={}", request.content().toString(io.netty.util.CharsetUtil.UTF_8));
						}

						Object responseBody = serviceHandler.call(request, ctx.channel());
						if (responseBody != null) {
							if (isDebug() && logger.isDebugEnabled())
								logger.debug("{} Write to Channel with response message : ({}){}, is short connection ? {}.", ctx, responseBody
								        .getClass(), (responseBody instanceof byte[]) ? new String((byte[]) responseBody) : responseBody, shortConnection);

							byte[] responseBodyBytes = (responseBody instanceof byte[]) ? (byte[]) responseBody : ((String) responseBody)
							        .getBytes(contentEncoding);
							FullHttpResponse response = new DefaultFullHttpResponse(protocalVersion, OK, Unpooled.wrappedBuffer(responseBodyBytes));
							response.headers().set(CONTENT_TYPE, contentType);
							response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

							if (shortConnection)
								ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
							else
								ctx.writeAndFlush(response);

						} else {
							if (isDebug() && logger.isDebugEnabled()) {
								logger.debug("{} Write to Channel with none response message", ctx);
							}
						}
					} catch (Exception thr) {
						logger.error("thread proccess exception, " + thr.getMessage(), thr);
						errorResponse(ctx, thr);
					}
				}
			});
		} catch (Throwable thr) {
			errorResponse(ctx, thr);
		}
	}

	// @Override
	// public void channelReadComplete(ChannelHandlerContext ctx) throws
	// Exception {
	// ctx.fireChannelReadComplete();
	// }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.fireExceptionCaught(cause);
		logger.warn("{} server channel exceptionCaught : {}", this, ExceptionUtil.getStackTrace(cause));
		this.serviceHandler.onChannelException(ctx.channel(), cause);
		if (cause != null && !(cause instanceof ReadTimeoutException) && !(cause instanceof WriteTimeoutException)) {
			ctx.close();
			logger.debug("close the context {} for the causedException {}", ctx, cause);
		}
	}

}