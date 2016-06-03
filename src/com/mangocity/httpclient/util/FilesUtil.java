package com.mangocity.httpclient.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class FilesUtil {

	public static BufferedReader newBufferedReader(InputStream in)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return br;
	}

	public static String readLines(BufferedReader br) throws IOException {
		String newLine = null;
		StringBuilder sb = new StringBuilder();
		while ((newLine = br.readLine()) != null) {
			sb.append(newLine);
			sb.append("\r\n");
		}
		try {
			return sb.toString();
		} finally {
			br.close();
		}
	}
}
