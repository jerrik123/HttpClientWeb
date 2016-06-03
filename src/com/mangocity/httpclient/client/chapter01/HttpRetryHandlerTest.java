package com.mangocity.httpclient.client.chapter01;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.google.common.base.Throwables;

/**
 * 请求重试处理器
 * @author mbr.yangjie
 */
public class HttpRetryHandlerTest {
	private static final String HTTP_PERFORMANCE_URL = "http://localhost:8081/client/perform/test.do";

	// 请求重试
	@Test
	public void testHttpRetryHandler() throws IOException {
		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception,
					int executionCount, HttpContext context) {
				System.out.println("retryRequest begin..." + exception + " ,executionCount: " + executionCount);
				if (executionCount >= 5) {//超过5次,就结束调用
					System.out.println("over executionCount limit...");
					return false;
				}
				/**
				 * SocketTimeOutException 是InterruptedIOException的子类型
				 * 如果HTTP服务响应慢,HTTPClient设置了超时,将会抛出SocketTimeOutException这个异常
				 */
				if (exception instanceof InterruptedIOException) {
					System.out.println("InterruptedIOException...");
					return true;//if return false,will not retry.
				}
				if (exception instanceof UnknownHostException) {
					System.out.println("UnknownHostException...");
					return false;
				}
				if (exception instanceof ConnectTimeoutException) {
					System.out.println("ConnectTimeoutException...");
					return false;
				}
				if (exception instanceof SSLException) {
					System.out.println("SSLException...");
					return false;
				}
				if (exception instanceof SocketTimeoutException) {
					System.out.println("SocketTimeoutException...");
					return false;
				}
				System.out.println("after exception...");
				HttpClientContext clientContext = HttpClientContext
						.adapt(context);
				HttpRequest request = clientContext.getRequest();
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					return true;
				}
				return false;
			}
		};
		// 设置重试机制(如果不设置自动重试,默认会使用DefaultHttpRequestRetryHandler,重试3次。SocketTimeoutException不会有重试机制)
		CloseableHttpClient httpclient = HttpClients.custom()
				.setRetryHandler(myRetryHandler).build();
		
		// 设置请求超时 1秒
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(1000).setConnectTimeout(1000).build();

		HttpGet httpget1 = new HttpGet(HTTP_PERFORMANCE_URL
				+ "?method=add&visit=true");
		httpget1.setConfig(requestConfig);

		HttpContext context = null;
		CloseableHttpResponse response1 = httpclient.execute(httpget1, context);
		try {
			HttpEntity entity1 = response1.getEntity();
			System.out.println(EntityUtils.toString(entity1));
		} finally {
			response1.close();
		}
	}
	
	@Test
	public void testOk(){
		System.out.println(Integer.MAX_VALUE);
		System.out.println(Long.MAX_VALUE);
	}
}
