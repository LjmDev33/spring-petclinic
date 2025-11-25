package org.springframework.samples.petclinic.community.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;
import org.springframework.samples.petclinic.community.service.CommunityService;
import org.springframework.security.access.prepost.PreAuthorize;
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
 *   TODO: 커뮤니티 요청 컨트롤러
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

	@GetMapping("/list")
	public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
					   @RequestParam(value = "type", required = false) String type,
					   @RequestParam(value = "keyword", required = false) String keyword,
					   @RequestParam(value = "subject", required = true) String subject,
					   Model model) {

		PageResponse<CommunityPostDto> pageResponse;

		type = (type == null || type.isBlank()) ? "" : type;

		if(keyword != null && !keyword.isBlank()) {
			pageResponse = communityService.search(type,keyword,pageable);
		}else{
			pageResponse = communityService.getPagedPosts(pageable);
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

	@GetMapping("detail/{id}")
	public String detail(@PathVariable("id") Long id,
						 @RequestParam(value = "subject", required = true) String subject,
						 Model model) {
		log.info("### detail called");

		model.addAttribute("post", communityService.getPost(id));
		model.addAttribute("prevPost", communityService.getPrevPost(id).orElse(null));
		model.addAttribute("nextPost", communityService.getNextPost(id).orElse(null));
		model.addAttribute("subject", subject);
		if (subject.equalsIgnoreCase("notice")) {
			model.addAttribute("template", "community/noticeDetail");
		}

		return "fragments/layout";
	}

	/**
	 * 글쓰기 화면 표시
	 * - 관리자만 접근 가능
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/write")
	public String writeForm(@RequestParam(value = "subject", required = false, defaultValue = "notice") String subject,
							Model model) {
		log.info("### write form called: subject={}", subject);

		model.addAttribute("subject", subject);
		if (subject.equalsIgnoreCase("notice")) {
			model.addAttribute("template", "community/noticeWrite");
		}

		return "fragments/layout";
	}

	/**
	 * 글쓰기 저장 처리
	 * - 관리자만 접근 가능
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/write")
	public String create(@ModelAttribute CommunityPostDto postDto,
						 @RequestParam(value = "subject", required = false, defaultValue = "notice") String subject) {
		log.info("### create called: subject={}, title={}", subject, postDto.getTitle());

		communityService.createPost(postDto);

		return "redirect:/community/list?subject=" + subject;
	}
}
