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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/*
 * Project : spring-petclinic
 * File    : CounselController.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 온라인상담 게시판에 대한 라우팅(목록 / 상세 / 작성 / 비밀번호 검증 / 댓글 작성·삭제 / 게시글 수정·삭제 / 조회수 처리)을 담당한다.
 *   구현현황:
 *     - 댓글 작성/삭제
 *     - 비공개 게시글 비밀번호 검증 및 세션 unlock 처리
 *     - 게시글 등록/수정/삭제(Soft Delete 정책 적용은 Service/Entity 레벨에서 처리)
 *     - 조회수 중복 방지(세션 + IP 기반)
 *   후속(미구현 또는 추가 고도화 대상):
 *     - 첨부파일 업로드 UI(Uppy 연동) 및 업로드 진행률 표시
 *     - 비공개 게시글 첨부파일 다운로드 권한 검증(관리자/작성자 구분, 세션 unlock 연계)
 *     - 관리자 권한 기반의 답변/댓글 UI 고도화(배지, 정렬, 필터 등)
 */
@Controller
@RequestMapping("/counsel")
public class CounselController {

	private static final Logger log = LoggerFactory.getLogger(CounselController.class);
	private final CounselService counselService;

	public CounselController(CounselService counselService) { this.counselService = counselService; }

	/**
	 * 온라인상담 목록 조회
	 * - 검색(type, keyword) 및 페이징(Pageable)을 지원한다.
	 * - 검색어가 있으면 QueryDSL 기반 검색, 없으면 단순 페이징 목록을 조회한다.
	 */
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

	/**
	 * 게시글 상세 조회
	 * - 비공개 글이고 아직 세션에서 unlock 되지 않았다면 비밀번호 입력 화면으로 리다이렉트한다.
	 * - 세션, 클라이언트 IP, 쿠키(24시간 유지)를 이용해 조회수 중복 증가를 방지한다.
	 * - 게시글 상세 및 댓글 목록을 모델에 담아 상세 화면 템플릿을 렌더링한다.
	 */
	@GetMapping("/detail/{id}")
	public String detail(@PathVariable Long id, Model model,
				   @SessionAttribute(value = "counselUnlocked", required = false) java.util.Set<Long> unlocked,
				   jakarta.servlet.http.HttpSession session,
				   jakarta.servlet.http.HttpServletRequest request,
				   jakarta.servlet.http.HttpServletResponse response) {
		CounselPostDto post;
		try {
			post = counselService.getDetail(id);
		} catch (Exception e) {
			log.error("Failed to load post detail: id={}", id, e);
			model.addAttribute("error", "게시글을 불러오는 중 오류가 발생했습니다.");
			return "error";
		}
		boolean unlockedOk = unlocked != null && unlocked.contains(id);
		if (post.isSecret() && !unlockedOk) {
			return "redirect:/counsel/detail/" + id + "/password";
		}

		// 조회수 중복 방지: 세션 + IP + 쿠키(24시간) 기반
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

		// 3. 쿠키 기반 중복 방지 (24시간 유지, 세션 만료 후에도 유효)
		String cookieName = "counsel_view_" + id;
		boolean viewedByCookie = false;

		jakarta.servlet.http.Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (jakarta.servlet.http.Cookie cookie : cookies) {
				if (cookieName.equals(cookie.getName())) {
					viewedByCookie = true;
					break;
				}
			}
		}

		// 4. 세션, IP, 쿠키 모두 확인하여 중복 조회 방지
		if (!viewedPosts.contains(id) && !viewedByIp.contains(viewKey) && !viewedByCookie) {
			counselService.incrementViewCount(id);
			viewedPosts.add(id);
			viewedByIp.add(viewKey);
			session.setAttribute("viewedCounselPosts", viewedPosts);
			session.setAttribute("viewedCounselPostsByIp", viewedByIp);

			// 쿠키 생성 (24시간 유지)
			jakarta.servlet.http.Cookie viewCookie = new jakarta.servlet.http.Cookie(cookieName, "viewed");
			viewCookie.setMaxAge(24 * 60 * 60); // 24시간 (초 단위)
			viewCookie.setPath("/"); // 전체 경로에서 유효
			viewCookie.setHttpOnly(true); // XSS 방지
			response.addCookie(viewCookie);

			log.info("View count incremented: postId={}, clientIp={}, cookie created", id, clientIp);
		} else {
			log.debug("View count NOT incremented (already viewed): postId={}, session={}, ip={}, cookie={}",
				id, viewedPosts.contains(id), viewedByIp.contains(viewKey), viewedByCookie);
		}

