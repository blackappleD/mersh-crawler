package com.pkz.bla.mershcrawler.util;

import lombok.extern.slf4j.Slf4j;
import org.brotli.dec.BrotliInputStream;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/11 12:51
 */
@Slf4j
@Component
public class JsoupUtil {

	public static Document parseResponseHtml(Connection.Response response) {

		return Jsoup.parse(new String(brDecode(response), StandardCharsets.UTF_8), response.url().toExternalForm());
	}

	public static String parseResponseJson(Connection.Response response) {
		return new String(brDecode(response), StandardCharsets.UTF_8);
	}


	private static byte[] brDecode(Connection.Response response) {
		String contentEncoding = response.header("Content-Encoding");
		byte[] bodyBytes = response.bodyAsBytes();

		if ("br".equalsIgnoreCase(contentEncoding)) {
			try {
				bodyBytes = decompressBrotli(bodyBytes);
			} catch (IOException e) {
				throw new RuntimeException("Html解码失败");
			}
		}
		return bodyBytes;
	}


	private static byte[] decompressBrotli(byte[] compressedData) throws IOException {
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
		     BrotliInputStream brotliInputStream = new BrotliInputStream(inputStream);
		     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[4096];
			int read;
			while ((read = brotliInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}

			return outputStream.toByteArray();
		}
	}

}