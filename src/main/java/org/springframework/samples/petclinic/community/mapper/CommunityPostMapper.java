package org.springframework.samples.petclinic.community.mapper;

import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;

/**
 * Project : spring-petclinic
 * File    : CommunityPostMapper.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   공지사항 Entity ↔ DTO 변환 Mapper
 *
 * Purpose (만든 이유):
 *   1. Entity와 DTO 간의 변환 로직을 한 곳에서 관리
 *   2. Service에서 변환 로직 중복 제거
 *   3. 필드 매핑 규칙을 명확하게 정의
 *   4. static 메서드로 간단하게 사용 (의존성 불필요)
 *
 * Key Features (주요 기능):
 *   - toDto(): Entity → DTO 변환 (DB 조회 후 화면 전달용)
 *   - toEntity(): DTO → Entity 변환 (화면 입력 후 DB 저장용)
 *   - 양방향 변환 지원
 *
 * Why Static (왜 static인가):
 *   - 다른 의존성 필요 없음 (AttachmentMapper 같은 Bean 주입 불필요)
 *   - 간단한 필드 복사만 수행
 *   - vs @Component: 의존성 주입이 필요없는 단순 변환
 *
 * Mapping Rules (매핑 규칙):
 *   - 모든 필드를 1:1로 복사
 *   - Entity → DTO: DB 조회 결과를 화면에 전달
 *   - DTO → Entity: 화면 입력을 DB에 저장
 *
 * Usage Examples (사용 예시):
 *   // Entity → DTO
 *   CommunityPostDto dto = CommunityPostMapper.toDto(entity);
 *
 *   // DTO → Entity
 *   CommunityPost entity = CommunityPostMapper.toEntity(dto);
 *
 *   // 목록 변환 (Stream)
 *   List<CommunityPostDto> dtoList = entityList.stream()
 *       .map(CommunityPostMapper::toDto)
 *       .collect(Collectors.toList());
 *
 * vs CounselPostMapper:
 *   - CounselPostMapper: @Component, AttachmentMapper 의존
 *   - CommunityPostMapper: static, 의존성 없음
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class CommunityPostMapper {

	// TODO 목적: DB에서 꺼낸 Entity 객체를 화면/응답 전용 DTO 객체로 변환 (DB -> 화면 / 화면출력전용)
	public static CommunityPostDto toDto(CommunityPost entity) {
		CommunityPostDto dto = new CommunityPostDto();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setContent(entity.getContent());
		dto.setAuthor(entity.getAuthor());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setViewCount(entity.getViewCount());
		dto.setLikeCount(entity.getLikeCount());

		// Phase 3: 첨부파일 목록 변환
		if (entity.getAttachments() != null && !entity.getAttachments().isEmpty()) {
			entity.getAttachments().forEach(postAttachment -> {
				if (postAttachment.getAttachment() != null && !postAttachment.getAttachment().isDelFlag()) {
					CommunityPostDto.AttachmentInfo info = new CommunityPostDto.AttachmentInfo(
						postAttachment.getAttachment().getId(),
						postAttachment.getAttachment().getOriginalFilename(),
						postAttachment.getAttachment().getFileSize()
					);
					dto.getAttachments().add(info);
				}
			});
		}

		return dto;
	}

	// TODO 목적: 화면/요청에서 받은 DTO 객체를 DB 저장용 Entity 객체로 변환 (화면 -> DB / 여기서 세팅한 값만 DB에 반영)
	public static CommunityPost toEntity(CommunityPostDto dto) {
		CommunityPost entity = new CommunityPost();
		entity.setId(dto.getId());
		entity.setTitle(dto.getTitle());
		entity.setContent(dto.getContent());
		entity.setAuthor(dto.getAuthor());
		entity.setCreatedAt(dto.getCreatedAt());
		entity.setViewCount(dto.getViewCount());
		entity.setLikeCount(dto.getLikeCount());
		return entity;
	}
}
