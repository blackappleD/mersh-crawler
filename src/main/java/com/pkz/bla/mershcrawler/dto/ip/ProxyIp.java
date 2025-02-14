package com.pkz.bla.mershcrawler.dto.ip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 代理IP信息实体类
 */
@Data
public class ProxyIp {
	/**
	 * IP地址
	 */
	@JsonProperty("Ip")
	private String ip;

	/**
	 * 代理端口
	 */
	@JsonProperty("Port")
	private Integer port;

	/**
	 * 代理国家
	 */
	@JsonProperty("Country")
	private String country;

	/**
	 * 代理省份
	 */
	@JsonProperty("Province")
	private String province;

	/**
	 * 代理城市
	 */
	@JsonProperty("City")
	private String city;

	/**
	 * IP提供商
	 */
	@JsonProperty("Isp")
	private String isp;

	/**
	 * 代理类型
	 */
	@JsonProperty("Type")
	private String type;

	/**
	 * 代理匿名度
	 * 透明：显示真实IP
	 * 普匿：显示假的IP
	 * 高匿：无代理IP特征
	 */
	@JsonProperty("Anonymity")
	private String anonymity;

	/**
	 * 代理验证时间
	 */
	@JsonProperty("Time")
	private String time;

	/**
	 * 代理响应速度
	 */
	@JsonProperty("Speed")
	private String speed;

	/**
	 * 验证请求成功的次数
	 */
	@JsonProperty("SuccessNum")
	private Integer successNum;

	/**
	 * 验证请求的次数
	 */
	@JsonProperty("RequestNum")
	private Integer requestNum;

	/**
	 * 代理源
	 */
	@JsonProperty("Source")
	private String source;
} 