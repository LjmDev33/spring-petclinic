package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.counsel.table.CounselComment;

import java.util.List;
import java.util.Optional;

/*
 * Project : spring-petclinic
 * File    : CounselCommentRepository.java
 * Created : 2025-10-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 댓글 저장소
 *   - 사용목적: 댓글 CRUD 및 게시글별 댓글 조회 제공
 *   - 미구현(후속): 대댓글 트리 조회/페이징, 비공개 댓글 열람권한 제어
 */
public interface CounselCommentRepository extends JpaRepository<CounselComment,Long> {
	/**
	 * 게시글 ID로 댓글을 생성일 오름차순으로 조회합니다.
	 * - 상세 화면에서 댓글 목록 렌더링에 사용합니다.
	 */
	List<CounselComment> findByPost_IdOrderByCreatedAtAsc(Long postId);

	/**
	 * 게시글의 가장 최신 댓글 1건을 조회합니다.
	 * - 목록 화면에서 제목 아래 요약 표시용
	 */
	Optional<CounselComment> findTopByPost_IdOrderByCreatedAtDesc(Long postId);
}
