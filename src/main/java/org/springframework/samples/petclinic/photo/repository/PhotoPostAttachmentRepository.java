package org.springframework.samples.petclinic.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPostAttachment;
import org.springframework.stereotype.Repository;

/**
 * Project : spring-petclinic
 * File    : PhotoPostAttachmentRepository.java
 * Created : 2025-11-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 첨부파일 중간 테이블 Repository
 *
 * Purpose (만든 이유):
 *   - 게시글-첨부파일 관계 관리
 *   - JPA 기본 CRUD 제공
 */
@Repository
public interface PhotoPostAttachmentRepository extends JpaRepository<PhotoPostAttachment, Long> {
	// 기본 JpaRepository 메서드만 사용
}

