package org.springframework.samples.petclinic.counsel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.service.CounselService;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Project : spring-petclinic
 * File    : CounselController.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/counsel")
public class CounselController {

	private static final Logger log = LoggerFactory.getLogger(CounselController.class);

	private final CounselService counselService;

	public CounselController(CounselService counselService) {
		this.counselService = counselService;
	}

	@GetMapping("/list")
	public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
					   @RequestParam(value = "type", required = false) String type,
					   @RequestParam(value = "keyword", required = false) String keyword,
					   @RequestParam(value = "subject", required = true) String subject,
					   Model model) {

		PageResponse<CounselPost> pageResponse;

		type = (type == null || type.isBlank()) ? "" : type;

		if(keyword != null && !keyword.isBlank()) {
			pageResponse = counselService.search(type,keyword,pageable);
		}else{
			pageResponse = counselService.getPagedPosts(pageable);
		}

		model.addAttribute("page", pageResponse);
		model.addAttribute("posts", pageResponse.getContent());
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", type);
		model.addAttribute("subject", subject);
		if(subject.equalsIgnoreCase("notice")) {
			model.addAttribute("template", "community/noticeList");
		}

		return "fragments/layout";
	}
}
