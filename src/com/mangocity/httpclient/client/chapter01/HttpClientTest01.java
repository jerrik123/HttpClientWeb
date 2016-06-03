package com.mangocity.httpclient.client.chapter01;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.mangocity.httpclient.util.FilesUtil;

public class HttpClientTest01 {

	private static final String HTTP_PERFORMANCE_URL = "http://localhost:8081/client/perform/test.do";

	@Test
	public void testHttpClient01() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet("http://localhost/");
		CloseableHttpResponse response = httpclient.execute(httpget);
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				int byteOne = instream.read();
				int byteTwo = instream.read();
			}
		} finally {
			response.close();
		}
	}

	@Test
	public void testResponse() {
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, "OK");
		System.out.println(response.getProtocolVersion());
		System.out.println(response.getStatusLine().getStatusCode());
		System.out.println(response.getStatusLine().getReasonPhrase());
		System.out.println(response.getStatusLine().toString());
	}

	@Test
	public void testHeader() {
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, "OK");
		response.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
		response.addHeader("Set-Cookie",
				"c2=b; path=\"/\", c3=c; domain=\"localhost\"");
		Header h1 = response.getFirstHeader("Set-Cookie");
		System.out.println(h1);
		Header h2 = response.getLastHeader("Set-Cookie");
		System.out.println(h2);
		Header[] hs = response.getHeaders("Set-Cookie");
		System.out.println(hs.length);
	}

	@Test
	public void testHeaderIterator() {
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, "OK");
		response.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
		response.addHeader("Set-Cookie",
				"c2=b; path=\"/\", c3=c; domain=\"localhost\"");
		HeaderIterator it = response.headerIterator("Set-Cookie");
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}

	@Test
	public void testHttpClient02() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(HTTP_PERFORMANCE_URL);
		CloseableHttpResponse response = httpclient.execute(httpget);
		try {
			HttpEntity entity = response.getEntity();
			// entity.writeTo(null);推荐使用该种方式,而不是EentityUtils

			/**
			 * 在一些情况下可能会不止一次的读取实体。此时实体内容必须以某种方式在内存或磁盘上被缓冲起来。
			 * 最简单的方法是通过使用BufferedHttpEntity类来包装源实体完成。
			 * 这会引起源实体内容被读取到内存的缓冲区中。在其它所有方式中，实体包装器将会得到源实体。
			 */
			if (entity != null) {
				entity = new BufferedHttpEntity(entity);
			}
			if (entity != null) {
				long len = entity.getContentLength();
				if (len != -1 && len < 2048) {
					System.out.println(EntityUtils.toString(entity));
				} else {
				}
			}
		} finally {
			response.close();
		}
	}

	@Test
	public void testSendEntityContent() throws ClientProtocolException,
			IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(HTTP_PERFORMANCE_URL);
		File file = new File("somefile.txt");
		FileEntity reqEntity = new FileEntity(file, ContentType.create(
				"text/plain", "UTF-8"));
		httppost.setEntity(reqEntity);
		CloseableHttpResponse response = httpclient.execute(httppost);
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				long len = entity.getContentLength();
				if (len != -1 && len < 2048) {
					System.out.println(EntityUtils.toString(entity));
				} else {
				}
			}
		} finally {
			response.close();
		}
	}

	// 不设置超时
	@Test
	public void testNoTimeOut() {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget1 = new HttpGet(HTTP_PERFORMANCE_URL
				+ "?method=add&visit=true");
		HttpContext context = null;
		try {
			CloseableHttpResponse response1 = httpclient.execute(httpget1);
			try {
				HttpEntity entity1 = response1.getEntity();
				System.out.println(EntityUtils.toString(entity1));
			} finally {
				response1.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("testNoTimeOut SUCCESS....");
	}

	// 设置超时
	@Test
	public void testTimeOut() {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(1000).setConnectTimeout(1000).build();
		HttpGet httpget1 = new HttpGet(HTTP_PERFORMANCE_URL
				+ "?method=add&visit=true");
		httpget1.setConfig(requestConfig);
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
			try {
				Throwables.propagateIfInstanceOf(e,
						SocketTimeoutException.class);
			} catch (SocketTimeoutException e1) {
				System.out.println("请求超时...");
			}// 抛出一个SocketTimeOutException
		}
	}

	@Test
	public void testFileEntity() {
		File file = new File("somefile.txt");
		FileEntity entity = new FileEntity(file, ContentType.create(
				"text/plain", "UTF-8"));
		HttpPost httppost = new HttpPost("http://localhost/action.do");
		httppost.setEntity(entity);
	}

	// HTML表单
	// UrlEncodedFormEntity实例将会使用URL编码来编码参数，生成如下的内容： param1=value1&param2=value2
	@Test
	public void testHtmlForm() throws IOException {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("param1", "value1"));
		formparams.add(new BasicNameValuePair("param2", "value2"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
				Consts.UTF_8);// 类似UrlEncode
		System.out.println("entity: "
				+ FilesUtil.readLines(FilesUtil.newBufferedReader(entity
						.getContent())));// param1=value1&param2=value2
		HttpPost httppost = new HttpPost("http://localhost/handler.do");
		httppost.setEntity(entity);
	}

	//响应控制器,自动释放连接到池里面,不用管异常
	@Test
	public void testResponseController() throws ClientProtocolException,
			IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet("http://localhost/json");
		ResponseHandler<Object> rh = new ResponseHandler<Object>() {
			@Override
			public Object handleResponse(final HttpResponse response)
					throws IOException {
				StatusLine statusLine = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getReasonPhrase());
				}
				if (entity == null) {
					throw new ClientProtocolException(
							"Response contains no content");
				}
				// Gson gson = new GsonBuilder().create();
				ContentType contentType = ContentType.getOrDefault(entity);
				Charset charset = contentType.getCharset();
				Reader reader = new InputStreamReader(entity.getContent(),
						charset);
				// return gson.fromJson(reader, Object.class);
				return null;
			}
		};
		Object myjson = httpclient.execute(httpget, rh);
	}

}
