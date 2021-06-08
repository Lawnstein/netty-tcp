/**
 * netty-tcp. <br>
 * Copyright (C) 1999-2017, All rights reserved. <br>
 * <br>
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0. <br>
 */

package io.netty.http.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单Http客户端使用..
 * 
 * @author lawnstein.chan
 * @version $Revision:$
 */
public class HttpClient {
	protected final static Logger logger = LoggerFactory.getLogger(HttpClient.class);

	protected final static int CONNECT_TIMEOUT_MILLIS = 30000;// 设置连接主机服务器的超时时间：30秒
	protected final static int READ_TIMEOUT_MILLIS = 300000;// 设置读取远程返回的数据时间：5分

	private static void disconnect(HttpURLConnection connection) {
		if (connection == null) {
			return;
		}
		try {
			if (connection.getInputStream() != null) {
				connection.getInputStream().close();
			}
		} catch (IOException e) {
		}

		try {
			if (connection.getOutputStream() != null) {
				connection.getOutputStream().close();
			}
		} catch (IOException e) {
		}

		connection.disconnect();
	}

	private static List<String> readResponse(InputStream is) {
		if (is == null) {
			return null;
		}

		List<String> outputs = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			outputs = new ArrayList<String>();
			String line = null;
			while ((line = br.readLine()) != null) {
				outputs.add(line);
			}
			return outputs;
		} catch (Throwable thr) {
			throw new RuntimeException("Read http response content failed, " + thr.getMessage(), thr);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static String doGet(String httpUrl) {
		return doGet(httpUrl, null, null);
	}

	public static String doGet(String httpUrl, Map<String, String> headersMap, Map<String, Object> paramsMap) {
		httpUrl = httpUrl.startsWith("http://") ? httpUrl : "http://" + httpUrl;
		String paramUrl = null;
		if (paramsMap != null && paramsMap.size() > 0) {
			paramUrl = "";
			for (Entry<String, Object> en : paramsMap.entrySet()) {
				if (paramUrl.length() > 0) {
					paramUrl += "&";
				}
				paramUrl += en.getKey() + "=" + en.getValue().toString();
			}
		}
		if (paramUrl != null && paramUrl.length() > 0) {
			try {
				paramUrl = URLEncoder.encode(paramUrl, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("URLEncoding the paramUrl with UTF-8 failed, " + e.getMessage(), e);
			}
			httpUrl += "?" + paramUrl;
		}

		if (headersMap == null) {
			headersMap = new LinkedHashMap<>();
		}
		if (!headersMap.containsKey("Content-Type")) {
			headersMap.put("Content-Type", "application/x-www-form-urlencoded");
		}
		if (!headersMap.containsKey("Connection")) {
			headersMap.put("Connection", "Keep-Alive");
		}
		if (!headersMap.containsKey("Accept")) {
			headersMap.put("Accept", "text/plain");
		}

		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			URL url = new URL(httpUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);// 设置连接主机服务器的超时时间：10秒
			connection.setReadTimeout(READ_TIMEOUT_MILLIS); // 设置读取远程返回的数据时间：5分
			for (Entry<String, String> en : headersMap.entrySet()) {
				connection.setRequestProperty(en.getKey(), en.getValue());
			}
			connection.setDoInput(true);
			connection.connect();
			logger.debug("doGet {}, responseCode={}", httpUrl, connection.getResponseCode());

			is = connection.getInputStream();
			List<String> responseLines = readResponse(is);
			if (connection.getResponseCode() != 200) {
				String detailMessage = "";
				if (responseLines != null && responseLines.size() > 0) {
					detailMessage = responseLines.get(0);
				}
				throw new RuntimeException("doGet " + httpUrl + " failed, response code " + connection.getResponseCode() + ", " + detailMessage);
			}

			StringBuffer sbf = new StringBuffer();
			for (String line : responseLines) {
				if (sbf.length() > 0) {
					sbf.append("\n\r");
				}
				sbf.append(line);
			}
			return sbf.toString();
		} catch (Throwable thr) {
			throw new RuntimeException("doGet " + httpUrl + " failed, " + thr.getMessage(), thr);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}

			disconnect(connection);
		}
	}

	public static String doPost(String httpUrl, String contentValue) {
		return doPost(httpUrl, (Map<String, String>) null, contentValue);
	}

	public static String doPost(String httpUrl, String contentType, String contentValue) {
		Map<String, String> headersMap = new LinkedHashMap<>();
		if (contentType != null && contentType.length() > 0) {
			headersMap.put("Content-Type", contentType);
		}

		return doPost(httpUrl, headersMap, contentValue);
	}

	public static String doPost(String httpUrl, Map<String, String> headersMap, String contentValue) {
		httpUrl = httpUrl.startsWith("http://") ? httpUrl : "http://" + httpUrl;

		if (headersMap == null) {
			headersMap = new LinkedHashMap<>();
		}
		if (!headersMap.containsKey("Content-Type")) {
			headersMap.put("Content-Type", "text/plain");
		}
		if (!headersMap.containsKey("Connection")) {
			headersMap.put("Connection", "Keep-Alive");
		}
		if (!headersMap.containsKey("Accept")) {
			headersMap.put("Accept", "*/*");
		}

		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			URL url = new URL(httpUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);// 设置连接主机服务器的超时时间：10秒
			connection.setReadTimeout(READ_TIMEOUT_MILLIS); // 设置读取远程返回的数据时间：5分
			for (Entry<String, String> en : headersMap.entrySet()) {
				connection.setRequestProperty(en.getKey(), en.getValue());
			}
			connection.setDoOutput(true);
			connection.setDoInput(true);
			if (contentValue != null) {
				connection.getOutputStream().write(contentValue.getBytes("UTF-8"));
			}

			connection.connect();
			is = connection.getInputStream();
			List<String> responseLines = readResponse(is);
			if (connection.getResponseCode() != 200) {
				String detailMessage = "";
				if (responseLines != null && responseLines.size() > 0) {
					detailMessage = responseLines.get(0);
				}
				throw new RuntimeException("doPost " + httpUrl + " failed, response code " + connection.getResponseCode() + ", " + detailMessage);
			}

			StringBuffer sbf = new StringBuffer();
			for (String line : responseLines) {
				if (sbf.length() > 0) {
					sbf.append("\n\r");
				}
				sbf.append(line);
			}
			return sbf.toString();
		} catch (Throwable thr) {
			throw new RuntimeException("doPost " + httpUrl + " failed, " + thr.getMessage(), thr);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}

			disconnect(connection);
		}
	}
}
