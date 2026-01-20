# 프로젝트 성능 및 효율성 개선 규칙

**작성일**: 2025-11-26  
**최종 업데이트**: 2025-12-03  
**목적**: I/O 최소화, 트래픽 병목 방지, 효율적인 예외 처리, 동시성 제어

---

## 📋 1. I/O 최소화 규칙

### 1.1 파일 I/O 최적화

#### 원칙
- 파일 경로는 **상대 경로**로 저장
- 파일 읽기는 **BufferedInputStream** 사용
- 파일 쓰기는 **BufferedOutputStream** 사용
- **try-with-resources** 필수 사용 (자동 close)

#### 예제
```java
// ❌ 비효율적
public void saveFile(byte[] data, String path) {
    FileOutputStream fos = new FileOutputStream(path);
    fos.write(data);
    fos.close(); // 예외 발생 시 리소스 누수
}

// ✅ 효율적
public void saveFile(byte[] data, String path) {
    try (BufferedOutputStream bos = new BufferedOutputStream(
            new FileOutputStream(path), 8192)) {
        bos.write(data);
    } catch (IOException e) {
        throw new FileException(ErrorCode.FILE_WRITE_ERROR, e);
    }
}
```

### 1.2 데이터베이스 I/O 최적화

#### 원칙
- **N+1 문제 방지**: Fetch Join 또는 EntityGraph 사용
- **Batch Insert/Update**: JPA batch size 설정
- **불필요한 SELECT 방지**: exists() 활용

#### application-dev.yml 설정
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50 # Batch 처리 크기
        order_inserts: true # INSERT 정렬
        order_updates: true # UPDATE 정렬
```

#### QueryDSL 최적화 예제
```java
// ❌ N+1 문제 발생
List<Post> posts = repository.findAll();
for (Post post : posts) {
    post.getComments().size(); // 각 Post마다 SELECT
}

// ✅ Fetch Join으로 한 번에 조회
List<Post> posts = queryFactory
    .selectFrom(post)
    .leftJoin(post.comments).fetchJoin()
    .fetch();
```

---

## 🚦 2. 트래픽 병목 방지 규칙

### 2.1 페이징 처리 필수

#### 원칙
- 모든 목록 조회는 **Pageable** 사용
- 기본 페이지 크기: **10~20개**
- 최대 페이지 크기: **100개** 제한

#### Controller 예제
```java
@GetMapping("/list")
public String list(
    @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) 
    Pageable pageable,
    Model model
) {
    PageResponse<PostDto> page = service.getPagedPosts(pageable);
    model.addAttribute("page", page);
    return "fragments/layout";
}
```

### 2.2 COUNT 쿼리 최적화

#### 원칙
- COUNT 쿼리는 **SELECT 쿼리와 분리**
- 불필요한 JOIN 제거
- fetchResults() 사용 금지 (Deprecated)

#### QueryDSL 예제
```java
// ❌ fetchResults() 사용 (Deprecated)
QueryResults<Post> results = queryFactory
    .selectFrom(post)
    .fetchResults(); // COUNT + SELECT 동시 수행

// ✅ fetch() + fetchCount() 분리
List<Post> content = queryFactory
    .selectFrom(post)
    .offset(pageable.getOffset())
    .limit(pageable.getPageSize())
    .fetch();

Long total = queryFactory
    .select(post.count())
    .from(post)
    .fetchOne();
```

### 2.3 조회수 중복 방지 (세션 기반) ⭐프로젝트 실제 적용

#### 원칙
- **세션 기반** 중복 방지 (간단하고 효과적)
- 브라우저 종료 시 세션 초기화
- 세션 메모리 부담 최소화 (Set<Long>만 저장)

#### Controller 구현 예제
```java
@GetMapping("/detail/{id}")
public String detail(@PathVariable Long id, HttpSession session) {
    // 1. 세션에서 조회한 게시글 ID Set 가져오기
    Set<Long> viewedPosts = (Set<Long>) session.getAttribute("viewedCounselPosts");
    if (viewedPosts == null) {
        viewedPosts = new HashSet<>();
    }
    
    // 2. 처음 조회하는 게시글이면 조회수 증가
    if (!viewedPosts.contains(id)) {
        counselService.incrementViewCount(id);
        viewedPosts.add(id);
        session.setAttribute("viewedCounselPosts", viewedPosts);
    }
    
    // 3. 게시글 상세 정보 조회
    CounselPostDto post = counselService.getPostDetail(id);
    model.addAttribute("post", post);
    return "counsel/counselDetail";
}
```

#### Service 구현 예제
```java
@Service
@Transactional
public class CounselService {
    
