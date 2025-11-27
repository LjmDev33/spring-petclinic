package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.counsel.table.CounselPost;

import java.util.Optional;

/*
 * Project : spring-petclinic
 * File    : CounselPostRepositoryCustom.java
 * Created : 2025-09-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 커스텀 JPA Repository이며, JPA/QueryDSL 프로젝트에서 사용하는 Repository 확장 패턴
 *         방식 : Repository → Custom 인터페이스 → Impl 구현체 → Service에서 호출
 *         쿼리/검색 조건 로직은 전부 Impl 안으로 들어가고, Service는 비즈니스 로직 중심 (트랜잭션, 순서 제어, 예외처리) 에 집중 가능
 *         기존 Spring
 *         장점
 *         1. 책임 분리 명확 : Service는 “비즈니스 로직”, RepositoryImpl은 “쿼리 로직”만 담당. 코드 역할이 명확해져서 가독성과 유지보수 향상.
 *         2. 테스트 용이성 : 쿼리 로직만 따로 RepositoryImplTest로 단위 테스트 가능. Service 단위 테스트에서는 Mock Repository로 빠르게 검증 가능.
 *         3. QueryDSL과 궁합 최고 :복잡한 동적 쿼리를 구현할 때 Service가 더럽혀지지 않음. QueryDSL 관련 의존성(JPAQueryFactory)은 Repository 쪽에만 한정됨.
 *         4. 확장성 : 같은 엔티티라도 CustomImpl만 추가하면 별도 확장 가능. 예: CommunityPostRepositoryImpl 외에 CommunityPostSearchRepositoryImpl 등 다형성 구조 지원.
 *         5. 유지보수 & 협업 용이 : 팀원 간 역할 분리 명확. DB 담당자(쿼리 담당)와 비즈니스 담당자가 서로의 코드에 의존 안 함.
 *         단점
 *         1. 클래스/파일 수 증가 : 게시판 수만큼 RepositoryCustom, RepositoryImpl 파일이 늘어남. 구조는 깔끔하지만 세팅이 많아짐.
 *         2. 입문자에겐 진입장벽 : 스프링 데이터 JPA의 기본 동작을 이해해야 Impl이 정상 인식됨(Impl 네이밍 규칙 등). 초반에 혼란스럽기 쉬움
 *         3. 단순 쿼리에도 오버엔지니어링 가능 : CRUD 몇 줄이면 되는 걸 CustomImpl로 빼면 관리 오히려 불편해짐. “복잡한 동적 쿼리”일 때만 가치가 큼.
 *         4. Service와 Repository의 경계가 모호해질 수 있음 : 쿼리 관련 비즈니스 로직을 어디까지 Impl에 둘지 경계 설정이 필요. 안 하면 중복/혼동 생김.
 *		   커스텀과 impl구조를 사용하는 이유
 *         저는 RepositoryCustom + Impl 구조를 사용했습니다. 이유는 QueryDSL을 통한 동적 검색 쿼리를 Service에서 직접 구현하면 비즈니스 로직이 복잡해지고 테스트가 어려워지기 때문입니다.
 * 		   RepositoryImpl 쪽에서 쿼리를 캡슐화하면 Service는 비즈니스 로직에 집중할 수 있고, 쿼리 로직만 단위 테스트로 따로 검증할 수 있습니다.
 * 		   단, 단순 CRUD만 있는 경우에는 오히려 구조가 과해질 수 있으므로, 저는 복잡한 조건 검색이나 다중 조인 쿼리가 필요한 모듈에 한해 CustomImpl을 적용합니다.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CounselPostRepositoryCustom {
	/**
	 * 기본 검색 (제목, 내용, 작성자)
	 * @param type 검색 타입 (title, content, author)
	 * @param keyword 검색 키워드
	 * @param pageable 페이징 정보
	 * @return 검색 결과 페이지
	 */
	PageResponse<CounselPost> search(String type, String keyword, Pageable pageable);

	/**
	 * 고급 검색 (날짜 범위, 상태별 필터링 추가) - Phase 7
	 * @param type 검색 타입 (title, content, author, 전체)
	 * @param keyword 검색 키워드
	 * @param status 상태 필터 (WAIT, COMPLETE, END, null=전체)
	 * @param startDate 시작 날짜 (null 가능)
	 * @param endDate 종료 날짜 (null 가능)
	 * @param pageable 페이징 정보
	 * @return 검색 결과 페이지
	 */
	PageResponse<CounselPost> advancedSearch(
		String type,
		String keyword,
		String status,
		java.time.LocalDateTime startDate,
		java.time.LocalDateTime endDate,
		Pageable pageable
	);
}
