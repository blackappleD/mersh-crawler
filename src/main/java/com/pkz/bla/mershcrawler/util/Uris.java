package com.pkz.bla.mershcrawler.util;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/13 22:51
 */
public class Uris {

	public interface Ozon {

		String SELLER_BASE_URL = "https://www.ozon.ru{}";
		String PRODUCT_BASE_URL = "https://www.ozon.ru/product/{}/";
		String FOLLOW_SELLER_PAGE_BASE_URL = "https://www.ozon.ru/api/entrypoint-api.bx/page/json/v2?url={}";
	}
}
