package com.pkz.bla.mershcrawler.core;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.pkz.bla.mershcrawler.config.ProxyConfig;
import com.pkz.bla.mershcrawler.dto.ip.ProxyIp;
import com.pkz.bla.mershcrawler.exception.MershCrawlerException;
import com.pkz.bla.mershcrawler.robot.RobotChecker;
import com.pkz.bla.mershcrawler.util.CookieUtil;
import com.pkz.bla.mershcrawler.util.HeaderUtil;
import com.pkz.bla.mershcrawler.util.JsoupUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/13 21:10
 */
@Slf4j
@Data
public class CrawlerInstance {

	private Long sku;
	private CrawlerRequest request;
	private CrawlerResponse response;
	private Connection connection = null;
	private boolean updateCookies = false;

	public static CrawlerInstance create(Long sku) {
		CrawlerInstance crawlerInstance = new CrawlerInstance(sku);
		crawlerInstance.setRequest(new CrawlerRequest());
		crawlerInstance.setResponse(new CrawlerResponse());
		return crawlerInstance;
	}

	public CrawlerInstance url(String url) {
		this.request.setUrl(url);
		return this;
	}

	public CrawlerInstance updateCookies(boolean updateCookies) {
		this.updateCookies = updateCookies;
		return this;
	}

	public CrawlerInstance cookies(Map<String, String> cookies) {
		this.request.setCookies(cookies);
		return this;
	}

	public CrawlerInstance headers(Map<String, String> headers) {
		this.request.setHeaders(headers);
		return this;
	}

	public CrawlerInstance followRedirects(boolean followRedirects) {
		this.request.followRedirects = followRedirects;
		return this;
	}

	public CrawlerInstance proxy(ProxyIp proxy) {
		ProxyConfig proxyConfig = SpringUtil.getBean(ProxyConfig.class);
		if (CharSequenceUtil.isAllNotBlank(proxyConfig.getAuthKey(), proxyConfig.getAuthPassword())) {
			this.request.setProxyAuthKey(proxyConfig.getAuthKey());
			this.request.setProxyPassword(proxyConfig.getAuthPassword());
		}
		this.request.setProxy(proxy);
		return this;
	}


	private CrawlerInstance(Long sku) {
		this.sku = sku;
	}


	@Data
	public static class CrawlerRequest {

		private String url;

		private Map<String, String> cookies;

		private Map<String, String> headers;

		private int timeout = 10000;

		private boolean ignoreContentType = true;

		private boolean followRedirects = true;

		private ProxyIp proxy = null;

		private String proxyAuthKey;

		private String proxyPassword;

//		private SSLSocketFactory sslSocketFactory = null;

	}


	@Data
	public static class CrawlerResponse {

		private Connection.Response jsoupResponse;

		private byte[] byteBody;

		private int httpStatus;

		private String jsonBody;

		private Document documentBody;

		public String jsonBody() throws IOException {
			this.jsonBody = JsoupUtil.parseResponseJson(jsoupResponse);
			return this.jsonBody;
		}

		public Document documentBody() throws IOException {
			this.documentBody = JsoupUtil.parseResponseHtml(jsoupResponse);
			return this.documentBody;
		}

		public String stringBody() {
			return this.jsoupResponse.body();
		}

	}

	public CrawlerInstance build() {
		try {
			// 使用 URI 来正确处理 URL
			URI uri = new URI(this.request.url);
			String processedUrl = uri.toASCIIString();

			this.connection = Jsoup.connect(processedUrl)
					.method(Connection.Method.GET)
					.timeout(this.request.timeout)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.followRedirects(this.request.followRedirects)
					.maxBodySize(0);


			if (Objects.isNull(this.request.cookies)) {
				this.request.cookies = CookieUtil.getRandomCookies();
				if (Objects.isNull(this.request.cookies)) {
					log.error("=== Cookie池中存在0个可用Cookie，请及时更新Cookie ===");
					throw new MershCrawlerException("Cookie池中Cookie已耗尽，无法执行，请在resources/init/ozon_cookie.txt中添加Cookie");
				}
			}
			connection.cookies(this.request.cookies);
			if (Objects.isNull(this.request.headers)) {
				this.request.headers = HeaderUtil.getRandomHeaders();
			}
			connection.headers(this.request.headers);
			if (Objects.nonNull(this.request.proxy)) {

				System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "false");
				System.setProperty("jdk.http.auth.proxying.disabledSchemes", "false");
				Authenticator.setDefault(new Authenticator() {
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(request.getProxyAuthKey(), request.getProxyPassword().toCharArray());
					}
				});
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(request.getProxy().getIp(), request.getProxy().getPort()));
				log.info("=== 使用代理: {}:{}", request.getProxy().getIp(), request.getProxy().getPort());
				connection.proxy(proxy);
			}
		} catch (Exception e) {
			log.error("==== URL处理失败: {}", e.getMessage());
			throw new MershCrawlerException(CharSequenceUtil.format("URL处理失败:{}", e.getMessage()));
		}
		return this;
	}

	public CrawlerResponse execute() {
		if (Objects.isNull(connection)) {
			this.build();
		}
		try {
			Connection.Response execute = connection.execute();
			this.response.byteBody = execute.bodyAsBytes();
			this.response.jsoupResponse = execute;
			this.response.httpStatus = execute.statusCode();

			if (response.httpStatus == 403) {
				log.warn("=== Cookies失效触发检测机制，使用RobotChecker ===");
				RobotChecker checker = new RobotChecker();
				try {
					Map<String, String> newCookies = checker.handleRobotCheck(this.request.getUrl());
					if (newCookies != null && !newCookies.isEmpty()) {
						log.info("=== 获取到新的Cookies，数量: {}", newCookies.size());
						// 移除旧Cookie获取
						CookieUtil.remove(this.getRequest().getCookies());
						CookieUtil.putCookies(newCookies);
						// 更新请求的Cookies
						this.request.setCookies(newCookies);
						// 清除旧的连接和响应
						this.connection = null;
						this.response = new CrawlerResponse();
						// 重新执行请求
						return execute();
					} else {
						log.error("==== 未获取到新的Cookies");
					}
				} finally {
					checker.close();
				}
			}

			updateResponseCookies(execute);
		} catch (IOException e) {
			throw new MershCrawlerException(CharSequenceUtil.format("IO异常:{}", e.getMessage()));
		}
		return this.response;
	}

	// todo 更新Cookie需要优化，使用 Post https://xapi.ozon.ru/dlte/multi 接口获取Set-Cookie头
	private void updateResponseCookies(Connection.Response response) {

		if (!updateCookies) {
			return;
		}
		String updateCookieKey = "";
		List<String> headers = response.headers("Set-Cookie");
		for (String header : headers) {
			String[] cookieParams = header.split(";");
			for (String cookieParam : cookieParams) {
				String[] split = cookieParam.split("=");
				if (request.getCookies().containsKey(split[0])) {
					String cookieValue = request.getCookies().get(split[0]);
					if (!cookieValue.equals(split[1])) {
						request.getCookies().put(split[0], split[1]);
						updateCookieKey = CharSequenceUtil.format("{} | {}", updateCookieKey, split[0]);
					}

				}
			}
		}
		log.info("=== 本次请求更新Cookie: {} ===", updateCookieKey);

	}


}
