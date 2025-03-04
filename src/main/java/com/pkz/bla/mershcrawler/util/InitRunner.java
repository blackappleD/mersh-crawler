package com.pkz.bla.mershcrawler.util;

import com.pkz.bla.mershcrawler.enums.Domain;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/10 11:51
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class InitRunner implements ApplicationRunner {

	@Resource
	private ResourceLoader resourceLoader;

	@Override
	public void run(ApplicationArguments args) {
		initCookie();
		initMonthLang();
		initAccountPool();
//		initSslSocketFactory();
	}

	private void initAccountPool() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceLoader.getResource("classpath:init/tkshop_account.txt").getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					String[] split = line.split(";");
					if (split.length == 2) {
						AccountUtil.Account account = new AccountUtil.Account(split[0], split[1]);
						AccountUtil.putAccount(Domain.TK_SHOP, account);
					}
				}
			}
			log.info("=== {} Account加载完成，当前数量：{} ===", Domain.TK_SHOP, AccountUtil.accountQuantity(Domain.TK_SHOP));
		} catch (IOException e) {
			log.error("加载Account配置文件失败: {}", e.getMessage());
		}
	}

	private void initCookie() {
		initOzonCookie();
	}

	private void initOzonCookie() {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceLoader.getResource("classpath:init/ozon_cookie.txt").getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					HashMap<String, String> cookie = new HashMap<>();
					String[] cookieParams = line.split(";");
					for (String cookieParam : cookieParams) {
						String[] split = cookieParam.split("=");
						cookie.put(split[0].trim(), split[1].trim());
					}
					CookieUtil.putCookies(Domain.OZON, cookie);
				}
			}
			log.info("=== {} Cookie加载完成，当前数量：{} ===", Domain.OZON, CookieUtil.cookieQuantity(Domain.OZON));
		} catch (IOException e) {
			log.error("加载Cookie配置文件失败: {}", e.getMessage());
		}
	}

	private void initMonthLang() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceLoader.getResource("classpath:init/lang_month_map.txt").getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					String[] split = line.split(",");
					LangConstant.MONTH_MAP.put(split[0], Integer.valueOf(split[1]));
				}
			}
			log.info("=== 俄语映射加载完成，当前数量：{} ===", LangConstant.MONTH_MAP.size());
		} catch (IOException e) {
			log.error("加载俄语映射文件失败: {}", e.getMessage());
		}
	}

//	private void initSslSocketFactory() {
//		try {
//			// 创建一个不验证证书链的TrustManager
//			TrustManager[] trustAllCerts = new TrustManager[]{
//					new X509TrustManager() {
//						public X509Certificate[] getAcceptedIssuers() {
//							return new X509Certificate[0];
//						}
//
//						public void checkClientTrusted(X509Certificate[] certs, String authType) {
//						}
//
//						public void checkServerTrusted(X509Certificate[] certs, String authType) {
//						}
//					}
//			};
//
//			// 创建SSLContext并初始化
//			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
//			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//
//			// 设置主机名验证器
//			HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
//
//			log.info("=== SSL配置初始化成功 ===");
//			JsoupUtil.setSslSocketFactory(sslContext.getSocketFactory());
//
//		} catch (NoSuchAlgorithmException | KeyManagementException e) {
//			log.error("SSL配置初始化失败", e);
//			throw new RuntimeException("Failed to initialize SSL configuration", e);
//		}
//	}


}
