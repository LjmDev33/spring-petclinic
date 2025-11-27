package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPostLike;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Project : spring-petclinic
 * File    : CounselPostLikeRepository.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 게시글 좋아요 Repository
 *
 * Purpose (만든 이유):
 *   1. 좋아요 데이터 CRUD
 *   2. 특정 사용자의 특정 게시글 좋아요 여부 확인
 *   3. 게시글별 좋아요 개수 조회
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Repository
public interface CounselPostLikeRepository extends JpaRepository<CounselPostLike, Long> {

	/**
	 * 특정 게시글에 특정 사용자가 좋아요를 눌렀는지 확인
	 *
	 * @param postId 게시글 ID
	 * @param username 사용자 아이디
	 * @return 좋아요 엔티티 (Optional)
	 */
	Optional<CounselPostLike> findByPostIdAndUsername(Long postId, String username);

	/**
	 * 특정 게시글의 좋아요 개수 조회
	 *
	 * @param postId 게시글 ID
	 * @return 좋아요 개수
	 */
	long countByPostId(Long postId);

	/**
	 * 특정 게시글에 특정 사용자가 좋아요를 눌렀는지 확인 (boolean)
	 *
	 * @param postId 게시글 ID
	 * @param username 사용자 아이디
	 * @return 좋아요 여부 (true: 좋아요 누름, false: 안 누름)
	 */
	boolean existsByPostIdAndUsername(Long postId, String username);

	/**
	 * 특정 게시글의 모든 좋아요 삭제 (게시글 삭제 시 사용)
	 *
	 * @param postId 게시글 ID
	 */
	void deleteByPostId(Long postId);
}

