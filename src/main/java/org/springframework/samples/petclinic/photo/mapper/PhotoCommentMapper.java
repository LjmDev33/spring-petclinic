package org.springframework.samples.petclinic.photo.mapper;

import org.springframework.samples.petclinic.photo.dto.PhotoCommentDto;
import org.springframework.samples.petclinic.photo.table.PhotoComment;

/**
 * Project : spring-petclinic
 * File    : PhotoCommentMapper.java
 * Created : 2026-01-26
 * Author  : Jeongmin Lee
 *
 * Description :
 * 포토게시판 댓글 Entity ↔ DTO 변환 Mapper
 *
 * Key Features:
 * - toDto(): Entity → DTO 변환
 * - 부모 댓글 정보(답글 대상) 자동 설정
 */
public class PhotoCommentMapper {

	/**
	 * 댓글 Entity를 화면/응답용 DTO로 변환합니다.
	 */
	public static PhotoCommentDto toDto(PhotoComment entity) {
		PhotoCommentDto d = new PhotoCommentDto();
		d.setId(entity.getId());
		d.setContent(entity.getContent());
		d.setAuthorName(entity.getAuthorName());
		d.setStaffReply(entity.isStaffReply());
		d.setCreatedAt(entity.getCreatedAt());
		d.setPasswordHash(entity.getPasswordHash()); // 삭제/검증 로직을 위해 DTO에 전달 (보안상 화면 노출 주의)

		// 부모 댓글 정보 설정 (대댓글인 경우)
		if (entity.getParent() != null) {
			d.setParentId(entity.getParent().getId());
			d.setParentAuthorName(entity.getParent().getAuthorName());
		}

		// depth 초기값 설정 (Service의 Tree 빌딩 로직에서 재계산됨)
		d.setDepth(0);

		return d;
	}
}
