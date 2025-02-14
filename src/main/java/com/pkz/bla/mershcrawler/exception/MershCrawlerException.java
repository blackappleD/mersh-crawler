package com.pkz.bla.mershcrawler.exception;

import lombok.Data;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/15 1:02
 */
@Data
public class MershCrawlerException extends RuntimeException {

	private final String code = "500";
	private String message;

	public MershCrawlerException(String message) {
		super(message);
		this.message = message;
	}

}
