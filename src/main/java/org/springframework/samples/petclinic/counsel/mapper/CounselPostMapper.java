package org.springframework.samples.petclinic.counsel.mapper;

import org.springframework.samples.petclinic.counsel.dto.CounselPostDto;
import org.springframework.samples.petclinic.counsel.table.CounselPostAttachment;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@Component
public class CounselPostMapper {

	private final AttachmentMapper attachmentMapper;

	@Autowired
	public CounselPostMapper(AttachmentMapper attachmentMapper) {
		this.attachmentMapper = attachmentMapper;
	}

	public CounselPostDto toDto(CounselPost entity){
		CounselPostDto dto = new CounselPostDto();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setContent(entity.getContent());
		dto.setContentPath(entity.getContentPath());
		dto.setAuthorName(entity.getAuthorName());
		dto.setAuthorEmail(entity.getAuthorEmail());
		dto.setSecret(entity.isSecret());
		dto.setStatus(entity.getStatus());
		dto.setViewCount(entity.getViewCount());
		dto.setCommentCount(entity.getCommentCount());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setUpdatedAt(entity.getUpdatedAt());

		if (entity.getAttachments() != null) {
			dto.setAttachments(entity.getAttachments().stream()
				.map(CounselPostAttachment::getAttachment) // CounselPostAttachment에서 Attachment를 가져옴
				.map(attachmentMapper::toDto)
				.collect(Collectors.toList()));
		}

		return dto;
	}
}
