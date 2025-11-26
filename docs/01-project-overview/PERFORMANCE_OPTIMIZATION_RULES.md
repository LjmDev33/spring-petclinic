# 프로젝트 성능 및 효율성 개선 규칙

**작성일**: 2025-11-26  
**목적**: I/O 최소화, 트래픽 병목 방지, 효율적인 예외 처리

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

### 2.3 조회수 중복 방지 (캐싱)

#### 원칙
- 세션 + IP + 쿠키 3단계 검증
- 쿠키 만료 시간: **24시간**
- 세션 메모리 부담 최소화

#### 구현 예제
```java
// 세션에 조회한 게시글 ID 저장
Set<Long> viewedPosts = (Set<Long>) session.getAttribute("viewedPosts");
if (viewedPosts == null) {
    viewedPosts = new HashSet<>();
}

// IP 기반 중복 방지
String clientIp = getClientIp(request);
String viewKey = postId + "_" + clientIp;

// 쿠키 기반 중복 방지 (24시간)
String cookieName = "post_view_" + postId;
Cookie viewCookie = new Cookie(cookieName, "viewed");
viewCookie.setMaxAge(24 * 60 * 60);
viewCookie.setHttpOnly(true);
response.addCookie(viewCookie);
```

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

### 4.1 트랜잭션 범위 최소화

#### 원칙
- **@Transactional**은 Service 계층에만 사용
- 읽기 전용: **@Transactional(readOnly = true)**
- 긴 트랜잭션 분리: 여러 메서드로 나눔

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

@Transactional
private void saveOrderWithTransaction(Order order) {
    updateInventory(order);
    saveOrder(order);
}
```

### 4.2 낙관적 락 (Optimistic Locking)

#### 원칙
- 동시 수정 가능성이 낮은 경우 사용
- **@Version** 어노테이션 활용
- 충돌 시 재시도 로직 구현

#### Entity 예제
```java
@Entity
public class Post extends BaseEntity {
    @Version
    @Column(name = "version")
    private Long version; // 낙관적 락 버전 관리
    
    // ...existing code...
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
```

#### Service 예제
```java
@Cacheable(value = "systemConfig", key = "#configKey")
public SystemConfig getConfig(String configKey) {
    return repository.findByConfigKey(configKey)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SYSTEM_CONFIG_NOT_FOUND));
}

@CacheEvict(value = "systemConfig", key = "#config.configKey")
public void updateConfig(SystemConfig config) {
    repository.save(config);
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

- [ ] **I/O 최소화**: try-with-resources 사용
- [ ] **페이징 처리**: Pageable 파라미터 추가
- [ ] **Custom Exception**: BusinessException 사용
- [ ] **트랜잭션 범위**: 최소화 (1~2초 이내)
- [ ] **N+1 문제**: Fetch Join 또는 EntityGraph
- [ ] **로그 기록**: 중요 동작은 INFO 이상
- [ ] **예외 처리**: try-catch + 의미 있는 에러 메시지
- [ ] **동시성 검증**: synchronized 또는 @Version 사용

---

**작성 완료일**: 2025-11-26  
**다음 검토 예정일**: 2025-12-03

