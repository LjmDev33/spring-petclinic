package org.springframework.samples.petclinic.counsel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * @author Jeongmin Lee
 * @description 첨부파일 정보를 담는 엔티티 클래스.
 * 이 클래스는 업로드된 파일의 메타데이터를 관리합니다.
 * 아직 구현되지 않은 기능:
 * - 파일 다운로드 횟수 추적
 * - 파일 만료 정책
 */
@Entity(name = "CounselAttachment")
@Table(name = "counsel_attachments")
@SQLDelete(sql = "UPDATE counsel_attachments SET del_flag = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("del_flag = false")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 첨부파일 고유 ID

    @Column(name = "file_path", nullable = false)
    private String filePath; // 파일 저장 상대 경로

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName; // 원본 파일명

    @Column(name = "file_size", nullable = false)
    private long fileSize; // 파일 크기 (bytes)

    @Column(name = "mime_type", nullable = false)
    private String mimeType; // 파일 MIME 타입

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성 일시

    @Column(name = "del_flag", nullable = false)
    private boolean delFlag = false; // 삭제 플래그

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 삭제 일시

    // Getters and Setters

    /**
     * 첨부파일 ID를 반환합니다.
     * @return Integer ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 첨부파일 ID를 설정합니다.
     * @param id 첨부파일 ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 저장된 파일 경로를 반환합니다.
     * @return String 파일 경로
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 저장된 파일 경로를 설정합니다.
     * @param filePath 파일 경로
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 원본 파일명을 반환합니다.
     * @return String 원본 파일명
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * 원본 파일명을 설정합니다.
     * @param originalFileName 원본 파일명
     */
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    /**
     * 파일 크기를 반환합니다.
     * @return long 파일 크기 (bytes)
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * 파일 크기를 설정합니다.
     * @param fileSize 파일 크기 (bytes)
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 파일의 MIME 타입을 반환합니다.
     * @return String MIME 타입
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 파일의 MIME 타입을 설정합니다.
     * @param mimeType MIME 타입
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * 생성 일시를 반환합니다.
     * @return LocalDateTime 생성 일시
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 생성 일시를 설정합니다.
     * @param createdAt 생성 일시
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDelFlag() {
        return delFlag;
    }

    public void setDelFlag(boolean delFlag) {
        this.delFlag = delFlag;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
