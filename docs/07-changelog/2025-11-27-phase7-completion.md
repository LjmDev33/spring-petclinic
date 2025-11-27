# Phase 7 완료 - 검색 기능 강화

**날짜**: 2025-11-27  
**작성자**: GitHub Copilot  
**버전**: 3.5.7  
**작업 분류**: 검색 기능 강화 + QueryDSL 고급 필터링

---

## 📋 작업 개요

### Phase 7: 검색 기능 강화 (100% 완료)
- **목표**: 날짜 범위 및 상태별 필터링 추가
- **영향 범위**: CounselPostRepositoryCustom, CounselPostRepositoryImpl, CounselService, CounselController
- **완료일**: 2025-11-27

---

## ✅ Phase 7 완료 사항

### 1️⃣ 고급 검색 Repository 레이어

**파일**: 
- `CounselPostRepositoryCustom.java`
- `CounselPostRepositoryImpl.java`

**추가된 메서드**:
```java
// Custom 인터페이스에 메서드 추가
PageResponse<CounselPost> advancedSearch(
    String type,
    String keyword,
    String status,
    java.time.LocalDateTime startDate,
    java.time.LocalDateTime endDate,
    Pageable pageable
);
```

**구현 내용 (QueryDSL)**:
```java
@Override
public PageResponse<CounselPost> advancedSearch(
    String type, String keyword, String status,
    LocalDateTime startDate, LocalDateTime endDate,
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
            CounselStatus counselStatus = CounselStatus.valueOf(status.toUpperCase());
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
        LocalDateTime endOfDay = endDate.plusDays(1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
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
```

**주요 특징**:
- ✅ **BooleanBuilder**: 동적 조건 조합
- ✅ **상태 필터**: WAIT, COMPLETE, END 중 선택
- ✅ **날짜 범위**: startDate(시작일), endDate(종료일) 포함
- ✅ **정렬**: 최신순 (createdAt DESC)
- ✅ **COUNT 쿼리 분리**: 성능 최적화

---

### 2️⃣ Service 레이어

**파일**: `CounselService.java`

**추가된 메서드**:
```java
/**
 * 고급 검색 (Phase 7: 검색 기능 강화)
 * - 날짜 범위, 상태별 필터링 추가
 */
public PageResponse<CounselPostDto> advancedSearch(
    String type,
    String keyword,
    String status,
    String startDateStr,
    String endDateStr,
    Pageable pageable) {
    
    // 문자열 날짜를 LocalDateTime으로 변환
    LocalDateTime startDate = null;
    LocalDateTime endDate = null;
    
    try {
        if (startDateStr != null && !startDateStr.isBlank()) {
            startDate = LocalDate.parse(startDateStr).atStartOfDay();
        }
        if (endDateStr != null && !endDateStr.isBlank()) {
            endDate = LocalDate.parse(endDateStr).atStartOfDay();
        }
    } catch (DateTimeParseException e) {
        log.error("Invalid date format: startDate={}, endDate={}", startDateStr, endDateStr);
        // 날짜 파싱 실패 시 null로 유지
    }
    
    // Repository 호출
    PageResponse<CounselPost> entityResponse = repository.advancedSearch(
        type, keyword, status, startDate, endDate, pageable);
    
    // Entity -> DTO 변환
    List<CounselPostDto> dtoList = entityResponse.getContent().stream()
        .map(postMapper::toDto)
        .collect(Collectors.toList());
    
    // 최근 댓글 요약 주입
    for (CounselPostDto d : dtoList) {
        commentRepository.findTopByPost_IdOrderByCreatedAtDesc(d.getId()).ifPresent(c -> {
            d.setLastCommentTitle("댓글");
            d.setLastCommentAuthor(c.getAuthorName());
            d.setLastCommentCreatedAt(c.getCreatedAt());
        });
    }
    
    Page<CounselPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityResponse.getTotalElements());
    return new PageResponse<>(dtoPage);
}
```

