package org.springframework.samples.petclinic.counsel.dto;

import java.time.LocalDateTime;

/**
 * Project : spring-petclinic
 * File    : AttachmentDto.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   첨부파일 정보 DTO
 *
 * Purpose (만든 이유):
 *   1. Entity를 직접 노출하지 않고 첨부파일 정보 전달
 *   2. 다운로드 URL 포함하여 화면에서 바로 사용
 *   3. 파일 크기를 사람이 읽기 쉬운 형태로 변환 가능
 *   4. Thymeleaf에서 바로 렌더링 가능
 *
 * Key Fields (주요 필드):
 *   - id: 첨부파일 ID
 *   - originalFileName: 원본 파일명
 *   - fileSize: 파일 크기 (byte)
 *   - mimeType: MIME 타입 (image/jpeg, application/pdf 등)
 *   - createdAt: 업로드 일시
 *   - downloadUrl: 다운로드 URL (Controller에서 생성)
 *
 * Usage Examples (사용 예시):
 *   // Service에서 Entity → DTO 변환
 *   AttachmentDto dto = attachmentMapper.toDto(entity);
 *   dto.setDownloadUrl("/counsel/attachments/" + entity.getId());
 *
 *   // Thymeleaf에서 다운로드 링크
 *   <a th:href="${attachment.downloadUrl}" th:text="${attachment.originalFileName}">파일명</a>
 *
 * File Size Display (파일 크기 표시):
 *   - 화면에서 fileSize를 KB, MB 단위로 변환하여 표시
 *   - 예: 1024 → "1.0 KB", 1048576 → "1.0 MB"
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class AttachmentDto {

    private Integer id;
    private String originalFileName;
    private long fileSize;
    private String mimeType;
    private LocalDateTime createdAt;
    private String downloadUrl; // 다운로드 URL 추가

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}

