package org.springframework.samples.petclinic.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.user.service.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Project : spring-petclinic
 * File    : CustomAuthenticationSuccessHandler.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 로그인 성공 시 처리
 *   - 마지막 로그인 시간 업데이트
 *   - 로그인 IP 기록
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
	private final CustomUserDetailsService userDetailsService;

	public CustomAuthenticationSuccessHandler(CustomUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws IOException, ServletException {
		String username = authentication.getName();
		String ip = getClientIP(request);

		// 마지막 로그인 시간 업데이트
		userDetailsService.updateLastLogin(username, ip);

		log.info("Login success: {} from IP: {}", username, ip);

		// 로그인 후 리다이렉트
		response.sendRedirect("/");
	}

	/**
	 * 클라이언트 IP 주소 추출
	 */
	private String getClientIP(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
