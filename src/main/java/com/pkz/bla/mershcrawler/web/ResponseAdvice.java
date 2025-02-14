package com.pkz.bla.mershcrawler.web;

import com.pkz.bla.mershcrawler.exception.MershCrawlerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/14 22:44
 */
@Order(0)
@Slf4j
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		var method = returnType.getMethod();
		return method != null;
	}

	@Override
	public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
	                              Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
	                              ServerHttpResponse response) {
		return body;
	}

	@ExceptionHandler(MershCrawlerException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Object dgExceptionHandler(MershCrawlerException ex) {

		return new Response(ex.getCode(), ex.getMessage());

	}


}