package org.springframework.samples.petclinic.counsel.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Project : spring-petclinic
 * File    : CounselCommentDto.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 댓글 정보를 뷰/API로 전달하는 전용 DTO (Entity 직접 노출 금지)
 *   구현완료: Tree 구조 지원 (children, depth 필드 추가)
 *   미구현(후속): 첨부/비공개 비밀번호 필드, 작성자 마스킹 정책
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
	private String parentAuthorName; // 대댓글의 부모 댓글 작성자 이름
	private String passwordHash;

	// Tree 구조 지원 필드
	private List<CounselCommentDto> children = new ArrayList<>(); // 자식 댓글 목록
	private int depth = 0; // 깊이 (0 = 최상위)

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
	/** 대댓글의 부모 댓글 작성자 이름 */
	public String getParentAuthorName() {
		return parentAuthorName;
	}
	public void setParentAuthorName(String parentAuthorName) {
		this.parentAuthorName = parentAuthorName;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/** 자식 댓글 목록 (Tree 구조) */
	public List<CounselCommentDto> getChildren() {
		return children;
	}
	public void setChildren(List<CounselCommentDto> children) {
		this.children = children;
	}

	/** 댓글 깊이 (0 = 최상위) */
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
}
