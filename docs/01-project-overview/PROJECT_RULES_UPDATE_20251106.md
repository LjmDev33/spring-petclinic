# ✅ 프로젝트 규칙 추가 완료 (2025-11-26 업데이트)

## 최근 추가된 규칙 (2025-11-27)

### 📋 규칙 14: ACID 트랜잭션 속성 보장 규칙 ⭐NEW

#### 핵심 내용
**목적**: 모든 비즈니스 로직에서 데이터 일관성과 무결성을 보장하기 위해 ACID 속성을 철저히 준수

**ACID 속성**:
1. **Atomicity (원자성)**
   - 트랜잭션의 모든 작업이 전부 성공하거나 전부 실패해야 함
   - 일부만 성공하는 상황 방지 (All or Nothing)

2. **Consistency (일관성)**
   - 트랜잭션 전후로 데이터베이스가 일관된 상태를 유지
   - 무결성 제약조건이 항상 만족되어야 함

3. **Isolation (격리성)**
   - 동시에 실행되는 트랜잭션들이 서로 간섭하지 않음
   - 격리 수준에 따라 데드락, 더티리드 등을 방지

4. **Durability (지속성)**
   - 커밋된 트랜잭션은 시스템 장애가 발생해도 영구적으로 보존

**필수 적용 사항**:

```java
// 1. Service 계층에 @Transactional 적용
@Service
@Transactional  // 클래스 레벨에 적용 (모든 public 메서드가 트랜잭션 내에서 실행)
public class CounselService {
    
    // 2. 읽기 전용 메서드는 readOnly = true 설정 (성능 최적화)
    @Transactional(readOnly = true)
    public CounselPostDto getPost(Long id) {
        // 조회 로직
    }
    
    // 3. 여러 DB 작업이 하나의 트랜잭션으로 묶여야 함
    @Transactional
    public void createPostWithComments(CounselPostDto postDto, List<CommentDto> comments) {
        // 게시글 저장
        CounselPost post = savePost(postDto);
        
        // 댓글들 저장 (하나의 트랜잭션)
        for (CommentDto comment : comments) {
            saveComment(post.getId(), comment);
        }
        
        // 둘 중 하나라도 실패하면 전체 롤백
    }
    
    // 4. 예외 발생 시 자동 롤백 (RuntimeException)
    @Transactional
    public void updatePost(Long id, CounselPostDto dto) {
        CounselPost post = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다")); // 롤백 발생
        
        post.setTitle(dto.getTitle());
        repository.save(post);
    }
    
    // 5. 체크 예외는 rollbackFor 명시 필요
    @Transactional(rollbackFor = Exception.class)
    public void processPayment() throws PaymentException {
        // 체크 예외도 롤백되도록 설정
    }
}
```

**격리 수준 설정**:
```java
// 격리 수준이 필요한 경우 명시
@Transactional(isolation = Isolation.READ_COMMITTED)
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    // 동시성 제어가 필요한 로직
}
```

**전파 속성 설정**:
```java
// 기존 트랜잭션이 있으면 참여, 없으면 새로 생성 (기본값)
@Transactional(propagation = Propagation.REQUIRED)
public void methodA() { }

// 항상 새로운 트랜잭션 생성
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void independentOperation() { }

// 트랜잭션 없이 실행 (필요한 경우에만)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void readOnlyNoTransaction() { }
```

**주의사항**:
1. ❌ **Controller에는 @Transactional 사용 금지**
   - 트랜잭션은 Service 계층에서만 관리
   - Controller는 요청/응답 처리만 담당

2. ❌ **private 메서드에 @Transactional 사용 불가**
   - Spring AOP는 public 메서드만 프록시 생성
   - private 메서드는 트랜잭션이 적용되지 않음

