package org.springframework.samples.petclinic.counsel.mapper;

import org.springframework.samples.petclinic.counsel.dto.CounselPostDto;
import org.springframework.samples.petclinic.counsel.table.CounselPostAttachment;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

/**
 * Project : spring-petclinic
 * File    : CounselPostMapper.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 Entity ↔ DTO 변환 Mapper
 *
 * Purpose (만든 이유):
 *   1. Entity와 DTO 간의 변환 로직을 한 곳에서 관리
 *   2. Service에서 변환 로직 중복 제거
 *   3. 필드 매핑 규칙을 명확하게 정의
 *   4. 첨부파일 목록 변환 (AttachmentMapper 재사용)
 *   5. 민감 정보 제외 (passwordHash는 DTO에 포함 안 됨)
 *
 * Key Features (주요 기능):
 *   - toDto(): Entity → DTO 변환 (DB 조회 후 화면 전달용)
 *   - 첨부파일 목록 변환 (CounselPostAttachment → AttachmentDto)
 *   - null 방어 (attachments가 null이면 변환 생략)
 *
 * Why Component (왜 @Component인가):
 *   - AttachmentMapper 의존성 주입 필요 (생성자 주입)
 *   - vs static: 다른 Mapper를 재사용할 수 있음
 *   - Spring Bean으로 관리되어 싱글톤 보장
 *
 * Mapping Rules (매핑 규칙):
 *   - Entity → DTO: 모든 필드 복사 (passwordHash 제외)
 *   - 첨부파일: CounselPostAttachment.getAttachment() → AttachmentDto
 *   - 비밀번호: passwordHash는 DTO에 노출 안 됨 (보안)
 *
 * Usage Examples (사용 예시):
 *   // Service에서 사용
 *   CounselPostDto dto = counselPostMapper.toDto(entity);
 *
 *   // 목록 변환 (Stream)
 *   List<CounselPostDto> dtoList = entityList.stream()
 *       .map(counselPostMapper::toDto)
 *       .collect(Collectors.toList());
 *
 * Security (보안):
 *   - passwordHash는 DTO에 포함하지 않음
 *   - password 필드는 입력용 (평문)
 *
 * Dependencies (의존 관계):
 *   - AttachmentMapper: 첨부파일 변환
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
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
