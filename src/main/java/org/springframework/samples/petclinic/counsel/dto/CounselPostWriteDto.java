package org.springframework.samples.petclinic.counsel.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * @author Jeongmin Lee
 * @description 온라인 상담 게시글 작성을 위한 데이터를 담는 DTO 클래스.
 * 이 클래스는 파일 업로드를 포함한 게시글 작성 폼 데이터를 컨트롤러에 전달합니다.
 * 아직 구현되지 않은 기능:
 * - 임시 저장 기능 지원
 */
public class CounselPostWriteDto {

    private String title;
    private String authorName;
    private String password;
    private String content;
    private boolean secret;
    private List<MultipartFile> attachments;
    private List<Integer> attachmentIds = new java.util.ArrayList<>();
    private String attachmentPaths; // Uppy 업로드된 파일 경로 (쉼표 구분)
    private String deletedFileIds; // 삭제할 첨부파일 ID 목록 (쉼표 구분, 수정 시만 사용)

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public List<MultipartFile> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MultipartFile> attachments) {
        this.attachments = attachments;
    }

    public List<Integer> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(List<Integer> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public String getAttachmentPaths() {
        return attachmentPaths;
    }

    public void setAttachmentPaths(String attachmentPaths) {
        this.attachmentPaths = attachmentPaths;
    }

    public String getDeletedFileIds() {
        return deletedFileIds;
    }

    public void setDeletedFileIds(String deletedFileIds) {
        this.deletedFileIds = deletedFileIds;
    }
}