**주요 기능**:
- ✅ 날짜 문자열 파싱 (yyyy-MM-dd → LocalDateTime)
- ✅ Repository 호출 및 Entity → DTO 변환
- ✅ 최근 댓글 요약 주입
- ✅ 날짜 파싱 실패 시 안전하게 처리 (로그 + null 유지)

---

### 3️⃣ Controller 레이어

**파일**: `CounselController.java`

**수정된 메서드**:
```java
/**
 * 온라인상담 게시글 목록 (Phase 7: 검색 기능 강화)
 * - 기본 검색: 제목, 내용, 작성자
 * - 고급 검색: 날짜 범위, 상태별 필터링
 */
@GetMapping("/list")
public String list(
    @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
    @RequestParam(value = "type", required = false) String type,
    @RequestParam(value = "keyword", required = false) String keyword,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "startDate", required = false) String startDate,
    @RequestParam(value = "endDate", required = false) String endDate,
    Model model) {
    
    PageResponse<CounselPostDto> pageResponse;
    
    type = (type == null || type.isBlank()) ? "" : type;
    
    // Phase 7: 고급 검색 (날짜/상태 필터 포함)
    boolean hasAdvancedFilter = (status != null && !status.isBlank()) || 
                                (startDate != null && !startDate.isBlank()) || 
                                (endDate != null && !endDate.isBlank());
    
    if (hasAdvancedFilter || (keyword != null && !keyword.isBlank())) {
        // 고급 검색 또는 일반 검색
        pageResponse = counselService.advancedSearch(type, keyword, status, startDate, endDate, pageable);
    } else {
        // 전체 목록
        pageResponse = counselService.getPagedPosts(pageable);
    }
    
    model.addAttribute("page", pageResponse);
    model.addAttribute("posts", pageResponse.getContent());
    model.addAttribute("keyword", keyword);
    model.addAttribute("type", type);
    model.addAttribute("status", status);
    model.addAttribute("startDate", startDate);
    model.addAttribute("endDate", endDate);
    model.addAttribute("template", "counsel/counselList");
    
    return "fragments/layout";
}
```

**주요 특징**:
- ✅ **새 파라미터 추가**: status, startDate, endDate
- ✅ **고급 검색 조건 판단**: 날짜/상태 필터가 있으면 advancedSearch 호출
- ✅ **기존 기능 유지**: 키워드만 있으면 기본 검색, 아무것도 없으면 전체 목록
- ✅ **Model 추가**: 검색 조건을 템플릿에 전달 (검색 폼 유지용)

---

## 🔧 기술적 개선 사항

### 1. QueryDSL BooleanBuilder
- **동적 쿼리**: 조건이 있을 때만 WHERE 절에 추가
- **Type-safe**: 컴파일 타임에 오류 감지
- **가독성**: SQL과 유사한 직관적인 문법

### 2. 날짜 범위 필터링
```java
// startDate: 해당 날짜 00:00:00부터
if (startDate != null) {
    builder.and(post.createdAt.goe(startDate)); // >=
}

// endDate: 해당 날짜 23:59:59까지 포함
if (endDate != null) {
    LocalDateTime endOfDay = endDate.plusDays(1)
        .withHour(0).withMinute(0).withSecond(0).withNano(0);
    builder.and(post.createdAt.lt(endOfDay)); // <
}
```

### 3. 상태별 필터링
```java
// Enum 변환 및 잘못된 값 처리
try {
    CounselStatus counselStatus = CounselStatus.valueOf(status.toUpperCase());
    builder.and(post.status.eq(counselStatus));
} catch (IllegalArgumentException e) {
    // 잘못된 상태값이면 무시 (전체 조회)
}
```

### 4. 성능 최적화
- **COUNT 쿼리 분리**: 데이터 조회와 COUNT 쿼리를 분리하여 성능 향상
- **최신순 정렬**: `orderBy(post.createdAt.desc())` 적용
- **null 방지**: total이 null일 경우 0L 반환

---

## 📊 사용 예시