    /**
     * 조회수 증가 (예외 처리 포함)
     * - 조회수 증가 실패는 치명적이지 않으므로 예외를 던지지 않음
     */
    public void incrementViewCount(Long postId) {
        try {
            CounselPost entity = repository.findById(postId).orElse(null);
            if (entity != null) {
                entity.setViewCount(entity.getViewCount() + 1);
                repository.save(entity);
            }
        } catch (Exception e) {
            log.error("Error incrementing view count for postId={}: {}", postId, e.getMessage());
            // 조회수 증가 실패 시에도 서비스는 정상 동작
        }
    }
}
```

#### 장점
- ✅ 구현 간단 (세션만 사용)
- ✅ 중복 조회 방지
- ✅ 브라우저 종료 시 자동 초기화
- ✅ 예외 발생 시에도 서비스 안정성 유지

---

## 🛡️ 3. 효율적인 예외 처리 규칙

### 3.1 Custom Exception 체계

#### 계층 구조
```
BaseException (추상)
├── BusinessException (비즈니스 로직 오류)
├── EntityNotFoundException (엔티티 미존재)
└── FileException (파일 I/O 오류)
```

#### 사용 원칙
- **IllegalArgumentException** → **BusinessException** 전환
- **NullPointerException** 사전 방지 (Optional 활용)
- **로그 레벨 구분**: WARN (예상 가능) / ERROR (예상 불가능)

#### 예제
```java
// ❌ 단순 예외 던지기
public Post getPost(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Post not found"));
}

// ✅ Custom Exception 사용
public Post getPost(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> EntityNotFoundException.of("Post", id));
}
```

### 3.2 GlobalExceptionHandler

#### 원칙
- REST API: **JSON 응답** (ErrorResponse)
- 일반 화면: **Thymeleaf 에러 페이지** (ModelAndView)
- 모든 예외 **로그 기록** (추적 가능)

#### 구현 예제
```java
@ExceptionHandler(BusinessException.class)
public Object handleBusinessException(BusinessException ex, HttpServletRequest request) {
    log.warn("BusinessException: code={}, message={}", ex.getCode(), ex.getMessage());
    
    if (isApiRequest(request)) {
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.of(ex, request.getRequestURI()));
    } else {
        return createErrorView(ex.getStatus(), ex.getMessage(), request);
    }
}
```

---

## 🔄 4. 동시성 문제 방지 규칙

### 4.1 ACID 트랜잭션 보장 ⭐NEW (2025-12-03)

#### ACID 속성 (Atomicity, Consistency, Isolation, Durability)

모든 비즈니스 로직은 다음 4가지 속성을 보장해야 합니다:

1. **Atomicity (원자성)**: 트랜잭션의 모든 작업이 완료되거나 전혀 수행되지 않음
2. **Consistency (일관성)**: 트랜잭션 전후로 데이터베이스의 일관성 유지
3. **Isolation (격리성)**: 동시 실행 중인 트랜잭션들이 서로 영향을 미치지 않음
4. **Durability (지속성)**: 트랜잭션 완료 후 결과가 영구적으로 저장됨

#### 격리 수준 (Isolation Level) 선택 가이드

```java
// 일반적인 CRUD 작업 (기본값)
@Transactional(isolation = Isolation.READ_COMMITTED)
public void savePost(CounselPostDto dto) {
    // ...
}

// 동일 트랜잭션 내 여러 번 읽을 때
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void processWithMultipleReads(Long id) {
    Post post1 = repository.findById(id).orElseThrow();
    // ... 다른 작업
    Post post2 = repository.findById(id).orElseThrow();
    // post1과 post2가 동일함을 보장
}

