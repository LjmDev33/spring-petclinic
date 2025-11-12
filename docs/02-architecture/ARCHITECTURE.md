# 🏛️ 시스템 아키텍처 (System Architecture)

**프로젝트**: Spring PetClinic  
**버전**: 3.5.3  
**최종 수정일**: 2025-11-11  
**작성자**: Jeongmin Lee

---

## 📋 목차
1. [시스템 개요](#시스템-개요)
2. [아키텍처 다이어그램](#아키텍처-다이어그램)
3. [레이어 구조](#레이어-구조)
4. [패키지 의존성](#패키지-의존성)
5. [데이터 흐름](#데이터-흐름)
6. [보안 아키텍처](#보안-아키텍처)
7. [파일 저장 구조](#파일-저장-구조)
8. [기술 스택](#기술-스택)

---

## 시스템 개요

### 아키텍처 패턴
- **Layered Architecture** (계층형 아키텍처)
- **MVC Pattern** (Model-View-Controller)
- **Repository Pattern** (데이터 접근 추상화)
- **DTO Pattern** (데이터 전송 객체)

### 설계 원칙
1. **관심사의 분리** (Separation of Concerns)
2. **단일 책임 원칙** (Single Responsibility Principle)
3. **의존성 역전 원칙** (Dependency Inversion Principle)
4. **인터페이스 분리 원칙** (Interface Segregation Principle)

---

## 아키텍처 다이어그램

### 전체 시스템 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│                  (Web Browser, Mobile)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/HTTPS
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                   Presentation Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Controller  │  │  Thymeleaf   │  │    View      │      │
│  │   (REST)     │→ │  Template    │→ │   (HTML)     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                    Business Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Service    │→ │    Mapper    │→ │     DTO      │      │
│  │ (@Service)   │  │              │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                   Persistence Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Repository  │→ │   QueryDSL   │→ │    Entity    │      │
│  │     (JPA)    │  │    (Impl)    │  │   (@Entity)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────┬──────────────────────────────────────┘
                       │ JDBC
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                      Database Layer                          │
│                    MySQL 8.0 (InnoDB)                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     File Storage Layer                       │
│              Local File System (data/ 디렉토리)              │
└─────────────────────────────────────────────────────────────┘
```

---

## 레이어 구조

### 1. Presentation Layer (프레젠테이션 계층)

**역할**: 사용자 요청 처리 및 응답 생성

**구성 요소**:
```
Controller → View (Thymeleaf)
```

**주요 클래스**:
- `CounselController` - 온라인상담 요청 처리
- `CommunityController` - 커뮤니티 요청 처리
- `AuthController` - 인증 요청 처리
- `FileDownloadController` - 파일 다운로드 처리

**책임**:
- ✅ HTTP 요청 파라미터 검증
- ✅ Service 계층 호출
- ✅ DTO → View Model 변환
- ✅ 응답 생성 (HTML, JSON, Redirect)
- ❌ 비즈니스 로직 포함 금지
- ❌ Entity 직접 노출 금지

---

### 2. Business Layer (비즈니스 계층)

**역할**: 비즈니스 로직 처리 및 트랜잭션 관리

**구성 요소**:
```
Service (@Service) → Mapper → DTO
```

**주요 클래스**:
- `CounselService` - 온라인상담 비즈니스 로직
- `CommunityService` - 커뮤니티 비즈니스 로직
- `UserService` - 사용자 관리 비즈니스 로직
- `FileStorageService` - 파일 저장 로직

**책임**:
- ✅ 비즈니스 로직 구현
- ✅ 트랜잭션 관리 (@Transactional)
- ✅ Entity ↔ DTO 변환 (Mapper 사용)
- ✅ 여러 Repository 조합
- ✅ 예외 처리 및 로깅
- ❌ HTTP 요청/응답 처리 금지
- ❌ SQL 쿼리 직접 작성 금지

---

### 3. Persistence Layer (영속성 계층)

**역할**: 데이터베이스 접근 및 CRUD 처리

**구성 요소**:
```
Repository (JpaRepository) → QueryDSL (RepositoryImpl) → Entity
```

**주요 인터페이스**:
- `CounselPostRepository` - 온라인상담 게시글 저장소
- `CounselCommentRepository` - 댓글 저장소
- `AttachmentRepository` - 첨부파일 저장소
- `UserRepository` - 사용자 저장소

**QueryDSL 구현**:
```
CounselPostRepositoryCustom (인터페이스)
    ↓
CounselPostRepositoryImpl (구현체)
    ↓
JPAQueryFactory (QueryDSL)
```

**책임**:
- ✅ CRUD 메서드 제공
- ✅ 동적 쿼리 생성 (QueryDSL)
- ✅ 페이징/정렬 처리
- ✅ Entity 반환
- ❌ 비즈니스 로직 포함 금지
- ❌ DTO 직접 생성 금지

---

## 패키지 의존성

### 패키지 구조

```
org.springframework.samples.petclinic
├── common/                  # 공통 모듈
│   ├── config/              # 설정 (QueryDSL, Database)
│   ├── dto/                 # 공통 DTO (PageResponse)
│   ├── entity/              # 공통 Entity (BaseEntity)
│   └── init/                # 데이터 초기화
│
├── counsel/                 # 온라인상담 모듈
│   ├── controller/          # Controller
│   ├── service/             # Service
│   ├── repository/          # Repository
│   ├── dto/                 # DTO
│   ├── mapper/              # Mapper
│   ├── model/               # Entity (Attachment)
│   ├── table/               # Entity (Post, Comment)
│   └── scheduler/           # 스케줄러
│
├── community/               # 커뮤니티 모듈
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   ├── mapper/
│   └── table/
│
├── user/                    # 사용자 모듈
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── table/
│
├── security/                # 보안 모듈
│   ├── SecurityConfig       # Spring Security 설정
│   └── handler/             # 인증 핸들러
│
└── system/                  # 시스템 모듈
    ├── CacheConfiguration   # 캐시 설정
    ├── WebConfiguration     # 웹 설정
    ├── repository/          # SystemConfig 저장소
    └── service/             # SystemConfig 서비스
```

### 의존성 규칙

```
Controller → Service → Repository → Entity
    ↓          ↓
   DTO    ←  Mapper
```

**허용되는 의존성**:
- Controller → Service ✅
- Controller → DTO ✅
- Service → Repository ✅
- Service → Mapper ✅
- Service → Entity ✅
- Repository → Entity ✅
- Mapper: Entity ↔ DTO ✅

**금지되는 의존성**:
- Controller → Repository ❌
- Controller → Entity ❌
- Repository → Service ❌
- Entity → DTO ❌

---

## 데이터 흐름

### 요청 처리 흐름 (온라인상담 목록 조회)

```
1. HTTP Request
   ↓
   GET /counsel/list?page=0&type=title&keyword=수술

2. Controller
   ↓
   CounselController.list(page, type, keyword)
   - 파라미터 검증
   - Service 호출

3. Service
   ↓
   CounselService.search(type, keyword, pageable)
   - 비즈니스 로직 처리
   - Repository 호출
   - Entity → DTO 변환 (Mapper)

4. Repository
   ↓
   CounselPostRepositoryImpl.search(...)
   - QueryDSL로 동적 쿼리 생성
   - WHERE title LIKE '%수술%'
   - LIMIT 10 OFFSET 0

5. Database
   ↓
   MySQL: SELECT * FROM counsel_post WHERE ...
   - Entity 객체로 반환

6. Mapper
   ↓
   CounselPostMapper.toDto(entity)
   - Entity → DTO 변환
   - 민감 정보 필터링

7. View (Thymeleaf)
   ↓
   counselList.html
   - DTO 데이터 렌더링
   - 페이지네이션 표시

8. HTTP Response
   ↓
   HTML 응답
```

### 게시글 등록 흐름

```
1. HTTP Request (Multipart)
   ↓
   POST /counsel
   - title, content, authorName, files

2. Controller
   ↓
   CounselController.create(dto, files)
   - MultipartFile 검증
   - Service 호출

3. Service
   ↓
   CounselService.createPost(dto, files)
   - @Transactional 시작
   - DTO → Entity 변환
   - 파일 저장 (FileStorageService)
   - 본문 HTML 저장 (CounselContentStorage)
   - Repository.save()

4. FileStorageService
   ↓
   - 파일 MIME 타입 검증
   - UUID 생성
   - data/counsel/uploads/yyyy/MM/ 디렉토리 생성
   - 파일 저장
   - Attachment Entity 생성

5. Repository
   ↓
   counselPostRepository.save(entity)
   - INSERT INTO counsel_post
   - @Transactional 커밋

6. Response
   ↓
   redirect:/counsel/detail/{id}
   - Flash Message: "게시글이 등록되었습니다."
```

---

## 보안 아키텍처

### Spring Security 구조

```
┌─────────────────────────────────────────┐
│         SecurityFilterChain             │
├─────────────────────────────────────────┤
│  1. SecurityContextPersistenceFilter    │  ← 세션 복원
│  2. UsernamePasswordAuthenticationFilter│  ← 로그인 처리
│  3. RememberMeAuthenticationFilter      │  ← Remember-Me 토큰
│  4. AnonymousAuthenticationFilter       │  ← 익명 사용자
│  5. ExceptionTranslationFilter          │  ← 예외 처리
│  6. FilterSecurityInterceptor           │  ← 권한 검사
└─────────────────────────────────────────┘
```

### 인증/인가 흐름

```
1. 로그인 요청
   ↓
   POST /login
   - username, password

2. AuthenticationManager
   ↓
   - UserDetailsService.loadUserByUsername()
   - CustomUserDetailsService 구현체

3. 사용자 조회
   ↓
   UserRepository.findByUsername(username)
   - User Entity 반환

4. 비밀번호 검증
   ↓
   PasswordEncoder.matches(rawPassword, encodedPassword)
   - BCrypt 해싱 비교

5. 인증 성공
   ↓
   SecurityContext에 Authentication 저장
   - 세션에 JSESSIONID 발급
   - Remember-Me 토큰 생성 (선택)

6. 권한 확인
   ↓
   @PreAuthorize("hasRole('ADMIN')")
   - ROLE_ADMIN 확인
   - 없으면 403 Forbidden
```

### 비밀번호 보안

**해싱 알고리즘**: BCrypt

```java
// 회원가입 시
String encodedPassword = passwordEncoder.encode(rawPassword);
// $2a$10$N9qo8uLOickgx2ZMRZoMye...

// 로그인 시
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

**게시글 비밀번호**:
- BCrypt 해싱 저장
- 세션에 unlock된 ID 저장
- 재방문 시 비밀번호 재입력 없이 접근

---

## 파일 저장 구조

### 디렉토리 구조

```
data/
└── counsel/
    ├── contents/              # 본문 HTML 파일
    │   └── yyyy/
    │       └── MM/
    │           └── {UUID}.html
    │
    └── uploads/               # 첨부파일
        └── yyyy/
            └── MM/
                └── {UUID}.{ext}
```

### 파일 저장 흐름

```
1. 파일 업로드
   ↓
   MultipartFile file

2. MIME 타입 검증
   ↓
   Apache Tika
   - image/jpeg, image/png, image/gif 허용
   - 최대 크기: 5MB

3. UUID 생성
   ↓
   UUID.randomUUID()
   - 예: 550e8400-e29b-41d4-a716-446655440000

4. 디렉토리 생성
   ↓
   data/counsel/uploads/2025/11/
   - Files.createDirectories()

5. 파일 저장
   ↓
   {UUID}.jpg
   - InputStream → FileOutputStream

6. Entity 생성
   ↓
   Attachment
   - file_path: 2025/11/{UUID}.jpg
   - original_file_name: 사진.jpg
   - file_size: 102400
   - mime_type: image/jpeg

7. 데이터베이스 저장
   ↓
   attachmentRepository.save()
```

### Soft Delete 정책

```
1. 삭제 요청
   ↓
   repository.delete(entity)

2. @SQLDelete 실행
   ↓
   UPDATE counsel_post 
   SET del_flag = 1, deleted_at = NOW() 
   WHERE id = ?

3. @SQLRestriction 적용
   ↓
   모든 SELECT 쿼리에 자동 추가:
   WHERE del_flag = 0

4. FileCleanupScheduler
   ↓
   매일 자정 실행
   - deleted_at < 2주 전
   - 물리적 파일 삭제
   - 데이터베이스 레코드 삭제 (DELETE)
```

---

## 기술 스택

### Backend

| 계층 | 기술 | 버전 | 용도 |
|------|------|------|------|
| Framework | Spring Boot | 3.5.0 | 애플리케이션 프레임워크 |
| ORM | Spring Data JPA | 3.5.0 | JPA 추상화 |
| Query | QueryDSL | 5.0.0 | 동적 쿼리 생성 |
| Database | MySQL | 8.0 | 관계형 데이터베이스 |
| Connection Pool | HikariCP | 6.3.0 | 커넥션 풀 |
| Security | Spring Security | 6.x | 인증/인가 |
| Validation | Hibernate Validator | 8.0.2 | 입력 검증 |
| Cache | Caffeine | 3.2.0 | 로컬 캐시 |
| Scheduler | Spring Scheduler | 6.x | 스케줄링 |
| Build | Gradle | 8.14.3 | 빌드 도구 |

### Frontend

| 기술 | 버전 | 용도 |
|------|------|------|
| Thymeleaf | 3.1.3 | 서버 사이드 템플릿 엔진 |
| Bootstrap | 5.3.x | CSS 프레임워크 |
| Bootstrap Icons | 1.11.x | 아이콘 |
| JavaScript | ES6+ | 클라이언트 스크립트 |

### DevOps

| 도구 | 용도 |
|------|------|
| Git | 버전 관리 |
| IntelliJ IDEA | IDE |
| MySQL Workbench | 데이터베이스 관리 |
| Postman | API 테스트 |

---

## 변경 이력

### [3.5.3] - 2025-11-11
#### 추가
- 최초 아키텍처 문서 작성
- 시스템 구조 다이어그램
- 레이어 구조 정의
- 패키지 의존성 규칙
- 데이터 흐름 설명
- 보안 아키텍처
- 파일 저장 구조
- 기술 스택 정리

---

**문서 버전**: 1.0  
**최종 검토**: 2025-11-11  
**담당자**: Jeongmin Lee

