package com.pkz.bla.mershcrawler.util;

import java.util.*;


/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/13 13:44
 */
public class HeaderUtil {

	public static final List<Map<String, String>> HEADER_POOL = new ArrayList<>();

	public static Map<String, String> getRandomHeaders() {
		return HEADER_POOL.get(new Random().nextInt(HEADER_POOL.size()));
	}

	static {
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6");
		headers.put("Cache-Control", "no-cache");
		headers.put("Connection", "keep-alive");
		headers.put("Host", "www.ozon.ru");
		headers.put("Pragma", "no-cache");
		headers.put("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
		headers.put("sec-ch-ua-mobile", "?0");
		headers.put("sec-ch-ua-platform", "\"Windows\"");
		headers.put("Sec-Fetch-Dest", "document");
		headers.put("Sec-Fetch-Mode", "navigate");
		headers.put("Sec-Fetch-Site", "none");
		headers.put("Sec-Fetch-User", "?1");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
		HEADER_POOL.add(headers);
	}

	public static Map<String, String> sellerHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		headers.put("accept-encoding", "gzip, deflate, br, zstd");
		headers.put("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6");
		headers.put("cache-control", "no-cache");
		headers.put("pragma", "no-cache");
		headers.put("priority", "u=0, i");
		headers.put("sec-ch-ua", "\"Not(A:Brand\";v=\"99\", \"Microsoft Edge\";v=\"133\", \"Chromium\";v=\"133\"");
		headers.put("sec-ch-ua-mobile", "?0");
		headers.put("sec-ch-ua-platform", "\"Windows\"");
		headers.put("sec-fetch-dest", "document");
		headers.put("sec-fetch-mode", "navigate");
		headers.put("sec-fetch-site", "none");
		headers.put("sec-fetch-user", "?1");
		headers.put("service-worker-navigation-preload", "true");
		headers.put("upgrade-insecure-requests", "1");
		headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 Edg/133.0.0.0");
		return headers;
	}

}
