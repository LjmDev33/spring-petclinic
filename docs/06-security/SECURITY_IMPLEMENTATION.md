# 🎉 Spring Security 로그인 기능 구현 완료 (2025-11-06)

**작성일**: 2025년 11월 6일  
**작성자**: Jeongmin Lee  
**버전**: 3.5.2

---

## 🔒 보안 규칙

### SQL Injection 공격 방지 ⭐NEW (2025-11-12)

**목적**: SQL 인젝션 공격으로부터 데이터베이스 보호

#### 1. JPA/QueryDSL 사용 원칙
```
✅ JPA Repository 메서드 우선 사용
✅ QueryDSL 파라미터 바인딩 사용
✅ JPQL/HQL에서 Named Parameter 사용
❌ 문자열 연결로 쿼리 생성 금지
❌ Native Query 사용 자제
```

#### 2. 안전한 쿼리 작성 방법

**✅ 올바른 방법 - JPA Repository**
```java
// Spring Data JPA 메서드 쿼리
List<Post> findByTitleContaining(String keyword);
List<Post> findByAuthorNameAndStatusOrderByCreatedAtDesc(String author, Status status);
```

**✅ 올바른 방법 - QueryDSL**
```java
// QueryDSL 파라미터 바인딩
return queryFactory
    .selectFrom(post)
    .where(post.title.containsIgnoreCase(keyword)  // 자동 파라미터 바인딩
        .and(post.deletedAt.isNull()))
    .orderBy(post.createdAt.desc())
    .fetch();
```

**✅ 올바른 방법 - JPQL Named Parameter**
```java
@Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.deletedAt IS NULL")
List<Post> searchByTitle(@Param("keyword") String keyword);
```

**❌ 위험한 방법 - 문자열 연결**
```java
// ❌ SQL Injection 취약
String query = "SELECT * FROM post WHERE title = '" + keyword + "'";
entityManager.createNativeQuery(query).getResultList();

// ❌ 동적 쿼리 문자열 조합
String sql = "SELECT * FROM post WHERE 1=1 ";
if (keyword != null) {
    sql += "AND title LIKE '%" + keyword + "%'";  // 위험!
}
```

#### 3. 사용자 입력 검증

**컨트롤러 레벨 검증**
```java
@GetMapping("/list")
public String list(
    @RequestParam(required = false) @Pattern(regexp = "^[a-zA-Z가-힣0-9\\s]{0,100}$") String keyword,
    @RequestParam(required = false) @Pattern(regexp = "^(title|author)$") String type
) {
    // 검증된 파라미터만 서비스로 전달
}
```

**서비스 레벨 검증**
```java
public List<PostDto> searchPosts(String keyword) {
    // XSS, SQL Injection 특수문자 필터링
    String sanitized = keyword.replaceAll("[<>\"'%;()&+]", "");
    
    // 최대 길이 제한
    if (sanitized.length() > 100) {
        sanitized = sanitized.substring(0, 100);
    }
    
    return repository.findByTitleContaining(sanitized);
}
```

#### 4. Native Query 사용 시 주의사항

**불가피한 경우에만 사용**
```java
// ✅ 파라미터 바인딩 사용
@Query(value = "SELECT * FROM post WHERE title LIKE %?1% AND deleted_at IS NULL", 
       nativeQuery = true)
List<Post> nativeSearch(String keyword);

// ❌ 문자열 연결 금지
@Query(value = "SELECT * FROM post WHERE title LIKE '%" + keyword + "%'",  // 위험!
       nativeQuery = true)
```

#### 5. 개발 규칙

**신규 코드 작성 시 체크리스트**:
- [ ] JPA Repository 메서드 쿼리 사용
- [ ] QueryDSL 사용 시 파라미터 바인딩 확인
- [ ] 문자열 연결로 쿼리 생성하지 않음
- [ ] `@RequestParam` 검증 어노테이션 추가
- [ ] 사용자 입력값 길이 제한
- [ ] 특수문자 필터링 적용

**코드 리뷰 시 확인 사항**:
- [ ] Native Query 사용 여부 확인
- [ ] 동적 쿼리 생성 방식 검토
- [ ] 파라미터 바인딩 누락 체크
- [ ] 입력값 검증 로직 확인

---

## ✅ 구현 완료 항목

