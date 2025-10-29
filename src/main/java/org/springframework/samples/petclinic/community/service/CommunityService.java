package org.springframework.samples.petclinic.community.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;
import org.springframework.samples.petclinic.community.mapper.CommunityPostMapper;
import org.springframework.samples.petclinic.community.repository.CommunityPostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Project : spring-petclinic
 * File    : CommunityService.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: RDBMS만 사용하기때문에 구현체는 만들지않고 service에서만 모든 비즈니스로직 처리(mysql , oracle , postgre 등)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class CommunityService {

	private final CommunityPostRepository repository;
	private final CommunityPostRepository communityPostRepository;

	public CommunityService(CommunityPostRepository repository, CommunityPostRepository communityPostRepository) {
		this.repository = repository;
		this.communityPostRepository = communityPostRepository;
	}

	public PageResponse<CommunityPost> getPagedPosts(Pageable pageable) {
		return new PageResponse<>(repository.findAll(pageable));
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

	public PageResponse<CommunityPost> search(String type, String keyword, Pageable pageable) {
		return communityPostRepository.search(type,keyword,pageable);
	}

	public Optional<CommunityPost> getPrevPost(Long id){
		return communityPostRepository.getPrevPost(id);
	}

	public Optional<CommunityPost> getNextPost(Long id){
		return communityPostRepository.getNextPost(id);
	}
}
