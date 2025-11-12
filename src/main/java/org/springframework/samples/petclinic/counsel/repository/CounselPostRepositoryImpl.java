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

/*
 * Project : spring-petclinic
 * File    : CounselPostRepositoryImpl.java
 * Created : 2025-10-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 커스텀 리포지토리 구현 (QueryDSL 기반 동적 검색/페이징)
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
}