### 1. 기본 검색 (기존)
```
GET /counsel/list?keyword=예약&type=title
→ 제목에 "예약"이 포함된 게시글 검색
```

### 2. 상태별 필터링 (Phase 7)
```
GET /counsel/list?status=WAIT
→ 답변대기 상태의 게시글만 조회
```

### 3. 날짜 범위 필터링 (Phase 7)
```
GET /counsel/list?startDate=2025-11-01&endDate=2025-11-30
→ 11월 1일 ~ 11월 30일 사이 작성된 게시글 조회
```

### 4. 복합 검색 (Phase 7)
```
GET /counsel/list?keyword=수술&type=title&status=COMPLETE&startDate=2025-11-01&endDate=2025-11-30
→ 11월 중 "수술"이 제목에 포함되고 답변완료 상태인 게시글 검색
```

---

## 📝 문서 업데이트

### 업데이트된 문서
1. **NEXT_STEPS_PROPOSAL.md**
   - Phase 7 완료 상태 반영
   - 버전 1.6으로 갱신

2. **API_SPECIFICATION.md** (업데이트 예정)
   - 고급 검색 API 명세 추가
   - startDate, endDate, status 파라미터 설명

3. **ARCHITECTURE.md** (업데이트 예정)
   - QueryDSL Custom Repository 패턴 설명 추가

---

## 🎯 다음 단계 (향후 개선 사항)

### 우선순위 1: UI 개선
- **목표**: 날짜 범위 선택기 (DatePicker) 추가
- **기술**: jQuery UI Datepicker 또는 HTML5 Date Input
- **예상 소요 시간**: 1시간

### 우선순위 2: 검색 조건 저장
- **목표**: 사용자가 마지막 검색 조건을 기억 (세션 또는 쿠키)
- **예상 소요 시간**: 30분

### 우선순위 3: 검색 결과 엑셀 다운로드
- **목표**: 검색된 게시글 목록을 Excel 파일로 다운로드
- **기술**: Apache POI
- **예상 소요 시간**: 2-3시간

---

## 🏆 성과 요약

### Phase 7 (검색 기능 강화) - 100% 완료
- ✅ 날짜 범위 필터링 (startDate ~ endDate)
- ✅ 상태별 필터링 (WAIT, COMPLETE, END)
- ✅ QueryDSL BooleanBuilder 동적 쿼리
- ✅ 기존 검색 기능과 호환성 유지

### 코드 품질
- ✅ 컴파일 성공 (BUILD SUCCESSFUL)
- ✅ Type-safe 쿼리 (QueryDSL)
- ✅ 성능 최적화 (COUNT 쿼리 분리)
- ✅ 오류 처리 (날짜 파싱 실패 시 안전 처리)

### 확장성
- ✅ 새 필터 추가 용이 (BooleanBuilder 패턴)
- ✅ Community, Photo 패키지에도 동일 패턴 적용 가능
- ✅ 페이징 처리 유지

---

## 📊 전체 프로젝트 진행률

### 완료된 Phase
- ✅ **Phase 1**: 기본 기능 (온라인상담, 커뮤니티, FAQ, Photo)
- ✅ **Phase 2**: 좋아요 기능 (Counsel, Community, Photo)
- ✅ **Phase 3**: 첨부파일 관리 (게시글 수정 시 첨부파일 추가/삭제)
- ✅ **Phase 4**: 보안 강화 (파일 다운로드 권한, 작성자 권한, 멀티 로그인 제어)
- ✅ **Phase 5**: 사용자 경험 개선 (마이페이지)
- ✅ **Phase 7**: 검색 기능 강화 (날짜 범위, 상태별 필터링)

### 보류된 Phase
- ⏸️ **Phase 6**: 이메일 발송 기능 (SMTP 연동) - 마지막으로 연기

---

**작성 완료**: 2025-11-27  
**최종 검증**: ✅ 컴파일 성공, 기능 테스트 완료  
**문서 버전**: 1.0

