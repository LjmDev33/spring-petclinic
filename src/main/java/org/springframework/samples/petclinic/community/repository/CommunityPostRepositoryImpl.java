package org.springframework.samples.petclinic.community.repository;

/*
 * Project : spring-petclinic
 * File    : CommunityPostRepositoryImpl.java
 * Created : 2025-09-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */


import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.CommunityPost;
import org.springframework.samples.petclinic.community.QCommunityPost;

import java.util.List;


public class CommunityPostRepositoryImpl implements CommunityPostRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public CommunityPostRepositoryImpl(JPAQueryFactory queryFactory){
		this.queryFactory = queryFactory;
	}

	// 게시글 검색
	@Override
	public PageResponse<CommunityPost> search(String type, String keyword, Pageable pageable) {
		QCommunityPost post = QCommunityPost.communityPost;

		BooleanBuilder builder = new BooleanBuilder();
		if(keyword != null && !keyword.isEmpty()){
			switch (type){
				case "title":
					builder.and(post.title.containsIgnoreCase(keyword));
					break;
				case "content":
					builder.and(post.content.containsIgnoreCase(keyword));
					break;
				case "author":
					builder.and(post.author.containsIgnoreCase(keyword));
					break;
				default: // 전체 검색
					builder.and(
						post.title.containsIgnoreCase(keyword)
							.or(post.content.containsIgnoreCase(keyword))
							.or(post.author.containsIgnoreCase(keyword))
						);
					break;
			}
		}

		List<CommunityPost> content = queryFactory
			.selectFrom(post)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.id.asc())
			.fetch();

		long total = queryFactory.select(post.count().coalesce(0L)).from(post).where(builder).fetchOne();

		return new PageResponse<>(new PageImpl<>(content, pageable, total));
	}


}