// 완벽한 격리 필요 시 (성능 저하 주의)
@Transactional(isolation = Isolation.SERIALIZABLE)
public void criticalOperation() {
    // 은행 거래, 재고 관리 등
}
```

#### 좋아요 기능 ACID 적용 예시 ⭐프로젝트 실제 적용

```java
@Service
@Transactional
public class CounselService {
    
    /**
     * 좋아요 토글 (ACID 보장)
     * - Atomicity: 좋아요 추가/삭제 + 카운트 증감이 원자적으로 수행
     * - Consistency: 좋아요 테이블과 게시글의 like_count가 항상 일치
     * - Isolation: READ_COMMITTED 격리 수준으로 동시성 제어
     * - Durability: 커밋 후 데이터 영구 저장
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public boolean toggleLike(Long postId, String username) {
        try {
            // 1. 기존 좋아요 확인
            Optional<CounselPostLikes> existing = likesRepository
                .findByPostIdAndUsername(postId, username);
            
            if (existing.isPresent()) {
                // 2-1. 좋아요 취소 (Atomicity 보장)
                likesRepository.delete(existing.get());
                decrementLikeCount(postId);
                log.info("Like removed: postId={}, username={}", postId, username);
                return false;
            } else {
                // 2-2. 좋아요 추가 (Atomicity 보장)
                CounselPostLikes like = new CounselPostLikes();
                like.setPostId(postId);
                like.setUsername(username);
                likesRepository.save(like);
                incrementLikeCount(postId);
                log.info("Like added: postId={}, username={}", postId, username);
                return true;
            }
            // 3. 트랜잭션 커밋 (Durability 보장)
        } catch (Exception e) {
            // 4. 예외 발생 시 롤백 (Atomicity 보장)
            log.error("Failed to toggle like: postId={}, username={}", postId, username, e);
            throw new BusinessException(ErrorCode.LIKE_TOGGLE_FAILED, e);
        }
    }
    
    private void incrementLikeCount(Long postId) {
        CounselPost post = repository.findById(postId).orElseThrow();
        post.setLikeCount(post.getLikeCount() + 1);
        repository.save(post);
    }
    
    private void decrementLikeCount(Long postId) {
        CounselPost post = repository.findById(postId).orElseThrow();
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        repository.save(post);
    }
}
```

### 4.2 트랜잭션 범위 최소화

#### 원칙
- **@Transactional**은 Service 계층에만 사용
- 읽기 전용: **@Transactional(readOnly = true)**
- 긴 트랜잭션 분리: 여러 메서드로 나눔
- **트랜잭션 시간 목표**: 1~2초 이내

#### 예제
```java
// ❌ 트랜잭션이 너무 큼
@Transactional
public void processOrder(Order order) {
    validateOrder(order);          // 1초
    sendEmail(order);              // 3초 (외부 API 호출)
    updateInventory(order);        // 1초
    saveOrder(order);              // 1초
    // 총 6초 동안 트랜잭션 유지 (병목)
}

// ✅ 트랜잭션 분리
public void processOrder(Order order) {
    validateOrder(order);          // 트랜잭션 X
    sendEmail(order);              // 트랜잭션 X (비동기 권장)
    
    saveOrderWithTransaction(order); // 트랜잭션 O (2초만 유지)
}

@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
private void saveOrderWithTransaction(Order order) {
    updateInventory(order);
    saveOrder(order);
}
```

### 4.3 낙관적 락 (Optimistic Locking)

#### 원칙
- 동시 수정 가능성이 **낮은** 경우 사용
- **@Version** 어노테이션 활용
- 충돌 시 재시도 로직 구현
- 조회수 증가, 좋아요 카운트 등에 적합

#### Entity 예제
```java
@Entity
public class CounselPost extends BaseEntity {
    @Version
    @Column(name = "version")
    private Long version; // 낙관적 락 버전 관리
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    // ...existing code...
}
```

#### Service 재시도 로직
```java
@Service
public class CounselService {
    
    private static final int MAX_RETRY = 3;
    
    /**
     * 조회수 증가 (낙관적 락 + 재시도)
     */
    public void incrementViewCount(Long postId) {
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            try {
                incrementViewCountInternal(postId);
                return; // 성공
            } catch (OptimisticLockException e) {
                attempt++;
                log.warn("View count update conflict, retry {}/{}", attempt, MAX_RETRY);
                if (attempt >= MAX_RETRY) {
                    log.error("Failed to increment view count after {} retries", MAX_RETRY);
                    // 조회수 증가 실패는 치명적이지 않으므로 예외를 던지지 않음
                }
            }
        }
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    private void incrementViewCountInternal(Long postId) {
        CounselPost post = repository.findById(postId).orElseThrow();
        post.setViewCount(post.getViewCount() + 1);
        repository.save(post); // version 자동 증가
    }
}
```

### 4.4 비관적 락 (Pessimistic Locking)

#### 원칙
- 동시 수정 가능성이 **높은** 경우 사용
- **SELECT ... FOR UPDATE** 쿼리 실행
- 재고 관리, 포인트 차감 등에 적합

#### Repository 예제
```java
public interface CounselPostRepository extends JpaRepository<CounselPost, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CounselPost p WHERE p.id = :id")
    Optional<CounselPost> findByIdWithLock(@Param("id") Long id);
}
```

#### Service 예제
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void decrementStock(Long productId, int quantity) {
    // SELECT ... FOR UPDATE로 행 잠금
    Product product = productRepository.findByIdWithLock(productId).orElseThrow();
    
    if (product.getStock() < quantity) {
        throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
    }
    
    product.setStock(product.getStock() - quantity);
    productRepository.save(product);
    // 트랜잭션 커밋 시 잠금 해제
}
```

