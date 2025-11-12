package org.springframework.samples.petclinic.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.user.repository.UserRepository;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Project : spring-petclinic
 * File    : CustomUserDetailsService.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: Spring Security UserDetailsService 구현
 *   - 로그인 시 사용자 정보 조회
 *   - 권한 정보 로드
 *   연관 기능: 로그인, 권한 관리
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		log.info("User logged in: {}", username);

		// CustomUserDetails 반환 (nickname 필드 포함)
		return new org.springframework.samples.petclinic.user.security.CustomUserDetails(user);
	}


	/**
	 * 마지막 로그인 시간 업데이트
	 * @param username 사용자 ID
	 * @param ip IP 주소
	 */
	@Transactional
	public void updateLastLogin(String username, String ip) {
		userRepository.findByUsername(username).ifPresent(user -> {
			user.setLastLoginAt(LocalDateTime.now());
			user.setLastLoginIp(ip);
			userRepository.save(user);
			log.info("Updated last login for user: {} from IP: {}", username, ip);
		});
	}
}
