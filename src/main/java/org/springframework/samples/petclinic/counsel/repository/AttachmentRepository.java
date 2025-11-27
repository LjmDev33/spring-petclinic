package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.common.table.Attachment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : AttachmentRepository.java
 * Created : 2025-11-26 (마이그레이션)
 * Author  : Jeongmin Lee
 *
 * Description :
 *   통합 Attachment Repository (common.table.Attachment 사용)
 *   - 모든 도메인에서 공통 사용
 *   - Soft Delete 된 첨부파일 조회 (스케줄러 연동)
 *   - 특정 기간 파일 조회
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

	/**
	 * 특정 시간 이전에 Soft Delete 된 첨부파일 목록 조회 (스케줄러용)
	 * @param cutoffTime 기준 시간 (예: 2주 전)
	 * @return 삭제 대상 파일 목록
	 */
	List<Attachment> findByDelFlagTrueAndDeletedAtBefore(LocalDateTime cutoffTime);

	/**
	 * 특정 기간 동안 업로드된 파일 목록 조회
	 * @param startDate 시작 일시
	 * @param endDate 종료 일시
	 * @return 업로드된 파일 목록
	 */
	List<Attachment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
