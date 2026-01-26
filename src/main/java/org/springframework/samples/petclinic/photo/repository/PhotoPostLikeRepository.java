package org.springframework.samples.petclinic.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.community.table.CommunityPostLike;
import org.springframework.samples.petclinic.photo.table.PhotoPostLike;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Project : spring-petclinic
 * File    : PhotoPostLikeRepository.java
 * Created : 2025-11-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 게시글 좋아요 Repository
 *
 * Purpose (만든 이유):
 *   1. 좋아요 데이터 CRUD 처리
 *   2. 특정 사용자의 특정 게시글 좋아요 여부 확인
 *   3. 게시글별 좋아요 개수 조회
 *   4. 좋아요 토글 기능 지원
 *
 * Key Features (주요 기능):
 *   - findByPostIdAndUsername: 특정 사용자의 좋아요 조회 (토글용)
 *   - countByPostId: 게시글의 총 좋아요 개수
 *   - existsByPostIdAndUsername: 좋아요 여부 확인 (boolean)
 *   - deleteByPostId: 게시글 삭제 시 연관 좋아요 일괄 삭제
 *
 * Query Methods:
 *   Spring Data JPA의 쿼리 메서드 네이밍 규칙 사용
 *   - find: SELECT 쿼리
 *   - count: COUNT 쿼리
 *   - exists: EXISTS 쿼리
 *   - delete: DELETE 쿼리
 *
 * Usage Examples (사용 예시):
 *   // 좋아요 추가
 *   PhotoPostLike like = new PhotoPostLike(post, username);
 *   likeRepository.save(like);
 *
 *   // 좋아요 토글 (있으면 삭제, 없으면 추가)
 *   Optional<PhotoPostLike> existing = likeRepository.findByPostIdAndUsername(postId, username);
 *   if (existing.isPresent()) {
 *       likeRepository.delete(existing.get()); // 취소
 *   } else {
 *       likeRepository.save(new PhotoPostLike(post, username)); // 추가
 *   }
 *
 *   // 좋아요 개수 조회
 *   long count = likeRepository.countByPostId(postId);
 *
 *   // 좋아요 여부 확인
 *   boolean liked = likeRepository.existsByPostIdAndUsername(postId, username);
 *
 * Transaction:
 *   @Transactional 어노테이션은 Service 계층에서 관리
 *   Repository는 단순 DB 접근만 담당
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Repository
public interface PhotoPostLikeRepository extends JpaRepository<PhotoPostLike, Long> {

	/**
	 * 특정 게시글에 특정 사용자가 좋아요를 눌렀는지 조회
	 *
	 * <p>좋아요 토글 기능에서 사용됩니다.</p>
	 * <p>존재하면 삭제, 없으면 추가하는 방식으로 동작합니다.</p>
	 *
	 * @param postId 게시글 ID
	 * @param username 사용자 아이디
	 * @return 좋아요 엔티티 (Optional)
	 */
	Optional<PhotoPostLike> findByPostIdAndUsername(Long postId, String username);

	/**
	 * 특정 게시글의 좋아요 개수 조회
	 *
	 * <p>게시글 상세 화면에서 총 좋아요 수를 표시할 때 사용됩니다.</p>
	 *
	 * @param postId 게시글 ID
	 * @return 좋아요 개수 (long)
	 */
	long countByPostId(Long postId);

	/**
	 * 특정 게시글에 특정 사용자가 좋아요를 눌렀는지 확인 (boolean 반환)
	 *
	 * <p>게시글 상세 화면에서 하트 아이콘 색상을 결정할 때 사용됩니다.</p>
	 * <p>true: 빨간 하트, false: 회색 빈 하트</p>
	 *
	 * @param postId 게시글 ID
	 * @param username 사용자 아이디
	 * @return 좋아요 여부 (true: 좋아요 누름, false: 안 누름)
	 */
	boolean existsByPostIdAndUsername(Long postId, String username);

	/**
	 * 특정 게시글의 모든 좋아요 삭제
	 *
	 * <p>게시글 삭제 시 연관된 좋아요도 함께 삭제하기 위해 사용됩니다.</p>
	 * <p>CASCADE 설정으로 자동 삭제되지만, 명시적 삭제도 지원합니다.</p>
	 *
	 * @param postId 게시글 ID
	 */
	void deleteByPostId(Long postId);

	/**
	 * 특정 게시글에 좋아요를 누른 모든 사용자 조회 (좋아요 패널용)
	 *
	 * @param postId 게시글 ID
	 * @return 좋아요 엔티티 리스트 (생성일시 기준 오름차순)
	 */
	@org.springframework.data.jpa.repository.Query("SELECT cl FROM PhotoPostLike cl WHERE cl.post.id = :postId ORDER BY cl.createdAt ASC")
	java.util.List<PhotoPostLike> findAllByPostIdOrderByCreatedAtAsc(@org.springframework.data.repository.query.Param("postId") Long postId);

}