3. ✅ **try-catch로 예외를 잡으면 롤백이 안 될 수 있음**
   - 예외를 잡고 처리한 후 다시 던져야 롤백 발생
   ```java
   @Transactional
   public void processData() {
       try {
           // 작업 수행
       } catch (Exception e) {
           log.error("Error: {}", e.getMessage());
           throw e; // 다시 던져야 롤백 발생!
       }
   }
   ```

4. ✅ **긴 트랜잭션 방지**
   - 트랜잭션 내에서 외부 API 호출, 파일 I/O 등 시간이 오래 걸리는 작업 지양
   - 필요하다면 트랜잭션을 분리하거나 @Async 사용

**적용 대상**:
- ✅ 모든 Service 클래스
- ✅ 데이터 생성/수정/삭제 로직
- ✅ 여러 테이블을 동시에 조작하는 로직
- ✅ 좋아요, 조회수 증가 등 동시성 이슈가 있는 로직

**테스트 검증**:
```java
@Test
public void testTransactionRollback() {
    // given
    CounselPostDto dto = new CounselPostDto();
    
    // when
    assertThrows(RuntimeException.class, () -> {
        service.createPost(dto); // 예외 발생
    });
    
    // then
    assertEquals(0, repository.count()); // 롤백으로 데이터 저장 안됨
}
```

**적용 위치**: 모든 Service 클래스의 비즈니스 로직 메서드

---

## 최근 추가된 규칙 (2025-11-26)

### 📋 규칙 13: 클래스/인터페이스/Enum 주석 작성 규칙 ⭐NEW

#### 핵심 내용
**목적**: 모든 클래스에 상세한 JavaDoc 주석을 추가하여 협업 및 유지보수 편의성 향상

**필수 작성 항목**:
```java
/**
 * Project : spring-petclinic
 * File    : ClassName.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   클래스에 대한 한 줄 설명
 *
 * Purpose (만든 이유):
 *   1. 왜 이 클래스가 필요한지
 *   2. 어떤 문제를 해결하는지
 *   3. 다른 방식 대신 이 방식을 선택한 이유
 *
 * Key Features (주요 기능):
 *   - 핵심 기능 1
 *   - 핵심 기능 2
 *   - 핵심 기능 3
 *
 * When to Use (사용 시점):
 *   - 언제 사용해야 하는지
 *   - 어떤 상황에서 필요한지
 *
 * Usage Examples (사용 예시):
 *   // 실제 코드 예시
 *   SomeClass obj = new SomeClass();
 *   obj.doSomething();
 *
 * How It Works (작동 방식):
 *   1. 단계별 동작 설명
 *   2. 데이터 흐름 설명
 *
 * Performance/Security (성능/보안):
 *   - 성능 최적화 내용
 *   - 보안 고려사항
 *
 * vs 비교 (다른 방식과 비교):
 *   - A방식 vs B방식 차이점
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
```

**작성 원칙**:
1. ✅ **Purpose는 필수**: 왜 만들었는지 반드시 명시
2. ✅ **실제 코드 예시 포함**: Usage Examples에 동작하는 코드
3. ✅ **Key Features는 구체적으로**: "기능 제공" 같은 모호한 표현 금지
4. ✅ **How It Works는 복잡한 클래스에만**: 간단한 클래스는 생략 가능
5. ✅ **vs 비교는 선택적**: 다른 방식과 비교가 필요할 때만 작성

**적용 대상**:
- ✅ Config 클래스 (WebConfig, QuerydslConfig 등)
- ✅ Service 클래스 (모든 비즈니스 로직)
- ✅ Repository Custom 구현체
- ✅ Exception 클래스 (BaseException, BusinessException 등)
- ✅ DTO 클래스 (PageResponse, ErrorResponse 등)
- ✅ Entity 클래스
- ✅ Controller 클래스
- ✅ Mapper 클래스
- ✅ Enum 클래스

