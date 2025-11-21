package org.springframework.samples.petclinic.user.controller;

import org.springframework.samples.petclinic.user.dto.UserRegisterDto;
import org.springframework.samples.petclinic.user.service.PasswordResetService;
import org.springframework.samples.petclinic.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Project : spring-petclinic
 * File    : AuthController.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 로그인/회원가입/비밀번호 찾기 컨트롤러
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
public class AuthController {

	private final UserService userService;
	private final PasswordResetService passwordResetService;

	public AuthController(UserService userService, PasswordResetService passwordResetService) {
		this.userService = userService;
		this.passwordResetService = passwordResetService;
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
	 * 비밀번호 재설정 토큰 생성 및 이메일 발송
	 * TODO: 이메일 발송 기능 연동 (현재는 콘솔 로그로 토큰 확인)
	 */
	@PostMapping("/forgot-password")
	public String forgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
		try {
			// 토큰 생성
			String token = passwordResetService.createPasswordResetToken(email);

			// TODO: 이메일 발송 기능 추가 시 토큰이 포함된 링크 전송
			// 예: http://localhost:8080/reset-password?token={token}

			// 임시: 개발 환경에서는 토큰을 콘솔에 출력 (실제 운영에서는 제거)
			System.out.println("==============================================");
			System.out.println("비밀번호 재설정 토큰: " + token);
			System.out.println("링크: http://localhost:8080/reset-password?token=" + token);
			System.out.println("==============================================");

			redirectAttributes.addFlashAttribute("message",
				"비밀번호 재설정 링크가 이메일로 발송되었습니다. (개발 환경: 콘솔 확인)");
			return "redirect:/login";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/forgot-password";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "오류가 발생했습니다. 다시 시도해주세요.");
			return "redirect:/forgot-password";
		}
	}

	/**
	 * 비밀번호 재설정 페이지 (토큰 검증)
	 */
	@GetMapping("/reset-password")
	public String resetPasswordForm(@RequestParam String token, Model model, RedirectAttributes redirectAttributes) {
		// 토큰 검증
		if (!passwordResetService.validateToken(token)) {
			redirectAttributes.addFlashAttribute("error", "유효하지 않거나 만료된 링크입니다.");
			return "redirect:/forgot-password";
		}

		// 사용자 이메일 조회 (확인용)
		String email = passwordResetService.getUserEmailByToken(token);

		model.addAttribute("token", token);
		model.addAttribute("email", email);
		model.addAttribute("template", "user/reset-password");
		return "fragments/layout";
	}

	/**
	 * 비밀번호 재설정 처리
	 */
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,
								@RequestParam String password,
								@RequestParam String confirmPassword,
								RedirectAttributes redirectAttributes) {
		try {
			// 비밀번호 확인
			if (!password.equals(confirmPassword)) {
				redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
				return "redirect:/reset-password?token=" + token;
			}

			// 비밀번호 길이 검증
			if (password.length() < 8) {
				redirectAttributes.addFlashAttribute("error", "비밀번호는 8자 이상이어야 합니다.");
				return "redirect:/reset-password?token=" + token;
			}

			// 비밀번호 재설정
			boolean success = passwordResetService.resetPassword(token, password);

			if (success) {
				redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인하세요.");
				return "redirect:/login";
			} else {
				redirectAttributes.addFlashAttribute("error", "비밀번호 재설정에 실패했습니다. 다시 시도해주세요.");
				return "redirect:/forgot-password";
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "오류가 발생했습니다: " + e.getMessage());
			return "redirect:/forgot-password";
		}
	}
}
