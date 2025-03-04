package com.pkz.bla.mershcrawler.test;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.pkz.bla.mershcrawler.core.CrawlerInstance;
import com.pkz.bla.mershcrawler.enums.Domain;
import com.pkz.bla.mershcrawler.service.ProxyIpService;
import com.pkz.bla.mershcrawler.util.JsonUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼多多商品详情页爬虫测试
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
public class PddCrawlerTest {

	private static final String PRODUCT_URL = "https://mobile.yangkeduo.com/goods.html?goods_id={}";

	@Resource
	private ProxyIpService proxyIpService;

	@Test
	public void testCrawlProduct() {
		try {
			// 1. 构建请求参数
			String goodsId = "698546404171";

			String url = CharSequenceUtil.format(PRODUCT_URL, goodsId);

			// 2. 构建请求头
			Map<String, String> headers = new HashMap<>();
			headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
			headers.put("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6");
			headers.put("cache-control", "no-cache");
			headers.put("pragma", "no-cache");
			headers.put("sec-ch-ua", "\"Not(A:Brand\";v=\"99\", \"Microsoft Edge\";v=\"133\", \"Chromium\";v=\"133\"");
			headers.put("sec-ch-ua-mobile", "?0");
			headers.put("sec-ch-ua-platform", "\"Windows\"");
			headers.put("sec-fetch-dest", "document");
			headers.put("sec-fetch-mode", "navigate");
			headers.put("sec-fetch-site", "same-origin");
			headers.put("sec-fetch-user", "?1");
			headers.put("upgrade-insecure-requests", "1");
			headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 Edg/133.0.0.0");

			// 3. 构建Cookie
			Map<String, String> cookies = new HashMap<>();
			cookies.put("api_uid", "CikCL2fADVYFXQBrPtzqAg==");
			cookies.put("_nano_fp", "XpmYX0Eqlpd8n5ToX9_Ikp89TmHjD2uPCXLNpLZc");
			cookies.put("webp", "1");
			cookies.put("jrpl", "4f9OyklZfZd1CDi9OCtRful5FYMTtSHM");
			cookies.put("njrpl", "4f9OyklZfZd1CDi9OCtRful5FYMTtSHM");
			cookies.put("dilx", "9CyJfndWFqaNg5jjPKoXm");
			cookies.put("PDDAccessToken", "Y6H6VYEUHCH56NHKGGWQGGUHZAXT2YF6OA7LFTFWUNJTOQHVNF2Q120c597");
			cookies.put("pdd_user_id", "9181282773189");
			cookies.put("pdd_user_uin", "RFFSFU7GXTBX5WV2W2QANI5DH4_GEXDA");
			cookies.put("pdd_vds", "gawLLyNybmtGGoGLOnayNntynIOObmoLaaNIQynaNGtbmtnEnbQbnLoLyNON");

			CrawlerInstance.CrawlerResponse response = CrawlerInstance.create(Domain.PDD, url)
					.proxy(proxyIpService.getRandomProxy1())
					.cookies(cookies)
					.headers(headers)
					.execute();
			// 5. 解析响应
			String html = response.stringBody();
			ProductDetail detail = parseProductDetail(html);
			log.info("商品详情: {}", JsonUtil.toJson(detail, true));
		} catch (Exception e) {
			log.error("爬取失败", e);
		}
	}

	private ProductDetail parseProductDetail(String html) {
		ProductDetail detail = new ProductDetail();

		try {
			// 1. 提取原始数据
			String rawData = extractRawData(html);
			if (rawData == null) {
				log.warn("未找到商品数据");
				return detail;
			}

			// 2. 解析JSON
			JsonNode data = JsonUtil.fromJson(rawData, JsonNode.class);
			JsonNode goods = data.path("store").path("initDataObj").path("goods");

			// 3. 设置基本信息
			detail.setProductUrl(PRODUCT_URL);
			detail.setProductTitle(goods.path("goodsName").asText(""));
			detail.setProductIntro(goods.path("shareDesc").asText(""));

			// 4. 设置商品详情图片
			List<String> detailImages = new ArrayList<>();
			JsonNode gallery = goods.path("detailGallery");
			if (gallery.isArray()) {
				for (JsonNode image : gallery) {
					if (image.has("url")) {
						detailImages.add(image.path("url").asText());
					}
				}
			}
			detail.setProductDetailImageUrls(detailImages);

			// 5. 设置商品视频
			List<String> videos = new ArrayList<>();
			JsonNode videoSection = goods.path("ui").path("live_section").path("float_info").path("replay_info");
			if (!videoSection.isMissingNode()) {
				String videoInfo = videoSection.asText();
				// 解析嵌套的JSON
				if (videoInfo != null && !videoInfo.isEmpty()) {
					JsonNode videoData = JsonUtil.fromJson(videoInfo, JsonNode.class);
					String videoUrl = videoData.path("small_window_player_info").path("video_url_info").path("videos").path(0).path("url").asText(null);
					if (videoUrl != null) {
						videos.add(videoUrl);
					}
				}
			}
			detail.setProductVideoUrls(videos);

			// 6. 设置SKU信息
			parseSKUInfo(goods, detail);

			// 7. 设置商品属性
			Map<String, String> attrs = new HashMap<>();
			JsonNode properties = goods.path("goodsProperty");
			if (properties.isArray()) {
				for (JsonNode prop : properties) {
					String key = prop.path("key").asText();
					JsonNode values = prop.path("values");
					if (values.isArray()) {
						attrs.put(key, String.join(", ", values.findValuesAsText("value")));
					}
				}
			}
			detail.setProductAttrs(attrs);

			// 8. 设置其他信息
			detail.setMinimumOrderQuantity(1);
			detail.setLogisticPrice(parseLogisticPrice(goods));
			detail.setPreDispatchPeriod(parsePreDispatchPeriod(goods));

		} catch (Exception e) {
			log.error("解析商品数据失败", e);
		}

		return detail;
	}

	private String extractRawData(String html) {
		String startMarker = "window.rawData=";
		String endMarker = ";</script>";

		int startIndex = html.indexOf(startMarker);
		if (startIndex == -1) return null;

		startIndex += startMarker.length();
		int endIndex = html.indexOf(endMarker, startIndex);
		if (endIndex == -1) return null;

		return html.substring(startIndex, endIndex);
	}

	private void parseSKUInfo(JsonNode goods, ProductDetail detail) {
		JsonNode skus = goods.path("skus");
		if (!skus.isArray()) return;

		// ... SKU解析逻辑，参考ts代码实现 ...
	}

	private double parseLogisticPrice(JsonNode goods) {
		JsonNode freightInfo = goods.path("freightInfo");
		if (freightInfo.path("isFree").asBoolean(false)) {
			return 0;
		}
		return freightInfo.path("amount").asDouble(0);
	}

	private int parsePreDispatchPeriod(JsonNode goods) {
		return goods.path("dispatchDuration").asInt(-1);
	}

	@Data
	public static class ProductDetail {
		private String productUrl;
		private String productTitle;
		private String productIntro;
		private List<String> productDetailImageUrls;
		private List<String> productVideoUrls;
		private List<SKUInfo> productSkuInfos;
		private Map<String, String> productAttrs;
		private int minimumOrderQuantity;
		private double logisticPrice;
		private int preDispatchPeriod;
	}

	@Data
	public static class SKUInfo {
		private String specName;
		private String specType;
		private double productOriginPrice;
		private String imgUrl;
		private int stock;
	}
}