---

## 📦 5. 캐싱 전략 규칙

### 5.1 적용 대상

#### 캐싱 대상 (자주 조회, 변경 적음)
- ✅ 시스템 설정 (SystemConfig)
- ✅ 공지사항 (읽기 전용)
- ✅ FAQ (자주 변경되지 않음)

#### 캐싱 비대상
- ❌ 사용자 게시글 (실시간 변경)
- ❌ 댓글 (실시간 변경)

### 5.2 Spring Cache 설정

#### application-dev.yml
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m # 10분 후 만료
    cache-names:
      - systemConfig
      - faqList
      - noticeList
```

#### CacheConfig 클래스 ⭐프로젝트 실제 적용
```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()); // 캐시 통계 기록
        return cacheManager;
    }
}
```

#### Service 예제 - SystemConfig 캐싱
```java
@Service
@Transactional
public class SystemConfigService {
    
    /**
     * 시스템 설정 조회 (캐싱)
     * - 자주 조회되고 변경이 적은 데이터
     * - 10분 동안 캐시 유지
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    @Transactional(readOnly = true)
    public SystemConfig getConfig(String configKey) {
        log.debug("Cache MISS: systemConfig [{}]", configKey);
        return repository.findByConfigKey(configKey)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SYSTEM_CONFIG_NOT_FOUND));
    }
    
    /**
     * 시스템 설정 수정 (캐시 무효화)
     */
    @CacheEvict(value = "systemConfig", key = "#config.configKey")
    public void updateConfig(SystemConfig config) {
        log.info("Cache EVICT: systemConfig [{}]", config.getConfigKey());
        repository.save(config);
    }
    
    /**
     * 전체 시스템 설정 캐시 초기화
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void clearAllCache() {
        log.info("Cache EVICT ALL: systemConfig");
    }
}
```

#### Service 예제 - FAQ 목록 캐싱
```java
@Service
@Transactional
public class FaqService {
    
    /**
     * FAQ 목록 조회 (캐싱)
     * - 카테고리별로 캐시 저장
     * - 10분 동안 캐시 유지
     */
    @Cacheable(value = "faqList", key = "#category")
    @Transactional(readOnly = true)
    public List<FaqPostDto> getFaqListByCategory(String category) {
        log.debug("Cache MISS: faqList [{}]", category);
        List<FaqPost> entities = repository.findByCategoryAndDelFlagFalse(category);
        return entities.stream()
            .map(faqMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * FAQ 작성/수정/삭제 시 해당 카테고리 캐시 무효화
     */
    @CacheEvict(value = "faqList", key = "#post.category")
    public void saveOrUpdate(FaqPost post) {
        log.info("Cache EVICT: faqList [{}]", post.getCategory());
        repository.save(post);
    }
}
```

### 5.3 캐시 모니터링

#### 캐시 통계 확인
```java
@RestController
@RequestMapping("/admin/cache")
public class CacheMonitorController {
    
    @Autowired
    private CacheManager cacheManager;
    
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
                    ((CaffeineCache) cache).getNativeCache();
                
                stats.put(cacheName, nativeCache.stats());
            }
        });
        
        return stats;
    }
}
```

---

## 🌐 6. 네트워크 최적화 규칙

### 6.1 HTTP 응답 압축

#### application-dev.yml
```yaml
server:
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,application/javascript,application/json
    min-response-size: 1024 # 1KB 이상만 압축