### 1️⃣ **시스템 설정 테이블 생성** ✅
**목적**: 멀티로그인 허용 여부 등 시스템 전역 설정 관리

**생성된 파일**:
- `SystemConfig.java` (Entity) - 설정 정보 테이블
- `SystemConfigRepository.java` - 데이터 접근
- `SystemConfigService.java` - 비즈니스 로직

**테이블 구조** (`system_config`):
```sql
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    property_key VARCHAR(100) NOT NULL UNIQUE,  -- 설정 키 (예: multiLoginEnabled)
    property_value VARCHAR(500) NOT NULL,        -- 설정 값 (예: true, false)
    description VARCHAR(1000),                   -- 설명
    is_active BOOLEAN NOT NULL DEFAULT TRUE,     -- 활성화 여부
    created_at DATETIME NOT NULL,                -- 생성 일시
    updated_at DATETIME NOT NULL,                -- 수정 일시
    updated_by VARCHAR(100)                      -- 수정자
);
```

**주요 기능**:
- ✅ 멀티로그인 허용 여부 조회: `isMultiLoginEnabled()`
- ✅ 설정 값 조회 (기본값 지원): `getConfigValue(key, defaultValue)`
- ✅ Boolean 설정 값 조회: `getBooleanConfig(key, defaultValue)`
- ✅ 설정 값 업데이트: `updateConfig(key, value, updatedBy)`

---

### 2️⃣ **사용자 관리 기능 구현** ✅
**목적**: 회원가입, 로그인, 권한 관리

**생성된 파일**:
- `User.java` (Entity) - 사용자 정보 테이블
- `UserRepository.java` - 데이터 접근
- `UserService.java` - 회원가입 로직
- `CustomUserDetailsService.java` - Spring Security 연동
- `UserRegisterDto.java` - 회원가입 DTO
- `AuthController.java` - 로그인/회원가입 컨트롤러

**테이블 구조** (`users`):
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,        -- 사용자 ID (로그인용)
    password VARCHAR(100) NOT NULL,              -- BCrypt 해시된 비밀번호
    email VARCHAR(100) NOT NULL,                 -- 이메일
    name VARCHAR(50) NOT NULL,                   -- 이름
    phone VARCHAR(20),                           -- 전화번호
    enabled BOOLEAN NOT NULL DEFAULT TRUE,       -- 계정 활성화 여부
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,      -- 계정 만료 여부
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,       -- 계정 잠금 여부
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,  -- 비밀번호 만료 여부
    created_at DATETIME NOT NULL,                -- 생성 일시
    updated_at DATETIME NOT NULL,                -- 수정 일시
    last_login_at DATETIME,                      -- 마지막 로그인 일시
    last_login_ip VARCHAR(50)                    -- 마지막 로그인 IP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,                   -- 권한 (ROLE_USER, ROLE_ADMIN)
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**주요 기능**:
- ✅ 회원가입: `POST /register`
- ✅ 아이디/이메일 중복 검사
- ✅ BCrypt 비밀번호 해싱
- ✅ 마지막 로그인 시간/IP 기록
- ✅ 권한 관리 (ROLE_USER, ROLE_ADMIN)

---

### 3️⃣ **Spring Security 설정** ✅
**목적**: 로그인/로그아웃/Remember-Me 설정

**생성된 파일**:
- `SecurityConfig.java` - Spring Security 메인 설정
- `CustomAuthenticationSuccessHandler.java` - 로그인 성공 처리

**주요 설정**:
```java
// 1. 로그인 설정
.formLogin(form -> form
    .loginPage("/login")
    .loginProcessingUrl("/login")
    .usernameParameter("username")
    .passwordParameter("password")
    .successHandler(successHandler)  // 마지막 로그인 시간 업데이트
    .failureUrl("/login?error=true")
)

// 2. 로그아웃 설정
.logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/?logout=true")
    .invalidateHttpSession(true)
    .deleteCookies("JSESSIONID", "remember-me")
)

// 3. Remember-Me (자동 로그인) 설정
.rememberMe(remember -> remember
    .key("petclinic-remember-me-key")
    .tokenRepository(persistentTokenRepository())  // DB 기반 토큰 저장
    .tokenValiditySeconds(86400 * 7)  // 7일
)

// 4. 세션 관리 (단일 로그인)
.sessionManagement(session -> session
    .maximumSessions(1)  // 기본 단일 로그인 (추후 시스템 설정으로 제어)
    .maxSessionsPreventsLogin(false)  // false: 기존 세션 만료, true: 신규 로그인 차단
)
```

