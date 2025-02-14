package com.pkz.bla.mershcrawler.dto.data;

import lombok.Data;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/11 13:57
 */
@Data
public class Product {
	private String context;

	private String type;

	private AggregateRating aggregateRating;

	private String brand;

	private String description;

	private String image;

	private String name;

	private Offer offers;

	private String sku;

	@Data
	public static class AggregateRating {
		private String type;

		private String ratingValue;

		private String reviewCount;
	}

	@Data
	public static class Offer {
		private String type;

		private String url;

		private String availability;

		private String price;

		private String priceCurrency;
	}
}