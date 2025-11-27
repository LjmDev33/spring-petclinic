package org.springframework.samples.petclinic.counsel.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : CounselPostWriteDto.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 게시글 작성/수정 폼 DTO
 *
 * Purpose (만든 이유):
 *   1. HTML 폼 데이터를 Controller에 전달
 *   2. 파일 업로드 지원 (MultipartFile)
 *   3. Uppy 업로드 경로 지원 (attachmentPaths)
 *   4. 게시글 수정 시 파일 삭제 지원 (deletedFileIds)
 *   5. 비밀번호 평문 입력 (저장 시 BCrypt 해싱)
 *
 * Key Fields (주요 필드):
 *   - title: 제목
 *   - authorName: 작성자 이름
 *   - password: 비밀번호 (평문, 비공개글용)
 *   - content: 본문 (HTML)
 *   - secret: 비공개 여부
 *   - attachments: 직접 업로드 파일 목록 (MultipartFile)
 *   - attachmentPaths: Uppy 업로드된 파일 경로 (쉼표 구분)
 *   - deletedFileIds: 삭제할 첨부파일 ID (쉼표 구분, 수정 시)
 *
 * File Upload Methods (파일 업로드 방식):
 *   1. 직접 업로드: MultipartFile 리스트 (attachments)
 *   2. Uppy 임시 업로드: 파일 경로 문자열 (attachmentPaths)
 *   - 두 방식 모두 지원 (하위 호환성)
 *
 * Usage Examples (사용 예시):
 *   // Controller에서 폼 데이터 수신
 *   @PostMapping("/counsel/write")
 *   public String write(@ModelAttribute CounselPostWriteDto dto) {
 *       counselService.saveNew(dto);
 *       return "redirect:/counsel/list";
 *   }
 *
 *   // Uppy 업로드 경로 전달
 *   dto.setAttachmentPaths("2025/11/file1.jpg,2025/11/file2.pdf");
 *
 *   // 수정 시 파일 삭제
 *   dto.setDeletedFileIds("1,3,5");
 *
 * vs CounselPostDto:
 *   - CounselPostDto: 조회/표시용 (Entity → DTO)
 *   - CounselPostWriteDto: 작성/수정용 (Form → Service)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
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
