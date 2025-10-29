package org.springframework.samples.petclinic.common.table;

/*
 * Project : spring-petclinic
 * File    : Attachment.java
 * Created : 2025-10-23
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 공용첨부 테이블
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.samples.petclinic.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachment",
	uniqueConstraints = @UniqueConstraint(name = "uq_attachment_store_alive",
		columnNames = {"stored_filename","del_flag"}))
@SQLDelete(sql = "UPDATE attachment SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class Attachment extends BaseEntity {

	@Column(name = "original_filename", nullable = false, length = 255)
	private String originalFilename;

	@Column(name = "stored_filename", nullable = false, length = 255)
	private String storedFilename;

	@Column(name = "content_type", length = 100)
	private String contentType;

	@Column(name = "file_size", nullable = false)
	private Long fileSize;

	@Column(name = "del_flag", nullable = false)
	private boolean delFlag = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 60)
	private String deletedBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
