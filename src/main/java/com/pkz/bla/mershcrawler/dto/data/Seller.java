package com.pkz.bla.mershcrawler.dto.data;

import lombok.Data;

import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/11 13:58
 */
@Data
public class Seller {
	private String sku;

	private String id;

	private String name;

	private String link;

	private List<String> credentials;

	private String logoImageUrl;

	private List<Advantage> advantages;

	private String subtitle;

	private Object price;

	private String coverImage;

	private String productLink;

	@Data
	public static class Advantage {
		private Content contentRs;

		private String key;

		private String iconKey;
	}

	@Data
	public static class Content {
		private List<HeadContent> headRs;
	}

	@Data
	public static class HeadContent {
		private String type;

		private String content;
	}

	@Data
	public static class Price1 {
		private String originalPrice;

		private String price;
	}

	@Data
	public static class Price2 {
		private CardPrice cardPrice;

		@Data
		public static class CardPrice {

			private String price;

			private String text;

		}

	}
}