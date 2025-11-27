# 📊 테이블 정의서 (Table Definition Document)

**프로젝트**: Spring PetClinic  
**버전**: 3.5.3  
**최종 수정일**: 2025-11-27  
**작성자**: Jeongmin Lee

---

## 📑 목차

1. [사용자 관리 테이블](#1-사용자-관리-테이블)
2. [시스템 설정 테이블](#2-시스템-설정-테이블)
3. [온라인상담 테이블](#3-온라인상담-테이블)
4. [커뮤니티 테이블](#4-커뮤니티-테이블)
5. [포토게시판 테이블](#5-포토게시판-테이블)
6. [공통 테이블](#6-공통-테이블)
7. [변경 이력](#7-변경-이력)

---

## 1. 사용자 관리 테이블

### 1.1 users (사용자 정보)

**테이블 설명**: 회원 정보 및 로그인 계정 관리

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 사용자 고유 ID |
| username | 사용자 ID | VARCHAR(50) | NO | - | UNIQUE | 로그인용 사용자 ID |
| password | 비밀번호 | VARCHAR(100) | NO | - | - | BCrypt 해시된 비밀번호 |
| email | 이메일 | VARCHAR(100) | NO | - | - | 이메일 주소 |
| name | 이름 | VARCHAR(50) | NO | - | - | 실명 |
| phone | 전화번호 | VARCHAR(20) | YES | NULL | - | 연락처 |
| enabled | 계정 활성화 여부 | BOOLEAN | NO | TRUE | - | 계정 사용 가능 여부 |
| account_non_expired | 계정 만료 여부 | BOOLEAN | NO | TRUE | - | 계정 만료 안됨 여부 |
| account_non_locked | 계정 잠금 여부 | BOOLEAN | NO | TRUE | - | 계정 잠금 안됨 여부 |
| credentials_non_expired | 비밀번호 만료 여부 | BOOLEAN | NO | TRUE | - | 비밀번호 만료 안됨 여부 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 계정 생성 시간 |
| updated_at | 수정 일시 | DATETIME | NO | NOW() | - | 정보 수정 시간 (자동 업데이트) |
| last_login_at | 마지막 로그인 일시 | DATETIME | YES | NULL | - | 최근 로그인 시간 |
| last_login_ip | 마지막 로그인 IP | VARCHAR(50) | YES | NULL | - | 최근 로그인 IP 주소 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `username`
- INDEX: `email`

**Entity 클래스**: `org.springframework.samples.petclinic.user.table.User`

---

### 1.2 user_roles (사용자 권한)

**테이블 설명**: 사용자별 권한 정보 (다중 권한 지원)

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| user_id | 사용자 ID | BIGINT | NO | - | PK, FK | users.id 참조 |
| role | 권한 | VARCHAR(50) | NO | - | PK | 권한명 (ROLE_USER, ROLE_ADMIN) |

**인덱스**:
- PRIMARY KEY: `user_id, role`
- FOREIGN KEY: `user_id` REFERENCES `users(id)` ON DELETE CASCADE

**권한 종류**:
- `ROLE_USER`: 일반 사용자
- `ROLE_ADMIN`: 관리자

**Entity 클래스**: `User` 클래스 내 `@ElementCollection`으로 관리

---

### 1.3 persistent_logins (Remember-Me 토큰)

**테이블 설명**: Spring Security Remember-Me 기능을 위한 토큰 저장

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| username | 사용자 ID | VARCHAR(64) | NO | - | - | 로그인 ID |
| series | 시리즈 | VARCHAR(64) | NO | - | PK | 토큰 시리즈 (고유값) |
| token | 토큰 | VARCHAR(64) | NO | - | - | 인증 토큰 |
| last_used | 마지막 사용 일시 | TIMESTAMP | NO | NOW() | - | 토큰 최근 사용 시간 |

**인덱스**:
- PRIMARY KEY: `series`

**관리 방식**: Spring Security가 자동 관리

---

## 2. 시스템 설정 테이블

### 2.1 system_config (시스템 설정)

**테이블 설명**: 시스템 전역 설정 관리 (멀티로그인 허용 여부 등)

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 설정 고유 ID |
| property_key | 설정 키 | VARCHAR(100) | NO | - | UNIQUE | 설정 이름 (예: multiLoginEnabled) |
| property_value | 설정 값 | VARCHAR(500) | NO | - | - | 설정 값 (예: true, false) |
| description | 설명 | VARCHAR(1000) | YES | NULL | - | 설정에 대한 상세 설명 |
| is_active | 활성화 여부 | BOOLEAN | NO | TRUE | - | 설정 사용 여부 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 설정 생성 시간 |
| updated_at | 수정 일시 | DATETIME | NO | NOW() | - | 설정 수정 시간 (자동 업데이트) |
| updated_by | 수정자 | VARCHAR(100) | YES | NULL | - | 수정한 관리자 ID |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `property_key`

**주요 설정 키**:
- `multiLoginEnabled`: 멀티로그인 허용 여부 (true/false)

**Entity 클래스**: `org.springframework.samples.petclinic.system.table.SystemConfig`

---

## 3. 온라인상담 테이블

### 3.1 counsel_post (온라인상담 게시글)

**테이블 설명**: 온라인 상담 게시글 정보

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 게시글 고유 ID |
| title | 제목 | VARCHAR(255) | NO | - | - | 게시글 제목 |
| content | 내용 | MEDIUMTEXT | NO | - | - | 게시글 내용 (저장 표시용) |
| content_path | 본문 파일 경로 | VARCHAR(500) | YES | NULL | - | 실제 본문 HTML 파일 경로 |
| author_name | 작성자 이름 | VARCHAR(100) | NO | - | - | 작성자 이름 |
| author_email | 작성자 이메일 | VARCHAR(120) | YES | NULL | - | 작성자 이메일 |
| password_hash | 비밀번호 해시 | VARCHAR(100) | YES | NULL | - | BCrypt 해시 (비공개 글) |
| is_secret | 비공개 여부 | BOOLEAN | NO | FALSE | - | true: 비공개, false: 공개 |
| status | 상태 | VARCHAR(20) | NO | WAIT | - | WAIT/COMPLETE/END |
| view_count | 조회수 | INT | NO | 0 | - | 조회수 |
| comment_count | 댓글 수 | INT | NO | 0 | - | 댓글 개수 |
| attach_flag | 첨부파일 여부 | BOOLEAN | NO | FALSE | - | 첨부파일 존재 여부 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 게시글 작성 시간 |
| updated_at | 수정 일시 | DATETIME | NO | NOW() | - | 게시글 수정 시간 |
| del_flag | 삭제 플래그 | BOOLEAN | NO | FALSE | - | Soft Delete 플래그 |
| deleted_at | 삭제 일시 | DATETIME | YES | NULL | - | 삭제 시간 |
| deleted_by | 삭제자 | VARCHAR(100) | YES | NULL | - | 삭제한 사용자 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `status`, `is_secret`, `created_at`

**Soft Delete**: `@SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")`

**Entity 클래스**: `org.springframework.samples.petclinic.counsel.table.CounselPost`

---

### 3.2 counsel_comment (온라인상담 댓글)

**테이블 설명**: 온라인 상담 게시글의 댓글 및 대댓글

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 댓글 고유 ID |
| post_id | 게시글 ID | BIGINT | NO | - | FK | counsel_post.id 참조 |
| parent_id | 부모 댓글 ID | BIGINT | YES | NULL | FK | 대댓글인 경우 부모 댓글 ID |
| content | 내용 | TEXT | NO | - | - | 댓글 내용 |
| author_name | 작성자 이름 | VARCHAR(100) | NO | - | - | 댓글 작성자 이름 |
| author_email | 작성자 이메일 | VARCHAR(120) | YES | NULL | - | 작성자 이메일 |
| password_hash | 비밀번호 해시 | VARCHAR(100) | YES | NULL | - | BCrypt 해시 (삭제용) |
| is_staff_reply | 운영자 답변 여부 | BOOLEAN | NO | FALSE | - | true: 운영자, false: 일반 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 댓글 작성 시간 |
| updated_at | 수정 일시 | DATETIME | NO | NOW() | - | 댓글 수정 시간 |
| del_flag | 삭제 플래그 | BOOLEAN | NO | FALSE | - | Soft Delete 플래그 |
| deleted_at | 삭제 일시 | DATETIME | YES | NULL | - | 삭제 시간 |
| deleted_by | 삭제자 | VARCHAR(100) | YES | NULL | - | 삭제한 사용자 |

**인덱스**:
- PRIMARY KEY: `id`
- FOREIGN KEY: `post_id` REFERENCES `counsel_post(id)` ON DELETE CASCADE
- FOREIGN KEY: `parent_id` REFERENCES `counsel_comment(id)` ON DELETE CASCADE
- INDEX: `post_id`, `created_at`

**Entity 클래스**: `org.springframework.samples.petclinic.counsel.table.CounselComment`

---

### 3.3 counsel_attachments (온라인상담 첨부파일)

**테이블 설명**: 온라인 상담 게시글/댓글의 첨부파일 정보

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | INT | NO | AUTO_INCREMENT | PK | 첨부파일 고유 ID |
| file_path | 파일 경로 | VARCHAR(500) | NO | - | - | 저장된 파일 경로 (yyyy/MM/UUID.ext) |
| original_file_name | 원본 파일명 | VARCHAR(255) | NO | - | - | 사용자가 업로드한 원본 파일명 |
| file_size | 파일 크기 | BIGINT | NO | - | - | 파일 크기 (bytes) |
| mime_type | MIME 타입 | VARCHAR(100) | YES | NULL | - | 파일 MIME 타입 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 업로드 시간 |
| del_flag | 삭제 플래그 | BOOLEAN | NO | FALSE | - | Soft Delete 플래그 |
| deleted_at | 삭제 일시 | DATETIME | YES | NULL | - | 삭제 시간 (2주 후 물리 삭제) |

**인덱스**:
- PRIMARY KEY: `id`

**Entity 클래스**: `org.springframework.samples.petclinic.counsel.model.Attachment`

---

### 3.4 counsel_post_attachment (게시글-첨부파일 관계)

**테이블 설명**: 게시글과 첨부파일의 N:M 관계 중간 테이블

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| counsel_post_id | 게시글 ID | BIGINT | NO | - | PK, FK | counsel_post.id 참조 |
| attachment_id | 첨부파일 ID | INT | NO | - | PK, FK | counsel_attachments.id 참조 |

**인덱스**:
- PRIMARY KEY: `counsel_post_id, attachment_id`
- FOREIGN KEY: `counsel_post_id` REFERENCES `counsel_post(id)` ON DELETE CASCADE
- FOREIGN KEY: `attachment_id` REFERENCES `counsel_attachments(id)` ON DELETE CASCADE

**Entity 클래스**: `org.springframework.samples.petclinic.counsel.table.CounselPostAttachment`

---

### 3.5 counsel_comment_attachment (댓글-첨부파일 관계)

**테이블 설명**: 댓글과 첨부파일의 N:M 관계 중간 테이블

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| counsel_comment_id | 댓글 ID | BIGINT | NO | - | PK, FK | counsel_comment.id 참조 |
| attachment_id | 첨부파일 ID | INT | NO | - | PK, FK | counsel_attachments.id 참조 |

**인덱스**:
- PRIMARY KEY: `counsel_comment_id, attachment_id`
- FOREIGN KEY: `counsel_comment_id` REFERENCES `counsel_comment(id)` ON DELETE CASCADE
- FOREIGN KEY: `attachment_id` REFERENCES `counsel_attachments(id)` ON DELETE CASCADE

**Entity 클래스**: `org.springframework.samples.petclinic.counsel.table.CounselCommentAttachment`

---

## 4. 커뮤니티 테이블

### 4.1 community_post (커뮤니티 게시글)

**테이블 설명**: 공지사항 및 자유게시판 게시글

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 게시글 고유 ID |
| subject | 게시판 구분 | VARCHAR(50) | NO | - | - | notice/free |
| title | 제목 | VARCHAR(255) | NO | - | - | 게시글 제목 |
| content | 내용 | MEDIUMTEXT | NO | - | - | 게시글 내용 |
| author_name | 작성자 이름 | VARCHAR(100) | NO | - | - | 작성자 이름 |
| view_count | 조회수 | INT | NO | 0 | - | 조회수 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 게시글 작성 시간 |
| updated_at | 수정 일시 | DATETIME | NO | NOW() | - | 게시글 수정 시간 |
| del_flag | 삭제 플래그 | BOOLEAN | NO | FALSE | - | Soft Delete 플래그 |
| deleted_at | 삭제 일시 | DATETIME | YES | NULL | - | 삭제 시간 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `subject`, `created_at`

**Entity 클래스**: `org.springframework.samples.petclinic.community.table.CommunityPost`

---

### 4.2 community_post_attachment (커뮤니티 첨부파일 관계)

**테이블 설명**: 커뮤니티 게시글과 첨부파일의 N:M 관계

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| community_post_id | 게시글 ID | BIGINT | NO | - | PK, FK | community_post.id 참조 |
| attachment_id | 첨부파일 ID | INT | NO | - | PK, FK | attachment.id 참조 |

**인덱스**:
- PRIMARY KEY: `community_post_id, attachment_id`
- FOREIGN KEY: `community_post_id` REFERENCES `community_post(id)` ON DELETE CASCADE
- FOREIGN KEY: `attachment_id` REFERENCES `attachment(id)` ON DELETE CASCADE

**Entity 클래스**: `org.springframework.samples.petclinic.community.table.CommunityPostAttachment`

---

### 4.3 community_post_likes (커뮤니티 게시글 좋아요) ✨ NEW (2025-11-27)

**테이블 설명**: 커뮤니티 게시글 좋아요 기능 (Phase 2-2)

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 좋아요 고유 ID |
| post_id | 게시글 ID | BIGINT | NO | - | FK | community_post.id 참조 |
| username | 사용자 아이디 | VARCHAR(50) | NO | - | - | 로그인 사용자 ID |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 좋아요 누른 시간 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `(post_id, username)` - 중복 좋아요 방지
- FOREIGN KEY: `post_id` REFERENCES `community_post(id)` ON DELETE CASCADE

**Entity 클래스**: `org.springframework.samples.petclinic.community.table.CommunityPostLike`

**비즈니스 규칙**:
- 로그인 사용자만 좋아요 가능
- 한 사용자는 한 게시글에 1개의 좋아요만 가능 (UNIQUE 제약)
- 좋아요 취소 시 레코드 삭제 (토글 방식)
- 게시글 삭제 시 연관된 좋아요도 함께 삭제 (CASCADE)

---

## 5. 포토게시판 테이블

### 5.1 photo_post (포토게시글)

**테이블 설명**: 이미지 중심의 갤러리형 게시판

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 게시글 고유 ID |
| title | 제목 | VARCHAR(200) | NO | - | - | 게시글 제목 |
| content | 내용 | TEXT | YES | NULL | - | 게시글 내용 (Quill 에디터 HTML) |
| author | 작성자 | VARCHAR(100) | NO | - | - | 작성자 이름 |
| thumbnail_url | 썸네일 URL | VARCHAR(500) | YES | NULL | - | 대표 이미지 URL |
| view_count | 조회수 | INT | NO | 0 | - | 조회수 |
| like_count | 좋아요 수 | INT | NO | 0 | - | 좋아요 개수 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 게시글 작성 시간 |
| updated_at | 수정 일시 | DATETIME | NO | NOW() | - | 게시글 수정 시간 |
| del_flag | 삭제 플래그 | BOOLEAN | NO | FALSE | - | Soft Delete 플래그 |
| deleted_at | 삭제 일시 | DATETIME | YES | NULL | - | 삭제 시간 |
| deleted_by | 삭제자 | VARCHAR(60) | YES | NULL | - | 삭제한 사용자 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `idx_photo_created` (created_at DESC)
- INDEX: `idx_photo_author` (author)

**Entity 클래스**: `org.springframework.samples.petclinic.photo.table.PhotoPost`

---

### 5.2 photo_post_likes (포토게시글 좋아요) ✨ NEW (2025-11-27)

**테이블 설명**: 포토게시판 게시글 좋아요 기능 (Phase 2-3)

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | BIGINT | NO | AUTO_INCREMENT | PK | 좋아요 고유 ID |
| post_id | 게시글 ID | BIGINT | NO | - | FK | photo_post.id 참조 |
| username | 사용자 아이디 | VARCHAR(50) | NO | - | - | 로그인 사용자 ID |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 좋아요 누른 시간 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_photo_post_likes_post_username` (post_id, username) - 중복 좋아요 방지
- INDEX: `idx_photo_post_likes_post` (post_id)
- INDEX: `idx_photo_post_likes_username` (username)
- FOREIGN KEY: `fk_photo_post_likes_post` REFERENCES `photo_post(id)` ON DELETE CASCADE

**Entity 클래스**: `org.springframework.samples.petclinic.photo.table.PhotoPostLike`

**비즈니스 규칙**:
- 로그인 사용자만 좋아요 가능
- 한 사용자는 한 게시글에 1개의 좋아요만 가능 (UNIQUE 제약)
- 좋아요 취소 시 레코드 삭제 (토글 방식)
- 게시글 삭제 시 연관된 좋아요도 함께 삭제 (CASCADE)

---

## 6. 공통 테이블

### 5.1 attachment (공용 첨부파일)

**테이블 설명**: 커뮤니티 게시판용 공용 첨부파일 (온라인상담은 별도 테이블 사용)

| 컬럼명 | 한글명 | 데이터 타입 | NULL | 기본값 | 키 | 설명 |
|--------|--------|------------|------|--------|-----|------|
| id | ID | INT | NO | AUTO_INCREMENT | PK | 첨부파일 고유 ID |
| file_path | 파일 경로 | VARCHAR(500) | NO | - | - | 저장된 파일 경로 |
| original_file_name | 원본 파일명 | VARCHAR(255) | NO | - | - | 원본 파일명 |
| file_size | 파일 크기 | BIGINT | NO | - | - | 파일 크기 (bytes) |
| mime_type | MIME 타입 | VARCHAR(100) | YES | NULL | - | 파일 MIME 타입 |
| created_at | 생성 일시 | DATETIME | NO | NOW() | - | 업로드 시간 |
| del_flag | 삭제 플래그 | BOOLEAN | NO | FALSE | - | Soft Delete 플래그 |
| deleted_at | 삭제 일시 | DATETIME | YES | NULL | - | 삭제 시간 |

**인덱스**:
- PRIMARY KEY: `id`

**Entity 클래스**: `org.springframework.samples.petclinic.common.table.Attachment`

---

## 7. 변경 이력

### [3.5.3] - 2025-11-27 ✨ NEW

#### 추가
- **community_post_likes** 테이블 추가 (커뮤니티 좋아요 기능, Phase 2-2)
  - 로그인 사용자만 좋아요 가능
  - UNIQUE 제약으로 중복 좋아요 방지
  - CASCADE 삭제로 게시글 삭제 시 연관 데이터 자동 삭제
  
- **photo_post** 테이블 추가 (포토게시판)
  - 이미지 중심 갤러리형 게시판
  - 썸네일 자동 추출 기능 지원
  - Soft Delete 정책 적용

- **photo_post_likes** 테이블 추가 (포토게시판 좋아요 기능, Phase 2-3)
  - 로그인 사용자만 좋아요 가능
  - UNIQUE 제약으로 중복 좋아요 방지
  - CASCADE 삭제로 게시글 삭제 시 연관 데이터 자동 삭제

#### 수정
- 없음

#### 관련 문서
- [2025-11-27 Community 좋아요 기능 완료](../07-changelog/2025-11-27-community-like-feature.md)
- [2025-11-27 Photo 좋아요 기능 완료](../07-changelog/2025-11-27-photo-like-feature.md)

---

### [3.5.2] - 2025-11-06

#### 추가
- **users** 테이블 추가 (사용자 정보 관리)
- **user_roles** 테이블 추가 (사용자 권한 관리)
- **persistent_logins** 테이블 추가 (Remember-Me 토큰)
- **system_config** 테이블 추가 (시스템 설정 관리)

#### 수정
- 없음

#### 삭제
- 없음

---

### [3.5.1] - 2025-11-05

#### 추가
- **counsel_post** 테이블 생성
- **counsel_comment** 테이블 생성
- **counsel_attachments** 테이블 생성
- **counsel_post_attachment** 관계 테이블 생성
- **counsel_comment_attachment** 관계 테이블 생성
- **community_post** 테이블 생성
- **community_post_attachment** 관계 테이블 생성
- **attachment** 공용 테이블 생성

#### 수정
- 없음

#### 삭제
- 없음

---

## 📌 테이블 관계도

```
[users] 1 ──< N [user_roles]
           (사용자-권한)

[users] 1 ──< N [persistent_logins]
           (사용자-Remember-Me)

[counsel_post] 1 ──< N [counsel_comment]
              (게시글-댓글)

[counsel_comment] 1 ──< N [counsel_comment]
                  (댓글-대댓글, self-join)

[counsel_post] N ──< N [counsel_attachments]
              (중간: counsel_post_attachment)

[counsel_comment] N ──< N [counsel_attachments]
                  (중간: counsel_comment_attachment)

[community_post] N ──< N [attachment]
                (중간: community_post_attachment)
```

---

## 📝 작성 규칙

1. **테이블 추가 시**: 
   - 테이블 정의서에 컬럼 정보 추가
   - 변경 이력에 날짜와 함께 기록
   - Entity 클래스 경로 명시

2. **컬럼 수정 시**:
   - 변경 이력에 수정 내역 기록
   - 변경 사유 및 영향 범위 명시

3. **테이블 삭제 시**:
   - 변경 이력에 삭제 사유 기록
   - 데이터 마이그레이션 계획 명시

4. **문서 업데이트 주기**:
   - 테이블 구조 변경 시 즉시 업데이트
   - 월 1회 전체 검토

---

**문서 버전**: 1.0  
**최종 검토**: 2025-11-06  
**담당자**: Jeongmin Lee

