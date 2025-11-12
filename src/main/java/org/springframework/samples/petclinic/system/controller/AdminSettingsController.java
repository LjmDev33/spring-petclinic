package org.springframework.samples.petclinic.system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.system.service.SystemConfigService;
import org.springframework.samples.petclinic.system.table.SystemConfig;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Project : spring-petclinic
 * File    : AdminSettingsController.java
 * Created : 2025-11-11
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 관리자 시스템 설정 관리
 *   연관 기능:
 *     - 시스템 설정 조회 (멀티로그인, 파일 업로드 등)
 *     - 시스템 설정 수정 (관리자 전용)
 *     - 설정 변경 이력 로그 기록
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/admin/settings")
@PreAuthorize("hasRole('ADMIN')")  // 관리자만 접근 가능
public class AdminSettingsController {

	private static final Logger log = LoggerFactory.getLogger(AdminSettingsController.class);
	private final SystemConfigService systemConfigService;

	public AdminSettingsController(SystemConfigService systemConfigService) {
		this.systemConfigService = systemConfigService;
	}

	/**
	 * 시스템 설정 관리 페이지
	 * - 관리자만 접근 가능
	 * - 모든 활성화된 설정 조회
	 *
	 * @param model 뷰 모델
	 * @return 설정 관리 페이지
	 */
	@GetMapping
	public String settingsPage(Model model) {
		log.info("Admin settings page accessed");

		// 활성화된 설정 목록 조회
		List<SystemConfig> configs = systemConfigService.getActiveConfigs();

		model.addAttribute("configs", configs);
		model.addAttribute("template", "admin/settings");
		return "fragments/layout";
	}

	/**
	 * 시스템 설정 업데이트
	 * - 관리자만 실행 가능
	 * - 설정 변경 시 로그 기록
	 *
	 * @param key 설정 키
	 * @param value 설정 값
	 * @param authentication Spring Security 인증 정보
	 * @param redirectAttributes Flash 메시지
	 * @return 설정 페이지로 리다이렉트
	 */
	@PostMapping("/update")
	public String updateSetting(
		@RequestParam String key,
		@RequestParam String value,
		Authentication authentication,
		RedirectAttributes redirectAttributes) {

		try {
			String username = authentication.getName();
			systemConfigService.updateConfig(key, value, username);

			log.info("System setting updated successfully: key={}, value={}, updatedBy={}",
					 key, value, username);

			redirectAttributes.addFlashAttribute("message",
				String.format("설정이 업데이트되었습니다: %s = %s", key, value));
		} catch (Exception e) {
			log.error("Failed to update system setting: key={}, error={}", key, e.getMessage());
			redirectAttributes.addFlashAttribute("error",
				"설정 업데이트 실패: " + e.getMessage());
		}

		return "redirect:/admin/settings";
	}

	/**
	 * 멀티로그인 설정 토글
	 * - true/false 토글
	 *
	 * @param authentication Spring Security 인증 정보
	 * @param redirectAttributes Flash 메시지
	 * @return 설정 페이지로 리다이렉트
	 */
	@PostMapping("/toggle-multi-login")
	public String toggleMultiLogin(
		Authentication authentication,
		RedirectAttributes redirectAttributes) {

		try {
			String username = authentication.getName();
			boolean currentValue = systemConfigService.isMultiLoginEnabled();
			String newValue = String.valueOf(!currentValue);

			systemConfigService.updateConfig("multiLoginEnabled", newValue, username);

			String status = currentValue ? "비활성화" : "활성화";
			log.info("Multi-login setting toggled: {} by {}", status, username);

			redirectAttributes.addFlashAttribute("message",
				"멀티로그인 설정이 " + status + "되었습니다.");
		} catch (Exception e) {
			log.error("Failed to toggle multi-login setting: error={}", e.getMessage());
			redirectAttributes.addFlashAttribute("error",
				"멀티로그인 설정 변경 실패: " + e.getMessage());
		}

		return "redirect:/admin/settings";
	}
}

