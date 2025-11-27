package org.springframework.samples.petclinic.counsel.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : CounselCommentDto.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 댓글 DTO (Tree 구조 지원)
 *
 * Purpose (만든 이유):
 *   1. Entity를 직접 노출하지 않고 댓글 데이터 전달
 *   2. Tree 구조 지원 (무제한 depth 대댓글)
 *   3. 부모 댓글 정보 포함 (parentAuthorName)
 *   4. 관리자 답변 구분 (staffReply)
 *   5. 비밀번호 보호 댓글 지원
 *
 * Key Fields (주요 필드):
 *   - id: 댓글 ID
 *   - content: 댓글 내용 (HTML)
 *   - authorName: 작성자 이름 (닉네임)
 *   - staffReply: 관리자 답변 여부
 *   - parentId: 부모 댓글 ID (대댓글인 경우)
 *   - parentAuthorName: 부모 댓글 작성자 이름
 *   - children: 자식 댓글 목록 (Tree 구조)
 *   - depth: 댓글 깊이 (0 = 최상위)
 *
 * Tree Structure (트리 구조):
 *   - depth 0: 최상위 댓글
 *   - depth 1: 대댓글
 *   - depth 2: 대대댓글
 *   - depth N: 무제한 깊이 지원
 *   - children: 자식 댓글 목록을 재귀적으로 포함
 *
 * Usage Examples (사용 예시):
 *   // Service에서 Tree 구조 생성
 *   List<CounselCommentDto> comments = counselService.getCommentsForPost(postId);
 *
 *   // Thymeleaf에서 재귀 렌더링
 *   <div th:each="comment : ${comments}" th:style="'margin-left: ' + ${comment.depth * 20} + 'px'">
 *     <span th:text="${comment.authorName}">작성자</span>
 *     <div th:each="child : ${comment.children}">...</div>
 *   </div>
 *
 * Security (보안):
 *   - passwordHash는 DTO에 포함 안 됨
 *   - password는 입력용 (평문)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
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
