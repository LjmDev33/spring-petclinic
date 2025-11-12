package org.springframework.samples.petclinic.counsel.mapper;

import org.springframework.samples.petclinic.counsel.dto.CounselCommentDto;
import org.springframework.samples.petclinic.counsel.table.CounselComment;

/*
 * Project : spring-petclinic
 * File    : CounselCommentMapper.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 댓글 Entity ↔ DTO 변환 전용 매퍼
 *   미구현(후속): 리스트 변환 유틸, 마스킹/권한 기반 필드 필터링
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
		return d;
	}
}
