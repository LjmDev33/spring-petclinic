package org.springframework.samples.petclinic.counsel.controller;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.dto.CounselCommentDto;
import org.springframework.samples.petclinic.counsel.dto.CounselPostDto;
import org.springframework.samples.petclinic.counsel.dto.CounselPostWriteDto;
import org.springframework.samples.petclinic.counsel.service.CounselService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/*
 * Project : spring-petclinic
 * File    : CounselController.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 온라인상담 라우팅(목록/상세/작성/검증)
 *   미구현(후속): 댓글 작성/삭제, 첨부 업로드/서빙, 관리자 권한 제어
 */
@Controller
@RequestMapping("/counsel")
public class CounselController {

	private static final Logger log = LoggerFactory.getLogger(CounselController.class);
	private final CounselService counselService;

	public CounselController(CounselService counselService) { this.counselService = counselService; }

	/** 목록 조회(검색/페이징) */
	@GetMapping("/list")
	public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
				   @RequestParam(value = "type", required = false) String type,
				   @RequestParam(value = "keyword", required = false) String keyword,
				   Model model) {

		PageResponse<CounselPostDto> pageResponse;

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
		model.addAttribute("template", "counsel/counselList");

		return "fragments/layout";
	}

	/** 상세 보기: 비공개글은 unlock 전이면 비밀번호 입력 페이지로 이동 */
	@GetMapping("/detail/{id}")
	public String detail(@PathVariable Long id, Model model,
					   @SessionAttribute(value = "counselUnlocked", required = false) java.util.Set<Long> unlocked,
					   jakarta.servlet.http.HttpSession session,
					   jakarta.servlet.http.HttpServletRequest request) throws IOException {
		CounselPostDto post = counselService.getDetail(id);
		boolean unlockedOk = unlocked != null && unlocked.contains(id);
		if (post.isSecret() && !unlockedOk) {
			return "redirect:/counsel/detail/" + id + "/password";
		}

		// 조회수 중복 방지: 세션 + IP 기반
		// 1. 세션 기반 중복 방지
		@SuppressWarnings("unchecked")
		java.util.Set<Long> viewedPosts = (java.util.Set<Long>) session.getAttribute("viewedCounselPosts");
		if (viewedPosts == null) {
			viewedPosts = new java.util.HashSet<>();
		}

		// 2. IP 기반 중복 방지 (세션과 함께 사용)
		String clientIp = getClientIp(request);
		String viewKey = id + "_" + clientIp;

		@SuppressWarnings("unchecked")
		java.util.Set<String> viewedByIp = (java.util.Set<String>) session.getAttribute("viewedCounselPostsByIp");
		if (viewedByIp == null) {
			viewedByIp = new java.util.HashSet<>();
		}

		// 3. 세션에도 없고 IP+게시글 조합으로도 조회하지 않았으면 조회수 증가
		if (!viewedPosts.contains(id) && !viewedByIp.contains(viewKey)) {
			counselService.incrementViewCount(id);
			viewedPosts.add(id);
			viewedByIp.add(viewKey);
			session.setAttribute("viewedCounselPosts", viewedPosts);
			session.setAttribute("viewedCounselPostsByIp", viewedByIp);

			log.info("View count incremented: postId={}, clientIp={}", id, clientIp);
		}

		java.util.List<CounselCommentDto> comments = counselService.getCommentsForPost(id);
		model.addAttribute("post", post);
		model.addAttribute("comments", comments);
		model.addAttribute("template", "counsel/counselDetail");
		return "fragments/layout";
	}

	/** 비밀번호 입력 화면: 비공개 글 접근 시 렌더링 */
	@GetMapping("/detail/{id}/password")
	public String passwordPage(@PathVariable Long id,
							 @RequestParam(value = "fail", required = false) String fail,
							 Model model) throws IOException {
		CounselPostDto post = counselService.getDetail(id);
		model.addAttribute("post", post);
		model.addAttribute("fail", fail != null);
		model.addAttribute("template", "counsel/counsel-password");
		return "fragments/layout";
	}

	/** 비공개글 unlock 처리 */
	@PostMapping("/detail/{id}/unlock")
	public String unlock(@PathVariable Long id, @RequestParam("password") String password,
					   jakarta.servlet.http.HttpSession session) {
		if (counselService.verifyPassword(id, password)) {
			@SuppressWarnings("unchecked")
			java.util.Set<Long> set = (java.util.Set<Long>) session.getAttribute("counselUnlocked");
			if (set == null) { set = new java.util.HashSet<>(); }
			set.add(id);
			session.setAttribute("counselUnlocked", set);
			return "redirect:/counsel/detail/" + id;
		}
		return "redirect:/counsel/detail/" + id + "/password?fail=1";
	}

	/** 글쓰기 폼 */
	@GetMapping("/write")
	public String writeForm(Model model){
		model.addAttribute("template", "counsel/counsel-write");
		return "fragments/layout";
	}

	/** 글 등록 */
	@PostMapping
	public String submit(@ModelAttribute CounselPostWriteDto form) throws IOException {
		Long id = counselService.saveNew(form);
		return "redirect:/counsel/detail/" + id;
	}

	/** 댓글 등록 */
	@PostMapping("/detail/{postId}/comments")
	public String submitComment(@PathVariable Long postId, @ModelAttribute CounselCommentDto commentDto, RedirectAttributes redirectAttributes) {
		try {
			// Jsoup을 사용한 HTML 필터링
			String sanitizedContent = Jsoup.clean(commentDto.getContent(), Safelist.basic());
			commentDto.setContent(sanitizedContent);

			counselService.createComment(postId, commentDto);
		} catch (Exception e) {
			log.error("Error creating comment: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "댓글 작성에 실패했습니다.");
		}
		return "redirect:/counsel/detail/" + postId;
	}

	/** 댓글 삭제 */
	@PostMapping("/detail/{postId}/comments/{commentId}/delete")
	public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId,
								@RequestParam(value = "password", required = false) String password,
								RedirectAttributes redirectAttributes) {
		try {
			boolean deleted = counselService.deleteComment(commentId, password);
			if (deleted) {
				redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
			} else {
				redirectAttributes.addFlashAttribute("error", "댓글을 삭제할 수 없습니다. 비밀번호를 확인하거나 관리자 댓글인지 확인하세요.");
			}
		} catch (Exception e) {
			log.error("Error deleting comment: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "댓글 삭제 중 오류가 발생했습니다.");
		}
		return "redirect:/counsel/detail/" + postId;
	}

	/** 게시글 수정 폼 */
	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable Long id, Model model,
						 @SessionAttribute(value = "counselUnlocked", required = false) java.util.Set<Long> unlocked) throws IOException {
		CounselPostDto post = counselService.getDetail(id);
		boolean unlockedOk = unlocked != null && unlocked.contains(id);
		if (post.isSecret() && !unlockedOk) {
			return "redirect:/counsel/detail/" + id + "/password";
		}
		model.addAttribute("post", post);
		model.addAttribute("template", "counsel/counsel-edit");
		return "fragments/layout";
	}

	/** 게시글 수정 처리 */
	@PostMapping("/edit/{id}")
	public String updatePost(@PathVariable Long id, @ModelAttribute CounselPostWriteDto form,
						   @RequestParam(value = "password", required = false) String password,
						   RedirectAttributes redirectAttributes) {
		try {
			boolean updated = counselService.updatePost(id, form, password);
			if (updated) {
				redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
			} else {
				redirectAttributes.addFlashAttribute("error", "게시글을 수정할 수 없습니다. 비밀번호를 확인하세요.");
			}
		} catch (Exception e) {
			log.error("Error updating post: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "게시글 수정 중 오류가 발생했습니다.");
		}
		return "redirect:/counsel/detail/" + id;
	}

	/** 게시글 삭제 처리 */
	@PostMapping("/delete/{id}")
	public String deletePost(@PathVariable Long id,
						   @RequestParam(value = "password", required = false) String password,
						   RedirectAttributes redirectAttributes) {
		try {
			boolean deleted = counselService.deletePost(id, password);
			if (deleted) {
				redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
				return "redirect:/counsel/list";
			} else {
				redirectAttributes.addFlashAttribute("error", "게시글을 삭제할 수 없습니다. 비밀번호를 확인하세요.");
			}
		} catch (Exception e) {
			log.error("Error deleting post: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "게시글 삭제 중 오류가 발생했습니다.");
		}
		return "redirect:/counsel/detail/" + id;
	}

	/**
	 * 클라이언트 IP 추출
	 * - Proxy, Load Balancer 환경 고려
	 * - X-Forwarded-For 헤더 우선 확인
	 *
	 * @param request HttpServletRequest
	 * @return 클라이언트 IP 주소
	 */
	private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		// X-Forwarded-For에 여러 IP가 있을 경우 첫 번째 IP 사용
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}

		return ip;
	}
}
