package org.springframework.samples.petclinic.community.repository;

/*
 * Project : spring-petclinic
 * File    : CommunityPostRepositoryImpl.java
 * Created : 2025-09-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO:
 *    본 클래스는 Spring Data JPA Repository의 확장 구현체로,
 *     단순 CRUD로 처리할 수 없는 복잡한 검색/정렬/페이징 쿼리를
 *     QueryDSL을 사용해 구현하기 위해 분리된 커스텀 레이어입니다.
 *     Service 레이어에서는 비즈니스 로직만 담당하도록 하고,
 *     쿼리 작성 및 데이터 접근 로직은 RepositoryImpl에서 수행함으로써
 *     역할을 명확히 분리하고 유지보수성을 높였습니다.
 *     즉, Repository 인터페이스는 Custom 인터페이스를 상속받아
 *     기본 CRUD와 커스텀 동적 쿼리를 함께 제공하도록 설계되었습니다.
 *     - 주요 장점:
 *       1) Service 로직 단순화 (비즈니스 흐름만 담당)
 *       2) 복잡한 QueryDSL 로직의 재사용 및 테스트 용이
 *       3) Controller → Service → Repository 간의 책임 분리 명확화
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */


import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.community.table.QCommunityPost;

import java.util.List;
import java.util.Optional;


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

	@Override
	public Optional<CommunityPost> getPrevPost(Long id) {
		QCommunityPost post = QCommunityPost.communityPost;
		CommunityPost result = queryFactory.selectFrom(post).where(post.id.lt(id)).orderBy(post.id.desc()).limit(1).fetchOne();
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<CommunityPost> getNextPost(Long id) {
		QCommunityPost post = QCommunityPost.communityPost;
		CommunityPost result = queryFactory.selectFrom(post).where(post.id.gt(id)).orderBy(post.id.asc()).limit(1).fetchOne();
		return Optional.ofNullable(result);
	}


}
