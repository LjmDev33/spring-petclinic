package org.springframework.samples.petclinic.counsel.dto;

import org.springframework.samples.petclinic.counsel.CounselStatus;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Project : spring-petclinic
 * File    : CounselPostDto.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 게시글 DTO (뷰/API 전달 전용)
 */
public class CounselPostDto {
	private Long id;
	private String title;
	private String content;
	private String contentPath;
	private String authorName;
	private String authorEmail;
	private boolean secret;
	private String password; // 입력 비밀번호(저장/검증 용도)
	private CounselStatus status;
	private int viewCount;
	private int commentCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String lastCommentTitle;
	private String lastCommentAuthor;
	private LocalDateTime lastCommentCreatedAt;
	private List<AttachmentDto> attachments;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public String getContentPath() { return contentPath; }
	public void setContentPath(String contentPath) { this.contentPath = contentPath; }

	public String getAuthorName() { return authorName; }
	public void setAuthorName(String authorName) { this.authorName = authorName; }

	public String getAuthorEmail() { return authorEmail; }
	public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

	public boolean isSecret() { return secret; }
	public void setSecret(boolean secret) { this.secret = secret; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public CounselStatus getStatus() { return status; }
	public void setStatus(CounselStatus status) { this.status = status; }

	public int getViewCount() { return viewCount; }
	public void setViewCount(int viewCount) { this.viewCount = viewCount; }

	public int getCommentCount() { return commentCount; }
	public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	public String getLastCommentTitle() { return lastCommentTitle; }
	public void setLastCommentTitle(String lastCommentTitle) { this.lastCommentTitle = lastCommentTitle; }

	public String getLastCommentAuthor() { return lastCommentAuthor; }
	public void setLastCommentAuthor(String lastCommentAuthor) { this.lastCommentAuthor = lastCommentAuthor; }

	public LocalDateTime getLastCommentCreatedAt() { return lastCommentCreatedAt; }
	public void setLastCommentCreatedAt(LocalDateTime lastCommentCreatedAt) { this.lastCommentCreatedAt = lastCommentCreatedAt; }

	public List<AttachmentDto> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentDto> attachments) {
		this.attachments = attachments;
	}
}
