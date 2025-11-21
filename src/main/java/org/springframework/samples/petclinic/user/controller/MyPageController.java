package org.springframework.samples.petclinic.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.dto.CounselPostDto;
import org.springframework.samples.petclinic.counsel.table.CounselComment;
import org.springframework.samples.petclinic.user.service.UserService;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Project : spring-petclinic
 * File    : MyPageController.java
 * Created : 2025-11-11
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 마이페이지 (사용자 정보 수정)
 *   연관 기능:
 *     - 프로필 조회
 *     - 프로필 수정 (이메일, 이름, 닉네임, 전화번호)
 *     - 비밀번호 변경
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/mypage")
public class MyPageController {

	private final UserService userService;

	public MyPageController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 마이페이지 메인 화면
	 * - 로그인한 사용자의 정보 조회
	 *
	 * @param authentication Spring Security 인증 정보
	 * @param model 뷰 모델
	 * @return 마이페이지 템플릿
	 */
	@GetMapping
	public String myPage(Authentication authentication, Model model) {
		String username = authentication.getName();
		User user = userService.findByUsername(username);

		model.addAttribute("user", user);
		model.addAttribute("template", "user/mypage");
		return "fragments/layout";
	}

	/**
	 * 프로필 수정 처리
	 * - 이메일, 이름, 닉네임, 전화번호 수정 가능
	 *
	 * @param authentication Spring Security 인증 정보
	 * @param email 이메일
	 * @param name 이름
	 * @param nickname 닉네임
	 * @param phone 전화번호
	 * @param redirectAttributes Flash 메시지
	 * @return 마이페이지로 리다이렉트
	 */
	@PostMapping("/update")
	@ResponseBody
	public Map<String, Object> updateProfile(
		Authentication authentication,
		@RequestParam String email,
		@RequestParam String name,
		@RequestParam String nickname,
		@RequestParam(required = false) String phone) {

		Map<String, Object> response = new HashMap<>();
		try {
			String username = authentication.getName();
			userService.updateProfile(username, email, name, nickname, phone);
			response.put("success", true);
			response.put("message", "프로필이 수정되었습니다.");
			response.put("nickname", nickname);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", e.getMessage());
		}

		return response;
	}

	/**
	 * 비밀번호 변경 처리
	 * - 새 비밀번호 확인
	 * - BCrypt 암호화 후 저장
	 *
	 * @param authentication Spring Security 인증 정보
	 * @param newPassword 새 비밀번호
	 * @param confirmPassword 새 비밀번호 확인
	 * @param redirectAttributes Flash 메시지
	 * @return 마이페이지로 리다이렉트
	 */
	@PostMapping("/change-password")
	public String changePassword(
		Authentication authentication,
		@RequestParam String newPassword,
		@RequestParam String confirmPassword,
		RedirectAttributes redirectAttributes) {

		try {
			// 비밀번호 일치 확인
			if (!newPassword.equals(confirmPassword)) {
				redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
				return "redirect:/mypage";
			}

			// 비밀번호 길이 확인
			if (newPassword.length() < 8) {
				redirectAttributes.addFlashAttribute("error", "비밀번호는 8자 이상이어야 합니다.");
				return "redirect:/mypage";
			}

			String username = authentication.getName();
			userService.changePassword(username, newPassword);
			redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}

		return "redirect:/mypage";
	}

	/**
	 * 내가 작성한 온라인상담 게시글 목록
	 *
	 * @param authentication Spring Security 인증 정보
	 * @param page 페이지 번호 (0부터 시작)
	 * @param size 페이지 크기 (기본 10개)
	 * @param model 뷰 모델
	 * @return 내 게시글 목록 템플릿
	 */
	@GetMapping("/my-posts")
	public String myPosts(
		Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		Model model) {

		String username = authentication.getName();
		User user = userService.findByUsername(username);
		String nickname = user.getNickname();

		Pageable pageable = PageRequest.of(page, size);
		PageResponse<CounselPostDto> posts = userService.getMyPosts(nickname, pageable);

		model.addAttribute("posts", posts);
		model.addAttribute("currentPage", page);
		model.addAttribute("template", "user/my-posts");
		return "fragments/layout";
	}

	/**
	 * 내가 작성한 댓글 목록
	 *
	 * @param authentication Spring Security 인증 정보
	 * @param page 페이지 번호 (0부터 시작)
	 * @param size 페이지 크기 (기본 10개)
	 * @param model 뷰 모델
	 * @return 내 댓글 목록 템플릿
	 */
	@GetMapping("/my-comments")
	public String myComments(
		Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		Model model) {

		String username = authentication.getName();
		User user = userService.findByUsername(username);
		String nickname = user.getNickname();

		Pageable pageable = PageRequest.of(page, size);
		Page<CounselComment> comments = userService.getMyComments(nickname, pageable);

		model.addAttribute("comments", comments);
		model.addAttribute("currentPage", page);
		model.addAttribute("template", "user/my-comments");
		return "fragments/layout";
	}
}

