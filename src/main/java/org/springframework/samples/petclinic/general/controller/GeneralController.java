package org.springframework.samples.petclinic.general.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.general.service.GeneralService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * Project : spring-petclinic
 * File    : GeneralController.java
 * Created : 2026-01-29
 * Author  : JeongMin Lee
 *
 * Description :
 *   사용목적: 일반클리닉 항목에 대한 라우팅(상세)을 담당한다.
 */
@Controller
@RequestMapping("/general")
public class GeneralController {

	private static final Logger log = LoggerFactory.getLogger(GeneralController.class);
	private final GeneralService generalService;

	public GeneralController(GeneralService generalService) { this.generalService = generalService; }

	/**
	 * 병원 소개
	 * - 타입별 소개 페이지로 이동
	 */
	/**
	 * @param type 소개 페이지 타입
	 * @return 목록 템플릿
	 */
	@GetMapping("/{type}")
	public String general(@PathVariable("type") String type, Model model) {

		model.addAttribute("template", "general/"+type);

		return "fragments/layout";
	}
}
