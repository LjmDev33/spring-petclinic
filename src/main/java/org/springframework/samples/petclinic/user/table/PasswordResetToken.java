package org.springframework.samples.petclinic.user.table;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 토큰 엔티티
 *
 * @author Jeongmin Lee
 * @description 사용자가 비밀번호를 잊었을 때, 이메일로 받은 토큰을 통해 비밀번호를 재설정할 수 있도록 하는 엔티티
 * 
 * 연관 기능:
 * - 비밀번호 찾기 요청 시 토큰 생성
 * - 이메일 발송 (향후 구현)
 * - 토큰 유효성 검증 (24시간)
 * - 비밀번호 재설정
 * - 만료된 토큰 자동 삭제 (스케줄러)
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

	/** 토큰 ID (자동 생성) */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 재설정 토큰 (UUID 기반, 고유값) */
	@Column(name = "token", nullable = false, unique = true, length = 100)
	private String token;

	/** 토큰과 연결된 사용자 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/** 토큰 생성 시각 */
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	/** 토큰 만료 시각 (생성 후 24시간) */
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	/** 토큰 사용 여부 (재설정 완료 시 true) */
	@Column(name = "used", nullable = false)
	private boolean used = false;

	// Constructors
	public PasswordResetToken() {
	}

	public PasswordResetToken(String token, User user, LocalDateTime expiresAt) {
		this.token = token;
		this.user = user;
		this.createdAt = LocalDateTime.now();
		this.expiresAt = expiresAt;
		this.used = false;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	/**
	 * 토큰이 만료되었는지 확인
	 * @return 만료되었으면 true, 유효하면 false
	 */
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt);
	}

	/**
	 * 토큰이 유효한지 확인 (사용되지 않았고 만료되지 않음)
	 * @return 유효하면 true, 그렇지 않으면 false
	 */
	public boolean isValid() {
		return !this.used && !isExpired();
	}
}