**예시 (GlobalExceptionHandler)**:
```java
/**
 * Purpose (만든 이유):
 *   1. 모든 Controller의 예외를 중앙에서 통합 처리
 *   2. 중복 코드 제거 (각 Controller마다 try-catch 불필요)
 *
 * How It Works (작동 방식):
 *   1. Controller에서 예외 발생
 *   2. @RestControllerAdvice가 예외를 자동 감지
 *   3. 예외 타입에 맞는 @ExceptionHandler 메서드 실행
 */
```

**적용 위치**: 모든 .java 파일의 클래스 선언 직전

---

### 📋 규칙 12: ErrorCode 작성 규칙 (사용자 친화적 메시지) ⭐NEW

#### 핵심 내용
**목적**: 사용자가 오류 발생 시 전산팀에 명확하게 보고할 수 있도록 일목요연한 메시지 제공

**필수 형식**:
```java
ERROR_CODE(HTTP_STATUS, "CODE", "[카테고리] 상세 설명 (에러코드: CODE)")
```

**작성 원칙**:
1. ✅ **카테고리 명시**: `[파일 업로드 실패]`, `[비밀번호 불일치]` 등 대괄호로 분류
2. ✅ **상세 설명**: 무엇이 잘못되었는지 명확히 설명
3. ✅ **해결 방법 제시**: 사용자가 할 수 있는 조치 또는 전산팀 문의 안내
4. ✅ **에러코드 표기**: `(에러코드: A002)` 형식으로 코드 명시
5. ✅ **존댓말 사용**: "~해주세요" 형식으로 친절한 안내

**예시**:
```java
// ❌ 나쁜 예
ATTACHMENT_UPLOAD_FAILED(500, "A002", "첨부파일 업로드에 실패했습니다.")

// ✅ 좋은 예
ATTACHMENT_UPLOAD_FAILED(500, "A002", 
    "[파일 업로드 실패] 파일 업로드 중 오류가 발생했습니다. " +
    "파일 크기와 형식을 확인하거나 전산팀에 문의해주세요. (에러코드: A002)")
```

**카테고리 분류**:
- Common (1000~1999): `[입력 오류]`, `[타입 오류]`, `[조회 실패]`, `[서버 오류]`
- User (2000~2999): `[사용자 조회 실패]`, `[이메일 중복]`, `[비밀번호 오류]`
- Post (3000~3999): `[게시글 조회 실패]`, `[게시글 삭제 불가]`, `[비밀번호 불일치]`
- Comment (4000~4999): `[댓글 조회 실패]`, `[댓글 삭제 불가]`
- Attachment (5000~5999): `[파일 업로드 실패]`, `[파일 다운로드 실패]`
- System (6000~6999): `[시스템 설정 조회 실패]`
- I/O (7000~7999): `[파일 읽기 오류]`, `[파일 쓰기 오류]`

**적용 위치**: `common/exception/ErrorCode.java`

---

## 최근 추가된 규칙 (2025-11-14)

### 📋 규칙 11: 요구사항 구현 후 검증 프로세스 ⭐NEW

#### 핵심 내용
**목적**: 모든 요구사항 구현 후 자체 검증을 통해 품질 보증

**필수 검증 항목**:
1. ✅ **컴파일 검증**: `gradlew compileJava` 또는 `build -x test` 실행
2. ✅ **에러 체크**: 모든 수정 파일에 대해 컴파일 에러 확인
3. ✅ **요구사항 재확인**: 사용자가 요청한 모든 항목 구현 확인
4. ✅ **파일 상태 검증**: 생성/수정/삭제된 파일의 존재 및 크기 확인
5. ✅ **문서 업데이트**: 변경사항을 관련 문서에 반영

#### 검증 프로세스
```
1. 요구사항 구현 완료
   ↓
2. 컴파일 검증 (gradlew compileJava)
   ↓
3. 에러 체크 (get_errors 도구 사용)
   ↓
4. 요구사항 대조 (모든 항목 체크리스트)
   ↓
5. 파일 상태 확인 (생성/수정된 파일 검증)
   ↓
6. 문서 업데이트 (CHANGELOG, 관련 문서)
   ↓
7. 최종 보고 (완료된 작업 요약)
```

