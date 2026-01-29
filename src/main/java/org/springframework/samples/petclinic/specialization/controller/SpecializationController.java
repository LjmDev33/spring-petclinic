package org.springframework.samples.petclinic.specialization.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.specialization.service.SpecializationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * Project : spring-petclinic
 * File    : SpecializationController.java
 * Created : 2026-01-29
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적 : 특화클리닉 항목에 대한 라우팅(상세)을 담당한다.
 *
 * License :
 *   Copyright (c) 2026 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/specialization")
public class SpecializationController {

	private static final Logger log = LoggerFactory.getLogger(SpecializationController.class);
	private final SpecializationService specializationService;

	public SpecializationController(SpecializationService specializationService) {this.specializationService = specializationService;}

	/**
	 * 특화 클리닉 소개
	 * - 타입별 소개 페이지로 이동
	 */
	/**
	 * @param type 소개 페이지 타입
	 * @return 목록 템플릿
	 */
	@GetMapping("/{type}")
	public String specialization(@PathVariable("type") String type, Model model) {

		model.addAttribute("template", "specialization/"+type);

		return "fragments/layout";
	}
}
