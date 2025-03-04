package com.pkz.bla.mershcrawler.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.pkz.bla.mershcrawler.core.CrawlerInstance;
import com.pkz.bla.mershcrawler.core.analyzer.OzonAnalyzer;
import com.pkz.bla.mershcrawler.dto.DetectionPlanResult;
import com.pkz.bla.mershcrawler.dto.ip.ProxyIp;
import com.pkz.bla.mershcrawler.enums.Domain;
import com.pkz.bla.mershcrawler.util.HeaderUtil;
import com.pkz.bla.mershcrawler.util.Uris;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/8 16:16
 */
@Slf4j
@Service
public class CrawlerService {

	@Resource
	private ProxyIpService proxyIpService;

	public DetectionPlanResult getProductInfo(Long sku) {
		String merchandiseUrl = CharSequenceUtil.format(Uris.Ozon.PRODUCT_BASE_URL, sku);

		ProxyIp proxyIp = proxyIpService.getRandomProxy1();
		if (proxyIp == null) {
			log.warn("未获取到可用代理，尝试直连...");
		}

		try {
			Document doc = CrawlerInstance.create(Domain.OZON, merchandiseUrl)
					.proxy(proxyIp)
					.updateCookies(true)
					.execute()
					.documentBody();
//			debugApiResponse("main_page.html", doc.html());
			log.info("查询到商品：{}", doc.title());

			OzonAnalyzer ozonAnalyzer = OzonAnalyzer
					.create(sku, merchandiseUrl)
					.mainAccepter(doc);

			String sellerPath = ozonAnalyzer.getSellerPath();

			String followSellerPageUrl = CharSequenceUtil.format(Uris.Ozon.FOLLOW_SELLER_PAGE_BASE_URL, sellerPath);
			log.info("解析到SellersUrl: {}", followSellerPageUrl);

			String sellersJson = CrawlerInstance.create(Domain.OZON, followSellerPageUrl)
					.headers(HeaderUtil.sellerHeaders())
					.proxy(proxyIp)
					.randomCookies()
					.updateCookies(false)
					.execute()
					.jsonBody();

//			debugApiResponse("sellers_json.json", sellersJson);
			ozonAnalyzer.sellersAccepter(sellersJson);
			return ozonAnalyzer.analyse();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private void debugApiResponse(String filename, String content) {
		try {
			// 将内容保存到文件以便分析
			java.nio.file.Files.writeString(
					java.nio.file.Path.of(filename),
					content);
			log.info("数据已保存到 {}", filename);
		} catch (IOException e) {
			log.error("保存debug数据失败：{}", e.getMessage());
		}
	}

}
