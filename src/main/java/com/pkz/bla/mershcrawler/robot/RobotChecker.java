package com.pkz.bla.mershcrawler.robot;

import cn.hutool.core.util.RandomUtil;
import com.pkz.bla.mershcrawler.exception.MershCrawlerException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/14 21:01
 */
@Slf4j
@SuppressWarnings("ResultOfMethodCallIgnored")
public class RobotChecker {

	private WebDriver driver;

	public RobotChecker() {
		initDriver();
	}

	private void initDriver() {
		try {
			String chromedriverPath = extractDriverFromResources();
			System.setProperty("webdriver.chrome.driver", chromedriverPath);
			log.info("=== ChromeDriver路径: {}", chromedriverPath);

			ChromeOptions options = new ChromeOptions();
			String chromePath = getChromePath();
			options.setBinary(chromePath);

			options.addArguments("--headless=new");
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--disable-gpu");
			options.addArguments("--window-size=1920,1080");

			options.addArguments("--disable-blink-features=AutomationControlled");
			options.addArguments("--disable-extensions");
			options.addArguments("--disable-software-rasterizer");
			options.addArguments("--disable-dev-tools");
			options.addArguments("--remote-debugging-port=0");

			options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

			options.addArguments("--lang=en-US,en;q=0.9");

			Map<String, Object> prefs = new HashMap<>();
			prefs.put("profile.managed_default_content_settings.images", 2);
			prefs.put("profile.default_content_settings.popups", 0);
			prefs.put("profile.password_manager_enabled", false);
			prefs.put("credentials_enable_service", false);
			prefs.put("profile.default_content_setting_values.notifications", 2);
			options.setExperimentalOption("prefs", prefs);

			options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
			options.setExperimentalOption("useAutomationExtension", false);

			options.setPageLoadStrategy(PageLoadStrategy.EAGER);

			driver = new ChromeDriver(options);

			((JavascriptExecutor) driver).executeScript(
					"Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
			);

			log.info("=== ChromeDriver初始化成功");

		} catch (Exception e) {
			log.error("=== 初始化WebDriver失败: {}", e.getMessage(), e);
			throw new RuntimeException("初始化WebDriver失败", e);
		}
	}


	private String extractDriverFromResources() {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			String resourcePath;
			String driverFileName;

			if (os.contains("windows")) {
				resourcePath = "/chrome/windows/driver/chromedriver.exe";
				driverFileName = "chromedriver.exe";
			} else if (os.contains("linux")) {
				resourcePath = "/chrome/linux/driver/chromedriver";
				driverFileName = "chromedriver";
			} else {
				throw new RuntimeException("不支持的操作系统: " + os);
			}

			String tempDirName = "selenium-drivers-" + System.currentTimeMillis();
			File driverDir = new File(System.getProperty("java.io.tmpdir"), tempDirName);
			if (!driverDir.exists()) {
				driverDir.mkdirs();
			}

			File driverFile = new File(driverDir, driverFileName);

			try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
				if (in == null) {
					throw new RuntimeException("未找到ChromeDriver资源文件: " + resourcePath);
				}

				try (FileOutputStream out = new FileOutputStream(driverFile)) {
					byte[] buffer = new byte[1024];
					int length;
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
				}
			}

			if (!os.contains("windows")) {
				driverFile.setExecutable(true, false);
			}

			driverFile.deleteOnExit();
			driverDir.deleteOnExit();

			log.info("=== ChromeDriver已从resources提取到: {}", driverFile.getAbsolutePath());
			return driverFile.getAbsolutePath();

		} catch (Exception e) {
			log.error("=== 提取ChromeDriver失败: {}", e.getMessage());
			throw new RuntimeException("提取ChromeDriver失败", e);
		}
	}

	private String getChromePath() {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			File chromeFile;

			if (os.contains("windows")) {
				String resourcePath = "/chrome/windows/explore/chrome.exe";
				URL resource = getClass().getResource(resourcePath);
				if (resource == null) {
					throw new MershCrawlerException("=== 未找到Chrome.exe文件");
				}
				chromeFile = new File(resource.toURI());
			} else if (os.contains("linux")) {
				String resourcePath = "/usr/bin/google-chrome";
				chromeFile = new File(resourcePath);
			} else {
				throw new RuntimeException("不支持的操作系统: " + os);
			}
			String absolutePath = chromeFile.getAbsolutePath();

			log.info("=== 当前操作系统: {}, 使用Chrome路径: {}", os, absolutePath);

			if (!os.contains("windows")) {
				chromeFile.setExecutable(true, false);
			}
			return absolutePath;
		} catch (Exception e) {
			log.error("=== 获取Chrome路径失败: {}", e.getMessage());
			throw new RuntimeException("获取Chrome路径失败", e);
		}
	}

	public Map<String, String> handleRobotCheck(String url) {
		try {
			log.info("=== 开始处理机器人检测，URL: {}", url);

			driver.get(url);
			long delay = 6000 + RandomUtil.randomInt(5) * 1000L;
			log.info("=== 已加载页面，并随机等待 {} ms", delay);
			Thread.sleep(delay);

			Actions actions = new Actions(driver);
			actions.moveByOffset(10, 20).perform();
			Thread.sleep(500);
			actions.moveByOffset(-5, -10).perform();

			((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight/2)");
			Thread.sleep(1000);

			Map<String, String> cookieMap = new HashMap<>();
			driver.manage().getCookies().forEach(cookie ->
					cookieMap.put(cookie.getName(), cookie.getValue()));

			if (!cookieMap.containsKey("abt_data")) {
				log.warn("=== 未找到abt_data cookie");
				// 多尝试几次获取Cookie
				for (int i = 0; i < 3; i++) {
					Thread.sleep(1000);
					org.openqa.selenium.Cookie abtCookie = driver.manage().getCookieNamed("abt_data");
					if (abtCookie != null) {
						cookieMap.put("abt_data", abtCookie.getValue());
						log.info("=== 第{}次尝试通过getCookieNamed获取到abt_data: {}", i + 1, abtCookie.getValue());
						break;
					}
				}
			}

			return cookieMap;
		} catch (Exception e) {
			log.error("=== 处理机器人检测失败: {}", e.getMessage(), e);
			return null;
		}
	}

	public void close() {
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception e) {
				log.error("=== 关闭WebDriver失败", e);
			}
		}
	}
} 