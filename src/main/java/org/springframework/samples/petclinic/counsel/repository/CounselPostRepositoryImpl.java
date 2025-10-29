package org.springframework.samples.petclinic.counsel.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.table.CounselPost;

/*
 * Project : spring-petclinic
 * File    : CounselPostRepositoryImpl.java
 * Created : 2025-10-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class CounselPostRepositoryImpl implements CounselPostRepositoryCustom{

	private final JPAQueryFactory queryFactory;

	public CounselPostRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public PageResponse<CounselPost> search(String type, String keyword, Pageable pageable) {
		return null;
	}
}
