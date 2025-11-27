package org.springframework.samples.petclinic.counsel.dto;

import org.springframework.samples.petclinic.counsel.CounselStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : CounselPostDto.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 게시글 DTO (Data Transfer Object)
 *
 * Purpose (만든 이유):
 *   1. Entity를 직접 노출하지 않고 필요한 데이터만 전달 (캡슐화)
 *   2. Controller ↔ Service 간 데이터 전달
 *   3. Thymeleaf 템플릿 또는 REST API 응답에 사용
 *   4. Entity의 민감한 정보 제외 (passwordHash는 DTO에 노출 안 됨)
 *   5. 화면 표시에 필요한 추가 정보 포함 (최근 댓글 정보)
 *
 * Key Fields (주요 필드):
 *   - id: 게시글 ID
 *   - title: 제목
 *   - content: 본문 (HTML 파일에서 로드된 내용)
 *   - contentPath: 본문 HTML 파일 경로
 *   - authorName: 작성자 이름 (닉네임)
 *   - authorEmail: 작성자 이메일
 *   - secret: 비공개 여부
 *   - password: 비밀번호 (입력용, 평문 - 저장 시 BCrypt 해싱)
 *   - status: 상태 (WAIT, COMPLETE, END)
 *   - viewCount: 조회수
 *   - commentCount: 댓글 수
 *   - lastComment*: 최근 댓글 정보 (목록 표시용)
 *   - attachments: 첨부파일 목록
 *
 * Why DTO (Entity 대신 DTO를 사용하는 이유):
 *   1. 보안: passwordHash 같은 민감 정보 노출 방지
 *   2. 유연성: 화면에 필요한 필드만 선택적으로 포함
 *   3. 성능: 불필요한 연관 관계 로딩 방지 (N+1 문제 회피)
 *   4. 변경 격리: Entity 구조 변경이 View/API에 영향 주지 않음
 *
 * Usage Examples (사용 예시):
 *   // Service에서 Entity → DTO 변환
 *   CounselPostDto dto = counselPostMapper.toDto(entity);
 *
 *   // Controller에서 Model에 추가 (Thymeleaf)
 *   model.addAttribute("post", dto);
 *
 *   // REST API 응답
 *   return ResponseEntity.ok(dto);
 *
 * vs Entity:
 *   - Entity: DB 테이블과 1:1 매핑, JPA 관리
 *   - DTO: 데이터 전달 전용, 화면/API 특화
 *
 * Mapper (변환):
 *   - CounselPostMapper.toDto(Entity) → DTO
 *   - CounselPostMapper.toEntity(DTO) → Entity (일부 필드만)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
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
