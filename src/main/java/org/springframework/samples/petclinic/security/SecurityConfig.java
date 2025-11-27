package org.springframework.samples.petclinic.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.security.handler.CustomAuthenticationSuccessHandler;
import org.springframework.samples.petclinic.user.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

/**
 * Project : spring-petclinic
 * File    : SecurityConfig.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: Spring Security 설정
 *   - 로그인/로그아웃 설정
 *   - Remember-Me (자동 로그인) 설정
 *   - 권한별 접근 제어
 *   - 멀티로그인 제어 (시스템 설정 기반)
 *   연관 기능: 로그인, 회원가입, 권한 관리
 *   미구현: OAuth2 소셜 로그인
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final CustomAuthenticationSuccessHandler successHandler;
	private final DataSource dataSource;
	private final org.springframework.samples.petclinic.system.service.SystemConfigService systemConfigService;

	public SecurityConfig(CustomUserDetailsService userDetailsService,
						  CustomAuthenticationSuccessHandler successHandler,
						  DataSource dataSource,
						  org.springframework.samples.petclinic.system.service.SystemConfigService systemConfigService) {
		this.userDetailsService = userDetailsService;
		this.successHandler = successHandler;
		this.dataSource = dataSource;
		this.systemConfigService = systemConfigService;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				// 공개 리소스
				.requestMatchers("/", "/welcome", "/css/**", "/fonts/**", "/images/**", "/webjars/**", "/js/**").permitAll()
				// 회원가입, 로그인 페이지
				.requestMatchers("/login", "/register", "/forgot-password", "/reset-password").permitAll()
				// 커뮤니티 (공개)
				.requestMatchers("/community/list", "/community/detail/**").permitAll()
				// 온라인상담 (공개, 단 비공개 글은 비밀번호 검증)
				.requestMatchers("/counsel/list", "/counsel/detail/**", "/counsel/write", "/counsel").permitAll()
				.requestMatchers("/counsel/download/**").permitAll()
				// FAQ (공개)
				.requestMatchers("/faq", "/faq/list", "/faq/detail/**").permitAll()
				// 포토게시판 (공개)
				.requestMatchers("/photo/list", "/photo/detail/**").permitAll()
				// 관리자 전용
				.requestMatchers("/admin/**").hasRole("ADMIN")
				// 나머지는 인증 필요
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.usernameParameter("username")
				.passwordParameter("password")
				.successHandler(successHandler)
				.failureUrl("/login?error=true")
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/?logout=true")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID", "remember-me", "savedUsername")
				.permitAll()
			)
			// Remember-Me (자동 로그인) 설정
			.rememberMe(remember -> remember
				.key("petclinic-remember-me-key")
				.tokenValiditySeconds(7 * 24 * 60 * 60) // 7일
				.userDetailsService(userDetailsService)
				.rememberMeParameter("remember-me")
				.rememberMeCookieName("remember-me")
			)
			// Phase 4-3: 멀티 로그인 제어 (SystemConfig 기반 동적 설정)
			.sessionManagement(session -> {
				// 시스템 설정에서 멀티로그인 허용 여부 조회
				boolean multiLoginEnabled = systemConfigService.isMultiLoginEnabled();
				int maxSessions = multiLoginEnabled ? 5 : 1; // 멀티로그인: 최대 5개, 단일로그인: 1개

				session.maximumSessions(maxSessions)
					.maxSessionsPreventsLogin(false); // false: 기존 세션 만료, true: 신규 로그인 차단
			});

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
