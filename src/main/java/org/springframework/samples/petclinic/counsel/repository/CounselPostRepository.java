package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPost;

/*
 * Project : spring-petclinic
 * File    : CounselPostRepository.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 온라인상담 게시판 레포지토리
 *   - 기본 CRUD 및 작성자별 조회 기능 제공
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CounselPostRepository extends JpaRepository<CounselPost, Long>, CounselPostRepositoryCustom {

	/**
	 * 작성자 이름으로 게시글 목록 조회 (페이징)
	 * @param authorName 작성자 이름
	 * @param pageable 페이징 정보
	 * @return 게시글 페이지
	 */
	Page<CounselPost> findByAuthorNameOrderByCreatedAtDesc(String authorName, Pageable pageable);

	/**
	 * 작성자 아이디 조회
	 * @param id 게시판 아이디
	 * @return 작성자 아이디
	 */
	String getBoardOnwerId(long id);
}
