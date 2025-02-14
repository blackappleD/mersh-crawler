package com.pkz.bla.mershcrawler.dto.data;

import lombok.Data;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/13 22:56
 */
@Data
public class PriceInfo {
	private Price price;

	@Data
	public static class Price {
		private String price;
		private String originalPrice;
		private String discount;
	}
}
