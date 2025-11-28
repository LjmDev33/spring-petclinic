package org.springframework.samples.petclinic.introduce.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.introduce.service.IntroduceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * Project : spring-petclinic
 * File    : IntroductionController.java
 * Created : 2025-11-27
 * Author  : ChangYong Kim
 *
 * Description :
 *   사용목적: 소개에 대한 라우팅(상세)을 담당한다.
 */
@Controller
@RequestMapping("/introduce")
public class IntroduceController {

	private static final Logger log = LoggerFactory.getLogger(IntroduceController.class);
	private final IntroduceService introduceService;

	public IntroduceController(IntroduceService introduceService) { this.introduceService = introduceService; }

	/**
	 * 병원 소개
	 * - 타입별 소개 페이지로 이동
	 */
	/**
	 * @param type 소개 페이지 타입
	 * @return 목록 템플릿
	 */
	@GetMapping("/{type}")
	public String introduce(@PathVariable("type") String type, Model model) {

		model.addAttribute("template", "introduce/"+type);

		return "fragments/layout";
	}
}
