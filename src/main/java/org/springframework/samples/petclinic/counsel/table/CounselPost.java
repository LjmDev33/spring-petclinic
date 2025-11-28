package org.springframework.samples.petclinic.counsel.table;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.springframework.samples.petclinic.common.entity.BaseEntity;
import org.springframework.samples.petclinic.counsel.CounselStatus;
import org.springframework.samples.petclinic.counsel.table.CounselPostAttachment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : CounselPost.java
 * Created : 2025-10-21
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 게시글 Entity (DB 테이블: counsel_post)
 *
 * Purpose (만든 이유):
 *   1. 온라인상담 게시글 데이터 영속화
 *   2. 비공개글 비밀번호 보호 (BCrypt)
 *   3. Soft Delete 정책 적용
 *   4. 첨부파일 관계 관리 (OneToMany)
 *   5. 본문 파일 저장 (contentPath)
 *
 * Key Features (주요 기능):
 *   - Soft Delete: @SQLDelete, @SQLRestriction
 *   - 비밀번호 해싱: passwordHash (BCrypt)
 *   - 상태 관리: CounselStatus Enum
 *   - 첨부파일: OneToMany (CounselPostAttachment)
 *   - 본문 파일: contentPath로 파일 경로 저장
 *
 * 필드 설명은 각 필드의 인라인 주석 참조
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
	private String title; // 게시글 제목

	@Lob
	@Column(columnDefinition = "MEDIUMTEXT", nullable = false)
	private String content; // 게시글 내용 (TUI Editor의 HTML)

	// 본문 파일 경로 (요구사항: 파일로 저장, 이 경로에서 로드)
	@Column(name = "content_path", length = 500)
	private String contentPath; // 본문 내용이 저장된 파일 경로

	@Column(name = "author_name", nullable = false, length = 100)
	private String authorName; // 작성자 이름

	@Column(name = "author_email", length = 120)
	private String authorEmail; // 작성자 이메일

	@Column(name = "password_hash", length = 100)
	private String passwordHash; // BCrypt로 해시된 비밀번호

	@Column(name = "is_secret", nullable = false)
	private boolean secret = false; // 비공개 글 여부 (true: 비공개)

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CounselStatus status = CounselStatus.WAIT; // 상담 상태 (WAIT: 대기, COMPLETE: 완료)

	@Column(name = "view_count", nullable = false)
	private int viewCount = 0; // 조회수

	@Column(name = "comment_count", nullable = false)
	private int commentCount = 0; // 댓글 수

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

	// VARCHAR(1) : 'Y' / 'N'
	@Column(name = "attach_flag", nullable = false)
	private boolean attachFlag = false; // 첨부파일 존재 여부

	@OneToMany(mappedBy = "counselPost", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	private List<CounselPostAttachment> attachments = new ArrayList<>(); // 첨부파일 관계 목록

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

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(String contentPath) {
		this.contentPath = contentPath;
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

	public List<CounselPostAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<CounselPostAttachment> attachments) {
		this.attachments = attachments;
	}

	public void addAttachment(org.springframework.samples.petclinic.counsel.table.CounselPostAttachment attachment) {
		this.attachments.add(attachment);
		attachment.setCounselPost(this);
	}
}
