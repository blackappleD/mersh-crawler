package com.pkz.bla.mershcrawler.controller;

import com.pkz.bla.mershcrawler.dto.DetectionPlanResult;
import com.pkz.bla.mershcrawler.service.CrawlerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/2/8 17:13
 */
@RestController
@RequestMapping("/ozon")
public class OzonController {

	@Resource
	private CrawlerService crawlerService;

	@GetMapping("/product")
	public DetectionPlanResult getProductInfo(@RequestParam Long sku) {
		return crawlerService.getProductInfo(sku);
	}
}