#### 검증 완료 기준
- ✅ 컴파일 에러 0개
- ✅ 요구사항 100% 구현
- ⚠️ 경고는 허용 (기능에 영향 없는 경우)
- ✅ 문서 업데이트 완료

---

## 기존 프로젝트 규칙 (2025-11-06)

### 📜 규칙 9: Hibernate DDL 및 스키마 관리 ⭐NEW

#### 핵심 내용
**목적**: 모든 테이블(기존 + 신규)에서 DROP 오류 발생 방지

**필수 규칙**:
1. ✅ **개발 환경 DDL**: 항상 `ddl-auto: update` 사용
2. ❌ **create-drop 절대 금지**: 테이블 DROP 오류 및 데이터 손실
3. ✅ **Entity 작성 시**: `@Table(name = "테이블명")` 명시
4. ✅ **Soft Delete 필수**: `del_flag`, `deleted_at` 필드 포함
5. ✅ **초기화 필요 시**: `drop-all-tables.sql` 수동 실행

#### 설정 (application-dev.yml)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ✅ 권장
      # ddl-auto: create-drop  # ❌ 절대 금지
```

#### 적용 범위
- ✅ **기존 테이블**: 모든 테이블 적용 완료
- ✅ **신규 테이블**: 규칙 준수 시 자동 적용
- ✅ **외래키 제약조건**: 개발 환경에서는 생성 안 함

#### ddl-auto 옵션 비교

| 옵션 | DROP | CREATE | ALTER | 데이터 유지 | 권장 |
|------|------|--------|-------|------------|------|
| **update** | ❌ | ✅ | ✅ | ✅ | ✅ 개발 환경 권장 |
| create | ❌ | ✅ | ❌ | ❌ | ⚠️ 초기 개발만 |
| create-drop | ✅ | ✅ | ❌ | ❌ | ❌ 절대 금지 |
| validate | ❌ | ❌ | ❌ | ✅ | ✅ 운영 환경 |
| none | ❌ | ❌ | ❌ | ✅ | ✅ 운영 환경 |

---

### 🚀 규칙 10: 테스트 및 서버 실행 ⭐NEW

#### 핵심 내용
**목적**: 포트 충돌 방지 및 안정적인 서버 관리

**필수 규칙**:
1. ✅ **서버 실행**: 항상 IDE(IntelliJ IDEA)에서 실행
2. ❌ **터미널 bootRun 금지**: 포트 점유 문제 발생
3. ✅ **컴파일/빌드**: Gradle 명령어 사용 가능
4. ❌ **테스트 코드**: 별도 요청 없으면 작성하지 않음

#### 허용/금지 명령어

| 명령어 | 용도 | 허용 여부 |
|--------|------|----------|
| `./gradlew compileJava` | 컴파일 확인 | ✅ 허용 |
| `./gradlew build -x test` | 빌드 확인 | ✅ 허용 |
| `./gradlew dependencies` | 의존성 확인 | ✅ 허용 |
| `./gradlew --stop` | Daemon 종료 | ✅ 허용 |
| `./gradlew bootRun` | 서버 실행 | ❌ **금지** |

#### 서버 실행 방법 (IDE)
```
1. IntelliJ IDEA에서 PetClinicApplication.java 열기
2. main 메서드 옆 실행 버튼 클릭
3. Active profiles: dev 확인
4. Run 또는 Debug 실행
5. http://localhost:8080 접속
```

#### bootRun 금지 이유
- 백그라운드 실행 시 포트가 살아있어 수동 종료 필요
- `taskkill` 명령어로 강제 종료해야 함
- IDE Stop 버튼이 훨씬 간편하고 안전

---

## 📝 업데이트된 문서 (5개)

### 1. PROJECT_DOCUMENTATION.md
**섹션 6. 개발 규칙**:
- ✅ 규칙 9: Hibernate DDL 및 스키마 관리
  - ddl-auto 옵션 상세 설명
  - Entity 작성 가이드
  - DROP 오류 대응 방법
  
- ✅ 규칙 10: 테스트 및 서버 실행
  - IDE 실행 방법
  - 허용/금지 명령어
  - 포트 충돌 해결 방법

### 2. QUICK_REFERENCE.md
**설정 파일 섹션**:
- ✅ ddl-auto: update로 변경
- ✅ DDL 옵션 선택 가이드 추가

**신규 섹션**:
- ✅ 서버 실행 규칙
- ✅ 허용/금지 명령어 명시

### 3. CHANGELOG.md
**버전 3.5.3**:
- ✅ 프로젝트 규칙 추가 이력
- ✅ 규칙 9, 10 상세 내용
- ✅ 문서 업데이트 내역

### 4. README.md
**신규 섹션**:
- ✅ 서버 실행 방법
- ✅ 허용/금지 명령어
- ✅ 빌드 및 컴파일 가이드

### 5. application-dev.yml
**Hibernate 설정**:
- ✅ ddl-auto: update
- ✅ naming strategy 추가
- ✅ 스키마 관리 옵션 명시

---

## 🎯 적용 효과

### Before (문제 발생)
```
❌ 5개 테이블 DROP 오류
❌ 신규 테이블 추가 시 매번 오류 발생 가능성
❌ 서버 시작 시마다 데이터 삭제
❌ bootRun 사용 시 포트 점유 문제
```

### After (규칙 적용 후)
```
✅ 모든 테이블 DROP 오류 없음
✅ 신규 테이블 자동으로 규칙 적용
✅ 기존 데이터 유지 (update 모드)
✅ IDE 실행으로 포트 관리 간편
✅ 스키마 변경 시 자동 ALTER TABLE
```

---

## 📋 개발자 체크리스트

### 신규 Entity 추가 시
- [ ] `@Table(name = "테이블명")` 명시
- [ ] `del_flag`, `deleted_at` 필드 추가 (Soft Delete)
- [ ] `@SQLDelete` 어노테이션 추가
- [ ] `@SQLRestriction("del_flag = 0")` 추가
- [ ] TABLE_DEFINITION.md 업데이트
- [ ] CHANGELOG.md에 이력 기록

### 서버 실행 시
- [ ] IDE에서 실행 (Run 버튼)
- [ ] Active profiles: dev 확인
- [ ] http://localhost:8080 접속 테스트
- [ ] ❌ `./gradlew bootRun` 사용하지 않기

### 데이터 초기화 필요 시
- [ ] IDE에서 서버 중지
- [ ] MySQL에서 `drop-all-tables.sql` 실행
- [ ] 서버 재시작
- [ ] DataInit이 자동으로 데이터 생성

---

## 🔄 다음 단계

### 개발 환경 확인
1. ✅ application-dev.yml 설정 확인
2. ✅ IDE 실행 설정 확인 (Active profiles: dev)
3. ✅ MySQL 데이터베이스 실행 확인

### 서버 재시작
1. IDE에서 서버 중지
2. PetClinicApplication.java 실행
3. 콘솔에서 오류 없는지 확인
4. http://localhost:8080 접속 테스트

### 예상 결과
```
✅ 서버 정상 기동
✅ 테이블 자동 생성 (CREATE TABLE)
✅ 데이터 자동 생성 (DataInit)
✅ 메인 페이지 정상 접속
✅ 로그인/회원가입 페이지 정상 작동
```

---

## 📝 규칙 11: 게시판 콘텐츠 에디터 사용 (추가: 2025-11-25)

### 원칙
- 모든 게시판의 글쓰기 및 수정 기능에서 **Quill Editor 사용 필수**
- 단순 `<textarea>` 대신 풍부한 텍스트 편집 기능 제공
- 향후 추가되는 모든 게시판도 동일하게 적용

### 적용 대상
1. ✅ 온라인상담 글쓰기 (`counsel-write.html`)
2. ✅ 공지사항 글쓰기 (`noticeWrite.html`)
3. ✅ 향후 추가되는 모든 게시판

### Quill Editor 설정
```html
<!-- Quill Editor CSS -->
<link rel="stylesheet" th:href="@{/css/quill/quill.snow.css}">

