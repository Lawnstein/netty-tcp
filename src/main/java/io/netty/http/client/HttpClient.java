/*
 * Copyright 2005-2021 Client Service International, Inc. All rights reserved. <br> CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.<br> <br>
 * project: netty-tcp <br> create: 2021年4月15日 上午11:57:56 <br> vc: $Id: $
 */

package io.netty.http.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		return doGet(httpUrl, null);
	}

	public static String doGet(String httpUrl, Map<String, Object> paramMap) {
		httpUrl = httpUrl.startsWith("http://") ? httpUrl : "http://" + httpUrl;

		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			URL url = new URL(httpUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);// 设置连接主机服务器的超时时间：10秒
			connection.setReadTimeout(READ_TIMEOUT_MILLIS); // 设置读取远程返回的数据时间：2分
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	// public static String doGet(String httpurl) {
	// HttpURLConnection connection = null;
	// InputStream is = null;
	// BufferedReader br = null;
	// String result = null;// 返回结果字符串
	// try {
	// // 创建远程url连接对象
	// URL url = new URL(httpurl);
	// // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
	// connection = (HttpURLConnection) url.openConnection();
	// // 设置连接方式：get
	// connection.setRequestMethod("GET");
	// // 设置连接主机服务器的超时时间：15000毫秒
	// connection.setConnectTimeout(15000);
	// // 设置读取远程返回的数据时间：60000毫秒
	// connection.setReadTimeout(60000);
	// connection.setRequestProperty("accept", "*/*");
	// connection.setRequestProperty("connection", "Keep-Alive");
	// // connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"); // 建立实际的连接
	//
	// // 发送请求
	// connection.connect();
	//
	// logger.debug("doGet getResponseCode={}", connection.getResponseCode());
	// // 通过connection连接，获取输入流
	// // if (connection.getResponseCode() == 200) {
	// is = connection.getInputStream();
	// // 封装输入流is，并指定字符集
	// br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	// // 存放数据
	// StringBuffer sbf = new StringBuffer();
	// String temp = null;
	// while ((temp = br.readLine()) != null) {
	// sbf.append(temp);
	// sbf.append("\r\n");
	// }
	// result = sbf.toString();
	// // }
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// // 关闭资源
	// if (null != br) {
	// try {
	// br.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// if (null != is) {
	// try {
	// is.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// connection.disconnect();// 关闭远程连接
	// }
	//
	// return result;
	// }

	public static String doPost(String httpUrl, String contentValue) {
		return doPost(httpUrl, null, contentValue);
	}

	public static String doPost(String httpUrl, String contentType, String contentValue) {
		httpUrl = httpUrl.startsWith("http://") ? httpUrl : "http://" + httpUrl;
		contentType = contentType == null ? "text/plain" : contentType;

		HttpURLConnection connection = null;
		InputStream is = null;
		try {
			URL url = new URL(httpUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);// 设置连接主机服务器的超时时间：10秒
			connection.setReadTimeout(READ_TIMEOUT_MILLIS); // 设置读取远程返回的数据时间：2分
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", contentType);// "application/x-www-form-urlencoded");
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
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	// public static String doPost(String httpUrl, String param) {
	//
	// HttpURLConnection connection = null;
	// InputStream is = null;
	// OutputStream os = null;
	// BufferedReader br = null;
	// String result = null;
	// try {
	// URL url = new URL(httpUrl);
	// // 通过远程url连接对象打开连接
	// connection = (HttpURLConnection) url.openConnection();
	// // 设置连接请求方式
	// connection.setRequestMethod("POST");
	// // 设置连接主机服务器超时时间：15000毫秒
	// connection.setConnectTimeout(15000);
	// // 设置读取主机服务器返回数据超时时间：60000毫秒
	// connection.setReadTimeout(60000);
	//
	// // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
	// connection.setDoOutput(true);
	// // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
	// connection.setDoInput(true);
	// // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
	// connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	// // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
	// connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
	// // 通过连接对象获取一个输出流
	// os = connection.getOutputStream();
	// // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
	// os.write(param.getBytes());
	//
	// logger.debug("doGet getResponseCode={}", connection.getResponseCode());
	// // 通过连接对象获取一个输入流，向远程读取
	// if (connection.getResponseCode() == 200) {
	//
	// is = connection.getInputStream();
	// // 对输入流对象进行包装:charset根据工作项目组的要求来设置
	// br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	//
	// StringBuffer sbf = new StringBuffer();
	// String temp = null;
	// // 循环遍历一行一行读取数据
	// while ((temp = br.readLine()) != null) {
	// sbf.append(temp);
	// sbf.append("\r\n");
	// }
	// result = sbf.toString();
	// }
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// // 关闭资源
	// if (null != br) {
	// try {
	// br.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// if (null != os) {
	// try {
	// os.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// if (null != is) {
	// try {
	// is.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// // 断开与远程地址url的连接
	// connection.disconnect();
	// }
	// return result;
	// }
}
