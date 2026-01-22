package org.springframework.samples.petclinic.photo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
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
 *
 * Purpose (만든 이유):
 *   1. 동적 검색 쿼리 구현 (제목, 내용, 작성자)
 *   2. 인기 게시글 조회 기능 (조회수 + 좋아요 기반)
 *   3. 작성자별 게시글 조회 (마이페이지 연동)
 *   4. BooleanExpression으로 더 명확한 조건 조합
 *   5. 성능 최적화 (불필요한 WHERE 절 제거)
 *
 * Key Features (주요 기능):
 *   - 동적 검색: 제목, 내용, 작성자, 제목+내용
 *   - 작성자별 조회: findByAuthor (마이페이지용)
 *   - 인기 게시글: findPopularPosts (조회수 + 좋아요 순)
 *   - 페이징 처리: offset, limit 적용
 *   - COUNT 쿼리 분리: 성능 최적화
 *
 * Why BooleanExpression (BooleanExpression을 선택한 이유):
 *   - vs BooleanBuilder: 더 명확한 조건 조합
 *   - 메서드 추출 가능: createSearchCondition()
 *   - null 반환 가능: 조건이 없으면 WHERE 절 자체를 생략
 *   - 재사용 가능: 다른 쿼리에서도 동일 조건 사용
 *
 * Search Types (검색 타입):
 *   - "title": 제목만 검색
 *   - "content": 내용만 검색
 *   - "titleandcontent", "title_and_content": 제목 + 내용 검색
 *   - "author": 작성자만 검색
 *   - 기본값: 제목 + 내용 검색
 *
 * Popular Posts Logic (인기 게시글 로직):
 *   - 정렬 기준: (viewCount + likeCount) DESC, createdAt DESC
 *   - 조회수와 좋아요 수를 합산하여 인기도 계산
 *   - 최신 게시글을 우선 (동점일 경우)
 *
 * Performance Optimization (성능 최적화):
 *   - keyword가 null이면 BooleanExpression null 반환 (WHERE 절 생략)
 *   - trim()으로 불필요한 공백 제거
 *   - fetchResults() 미사용 (Deprecated)
 *   - COUNT 쿼리와 데이터 조회 쿼리 분리
 *   - null 방지: total이 null일 경우 0L 반환
 *
 * Usage Examples (사용 예시):
 *   // 제목 검색
 *   PageResponse<PhotoPost> results = repository.search("title", "강아지", pageable);
 *
 *   // 작성자별 조회
 *   PageResponse<PhotoPost> myPosts = repository.findByAuthor("user01", pageable);
 *
 *   // 인기 게시글 Top 10
 *   List<PhotoPost> popular = repository.findPopularPosts(10);
 *
 * How It Works (작동 방식):
 *   1. createSearchCondition(): keyword와 type에 따라 BooleanExpression 생성
 *   2. keyword가 null이면 null 반환 → WHERE 절 생략
 *   3. 데이터 조회 쿼리 실행 (페이징 적용)
 *   4. COUNT 쿼리 실행 (전체 개수)
 *   5. Page 객체 생성 및 PageResponse 반환
 *
 * vs BooleanBuilder:
 *   - BooleanBuilder: and(), or() 체이닝 방식
 *   - BooleanExpression: 메서드 추출 가능, null 안전
 *
 * Dependencies (의존 관계):
 *   - JPAQueryFactory: QueryDSL 쿼리 생성
 *   - QPhotoPost: QueryDSL Q-Type
 *   - BooleanExpression: 동적 조건 (null 가능)
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

	@Override
	public String getBoardOnwerId(long id) {
		return queryFactory
			.select(photoPost.user.username)
			.from(photoPost)
			.where(photoPost.id.eq(id))
			.fetchOne();
	}
}

