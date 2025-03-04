package com.pkz.bla.mershcrawler.core.analyzer;

import cn.hutool.core.text.CharSequenceUtil;
import com.jayway.jsonpath.JsonPath;
import com.pkz.bla.mershcrawler.dto.DetectionPlanResult;
import com.pkz.bla.mershcrawler.dto.data.Product;
import com.pkz.bla.mershcrawler.dto.data.Seller;
import com.pkz.bla.mershcrawler.dto.data.Sellers;
import com.pkz.bla.mershcrawler.util.JsonUtil;
import com.pkz.bla.mershcrawler.util.Uris;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/13 22:26
 */
@Slf4j
public class OzonAnalyzer {


	private final DetectionPlanResult product;

	private Document mainPageDoc;

	private String sellersJson;

	public static OzonAnalyzer create(Long sku, String url) {
		return new OzonAnalyzer(sku, url);
	}

	private OzonAnalyzer(Long sku, String url) {
		this.product = new DetectionPlanResult();
		product.setUrl(url);
		product.setSku(sku);
	}

	public OzonAnalyzer mainAccepter(Document mainPageDoc) {

		this.mainPageDoc = mainPageDoc;
		return this;
	}

	public String getSellerPath() {
		if (Objects.isNull(mainPageDoc)) {
			return "";
		}
		// 获取分页器状态数据
		Element paginatorElement = mainPageDoc.selectFirst("div[id^=state-paginator]");
		String sellerPath;
		if (Objects.nonNull(paginatorElement)) {
			sellerPath = JsonPath.read(paginatorElement.attr("data-state"), "$.nextPage");
		} else {
			log.error("未找到分页器状态元素");
			sellerPath = "";
		}
		return sellerPath;
	}

	public OzonAnalyzer sellersAccepter(String sellersJson) {
		this.sellersJson = sellersJson;
		return this;
	}

	public DetectionPlanResult analyse() {

		if (Objects.nonNull(mainPageDoc)) {
			analyseMainInfo();
		}
		if (Objects.nonNull(sellersJson)) {
			analyseSellers();
		}
		return product;

	}

	public void analyseSellers() {

		Map<String, String> widgetStates = JsonPath.read(this.sellersJson, "$.widgetStates");

		String sellerListData = widgetStates.entrySet().stream()
				.filter(entry -> entry.getKey().contains("webSellerList"))
				.map(Map.Entry::getValue)
				.findFirst()
				.orElse(null);

		if (sellerListData != null) {
			Sellers sellers = JsonUtil.fromJson(sellerListData, Sellers.class);

			List<DetectionPlanResult.FollowSellProductInfo> list = sellers.getSellers().stream()
					.map(seller -> {
						DetectionPlanResult.FollowSellProductInfo info = new DetectionPlanResult.FollowSellProductInfo();
						info.setStoreName(seller.getName());
						info.setStoreUrl(CharSequenceUtil.format(Uris.Ozon.SELLER_BASE_URL, seller.getLink()));
						info.setImageUrl(seller.getLogoImageUrl());
						info.setProductName(product.getTitle());
						String priceJson = JsonUtil.toJson(seller.getPrice());
						if (priceJson.contains("cardPrice")) {
							info.setSalePrice(new BigDecimal(JsonUtil.fromJson(priceJson, Seller.Price2.class).getCardPrice().getPrice().replaceAll("[^0-9]", "")));
						} else {
							info.setSalePrice(new BigDecimal(JsonUtil.fromJson(priceJson, Seller.Price1.class).getPrice().replaceAll("[^0-9]", "")));
						}
						info.setSku(seller.getSku());
						Optional<Seller.Advantage> delivery = seller.getAdvantages().stream()
								.filter(tag -> tag.getKey().equals("delivery")).findFirst();
						if (delivery.isPresent()) {
							Seller.Advantage advantage = delivery.get();
							info.setLogisticsTime(advantage.getContentRs().getHeadRs().getFirst().getContent());
						} else {
							info.setLogisticsTime("未识别到物流信息");
						}
						info.setChineseSeller(
								seller.getCredentials().stream().anyMatch(credit -> credit.contains("CN")));
						return info;
					}).toList();
			product.setFollowSellProductInfoList(list);

		}

	}

	public void analyseMainInfo() {

		try {
			// 商品标题
			Element titleElement = mainPageDoc.selectFirst("h1");
			if (titleElement != null) {
				product.setTitle(titleElement.text());
			}
			// 从JSON-LD脚本中获取价格信息
			Element priceScript = mainPageDoc.selectFirst("script[type=application/ld+json]");
			if (priceScript != null) {
				String jsonContent = priceScript.html();
				Product productJsonBody = JsonUtil.fromJson(jsonContent, Product.class);
				product.setPrice(new BigDecimal(productJsonBody.getOffers().getPrice()));
			} else {
				log.error("未找到价格脚本标签");
			}

		} catch (Exception e) {
			log.error("获取主商品价格失败: {}", e.getMessage());
			e.printStackTrace();
		}
	}


}
