# Spring PetClinic 프로젝트 구조 요약

**분석 날짜:** 2025-11-20

## 1️⃣ 패키지 구조

```
org.springframework.samples.petclinic
├── common/          # 공통 설정, DTO, 유틸리티
│   ├── config/     # Spring 설정 (Security, QueryDSL 등)
│   ├── dto/        # 공통 DTO (PageResponse 등)
│   └── table/      # 공통 엔티티 (Attachment 등)
├── community/      # 커뮤니티 게시판
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── table/      # Entity
│   └── dto/
├── counsel/        # 온라인 상담 게시판 ⭐ 주요 모듈
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── table/
│   ├── dto/
│   └── init/       # 초기 데이터
├── faq/            # 자주 묻는 질문
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── table/
│   └── init/
├── user/           # 사용자 관리
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── table/
│   ├── dto/
│   └── init/
├── security/       # Spring Security 설정
│   └── handler/
└── system/         # 시스템 설정
    ├── controller/
    ├── service/
    ├── repository/
    ├── table/
    └── init/
```

## 2️⃣ 주요 도메인/모듈 역할

### Counsel (온라인 상담) - 핵심 모듈
- **CounselPost**: 상담 게시글
- **CounselComment**: 게시글 댓글/답변
- **CounselPostAttachment**: 게시글 첨부파일
- **CounselCommentAttachment**: 댓글 첨부파일
- **특징**: 비공개/공개, 비밀번호 보호, 상태 관리(대기/완료/종료)

### Community (커뮤니티)
- **CommunityPost**: 일반 게시글
- **CommunityPostAttachment**: 첨부파일

### FAQ (자주 묻는 질문)
- **FaqPost**: FAQ 게시글
- **카테고리**: 일반, 진료, 예약, 수술, 기타

### User (사용자)
- **Users**: 사용자 정보
- **UserRole**: 사용자 권한 (ROLE_USER, ROLE_ADMIN)
- **Spring Security 연동**

### System (시스템 설정)
- **SystemConfig**: 시스템 설정 (멀티로그인 여부 등)

### Common (공통)
- **Attachment**: 공통 첨부파일 엔티티
- **PageResponse**: 페이징 응답 DTO

## 3️⃣ 데이터 흐름

```
[요청]
Client → Controller
         ↓
      DTO 수신
         ↓
      Service (비즈니스 로직)
         ↓
      Repository (DB 접근)
         ↓
      Entity (JPA)
         ↓
      Database

[응답]
Database → Entity
           ↓
        Repository
           ↓
        Service
           ↓
        Mapper (Entity → DTO 변환)
           ↓
        Controller
           ↓
        Client (JSON 또는 Thymeleaf)
```

### 주요 규칙
- ❌ Entity를 뷰/API에 직접 노출 금지
- ✅ DTO + Mapper 패턴 필수
- ✅ QueryDSL은 RepositoryImpl에서만 사용
- ✅ Soft Delete 적용 (@SQLDelete, @SQLRestriction)

## 4️⃣ 템플릿 렌더링 구조

```
templates/
├── fragments/
│   ├── layout.html         # 공통 레이아웃 (header, footer, pagination)
│   └── pagination.html     # 페이징 컴포넌트
├── counsel/
│   ├── list.html          # 상담 목록
│   ├── detail.html        # 상담 상세
│   ├── write.html         # 상담 작성
│   └── password.html      # 비밀번호 확인
├── faq/
│   └── list.html          # FAQ 목록
├── user/
│   ├── login.html         # 로그인
│   ├── register.html      # 회원가입
│   └── mypage.html        # 마이페이지
├── system/
│   └── settings.html      # 시스템 설정
└── welcome.html           # 메인 페이지
```

### Thymeleaf + Bootstrap 구조
- **Layout 패턴**: `th:replace="~{fragments/layout :: main}"`
- **Fragment 재사용**: header, footer, pagination 공통화
- **Bootstrap 5.3.6** 사용
- **Font Awesome 4.7.0** 아이콘
- **일관된 UI/UX**: 버튼 크기, 간격, 반응형 통일

## 5️⃣ 데이터베이스

### 핵심 테이블
```sql
-- 상담
counsel_post
counsel_comment
counsel_post_attachment
counsel_comment_attachment

-- 커뮤니티
community_post
community_post_attachment

-- FAQ
faq_posts

-- 사용자
users
user_roles
persistent_logins (자동 로그인)

-- 시스템
system_config

-- 공통
attachment
```

### JPA 설정
- **ddl-auto: create** (개발 환경)
- **Physical Naming Strategy**: 테이블명 그대로 사용
- **Dialect**: MySQL8Dialect
- **Soft Delete**: 모든 엔티티에 `del_flag` 적용

## 6️⃣ 보안

### Spring Security
- **로그인/로그아웃**: Form 기반 인증
- **권한 관리**: ROLE_USER, ROLE_ADMIN
- **Remember-Me**: persistent_logins 테이블 사용
- **비밀번호 암호화**: BCryptPasswordEncoder

### 접근 제어
- 공개 게시글: 모두 열람 가능
- 비공개 게시글: 작성자만 열람 (비밀번호 입력)
- 관리자: 모든 게시글 열람 가능 (비밀번호 불필요)

## 7️⃣ 주요 기능

### 온라인 상담 (Counsel)
- ✅ 공개/비공개 설정
- ✅ 비밀번호 보호
- ✅ 상태 관리 (답변대기/답변완료/상담종료)
- ✅ 첨부파일 업로드
- ✅ 댓글/대댓글 (트리 구조)
- ✅ 관리자 답변

### FAQ
- ✅ 카테고리별 필터링
- ✅ 검색 (제목, 제목+내용)
- ✅ 페이징 (10/20/30/40/50개씩)
- ✅ 관리자만 등록/수정/삭제

### 사용자
- ✅ 회원가입 (닉네임, 이메일, 전화번호)
- ✅ 로그인 (아이디 저장, 자동 로그인)
- ✅ 마이페이지 (프로필 수정)

### 시스템 설정
- ✅ 멀티로그인 허용 여부
- ✅ 관리자 전용 설정 페이지

## 8️⃣ 빌드 및 실행

### Gradle
```bash
.\gradlew.bat compileJava   # 컴파일
.\gradlew.bat bootRun       # 실행 (IDE에서)
```

### 환경
- **Java 17**
- **Spring Boot 3.5.0**
- **MySQL 8.0**
- **Gradle 8.x**

## 9️⃣ 개발 규칙

### 코딩 스타일
1. DTO ↔ Entity 변환은 Mapper 사용
2. LocalDateTime 사용 (Date 금지)
3. Soft Delete 필수
4. SQL Injection 방지 (QueryDSL 사용)
5. 로그 남기기 (수정/삭제 시)

### UI/UX
1. 사용자 직관적 디자인
2. 버튼 크기/간격 통일
3. 반응형 레이아웃
4. 아이콘 + 텍스트 병행
5. 일관된 색상/폰트

### 보안
1. Entity 직접 노출 금지
2. 비밀번호 암호화
3. XSS 방지 (Thymeleaf 자동 이스케이프)
4. CSRF 토큰 사용

## 🎯 결론

**핵심 원칙: KISS (Keep It Simple, Stupid)**
- 단순하고 명확한 구조
- Spring Boot 기본 기능 최대 활용
- 불필요한 복잡성 제거
- 일관된 패턴 유지

