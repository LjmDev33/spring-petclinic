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

		// 좋아요 정보 추가
		long likeCount = photoService.getLikeCount(id);
		org.springframework.security.core.Authentication authentication =
			org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		boolean isLiked = photoService.isLikedByUser(id, authentication);

		model.addAttribute("post", post);
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("isLiked", isLiked);
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
	 * 게시글 삭제
	 */
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Long id) {
		log.info("포토게시글 삭제: ID={}", id);

		photoService.deletePost(id);

		return "redirect:/photo/list";
	}

	/**
	 * 게시글 좋아요 토글 (AJAX API)
	 *
	 * <p>Phase 2-3: 포토게시판 좋아요 기능</p>
	 *
	 * Purpose:
	 * - 로그인한 사용자만 좋아요를 누를 수 있다.
	 * - 이미 좋아요를 눌렀으면 취소하고, 안 눌렀으면 추가한다.
	 * - JSON 응답으로 좋아요 상태와 개수를 반환한다.
	 *
	 * @param id 게시글 ID
	 * @param authentication Spring Security 인증 객체
	 * @return JSON 응답 { success, liked, likeCount, message }
	 */
	@PostMapping("/detail/{id}/like")
	@ResponseBody
	public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> toggleLike(
		@PathVariable Long id,
		org.springframework.security.core.Authentication authentication) {

		java.util.Map<String, Object> response = new java.util.HashMap<>();

		try {
			// 좋아요 토글
			boolean liked = photoService.toggleLike(id, authentication);

			// 좋아요 개수 조회
			long likeCount = photoService.getLikeCount(id);

			response.put("success", true);
			response.put("liked", liked);
			response.put("likeCount", likeCount);
			response.put("message", liked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");

			log.info("Photo Like toggled: postId={}, username={}, liked={}",
				id, authentication != null ? authentication.getName() : "anonymous", liked);

			return org.springframework.http.ResponseEntity.ok(response);
		} catch (IllegalStateException e) {
			// 비로그인 사용자
			log.warn("Unauthorized photo like attempt: postId={}", id);
			response.put("success", false);
			response.put("error", e.getMessage());
			return org.springframework.http.ResponseEntity.status(401).body(response);
		} catch (Exception e) {
			log.error("Error toggling photo like: postId={}, error={}", id, e.getMessage(), e);
			response.put("success", false);
			response.put("error", "좋아요 처리 중 오류가 발생했습니다.");
			return org.springframework.http.ResponseEntity.badRequest().body(response);
		}
	}
}

