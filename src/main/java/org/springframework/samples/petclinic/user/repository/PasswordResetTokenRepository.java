package org.springframework.samples.petclinic.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.user.table.PasswordResetToken;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 비밀번호 재설정 토큰 Repository
 *
 * @author Jeongmin Lee
 * @description 비밀번호 재설정 토큰의 CRUD 및 검색 기능 제공
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	/**
	 * 토큰 문자열로 토큰 엔티티 조회
	 * @param token 토큰 문자열
	 * @return PasswordResetToken (Optional)
	 */
	Optional<PasswordResetToken> findByToken(String token);

	/**
	 * 사용자 ID로 토큰 목록 조회
	 * @param userId 사용자 ID
	 * @return 토큰 목록
	 */
	List<PasswordResetToken> findByUser_Id(Long userId);

	/**
	 * 만료된 토큰 목록 조회 (스케줄러에서 삭제용)
	 * @param now 현재 시각
	 * @return 만료된 토큰 목록
	 */
	List<PasswordResetToken> findByExpiresAtBefore(LocalDateTime now);

	/**
	 * 사용자의 미사용 토큰 삭제
	 * @param userId 사용자 ID
	 */
	void deleteByUser_IdAndUsedFalse(Long userId);
}

