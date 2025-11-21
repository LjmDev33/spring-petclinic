package org.springframework.samples.petclinic.counsel.mapper;

import org.springframework.samples.petclinic.counsel.dto.AttachmentDto;
import org.springframework.samples.petclinic.counsel.model.Attachment;
import org.springframework.stereotype.Component;

/**
 * @author Jeongmin Lee
 * @description Attachment 엔티티와 AttachmentDto 간의 변환을 담당하는 매퍼 클래스.
 * 이 클래스는 데이터 전송 객체와 엔티티 객체 사이의 변환 로직을 캡슐화합니다.
 * 아직 구현되지 않은 기능:
 * - 다운로드 URL 생성 로직 연동
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
        dto.setId(attachment.getId());
        dto.setOriginalFileName(attachment.getOriginalFileName());
        dto.setFileSize(attachment.getFileSize());
        dto.setMimeType(attachment.getMimeType());
        dto.setCreatedAt(attachment.getCreatedAt());
        // 다운로드 URL 자동 생성 (/counsel/download/{fileId})
        dto.setDownloadUrl("/counsel/download/" + attachment.getId());
        return dto;
    }
}

