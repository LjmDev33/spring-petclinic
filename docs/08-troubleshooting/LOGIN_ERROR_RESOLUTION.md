# Spring Security 로그인 에러 해결 가이드

**작성일**: 2025-11-11  
**작성자**: Jeongmin Lee  
**문서 버전**: 1.0

---

## 📋 목차
1. [문제 상황](#문제-상황)
2. [에러 분석](#에러-분석)
3. [원인](#원인)
4. [해결 방법](#해결-방법)
5. [재발 방지 가이드](#재발-방지-가이드)

---

## 문제 상황

### 에러 스택 트레이스
```
2025-11-11T14:12:37.914+09:00  INFO 8200 --- [petclinic] [nio-8080-exec-6] 
p.s.h.CustomAuthenticationSuccessHandler : Login success: pet01 from IP: 0:0:0:0:0:0:0:1 

2025-11-11T14:12:37.933+09:00 ERROR 8200 --- [petclinic] [nio-8080-exec-7] 
org.thymeleaf.TemplateEngine : [THYMELEAF][http-nio-8080-exec-7] 
Exception processing template "welcome": An error happened during template parsing 
(template: "class path resource [templates/welcome.html]")  

org.thymeleaf.exceptions.TemplateInputException: An error happened during template parsing 
(template: "class path resource [templates/welcome.html]")
```

### 증상
- ✅ 로그인 자체는 성공 (CustomAuthenticationSuccessHandler 로그 확인)
- ❌ welcome.html 템플릿 파싱 중 오류 발생
- ❌ 사용자는 로그인 후 홈 화면 접근 불가

---

## 에러 분석

### 1단계: 로그 분석
```
INFO: Login success: pet01 from IP: 0:0:0:0:0:0:0:1
```
→ Spring Security 인증은 정상 완료

```
ERROR: Exception processing template "welcome": 
An error happened during template parsing
```
→ Thymeleaf 템플릿 파싱 중 오류

### 2단계: 문제 지점 파악
**파일**: `fragments/layout.html`  
**라인**: 66

```html
<span sec:authentication="principal.nickname"></span>
```

### 3단계: 원인 특정
Spring Security의 기본 `UserDetails` 구현체는 `nickname` 필드를 가지고 있지 않음.

```java
// Spring Security 기본 UserDetails
public class User implements UserDetails {
    private String username;
    private String password;
    private Collection<GrantedAuthority> authorities;
    // nickname 필드 없음! ❌
}
```

Thymeleaf에서 `sec:authentication="principal.nickname"` 접근 시 `NoSuchMethodException` 발생.

---

## 원인

### 근본 원인
**Thymeleaf에서 존재하지 않는 필드에 접근**

```html
<!-- layout.html -->
<span sec:authentication="principal.nickname"></span>
```

Spring Security의 `Authentication.getPrincipal()`은 `UserDetails` 객체를 반환하는데,  
기본 `UserDetails`에는 `nickname` 필드가 없음.

### 왜 이전에는 작동했는가?
- 이전에는 `sec:authentication="name"` (username) 사용
- `name`은 `UserDetails`의 기본 메서드 (`getUsername()`)
- 최근 UI 개선 작업 중 `principal.nickname`으로 변경

### 트리거 이벤트
```
2025-11-11 작업: 홈페이지 상단 닉네임 표시
- sec:authentication="name" → sec:authentication="principal.nickname" 변경
- CustomUserDetails 구현 누락
```

---

## 해결 방법

### Solution: CustomUserDetails 구현

#### 1단계: CustomUserDetails 클래스 생성
**파일**: `user/security/CustomUserDetails.java`

```java
public class CustomUserDetails implements UserDetails {
    private final User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    // UserDetails 필수 메서드 구현
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
    
    // ... 기타 메서드
    
    // ✅ 커스텀 필드 추가
    public String getNickname() {
        return user.getNickname();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    public String getName() {
        return user.getName();
    }
}
```

#### 2단계: CustomUserDetailsService 수정
**파일**: `user/service/CustomUserDetailsService.java`

```java
@Override
public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // ✅ CustomUserDetails 반환
    return new CustomUserDetails(user);
}
```

#### 3단계: Thymeleaf에서 접근
```html
<!-- layout.html -->
<span sec:authentication="principal.nickname"></span>  ✅ 정상 작동
<span sec:authentication="principal.email"></span>     ✅ 정상 작동
<span sec:authentication="principal.name"></span>      ✅ 정상 작동
```

---

## 재발 방지 가이드

### 규칙 1: UserDetails 커스터마이징 시 항상 CustomUserDetails 사용

#### ❌ 잘못된 방법
```java
// Spring Security 기본 UserDetails 사용
return new org.springframework.security.core.userdetails.User(
    user.getUsername(),
    user.getPassword(),
    authorities
);
```

**문제점**:
- 커스텀 필드 (nickname, email 등) 접근 불가
- Thymeleaf에서 `principal.nickname` 사용 시 오류

#### ✅ 올바른 방법
```java
// CustomUserDetails 사용
return new CustomUserDetails(user);
```

**장점**:
- Entity의 모든 필드 접근 가능
- Thymeleaf에서 자유롭게 사용
- 타입 안전성 확보

---

### 규칙 2: Thymeleaf에서 principal 필드 접근 시 체크리스트

#### 접근 전 확인사항
1. ✅ `CustomUserDetails`에 getter 메서드 존재 확인
2. ✅ `CustomUserDetailsService`가 `CustomUserDetails` 반환 확인
3. ✅ 로컬 테스트 (로그인 후 페이지 정상 렌더링 확인)

#### 안전한 접근 패턴
```html
<!-- 1. 기본 필드 (항상 사용 가능) -->
<span sec:authentication="name"></span>           <!-- username -->
<span sec:authentication="authorities"></span>     <!-- 권한 목록 -->

<!-- 2. 커스텀 필드 (CustomUserDetails 필요) -->
<span sec:authentication="principal.nickname"></span>
<span sec:authentication="principal.email"></span>

<!-- 3. 조건부 렌더링 (안전) -->
<span th:if="${#authentication.principal.nickname != null}" 
      th:text="${#authentication.principal.nickname}"></span>
```

---

### 규칙 3: 로그인 후 페이지 접근 테스트 필수

#### 테스트 시나리오
```
1. 로그인 수행
   - admin / admin1234
   - user / user1234

2. 홈 화면 접근 확인
   - http://localhost:8080/

3. Thymeleaf 템플릿 오류 확인
   - 브라우저 콘솔 체크
   - 서버 로그 체크

4. 모든 인증 필요 페이지 접근
   - /mypage
   - /counsel/list
   - /admin/settings (관리자만)
```

#### 자동화 테스트 (향후 추가 권장)
```java
@SpringBootTest
@AutoConfigureMockMvc
class LoginIntegrationTest {
    
    @Test
    void loginAndAccessHomePage() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "user")
                .param("password", "user1234"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
        
        // 홈 페이지 접근 확인
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("테스트유저"))); // 닉네임 확인
    }
}
```

---

### 규칙 4: Entity 필드 추가 시 CustomUserDetails도 함께 업데이트

#### 시나리오: User 엔티티에 새 필드 추가
```java
@Entity
public class User {
    // ...existing fields...
    
    @Column(name = "phone_verified")
    private boolean phoneVerified;  // ✅ 새 필드 추가
}
```

#### 체크리스트
1. ✅ `CustomUserDetails`에 getter 추가
```java
public class CustomUserDetails implements UserDetails {
    // ...existing code...
    
    public boolean isPhoneVerified() {
        return user.isPhoneVerified();
    }
}
```

2. ✅ Thymeleaf에서 접근 테스트
```html
<span th:if="${#authentication.principal.phoneVerified}">
    <i class="bi bi-check-circle"></i> 전화번호 인증 완료
</span>
```

3. ✅ 컴파일 및 통합 테스트

---

### 규칙 5: 개발 환경에서 Thymeleaf 캐시 비활성화

#### application-dev.yml
```yaml
spring:
  thymeleaf:
    cache: false  # ✅ 개발 중 템플릿 변경 즉시 반영
    
logging:
  level:
    org.thymeleaf: DEBUG  # ✅ Thymeleaf 오류 상세 로그
    org.springframework.security: DEBUG  # ✅ Security 로그
```

**장점**:
- 템플릿 오류 즉시 확인
- 서버 재시작 불필요
- 디버깅 용이

---

## 오류 패턴 및 해결 방법

### 패턴 1: `NoSuchMethodException`
```
java.lang.NoSuchMethodException: 
org.springframework.security.core.userdetails.User.getNickname()
```

**원인**: 기본 `UserDetails` 사용  
**해결**: `CustomUserDetails` 구현 및 사용

---

### 패턴 2: `PropertyNotFoundException`
```
org.springframework.expression.spel.SpelEvaluationException: 
EL1008E: Property or field 'nickname' cannot be found
```

**원인**: `CustomUserDetails`에 getter 메서드 없음  
**해결**: `getNickname()` 메서드 추가

---

### 패턴 3: `NullPointerException`
```
java.lang.NullPointerException: 
Cannot invoke "String.toString()" because the return value of 
"CustomUserDetails.getNickname()" is null
```

**원인**: `User.nickname` 필드가 null  
**해결**: 
1. DB 데이터 확인 (nickname 컬럼 NOT NULL 제약)
2. DataInit에서 초기 데이터 설정
3. Thymeleaf에서 null 체크
```html
<span th:text="${#authentication.principal.nickname ?: '닉네임 없음'}"></span>
```

---

## 체크리스트

### 로그인 기능 추가/수정 시
- [ ] `CustomUserDetails` 구현 완료
- [ ] `CustomUserDetailsService`가 `CustomUserDetails` 반환
- [ ] Thymeleaf에서 접근할 필드 모두 getter 추가
- [ ] 로그인 후 홈 화면 접근 테스트
- [ ] 모든 인증 필요 페이지 접근 테스트
- [ ] 서버 로그에서 Thymeleaf 오류 확인

### Entity 필드 추가 시
- [ ] `CustomUserDetails`에 getter 추가
- [ ] Thymeleaf 템플릿에서 접근 테스트
- [ ] null 체크 로직 추가 (필요 시)
- [ ] 기존 사용자 데이터 마이그레이션 (필요 시)

### 배포 전
- [ ] 모든 사용자 권한으로 로그인 테스트 (USER, ADMIN)
- [ ] 브라우저 콘솔 오류 확인
- [ ] 서버 로그 ERROR 레벨 확인
- [ ] 성능 테스트 (로그인 → 페이지 이동)

---

## 참고 자료

### Spring Security 공식 문서
- [UserDetails Interface](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html)
- [UserDetailsService](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetailsService.html)

### Thymeleaf 공식 문서
- [Spring Security Integration](https://www.thymeleaf.org/doc/articles/springsecurity.html)
- [Expression Objects](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#expression-utility-objects)

---

## 변경 이력

### [1.0] - 2025-11-11
- 최초 문서 작성
- 로그인 에러 원인 분석 및 해결 방법 정리
- 재발 방지 가이드 작성

---

**문서 버전**: 1.0  
**최종 검토**: 2025-11-11  
**담당자**: Jeongmin Lee  
**관련 이슈**: Spring Security principal.nickname 접근 오류

