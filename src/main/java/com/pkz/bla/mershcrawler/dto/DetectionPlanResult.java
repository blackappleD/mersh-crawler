package com.pkz.bla.mershcrawler.dto;

import com.pkz.bla.mershcrawler.util.LangConstant;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/10 13:42
 */
@Data
public class DetectionPlanResult {

	private static final Logger log = LoggerFactory.getLogger(DetectionPlanResult.class);

	private Long sku;

	private String title;

	private BigDecimal price;

	private String url;

	private List<FollowSellProductInfo> followSellProductInfoList;

	@Data
	public static class FollowSellProductInfo {

		// 图片url
		private String imageUrl;

		// 商品名称
		private String productName;

		// 售卖价格
		private String salePrice;

		// 店铺名称
		private String storeName;

		// 店铺url
		private String storeUrl;

		// 物流时效
		private Integer logisticsTime;

		// 是否中国卖家
		private Boolean chineseSeller;

		@SuppressWarnings("")
		public void setLogisticsTime(Integer logisticsTime) {
			this.logisticsTime = logisticsTime;
		}

		public void setLogisticsTime(String logisticsTime) {

			try {
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s+(\\p{L}+)");
				java.util.regex.Matcher matcher = pattern.matcher(logisticsTime);

				if (matcher.find()) {
					String day = matcher.group(1);
					String month = matcher.group(2).toLowerCase();

					Integer monthNum = LangConstant.MONTH_MAP.get(month);
					LocalDateTime startTime = LocalDateTime.now();
					LocalDateTime endTime = LocalDateTime.of(startTime.getYear(), monthNum, Integer.parseInt(day), 0, 0, 0);

					setLogisticsTime((int) ChronoUnit.DAYS.between(startTime, endTime));

				}
			} catch (Exception e) {
				log.error("解析物流时间失败: {}", logisticsTime);
			}
		}
	}

}
