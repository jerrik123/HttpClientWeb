package com.mangocity.httpclient.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Strings;

public class HttpClientUtils {
	
	private final static Logger LOGGER = Logger.getLogger(HttpClientUtils.class);
	
	private static PoolingHttpClientConnectionManager cm;  
    private static String EMPTY_STR = "";  
    private static String UTF_8 = "UTF-8";  
      
    private static void init(){  
        if(cm == null){  
            cm = new PoolingHttpClientConnectionManager();  
            cm.setMaxTotal(100);//整个连接池最大连接数  
            cm.setDefaultMaxPerRoute(5);//每路由最大连接数，默认值是2  
        }  
    }  
      
    /** 
     * 通过连接池获取HttpClient 
     * @return 
     */  
    private static CloseableHttpClient getHttpClient(){  
        init();  
        return HttpClients.custom().build();  
    }  
      
    /** 
     *  
     * @param url 
     * @return 
     */  
    public static String httpGetRequest(String url){  
        HttpGet httpGet = new HttpGet(url);  
        return getResult(httpGet);  
    }  
      
    public static String httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException{  
        URIBuilder ub = new URIBuilder();  
        ub.setPath(url);  
          
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);  
        ub.setParameters(pairs);  
          
        HttpGet httpGet = new HttpGet(ub.build());  
        return getResult(httpGet);  
    }  
      
    public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params) throws URISyntaxException{  
        URIBuilder ub = new URIBuilder();  
        ub.setPath(url);  
          
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);  
        ub.setParameters(pairs);  
          
        HttpGet httpGet = new HttpGet(ub.build());  
        for (Map.Entry<String, Object> param: headers.entrySet()) {  
        	if(param.getValue()!=null){
        		httpGet.addHeader(param.getKey(), param.getValue().toString());  
        	}
        }  
        return getResult(httpGet);  
    }  
      
    public static String httpPostRequest(String url){  
        HttpPost httpPost = new HttpPost(url);  
        return getResult(httpPost);  
    }  
      
    public static String httpPostRequest(String url, Map<String, Object> params) throws UnsupportedEncodingException{  
        HttpPost httpPost = new HttpPost(url);  
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);  
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));  
        return getResult(httpPost);  
    }  
      
    public static String httpPostRequest(String url, Map<String, Object> headers,   
            Map<String, Object> params) throws UnsupportedEncodingException{  
        HttpPost httpPost = new HttpPost(url);  
          
        for (Map.Entry<String, Object> param: headers.entrySet()) {  
        	if(param.getValue()!=null){
        		httpPost.addHeader(param.getKey(), param.getValue().toString());  
        	}
        }  
          
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);  
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));  
          
        return getResult(httpPost);  
    }  
      
    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params){  
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();  
        for (Map.Entry<String, Object> param: params.entrySet()) {  
        	if(param.getValue()!=null){
        		pairs.add(new BasicNameValuePair(param.getKey(), param.getValue().toString()));  
        	}
        }  
        return pairs;  
    }  
      
      
    /** 
     * 处理Http请求 
     * @param request 
     * @return 
     */  
    private static String getResult(HttpRequestBase request){  
        //CloseableHttpClient httpClient = HttpClients.createDefault();  
        CloseableHttpClient httpClient = getHttpClient();  
        CloseableHttpResponse response = null;
        try{  
            response = httpClient.execute(request);  
            //response.getStatusLine().getStatusCode();  
            
            HttpEntity entity = response.getEntity();  
            if(entity!=null){  
                //long len = entity.getContentLength();// -1 表示长度未知  
                String result = EntityUtils.toString(entity);  
                return result;  
            }  
        }catch(ClientProtocolException e){  
        	LOGGER.error(e);
        }catch(IOException e){  
        	LOGGER.error(e);
        }finally{  
        	if(response!=null){
        		try {
					response.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}  
        	}
        	if(httpClient!=null){
        	   try {
        		   httpClient.close();
        	   } catch (IOException e) {
        		   LOGGER.error(e);
        	   }
           }   
        }  
        return EMPTY_STR;  
    }  
    
    /**
     * 通过get请求获取页面标题
     * @param url 
     * @return
     */
    public static String getPageTitle(String url){
    	try {
			String result = httpGetRequest(url);
			if(Strings.isNullOrEmpty(result)){
				return null;
			}
			String regex = "<title>(.*)</title>";  
			Pattern pattern = Pattern.compile(regex);  
			Matcher matcher = pattern.matcher(result);  
			if (matcher.find()){
				return matcher.group(1);  
			}else{
				return null;  
			}
		} catch (Exception e) {
			//LOGGER.error("get page title fail,url:"+url, e);
		}  
    	return null;
    }
    
	/**
	 * 从带有参数的url中解析出参数
	 * @param url
	 * @return
	 */
	public static Map<String,String> httpUrlParserParams(String url){
		Map<String,String> paramsMap = null;
		if(!Strings.isNullOrEmpty(url) && url.contains("?")){
			String urlParamStr = url.substring(url.indexOf("?")+1);
			String[] urlParams = urlParamStr.split("&");
			if(urlParams.length == 0){
				return paramsMap;
			}
			paramsMap = new HashMap<String, String>();
			for(String urlParam:urlParams){
				String[] params = urlParam.split("=");
				if(params.length!=2){
					continue;
				}
				paramsMap.put(params[0], params[1]);
			}
		}
		return paramsMap;
	}
	
	public static void main(String[] args) {
		try {
			String result = URLDecoder.decode("bdhaiwai", "utf-8");
			result = URLDecoder.decode(result, "utf-8");
			System.out.println(result);
		} catch (UnsupportedEncodingException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	

}