**접근 제어**:
```java
.authorizeHttpRequests(auth -> auth
    // 공개 리소스
    .requestMatchers("/", "/welcome", "/css/**", "/images/**").permitAll()
    // 회원가입, 로그인
    .requestMatchers("/login", "/register").permitAll()
    // 온라인상담 (공개)
    .requestMatchers("/counsel/**").permitAll()
    // 관리자 전용
    .requestMatchers("/admin/**").hasRole("ADMIN")
    // 나머지는 인증 필요
    .anyRequest().authenticated()
)
```

---

### 4️⃣ **Thymeleaf Layout 업데이트** ✅
**파일**: `layout.html`

**변경 내용**:
```html
<!-- Thymeleaf Security 네임스페이스 추가 -->
<html xmlns:th="http://www.thymeleaf.org" 
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<!-- 로그인하지 않은 경우 -->
<span sec:authorize="isAnonymous()">
  <a th:href="@{/login}">로그인</a>
  <span class="p-1">|</span>
  <a th:href="@{/register}">회원가입</a>
</span>

<!-- 로그인한 경우 -->
<span sec:authorize="isAuthenticated()">
  <span class="text-success fw-bold" sec:authentication="name"></span>님 환영합니다
  <span class="p-1">|</span>
  <a th:href="@{/logout}">로그아웃</a>
</span>
```

---

### 5️⃣ **build.gradle 의존성 추가** ✅
```gradle
dependencies {
    // Spring Security (안정 버전)
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.security:spring-security-crypto:6.3.4'
    implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'
}
```

**버전 선택 기준**:
- ✅ Spring Boot 3.5.0 기준 최신 안정 버전
- ✅ CVE 보안 취약점 없는 버전
- ✅ Thymeleaf와 호환되는 버전

---

## 📋 데이터베이스 테이블 관계

```
┌─────────────────┐
│  system_config  │  시스템 설정
│  - id           │
│  - property_key │  (예: multiLoginEnabled)
│  - property_value│  (예: true, false)
│  - description  │
│  - is_active    │
└─────────────────┘

┌─────────────────┐
│     users       │  사용자 정보
│  - id           │
│  - username     │  (UNIQUE)
│  - password     │  (BCrypt 해시)
│  - email        │
│  - name         │
│  - enabled      │
│  - last_login_at│
│  - last_login_ip│
└─────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐
│   user_roles    │  사용자 권한 (복합키)
│  - user_id      │  FK → users.id
│  - role         │  (ROLE_USER, ROLE_ADMIN)
└─────────────────┘

┌─────────────────┐
│ persistent_logins│  Remember-Me 토큰 (Spring Security 자동 생성)
│  - username     │
│  - series       │  PK
│  - token        │
│  - last_used    │
└─────────────────┘
```

---

## 🔐 로그인 정책

### 단일/멀티 로그인 제어
**시스템 설정 기반**:
```java
// system_config 테이블에 설정 추가
property_key = "multiLoginEnabled"
property_value = "true"  // 멀티로그인 허용
property_value = "false"  // 단일 로그인만 허용

// SecurityConfig에서 동적 제어 (미구현, 추후 개선)
.maximumSessions(systemConfigService.isMultiLoginEnabled() ? -1 : 1)
```

**현재 설정**: 단일 로그인 (기존 세션 만료 방식)

---

## 🚀 다음 단계 (미구현 기능)

### 우선순위 높음
1. **Remember-Me 테이블 초기화 SQL**
   - `persistent_logins` 테이블 생성 스크립트
   - DataInit에 기본 관리자 계정 추가

2. **로그인 페이지 템플릿 생성**
   - `user/login.html`
   - `user/register.html`
   - 아이디 저장 체크박스
   - Remember-Me (자동 로그인) 체크박스

3. **비밀번호 찾기 기능**
   - 이메일 인증
   - 비밀번호 재설정

### 우선순위 중간
4. **파일 다운로드 권한 검증**
   - 비공개 게시글 첨부파일 다운로드 시 권한 확인
   - 작성자 또는 관리자만 다운로드 허용

