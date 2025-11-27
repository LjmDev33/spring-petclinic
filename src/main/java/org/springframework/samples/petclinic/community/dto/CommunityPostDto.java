package org.springframework.samples.petclinic.community.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : CommunityPostDto.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   공지사항 게시글 DTO (Data Transfer Object)
 *
 * Purpose (만든 이유):
 *   1. Entity를 직접 노출하지 않고 필요한 데이터만 전달
 *   2. Controller ↔ Service 간 데이터 전달
 *   3. Thymeleaf 템플릿 렌더링용
 *   4. REST API 응답용 (JSON 직렬화)
 *
 * Key Fields (주요 필드):
 *   - id: 게시글 ID
 *   - title: 제목
 *   - content: 본문
 *   - author: 작성자 (관리자)
 *   - createdAt: 작성일시
 *   - viewCount: 조회수
 *   - likeCount: 좋아요 수
 *
 * Why Simple (간단한 이유):
 *   - 공지사항은 관리자만 작성 (비밀번호 불필요)
 *   - 댓글 기능 없음 (댓글 관련 필드 제외)
 *   - 첨부파일 없음 (첨부파일 목록 제외)
 *   - vs CounselPostDto: 훨씬 단순한 구조
 *
 * Usage Examples (사용 예시):
 *   // Service에서 Entity → DTO 변환
 *   CommunityPostDto dto = CommunityPostMapper.toDto(entity);
 *
 *   // Controller에서 Model에 추가
 *   model.addAttribute("post", dto);
 *
 *   // PageResponse에 담아 반환
 *   PageResponse<CommunityPostDto> page = service.getPagedPosts(pageable);
 *
 * Mapper (변환):
 *   - CommunityPostMapper.toDto(Entity) → DTO
 *   - CommunityPostMapper.toEntity(DTO) → Entity
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class CommunityPostDto {

	private Long id;
	private String title;
	private String content;
	private String author;
	private LocalDateTime createdAt;

	private int viewCount;
	private int likeCount;

	/** 첨부파일 목록 (Phase 3) */
	private List<AttachmentInfo> attachments = new ArrayList<>();

	/** 삭제할 첨부파일 ID 목록 (쉼표 구분, 수정 시 사용) */
	private String deletedFileIds;

	/** 새로 업로드된 첨부파일 경로 목록 (쉼표 구분, 수정 시 사용) */
	private String attachmentPaths;

	// Getter & Setter
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public int getViewCount() {return viewCount;}
	public void setViewCount(int viewCount) {this.viewCount = viewCount;}

	public int getLikeCount() {return likeCount;}
	public void setLikeCount(int likeCount) {this.likeCount = likeCount;}

	public List<AttachmentInfo> getAttachments() { return attachments; }
	public void setAttachments(List<AttachmentInfo> attachments) { this.attachments = attachments; }

	public String getDeletedFileIds() { return deletedFileIds; }
	public void setDeletedFileIds(String deletedFileIds) { this.deletedFileIds = deletedFileIds; }

	public String getAttachmentPaths() { return attachmentPaths; }
	public void setAttachmentPaths(String attachmentPaths) { this.attachmentPaths = attachmentPaths; }

	/**
	 * 첨부파일 정보 DTO (내부 클래스)
	 * - Phase 3: 템플릿에서 첨부파일 목록 표시용
	 */
	public static class AttachmentInfo {
		private Long id;
		private String originalFileName;
		private Long fileSize;

		public AttachmentInfo() {}

		public AttachmentInfo(Long id, String originalFileName, Long fileSize) {
			this.id = id;
			this.originalFileName = originalFileName;
			this.fileSize = fileSize;
		}

		public Long getId() { return id; }
		public void setId(Long id) { this.id = id; }

		public String getOriginalFileName() { return originalFileName; }
		public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

		public Long getFileSize() { return fileSize; }
		public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
	}
}
