package com.pkz.bla.mershcrawler.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/6/11 9:33
 */
@Slf4j
public class JsonUtil {
	@Getter
	static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new JavaTimeModule());
	}

	public static String toJson(Object value, boolean beautify) {
		try {
			if (beautify) {
				return new String(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value),
						StandardCharsets.UTF_8);
			} else {
				return new String(objectMapper.writeValueAsBytes(value), StandardCharsets.UTF_8);
			}
		} catch (JsonProcessingException ex) {
			log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	public static String toJson(Object value) {
		return toJson(value, false);
	}

	public static <T> T fromJson(String content, Class<T> valueType) {
		try {
			return objectMapper.readValue(content, valueType);
		} catch (JsonProcessingException ex) {
			log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	public static <T> T fromJson(String content, TypeReference<T> valueType) {
		try {
			return objectMapper.readValue(content, valueType);
		} catch (JsonProcessingException ex) {
			log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	public static <T> T fromJson(byte[] content, Class<T> valueType) {
		try {
			return objectMapper.readValue(content, valueType);
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	public static <T> T fromJson(byte[] content, TypeReference<T> valueType) {
		try {
			return objectMapper.readValue(content, valueType);
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	public static <V> Map<String, V> mapFromJson(byte[] content) {
		return fromJson(content, new MapTypeRefenrence<V>());
	}

	public static <V> Map<String, V> mapFromJson(String content) {
		return fromJson(content, new MapTypeRefenrence<V>());
	}

	public static class MapTypeRefenrence<V> extends TypeReference<Map<String, V>> {

	}


	static Configuration config = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();

	/**
	 * 根据jsonpath读取字段
	 *
	 * @param <R>     返回类型，自动根据泛型转换
	 * @param json    要查找的json字符串
	 * @param path    jsonpath路径
	 * @param filters
	 * @return
	 */
	public static <R> R findByPath(String json, String path, Predicate... filters) {
		return JsonPath.using(config).parse(json).read(path, filters);
	}

}
