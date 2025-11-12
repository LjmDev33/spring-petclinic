package org.springframework.samples.petclinic.user.security;

import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Project : spring-petclinic
 * File    : CustomUserDetails.java
 * Created : 2025-11-11
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: Spring Security UserDetails 커스텀 구현
 *   연관 기능:
 *     - User 엔티티를 UserDetails로 변환
 *     - nickname 필드 제공 (Thymeleaf에서 접근 가능)
 *     - Spring Security 인증 정보 제공
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class CustomUserDetails implements UserDetails {

	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	/**
	 * 권한 목록 반환
	 * @return GrantedAuthority 컬렉션
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRoles().stream()
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());
	}

	/**
	 * 비밀번호 반환
	 * @return 암호화된 비밀번호
	 */
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	/**
	 * 사용자 아이디 반환
	 * @return username
	 */
	@Override
	public String getUsername() {
		return user.getUsername();
	}

	/**
	 * 계정 만료 여부
	 * @return true: 만료되지 않음
	 */
	@Override
	public boolean isAccountNonExpired() {
		return user.isAccountNonExpired();
	}

	/**
	 * 계정 잠금 여부
	 * @return true: 잠기지 않음
	 */
	@Override
	public boolean isAccountNonLocked() {
		return user.isAccountNonLocked();
	}

	/**
	 * 비밀번호 만료 여부
	 * @return true: 만료되지 않음
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return user.isCredentialsNonExpired();
	}

	/**
	 * 계정 활성화 여부
	 * @return true: 활성화됨
	 */
	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

	/**
	 * 닉네임 반환 (커스텀 필드)
	 * Thymeleaf에서 sec:authentication="principal.nickname" 접근 가능
	 *
	 * @return 닉네임
	 */
	public String getNickname() {
		return user.getNickname();
	}

	/**
	 * 이메일 반환 (커스텀 필드)
	 * @return 이메일
	 */
	public String getEmail() {
		return user.getEmail();
	}

	/**
	 * 이름 반환 (커스텀 필드)
	 * @return 이름
	 */
	public String getName() {
		return user.getName();
	}

	/**
	 * User 엔티티 반환
	 * @return User 엔티티
	 */
	public User getUser() {
		return user;
	}
}

