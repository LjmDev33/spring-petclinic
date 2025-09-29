package org.springframework.samples.petclinic.community.controller;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;
import org.springframework.samples.petclinic.community.service.CommunityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/*
 * Project : spring-petclinic
 * File    : CommunityController.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/community")
public class CommunityController {

	private static final Logger log = LoggerFactory.getLogger(CommunityController.class);

	private final CommunityService communityService;

	public CommunityController(CommunityService communityService) {
		this.communityService = communityService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("posts", communityService.getAllPosts());
		model.addAttribute("template", "community/noticeList");
		log.info("### list called");
		return "/fragments/layout";
	}

	@GetMapping("/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		log.info("### detail called");
		model.addAttribute("post", communityService.getPost(id));
		return "community/detail";
	}

	@PostMapping
	public String create(@ModelAttribute CommunityPostDto postDto) {
		log.info("### create called");
		communityService.createPost(postDto);
		return "redirect:/community";
	}
}
