package com.mangocity.httpclient.controller;

import static com.mangocity.httpclient.util.FilesUtil.newBufferedReader;
import static com.mangocity.httpclient.util.FilesUtil.readLines;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

/**
 * HTTPClient控制类
 * 
 * @author mbr.yangjie
 */
public class PerformanceController extends HttpServlet {

	private static final long serialVersionUID = -2521926554014492810L;

	private AtomicLong counter = new AtomicLong(1);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("PerformanceController begin()...第【"
				+ counter.getAndIncrement() + "】次访问");
		System.out.println("post param: " + req.getParameterMap());
		String reqJSON = extractFromRequest(req);
		System.out.println("reqJSON: " + reqJSON);
		
		//Sleep会导致SocketTimeOutException
		/*try {
			TimeUnit.SECONDS.sleep(6);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		/*****耗时操作*****/
		long start = System.currentTimeMillis();
		/*for(long i=0;i<9999999999L;i++){
			
		}*/
		
		try {
			TimeUnit.SECONDS.sleep(1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		long  end = System.currentTimeMillis();
		System.out.println("缓存完毕...totalTime: " + (end-start)/1000.0);
		
		/*****抛出异常*****/
		//throw new IOException("抛出自定义异常...");
		
		PrintWriter pw = resp.getWriter();
		String responseText = buildResponseText(req, reqJSON);
		pw.write(responseText);
		pw.flush();
		pw.close();
	}

	private String buildResponseText(HttpServletRequest req, String reqJSON) {
		Map<String, Object> resultMap = Maps.newHashMap();
		Map<String, Object> bodyMap = Maps.newHashMap();
		resultMap.put("resultCode", "00000");
		bodyMap.put("reqJSON", reqJSON);
		bodyMap.put("Post Param: ", req.getParameterMap());
		resultMap.put("bodyMap", bodyMap);
		resultMap.put("resultMsg", "SUCCESS");
		return JSON.toJSONString(resultMap);
	}

	private String extractFromRequest(HttpServletRequest req)
			throws IOException {
		String jsonStr = readLines(newBufferedReader(req.getInputStream()));
		return jsonStr;
	}

}
