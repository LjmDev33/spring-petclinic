package org.springframework.samples.petclinic.community.repository;

/**
 * Project : spring-petclinic
 * File    : CommunityPostRepositoryImpl.java
 * Created : 2025-09-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   공지사항 QueryDSL Custom Repository 구현체
 *
 * Purpose (만든 이유):
 *   1. Spring Data JPA의 단순 CRUD를 넘어선 동적 검색 구현
 *   2. 이전글/다음글 조회 기능 (공지사항 특화 기능)
 *   3. QueryDSL로 Type-safe한 쿼리 작성
 *   4. Service 계층의 비즈니스 로직과 쿼리 로직 분리
 *   5. 복잡한 쿼리의 재사용 및 테스트 용이성 향상
 *
 * Key Features (주요 기능):
 *   - 동적 검색 (제목, 내용, 작성자, 전체)
 *   - 이전글 조회 (getPrevPost): 현재 글보다 작은 ID 중 가장 큰 값
 *   - 다음글 조회 (getNextPost): 현재 글보다 큰 ID 중 가장 작은 값
 *   - 페이징 처리 (offset, limit)
 *   - COUNT 쿼리 최적화 (coalesce로 null 방지)
 *
 * Why Custom Repository (Custom Repository를 만든 이유):
 *   1. 책임 분리: Service는 비즈니스 로직, Repository는 쿼리 로직
 *   2. 재사용: 동일한 쿼리를 여러 Service에서 사용 가능
 *   3. 테스트: 쿼리 로직만 독립적으로 테스트 가능
 *   4. 유지보수: 쿼리 변경 시 Repository만 수정
 *
 * Search Types (검색 타입):
 *   - "title": 제목만 검색
 *   - "content": 내용만 검색
 *   - "author": 작성자만 검색
 *   - 기본값: 제목 + 내용 + 작성자 전체 검색
 *
 * Navigation Logic (이전글/다음글 로직):
 *   - 이전글: WHERE id < 현재ID ORDER BY id DESC LIMIT 1
 *   - 다음글: WHERE id > 현재ID ORDER BY id ASC LIMIT 1
 *   - Optional 반환: 없으면 Optional.empty()
 *
 * Performance (성능 최적화):
 *   - BooleanBuilder로 동적 조건만 WHERE에 추가
 *   - COUNT 쿼리에 coalesce(0L) 사용 (null 방지)
 *   - LIMIT 1로 이전글/다음글 1건만 조회
 *   - Optional 패턴으로 null 안전성 확보
 *
 * Usage Examples (사용 예시):
 *   // 제목 검색
 *   PageResponse<CommunityPost> results = repository.search("title", "공지", pageable);
 *
 *   // 이전글 조회
 *   Optional<CommunityPost> prev = repository.getPrevPost(10L);
 *
 *   // 다음글 조회
 *   Optional<CommunityPost> next = repository.getNextPost(10L);
 *
 * How It Works (작동 방식):
 *   1. search(): BooleanBuilder로 동적 조건 생성 → 페이징 조회 → COUNT
 *   2. getPrevPost(): id < currentId → ORDER BY DESC → LIMIT 1
 *   3. getNextPost(): id > currentId → ORDER BY ASC → LIMIT 1
 *
 * Architecture (아키텍처):
 *   Controller → Service (비즈니스 로직) → Repository (쿼리 로직) → DB
 *   - Repository: Custom 인터페이스 + Impl 구현체
 *   - Service: Custom 메서드 호출만 (쿼리 작성 X)
 *
 * Dependencies (의존 관계):
 *   - JPAQueryFactory: QueryDSL 쿼리 생성
 *   - QCommunityPost: QueryDSL Q-Type
 *   - BooleanBuilder: 동적 조건 조합
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

	/**
	 * 고급 검색 구현 (Phase 7: 검색 기능 강화)
	 *
	 * <p>추가된 필터링 옵션:</p>
	 * <ul>
	 *   <li>날짜 범위 필터링: 작성일 기준 (startDate ~ endDate)</li>
	 *   <li>기존 검색 타입: title, content, author, 전체</li>
	 *   <li>Community는 상태(status) 없음</li>
	 * </ul>
	 *
	 * @param type 검색 타입
	 * @param keyword 검색 키워드
	 * @param startDate 시작 날짜 (null이면 제한 없음)
	 * @param endDate 종료 날짜 (null이면 제한 없음)
	 * @param pageable 페이징 정보
	 * @return 검색 결과 페이지
	 */
	@Override
	public PageResponse<CommunityPost> advancedSearch(
		String type,
		String keyword,
		java.time.LocalDateTime startDate,
		java.time.LocalDateTime endDate,
		Pageable pageable) {

		QCommunityPost post = QCommunityPost.communityPost;
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
					builder.and(post.author.containsIgnoreCase(keyword));
					break;
				default:
					builder.and(
						post.title.containsIgnoreCase(keyword)
							.or(post.content.containsIgnoreCase(keyword))
							.or(post.author.containsIgnoreCase(keyword))
					);
			}
		}

		// 2. 날짜 범위 필터링 (Phase 7: 추가)
		if (startDate != null) {
			builder.and(post.createdAt.goe(startDate)); // Greater or Equal (>=)
		}
		if (endDate != null) {
			// endDate는 해당 날짜의 23:59:59까지 포함
			java.time.LocalDateTime endOfDay = endDate.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
			builder.and(post.createdAt.lt(endOfDay)); // Less Than (<)
		}

		// 3. 데이터 조회
		List<CommunityPost> content = queryFactory
			.selectFrom(post)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.createdAt.desc()) // 최신순 정렬
			.fetch();

		// 4. COUNT 쿼리
		Long total = queryFactory
			.select(post.count())
			.from(post)
			.where(builder)
			.fetchOne();

		org.springframework.data.domain.Page<CommunityPost> page = new PageImpl<>(content, pageable, total == null ? 0L : total);
		return new PageResponse<>(page);
	}
}
