# Phase 7 진행 중 - 검색 기능 강화 (패키지별 작업)

**날짜**: 2025-11-27  
**작성자**: GitHub Copilot  
**진행률**: Counsel 100%, Community 50%, FAQ 0%, Photo 0%  
**작업 분류**: 검색 기능 강화 (패키지별 순차 진행)

---

## 📋 현재 작업 상황

### ✅ **Counsel 패키지** - 100% 완료

**완료된 작업**:
1. **CounselPostRepositoryCustom.java**
   - advancedSearch 메서드 추가
   - 파라미터: type, keyword, status, startDate, endDate

2. **CounselPostRepositoryImpl.java**
   - QueryDSL BooleanBuilder 사용
   - 상태별 필터링 (WAIT, COMPLETE, END)
   - 날짜 범위 필터링 (startDate ~ endDate)

3. **CounselService.java**
   - advancedSearch 메서드 구현
   - 날짜 문자열 파싱 (yyyy-MM-dd → LocalDateTime)
   - Entity → DTO 변환

4. **CounselController.java**
   - status, startDate, endDate 파라미터 추가
   - 고급 검색 조건 판단 로직 추가

---

### ⏳ **Community 패키지** - 50% 완료

**완료된 작업**:
1. **CommunityPostRepositoryCustom.java** ✅
   ```java
   PageResponse<CommunityPost> advancedSearch(
       String type,
       String keyword,
       java.time.LocalDateTime startDate,
       java.time.LocalDateTime endDate,
       Pageable pageable
   );
   ```

2. **CommunityPostRepositoryImpl.java** ✅
   - QueryDSL BooleanBuilder 사용
   - 날짜 범위 필터링만 구현 (상태 없음)
   - 키워드 검색 (title, content, author)
   - 최신순 정렬 (createdAt DESC)

**남은 작업**:
3. **CommunityService.java** ❌
   - advancedSearch 메서드 추가 필요
   - 날짜 문자열 파싱 필요
   - Entity → DTO 변환 필요

4. **CommunityController.java** ❌
   - startDate, endDate 파라미터 추가 필요
   - 고급 검색 호출 로직 추가 필요

---

### ❌ **FAQ 패키지** - 0% 완료

**필요한 작업**:
1. **FaqPostRepositoryCustom.java**
   - advancedSearch 메서드 추가
   - 날짜 범위 필터링 (FAQ는 상태 없음)

2. **FaqPostRepositoryImpl.java**
   - QueryDSL 구현

3. **FaqService.java**
   - advancedSearch 메서드 추가

4. **FaqController.java**
   - 파라미터 및 호출 로직 추가

---

### ❌ **Photo 패키지** - 0% 완료

**필요한 작업**:
1. **PhotoPostRepositoryCustom.java**
   - advancedSearch 메서드 추가
   - 날짜 범위 필터링 (Photo는 상태 없음)

2. **PhotoPostRepositoryImpl.java**
   - QueryDSL 구현

3. **PhotoService.java**
   - advancedSearch 메서드 추가

4. **PhotoController.java**
   - 파라미터 및 호출 로직 추가

---

## 🔧 공통 패턴

### Repository Custom 인터페이스
```java
public interface [Package]PostRepositoryCustom {
    // 기존 search
    PageResponse<[Package]Post> search(String type, String keyword, Pageable pageable);
    
    // Phase 7: 고급 검색
    PageResponse<[Package]Post> advancedSearch(
        String type,
        String keyword,
        String status,  // Counsel만 해당
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
}
```

### Repository Impl 구현
```java
@Override
public PageResponse<[Package]Post> advancedSearch(...) {
    Q[Package]Post post = Q[Package]Post.[packageName]Post;
    BooleanBuilder builder = new BooleanBuilder();
    
    // 1. 키워드 검색 (title, content, author)
    if (keyword != null && !keyword.isBlank()) {
        switch (type) {
            case "title": builder.and(post.title.containsIgnoreCase(keyword)); break;
            // ...
        }
    }
    
    // 2. 상태별 필터링 (Counsel만)
    if (status != null && !status.isBlank()) {
        builder.and(post.status.eq(...));
    }
    
    // 3. 날짜 범위 필터링
    if (startDate != null) {
        builder.and(post.createdAt.goe(startDate));
    }
    if (endDate != null) {
        LocalDateTime endOfDay = endDate.plusDays(1).withHour(0)...;
        builder.and(post.createdAt.lt(endOfDay));
    }
    
    // 4. 조회 + COUNT
    List<[Package]Post> content = queryFactory.selectFrom(post)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(post.createdAt.desc())
        .fetch();
    
    Long total = queryFactory.select(post.count())
        .from(post).where(builder).fetchOne();
    
    return new PageResponse<>(new PageImpl<>(content, pageable, total));
}
```

