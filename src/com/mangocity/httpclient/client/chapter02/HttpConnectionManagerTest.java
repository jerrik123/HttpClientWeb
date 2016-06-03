package com.mangocity.httpclient.client.chapter02;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

public class HttpConnectionManagerTest {

	@Test
	public void testManagerConnection() throws IOException,
			InterruptedException, ExecutionException {
		HttpClientContext context = HttpClientContext.create();
		HttpClientConnectionManager clientConnMgr = new BasicHttpClientConnectionManager();
		HttpRoute route = new HttpRoute(new HttpHost("localhost", 8081));
		// Request new connection. This can be a long process
		ConnectionRequest connRequest = clientConnMgr.requestConnection(route,
				null);
		// Wait for connection up to 10 sec
		HttpClientConnection clientConn = connRequest.get(10, TimeUnit.SECONDS);
		try {
			// If not open
			if (!clientConn.isOpen()) {
				// establish connection based on its route info
				clientConnMgr.connect(clientConn, route, 1000, context);
				// and mark it as route complete
				clientConnMgr.routeComplete(clientConn, route, context);
			}
			// Do useful things with the connection.
		} finally {
			clientConnMgr.releaseConnection(clientConn, null, 1,
					TimeUnit.MINUTES);
		}
	}

	// 连接池管理器
	@Test
	public void testPoollingHttpClientConnectionManger() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);
		// Increase max connections for localhost:80 to 50
		HttpHost localhost = new HttpHost("locahost", 80);
		cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(cm).build();
	}

	static class GetThread extends Thread {
		private final CloseableHttpClient httpClient;
		private final HttpContext context;
		private final HttpGet httpget;

		public GetThread(CloseableHttpClient httpClient, HttpGet httpget) {
			this.httpClient = httpClient;
			this.context = HttpClientContext.create();
			this.httpget = httpget;
		}

		@Override
		public void run() {
			try {
				CloseableHttpResponse response = httpClient.execute(httpget,
						context);
				try {
					HttpEntity entity = response.getEntity();
				} finally {
					response.close();
				}
			} catch (ClientProtocolException ex) {
				// Handle protocol errors
			} catch (IOException ex) {
				// Handle I/O errors
			}
		}
	}

	//多线程请求
	@Test
	public void testMultiThreadRequest() throws InterruptedException {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(cm).build();
		// URIs to perform GETs on
		String[] urisToGet = { "http://www.domain1.com/",
				"http://www.domain2.com/", "http://www.domain3.com/",
				"http://www.domain4.com/" };
		// create a thread for each URI
		GetThread[] threads = new GetThread[urisToGet.length];
		for (int i = 0; i < threads.length; i++) {
			HttpGet httpget = new HttpGet(urisToGet[i]);
			threads[i] = new GetThread(httpClient, httpget);
		}
		// start the threads
		for (int j = 0; j < threads.length; j++) {
			threads[j].start();
		}
		// join the threads
		for (int j = 0; j < threads.length; j++) {
			threads[j].join();
		}
	}
}
