package org.springframework.samples.petclinic.photo.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.photo.dto.PhotoPostDto;
import org.springframework.samples.petclinic.photo.mapper.PhotoPostMapper;
import org.springframework.samples.petclinic.photo.repository.PhotoPostRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPost;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Project : spring-petclinic
 * File    : PhotoService.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 비즈니스 로직
 */
@Service
@Transactional
public class PhotoService {

	private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

	private final PhotoPostRepository repository;

	public PhotoService(PhotoPostRepository repository) {
		this.repository = repository;
	}

	/**
	 * 페이징된 게시글 목록 조회
	 */
	public PageResponse<PhotoPostDto> getPagedPosts(Pageable pageable) {
		Page<PhotoPost> entityPage = repository.findAll(pageable);
		List<PhotoPostDto> dtoList = entityPage.getContent()
			.stream()
			.map(PhotoPostMapper::toDto)
			.collect(Collectors.toList());
		Page<PhotoPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityPage.getTotalElements());
		return new PageResponse<>(dtoPage);
	}

	/**
	 * 게시글 상세 조회
	 */
	public PhotoPostDto getPost(Long id) {
		PhotoPost entity = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

		// 조회수 증가
		entity.setViewCount(entity.getViewCount() + 1);
		repository.save(entity);

		return PhotoPostMapper.toDto(entity);
	}

	/**
	 * 게시글 작성
	 * - 썸네일이 없으면 content에서 첫 번째 이미지 추출
	 */
	public PhotoPostDto createPost(PhotoPostDto dto) {
		// 썸네일이 없으면 content에서 첫 번째 이미지 추출
		if ((dto.getThumbnailUrl() == null || dto.getThumbnailUrl().isBlank()) && dto.getContent() != null) {
			String extractedThumbnail = extractFirstImageFromHtml(dto.getContent());
			if (extractedThumbnail != null) {
				dto.setThumbnailUrl(extractedThumbnail);
				log.info("썸네일 자동 추출: {}", extractedThumbnail);
			}
		}

		PhotoPost entity = PhotoPostMapper.toEntity(dto);
		PhotoPost saved = repository.save(entity);
		log.info("포토게시글 작성: ID={}, 제목={}", saved.getId(), saved.getTitle());
		return PhotoPostMapper.toDto(saved);
	}

	/**
	 * HTML 내용에서 첫 번째 이미지 URL 추출
	 */
	private String extractFirstImageFromHtml(String html) {
		if (html == null || html.isBlank()) {
			return null;
		}

		// <img src="..." 패턴 찾기
		int imgStart = html.indexOf("<img");
		if (imgStart == -1) {
			return null;
		}

		int srcStart = html.indexOf("src=\"", imgStart);
		if (srcStart == -1) {
			srcStart = html.indexOf("src='", imgStart);
			if (srcStart == -1) {
				return null;
			}
			srcStart += 5; // "src='" 길이
		} else {
			srcStart += 5; // "src=\"" 길이
		}

		int srcEnd = html.indexOf("\"", srcStart);
		if (srcEnd == -1) {
			srcEnd = html.indexOf("'", srcStart);
		}

		if (srcEnd == -1 || srcEnd <= srcStart) {
			return null;
		}

		return html.substring(srcStart, srcEnd);
	}

	/**
	 * 게시글 수정
	 */
	public PhotoPostDto updatePost(Long id, PhotoPostDto dto) {
		PhotoPost entity = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

		entity.setTitle(dto.getTitle());
		entity.setContent(dto.getContent());
		entity.setThumbnailUrl(dto.getThumbnailUrl());

		PhotoPost updated = repository.save(entity);
		log.info("포토게시글 수정: ID={}", id);
		return PhotoPostMapper.toDto(updated);
	}

	/**
	 * 게시글 삭제 (Soft Delete)
	 */
	public void deletePost(Long id) {
		PhotoPost entity = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

		repository.delete(entity); // @SQLDelete로 Soft Delete
		log.info("포토게시글 삭제: ID={}", id);
	}
}

