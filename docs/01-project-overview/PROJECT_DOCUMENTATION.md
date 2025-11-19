# 🏥 Spring PetClinic 프로젝트 상세 문서

**작성일**: 2025년 11월 6일  
**버전**: 3.5.1  
**작성자**: Jeongmin Lee

---

## 📑 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [전체 아키텍처](#2-전체-아키텍처)
3. [패키지 구조](#3-패키지-구조)
4. [데이터베이스 설계](#4-데이터베이스-설계)
5. [API 요청 흐름](#5-api-요청-흐름)
6. [개발 규칙](#6-개발-규칙)
7. [주요 기능 명세](#7-주요-기능-명세)
8. [설정 파일](#8-설정-파일)

---

## 1. 프로젝트 개요

### 1.1 프로젝트 정보

- **프로젝트명**: Spring PetClinic (동물병원 관리 시스템)
- **기술 스택**:
  - Backend: Spring Boot 3.5.0, Spring Data JPA, QueryDSL 5.0.0
  - Database: MySQL 8.0
  - View: Thymeleaf 3.1.3
  - Build: Gradle 8.14.3
  - Java: JDK 17
- **목적**: 동물병원 온라인 상담 및 커뮤니티 게시판 시스템

### 1.2 주요 모듈

| 모듈 | 설명 | 상태 |
|------|------|------|
| **counsel** | 온라인상담 게시판 (비공개/공개, 댓글, 첨부파일) | ✅ 구현 완료 |
| **community** | 커뮤니티 게시판 (공지사항, 자유게시판) | ✅ 구현 완료 |
| **common** | 공통 모듈 (Entity, DTO, Config, DataInit) | ✅ 구현 완료 |
| **system** | 시스템 설정 (Cache, Web, Welcome) | ✅ 구현 완료 |

---

## 2. 전체 아키텍처

### 2.1 레이어드 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
│  Controller (Thymeleaf Views / REST API)                    │
├─────────────────────────────────────────────────────────────┤
│                        Service Layer                         │
│  Business Logic, Transaction Management                     │
├─────────────────────────────────────────────────────────────┤
│                      Repository Layer                        │
│  JPA Repository + QueryDSL Custom (RepositoryImpl)          │
├─────────────────────────────────────────────────────────────┤
│                        Database Layer                        │
│  MySQL 8.0 (Foreign Key Checks = 0 in DEV)                 │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 의존성 흐름

```
Controller → Service → Repository → Entity
    ↓          ↓          ↓           ↓
   DTO ←── Mapper ←── Entity ←── Database
```

**핵심 원칙**:
- ❌ **Entity를 뷰/API에 직접 노출하지 않음**
- ✅ **DTO를 통한 데이터 전달** (Mapper 클래스 사용)
- ✅ **Service에서 비즈니스 로직 집중**
- ✅ **RepositoryImpl에서 QueryDSL 검색/페이징 처리**

---

## 3. 패키지 구조

### 3.1 전체 구조

```
org.springframework.samples.petclinic
│
├── 📦 common/                         # 공통 모듈
│   ├── config/                       # 설정 클래스
│   │   ├── QuerydslConfig            # JPAQueryFactory Bean
│   │   └── DatabaseConfig            # 개발환경 DB 설정
│   ├── dto/                          # 공통 DTO
│   │   └── PageResponse              # 페이징 공통 응답
│   ├── entity/                       # 공통 Entity
│   │   ├── BaseEntity                # id 필드 (Auto Increment)
│   │   └── NamedEntity               # name 필드
│   ├── init/                         # 데이터 초기화
│   │   └── DataInit                  # CommandLineRunner
│   └── table/                        # 공통 테이블
│       └── Attachment                # 공용 첨부파일
│
├── 📦 community/                      # 커뮤니티 게시판
│   ├── controller/
│   │   └── CommunityController       # 공지사항/자유게시판
│   ├── dto/
│   │   └── CommunityPostDto
│   ├── mapper/
│   │   └── CommunityPostMapper       # Entity ↔ DTO
│   ├── repository/
│   │   ├── CommunityPostRepository   # JpaRepository
│   │   └── CommunityPostRepositoryImpl  # QueryDSL Custom
│   ├── service/
│   │   └── CommunityService
│   └── table/
│       ├── CommunityPost             # 게시글 Entity
│       ├── CommunityPostAttachment   # 첨부파일 관계
│       └── CommunityPostAttachmentId # 복합키
│
├── 📦 counsel/                        # 온라인상담 게시판
│   ├── controller/
│   │   ├── CounselController         # 온라인상담 CRUD
│   │   └── FileDownloadController    # 파일 다운로드
│   ├── dto/
│   │   ├── CounselPostDto            # 게시글 DTO
│   │   ├── CounselPostWriteDto       # 작성 전용 DTO
│   │   ├── CounselCommentDto         # 댓글 DTO
│   │   └── AttachmentDto             # 첨부파일 DTO
│   ├── mapper/
│   │   ├── CounselPostMapper
│   │   ├── CounselCommentMapper
│   │   └── AttachmentMapper
│   ├── model/
│   │   └── Attachment                # 온라인상담 전용 첨부파일
│   ├── repository/
│   │   ├── CounselPostRepository     # JpaRepository
│   │   ├── CounselPostRepositoryImpl # QueryDSL
│   │   ├── CounselCommentRepository
│   │   ├── AttachmentRepository
│   │   └── CounselPostAttachmentRepository
│   ├── scheduler/
│   │   └── FileCleanupScheduler      # 2주 후 파일 삭제
│   ├── service/
│   │   ├── CounselService            # 비즈니스 로직
│   │   ├── FileStorageService        # 파일 저장/관리
│   │   └── CounselContentStorage     # 본문 파일 저장
│   ├── table/
│   │   ├── CounselPost               # 게시글 Entity
│   │   ├── CounselComment            # 댓글 Entity
│   │   ├── CounselPostAttachment     # 게시글-첨부파일 관계
│   │   ├── CounselCommentAttachment  # 댓글-첨부파일 관계
│   │   ├── CounselPostAttachmentId
│   │   └── CounselCommentAttachmentId
│   └── CounselStatus.java            # Enum (WAIT, COMPLETE, END)
│
└── 📦 system/                         # 시스템 설정
    ├── BooleanToYNConverter          # Boolean ↔ 'Y'/'N'
    ├── CacheConfiguration            # Caffeine 캐시
    ├── WebConfiguration              # 웹 설정
    ├── WelcomeController             # 홈 페이지
    └── CrashController               # 에러 테스트
```

---

## 4. 데이터베이스 설계

### 4.1 ERD (온라인상담 모듈)

```
┌──────────────────────┐
│   counsel_post       │ (게시글)
├──────────────────────┤
│ id (PK)              │ BIGINT
│ title                │ VARCHAR(255)
│ content              │ MEDIUMTEXT ("[stored]")
│ content_path         │ VARCHAR(500) (파일 경로)
│ author_name          │ VARCHAR(100)
│ author_email         │ VARCHAR(120)
│ password_hash        │ VARCHAR(100) (BCrypt)
│ is_secret            │ BOOLEAN (비공개 여부)
│ status               │ ENUM (WAIT, COMPLETE, END)
│ view_count           │ INT
│ comment_count        │ INT
│ created_at           │ DATETIME
│ updated_at           │ DATETIME
│ del_flag             │ BOOLEAN
│ deleted_at           │ DATETIME
│ deleted_by           │ VARCHAR(60)
│ attach_flag          │ BOOLEAN
└──────────────────────┘
         │ 1
         │
         │ N
┌──────────────────────┐
│  counsel_comment     │ (댓글)
├──────────────────────┤
│ id (PK)              │ BIGINT
│ post_id (FK)         │ BIGINT → counsel_post.id
│ parent_id (FK)       │ BIGINT → counsel_comment.id
│ content              │ TEXT
│ author_name          │ VARCHAR(100)
│ author_email         │ VARCHAR(120)
│ password_hash        │ VARCHAR(100)
│ is_staff_reply       │ BOOLEAN (운영자 답변)
│ created_at           │ DATETIME
│ updated_at           │ DATETIME
│ del_flag             │ BOOLEAN
│ deleted_at           │ DATETIME
│ deleted_by           │ VARCHAR(60)
└──────────────────────┘

┌──────────────────────┐
│ counsel_attachments  │ (첨부파일)
├──────────────────────┤
│ id (PK)              │ INT
│ file_path            │ VARCHAR (yyyy/MM/UUID.ext)
│ original_file_name   │ VARCHAR
│ file_size            │ BIGINT (bytes)
│ mime_type            │ VARCHAR
│ created_at           │ DATETIME
│ del_flag             │ BOOLEAN
│ deleted_at           │ DATETIME
└──────────────────────┘
         │ 1
         │
         │ N
┌────────────────────────────┐
│ counsel_post_attachments   │ (게시글-첨부파일 관계)
├────────────────────────────┤
│ id (PK)                    │ INT
│ counsel_post_id (FK)       │ BIGINT
│ attachment_id (FK)         │ INT
└────────────────────────────┘
```

### 4.2 테이블별 역할

#### **counsel_post** (온라인상담 게시글)

| 컬럼 | 타입 | 설명 | 비고 |
|------|------|------|------|
| `id` | BIGINT | Primary Key | Auto Increment |
| `title` | VARCHAR(255) | 게시글 제목 | NOT NULL |
| `content` | MEDIUMTEXT | 본문 내용 | "[stored]" 표시 (실제는 파일) |
| `content_path` | VARCHAR(500) | 본문 파일 경로 | `yyyy/MM/UUID.html` |
| `author_name` | VARCHAR(100) | 작성자 이름 | NOT NULL |
| `author_email` | VARCHAR(120) | 작성자 이메일 | Nullable |
| `password_hash` | VARCHAR(100) | BCrypt 해시 | 비공개 글만 사용 |
| `is_secret` | BOOLEAN | 비공개 여부 | false=공개, true=비공개 |
| `status` | ENUM | 상담 상태 | WAIT/COMPLETE/END |
| `view_count` | INT | 조회수 | DEFAULT 0 |
| `comment_count` | INT | 댓글 수 | DEFAULT 0 |
| `created_at` | DATETIME | 생성 일시 | @CreationTimestamp |
| `updated_at` | DATETIME | 수정 일시 | @UpdateTimestamp |
| `del_flag` | BOOLEAN | 삭제 플래그 | Soft Delete |
| `deleted_at` | DATETIME | 삭제 일시 | @SQLDelete 트리거 |
| `deleted_by` | VARCHAR(60) | 삭제자 | 추후 구현 |
| `attach_flag` | BOOLEAN | 첨부파일 존재 | DEFAULT false |

**인덱스**:
- `PRIMARY KEY (id)`
- `INDEX idx_post_created (created_at)`
- `INDEX idx_post_status (status)`

#### **counsel_comment** (댓글)

| 컬럼 | 타입 | 설명 | 비고 |
|------|------|------|------|
| `id` | BIGINT | Primary Key | Auto Increment |
| `post_id` | BIGINT | 게시글 ID (FK) | NOT NULL |
| `parent_id` | BIGINT | 부모 댓글 ID (FK) | 대댓글 기능 (1-depth) |
| `content` | TEXT | 댓글 내용 | NOT NULL |
| `author_name` | VARCHAR(100) | 작성자 이름 | NOT NULL |
| `author_email` | VARCHAR(120) | 작성자 이메일 | Nullable |
| `password_hash` | VARCHAR(100) | BCrypt 해시 | 삭제 시 검증용 |
| `is_staff_reply` | BOOLEAN | 운영자 답변 | true=운영자 댓글 |
| `created_at` | DATETIME | 생성 일시 | @CreationTimestamp |
| `updated_at` | DATETIME | 수정 일시 | @UpdateTimestamp |
| `del_flag` | BOOLEAN | 삭제 플래그 | Soft Delete |
| `deleted_at` | DATETIME | 삭제 일시 | @SQLDelete |
| `deleted_by` | VARCHAR(60) | 삭제자 | 추후 구현 |

**인덱스**:
- `PRIMARY KEY (id)`
- `INDEX idx_comment_post_created (post_id, created_at)`
- `INDEX idx_comment_parent (parent_id)`

#### **counsel_attachments** (첨부파일)

| 컬럼 | 타입 | 설명 | 비고 |
|------|------|------|------|
| `id` | INT | Primary Key | Auto Increment |
| `file_path` | VARCHAR | 파일 경로 | `yyyy/MM/UUID.ext` |
| `original_file_name` | VARCHAR | 원본 파일명 | 사용자가 업로드한 이름 |
| `file_size` | BIGINT | 파일 크기 | bytes 단위 |
| `mime_type` | VARCHAR | MIME 타입 | `image/jpeg`, `image/png` 등 |
| `created_at` | DATETIME | 생성 일시 | @CreationTimestamp |
| `del_flag` | BOOLEAN | 삭제 플래그 | Soft Delete |
| `deleted_at` | DATETIME | 삭제 일시 | @SQLDelete |

**스케줄러 연계**:
- `FileCleanupScheduler`가 매일 자정에 `del_flag=true && deleted_at < 2주 전` 파일 물리 삭제

### 4.3 데이터 관계

```
1. counsel_post (1) ──< (N) counsel_comment
   - 하나의 게시글은 여러 댓글을 가질 수 있음
   - @OneToMany (cascade = ALL, orphanRemoval = true)

2. counsel_comment (1) ──< (N) counsel_comment (대댓글)
   - 자기 참조 관계 (1-depth만 지원)
   - parent_id FK

3. counsel_post (N) ──< (N) counsel_attachments
   - 중간 테이블: counsel_post_attachments
   - Many-to-Many 관계

4. counsel_comment (N) ──< (N) attachment (공용)
   - 중간 테이블: counsel_comment_attachment
   - Many-to-Many 관계
```

### 4.4 Soft Delete 구현

```java
@Entity
@SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CounselPost extends BaseEntity {
    // ...
}
```

**동작 방식**:
1. `repository.delete(entity)` 호출 시 → `@SQLDelete` SQL 실행
2. 물리적 DELETE 대신 `del_flag=1`, `deleted_at=NOW()` UPDATE
3. `@SQLRestriction("del_flag = 0")` 으로 조회 시 자동 필터링
4. 2주 후 `FileCleanupScheduler`가 물리적 DELETE 수행

---

## 5. API 요청 흐름

### 5.1 온라인상담 목록 조회

**URL**: `GET /counsel/list?page=0&size=10&type=title&keyword=수술`

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTP GET /counsel/list
       ↓
┌──────────────────────────────────────────────────────────┐
│ 1. CounselController.list()                             │
│    - @GetMapping("/list")                               │
│    - Pageable: page=0, size=10, sort=id, ASC            │
│    - type="title", keyword="수술"                       │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 2. CounselService.search(type, keyword, pageable)       │
│    - @Transactional                                      │
│    - Entity 조회 후 DTO 변환                             │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 3. CounselPostRepositoryImpl.search()                   │
│    - QueryDSL BooleanBuilder로 동적 쿼리 생성            │
│    - WHERE title LIKE '%수술%'                           │
│    - OFFSET 0 LIMIT 10                                   │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 4. MySQL Database                                        │
│    SELECT * FROM counsel_post                            │
│    WHERE del_flag=0 AND title LIKE '%수술%'              │
│    ORDER BY id ASC                                       │
│    LIMIT 10 OFFSET 0;                                    │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 5. Entity → DTO 변환 (CounselPostMapper)                │
│    - Entity 필드 → DTO 필드 매핑                         │
│    - 최근 댓글 정보 주입 (commentRepository)             │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 6. Controller → Model에 데이터 추가                       │
│    model.addAttribute("posts", dtoList);                 │
│    model.addAttribute("page", pageResponse);             │
│    return "fragments/layout";                            │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 7. Thymeleaf Template 렌더링                             │
│    - counsel/counselList.html                            │
│    - th:each="post : ${posts}"                           │
│    - 페이지네이션: fragments/pagination.html              │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────┐
│ HTML Response │
└───────────────┘
```

### 5.2 온라인상담 게시글 작성

**URL**: `POST /counsel` (MultipartFile 포함)

```
┌─────────────┐
│   Browser   │ (Form Submit with File)
└──────┬──────┘
       │ POST /counsel (multipart/form-data)
       ↓
┌──────────────────────────────────────────────────────────┐
│ 1. CounselController.submit()                           │
│    - @PostMapping("")                                    │
│    - @ModelAttribute CounselPostWriteDto form            │
│    - title, content, authorName, secret, password,       │
│      attachments (MultipartFile[])                       │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 2. CounselService.saveNew(dto)                          │
│    - @Transactional (롤백 보장)                          │
└──────┬───────────────────────────────────────────────────┘
       │
       ├→ 2-1. CounselContentStorage.saveHtml(content)
       │    - HTML을 파일로 저장
       │    - 경로: data/counsel/contents/yyyy/MM/UUID.html
       │    - return path: "yyyy/MM/UUID.html"
       │
       ├→ 2-2. BCrypt 비밀번호 해싱
       │    - BCrypt.hashpw(password, BCrypt.gensalt())
       │
       ├→ 2-3. FileStorageService.storeFile(file)
       │    ├─ Apache Tika로 MIME 타입 검증
       │    ├─ 파일 크기 검증 (5MB 제한)
       │    ├─ UUID로 파일명 난수화
       │    └─ 경로: data/counsel/uploads/yyyy/MM/UUID.ext
       │
       └→ 2-4. Entity 생성 및 저장
            ├─ CounselPost entity
            ├─ Attachment entity
            └─ CounselPostAttachment (관계 테이블)
       ↓
┌──────────────────────────────────────────────────────────┐
│ 3. CounselPostRepository.save(entity)                   │
│    - JPA가 INSERT SQL 생성                               │
│    - @Transactional에 의해 커밋                          │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 4. MySQL Database                                        │
│    INSERT INTO counsel_post (...) VALUES (...);          │
│    INSERT INTO counsel_attachments (...);                │
│    INSERT INTO counsel_post_attachments (...);           │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 5. 생성된 ID 반환 → Redirect                             │
│    return "redirect:/counsel/detail/" + id;              │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────┐
│ 상세 페이지   │
└───────────────┘
```

### 5.3 비공개 게시글 비밀번호 검증

**URL**: `POST /counsel/detail/{id}/unlock`

```
┌─────────────┐
│   Browser   │ (비밀번호 입력 폼)
└──────┬──────┘
       │ POST /counsel/detail/5/unlock
       │ password=1234
       ↓
┌──────────────────────────────────────────────────────────┐
│ 1. CounselController.unlock()                           │
│    - @PostMapping("/detail/{id}/unlock")                │
│    - @PathVariable Long id                               │
│    - @RequestParam String password                       │
└──────┬───────────────────────────────────────────────────┘
       ↓
┌──────────────────────────────────────────────────────────┐
│ 2. CounselService.verifyPassword(id, password)          │
│    - repository.findById(id)                             │
│    - BCrypt.checkpw(rawPassword, passwordHash)           │
└──────┬───────────────────────────────────────────────────┘
       ↓
      [비밀번호 일치?]
       │
       ├─ YES → Session에 ID 저장
       │        session.setAttribute("counselUnlocked", Set<Long>)
       │        return "redirect:/counsel/detail/" + id;
       │
       └─ NO  → 비밀번호 입력 페이지로 다시 이동
                return "redirect:/counsel/detail/{id}/password?fail=1";
```

**세션 관리**:
```java
// 세션에 unlock된 게시글 ID Set 저장
Set<Long> unlocked = session.getAttribute("counselUnlocked");
unlocked.add(5L); // 게시글 ID 5번 unlock
session.setAttribute("counselUnlocked", unlocked);

// 상세 페이지 접근 시 세션 확인
boolean unlockedOk = unlocked != null && unlocked.contains(id);
if (post.isSecret() && !unlockedOk) {
    return "redirect:/counsel/detail/{id}/password";
}
```

---

## 6. 개발 규칙

### 6.1 코드 작성 규칙

#### **1. Entity 노출 금지**
```java
// ❌ 잘못된 예시
@GetMapping("/list")
public String list(Model model) {
    List<CounselPost> posts = repository.findAll();
    model.addAttribute("posts", posts); // Entity 직접 노출!
    return "list";
}

// ✅ 올바른 예시
@GetMapping("/list")
public String list(Model model) {
    List<CounselPost> entities = repository.findAll();
    List<CounselPostDto> dtos = entities.stream()
        .map(postMapper::toDto)
        .collect(Collectors.toList());
    model.addAttribute("posts", dtos); // DTO로 변환 후 전달
    return "list";
}
```

#### **2. Mapper 사용 강제**
```java
// Mapper 클래스
@Component
public class CounselPostMapper {
    public CounselPostDto toDto(CounselPost entity) {
        CounselPostDto dto = new CounselPostDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        // ... 필드 매핑
        return dto;
    }
}
```

#### **3. QueryDSL은 RepositoryImpl에서만**
```java
// CounselPostRepositoryImpl.java
public class CounselPostRepositoryImpl implements CounselPostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    
    @Override
    public PageResponse<CounselPost> search(String type, String keyword, Pageable pageable) {
        QCounselPost post = QCounselPost.counselPost;
        
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            switch (type) {
                case "title":
                    builder.and(post.title.containsIgnoreCase(keyword));
                    break;
                // ...
            }
        }
        
        List<CounselPost> content = queryFactory
            .selectFrom(post)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        return new PageResponse<>(content, pageable, total);
    }
}
```

#### **4. 날짜/시간은 LocalDateTime**
```java
// ✅ 올바른 방법
@CreationTimestamp
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

// Thymeleaf에서 날짜 포맷
<span th:text="${#temporals.format(post.createdAt, 'yyyy-MM-dd HH:mm:ss')}"></span>
```

#### **5. Soft Delete 사용**
```java
@Entity
@SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CounselPost extends BaseEntity {
    @Column(name = "del_flag", nullable = false)
    private boolean delFlag = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

#### **5-1. 테이블 변경 시 문서 즉각 반영** ⭐NEW (2025-11-12)

**규칙**: 테이블 추가/수정 시 `TABLE_DEFINITION.md`를 즉각 업데이트

**적용 시점**:
- ✅ Entity 클래스 생성/수정 완료 직후
- ✅ 테이블 컬럼 추가/삭제/변경 직후
- ✅ 외래키 제약 조건 변경 직후
- ✅ 인덱스 추가/삭제 직후

**업데이트 내용**:
1. 테이블 구조 (컬럼명, 타입, 제약조건)
2. 컬럼 설명 (각 필드의 용도)
3. 관계도 (외래키, 연관 관계)
4. 변경 이력 (날짜, 변경 사유)

**예시**:
```markdown
## counsel_post (온라인 상담 게시글)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 게시글 ID |
| title | VARCHAR(200) | NOT NULL | 게시글 제목 |
| content | TEXT | NOT NULL | 게시글 내용 |
| secret | BOOLEAN | NOT NULL, DEFAULT false | 비공개 여부 |
| password | VARCHAR(100) | NULL | 비공개 게시글 비밀번호 (BCrypt) |
| author_name | VARCHAR(50) | NOT NULL | 작성자 이름 |
| status | VARCHAR(20) | NOT NULL | 상태 (WAIT, COMPLETE, END) |
| view_count | INT | NOT NULL, DEFAULT 0 | 조회수 |
| del_flag | BOOLEAN | NOT NULL, DEFAULT false | 삭제 플래그 (Soft Delete) |
| created_at | DATETIME | NOT NULL | 생성일시 |
| updated_at | DATETIME | NOT NULL | 수정일시 |
| deleted_at | DATETIME | NULL | 삭제일시 |

**변경 이력**:
- 2025-11-06: 테이블 생성
- 2025-11-10: `view_count` 컬럼 추가
```

**체크리스트**:
- [ ] Entity 클래스 코드 작성 완료
- [ ] 테이블 정의서 업데이트 완료
- [ ] 컬럼 설명 주석 추가 완료
- [ ] 변경 이력 기록 완료
- [ ] CHANGELOG.md 업데이트 완료

#### **5-2. API 변경 시 문서 즉각 반영** ⭐NEW (2025-11-12)

**규칙**: API 추가/수정 시 `API_SPECIFICATION.md`를 즉각 업데이트

**적용 시점**:
- ✅ Controller 메서드 추가/수정 완료 직후
- ✅ 요청/응답 DTO 변경 직후
- ✅ 엔드포인트 URL 변경 직후
- ✅ HTTP 메서드 변경 직후

**업데이트 내용**:
1. 엔드포인트 정보 (URL, HTTP 메서드)
2. 요청 파라미터/바디 (DTO 구조)
3. 응답 포맷 (성공/실패 케이스)
4. 권한 요구사항 (로그인 필요 여부)
5. 변경 이력 (날짜, 변경 사유)

**예시**:
```markdown
### 온라인 상담 게시글 목록 조회

**엔드포인트**: `GET /counsel/list`

**권한**: 공개 (로그인 불필요)

**요청 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| type | String | X | 검색 타입 (title, author) | title |
| keyword | String | X | 검색 키워드 | - |
| page | Integer | X | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | X | 페이지 크기 | 10 |

**응답 (성공 - 200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "title": "강아지 중성화 수술 문의",
      "authorName": "홍길동",
      "status": "COMPLETE",
      "createdAt": "2025-11-06T10:30:00",
      "secret": false
    }
  ],
  "totalElements": 112,
  "totalPages": 12,
  "currentPage": 0
}
```

**응답 (실패 - 400 Bad Request)**:
```json
{
  "error": "Invalid search type",
  "message": "검색 타입은 title 또는 author만 가능합니다."
}
```

**변경 이력**:
- 2025-11-06: API 생성
- 2025-11-10: `secret` 필드 추가
```

**체크리스트**:
- [ ] Controller 메서드 구현 완료
- [ ] API 명세서 업데이트 완료
- [ ] 요청/응답 예시 작성 완료
- [ ] 권한 요구사항 명시 완료
- [ ] CHANGELOG.md 업데이트 완료

#### **6. 로그 관리**
```java
private static final Logger log = LoggerFactory.getLogger(CounselService.class);

// 삭제/갱신 시 명확한 로그
log.info("Successfully deleted comment with ID: {}", commentId);
log.error("Failed to process attachment file {}: {}", fileName, e.getMessage());
```

#### **7. 메모리 누수 방지**
```java
// try-catch로 파일 처리 보호
try (InputStream inputStream = file.getInputStream()) {
    String mimeType = tika.detect(inputStream);
    // ...
} catch (IOException e) {
    log.error("File validation failed: {}", e.getMessage());
    throw new RuntimeException("Error processing file.", e);
}
```

#### **8. 라이브러리 및 의존성 관리 규칙** ⭐NEW
```gradle
dependencies {
    // ✅ 올바른 예시: 안정적인 버전 사용
    implementation 'org.springframework.boot:spring-boot-starter-security:3.5.0'  // 최신 안정 버전
    implementation 'org.jsoup:jsoup:1.18.1'  // 보안 업데이트 버전
    implementation 'org.apache.tika:tika-core:2.9.2'  // 안정 버전
    
    // ❌ 잘못된 예시
    implementation 'some-library:1.0.0-SNAPSHOT'  // SNAPSHOT 버전 사용 금지
    implementation 'old-library:0.0.1-alpha'  // alpha/beta 버전 사용 금지
}
```

**라이브러리 추가 시 확인사항**:
1. ✅ **보안 이슈 확인**: CVE 데이터베이스에서 알려진 취약점 확인
2. ✅ **안정 버전 사용**: GA(General Availability) 버전 또는 Stable 버전만 사용
3. ✅ **최신 보안 패치**: 마이너 버전 업데이트 적용 (예: 1.18.0 → 1.18.1)
4. ✅ **라이선스 검토**: Apache 2.0, MIT 등 호환 가능한 라이선스 확인
5. ✅ **의존성 충돌 확인**: `./gradlew dependencies` 명령어로 충돌 검사
6. ❌ **금지 버전**: SNAPSHOT, alpha, beta, RC(Release Candidate) 금지

**예시: Spring Security 추가 시**:
```gradle
// build.gradle
dependencies {
    // Spring Security - 안정 버전 (3.5.0 기준)
    implementation 'org.springframework.boot:spring-boot-starter-security'  // ✅ BOM 기반
    implementation 'org.springframework.security:spring-security-crypto:6.3.4'  // ✅ 명시 버전
    implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'  // ✅ 호환 버전
    
    // ❌ 피해야 할 케이스
    // implementation 'org.springframework.security:spring-security-core:5.0.0'  // 구버전 (보안 취약)
    // implementation 'spring-security:spring-security:4.2.3-SNAPSHOT'  // SNAPSHOT 버전
}
```

**버전 선택 가이드**:
- Spring Boot: 최신 안정 버전 (3.x.x)
- Spring Security: Spring Boot BOM에 포함된 버전 우선
- QueryDSL: 5.0.0 이상 (Jakarta EE 지원)
- MySQL Connector: 9.x.x (최신 보안 패치)
- Thymeleaf: 3.1.x 이상
- Jackson: Spring Boot BOM 버전

**의존성 검증 명령어**:
```bash
# 의존성 트리 확인
./gradlew dependencies

# 취약점 스캔 (CycloneDX 플러그인 사용)
./gradlew cyclonedxBom

# 업데이트 가능한 버전 확인
./gradlew dependencyUpdates
```

#### **9. UI 설계 규칙** ⭐NEW (2025-11-12 업데이트)

**목적**: 사용자가 직관적으로 이해하고 사용할 수 있는 인터페이스 제공

**핵심 원칙**:
1. ✅ **직관성**: 사용자가 설명 없이도 기능을 이해할 수 있어야 함
2. ✅ **일관성**: 동일한 기능은 동일한 UI 패턴 사용
3. ✅ **피드백**: 사용자 액션에 대한 즉각적인 피드백 제공
4. ✅ **접근성**: 모바일 환경 및 다양한 화면 크기 지원
5. ✅ **모달 사용**: alert 대신 Bootstrap 모달 팝업 사용 ⭐NEW
6. ✅ **수량 명시**: 사용 가능한 개수/제한을 명확히 표시 ⭐NEW

**UI 작성 규칙**:
```html
<!-- ✅ 올바른 예시: 직관적인 아이콘 + 텍스트 -->
<button type="submit" class="btn btn-primary">
  <i class="bi bi-send"></i> 댓글 작성
</button>

<!-- ✅ 올바른 예시: 명확한 라벨 + 안내 문구 -->
<label for="nickname" class="form-label">
  <i class="bi bi-chat-dots"></i> 닉네임 <span class="text-danger">*</span>
</label>
<input type="text" id="nickname" name="nickname" class="form-control" 
       placeholder="게시판에 표시될 닉네임" required>
<small class="form-text text-muted">2-15자의 한글, 영문, 숫자만 사용 가능합니다.</small>

<!-- ✅ 올바른 예시: 실시간 피드백 -->
<input type="tel" id="phone" class="form-control" 
       placeholder="숫자만 입력하세요 (자동으로 형식이 적용됩니다)">
<small class="form-text text-muted">
  숫자만 입력하면 자동으로 010-0000-0000 형식으로 변환됩니다.
</small>

<!-- ❌ 잘못된 예시: 아이콘만 표시 -->
<button type="submit" class="btn btn-primary">
  <i class="bi bi-send"></i>
</button>

<!-- ❌ 잘못된 예시: 불명확한 라벨 -->
<label for="input1">입력</label>
<input type="text" id="input1" name="input1">
```

**버튼 균일성 규칙** ⭐NEW (2025-11-12):
```html
<!-- ✅ 같은 행의 버튼은 크기 및 라인 동일 -->
<div class="d-flex justify-content-end gap-2">
  <a href="/list" class="btn btn-secondary" style="min-width: 120px; height: 42px;">목록</a>
  <button type="submit" class="btn btn-primary" style="min-width: 120px; height: 42px;">확인</button>
</div>

<!-- ❌ 잘못된 예시: 크기 불일치 -->
<div class="d-flex gap-2">
  <a href="/list" class="btn btn-secondary">목록</a>
  <button type="submit" class="btn btn-primary btn-lg">확인</button>
</div>
```

**버튼 배치 규칙**:
```html
<!-- ✅ 주요 액션 버튼은 오른쪽 끝 -->
<div class="d-flex justify-content-between">
  <a href="/list" class="btn btn-secondary" style="min-width: 120px; height: 42px;">취소</a>
  <button type="submit" class="btn btn-primary" style="min-width: 120px; height: 42px;">저장</button>
</div>

<!-- ✅ 여러 액션 버튼은 gap으로 간격 조정 -->
<div class="d-flex justify-content-end gap-2">
  <button class="btn btn-warning">수정</button>
  <button class="btn btn-danger">삭제</button>
</div>
```

**UI/UX 일관성 규칙** ⭐ 중요:
```
✅ 버튼 크기 통일
   - 일반 버튼: height: 42px
   - 주요 액션 버튼: min-width: 120px; height: 42px
   - d-grid 버튼: width: 100%; height: 42px

✅ 폰트 크기 통일
   - 헤더 링크: font-size: 0.95rem
   - placeholder: font-size: 0.95rem (축소)
   - 본문: 기본 크기 (1rem)
   
✅ 간격(spacing) 통일
   - 링크 간격: px-2 (좌우), px-1 (구분선)
   - 버튼 간격: gap-2
   - 카드 여백: p-4 또는 p-5

✅ 입력 필드 규칙
   - 필수 필드: <span class="text-danger">*</span>
   - placeholder: 간단 명료하게
   - 안내 문구: <small class="form-text text-muted">
   - 실시간 검증: is-valid (초록), is-invalid (빨강)
```

**버튼 크기 통일 예시**:
```html
<!-- ✅ 로그인/회원가입 페이지 -->
<button type="submit" class="btn btn-primary" style="height: 42px;">
  로그인
</button>

<!-- ✅ 마이페이지/수정 페이지 -->
<button type="submit" class="btn btn-primary" style="min-width: 120px; height: 42px;">
  프로필 저장
</button>

<!-- ✅ 상세화면 수정/삭제 버튼 (붙여서 배치) -->
<div class="d-flex">
  <a class="btn btn-warning" style="min-width: 80px;">수정</a>
  <button class="btn btn-danger" style="min-width: 80px;">삭제</button>
</div>

<!-- ❌ 잘못된 예시: 크기 불규칙 -->
<button class="btn btn-primary btn-lg">저장</button>
<button class="btn btn-secondary">취소</button>
```

**alert 대신 모달 팝업 사용** ⭐NEW (2025-11-12):
```html
<!-- ❌ 잘못된 예시: alert 사용 -->
<script>
alert('정말로 삭제하시겠습니까?');
</script>

<!-- ✅ 올바른 예시: Bootstrap 모달 사용 -->
<div class="modal fade" id="confirmModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header bg-warning">
        <h5 class="modal-title">
          <i class="bi bi-exclamation-triangle"></i> 확인
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <div class="alert alert-warning">
          <i class="bi bi-exclamation-triangle-fill"></i>
          <strong>경고:</strong> 이 작업을 수행하시겠습니까?
        </div>
        <p>작업을 진행하면 되돌릴 수 없습니다.</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
          <i class="bi bi-x-circle"></i> No (취소)
        </button>
        <button type="button" class="btn btn-primary" onclick="confirmAction()">
          <i class="bi bi-check-circle"></i> Yes (확인)
        </button>
      </div>
    </div>
  </div>
</div>

<script>
function showConfirmModal() {
  const modal = new bootstrap.Modal(document.getElementById('confirmModal'));
  modal.show();
}
</script>
```

**사용 가능한 개수/제한 명시** ⭐NEW (2025-11-12):
```html
<!-- ✅ 올바른 예시: 멀티로그인 개수 명시 -->
<p class="small">
  활성화 시 동일 계정으로 <strong>최대 5개 기기</strong>에서 동시 로그인이 가능합니다.
</p>

<!-- ✅ 올바른 예시: 파일 크기 제한 명시 -->
<small class="form-text text-muted">
  <i class="bi bi-info-circle"></i> 
  최대 파일 크기: <strong>5MB</strong> | 허용 형식: JPG, PNG, PDF
</small>

<!-- ✅ 올바른 예시: 첨부 파일 개수 제한 -->
<label class="form-label">
  <i class="bi bi-paperclip"></i> 첨부 파일 <span class="badge bg-secondary">최대 3개</span>
</label>

<!-- ✅ 올바른 예시: 게시글 제목 글자 수 제한 -->
<input type="text" 
       maxlength="100" 
       placeholder="제목을 입력하세요 (최대 100자)">
<small class="form-text text-muted">
  <span id="charCount">0</span> / 100자
</small>

<!-- ❌ 잘못된 예시: 제한 명시 없음 -->
<p>멀티로그인이 가능합니다.</p>
<input type="file" multiple>
<input type="text" placeholder="제목을 입력하세요">
```

**멀티로그인 설정 예시**:
```html
<!-- 시스템 설정 페이지 -->
<h6>멀티로그인 설정</h6>
<p class="small">
  활성화 시 동일 계정으로 <strong class="text-primary">최대 5개 기기</strong>에서 
  동시 로그인이 가능합니다.
</p>
<ul class="small text-muted">
  <li>예: PC 2대 + 모바일 3대 = 총 5개 기기</li>
  <li>6번째 기기에서 로그인 시 가장 오래된 세션이 자동으로 종료됩니다.</li>
</ul>
```

**헤더 링크 통일 예시**:
```html
<!-- ✅ 올바른 예시: 모든 링크 크기/간격 통일 -->
<div class="d-flex align-items-center">
  <a href="/" class="px-2" style="font-size: 0.95rem;">HOME</a>
  <span class="px-1" style="font-size: 0.95rem;">|</span>
  <a href="/login" class="px-2" style="font-size: 0.95rem;">로그인</a>
  <span class="px-1" style="font-size: 0.95rem;">|</span>
  <a href="/register" class="px-2" style="font-size: 0.95rem;">회원가입</a>
</div>

<!-- ❌ 잘못된 예시: 크기/간격 불규칙 -->
<a href="/" class="p-1">HOME</a>
<a href="/login" class="p-2" style="font-size: 1.1rem;">로그인</a>
<a href="/register">회원가입</a>
```

**입력 필드 placeholder 규칙**:
```html
<!-- ✅ 올바른 예시: 간단명료 + 안내 문구 분리 -->
<input type="password" 
       placeholder="비밀번호를 입력하세요"
       style="font-size: 0.95rem;">
<small class="form-text text-muted">
  게시글 작성 시 설정한 비밀번호를 입력하세요.
</small>

<!-- ✅ 올바른 예시: 이메일 형식 안내 -->
<input type="email" 
       placeholder="예: abc123@example.com"
       pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}">
<small class="form-text text-muted">
  <i class="bi bi-info-circle"></i> 
  올바른 형식: abc123@example.com (영문, 숫자, @, 도메인)
</small>

<!-- ❌ 잘못된 예시: placeholder에 모든 내용 포함 -->
<input placeholder="게시글 작성 시 설정한 비밀번호를 입력하세요. 8자 이상 입력해야 합니다.">
```

**체크리스트: 새 페이지/기능 추가 시**:
- [ ] 버튼 크기 통일 확인 (42px, 120px×42px)
- [ ] 폰트 크기 통일 확인 (0.95rem, 1rem)
- [ ] 간격 통일 확인 (px-2, px-1, gap-2)
- [ ] placeholder 간소화 확인
- [ ] 안내 문구 <small> 태그 사용 확인
- [ ] 필수 필드 * 표시 확인
- [ ] 실시간 검증 피드백 확인 (is-valid, is-invalid)
- [ ] 아이콘 + 텍스트 함께 표시 확인
- [ ] 버튼 배치 확인 (오른쪽 끝)
  <button class="btn btn-warning">수정</button>
  <button class="btn btn-danger">삭제</button>
</div>
```

**색상 사용 규칙**:
- 🔵 **Primary (파란색)**: 주요 액션 (저장, 등록, 확인)
- 🟢 **Success (초록색)**: 성공 메시지, 운영자 표시
- 🟡 **Warning (노란색)**: 경고, 수정
- 🔴 **Danger (빨간색)**: 삭제, 오류
- ⚫ **Secondary (회색)**: 보조 액션 (취소, 목록)

**모달 사용 규칙**:
```html
<!-- ✅ 모달 제목에 아이콘 추가 -->
<div class="modal-header">
  <h5 class="modal-title">
    <i class="bi bi-pencil-square"></i> 댓글 작성
  </h5>
  <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
</div>

<!-- ✅ 모달 푸터에 버튼 순서: 취소 → 확인 -->
<div class="modal-footer">
  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
  <button type="submit" class="btn btn-primary">등록</button>
</div>
```

**폼 입력 필드 규칙**:
```html
<!-- ✅ 필수 필드는 * 표시 -->
<label for="title" class="form-label">
  제목 <span class="text-danger">*</span>
</label>

<!-- ✅ placeholder로 입력 예시 제공 -->
<input type="email" placeholder="example@email.com">

<!-- ✅ small 태그로 안내 메시지 제공 -->
<small class="form-text text-muted">
  이메일은 비밀번호 찾기 시 사용됩니다.
</small>

<!-- ✅ 입력 검증 후 시각적 피드백 -->
<input type="text" class="form-control is-valid">  <!-- 성공: 초록색 테두리 -->
<input type="text" class="form-control is-invalid">  <!-- 실패: 빨간색 테두리 -->
```

**Flash 메시지 규칙**:
```html
<!-- ✅ 성공 메시지 -->
<div th:if="${message}" class="alert alert-success alert-dismissible fade show">
  <i class="bi bi-check-circle-fill"></i> <span th:text="${message}"></span>
  <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>

<!-- ✅ 오류 메시지 -->
<div th:if="${error}" class="alert alert-danger alert-dismissible fade show">
  <i class="bi bi-exclamation-triangle-fill"></i> <span th:text="${error}"></span>
  <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
```

---

#### **10. 로그 및 감사(Audit) 규칙** ⭐NEW

**목적**: 모든 데이터 변경 이력을 추적하여 문제 발생 시 원인 파악 및 복구 지원

**핵심 원칙**:
1. ✅ **생성/수정/삭제 시 자동 기록**: `@CreationTimestamp`, `@UpdateTimestamp` 사용
2. ✅ **중요 액션 로그 기록**: 게시글/댓글 삭제, 파일 삭제, 권한 변경 등
3. ✅ **사용자 식별 정보 기록**: 누가(username), 언제(timestamp), 무엇을(action) 했는지

**Entity에 감사 필드 추가**:
```java
@Entity
@Table(name = "counsel_post")
public class CounselPost extends BaseEntity {
    // 생성 일시 (자동 설정, 수정 불가)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 수정 일시 (자동 갱신)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 삭제 일시 (Soft Delete)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // 작성자 (게시글 생성 시 기록)
    @Column(name = "author_name", nullable = false)
    private String authorName;
    
    // Soft Delete 플래그
    @Column(name = "del_flag", nullable = false)
    private boolean delFlag = false;
}
```

**Service 계층 로그 규칙**:
```java
@Service
@Transactional
public class CounselService {
    private static final Logger log = LoggerFactory.getLogger(CounselService.class);
    
    // ✅ 올바른 예시: 생성 시 로그 기록
    public Long createPost(CounselPostWriteDto dto, List<MultipartFile> files) {
        CounselPost post = new CounselPost();
        // ... 엔티티 설정
        CounselPost saved = counselPostRepository.save(post);
        
        log.info("Counsel post created: id={}, title={}, author={}, secret={}", 
                 saved.getId(), saved.getTitle(), saved.getAuthorName(), saved.isSecret());
        
        return saved.getId();
    }
    
    // ✅ 올바른 예시: 수정 시 로그 기록
    public boolean updatePost(Long postId, CounselPostWriteDto dto) {
        CounselPost post = findById(postId);
        String oldTitle = post.getTitle();
        
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        // updatedAt은 @UpdateTimestamp로 자동 갱신
        
        counselPostRepository.save(post);
        
        log.info("Counsel post updated: id={}, oldTitle={}, newTitle={}, updatedAt={}", 
                 postId, oldTitle, dto.getTitle(), post.getUpdatedAt());
        
        return true;
    }
    
    // ✅ 올바른 예시: 삭제 시 로그 기록
    public boolean deletePost(Long postId) {
        CounselPost post = findById(postId);
        String title = post.getTitle();
        
        counselPostRepository.delete(post);  // @SQLDelete 실행
        
        log.info("Counsel post soft-deleted: id={}, title={}, deletedAt={}", 
                 postId, title, LocalDateTime.now());
        
        return true;
    }
    
    // ✅ 올바른 예시: 오류 발생 시 로그 기록
    public void processAttachment(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            // ... 파일 처리
            log.info("Attachment processed: fileName={}, size={}", fileName, file.getSize());
        } catch (IOException e) {
            log.error("Attachment processing failed: fileName={}, error={}", 
                      file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }
    }
}
```

**로그 레벨 사용 지침**:
| 레벨 | 용도 | 예시 |
|------|------|------|
| **INFO** | 정상적인 비즈니스 액션 | 게시글 생성, 수정, 삭제 |
| **WARN** | 경고성 이벤트 | 비밀번호 검증 실패, 권한 없는 접근 시도 |
| **ERROR** | 오류 발생 | 파일 업로드 실패, DB 트랜잭션 실패 |
| **DEBUG** | 개발 중 디버깅 | QueryDSL 쿼리 생성, DTO 변환 과정 |

**로그 메시지 작성 규칙**:
```java
// ✅ 올바른 예시: 구조화된 로그
log.info("User login: username={}, ip={}, timestamp={}", 
         username, request.getRemoteAddr(), LocalDateTime.now());

// ✅ 올바른 예시: 삭제 이력 명확히 기록
log.info("File deleted: fileId={}, fileName={}, filePath={}, deletedBy={}", 
         fileId, fileName, filePath, username);

// ❌ 잘못된 예시: 불명확한 로그
log.info("삭제 완료");

// ❌ 잘못된 예시: 민감 정보 노출
log.info("User login: username={}, password={}", username, password);  // 절대 금지!
```

**스케줄러 작업 로그**:
```java
@Scheduled(cron = "0 0 0 * * ?")  // 매일 자정
public void cleanupDeletedFiles() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(14);
    
    log.info("File cleanup scheduler started: cutoffDate={}", cutoffDate);
    
    List<Attachment> deletedFiles = attachmentRepository
        .findByDelFlagTrueAndDeletedAtBefore(cutoffDate);
    
    int deletedCount = 0;
    for (Attachment file : deletedFiles) {
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
            attachmentRepository.delete(file);
            deletedCount++;
            
            log.info("File physically deleted: fileId={}, fileName={}, deletedAt={}", 
                     file.getId(), file.getOriginalFileName(), file.getDeletedAt());
        } catch (IOException e) {
            log.error("File deletion failed: fileId={}, fileName={}, error={}", 
                      file.getId(), file.getOriginalFileName(), e.getMessage());
        }
    }
    
    log.info("File cleanup scheduler completed: total={}, deleted={}", 
             deletedFiles.size(), deletedCount);
}
```

**감사(Audit) 테이블 (선택사항)**:
```java
// 중요한 액션은 별도 audit_log 테이블에 기록 가능
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;  // 누가
    
    @Column(nullable = false)
    private String action;  // 무엇을 (CREATE, UPDATE, DELETE)
    
    @Column(nullable = false)
    private String entityType;  // 어디서 (CounselPost, User, etc.)
    
    @Column(nullable = false)
    private Long entityId;  // 어떤 것을
    
    @Column(columnDefinition = "TEXT")
    private String details;  // 상세 정보 (JSON 형식)
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 언제
    
    @Column(length = 50)
    private String ipAddress;  // 어디서 (IP)
}
```

---

#### **11. Hibernate DDL 및 스키마 관리 규칙** ⭐NEW

**개발 환경 DDL 설정**:
```yaml
# application-dev.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ✅ 권장: 기존 데이터 유지, 스키마 자동 업데이트
      # ddl-auto: create-drop  # ❌ 금지: DROP 오류 및 데이터 손실
      # ddl-auto: create  # ⚠️ 주의: 매번 데이터 삭제
```

**ddl-auto 옵션 사용 지침**:

| 옵션 | 사용 시점 | 데이터 유지 | DROP 오류 | 권장 여부 |
|------|----------|------------|----------|----------|
| **update** | 개발 환경 | ✅ | ❌ | ✅ **권장** |
| create | 초기 개발 시작 | ❌ | ❌ | ⚠️ |
| create-drop | 절대 사용 금지 | ❌ | ✅ | ❌ **금지** |
| validate | 운영 환경 | ✅ | ❌ | ✅ |
| none | 운영 환경 (수동 관리) | ✅ | ❌ | ✅ |

**규칙**:
1. ✅ **개발 환경**: 항상 `ddl-auto: update` 사용
2. ❌ **create-drop 절대 금지**: 테이블 DROP 시 오류 발생 및 데이터 손실
3. ✅ **초기화 필요 시**: `drop-all-tables.sql` 스크립트 수동 실행
4. ✅ **새 Entity 추가 시**: `@Table(name = "테이블명")` 명시적으로 지정
5. ✅ **외래키 제약조건**: 개발 환경에서는 생성하지 않음 (성능 및 유연성)

**테이블 초기화 방법**:
```sql
-- MySQL 클라이언트에서 실행
SET FOREIGN_KEY_CHECKS = 0;
-- 테이블 삭제
DROP TABLE IF EXISTS 테이블명;
SET FOREIGN_KEY_CHECKS = 1;
```

또는:
```bash
# 전체 테이블 초기화
mysql -u dev33 -p petclinic < src/main/resources/db/mysql/drop-all-tables.sql
```

**Entity 작성 시 주의사항**:
```java
// ✅ 올바른 예시
@Entity
@Table(name = "users")  // 테이블명 명시
@SQLDelete(sql = "UPDATE users SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class User extends BaseEntity {
    // del_flag 필드 필수 (Soft Delete)
    @Column(name = "del_flag", nullable = false)
    private boolean delFlag = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

// ❌ 잘못된 예시
@Entity
public class User {  // @Table 누락 - Hibernate가 자동으로 테이블명 생성
    // del_flag 없음 - Soft Delete 불가
}
```

**DROP 오류 발생 시 대응**:
1. `ddl-auto: update`로 변경되어 있는지 확인
2. `drop-all-tables.sql` 스크립트로 수동 초기화
3. MySQL에서 직접 `DROP TABLE IF EXISTS` 실행
4. 외래키 제약조건 확인: `SHOW CREATE TABLE 테이블명;`

#### **10. 테스트 및 서버 실행 규칙** ⭐NEW

**서버 실행 방법**:

| 방법 | 명령어 | 권장 여부 | 사유 |
|------|--------|----------|------|
| **IDE 실행** | Run 버튼 | ✅ **권장** | 포트 관리 자동, 종료 쉬움 |
| Gradle bootRun | `./gradlew bootRun` | ❌ **금지** | 백그라운드 실행 시 포트 점유 문제 |

**규칙**:
1. ✅ **서버 실행**: 항상 IDE(IntelliJ IDEA)에서 실행
2. ❌ **터미널 bootRun 금지**: 포트가 살아있어 수동 종료 필요
3. ✅ **컴파일 확인**: `./gradlew compileJava` 사용 가능
4. ✅ **빌드 확인**: `./gradlew build -x test` 사용 가능
5. ❌ **터미널에서 bootRun 실행 금지**: 프로세스 관리 어려움

**IDE 실행 설정**:
```
1. IntelliJ IDEA에서 PetClinicApplication.java 열기
2. main 메서드 옆 실행 버튼 클릭
3. Edit Configurations...
   - Active profiles: dev
   - VM options: -Dspring.profiles.active=dev
4. Run 또는 Debug 모드 실행
```

**포트 충돌 해결**:
```bash
# 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :8080

# 프로세스 강제 종료 (PID 확인 후)
taskkill /F /PID [PID]

# 또는 Java 프로세스 전체 종료
taskkill /F /IM java.exe
```

**Gradle 명령어 사용 가이드**:
```bash
# ✅ 허용: 컴파일만
./gradlew compileJava

# ✅ 허용: 빌드 (테스트 제외)
./gradlew build -x test

# ✅ 허용: 의존성 확인
./gradlew dependencies

# ✅ 허용: Gradle Daemon 종료
./gradlew --stop

# ❌ 금지: 서버 실행
./gradlew bootRun  # 포트 점유 문제 발생
```

**서버 종료 방법**:
- **IDE**: Stop 버튼 클릭 (권장)
- **터미널 (비상시만)**: Ctrl+C 후 프로세스 확인

**테스트 코드 작성 규칙**:
- 별도 요청이 없으면 테스트 코드 작성하지 않음
- 빌드 시 테스트 스킵: `./gradlew build -x test`

### 6.2 문서 관리 규칙 ⭐NEW

#### **관리 대상 문서**

| 문서명 | 경로 | 용도 | 업데이트 주기 |
|--------|------|------|--------------|
| **PROJECT_DOCUMENTATION.md** | `docs/01-project-overview/` | 프로젝트 전체 문서 | 주요 기능 추가 시 |
| **CHANGELOG.md** | `docs/07-changelog/` | 변경 이력 관리 | 버전 배포 시 필수 |
| **QUICK_REFERENCE.md** | `docs/09-quick-reference/` | 빠른 참조 가이드 | API/URL 변경 시 |
| **TABLE_DEFINITION.md** | `docs/03-database/` | 테이블 정의서 | 테이블 구조 변경 시 필수 |
| **UI_SCREEN_DEFINITION.md** | `docs/05-ui-screens/` | UI 화면 정의서 | 화면 추가/수정 시 필수 |
| **README.md** | 루트 | 프로젝트 소개 | 주요 기능 추가 시 |
| **SECURITY_IMPLEMENTATION.md** | `docs/06-security/` | 보안 구현 문서 | 보안 기능 추가 시 |
| **DOCUMENTATION_MANAGEMENT_GUIDE.md** | `docs/` | 문서 관리 가이드 | 문서 규칙 변경 시 |

#### **문서 업데이트 규칙**

**1. 테이블 구조 변경 시** (필수):
```markdown
# TABLE_DEFINITION.md 업데이트
1. 해당 테이블의 컬럼 정보 수정
2. 변경 이력 섹션에 날짜와 변경 내용 기록
3. Entity 클래스 경로 확인 및 업데이트

# 예시
### [3.5.3] - 2025-11-07
#### 수정
- **users** 테이블: `profile_image` 컬럼 추가 (VARCHAR(500), 프로필 이미지 경로)

#### 영향 범위
- Entity: `User.java` 필드 추가
- Service: `UserService.java` 프로필 업데이트 메서드 추가
```

**2. 기능 추가 시** (필수):
```markdown
# CHANGELOG.md 업데이트
## [버전] - 날짜
### 추가된 기능
- 기능명: 상세 설명
- 관련 파일: 파일 목록
- 영향 범위: 변경된 부분

# PROJECT_DOCUMENTATION.md 업데이트
- 섹션 7. 주요 기능 명세에 새 기능 추가
- API 엔드포인트 테이블 업데이트

# QUICK_REFERENCE.md 업데이트
- 주요 URL 테이블 업데이트
- 코드 예시 추가 (필요 시)
```

**3. API/URL 변경 시** (필수):
```markdown
# QUICK_REFERENCE.md 업데이트
- 주요 URL 테이블에서 변경된 엔드포인트 수정
- 삭제된 API는 취소선(~~strikethrough~~) 처리 후 "Deprecated" 표시

# PROJECT_DOCUMENTATION.md 업데이트
- 섹션 5. API 요청 흐름 다이어그램 수정
**4. UI 화면 추가/수정 시** (필수) ⭐NEW:
```markdown
# UI_SCREEN_DEFINITION.md 업데이트
- 화면별 레이아웃 다이어그램 작성 (ASCII Art)
- 입력 필드 테이블 작성
  | 필드명 | 타입 | 필수 | 검증 | 설명 |
  |--------|------|------|------|------|
- 화면 동작 명세 작성
- 변경 이력 섹션에 날짜와 내용 기록

# CHANGELOG.md 업데이트
- UI 카테고리에 변경 내용 기록

# 예시
### [3.5.3] - 2025-11-06
#### UI 화면 추가
- **user/login.html**: 로그인 화면
  - Remember-Me 기능
  - Flash 메시지 표시
  - 입력 필드: username, password, remember-me
```

**5. 보안 기능 추가 시** (필수):

**4. 보안 기능 추가 시** (필수):
```markdown
# SECURITY_IMPLEMENTATION.md 업데이트
- 새로운 보안 기능 섹션 추가
- 설정 방법 및 코드 예시 포함

# CHANGELOG.md 업데이트
- 보안 카테고리에 변경 내용 기록
```

**5. 의존성 추가/변경 시** (필수):
```markdown
# CHANGELOG.md 업데이트
### 의존성 변경
- 라이브러리명: 버전 변경 (구버전 → 신버전)
- 변경 사유: CVE 보안 패치 / 기능 추가 등

# PROJECT_DOCUMENTATION.md 업데이트
- 섹션 1.1 기술 스택 버전 업데이트
```

#### **문서 작성 시 준수 사항**

**공통 규칙**:
```markdown
1. ✅ 날짜는 ISO 8601 형식 사용: YYYY-MM-DD
2. ✅ 버전은 Semantic Versioning 사용: X.Y.Z
3. ✅ 코드 블록은 언어 지정: ```java, ```sql, ```bash
4. ✅ 테이블은 Markdown 표 형식 사용
5. ✅ 이모지 사용으로 가독성 향상: ✅ ❌ ⭐ 🔴 🟡 🟢
```

**CHANGELOG.md 작성 규칙**:
```markdown
## [버전] - 날짜

### 🎉 추가된 기능 (Added)
- 새로운 기능 설명

### 🔧 수정된 기능 (Changed)
- 기존 기능 변경 사항

### 🐛 수정된 버그 (Fixed)
- 버그 수정 내역

### 🗑️ 삭제된 기능 (Removed)
- 제거된 기능 (Deprecated 처리 후 삭제)

### 🔒 보안 업데이트 (Security)
- 보안 취약점 패치

### 📊 의존성 변경 (Dependencies)
- 라이브러리 버전 변경
```

**TABLE_DEFINITION.md 작성 규칙**:
```markdown
1. ✅ 모든 컬럼에 한글명 필수 기입
2. ✅ NULL 허용 여부 명확히 표시
3. ✅ 기본값이 있으면 반드시 기재
4. ✅ 외래키 관계는 REFERENCES로 명시
5. ✅ Entity 클래스 경로 풀패스로 기재
6. ✅ 변경 이력에 영향 범위 상세 기록
```

#### **문서 검토 프로세스**

**월간 검토** (매월 1일):
```markdown
1. CHANGELOG.md 누락 항목 확인
2. TABLE_DEFINITION.md와 실제 DB 스키마 일치 여부 확인
3. API 문서와 실제 엔드포인트 일치 여부 확인
4. 버전 번호 일관성 검토
```

**배포 전 필수 검토**:
```markdown
1. ✅ CHANGELOG.md에 모든 변경사항 기록되었는지 확인
2. ✅ 버전 번호가 모든 문서에서 일치하는지 확인
3. ✅ 새로운 API가 QUICK_REFERENCE.md에 추가되었는지 확인
4. ✅ 테이블 변경사항이 TABLE_DEFINITION.md에 반영되었는지 확인
```

#### **문서 자동화 도구 (추후 도입 예정)**

```bash
# 테이블 정의서 자동 생성 (계획)
./gradlew generateTableDoc

# API 문서 자동 생성 (계획)
./gradlew generateApiDoc

# CHANGELOG 항목 추출 (계획)
./gradlew extractChangelog --version=3.5.3
```

### 6.2 트랜잭션 관리

```java
@Service
@Transactional // 클래스 레벨에 선언
public class CounselService {
    
    // 조회 메서드는 readOnly 권장
    @Transactional(readOnly = true)
    public PageResponse<CounselPostDto> getPagedPosts(Pageable pageable) {
        // ...
    }
    
    // 수정/삭제는 기본 Transactional
    public Long saveNew(CounselPostWriteDto dto) {
        // 파일 저장 → DB 저장이 하나의 트랜잭션
        // 실패 시 자동 롤백
    }
}
```

### 6.3 JavaDoc 및 주석 규칙

```java
/**
 * 온라인상담 게시글 저장
 * 
 * @param dto 작성 폼 DTO (제목, 내용, 첨부파일 포함)
 * @return 생성된 게시글 ID
 * @throws IOException 파일 저장 실패 시
 */
public Long saveNew(CounselPostWriteDto dto) throws IOException {
    // 1. 본문 파일 저장
    String path = contentStorage.saveHtml(dto.getContent());
    
    // 2. Entity 생성
    CounselPost entity = new CounselPost();
    // ...
}
```

### 6.4 Author 정보 규칙

```java
/*
 * Project : spring-petclinic
 * File    : CounselService.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 온라인상담 게시판 비즈니스 로직 집약
 *   연관 기능: 댓글 CRUD, 파일 업로드/다운로드
 *   미구현: 관리자 권한 제어, 대댓글 트리 구조
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
```

---

## 7. 주요 기능 명세

### 7.1 온라인상담 게시판

#### **기능 목록**

| 기능 | URL | HTTP Method | 권한 | 상태 |
|------|-----|-------------|------|------|
| 목록 조회 | `/counsel/list` | GET | 공개 | ✅ |
| 검색 | `/counsel/list?type=title&keyword=수술` | GET | 공개 | ✅ |
| 상세 조회 | `/counsel/detail/{id}` | GET | 공개/비공개 | ✅ |
| 비밀번호 입력 | `/counsel/detail/{id}/password` | GET | 공개 | ✅ |
| 비밀번호 검증 | `/counsel/detail/{id}/unlock` | POST | 공개 | ✅ |
| 글쓰기 폼 | `/counsel/write` | GET | 공개 | ✅ |
| 글 등록 | `/counsel` | POST | 공개 | ✅ |
| **글 수정 폼** | `/counsel/edit/{id}` | GET | 비밀번호 검증 | ✅ **NEW** |
| **글 수정 처리** | `/counsel/edit/{id}` | POST | 비밀번호 검증 | ✅ **NEW** |
| **글 삭제 (Soft Delete)** | `/counsel/delete/{id}` | POST | 비밀번호 검증 | ✅ **NEW** |
| 댓글 등록 | `/counsel/detail/{postId}/comments` | POST | 공개 | ✅ |
| 댓글 삭제 | `/counsel/detail/{postId}/comments/{commentId}/delete` | POST | 비밀번호 검증 | ✅ |
| **파일 다운로드** | `/counsel/download/{fileId}` | GET | 공개 | ✅ **NEW** |

#### **공개/비공개 기준**

| 항목 | 공개 (`secret=false`) | 비공개 (`secret=true`) |
|------|----------------------|----------------------|
| **비밀번호** | `passwordHash = null` | BCrypt 해시 저장 |
| **목록 표시** | "공개" 배지 | "비공개" 배지 |
| **상세 접근** | 누구나 조회 가능 | 비밀번호 입력 필요 |
| **세션 관리** | 불필요 | `counselUnlocked` Set에 ID 저장 |

#### **상담 상태 (CounselStatus)**

```java
public enum CounselStatus {
    WAIT("답변대기"),      // 댓글 없음
    COMPLETE("답변완료"),   // 댓글 1개 보장
    END("상담종료");        // 댓글 랜덤 (있을 수도, 없을 수도)
    
    private final String displayName;
}
```

**데이터 초기화 규칙**:
- 총 112개 게시글 생성 (페이지당 10개 기준)
- WAIT/COMPLETE/END 완전 랜덤 분배 (각 약 1/3)
- 공개/비공개 50% 확률
- COMPLETE 상태는 운영자 댓글 1개 보장

#### **파일 저장 구조**

```
data/
├── counsel/
│   ├── contents/              # 본문 HTML 파일
│   │   ├── 2025/
│   │   │   ├── 06/
│   │   │   │   ├── {UUID}.html
│   │   │   │   └── ...
│   │   │   ├── 07/
│   │   │   └── ...
│   │   └── ...
│   └── uploads/               # 첨부파일
│       ├── 2025/
│       │   ├── 06/
│       │   │   ├── {UUID}.jpg
│       │   │   ├── {UUID}.png
│       │   │   └── ...
│       │   └── ...
│       └── ...
```

#### **파일 검증 규칙**

| 항목 | 규칙 |
|------|------|
| **허용 MIME 타입** | `image/jpeg`, `image/png`, `image/gif` |
| **최대 파일 크기** | 5MB |
| **파일명 난수화** | UUID 사용 |
| **경로 역참조 방지** | `Path.normalize()` 검증 |
| **Soft Delete** | `del_flag=true` 2주 후 물리 삭제 |

#### **Soft Delete 정책**

**구현 방식**:
```java
@Entity
@SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CounselPost extends BaseEntity {
    @Column(name = "del_flag", nullable = false)
    private boolean delFlag = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

**동작 흐름**:
1. 사용자가 게시글 삭제 요청 (`POST /counsel/delete/{id}`)
2. 비밀번호 검증 (비공개 글인 경우)
3. `repository.delete(entity)` 호출 → `@SQLDelete` SQL 실행
4. 물리적 DELETE 대신 `UPDATE counsel_post SET del_flag=1, deleted_at=NOW()`
5. `@SQLRestriction("del_flag = 0")`으로 조회 시 자동 필터링
6. 로그 기록: `log.info("Successfully soft-deleted post with ID: {} (title: {})", id, title)`
7. 2주 후 `FileCleanupScheduler`가 물리적 DELETE 수행

**장점**:
- ✅ 데이터 복구 가능
- ✅ 삭제 이력 추적
- ✅ 연관 데이터 보호 (외래키 제약조건 유지)
- ✅ 스케줄러를 통한 자동 정리

#### **조회수 중복 방지 (세션 기반)**

**구현 방식**:
```java
// Controller
@GetMapping("/detail/{id}")
public String detail(@PathVariable Long id, HttpSession session) {
    Set<Long> viewedPosts = (Set<Long>) session.getAttribute("viewedCounselPosts");
    if (viewedPosts == null) {
        viewedPosts = new HashSet<>();
    }
    
    // 처음 조회하는 게시글이면 조회수 증가
    if (!viewedPosts.contains(id)) {
        counselService.incrementViewCount(id);
        viewedPosts.add(id);
        session.setAttribute("viewedCounselPosts", viewedPosts);
    }
    // ...
}

// Service
public void incrementViewCount(Long postId) {
    try {
        CounselPost entity = repository.findById(postId).orElse(null);
        if (entity != null) {
            entity.setViewCount(entity.getViewCount() + 1);
            repository.save(entity);
        }
    } catch (Exception e) {
        log.error("Error incrementing view count: {}", e.getMessage());
        // 조회수 증가 실패는 치명적이지 않으므로 예외를 던지지 않음
    }
}
```

**동작 방식**:
1. 세션에 조회한 게시글 ID Set 저장
2. 같은 세션에서 재방문 시 조회수 증가하지 않음
3. 브라우저 종료 시 세션 초기화되어 재집계
4. 예외 발생 시에도 서비스 중단 없음

**장점**:
- ✅ 중복 조회 방지
- ✅ 세션 단위 이력 관리
- ✅ 안정성 확보 (예외 처리)

### 7.2 커뮤니티 게시판

#### **기능 목록**

| 기능 | URL | HTTP Method | 상태 |
|------|-----|-------------|------|
| 공지사항 목록 | `/community/list?subject=notice` | GET | ✅ |
| 공지사항 상세 | `/community/detail/{id}?subject=notice` | GET | ✅ |
| 검색 | `/community/list?subject=notice&type=title&keyword=이벤트` | GET | ✅ |

**초기 데이터**:
- 공지사항 3개 + 더미 103개 = 총 106개

---

## 8. 설정 파일

### 8.1 application.yml (기본 설정)

```yaml
spring:
  application:
    name: petclinic
  
  profiles:
    active: dev  # 개발 프로파일 활성화
  
  jpa:
    open-in-view: false
    show-sql: false
  
  sql:
    init:
      mode: never
  
  thymeleaf:
    mode: HTML
  
  messages:
    basename: messages/messages
    encoding: UTF-8
    cache-duration: 5m
```

### 8.2 application-dev.yml (개발 환경)

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/petclinic?sessionVariables=FOREIGN_KEY_CHECKS=0
    username: dev33
    password: ezflow_010
  
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
    hibernate:
      ddl-auto: create-drop  # 시작 시 DROP→CREATE, 종료 시 DROP
    show-sql: true
  
logging:
  level:
    root: DEBUG
```

**주요 설정 설명**:
- `sessionVariables=FOREIGN_KEY_CHECKS=0`: 외래키 제약조건 비활성화 (개발 환경 전용)
- `ddl-auto: create-drop`: 서버 시작 시 테이블 재생성, 종료 시 삭제
- `show-sql: true`: SQL 쿼리 로그 출력

### 8.3 build.gradle (의존성)

```gradle
dependencies {
    // Spring Boot Core
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'
    
    // QueryDSL 5.0.0
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    implementation 'com.querydsl:querydsl-core'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    
    // 보안/검증
    implementation 'org.jsoup:jsoup:1.18.1'  // HTML Sanitize
    implementation 'org.apache.tika:tika-core:2.9.2'  // MIME 타입 검증
    implementation 'org.springframework.security:spring-security-crypto:6.3.4'  // BCrypt
    
    // 파일 업로드
    implementation 'commons-io:commons-io:2.16.1'
}
```

---

## 9. 문제 해결 및 개선 이력

### 9.1 외래키 DROP 에러 해결

**문제**: 서버 종료 시 `Cannot drop table 'counsel_post' referenced by a foreign key` 에러

**해결**:
1. MySQL URL에 `sessionVariables=FOREIGN_KEY_CHECKS=0` 추가
2. Entity에서 `@JoinColumn`의 `foreignKey` 옵션 제거
3. 개발 환경에서만 외래키 제약조건 비활성화

### 9.2 Entity 이름 충돌 해결

**문제**: `common.table.Attachment`와 `counsel.model.Attachment` 충돌

**해결**:
```java
@Entity(name = "CounselAttachment")
@Table(name = "counsel_attachments")
public class Attachment {
    // counsel 전용 첨부파일
}
```

### 9.3 데이터 초기화 개선

**변경 전**: COMPLETE 상태 72개 고정 → 나머지 WAIT/END

**변경 후**: WAIT/COMPLETE/END 완전 랜덤 분배 (1/3 확률)

```java
private CounselStatus randomStatus() {
    CounselStatus[] values = CounselStatus.values();
    return values[ThreadLocalRandom.current().nextInt(values.length)];
}
```

---

## 10. 향후 개발 계획

### 10.1 미구현 기능

| 기능 | 우선순위 | 예상 개발 기간 |
|------|---------|---------------|
| **로그인/회원가입** | 🔴 높음 | 2주 |
| **관리자 권한 관리** | 🔴 높음 | 1주 |
| **파일 다운로드** | 🟡 중간 | 3일 |
| **대댓글 트리 구조** | 🟡 중간 | 1주 |
| **게시글 수정/삭제** | 🟡 중간 | 3일 |
| **조회수 중복 방지** | 🟢 낮음 | 2일 |
| **좋아요 기능** | 🟢 낮음 | 3일 |

### 10.2 성능 최적화 계획

1. **N+1 문제 해결**
   - `@EntityGraph` 또는 `fetch join` 사용
   - 댓글 조회 시 게시글 정보 함께 로드

2. **Redis 캐싱 도입**
   - 조회수가 높은 게시글 캐싱
   - 세션 관리를 Redis로 이전

3. **DB 인덱스 최적화**
   - 검색 쿼리 분석 후 추가 인덱스 생성

4. **파일 서빙 최적화**
   - CDN 도입 검토
   - 이미지 썸네일 자동 생성

---

## 부록

### A. QueryDSL Q클래스 생성

```bash
# Gradle 빌드 시 자동 생성
./gradlew clean compileJava

# 생성 경로
src/main/generated/org/springframework/samples/petclinic/
├── common/entity/
│   ├── QBaseEntity.java
│   └── QNamedEntity.java
├── counsel/table/
│   ├── QCounselPost.java
│   ├── QCounselComment.java
│   └── ...
```

### B. Thymeleaf 레이아웃 구조

```
templates/
├── fragments/
│   ├── layout.html           # 공통 레이아웃
│   ├── pagination.html       # 페이지네이션 컴포넌트
│   ├── inputField.html       # 입력 필드 컴포넌트
│   └── selectField.html      # 선택 필드 컴포넌트
├── counsel/
│   ├── counselList.html      # 목록 페이지
│   ├── counselDetail.html    # 상세 페이지
│   ├── counsel-write.html    # 작성 페이지
│   └── counsel-password.html # 비밀번호 입력 페이지
├── community/
│   ├── noticeList.html       # 공지사항 목록
│   └── noticeDetail.html     # 공지사항 상세
└── welcome.html              # 홈 페이지
```

### C. 개발 환경 설정

**필수 프로그램**:
- JDK 17
- MySQL 8.0
- Gradle 8.14.3
- IntelliJ IDEA (권장)

**데이터베이스 설정**:
```sql
CREATE DATABASE petclinic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dev33'@'localhost' IDENTIFIED BY 'ezflow_010';
GRANT ALL PRIVILEGES ON petclinic.* TO 'dev33'@'localhost';
FLUSH PRIVILEGES;
```

**서버 실행**:
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

**접속 URL**:
- 홈: `http://localhost:8080`
- 온라인상담: `http://localhost:8080/counsel/list`
- 커뮤니티: `http://localhost:8080/community/list?subject=notice`

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-05  
**작성자**: Jeongmin Lee

