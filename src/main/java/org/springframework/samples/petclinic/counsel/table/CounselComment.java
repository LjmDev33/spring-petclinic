package org.springframework.samples.petclinic.counsel.table;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.springframework.samples.petclinic.common.entity.BaseEntity;

import java.time.LocalDateTime;

/*
 * Project : spring-petclinic
 * File    : CounselComment.java
 * Created : 2025-10-23
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 온라인상담 댓글 테이블
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "counsel_comment",
	indexes = {
		@Index(name = "idx_comment_post_created", columnList = "post_id, created_at"),
		@Index(name = "idx_comment_parent", columnList = "parent_id")
	})
@SQLDelete(sql = "UPDATE counsel_comment SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CounselComment extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_post"))
	@OnDelete(action = OnDeleteAction.CASCADE) // DB 레벨
	private CounselPost post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comment_parent"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private CounselComment parent;

	@Lob
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column(name = "author_name", nullable = false, length = 100)
	private String authorName;

	@Column(name = "author_email", length = 120)
	private String authorEmail;

	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@Column(name = "is_staff_reply", nullable = false)
	private boolean staffReply = false;

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

	public CounselPost getPost() {
		return post;
	}

	public void setPost(CounselPost post) {
		this.post = post;
	}

	public CounselComment getParent() {
		return parent;
	}

	public void setParent(CounselComment parent) {
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
