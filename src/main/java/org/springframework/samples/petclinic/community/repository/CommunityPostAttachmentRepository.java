package org.springframework.samples.petclinic.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.community.table.CommunityPostAttachment;
import org.springframework.stereotype.Repository;

/**
 * Project : spring-petclinic
 * File    : CommunityPostAttachmentRepository.java
 * Created : 2025-11-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   Community 게시글 첨부파일 중간 테이블 Repository
 *   - Phase 3: 게시글 첨부파일 관리 기능
 *
 * Purpose (만든 이유):
 *   1. CommunityPostAttachment 중간 테이블 CRUD
 *   2. 게시글 수정 시 첨부파일 추가/삭제 처리
 *
 * Key Methods (주요 메서드):
 *   - 기본 JpaRepository 메서드 사용
 *   - findBy*, deleteBy* 등 Query Method 사용 가능
 *
 * Usage Examples (사용 예시):
 *   // 첨부파일 추가
 *   CommunityPostAttachment postAttachment = new CommunityPostAttachment(post, attachment);
 *   repository.save(postAttachment);
 *
 *   // 첨부파일 삭제
 *   repository.delete(postAttachment);
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Repository
public interface CommunityPostAttachmentRepository extends JpaRepository<CommunityPostAttachment, Long> {
	// 기본 JpaRepository 메서드만 사용
	// 필요 시 커스텀 Query Method 추가 가능
}

