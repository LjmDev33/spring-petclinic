package org.springframework.samples.petclinic.photo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : PhotoPostDto.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 게시글 DTO (Data Transfer Object)
 *
 * Purpose (만든 이유):
 *   1. Entity를 직접 노출하지 않고 필요한 데이터만 전달
 *   2. 썸네일 URL 포함하여 화면에서 바로 표시 가능
 *   3. Controller ↔ Service 간 데이터 전달
 *   4. REST API 응답용 (JSON 직렬화)
 *
 * Key Fields (주요 필드):
 *   - id: 게시글 ID
 *   - title: 제목
 *   - content: 본문 (HTML)
 *   - author: 작성자
 *   - thumbnailUrl: 썸네일 이미지 URL (목록 표시용)
 *   - viewCount: 조회수
 *   - likeCount: 좋아요 수
 *   - createdAt: 작성일시
 *   - updatedAt: 수정일시
 *
 * Thumbnail Feature (썸네일 특징):
 *   - 사용자가 직접 설정 가능
 *   - 미설정 시 content에서 첫 번째 이미지 자동 추출
 *   - PhotoService.createPost()에서 자동 처리
 *
 * Usage Examples (사용 예시):
 *   // Service에서 Entity → DTO 변환
 *   PhotoPostDto dto = PhotoPostMapper.toDto(entity);
 *
 *   // Controller에서 JSON 응답
 *   return ResponseEntity.ok(dto);
 *
 *   // Thymeleaf에서 썸네일 표시
 *   <img th:src="${post.thumbnailUrl}" alt="썸네일">
 *
 * vs CounselPostDto:
 *   - CounselPostDto: 댓글, 첨부파일, 비밀번호 포함 (복잡)
 *   - PhotoPostDto: 썸네일 중심, 단순 구조
 *
 * Mapper (변환):
 *   - PhotoPostMapper.toDto(Entity) → DTO
 *   - PhotoPostMapper.toEntity(DTO) → Entity
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class PhotoPostDto {

	private Long id;
	private String title;
	private String content;
	private String author;
	private String thumbnailUrl;
	private int viewCount;
	private int likeCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	/** Phase 3: 첨부파일 목록 */
	private List<AttachmentInfo> attachments = new ArrayList<>();

	/** Phase 3: 삭제할 첨부파일 ID 목록 (쉼표 구분) */
	private String deletedFileIds;

	/** Phase 3: 새로 업로드된 첨부파일 경로 목록 (쉼표 구분) */
	private String attachmentPaths;

	/**
	 * Phase 3: 첨부파일 정보 내부 클래스
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

	// Getter & Setter

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
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

	/** Phase 3: 첨부파일 목록 */
	public List<AttachmentInfo> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentInfo> attachments) {
		this.attachments = attachments;
	}

	public String getDeletedFileIds() {
		return deletedFileIds;
	}

	public void setDeletedFileIds(String deletedFileIds) {
		this.deletedFileIds = deletedFileIds;
	}

	public String getAttachmentPaths() {
		return attachmentPaths;
	}

	public void setAttachmentPaths(String attachmentPaths) {
		this.attachmentPaths = attachmentPaths;
	}
}

