package com.pkz.bla.mershcrawler.controller;

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
@RequestMapping("/pdd")
public class PddController {


	@GetMapping("/product")
	public void getProductInfo(@RequestParam Long sku) {

	}
}
