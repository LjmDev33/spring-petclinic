package org.springframework.samples.petclinic.photo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.photo.table.PhotoComment;

import java.util.List;
import java.util.Optional;

/*
 * Project : spring-petclinic
 * File    : PhotoCommentRepository.java
 * Created : 2026-01-26
 * Author  : Jeongmin Lee
 *
 * Description :
 * 포토게시판 댓글 저장소
 * - 댓글 CRUD 및 게시글별/작성자별/부모별 댓글 조회 제공
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {

	/**
	 * 게시글 ID로 댓글을 생성일 오름차순으로 조회합니다.
	 * - 상세 화면에서 댓글 목록 렌더링(Tree 구조 생성 전 기초 데이터)에 사용합니다.
	 */
	List<PhotoComment> findByPost_IdOrderByCreatedAtAsc(Long postId);

	/**
	 * 게시글의 가장 최신 댓글 1건을 조회합니다.
	 * - 목록 화면에서 요약 정보 표시용
	 */
	Optional<PhotoComment> findTopByPost_IdOrderByCreatedAtDesc(Long postId);

	/**
	 * 작성자 이름으로 댓글 목록 조회 (페이징)
	 * @param authorName 작성자 이름
	 * @param pageable 페이징 정보
	 * @return 댓글 페이지
	 */
	Page<PhotoComment> findByAuthorNameOrderByCreatedAtDesc(String authorName, Pageable pageable);

	/**
	 * 부모 댓글 ID로 자식 댓글 목록 조회
	 * - 댓글 삭제 시 자식 댓글(대댓글) 존재 여부 확인에 사용 (무결성 제어)
	 * @param parentId 부모 댓글 ID
	 * @return 자식 댓글 목록
	 */
	List<PhotoComment> findByParent_Id(Long parentId);
}
