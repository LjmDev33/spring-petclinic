package org.springframework.samples.petclinic.photo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.photo.table.PhotoPost;

/**
 * Project : spring-petclinic
 * File    : PhotoPostRepositoryCustom.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 QueryDSL Custom Repository 인터페이스
 *   - 복잡한 검색 조건 처리
 *   - 동적 쿼리 생성
 *   - 페이징 처리 최적화
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface PhotoPostRepositoryCustom {

	/**
	 * 제목 또는 내용으로 검색 (페이징)
	 * @param type 검색 타입 (title, content, titleAndContent)
	 * @param keyword 검색 키워드
	 * @param pageable 페이징 정보
	 * @return 검색 결과 PageResponse
	 */
	PageResponse<PhotoPost> search(String type, String keyword, Pageable pageable);

	/**
	 * 작성자별 게시글 조회 (페이징)
	 * @param author 작성자 이름
	 * @param pageable 페이징 정보
	 * @return 작성자 게시글 목록
	 */
	PageResponse<PhotoPost> findByAuthor(String author, Pageable pageable);

	/**
	 * 인기 게시글 조회 (조회수 또는 좋아요 수 기준)
	 * @param limit 조회할 개수
	 * @return 인기 게시글 목록
	 */
	java.util.List<PhotoPost> findPopularPosts(int limit);
}

