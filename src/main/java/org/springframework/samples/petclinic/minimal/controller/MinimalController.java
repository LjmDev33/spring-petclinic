package org.springframework.samples.petclinic.minimal.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.minimal.service.MinimalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * Project : spring-petclinic
 * File    : MinimalController.java
 * Created : 2026-01-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 최소침습수술에 대한 라우팅(상세)을 담당한다.
 *
 * License :
 *   Copyright (c) 2026 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/minimal")
public class MinimalController {

	private static final Logger log = LoggerFactory.getLogger(MinimalController.class);
	private final MinimalService minimalService;

	public MinimalController(MinimalService minimalService){this.minimalService=minimalService;}

	/**
	 * 최소침습수술 소개
	 * - 타입별 소개 페이지로 이동
	 */
	/**
	 * @param type 소개 페이지 타입
	 * @return 목록 템플릿
	 */
	@GetMapping("/{type}")
	public String general(@PathVariable("type") String type, Model model) {

		model.addAttribute("template", "minimal/"+type);

		return "fragments/layout";
	}
}
