package org.springframework.samples.petclinic.counsel.mapper;

import org.springframework.samples.petclinic.counsel.dto.AttachmentDto;
import org.springframework.samples.petclinic.common.table.Attachment;
import org.springframework.stereotype.Component;

/**
 * Project : spring-petclinic
 * File    : AttachmentMapper.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   첨부파일 Entity ↔ DTO 변환 Mapper
 *
 * Purpose (만든 이유):
 *   1. Entity와 DTO 간의 변환 로직을 한 곳에서 관리
 *   2. 다운로드 URL 자동 생성
 *   3. 필드명 매핑 (originalFilename → originalFileName 등)
 *   4. null 안전성 (null 체크 후 변환)
 *
 * Key Features (주요 기능):
 *   - toDto(): Entity → DTO 변환
 *   - 다운로드 URL 자동 생성 (/counsel/download/{id})
 *   - null 방어 (entity가 null이면 null 반환)
 *   - Long → Integer 변환 (ID 필드)
 *
 * Why Component (왜 @Component인가):
 *   - CounselPostMapper에서 의존성 주입
 *   - Spring Bean으로 관리되어 싱글톤 보장
 *   - vs static: 재사용 가능
 *
 * Field Mapping (필드 매핑):
 *   - originalFilename → originalFileName (Entity → DTO)
 *   - contentType → mimeType (Entity → DTO)
 *   - id: Long → Integer 변환
 *
 * Download URL (다운로드 URL):
 *   - 자동 생성: "/counsel/download/" + id
 *   - Controller에서 이 URL로 다운로드 처리
 *
 * Usage Examples (사용 예시):
 *   // CounselPostMapper에서 사용
 *   dto.setAttachments(entity.getAttachments().stream()
 *       .map(attachmentMapper::toDto)
 *       .collect(Collectors.toList()));
 *
 *   // null 안전
 *   AttachmentDto dto = attachmentMapper.toDto(null); // null 반환
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Component
public class AttachmentMapper {

    /**
     * Attachment 엔티티를 AttachmentDto로 변환합니다.
     * @param attachment 변환할 Attachment 엔티티
     * @return 변환된 AttachmentDto
     */
    public AttachmentDto toDto(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        AttachmentDto dto = new AttachmentDto();
        dto.setId(attachment.getId().intValue()); // Long → Integer 변환
        dto.setOriginalFileName(attachment.getOriginalFilename()); // 필드명 변경
        dto.setFileSize(attachment.getFileSize());
        dto.setMimeType(attachment.getContentType()); // contentType → mimeType
        dto.setCreatedAt(attachment.getCreatedAt());
        // 다운로드 URL 자동 생성 (/counsel/download/{fileId})
        dto.setDownloadUrl("/counsel/download/" + attachment.getId());
        return dto;
    }
}