		java.util.List<CounselCommentDto> comments = counselService.getCommentsForPost(id);
		model.addAttribute("post", post);
		model.addAttribute("comments", comments);
		model.addAttribute("template", "counsel/counselDetail");
		return "fragments/layout";
	}

	/**
	 * 비공개 게시글에 접근할 때 사용하는 비밀번호 입력 화면 렌더링
	 * - 비공개 게시글 정보를 조회하여 제목 등을 함께 노출한다.
	 * - 이전 비밀번호 검증 실패 여부(fail 플래그)를 모델에 전달한다.
	 */
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

	/**
	 * 비공개 게시글 비밀번호 검증 및 unlock 처리
	 * - 비밀번호가 일치하면 세션에 해당 게시글 ID를 unlock 목록으로 저장하고 상세 화면으로 이동한다.
	 * - 비밀번호가 틀리면 비밀번호 입력 화면으로 다시 리다이렉트하고 실패 플래그를 전달한다.
	 */
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

	/**
	 * 게시글 작성 폼 진입
	 * - 신규 온라인상담 글 등록을 위한 입력 화면을 렌더링한다.
	 */
	@GetMapping("/write")
	public String writeForm(Model model){
		model.addAttribute("template", "counsel/counsel-write");
		return "fragments/layout";
	}

	/**
	 * Uppy 임시 파일 업로드 엔드포인트
	 * - Uppy Dashboard에서 파일 업로드 시 호출되는 REST API
	 * - 파일을 임시 저장하고 파일 ID 목록을 JSON 응답으로 반환
	 * - 실제 게시글 등록 시 attachmentIds로 전달받아 연결
	 *
	 * @param files 업로드된 파일 배열
	 * @return JSON 응답 (파일 ID 목록)
	 */
	@PostMapping("/upload-temp")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> uploadTemp(@RequestParam("files") MultipartFile[] files) {
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> uploadedFiles = new ArrayList<>();

		try {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					// 파일 저장 및 경로 반환
					String filePath = counselService.storeFileTemp(file);

					Map<String, Object> fileInfo = new HashMap<>();
					fileInfo.put("id", filePath); // 파일 경로를 ID로 사용
					fileInfo.put("name", file.getOriginalFilename());
					fileInfo.put("size", file.getSize());
					fileInfo.put("path", filePath);

					uploadedFiles.add(fileInfo);

					log.info("Temp file uploaded: name={}, size={}, path={}",
						file.getOriginalFilename(), file.getSize(), filePath);
				}
			}

			response.put("success", true);
			response.put("files", uploadedFiles);
			response.put("message", uploadedFiles.size() + "개 파일이 업로드되었습니다.");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Temp file upload failed: {}", e.getMessage(), e);

			response.put("success", false);
			response.put("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());

			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * 게시글 등록 처리
	 * - 사용자가 작성한 CounselPostWriteDto를 기반으로 신규 게시글을 저장한다.
	 * - 저장 완료 후 상세 화면으로 리다이렉트한다.
	 */
	@PostMapping
	public String submit(@ModelAttribute CounselPostWriteDto form) throws IOException {
		Long id = counselService.saveNew(form);
		return "redirect:/counsel/detail/" + id;
	}

	/**
	 * 댓글 등록 처리
	 * - 사용자가 입력한 댓글 내용을 Jsoup Safelist로 필터링하여 XSS를 방지한다.
	 * - 해당 게시글(postId)에 대한 댓글을 생성하고, 실패 시 오류 로그 및 에러 메시지를 플래시에 담는다.
	 */
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

	/**
	 * 댓글 삭제 처리
	 * - 비밀번호가 필요한 댓글의 경우 비밀번호 검증 후 Soft Delete를 수행한다.
	 * - 관리자 댓글 또는 권한이 없는 경우 삭제가 거부되고 에러 메시지를 반환한다.
	 */
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

	/**
	 * 게시글 수정 폼 진입
	 * - 비공개 게시글의 경우 세션 unlock 여부를 확인해 잠금 해제되지 않았다면 비밀번호 입력 화면으로 이동한다.
	 * - 수정 대상 게시글 정보를 조회하여 수정 화면 템플릿에 전달한다.
	 */
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

	/**
	 * 게시글 수정 처리
	 * - 비공개 게시글의 경우 비밀번호 검증 후 수정 가능하며, 공개 게시글은 비밀번호 없이 수정 가능하도록 Service에 위임한다.
	 * - 수정 성공/실패 여부에 따라 플래시 메시지를 설정하고 상세 화면으로 리다이렉트한다.
	 */
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

	/**
	 * 게시글 삭제 처리
	 * - Service 레벨에서 Soft Delete 정책을 적용하여 del_flag를 설정하고, 실제 물리 삭제는 스케줄러가 담당한다.
	 * - 비공개 게시글의 경우 비밀번호 검증 후 삭제 가능하며, 결과에 따라 플래시 메시지를 설정한다.
	 */
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
	 * 클라이언트 IP 추출 유틸리티 메서드
	 * - Proxy / Load Balancer 환경을 고려하여 X-Forwarded-For, Proxy-Client-IP 등 여러 헤더를 우선 확인한다.
	 * - 여러 IP가 존재하는 경우 첫 번째 IP를 실제 클라이언트 IP로 사용한다.
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
