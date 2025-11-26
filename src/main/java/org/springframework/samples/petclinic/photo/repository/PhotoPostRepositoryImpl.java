package org.springframework.samples.petclinic.photo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.photo.table.PhotoPost;
import org.springframework.samples.petclinic.photo.table.QPhotoPost;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project : spring-petclinic
 * File    : PhotoPostRepositoryImpl.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 QueryDSL Custom Repository 구현체
 *   - 동적 쿼리 생성 (검색 조건에 따라 유연하게 대응)
 *   - COUNT 쿼리 최적화 (불필요한 JOIN 제거)
 *   - 페이징 처리 성능 최적화
 *
 * Performance:
 *   - fetchResults() 대신 fetch() + fetchCount() 분리 (Deprecated 회피)
 *   - 동적 조건에서 null 체크로 불필요한 WHERE 절 제거
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Repository
public class PhotoPostRepositoryImpl implements PhotoPostRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QPhotoPost photoPost = QPhotoPost.photoPost;

	public PhotoPostRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public PageResponse<PhotoPost> search(String type, String keyword, Pageable pageable) {
		// 동적 검색 조건 생성
		BooleanExpression searchCondition = createSearchCondition(type, keyword);

		// 데이터 조회 쿼리 (페이징 적용)
		List<PhotoPost> content = queryFactory
			.selectFrom(photoPost)
			.where(searchCondition)
			.orderBy(photoPost.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// COUNT 쿼리 (페이징에 필요)
		Long total = queryFactory
			.select(photoPost.count())
			.from(photoPost)
			.where(searchCondition)
			.fetchOne();

		// null 방지
		total = (total != null) ? total : 0L;

		Page<PhotoPost> page = new PageImpl<>(content, pageable, total);
		return new PageResponse<>(page);
	}

	@Override
	public PageResponse<PhotoPost> findByAuthor(String author, Pageable pageable) {
		List<PhotoPost> content = queryFactory
			.selectFrom(photoPost)
			.where(photoPost.author.eq(author))
			.orderBy(photoPost.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(photoPost.count())
			.from(photoPost)
			.where(photoPost.author.eq(author))
			.fetchOne();

		total = (total != null) ? total : 0L;

		Page<PhotoPost> page = new PageImpl<>(content, pageable, total);
		return new PageResponse<>(page);
	}

	@Override
	public List<PhotoPost> findPopularPosts(int limit) {
		// 조회수 + 좋아요 수 합산하여 인기 게시글 선정
		return queryFactory
			.selectFrom(photoPost)
			.orderBy(
				photoPost.viewCount.add(photoPost.likeCount).desc(),
				photoPost.createdAt.desc()
			)
			.limit(limit)
			.fetch();
	}

	/**
	 * 검색 타입에 따른 동적 조건 생성
	 * - I/O 최소화: 불필요한 조건 제거 (keyword가 null이면 조건 자체를 생성하지 않음)
	 */
	private BooleanExpression createSearchCondition(String type, String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return null; // 검색 조건 없음
		}

		String trimmedKeyword = keyword.trim();

		switch (type != null ? type.toLowerCase() : "") {
			case "title":
				return photoPost.title.containsIgnoreCase(trimmedKeyword);
			case "content":
				return photoPost.content.containsIgnoreCase(trimmedKeyword);
			case "titleandcontent":
			case "title_and_content":
				return photoPost.title.containsIgnoreCase(trimmedKeyword)
					.or(photoPost.content.containsIgnoreCase(trimmedKeyword));
			case "author":
				return photoPost.author.containsIgnoreCase(trimmedKeyword);
			default:
				// 기본값: 제목 + 내용 검색
				return photoPost.title.containsIgnoreCase(trimmedKeyword)
					.or(photoPost.content.containsIgnoreCase(trimmedKeyword));
		}
	}
}

