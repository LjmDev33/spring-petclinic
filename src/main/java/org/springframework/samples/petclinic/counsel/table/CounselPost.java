package org.springframework.samples.petclinic.counsel.table;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.springframework.samples.petclinic.common.entity.BaseEntity;
import org.springframework.samples.petclinic.counsel.CounselStatus;

import java.time.LocalDateTime;

/*
 * Project : spring-petclinic
 * File    : CounselPost.java
 * Created : 2025-10-21
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 온라인상담 테이블
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */

@Entity
@Table(name = "counsel_post")
@SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CounselPost  extends BaseEntity {

	@Column(nullable = false, length = 255)
	private String title;

	@Lob
	@Column(columnDefinition = "MEDIUMTEXT", nullable = false)
	private String content;

	@Column(name = "author_name", nullable = false, length = 100)
	private String authorName;

	@Column(name = "author_email", length = 120)
	private String authorEmail;

	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@Column(name = "is_secret", nullable = false)
	private boolean secret = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CounselStatus status = CounselStatus.WAIT;

	@Column(name = "view_count", nullable = false , columnDefinition = "INT DEFAULT 0")
	private int viewCount = 0;

	@Column(name = "comment_count", nullable = false)
	private int commentCount = 0;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "del_flag", nullable = false)
	private boolean delFlag = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 60)
	private String deletedBy;

	// VARCHAR(1) : 'Y' / 'N'
	@Column(name = "attach_flag", nullable = false)
	private boolean attachFlag = false;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	public CounselStatus getStatus() {
		return status;
	}

	public void setStatus(CounselStatus status) {
		this.status = status;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
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

	public boolean isAttachFlag() {
		return attachFlag;
	}

	public void setAttachFlag(boolean attachFlag) {
		this.attachFlag = attachFlag;
	}
}
