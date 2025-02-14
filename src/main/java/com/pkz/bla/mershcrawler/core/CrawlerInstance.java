package com.pkz.bla.mershcrawler.core;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.pkz.bla.mershcrawler.config.ProxyConfig;
import com.pkz.bla.mershcrawler.dto.ip.ProxyIp;
import com.pkz.bla.mershcrawler.util.CookieUtil;
import com.pkz.bla.mershcrawler.util.HeaderUtil;
import com.pkz.bla.mershcrawler.util.JsoupUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.pkz.bla.mershcrawler.util.CookieUtil.COOKIE_POOL;

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
					.followRedirects(this.request.followRedirects)
					.maxBodySize(0);


			if (Objects.isNull(this.request.cookies)) {
				this.request.cookies = CookieUtil.getRandomCookies();
				if (Objects.isNull(this.request.cookies)) {
					log.error("=== Cookie池中存在0个可用Cookie，请及时更新Cookie ===");
					throw new RuntimeException("Cookie池中Cookie已耗尽，无法执行");
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
				log.info("使用代理: {}:{}", request.getProxy().getIp(), request.getProxy().getPort());
				connection.proxy(proxy);
			}
		} catch (Exception e) {
			log.error("URL处理失败: {}", e.getMessage());
			throw new RuntimeException("URL处理失败", e);
		}
		return this;
	}

	public CrawlerResponse execute() {

		if (Objects.isNull(connection)) {
			this.build();
		}
		try {
			Connection.Response execute = connection.execute();
			this.response.jsoupResponse = execute;
			this.response.httpStatus = execute.statusCode();

//			if (!getRequest().followRedirects) {
//				if (execute.statusCode() == 307) {
//					String redirectUrl = execute.header("Location");
//					System.out.println("Redirect URL: " + redirectUrl);
//					// 手动处理重定向
//					this.request.followRedirects = true;
//					url(redirectUrl);
//					CrawlerResponse resp = execute();
//					System.out.println("Final URL: " + resp.getJsoupResponse().url());
//					System.out.println("Status Code: " + resp.getJsoupResponse().statusCode());
//				}
//			}

			updateResponseCookies(execute);
		} catch (HttpStatusException e) {
			log.error("HttpStatusException： {}", e.getStatusCode());
			if (e.getStatusCode() == 403) {
				CookieUtil.remove(request.cookies);
				log.warn("==== Cookie已失效，从Cookie池中移除，剩余Cookie数 {} ====", COOKIE_POOL.size());
				log.info("=== 使用随机Cookie重新请求 ===");
				this.request.cookies = null;
				connection = null;
				execute();
			}
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
