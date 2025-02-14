package com.pkz.bla.mershcrawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 代理配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
	private boolean enable = true;

	private String authKey;

	private String authPassword;
} 