package org.springframework.samples.petclinic.photo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : PhotoCommentDto.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 * 포토게시판 댓글 DTO (Tree 구조 지원)
 *
 * Key Fields:
 * - children: 자식 댓글 목록 (대댓글)
 * - depth: 계층 깊이 (0: 댓글, 1: 대댓글 ...)
 */
public class PhotoCommentDto {
	private Long id;
	private String content;
	private String authorName;
	private boolean staffReply; // 운영자/관리자 표시 여부
	private LocalDateTime createdAt;
	private String authorEmail;
	private String password;    // 등록/삭제 시 확인용 (평문)
	private Long parentId;      // 부모 댓글 ID
	private String parentAuthorName; // 부모 댓글 작성자 (답글 대상)
	private String passwordHash; // Entity 변환용

	// Tree 구조 지원 필드
	private List<PhotoCommentDto> children = new ArrayList<>(); // 자식 댓글 목록
	private int depth = 0; // 깊이 (0 = 최상위)

	/** 댓글 ID */
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	/** 댓글 내용 */
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

	public String getAuthorEmail() { return authorEmail; }
	public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public Long getParentId() { return parentId; }
	public void setParentId(Long parentId) { this.parentId = parentId; }

	/** 대댓글의 부모 댓글 작성자 이름 */
	public String getParentAuthorName() { return parentAuthorName; }
	public void setParentAuthorName(String parentAuthorName) { this.parentAuthorName = parentAuthorName; }

	public String getPasswordHash() { return passwordHash; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

	/** 자식 댓글 목록 (Tree 구조) */
	public List<PhotoCommentDto> getChildren() { return children; }
	public void setChildren(List<PhotoCommentDto> children) { this.children = children; }

	/** 댓글 깊이 (0 = 최상위) */
	public int getDepth() { return depth; }
	public void setDepth(int depth) { this.depth = depth; }
}
