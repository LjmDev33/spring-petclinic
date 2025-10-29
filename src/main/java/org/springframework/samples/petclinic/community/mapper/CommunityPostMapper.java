package org.springframework.samples.petclinic.community.mapper;

import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;

/*
 * Project : spring-petclinic
 * File    : CommunityPostMapper.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
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
