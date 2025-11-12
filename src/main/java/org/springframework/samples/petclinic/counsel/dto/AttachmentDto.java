package org.springframework.samples.petclinic.counsel.dto;

import java.time.LocalDateTime;

/**
 * @author Jeongmin Lee
 * @description 첨부파일 정보를 전달하는 DTO 클래스.
 * 이 클래스는 뷰와 컨트롤러 사이에서 첨부파일 데이터를 전달하는 데 사용됩니다.
 * 아직 구현되지 않은 기능:
 * - 파일 다운로드 URL 생성 로직
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