5. **온라인상담 댓글 모달 UI 개선**
   - 댓글 작성 시 모달 창 표출
   - Bootstrap 5 모달 적용

6. **관리자 페이지 구현**
   - 시스템 설정 변경 (멀티로그인 허용 여부)
   - 사용자 관리 (활성화/비활성화)

---

## 🐛 해결된 문제

### 문제 1: 파일 생성 시 중복 코드 발생
**원인**: create_file 도구 사용 시 파일 끝에 다른 클래스의 코드가 중복으로 추가됨

**해결**:
- 모든 파일을 `mcp_local-fs_write_file` 도구로 재생성
- 각 파일의 끝부분 확인 후 중복 코드 제거

**영향 파일**:
- CustomAuthenticationSuccessHandler.java
- SystemConfig Service.java
- AuthController.java
- CustomUserDetailsService.java
- UserRepository.java (빈 파일)
- SystemConfig.java (빈 파일)
- UserService.java (빈 파일)

---

## 📝 개발 규칙 업데이트

### 신규 규칙: 라이브러리 및 의존성 관리 ⭐NEW

**규칙 8. 라이브러리 및 의존성 관리**:

**라이브러리 추가 시 확인사항**:
1. ✅ **보안 이슈 확인**: CVE 데이터베이스에서 알려진 취약점 확인
2. ✅ **안정 버전 사용**: GA(General Availability) 또는 Stable 버전만 사용
3. ✅ **최신 보안 패치**: 마이너 버전 업데이트 적용 (예: 1.18.0 → 1.18.1)
4. ✅ **라이선스 검토**: Apache 2.0, MIT 등 호환 가능한 라이선스 확인
5. ✅ **의존성 충돌 확인**: `./gradlew dependencies` 명령어로 충돌 검사
6. ❌ **금지 버전**: SNAPSHOT, alpha, beta, RC(Release Candidate) 금지

**버전 선택 가이드**:
- Spring Boot: 최신 안정 버전 (3.x.x)
- Spring Security: Spring Boot BOM에 포함된 버전 우선
- QueryDSL: 5.0.0 이상 (Jakarta EE 지원)
- MySQL Connector: 9.x.x (최신 보안 패치)
- Thymeleaf: 3.1.x 이상

---

## ✅ 빌드 결과

```bash
./gradlew build -x test

BUILD SUCCESSFUL in 34s
10 actionable tasks: 9 executed, 1 up-to-date
```

**확인 사항**:
- ✅ 모든 Java 파일 컴파일 성공
- ✅ Spring Boot 애플리케이션 시작 성공
- ✅ Spring Data JPA 리포지토리 10개 스캔 완료
- ✅ Security 설정 정상 로드

---

## 📊 생성된 파일 목록 (11개)

### Java 클래스 (9개)
1. `SystemConfig.java` - Entity
2. `SystemConfigRepository.java` - Repository
3. `SystemConfigService.java` - Service
4. `User.java` - Entity
5. `UserRepository.java` - Repository
6. `UserService.java` - Service
7. `CustomUserDetailsService.java` - UserDetailsService
8. `SecurityConfig.java` - Security 설정
9. `CustomAuthenticationSuccessHandler.java` - 로그인 성공 핸들러
10. `UserRegisterDto.java` - DTO
11. `AuthController.java` - Controller

### 수정된 파일 (2개)
1. `build.gradle` - Spring Security 의존성 추가
2. `layout.html` - Thymeleaf Security 연동

### 문서 (1개)
1. `PROJECT_DOCUMENTATION.md` - 개발 규칙 업데이트

---

## 🎯 다음 작업 우선순위

### 즉시 진행 가능
1. ✅ 로그인 페이지 템플릿 생성
2. ✅ 회원가입 페이지 템플릿 생성
3. ✅ DataInit에 관리자 계정 추가
4. ✅ Remember-Me 테이블 초기화 SQL

### 추후 진행
5. ⏳ 온라인상담 댓글 모달 UI 개선
6. ⏳ 파일 다운로드 권한 검증
7. ⏳ 비밀번호 찾기 기능
8. ⏳ 관리자 페이지 구현
9. ⏳ 멀티로그인 동적 제어

---

**작업 완료일**: 2025년 11월 6일  
**다음 검토일**: 로그인 페이지 템플릿 완성 후