### Service 메서드
```java
public PageResponse<[Package]PostDto> advancedSearch(
    String type, String keyword, 
    String status,  // Counsel만
    String startDateStr, String endDateStr,
    Pageable pageable) {
    
    // 날짜 파싱
    LocalDateTime startDate = null;
    LocalDateTime endDate = null;
    try {
        if (startDateStr != null) startDate = LocalDate.parse(startDateStr).atStartOfDay();
        if (endDateStr != null) endDate = LocalDate.parse(endDateStr).atStartOfDay();
    } catch (DateTimeParseException e) {
        log.error("Invalid date format");
    }
    
    // Repository 호출
    PageResponse<[Package]Post> entityResponse = 
        repository.advancedSearch(type, keyword, status, startDate, endDate, pageable);
    
    // DTO 변환
    List<[Package]PostDto> dtoList = entityResponse.getContent().stream()
        .map(mapper::toDto).collect(Collectors.toList());
    
    return new PageResponse<>(new PageImpl<>(dtoList, pageable, entityResponse.getTotalElements()));
}
```

### Controller 파라미터
```java
@GetMapping("/list")
public String list(
    @PageableDefault(...) Pageable pageable,
    @RequestParam(value = "type", required = false) String type,
    @RequestParam(value = "keyword", required = false) String keyword,
    @RequestParam(value = "status", required = false) String status,  // Counsel만
    @RequestParam(value = "startDate", required = false) String startDate,
    @RequestParam(value = "endDate", required = false) String endDate,
    Model model) {
    
    boolean hasAdvancedFilter = (status != null && !status.isBlank()) || 
                                (startDate != null && !startDate.isBlank()) || 
                                (endDate != null && !endDate.isBlank());
    
    if (hasAdvancedFilter || (keyword != null && !keyword.isBlank())) {
        pageResponse = service.advancedSearch(type, keyword, status, startDate, endDate, pageable);
    } else {
        pageResponse = service.getPagedPosts(pageable);
    }
    
    // Model에 검색 조건 추가
    model.addAttribute("status", status);
    model.addAttribute("startDate", startDate);
    model.addAttribute("endDate", endDate);
    
    return "fragments/layout";
}
```

---

## 🎯 다음 작업 순서

### 1단계: Community 패키지 완성 (50% → 100%)
- [ ] CommunityService.advancedSearch() 추가
- [ ] CommunityController 파라미터 및 호출 로직 추가
- [ ] 컴파일 검증

### 2단계: FAQ 패키지 (0% → 100%)
- [ ] FaqPostRepositoryCustom.advancedSearch() 추가
- [ ] FaqPostRepositoryImpl 구현
- [ ] FaqService.advancedSearch() 추가
- [ ] FaqController 파라미터 및 호출 로직 추가
- [ ] 컴파일 검증

### 3단계: Photo 패키지 (0% → 100%)
- [ ] PhotoPostRepositoryCustom.advancedSearch() 추가
- [ ] PhotoPostRepositoryImpl 구현
- [ ] PhotoService.advancedSearch() 추가
- [ ] PhotoController 파라미터 및 호출 로직 추가
- [ ] 컴파일 검증

### 4단계: 통합 테스트
- [ ] 서버 실행
- [ ] 각 게시판별 검색 기능 테스트
- [ ] 날짜 범위 필터링 테스트
- [ ] 상태별 필터링 테스트 (Counsel)

---

## 📝 특이사항

### 패키지별 차이점

| 패키지 | 상태 필터 | 날짜 필터 | 작성자 필드명 |
|--------|-----------|-----------|---------------|
| **Counsel** | ✅ status (WAIT/COMPLETE/END) | ✅ | authorName |
| **Community** | ❌ | ✅ | author |
| **FAQ** | ❌ | ✅ | (확인 필요) |
| **Photo** | ❌ | ✅ | (확인 필요) |

### 검증 완료
- ✅ CounselPostRepositoryImpl: 컴파일 성공
- ✅ CommunityPostRepositoryImpl: 컴파일 성공, 깨진 글자 수정 완료

---

## 🔍 문제 해결 이력

### Community 패키지
- **문제**: 152번 줄에 `e탸` 깨진 글자 발견
- **해결**: 제거 후 컴파일 성공

---

**최종 업데이트**: 2025-11-27  
**다음 작업**: Community Service → FAQ 전체 → Photo 전체

