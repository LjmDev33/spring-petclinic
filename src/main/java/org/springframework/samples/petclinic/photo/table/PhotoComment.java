package org.springframework.samples.petclinic.photo.table;


import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.springframework.samples.petclinic.common.entity.BaseEntity;

import java.time.LocalDateTime;

/*
 * Project : spring-petclinic
 * File    : PhotoComment.java
 * Created : 2026-01-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2026 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "photo_comment",
	indexes = {
		@Index(name = "idx_photo_comment_post_created", columnList = "post_id, created_at"),
		@Index(name = "idx_photo_comment_parent", columnList = "parent_id")
	})
@SQLDelete(sql = "UPDATE photo_comment SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class PhotoComment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private PhotoPost post; // 이 댓글이 속한 포토 게시글

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private PhotoComment parent; // 부모 댓글 (대댓글의 경우)

	@Lob
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content; // 댓글 내용

	@Column(name = "author_name", nullable = false, length = 100)
	private String authorName; // 작성자 이름

	@Column(name = "author_email", length = 120)
	private String authorEmail; // 작성자 이메일

	@Column(name = "password_hash", length = 100)
	private String passwordHash; // BCrypt로 해시된 비밀번호

	@Column(name = "is_staff_reply", nullable = false)
	private boolean staffReply = false; // 운영자 답변 여부

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt; // 생성 일시

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt; // 수정 일시

	@Column(name = "del_flag", nullable = false)
	private boolean delFlag = false; // 삭제 플래그

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt; // 삭제 일시

	@Column(name = "deleted_by", length = 60)
	private String deletedBy; // 삭제한 사용자

	// ================= Getters and Setters =================

	public PhotoPost getPost() {
		return post;
	}

	public void setPost(PhotoPost post) {
		this.post = post;
	}

	public PhotoComment getParent() {
		return parent;
	}

	public void setParent(PhotoComment parent) {
		this.parent = parent;
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

	public boolean isStaffReply() {
		return staffReply;
	}

	public void setStaffReply(boolean staffReply) {
		this.staffReply = staffReply;
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
}
