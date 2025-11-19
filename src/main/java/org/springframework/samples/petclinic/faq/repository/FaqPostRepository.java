package org.springframework.samples.petclinic.faq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.faq.table.FaqPost;

import java.util.List;

/**
 * Project : spring-petclinic
 * File    : FaqPostRepository.java
 * Created : 2025-11-14
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: FAQ 게시글 데이터 접근 레이어
 *   연관 기능:
 *     - FAQ 목록/상세 조회
 *     - FAQ 등록/수정/삭제(관리자)
 *   미구현 기능:
 *     - 카테고리/키워드 검색
 */
public interface FaqPostRepository extends JpaRepository<FaqPost, Long> {

    List<FaqPost> findByDelFlagFalseOrderByDisplayOrderAscCreatedAtDesc();
}

