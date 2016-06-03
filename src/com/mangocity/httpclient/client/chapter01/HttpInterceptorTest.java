package com.mangocity.httpclient.client.chapter01;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.google.common.base.Throwables;

public class HttpInterceptorTest {

	private static final String HTTP_PERFORMANCE_URL = "http://localhost:8081/client/perform/test.do";

	@Test
	public void testHttpReqInterceptor() {
		CloseableHttpClient httpclient = HttpClients.custom()
				.addInterceptorLast(new HttpRequestInterceptor() {
					public void process(final HttpRequest request,
							final HttpContext context) throws HttpException,
							IOException {
						System.out.println("拦截到的请求参数: "
								+ request.headerIterator());
						/*
						 * AtomicInteger count = (AtomicInteger) context
						 * .getAttribute("count"); request.addHeader("Count",
						 * Integer.toString(count.getAndIncrement()));
						 */
					}
				}).build();
		AtomicInteger count = new AtomicInteger(1);
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setAttribute("count", count);

		HttpGet httpget1 = new HttpGet(HTTP_PERFORMANCE_URL
				+ "?method=add&visit=true");
		HttpContext context = null;
		try {
			CloseableHttpResponse response1 = httpclient.execute(httpget1,
					context);
			try {
				HttpEntity entity1 = response1.getEntity();
				System.out.println(EntityUtils.toString(entity1));
			} finally {
				response1.close();
			}
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}
}
