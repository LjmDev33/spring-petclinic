package org.springframework.samples.petclinic.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Project : spring-petclinic
 * File    : UserRepository.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 사용자 데이터 접근
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * 사용자 아이디로 조회
	 * @param username 사용자 아이디
	 * @return User 엔티티 (Optional)
	 */
	Optional<User> findByUsername(String username);

	/**
	 * 이메일로 조회
	 * @param email 이메일
	 * @return User 엔티티 (Optional)
	 */
	Optional<User> findByEmail(String email);

	/**
	 * 닉네임으로 조회 (새로 추가)
	 * @param nickname 닉네임
	 * @return User 엔티티 (Optional)
	 */
	Optional<User> findByNickname(String nickname);

	/**
	 * 사용자 아이디 중복 확인
	 * @param username 사용자 아이디
	 * @return 중복 여부
	 */
	boolean existsByUsername(String username);

	/**
	 * 이메일 중복 확인
	 * @param email 이메일
	 * @return 중복 여부
	 */
	boolean existsByEmail(String email);

	/**
	 * 닉네임 중복 확인 (새로 추가)
	 * @param nickname 닉네임
	 * @return 중복 여부
	 */
	boolean existsByNickname(String nickname);

	/**
	 * username 리스트로 User 목록 조회 (좋아요 패널용)
	 * @param usernames username 리스트
	 * @return User 엔티티 리스트
	 */
	java.util.List<User> findByUsernameIn(java.util.List<String> usernames);
}
