package com.pkz.bla.mershcrawler.util;

import cn.hutool.core.util.RandomUtil;

import java.util.*;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/11 12:53
 */
public class CookieUtil {
	public static final List<Map<String, String>> COOKIE_POOL = new ArrayList<>();
	public static final List<String> COOKIE_STR_POOL = new ArrayList<>();

	private static final Random RANDOM = new Random();


	public static Map<String, String> getRandomCookies() {
		if (COOKIE_POOL.isEmpty()) {
			return null;
		}
		return COOKIE_POOL.get(randomIndex());
	}

	public static String getRandomCookieString() {
		return RandomUtil.randomEle(COOKIE_STR_POOL);
	}

	public static int randomIndex() {
		return RANDOM.nextInt(CookieUtil.COOKIE_POOL.size());
	}

	public static boolean remove(Map<String, String> cookies) {
		return COOKIE_POOL.remove(cookies);
	}
}