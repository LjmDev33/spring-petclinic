package org.springframework.samples.petclinic.user.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.user.repository.PasswordResetTokenRepository;
import org.springframework.samples.petclinic.user.repository.UserRepository;
import org.springframework.samples.petclinic.user.table.PasswordResetToken;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 비밀번호 재설정 서비스
 *
 * @author Jeongmin Lee
 * @description 비밀번호 찾기 및 재설정 기능 제공
 *
 * 주요 기능:
 * - 비밀번호 재설정 토큰 생성 (24시간 유효)
 * - 이메일 발송 (향후 구현)
 * - 토큰 검증
 * - 비밀번호 재설정
 */
@Service
@Transactional
public class PasswordResetService {

	private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
	private static final long TOKEN_EXPIRATION_HOURS = 24; // 토큰 유효 시간 (24시간)

	private final PasswordResetTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public PasswordResetService(PasswordResetTokenRepository tokenRepository,
								UserRepository userRepository,
								PasswordEncoder passwordEncoder) {
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * 비밀번호 재설정 토큰 생성
	 * - 이메일로 사용자 조회
	 * - 기존 미사용 토큰 삭제
	 * - 새 토큰 생성 (UUID 기반)
	 * - 토큰 저장
	 *
	 * @param email 사용자 이메일
	 * @return 생성된 토큰 문자열 (이메일 발송용)
	 * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
	 */
	public String createPasswordResetToken(String email) {
		// 1. 이메일로 사용자 조회
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

		// 2. 기존 미사용 토큰 삭제 (중복 방지)
		tokenRepository.deleteByUser_IdAndUsedFalse(user.getId());

		// 3. 새 토큰 생성
		String tokenString = UUID.randomUUID().toString();
		LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

		PasswordResetToken token = new PasswordResetToken(tokenString, user, expiresAt);
		tokenRepository.save(token);

		log.info("Password reset token created: email={}, token={}, expiresAt={}",
		         email, tokenString, expiresAt);

		// 4. 토큰 반환 (이메일 발송에 사용)
		return tokenString;
	}

	/**
	 * 토큰 검증
	 * - 토큰 존재 여부 확인
	 * - 토큰 만료 여부 확인
	 * - 토큰 사용 여부 확인
	 *
	 * @param tokenString 토큰 문자열
	 * @return 유효하면 true, 그렇지 않으면 false
	 */
	public boolean validateToken(String tokenString) {
		PasswordResetToken token = tokenRepository.findByToken(tokenString).orElse(null);

		if (token == null) {
			log.warn("Token not found: {}", tokenString);
			return false;
		}

		if (token.isUsed()) {
			log.warn("Token already used: {}", tokenString);
			return false;
		}

		if (token.isExpired()) {
			log.warn("Token expired: token={}, expiresAt={}", tokenString, token.getExpiresAt());
			return false;
		}

		return true;
	}

	/**
	 * 비밀번호 재설정
	 * - 토큰 검증
	 * - 비밀번호 해싱 및 저장
	 * - 토큰 사용 처리
	 *
	 * @param tokenString 토큰 문자열
	 * @param newPassword 새 비밀번호
	 * @return 성공 여부
	 */
	public boolean resetPassword(String tokenString, String newPassword) {
		// 1. 토큰 검증
		if (!validateToken(tokenString)) {
			return false;
		}

		// 2. 토큰 조회
		PasswordResetToken token = tokenRepository.findByToken(tokenString)
			.orElseThrow(() -> new IllegalArgumentException("Invalid token"));

		// 3. 사용자 비밀번호 변경
		User user = token.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		// 4. 토큰 사용 처리
		token.setUsed(true);
		tokenRepository.save(token);

		log.info("Password reset completed: username={}, email={}",
		         user.getUsername(), user.getEmail());

		return true;
	}

	/**
	 * 토큰으로 사용자 이메일 조회
	 * (비밀번호 재설정 화면에서 사용자 확인용)
	 *
	 * @param tokenString 토큰 문자열
	 * @return 사용자 이메일 (Optional)
	 */
	public String getUserEmailByToken(String tokenString) {
		return tokenRepository.findByToken(tokenString)
			.map(token -> token.getUser().getEmail())
			.orElse(null);
	}

	/**
	 * 만료된 토큰 삭제 (스케줄러에서 호출)
	 * - 만료 시각이 지난 토큰 조회
	 * - 일괄 삭제
	 *
	 * @return 삭제된 토큰 개수
	 */
	public int deleteExpiredTokens() {
		var expiredTokens = tokenRepository.findByExpiresAtBefore(LocalDateTime.now());
		int count = expiredTokens.size();

		if (count > 0) {
			tokenRepository.deleteAll(expiredTokens);
			log.info("Deleted {} expired password reset tokens", count);
		}

		return count;
	}
}

