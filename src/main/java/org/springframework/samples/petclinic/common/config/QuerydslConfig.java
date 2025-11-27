package org.springframework.samples.petclinic.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Project : spring-petclinic
 * File    : QuerydslConfig.java
 * Created : 2025-09-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   QueryDSL 설정 (동적 쿼리 및 복잡한 검색 기능 지원)
 *
 * Purpose (만든 이유):
 *   1. JPAQueryFactory Bean을 등록하여 QueryDSL 사용 환경 구성
 *   2. 동적 쿼리 생성 및 복잡한 검색 조건 처리
 *   3. Type-safe 쿼리 작성으로 컴파일 타임 오류 감지
 *   4. Repository Custom 구현체에서 JPAQueryFactory 주입받아 사용
 *
 * What is QueryDSL:
 *   - Java 코드로 SQL/JPQL 쿼리를 작성할 수 있는 프레임워크
 *   - Q-Type 클래스를 통한 Type-safe 쿼리
 *   - 복잡한 WHERE 조건을 메서드 체이닝으로 표현
 *   - JPA의 Criteria API보다 직관적이고 간결한 문법
 *
 * Key Features (주요 기능):
 *   - JPAQueryFactory Bean 등록 (모든 Repository에서 주입 가능)
 *   - EntityManager를 통한 JPA 연동
 *   - 동적 검색 조건 (BooleanExpression 조합)
 *   - 페이징 및 정렬 지원
 *   - JOIN, SubQuery, Projection 등 고급 기능
 *
 * Usage Examples (사용 예시):
 *   // Repository Custom 구현체에서 사용
 *   @Repository
 *   public class PostRepositoryImpl implements PostRepositoryCustom {
 *       private final JPAQueryFactory queryFactory; // 자동 주입
 *       private final QPost post = QPost.post;
 *
 *       public List<Post> search(String keyword) {
 *           return queryFactory
 *               .selectFrom(post)
 *               .where(post.title.contains(keyword))
 *               .fetch();
 *       }
 *   }
 *
 * Applied Packages (적용된 패키지):
 *   - counsel: CounselPostRepositoryImpl (온라인상담 검색)
 *   - community: CommunityPostRepositoryImpl (공지사항 검색)
 *   - photo: PhotoPostRepositoryImpl (포토게시판 검색)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Configuration
public class QuerydslConfig {

	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager em) {
		return new JPAQueryFactory(em);
	}
}
