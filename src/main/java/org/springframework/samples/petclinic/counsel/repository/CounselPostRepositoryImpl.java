package org.springframework.samples.petclinic.counsel.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.samples.petclinic.counsel.table.QCounselPost;

import java.util.List;

/**
 * Project : spring-petclinic
 * File    : CounselPostRepositoryImpl.java
 * Created : 2025-10-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 QueryDSL Custom Repository 구현체
 *
 * Purpose (만든 이유):
 *   1. Spring Data JPA의 단순 CRUD로는 처리할 수 없는 동적 검색 구현
 *   2. 복잡한 WHERE 조건을 Type-safe하게 작성 (QueryDSL)
 *   3. Service 계층을 비즈니스 로직에만 집중시키기 위해 쿼리 로직 분리
 *   4. 검색 타입에 따른 유연한 조건 조합 (제목, 내용, 작성자, 전체)
 *   5. 페이징 처리 및 COUNT 쿼리 최적화
 *
 * Key Features (주요 기능):
 *   - 동적 검색 (BooleanBuilder 사용)
 *   - 검색 타입: title, content, author(authorName), 전체(기본값)
 *   - 대소문자 구분 없는 검색 (containsIgnoreCase)
 *   - 페이징 처리 (offset, limit)
 *   - COUNT 쿼리 분리 (성능 최적화)
 *
 * Why QueryDSL (QueryDSL을 선택한 이유):
 *   - Type-safe: 컴파일 타임에 오류 감지
 *   - 가독성: SQL과 유사한 직관적인 문법
 *   - 유지보수: 리팩토링 시 자동으로 쿼리 오류 감지
 *   - 동적 쿼리: BooleanBuilder로 조건 조합 용이
 *   - vs JPQL/Native Query: 문자열 오류 발생 가능성 제거
 *
 * Search Types (검색 타입):
 *   - "title": 제목만 검색
 *   - "content": 내용만 검색
 *   - "author" 또는 "authorName": 작성자만 검색
 *   - 기본값 (null 또는 기타): 제목 + 내용 + 작성자 전체 검색
 *
 * Performance (성능 최적화):
 *   - BooleanBuilder로 동적 조건만 WHERE에 추가 (불필요한 조건 제거)
 *   - COUNT 쿼리와 데이터 조회 쿼리 분리
 *   - fetchResults() 미사용 (Deprecated)
 *   - null 방지: total이 null일 경우 0L 반환
 *
 * Usage Examples (사용 예시):
 *   // 제목 검색
 *   PageResponse<CounselPost> results = repository.search("title", "예약", pageable);
 *
 *   // 전체 검색 (제목 + 내용 + 작성자)
 *   PageResponse<CounselPost> results = repository.search(null, "진료", pageable);
 *
 *   // 키워드 없이 전체 목록
 *   PageResponse<CounselPost> results = repository.search(null, null, pageable);
 *
 * How It Works (작동 방식):
 *   1. BooleanBuilder 생성 (동적 조건 컨테이너)
 *   2. keyword가 있으면 type에 따라 조건 추가
 *   3. 데이터 조회 쿼리 실행 (페이징 적용)
 *   4. COUNT 쿼리 실행 (전체 개수)
 *   5. Page 객체 생성 및 PageResponse 반환
 *
 * vs JPQL:
 *   - JPQL: 문자열 기반, 런타임 오류
 *   - QueryDSL: Q-Type 기반, 컴파일 타임 오류 감지
 *
 * Dependencies (의존 관계):
 *   - JPAQueryFactory: QueryDSL 쿼리 생성
 *   - QCounselPost: QueryDSL Q-Type (자동 생성)
 *   - BooleanBuilder: 동적 조건 조합
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
		QCounselPost post = QCounselPost.counselPost;

		BooleanBuilder builder = new BooleanBuilder();
		if (keyword != null && !keyword.isBlank()) {
			switch (type == null ? "" : type) {
				case "title":
					builder.and(post.title.containsIgnoreCase(keyword));
					break;
				case "content":
					builder.and(post.content.containsIgnoreCase(keyword));
					break;
				case "author":
				case "authorName":
					builder.and(post.authorName.containsIgnoreCase(keyword));
					break;
				default:
					builder.and(
						post.title.containsIgnoreCase(keyword)
							.or(post.content.containsIgnoreCase(keyword))
							.or(post.authorName.containsIgnoreCase(keyword))
					);
			}
		}

		List<CounselPost> content = queryFactory
			.selectFrom(post)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.id.asc())
			.fetch();

		Long total = queryFactory
			.select(post.count())
			.from(post)
			.where(builder)
			.fetchOne();

		Page<CounselPost> page = new PageImpl<>(content, pageable, total == null ? 0L : total);
		return new PageResponse<>(page);
	}

	/**
	 * 고급 검색 구현 (Phase 7: 검색 기능 강화)
	 *
	 * <p>추가된 필터링 옵션:</p>
	 * <ul>
	 *   <li>상태별 필터링: WAIT, COMPLETE, END</li>
	 *   <li>날짜 범위 필터링: 작성일 기준 (startDate ~ endDate)</li>
	 *   <li>기존 검색 타입: title, content, author, 전체</li>
	 * </ul>
	 *
	 * @param type 검색 타입
	 * @param keyword 검색 키워드
	 * @param status 상태 필터 (null이면 전체)
	 * @param startDate 시작 날짜 (null이면 제한 없음)
	 * @param endDate 종료 날짜 (null이면 제한 없음)
	 * @param pageable 페이징 정보
	 * @return 검색 결과 페이지
	 */
	@Override
	public PageResponse<CounselPost> advancedSearch(
		String type,
		String keyword,
		String status,
		java.time.LocalDateTime startDate,
		java.time.LocalDateTime endDate,
		Pageable pageable) {

		QCounselPost post = QCounselPost.counselPost;
		BooleanBuilder builder = new BooleanBuilder();

		// 1. 키워드 검색 (기존 로직)
		if (keyword != null && !keyword.isBlank()) {
			switch (type == null ? "" : type) {
				case "title":
					builder.and(post.title.containsIgnoreCase(keyword));
					break;
				case "content":
					builder.and(post.content.containsIgnoreCase(keyword));
					break;
				case "author":
				case "authorName":
					builder.and(post.authorName.containsIgnoreCase(keyword));
					break;
				default:
					builder.and(
						post.title.containsIgnoreCase(keyword)
							.or(post.content.containsIgnoreCase(keyword))
							.or(post.authorName.containsIgnoreCase(keyword))
					);
			}
		}

		// 2. 상태별 필터링 (Phase 7: 추가)
		if (status != null && !status.isBlank()) {
			try {
				org.springframework.samples.petclinic.counsel.CounselStatus counselStatus =
					org.springframework.samples.petclinic.counsel.CounselStatus.valueOf(status.toUpperCase());
				builder.and(post.status.eq(counselStatus));
			} catch (IllegalArgumentException e) {
				// 잘못된 상태값이면 무시 (전체 조회)
			}
		}

		// 3. 날짜 범위 필터링 (Phase 7: 추가)
		if (startDate != null) {
			builder.and(post.createdAt.goe(startDate)); // Greater or Equal (>=)
		}
		if (endDate != null) {
			// endDate는 해당 날짜의 23:59:59까지 포함
			java.time.LocalDateTime endOfDay = endDate.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
			builder.and(post.createdAt.lt(endOfDay)); // Less Than (<)
		}

		// 4. 데이터 조회
		List<CounselPost> content = queryFactory
			.selectFrom(post)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.createdAt.desc()) // 최신순 정렬
			.fetch();

		// 5. COUNT 쿼리
		Long total = queryFactory
			.select(post.count())
			.from(post)
			.where(builder)
			.fetchOne();

		Page<CounselPost> page = new PageImpl<>(content, pageable, total == null ? 0L : total);
		return new PageResponse<>(page);
	}
}
