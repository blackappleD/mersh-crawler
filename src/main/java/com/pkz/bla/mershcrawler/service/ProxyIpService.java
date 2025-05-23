package com.pkz.bla.mershcrawler.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pkz.bla.mershcrawler.config.ProxyConfig;
import com.pkz.bla.mershcrawler.dto.ip.ProxyIp;
import com.pkz.bla.mershcrawler.exception.MershCrawlerException;
import com.pkz.bla.mershcrawler.util.JsonUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;

/**
 * @author chentong
 * @version 1.0
 * @description:
 * @date 2025/2/13 10:57
 */
@Slf4j
@Service
public class ProxyIpService {

	@Resource
	private ProxyConfig proxyConfig;

	private static final String PROXY_API_URL = "http://127.0.0.1:8080";

	public ProxyIp getRandomProxy() {
		try {
			String url = CharSequenceUtil.format("{}/get?type=HTTPS&count=10&anonymity=高匿", PROXY_API_URL);
			String response = HttpUtil.get(url);
			List<ProxyIp> proxyList = JsonUtil.fromJson(response, new TypeReference<>() {
			});
			if (proxyList == null || proxyList.isEmpty()) {
				log.warn("未获取到可用的代理IP");
				return null;
			}
			return RandomUtil.randomEle(proxyList);
		} catch (Exception e) {
			log.error("获取代理IP失败: {}", e.getMessage());
			return null;
		}
	}

	public ProxyIp getRandomProxy1() {
		String url = CharSequenceUtil.format("https://share.proxy.qg.net/get?key={}&pwd={}&num=1&area=&isp=0&format=json&distinct=true", proxyConfig.getAuthKey(), proxyConfig.getAuthPassword());
		String response = HttpUtil.get(url);
		ProxyResponse proxyResponse = JsonUtil.fromJson(response, ProxyResponse.class);
		List<ProxyData> proxyDataList = proxyResponse.getData();
		if (Objects.isNull(proxyDataList) || proxyDataList.isEmpty()) {

			throw new MershCrawlerException("代理获取失败");
		}
		ProxyData proxyData = RandomUtil.randomEle(proxyDataList);
		log.info("=== 获取带代理ip:{}", proxyData);
		String[] split = proxyData.getServer().split(":");
		ProxyIp proxyIp = new ProxyIp();
		proxyIp.setIp(split[0].trim());
		proxyIp.setPort(Integer.parseInt(split[1].trim()));
		return proxyIp;
	}

	@Data
	public static class ProxyResponse {
		public String code;
		public List<ProxyData> data;

		@JsonProperty("request_id")
		public String requestId;
	}

	@Data
	public static class ProxyData {
		@JsonProperty("proxy_ip")
		public String proxyIp;

		public String server;

		@JsonProperty("area_code")
		public Integer areaCode;

		public String area;

		public String isp;

		public String deadline;
	}

	public boolean validateProxy(ProxyIp proxyIp) {
		try {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp.getIp(), proxyIp.getPort()));

			String testUrl = "https://www.ozon.ru/";
			String response = HttpUtil.createGet(testUrl)
					.setProxy(proxy)
					.setFollowRedirects(true)
					.timeout(10000)
					.execute()
					.body();
			return response != null && !response.isEmpty();

		} catch (Exception e) {
			log.warn("代理IP {}:{} 验证失败: {}", proxyIp.getIp(), proxyIp.getPort(), e.getMessage());
			return false;
		}
	}
}
