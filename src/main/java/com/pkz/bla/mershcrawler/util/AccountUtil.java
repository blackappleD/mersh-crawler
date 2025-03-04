package com.pkz.bla.mershcrawler.util;

import cn.hutool.core.util.RandomUtil;
import com.pkz.bla.mershcrawler.enums.Domain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/26 14:43
 */
@Slf4j
public class AccountUtil {

	@Getter
	private static final Map<Domain, List<Account>> ACCOUNT_POOL = new HashMap<>();

	public static Account getRandomAccount(Domain tag) {
		List<Account> accounts = ACCOUNT_POOL.get(tag);
		if (accounts == null || accounts.isEmpty()) {
			log.error("=== {} 无可用账号账号 ===", tag);
			return null;
		}
		return RandomUtil.randomEle(accounts);

	}

	public static void putAccount(Domain domain, Account account) {
		List<Account> accounts = ACCOUNT_POOL.get(domain);
		if (accounts == null) {
			accounts = new ArrayList<>();
		}
		accounts.add(account);
		ACCOUNT_POOL.put(domain, accounts);
		log.info("=== {}新增了一个账号，当前账号池池数量：{} ===", domain, accounts.size());
	}

	public static boolean remove(Domain domain, Account account) {
		List<Account> accounts = ACCOUNT_POOL.get(domain);
		if (accounts == null || accounts.isEmpty()) {
			return true;
		}
		return accounts.remove(account);
	}

	public static int accountQuantity(Domain domain) {
		if (ACCOUNT_POOL.containsKey(domain)) {
			return ACCOUNT_POOL.get(domain).size();
		}
		return 0;

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Account {
		private String accountName;
		private String password;
	}
}
