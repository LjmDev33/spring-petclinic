package org.springframework.samples.petclinic.counsel.mapper;

import org.springframework.samples.petclinic.counsel.dto.CounselCommentDto;
import org.springframework.samples.petclinic.counsel.table.CounselComment;

/**
 * Project : spring-petclinic
 * File    : CounselCommentMapper.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 댓글 Entity ↔ DTO 변환 Mapper
 *
 * Purpose (만든 이유):
 *   1. Entity와 DTO 간의 변환 로직을 한 곳에서 관리
 *   2. 부모 댓글 정보 자동 설정 (parentId, parentAuthorName)
 *   3. Tree 구조 지원을 위한 초기값 설정 (depth)
 *   4. Service에서 변환 로직 중복 제거
 *
 * Key Features (주요 기능):
 *   - toDto(): Entity → DTO 변환
 *   - 부모 댓글 정보 자동 설정
 *   - depth 초기값 0 설정 (Service에서 재계산)
 *
 * Why Static (왜 static인가):
 *   - 다른 의존성 필요 없음
 *   - 간단한 필드 복사만 수행
 *   - vs @Component: Bean 주입 불필요
 *
 * Mapping Rules (매핑 규칙):
 *   - 모든 필드 1:1 복사
 *   - parent가 있으면 parentId, parentAuthorName 설정
 *   - depth는 0으로 초기화 (Service에서 Tree 구조 생성 시 재계산)
 *
 * Usage Examples (사용 예시):
 *   // Service에서 Entity → DTO 변환
 *   CounselCommentDto dto = CounselCommentMapper.toDto(entity);
 *
 *   // Tree 구조 생성 전 변환
 *   List<CounselCommentDto> dtoList = entityList.stream()
 *       .map(CounselCommentMapper::toDto)
 *       .collect(Collectors.toList());
 *
 * Tree Structure (트리 구조):
 *   - toDto()는 단순 변환만 수행
 *   - Tree 구조 (children, depth 계산)는 Service에서 처리
 *   - CounselService.getCommentsForPost() 참조
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class CounselCommentMapper {
	/**
	 * 댓글 Entity를 화면/응답용 DTO로 변환합니다.
	 */
	public static CounselCommentDto toDto(CounselComment entity){
		CounselCommentDto d = new CounselCommentDto();
		d.setId(entity.getId());
		d.setContent(entity.getContent());
		d.setAuthorName(entity.getAuthorName());
		d.setStaffReply(entity.isStaffReply());
		d.setCreatedAt(entity.getCreatedAt());
		d.setPasswordHash(entity.getPasswordHash());

		// 부모 댓글 정보 설정
		if (entity.getParent() != null) {
			d.setParentId(entity.getParent().getId());
			d.setParentAuthorName(entity.getParent().getAuthorName());
		}

		// depth 초기값 설정 (Service에서 재계산됨)
		d.setDepth(0);

		return d;
	}
}