<!-- Editor 영역 -->
<div id="editor" style="height: 400px;"></div>
<textarea name="content" id="content" hidden></textarea>

<!-- Quill Editor JS -->
<script th:src="@{/js/quill/quill.min.js}"></script>

<script>
  var quill = new Quill('#editor', {
    theme: 'snow',
    modules: {
      toolbar: [
        [{ 'header': [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        [{ 'color': [] }, { 'background': [] }],
        ['link'],
        ['clean']
      ]
    },
    placeholder: '내용을 입력하세요...'
  });
  
  // 폼 제출 시 에디터 내용을 textarea에 동기화
  document.getElementById('form').addEventListener('submit', function(e) {
    document.getElementById('content').value = quill.root.innerHTML;
  });
</script>
```

### 필수 검증
- [ ] 에디터 정상 표시
- [ ] 텍스트 서식 기능 작동 (Bold, Italic, 리스트 등)
- [ ] 폼 제출 시 content에 HTML 저장
- [ ] XSS 방지 (서버에서 Jsoup으로 sanitize)

---

## 🔒 규칙 12: 공지사항 권한 관리 (추가: 2025-11-25)

### 원칙
- **관리자만** 공지사항 작성/수정/삭제 가능
- 일반 회원 및 게스트는 **읽기만** 가능

### 권한 체크 방법

#### 1. 컨트롤러 레벨
```java
@PreAuthorize("hasRole('ROLE_ADMIN')")
@GetMapping("/write")
public String writeForm(Model model) {
    // ...
}

@PreAuthorize("hasRole('ROLE_ADMIN')")
@PostMapping("/write")
public String create(@ModelAttribute CommunityPostDto postDto) {
    // ...
}
```

#### 2. Thymeleaf 뷰 레벨
```html
<!-- 관리자만 버튼 표시 -->
<div th:if="${#authentication != null && #authorization.expression('hasRole(\'ROLE_ADMIN\')')}">
  <a th:href="@{/community/write}">글쓰기</a>
</div>
```

### 권한 확인 체크리스트
- [ ] 컨트롤러에 `@PreAuthorize` 추가
- [ ] 뷰에서 버튼 표시 조건 체크
- [ ] 비인가 접근 시 403 에러 또는 로그인 페이지 이동
- [ ] 로그 기록 (접근 시도, 권한 부족 등)

---

---

## 📝 규칙 13: 게시판 내용 작성 시 에디터 사용 (추가: 2025-11-25)

### 원칙
- 모든 게시판의 **글쓰기/수정** 화면에서 **Quill Editor 사용**
- 댓글/대댓글은 **일반 textarea 사용** (에디터 사용 안 함)

### 적용 대상
- ✅ 온라인상담 게시판 (counsel)
- ✅ 포토게시판 (photo)
- ✅ 공지사항 (community)
- ✅ 자주묻는질문 (FAQ)
- 📋 향후 추가되는 모든 게시판

### 적용 방법

#### 1. HTML 템플릿
```html
<!-- Quill CSS -->
<link rel="stylesheet" th:href="@{/css/quill/quill.snow.css}">

<!-- 에디터 영역 -->
<div id="editor" style="height: 400px;"></div>
<textarea name="content" id="content" hidden></textarea>

<!-- Quill JS -->
<script th:src="@{/js/quill/quill.min.js}"></script>
<script>
  var quill = new Quill('#editor', {
    theme: 'snow',
    modules: {
      toolbar: [
        [{ 'header': [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        [{ 'color': [] }, { 'background': [] }],
        ['link', 'image'],
        ['clean']
      ]
    }
  });
  
  // 폼 제출 시 에디터 내용 동기화
  document.getElementById('myForm').addEventListener('submit', function() {
    document.getElementById('content').value = quill.root.innerHTML;
  });
</script>
```

#### 2. 이미지 관련 규칙
- 포토게시판: 썸네일이 없으면 본문의 첫 번째 이미지 자동 추출
- animal 폴더의 이미지를 init 데이터 및 테스트용으로 사용
- 이미지 URL은 `/animal/파일명` 형식 사용

### 체크리스트
- [ ] 글쓰기 화면에 Quill Editor 적용
- [ ] 수정 화면에 Quill Editor 적용
- [ ] 에디터 초기값 설정 (수정 시)
- [ ] 폼 제출 시 content에 HTML 저장
- [ ] XSS 방지 (서버에서 Jsoup으로 sanitize)
- [ ] 댓글/대댓글은 일반 textarea 사용
- [ ] 정적 리소스 경로 검증 (실제 파일 존재 확인)

---

## 🖼️ 규칙 14: 썸네일 처리 규칙 (추가: 2025-11-25)

### 원칙
- 포토게시판 등 이미지 중심 게시판에서는 **썸네일 설정 방식 2가지 제공**
- 파일 첨부를 우선 노출, 이미지 URL도 선택 가능

### 썸네일 처리 방식

#### 1. 탭 UI 제공
```html
<ul class="nav nav-tabs">
  <li class="nav-item">
    <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#file-panel">
      파일 첨부
    </button>
  </li>
  <li class="nav-item">
    <button class="nav-link" data-bs-toggle="tab" data-bs-target="#url-panel">
      이미지 URL
    </button>
  </li>
</ul>
```

#### 2. 자동 추출 기능
- 썸네일이 비어있으면 **본문의 첫 번째 이미지 자동 추출**
- Service 계층에서 `extractFirstImageFromHtml()` 메서드로 처리

#### 3. 파일 업로드 (TODO)
- 현재는 이미지 URL만 지원
- 추후 파일 업로드 기능 구현 예정

### 체크리스트
- [x] 탭 UI 제공 (파일 첨부 / 이미지 URL)
- [x] 자동 추출 기능 구현
- [ ] 실제 파일 업로드 기능 구현 (추후)

---

## ⚠️ 규칙 15: 정적 리소스 경로 검증 (추가: 2025-11-25)

### 원칙
- HTML/Thymeleaf에서 JS/CSS 파일을 참조할 때 **실제 파일 존재 확인 필수**
- `quill.min.js` vs `quill.js` 같은 파일명 불일치 주의

### 검증 방법

#### 1. 개발 시 체크리스트
```
✅ src/main/resources/static/ 경로에 파일 존재 확인
✅ 파일명 정확히 일치 (min.js, .js 등)
✅ 브라우저 개발자 도구로 404 오류 확인
✅ MIME type 오류 확인 (application/json 등)
```

#### 2. 오류 예시 및 해결
```
❌ 오류: GET /js/quill/quill.min.js 404 (Not Found)
✅ 해결: /js/quill/quill.js (실제 파일명 확인)

❌ 오류: MIME type 'application/json' is not executable
✅ 해결: 정적 리소스 매핑 확인 (WebConfig)
```

#### 3. WebConfig 정적 리소스 매핑
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/animal/**")
        .addResourceLocations("classpath:/static/animal/");
}
```

### 체크리스트
- [ ] HTML에서 참조하는 모든 JS/CSS 파일 존재 확인
- [ ] 파일명 정확히 일치 (대소문자, 확장자)
- [ ] 브라우저 개발자 도구에서 404 오류 없는지 확인
- [ ] MIME type 오류 없는지 확인

---

**규칙 추가 완료**: 2025-11-06, 2025-11-25  
**업데이트된 문서**: 8개  
**적용 범위**: 모든 게시판 및 정적 리소스  
**서버 재시작**: IDE에서 실행하세요!

