package org.springframework.samples.petclinic.common.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.common.table.Attachment;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Project : spring-petclinic
 * File    : AttachmentRepository.java
 * Created : 2026-02-05
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2026 AOF(AllForOne) / All rights reserved.
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

	// 1. [기존 기능] 휴지통 비우기용: 삭제된 지 오래된(cutoffDate 이전) 파일 조회
	List<Attachment> findByDelFlagTrueAndDeletedAtBefore(LocalDateTime cutoffDate);

	// 2. [신규 기능] 고아 파일 청소용: 연결된 게시글이 없는 파일 조회 (통합 쿼리)
	@Query("SELECT a FROM Attachment a " +
		"WHERE a.createdAt < :cutoffDate " +
		"AND a.delFlag = false " +
		"AND a.id NOT IN (SELECT c.attachment.id FROM CounselPostAttachment c) " +
		"AND a.id NOT IN (SELECT p.attachment.id FROM PhotoPostAttachment p) " + // Photo 엔티티명 확인 필요
		"AND a.id NOT IN (SELECT n.attachment.id FROM CommunityPostAttachment n)") // Notice 엔티티명 확인 필요
	List<Attachment> findGlobalOrphanFiles(@Param("cutoffDate") LocalDateTime cutoffDate);

	// 3. 서비스단 연결용
	List<Attachment> findByStoredFilenameIn(List<String> storedFilenames);
}
