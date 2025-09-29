package org.springframework.samples.petclinic.community.service;

import jakarta.transaction.Transactional;
import org.springframework.samples.petclinic.community.CommunityPost;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;
import org.springframework.samples.petclinic.community.mapper.CommunityPostMapper;
import org.springframework.samples.petclinic.community.repository.CommunityPostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Project : spring-petclinic
 * File    : CommunityService.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class CommunityService {

	private final CommunityPostRepository repository;

	public CommunityService(CommunityPostRepository repository) {
		this.repository = repository;
	}

	/* 전체 게시글 리스트 가져오기 */
	public List<CommunityPostDto> getAllPosts() {
		return repository.findAll()
			.stream()
			.map(CommunityPostMapper::toDto)
			.collect(Collectors.toList());
	}

	public CommunityPostDto getPost(Long id) {
		CommunityPost entity = repository.findById(id).orElseThrow();
		return CommunityPostMapper.toDto(entity);
	}

	public CommunityPostDto createPost(CommunityPostDto dto) {
		CommunityPost entity = CommunityPostMapper.toEntity(dto);
		entity.setCreatedAt(LocalDateTime.now());
		CommunityPost saved = repository.save(entity);
		return CommunityPostMapper.toDto(saved);
	}
}
