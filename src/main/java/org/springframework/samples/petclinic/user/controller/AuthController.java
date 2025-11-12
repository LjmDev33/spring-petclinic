package org.springframework.samples.petclinic.user.controller;

import org.springframework.samples.petclinic.user.dto.UserRegisterDto;
import org.springframework.samples.petclinic.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Project : spring-petclinic
 * File    : AuthController.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 로그인/회원가입 컨트롤러
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String loginForm(Model model) {
		model.addAttribute("template", "user/login");
		return "fragments/layout";
	}

	@GetMapping("/register")
	public String registerForm(Model model) {
		model.addAttribute("template", "user/register");
		return "fragments/layout";
	}

	@PostMapping("/register")
	public String register(@ModelAttribute UserRegisterDto dto, RedirectAttributes redirectAttributes) {
		try {
			userService.register(dto);
			redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
			return "redirect:/login";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/register";
		}
	}

	/**
	 * 비밀번호 찾기 페이지
	 */
	@GetMapping("/forgot-password")
	public String forgotPasswordForm(Model model) {
		model.addAttribute("template", "user/forgot-password");
		return "fragments/layout";
	}

	/**
	 * 비밀번호 재설정 이메일 발송 (미구현)
	 * 향후 이메일 서비스 연동 시 구현 예정
	 */
	@PostMapping("/forgot-password")
	public String forgotPassword(String email, RedirectAttributes redirectAttributes) {
		try {
			// TODO: 이메일로 비밀번호 재설정 링크 발송
			// 현재는 임시 비밀번호 생성 및 안내 메시지만 표시
			redirectAttributes.addFlashAttribute("message",
				"비밀번호 찾기 기능은 준비 중입니다. 관리자에게 문의하세요.");
			return "redirect:/login";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/forgot-password";
		}
	}
}
