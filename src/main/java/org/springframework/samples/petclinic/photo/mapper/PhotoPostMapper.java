package org.springframework.samples.petclinic.photo.mapper;

import org.springframework.samples.petclinic.photo.dto.PhotoPostDto;
import org.springframework.samples.petclinic.photo.table.PhotoPost;

/**
 * Project : spring-petclinic
 * File    : PhotoPostMapper.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 Entity ↔ DTO 변환 Mapper
 *
 * Purpose (만든 이유):
 *   1. Entity와 DTO 간의 변환 로직을 한 곳에서 관리
 *   2. Service에서 변환 로직 중복 제거
 *   3. 썸네일 URL 포함 여부 등 필드 매핑 규칙 정의
 *   4. 양방향 변환 지원 (Entity ↔ DTO)
 *
 * Key Features (주요 기능):
 *   - toDto(): Entity → DTO 변환 (DB 조회 후 화면 전달용)
 *   - toEntity(): DTO → Entity 변환 (화면 입력 후 DB 저장용)
 *   - ID null 체크 (신규 등록 시 ID 없음)
 *
 * Why Static (왜 static인가):
 *   - 다른 의존성 필요 없음
 *   - 간단한 필드 복사만 수행
 *   - vs @Component: Bean 주입 불필요
 *
 * Mapping Rules (매핑 규칙):
 *   - 모든 필드를 1:1로 복사
 *   - toEntity() 시 ID가 null이면 설정 안 함 (신규 등록)
 *   - createdAt/updatedAt는 JPA가 자동 관리
 *
 * Usage Examples (사용 예시):
 *   // Entity → DTO (조회)
 *   PhotoPostDto dto = PhotoPostMapper.toDto(entity);
 *
 *   // DTO → Entity (등록/수정)
 *   PhotoPost entity = PhotoPostMapper.toEntity(dto);
 *
 *   // 목록 변환
 *   List<PhotoPostDto> dtoList = entityList.stream()
 *       .map(PhotoPostMapper::toDto)
 *       .collect(Collectors.toList());
 *
 * ID Handling (ID 처리):
 *   - toEntity(): ID가 null이면 설정 안 함 (JPA가 자동 생성)
 *   - toDto(): ID는 항상 복사
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class PhotoPostMapper {

	/**
	 * Entity를 DTO로 변환
	 */
	public static PhotoPostDto toDto(PhotoPost entity) {
		PhotoPostDto dto = new PhotoPostDto();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setContent(entity.getContent());
		dto.setAuthor(entity.getAuthor());
		dto.setThumbnailUrl(entity.getThumbnailUrl());
		dto.setViewCount(entity.getViewCount());
		dto.setLikeCount(entity.getLikeCount());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setUpdatedAt(entity.getUpdatedAt());
		return dto;
	}

	/**
	 * DTO를 Entity로 변환
	 */
	public static PhotoPost toEntity(PhotoPostDto dto) {
		PhotoPost entity = new PhotoPost();
		if (dto.getId() != null) {
			entity.setId(dto.getId());
		}
		entity.setTitle(dto.getTitle());
		entity.setContent(dto.getContent());
		entity.setAuthor(dto.getAuthor());
		entity.setThumbnailUrl(dto.getThumbnailUrl());
		entity.setViewCount(dto.getViewCount());
		entity.setLikeCount(dto.getLikeCount());
		return entity;
	}
}

