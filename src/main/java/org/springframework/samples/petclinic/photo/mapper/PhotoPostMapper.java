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
 *   포토게시판 Entity ↔ DTO 변환 매퍼
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

