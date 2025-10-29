package org.springframework.samples.petclinic.counsel.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.repository.CounselPostRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.stereotype.Service;


/*
 * Project : spring-petclinic
 * File    : CounselService.java
 * Created : 2025-10-24
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
public class CounselService {

	private final CounselPostRepository repository;

	public CounselService(CounselPostRepository repository) {
		this.repository = repository;
	}

	/*게시글 화면 페이징 목록 표출*/
	public PageResponse<CounselPost> getPagedPosts(Pageable pageable){
		return new PageResponse<>(repository.findAll(pageable));
	}

	public PageResponse<CounselPost> search(String type, String keyword, Pageable pageable) {
		return repository.search(type,keyword,pageable);
	}
}
