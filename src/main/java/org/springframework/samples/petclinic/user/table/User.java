package org.springframework.samples.petclinic.user.table;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.samples.petclinic.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Project : spring-petclinic
 * File    : User.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 사용자 정보 저장 (Spring Security 연동)
 *   - 로그인/회원가입 기능
 *   - 권한 관리 (USER, ADMIN)
 *   - 아이디 저장, 자동 로그인 지원
 *   연관 기능: 로그인, 회원가입, 권한 관리
 *   미구현: 소셜 로그인 (OAuth2)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

	@Column(nullable = false, unique = true, length = 50)
	private String username; // 사용자 ID (로그인용)

	@Column(nullable = false, length = 100)
	private String password; // 비밀번호 (BCrypt 해시)

	@Column(nullable = false, length = 100)
	private String email; // 이메일

	@Column(nullable = false, length = 50)
	private String name; // 이름

	@Column(nullable = false, unique = true, length = 30)
	private String nickname; // 닉네임 (게시판 표시용, 중복 불가)

	@Column(length = 20)
	private String phone; // 전화번호 (010-0000-0000 형식)

	@Column(nullable = false)
	private boolean enabled = true; // 계정 활성화 여부

	@Column(nullable = false)
	private boolean accountNonExpired = true; // 계정 만료 여부

	@Column(nullable = false)
	private boolean accountNonLocked = true; // 계정 잠금 여부

	@Column(nullable = false)
	private boolean credentialsNonExpired = true; // 비밀번호 만료 여부

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role")
	private Set<String> roles = new HashSet<>(); // 권한 (ROLE_USER, ROLE_ADMIN)

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt; // 생성 일시

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt; // 수정 일시

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt; // 마지막 로그인 일시

	@Column(name = "last_login_ip", length = 50)
	private String lastLoginIp; // 마지막 로그인 IP

	// Getters and Setters
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(LocalDateTime lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	/**
	 * 관리자 권한 여부 확인
	 * @return true: 관리자, false: 일반 사용자
	 */
	public boolean isAdmin() {
		return roles.contains("ROLE_ADMIN");
	}
}

