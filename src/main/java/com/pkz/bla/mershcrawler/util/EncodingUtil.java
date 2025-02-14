package com.pkz.bla.mershcrawler.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EncodingUtil {

	public static String detectCharset(byte[] data) {
		if (isValidUTF8(data)) {
			return StandardCharsets.UTF_8.name();
		}
		if (isValidCyrillic(data)) {
			return "Windows-1251";
		}
		return StandardCharsets.UTF_8.name();
	}

	private static boolean isValidUTF8(byte[] data) {
		try {
			Arrays.toString(data).getBytes(StandardCharsets.UTF_8);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean isValidCyrillic(byte[] data) {
		int cyrillicCount = 0;
		for (byte b : data) {
			int unsignedByte = b & 0xFF;
			if (unsignedByte >= 192 && unsignedByte <= 255) {
				cyrillicCount++;
			}
		}
		return cyrillicCount > data.length / 2;
	}

	public static void printHexDump(byte[] data, int length) {
		for (int i = 0; i < Math.min(data.length, length); i++) {
			System.out.printf("%02X ", data[i]);
			if ((i + 1) % 16 == 0) System.out.println();
		}
	}
} 