```

### 6.2 정적 리소스 캐싱

#### WebConfig 설정
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)); // 1년 캐싱
    }
}
```

---

## 📊 7. 모니터링 및 로깅 규칙

### 7.1 로그 레벨 구분

#### 원칙
- **DEBUG**: 개발 중 상세 정보
- **INFO**: 정상 동작 흐름
- **WARN**: 예상 가능한 오류 (비즈니스 로직 위반)
- **ERROR**: 예상 불가능한 오류 (시스템 장애)

#### 예제
```java
// 비즈니스 로직 오류
log.warn("Invalid password attempt: userId={}", userId);

// 시스템 장애
log.error("Database connection failed", exception);
```

### 7.2 성능 로깅

#### Slow Query 로깅
```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
logging:
  level:
    org.hibernate.stat: DEBUG
    org.hibernate.SQL: DEBUG
```

---

## ✅ 8. 체크리스트

### 신규 기능 개발 시 필수 점검 사항

#### 기본 규칙
- [ ] **I/O 최소화**: try-with-resources 사용
- [ ] **페이징 처리**: Pageable 파라미터 추가
- [ ] **Custom Exception**: BusinessException 사용
- [ ] **N+1 문제**: Fetch Join 또는 EntityGraph
- [ ] **로그 기록**: 중요 동작은 INFO 이상
- [ ] **예외 처리**: try-catch + 의미 있는 에러 메시지

#### ACID 트랜잭션 ⭐NEW (2025-12-03)
- [ ] **트랜잭션 범위**: Service 계층에만 적용
- [ ] **격리 수준**: READ_COMMITTED (기본값) 사용
- [ ] **트랜잭션 시간**: 1~2초 이내로 최소화
- [ ] **읽기 전용**: @Transactional(readOnly = true) 명시
- [ ] **롤백 설정**: rollbackFor = Exception.class 명시
- [ ] **원자성 보장**: 관련 작업이 모두 성공하거나 모두 실패

#### 동시성 제어
- [ ] **낙관적 락**: @Version 사용 (조회수, 좋아요 등)
- [ ] **비관적 락**: @Lock 사용 (재고 관리 등)
- [ ] **재시도 로직**: OptimisticLockException 처리
- [ ] **동시성 테스트**: 여러 스레드에서 동시 요청 검증

#### 캐싱
- [ ] **캐싱 대상**: 자주 조회, 변경 적음 데이터만
- [ ] **캐시 만료**: expireAfterWrite 10분 설정
- [ ] **캐시 무효화**: CacheEvict 적절히 사용
- [ ] **캐시 통계**: recordStats() 활성화

#### 성능 최적화
- [ ] **COUNT 쿼리**: SELECT와 분리 실행
- [ ] **Batch 처리**: batch_size 50 설정
- [ ] **정적 리소스**: 1년 캐싱 (max-age=365days)
- [ ] **HTTP 압축**: 1KB 이상 파일 압축 활성화

---

## 📊 9. 성능 모니터링 지표

### 측정 대상
1. **응답 시간**: 평균 < 500ms, P95 < 1s
2. **트랜잭션 시간**: 평균 < 2s
3. **캐시 적중률**: > 80%
4. **N+1 쿼리**: 0건
5. **Slow Query**: < 1s

### 모니터링 도구
- **Hibernate Statistics**: SQL 쿼리 수 측정
- **Caffeine Stats**: 캐시 적중률 측정
- **Spring Actuator**: 메트릭 수집
- **DB 슬로우 쿼리 로그**: 느린 쿼리 탐지

---

**작성 완료일**: 2025-11-26  
**최종 업데이트**: 2025-12-03  
**다음 검토 예정일**: 2025-12-10

