package org.springframework.samples.petclinic.common.table;

/**
 * Project : spring-petclinic
 * File    : Attachment.java
 * Created : 2025-11-26 (통합 개선)
 * Author  : Jeongmin Lee
 *
 * Description :
 *   통합 첨부파일 Entity (모든 도메인에서 공통 사용)
 *   - 게시글, 댓글, 포토게시판 등 모든 첨부파일 관리
 *   - Soft Delete 정책 적용
 *   - 파일 메타데이터 저장 (경로, 크기, MIME 타입)
 *
 * Features:
 *   - 다운로드 횟수 추적
 *   - 파일 만료 정책 (deleted_at + 2주 후 물리 삭제)
 *   - 도메인별 연결 (중간 테이블 사용)
 *
 * Performance:
 *   - 파일 경로는 상대 경로로 저장하여 I/O 최적화
 *   - 파일 크기 사전 검증으로 업로드 실패 최소화
 *   - 인덱스 최적화로 조회 성능 향상
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.samples.petclinic.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachment",
	uniqueConstraints = @UniqueConstraint(name = "uq_attachment_store_alive",
		columnNames = {"stored_filename","del_flag"}),
	indexes = {
		@Index(name = "idx_attachment_created", columnList = "created_at DESC"),
		@Index(name = "idx_attachment_del_flag", columnList = "del_flag")
	})
@SQLDelete(sql = "UPDATE attachment SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class Attachment extends BaseEntity {

	/** 원본 파일명 (사용자가 업로드한 파일명) */
	@Column(name = "original_filename", nullable = false, length = 255)
	private String originalFilename;

	/** 저장된 파일명 (UUID + 확장자) */
	@Column(name = "stored_filename", nullable = false, length = 255)
	private String storedFilename;

	/** 파일 MIME 타입 (예: image/jpeg, application/pdf) */
	@Column(name = "content_type", length = 100)
	private String contentType;

	/** 파일 크기 (bytes) */
	@Column(name = "file_size", nullable = false)
	private Long fileSize;

	/** 다운로드 횟수 (성능 모니터링 및 인기 파일 추적) */
	@Column(name = "download_count", nullable = false, columnDefinition = "INT DEFAULT 0")
	private int downloadCount = 0;

	/** 생성 일시 (업로드 시간) */
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/** 삭제 플래그 (Soft Delete) */
	@Column(name = "del_flag", nullable = false)
	private boolean delFlag = false;

	/** 삭제 일시 (스케줄러가 2주 후 물리 삭제) */
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	/** 삭제한 사용자 (추적 용도) */
	@Column(name = "deleted_by", length = 60)
	private String deletedBy;

	// Getters and Setters

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getStoredFilename() {
		return storedFilename;
	}

	public void setStoredFilename(String storedFilename) {
		this.storedFilename = storedFilename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public int getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	/**
	 * 다운로드 횟수 증가 (동시성 문제 방지를 위해 서비스 계층에서 @Transactional과 함께 사용)
	 */
	public void incrementDownloadCount() {
		this.downloadCount++;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isDelFlag() {
		return delFlag;
	}

	public void setDelFlag(boolean delFlag) {
		this.delFlag = delFlag;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public String getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
	}

	// Utility Methods

	/**
	 * 파일 확장자 추출
	 */
	public String getFileExtension() {
		if (originalFilename == null || !originalFilename.contains(".")) {
			return "";
		}
		return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
	}

	/**
	 * 파일 크기를 사람이 읽기 쉬운 형식으로 변환 (KB, MB, GB)
	 */
	public String getReadableFileSize() {
		if (fileSize == null) {
			return "0 B";
		}
		if (fileSize < 1024) {
			return fileSize + " B";
		} else if (fileSize < 1024 * 1024) {
			return String.format("%.2f KB", fileSize / 1024.0);
		} else if (fileSize < 1024 * 1024 * 1024) {
			return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
		} else {
			return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
		}
	}

	/**
	 * 이미지 파일 여부 확인
	 */
	public boolean isImageFile() {
		if (contentType == null) {
			return false;
		}
		return contentType.startsWith("image/");
	}

	/**
	 * PDF 파일 여부 확인
	 */
	public boolean isPdfFile() {
		return "application/pdf".equalsIgnoreCase(contentType);
	}
}
