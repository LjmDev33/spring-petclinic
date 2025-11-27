package org.springframework.samples.petclinic.user.dto;

/**
 * Project : spring-petclinic
 * File    : UserRegisterDto.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   회원가입 폼 DTO
 *
 * Purpose (만든 이유):
 *   1. HTML 회원가입 폼 데이터를 Controller에 전달
 *   2. 비밀번호 확인 필드 포함 (passwordConfirm)
 *   3. 닉네임 필드 포함 (게시판 표시용)
 *   4. 전화번호 자동 포맷 지원
 *   5. UserService에서 중복 검증 및 저장
 *
 * Key Fields (주요 필드):
 *   - username: 아이디 (로그인용, 중복 불가)
 *   - password: 비밀번호 (평문, 저장 시 BCrypt 해싱)
 *   - passwordConfirm: 비밀번호 확인
 *   - email: 이메일 (중복 불가)
 *   - name: 실명
 *   - nickname: 닉네임 (게시판 표시용, 중복 불가)
 *   - phone: 전화번호 (010-0000-0000 형식)
 *
 * Validation (검증):
 *   - UserService에서 수행:
 *     1. 아이디 중복 검증
 *     2. 이메일 중복 검증
 *     3. 닉네임 중복 검증
 *     4. 비밀번호 일치 검증 (Controller)
 *
 * Usage Examples (사용 예시):
 *   // Controller에서 회원가입 처리
 *   @PostMapping("/user/register")
 *   public String register(@ModelAttribute UserRegisterDto dto) {
 *       userService.register(dto);
 *       return "redirect:/login";
 *   }
 *
 *   // 비밀번호 확인 검증 (Controller)
 *   if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
 *       throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
 *   }
 *
 * Security (보안):
 *   - password는 평문으로 전달 (HTTPS 사용 권장)
 *   - UserService에서 BCrypt 해싱 후 저장
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class UserRegisterDto {
	private String username;
	private String password;
	private String passwordConfirm;
	private String email;
	private String name;
	private String nickname; // 게시판 표시용 닉네임
	private String phone;

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

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
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
}

