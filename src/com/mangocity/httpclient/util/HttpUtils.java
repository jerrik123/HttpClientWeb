package com.mangocity.httpclient.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * @ClassName: HttpUtils
 * @Description: (通用Http请求工具类,具备超时方法和重试机制)
 * @author Jerrik
 * @date 2016年6月03日 上午9:27:23
 */
public class HttpUtils {
	private static final Logger LOGGER = Logger.getLogger(HttpUtils.class);

	/**
	 * 发送Http Get请求
	 * 
	 * @param url
	 * @return
	 */
	@SuppressWarnings("resource")
	public static String doGetRequest(String url) {
		checkNotNull(url, "url can't be null.");
		return buildRequest(new HttpGet(url), null, null, null);
	}

	/**
	 * 发送Http Get请求(带超时) 如果millisecond为空,则不设置超时
	 * 
	 * @param url
	 * @param millisecond
	 * @return
	 */
	public static String doGetRequestWithTimeOut(String url, Integer millisecond) {
		checkNotNull(url, "url can't be null.");
		return buildRequest(new HttpGet(url), null, millisecond, null);
	}

	/**
	 * 发送Http Get请求(带超时) 如果millisecond为空,则不设置超时
	 * 
	 * @param url
	 * @param millisecond
	 * @return
	 */
	public static String doGetRequestWithTimeOutAndRetryTimes(String url,
			Integer millisecond, Integer retryTimes) {
		checkNotNull(url, "url can't be null.");
		return buildRequest(new HttpGet(url), null, millisecond, retryTimes);
	}

	/**
	 * 发送Http Post请求
	 * 
	 * @param url
	 * @param postParams
	 * @return HTTP响应返回内容
	 */
	@SuppressWarnings("unchecked")
	public static String doPostRequest(String url, Object postParams) {
		checkNotNull(url, "url can't be null.");
		return buildRequest(new HttpPost(url), postParams, null, null);
	}

	/**
	 * 发送Http Post请求(带超时) 如果millisecond为空,则不设置超时
	 * 
	 * @param url
	 * @param millisecond
	 * @return
	 */
	public static String doPostRequestWithTimeOut(String url,
			Object postParams, Integer millisecond) {
		checkNotNull(url, "url can't be null.");
		return buildRequest(new HttpPost(url), postParams, millisecond, null);
	}

	/**
	 * 发送Http Post请求(带超时) 如果millisecond为空,则不设置超时
	 * 
	 * @param url
	 * @param millisecond
	 * @return
	 */
	public static String doPostRequestWithTimeOutAndRetryTimes(String url,
			Object postParams, Integer millisecond, Integer retryTimes) {
		checkNotNull(url, "url can't be null.");
		return buildRequest(new HttpPost(url), postParams, millisecond,
				retryTimes);
	}

	/**
	 * 构建Request请求 如果请求超时,返回"null",防止空指针
	 * 
	 * @param requestBase
	 * @param millisecond
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String buildRequest(HttpRequestBase requestBase,
			Object postParams, Integer millisecond, final Integer retryTimes) {
		CloseableHttpClient client = null;
		String content = null;
		HttpRequestRetryHandler retryHandler = null;
		try {
			// set time out.
			if (null != millisecond && millisecond > 0) {
				LOGGER.info("buildRequest set timeout: " + millisecond);
				RequestConfig requestConfig = RequestConfig.custom()
						.setSocketTimeout(millisecond)
						.setConnectTimeout(millisecond).build();
				requestBase.setConfig(requestConfig);
			}

			// retryHandler
			if (null != retryTimes && retryTimes.intValue() > 0) {
				LOGGER.info("buildRequest retryTimes: " + retryTimes);
				retryHandler = new DefaultHttpRequestRetryHandler() {
					@Override
					public boolean retryRequest(IOException exception,
							int executionCount, HttpContext context) {
						if (executionCount >= retryTimes) {
							LOGGER.info("Over executionCount limit,STOP HTTP Request.");
							return false;
						}

						if (exception instanceof InterruptedIOException) {//SocketTimeoutException
							return true;
						}
						
						//set retryTimes
						return super.retryRequest(exception, retryTimes,
								context);
					}
				};
				client = HttpClients.custom().setRetryHandler(retryHandler)
						.build();
			} else {
				client = HttpClients.createDefault();
			}

			// set request params for post request.
			if (requestBase instanceof HttpPost) {// POST
				if (null != postParams) {
					if (postParams instanceof Map) {
						Map<String, String> params = (Map<String, String>) postParams;
						setPostParams(requestBase, params);
					} else if (postParams instanceof String) {
						String params = (String) postParams;
						setPostParams(requestBase, params);
					}
				}
			}
			LOGGER.info("buildRequest request method: 【"
					+ requestBase.getMethod() + "】,request params: "
					+ postParams);
			HttpResponse response = client.execute(requestBase);
			LOGGER.info("buildRequest statusLine: 【" + response.getStatusLine()
					+ "】");
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, HTTP.UTF_8);
		} catch (ClientProtocolException e) {
			LOGGER.error("requestBase ClientProtocolException.", e);
		} catch (IllegalStateException e) {
			LOGGER.error("requestBase IllegalStateException.", e);
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException
					|| e instanceof ConnectTimeoutException) {
				try {
					throw new SocketTimeoutException(
							"HTTP Request Time out. The overtime Time is: "
									+ millisecond + " milliseconds.");
				} catch (SocketTimeoutException ee) {
					LOGGER.error(ee.getMessage(), ee);
				}
			} else {
				LOGGER.error(e.getMessage(), e);
			}
		} finally {
			if (requestBase != null && !requestBase.isAborted()) {
				requestBase.abort();
			}
		}
		LOGGER.info("buildRequest responseText: " + content);
		return String.valueOf(content);
	}

	// 设置UrlEncodedFormEntity请求Entity
	private static void setPostParams(HttpRequestBase request,
			Map<String, String> postParams) throws UnsupportedEncodingException {
		List<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
		for (Map.Entry<String, String> entry : postParams.entrySet()) {
			postData.add(new BasicNameValuePair(entry.getKey(), entry
					.getValue()));
		}
		AbstractHttpEntity entity = new UrlEncodedFormEntity(postData,
				HTTP.UTF_8);
		((HttpPost) request).setEntity(entity);
	}

	// 设置StringEntity请求Entity
	private static void setPostParams(HttpRequestBase request, String postParams)
			throws UnsupportedEncodingException {
		AbstractHttpEntity entity = new StringEntity(postParams, HTTP.UTF_8);
		((HttpPost) request).setEntity(entity);
	}
}
