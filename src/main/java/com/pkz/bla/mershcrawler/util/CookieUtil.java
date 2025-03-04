package com.pkz.bla.mershcrawler.util;

import com.pkz.bla.mershcrawler.enums.Domain;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/11 12:53
 */
@Slf4j
public class CookieUtil {
	private static final List<Map<String, String>> OZON_COOKIE_POOL = new ArrayList<>();
	private static final List<Map<String, String>> TKSHOP_COOKIE_POOL = new ArrayList<>();
	private static final List<Map<String, String>> PDD_COOKIE_POOL = new ArrayList<>();

	private static final Random RANDOM = new Random();


	public static Map<String, String> getRandomCookies(Domain tag) {
		return switch (tag) {
			case OZON -> getOzonCookies();
			case TK_SHOP -> getTkShopCookies();
			case PDD -> getPddCookies();
		};

	}

	private static Map<String, String> getOzonCookies() {
		if (OZON_COOKIE_POOL.isEmpty()) {
			return null;
		}
		return OZON_COOKIE_POOL.get(RANDOM.nextInt(CookieUtil.OZON_COOKIE_POOL.size()));
	}

	private static Map<String, String> getTkShopCookies() {
		if (TKSHOP_COOKIE_POOL.isEmpty()) {
			return null;
		}
		return TKSHOP_COOKIE_POOL.get(RANDOM.nextInt(CookieUtil.TKSHOP_COOKIE_POOL.size()));
	}

	private static Map<String, String> getPddCookies() {
		if (PDD_COOKIE_POOL.isEmpty()) {
			return null;
		}
		return PDD_COOKIE_POOL.get(RANDOM.nextInt(CookieUtil.PDD_COOKIE_POOL.size()));
	}

	public static void putCookies(Domain domain, Map<String, String> cookies) {
		int size = switch (domain) {
			case OZON -> {
				OZON_COOKIE_POOL.add(cookies);
				yield OZON_COOKIE_POOL.size();
			}
			case TK_SHOP -> {
				TKSHOP_COOKIE_POOL.add(cookies);
				yield TKSHOP_COOKIE_POOL.size();
			}
			case PDD -> {
				PDD_COOKIE_POOL.add(cookies);
				yield PDD_COOKIE_POOL.size();
			}
		};
		log.info("=== {}更新了一条Cookie，当前Cookie池数量：{} ===", domain.name(), size);
	}

	public static boolean remove(Domain domain, Map<String, String> cookies) {

		switch (domain) {
			case OZON -> OZON_COOKIE_POOL.remove(cookies);
			case TK_SHOP -> TKSHOP_COOKIE_POOL.remove(cookies);
			default -> {
				return false;
			}
		}
		return true;
	}

	public static int cookieQuantity(Domain domain) {

		switch (domain) {
			case OZON -> {
				return OZON_COOKIE_POOL.size();
			}
			case TK_SHOP -> {
				return TKSHOP_COOKIE_POOL.size();
			}
		}
		return 0;
	}
}