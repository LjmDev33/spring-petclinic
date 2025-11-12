package org.springframework.samples.petclinic.counsel.dto;

import java.time.LocalDateTime;

/*
 * Project : spring-petclinic
 * File    : CounselCommentDto.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 댓글 정보를 뷰/API로 전달하는 전용 DTO (Entity 직접 노출 금지)
 *   미구현(후속): 첨부/비공개 비밀번호 필드, 대댓글 depth 정보, 작성자 마스킹 정책
 */
public class CounselCommentDto {
	private Long id;
	private String content;
	private String authorName;
	private boolean staffReply;
	private LocalDateTime createdAt;
	private String authorEmail;
	private String password;
	private Long parentId;
	private String passwordHash;

	/** 댓글 ID */
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	/** 댓글 내용 (이미 서버 측 sanitize 가정) */
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	/** 작성자 이름 */
	public String getAuthorName() { return authorName; }
	public void setAuthorName(String authorName) { this.authorName = authorName; }
	/** 스태프 답변 여부 */
	public boolean isStaffReply() { return staffReply; }
	public void setStaffReply(boolean staffReply) { this.staffReply = staffReply; }
	/** 생성 일시 */
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public String getAuthorEmail() {
		return authorEmail;
	}
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
