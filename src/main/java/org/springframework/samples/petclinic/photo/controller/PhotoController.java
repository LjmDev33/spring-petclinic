package org.springframework.samples.petclinic.photo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.photo.dto.PhotoPostDto;
import org.springframework.samples.petclinic.photo.service.PhotoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Project : spring-petclinic
 * File    : PhotoController.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 컨트롤러
 */
@Controller
@RequestMapping("/photo")
public class PhotoController {

	private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

	private final PhotoService photoService;

	public PhotoController(PhotoService photoService) {
		this.photoService = photoService;
	}

	/**
	 * 포토게시판 목록 (갤러리형)
	 */
	@GetMapping("/list")
	public String list(@PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
					   Model model) {
		log.info("포토게시판 목록 조회");

		PageResponse<PhotoPostDto> pageResponse = photoService.getPagedPosts(pageable);

		model.addAttribute("page", pageResponse);
		model.addAttribute("posts", pageResponse.getContent());
		model.addAttribute("template", "photo/photoList");

		return "fragments/layout";
	}

	/**
	 * 포토게시글 상세
	 */
	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		log.info("포토게시글 상세 조회: ID={}", id);

		PhotoPostDto post = photoService.getPost(id);

		model.addAttribute("post", post);
		model.addAttribute("template", "photo/photoDetail");

		return "fragments/layout";
	}

	/**
	 * 글쓰기 화면
	 */
	@GetMapping("/write")
	public String writeForm(Model model) {
		log.info("포토게시글 작성 화면");

		model.addAttribute("template", "photo/photoWrite");

		return "fragments/layout";
	}

	/**
	 * 글쓰기 저장
	 */
	@PostMapping("/write")
	public String write(@ModelAttribute PhotoPostDto postDto) {
		log.info("포토게시글 작성: 제목={}", postDto.getTitle());

		photoService.createPost(postDto);

		return "redirect:/photo/list";
	}

	/**
	 * 수정 화면
	 */
	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable("id") Long id, Model model) {
		log.info("포토게시글 수정 화면: ID={}", id);

		PhotoPostDto post = photoService.getPost(id);

		model.addAttribute("post", post);
		model.addAttribute("template", "photo/photoEdit");

		return "fragments/layout";
	}

	/**
	 * 수정 저장
	 */
	@PostMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, @ModelAttribute PhotoPostDto postDto) {
		log.info("포토게시글 수정: ID={}", id);

		photoService.updatePost(id, postDto);

		return "redirect:/photo/detail/" + id;
	}

	/**
	 * 삭제
	 */
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Long id) {
		log.info("포토게시글 삭제: ID={}", id);

		photoService.deletePost(id);

		return "redirect:/photo/list";
	}
}

