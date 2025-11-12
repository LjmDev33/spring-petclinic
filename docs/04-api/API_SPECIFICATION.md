# 📡 API 명세서 (API Specification)

**프로젝트**: Spring PetClinic  
**버전**: 3.5.3  
**최종 수정일**: 2025-11-11  
**작성자**: Jeongmin Lee

---

## 📋 목차
1. [개요](#개요)
2. [인증 API](#인증-api)
3. [온라인상담 API](#온라인상담-api)
4. [커뮤니티 API](#커뮤니티-api)
5. [파일 관리 API](#파일-관리-api)
6. [시스템 설정 API](#시스템-설정-api)
7. [공통 응답 형식](#공통-응답-형식)
8. [에러 코드](#에러-코드)

---

## 개요

### Base URL
```
http://localhost:8080
```

### 인증 방식
- **Spring Security** 기반 세션 인증
- **Remember-Me** 토큰 (7일간 유지)

### 공통 헤더
```http
Content-Type: application/json; charset=UTF-8
Accept: application/json
```

---

## 인증 API

### 1. 로그인

**엔드포인트**: `POST /login`

**요청 (Form Data)**:
```http
POST /login
Content-Type: application/x-www-form-urlencoded

username=admin&password=admin1234&remember-me=true
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| username | String | ✅ | 사용자 아이디 (4-20자) |
| password | String | ✅ | 비밀번호 (8자 이상) |
| remember-me | Boolean | ❌ | 자동 로그인 여부 (기본값: false) |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /
Set-Cookie: JSESSIONID=...; Path=/; HttpOnly
Set-Cookie: remember-me=...; Max-Age=604800; Path=/; HttpOnly
```

**실패 응답**:
```http
HTTP/1.1 302 Found
Location: /login?error
```

---

### 2. 회원가입

**엔드포인트**: `POST /register`

**요청 (Form Data)**:
```http
POST /register
Content-Type: application/x-www-form-urlencoded

username=user123&password=password123&passwordConfirm=password123
&name=홍길동&email=user@example.com&phone=010-1234-5678
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 | 검증 규칙 |
|------|------|------|------|----------|
| username | String | ✅ | 사용자 아이디 | 4-20자 영문/숫자 |
| password | String | ✅ | 비밀번호 | 8자 이상 |
| passwordConfirm | String | ✅ | 비밀번호 확인 | password와 일치 |
| name | String | ✅ | 이름 | - |
| email | String | ✅ | 이메일 | 이메일 형식 |
| phone | String | ❌ | 전화번호 | 010-1234-5678 형식 |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /login
Flash Message: "회원가입이 완료되었습니다."
```

**실패 응답**:
```http
HTTP/1.1 302 Found
Location: /register
Flash Error: "아이디가 이미 존재합니다."
```

---

### 3. 로그아웃

**엔드포인트**: `POST /logout`

**요청**:
```http
POST /logout
```

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /login?logout
Set-Cookie: JSESSIONID=; Max-Age=0
Set-Cookie: remember-me=; Max-Age=0
```

---

## 온라인상담 API

### 1. 게시글 목록 조회

**엔드포인트**: `GET /counsel/list`

**요청 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| page | Integer | ❌ | 페이지 번호 (0부터 시작) | 0 |
| type | String | ❌ | 검색 타입 (title, author, content) | - |
| keyword | String | ❌ | 검색 키워드 | - |

**요청 예시**:
```http
GET /counsel/list?page=0&type=title&keyword=수술
```

**응답 (HTML)**:
- View: `counsel/counselList.html`
- Model 속성:
  - `posts`: List<CounselPostDto>
  - `page`: PageResponse
  - `searchType`: String
  - `searchKeyword`: String

---

### 2. 게시글 상세 조회

**엔드포인트**: `GET /counsel/detail/{id}`

**경로 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 게시글 ID |

**요청 예시**:
```http
GET /counsel/detail/1
```

**응답 (HTML)**:
- **공개 게시글**: 상세 화면 표시
- **비공개 게시글**: 비밀번호 입력 화면으로 리다이렉트

**Model 속성**:
| 속성 | 타입 | 설명 |
|------|------|------|
| post | CounselPostDto | 게시글 정보 |
| comments | List<CounselCommentDto> | 댓글 목록 |

**비공개 게시글 처리**:
1. 세션에 `unlockedCounselPosts` 확인
2. ID가 없으면 `/counsel/detail/{id}/password`로 리다이렉트

---

### 3. 비공개 게시글 비밀번호 검증

**엔드포인트**: `POST /counsel/detail/{id}/unlock`

**요청 (Form Data)**:
```http
POST /counsel/detail/1/unlock
Content-Type: application/x-www-form-urlencoded

password=1234
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| password | String | ✅ | 게시글 비밀번호 |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/detail/1
Session: unlockedCounselPosts에 ID 추가
```

**실패 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/detail/1/password?error
Flash Error: "비밀번호가 올바르지 않습니다."
```

---

### 4. 게시글 작성

**엔드포인트**: `POST /counsel`

**요청 (Multipart Form Data)**:
```http
POST /counsel
Content-Type: multipart/form-data

title=문의드립니다
content=<p>문의 내용...</p>
authorName=홍길동
secret=true
password=1234
files=@file1.jpg
files=@file2.pdf
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | ✅ | 제목 |
| content | String | ✅ | 본문 (HTML) |
| authorName | String | ✅ | 작성자 |
| secret | Boolean | ❌ | 비공개 여부 (기본값: false) |
| password | String | ❌ | 비밀번호 (비공개 시 필수) |
| files | MultipartFile[] | ❌ | 첨부파일 (최대 5MB) |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/detail/{newId}
Flash Message: "게시글이 등록되었습니다."
```

---

### 5. 게시글 수정

**엔드포인트**: `POST /counsel/edit/{id}`

**요청 (Multipart Form Data)**:
```http
POST /counsel/edit/1
Content-Type: multipart/form-data

title=수정된 제목
content=<p>수정된 내용...</p>
authorName=홍길동
password=1234
files=@new_file.jpg
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | ✅ | 제목 |
| content | String | ✅ | 본문 (HTML) |
| authorName | String | ✅ | 작성자 |
| password | String | ❌ | 비밀번호 (비공개 게시글 시 필수) |
| files | MultipartFile[] | ❌ | 첨부파일 |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/detail/1
Flash Message: "게시글이 수정되었습니다."
```

**실패 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/edit/1
Flash Error: "비밀번호가 올바르지 않습니다."
```

---

### 6. 게시글 삭제 (Soft Delete)

**엔드포인트**: `POST /counsel/delete/{id}`

**요청 (Form Data)**:
```http
POST /counsel/delete/1
Content-Type: application/x-www-form-urlencoded

password=1234
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| password | String | ❌ | 비밀번호 (비공개 게시글 시 필수) |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/list
Flash Message: "게시글이 삭제되었습니다."
```

**Soft Delete 처리**:
```sql
UPDATE counsel_post 
SET del_flag = 1, deleted_at = NOW() 
WHERE id = 1;
```

---

### 7. 댓글 등록

**엔드포인트**: `POST /counsel/detail/{postId}/comments`

**요청 (Form Data)**:
```http
POST /counsel/detail/1/comments
Content-Type: application/x-www-form-urlencoded

authorName=홍길동&password=1234&content=댓글 내용입니다.
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| authorName | String | ✅ | 작성자 이름 |
| password | String | ❌ | 비밀번호 (삭제 시 필요) |
| content | String | ✅ | 댓글 내용 |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/detail/1
Flash Message: "댓글이 등록되었습니다."
```

---

### 8. 댓글 삭제

**엔드포인트**: `POST /counsel/detail/{postId}/comments/{commentId}/delete`

**요청 (Form Data)**:
```http
POST /counsel/detail/1/comments/10/delete
Content-Type: application/x-www-form-urlencoded

password=1234
```

**요청 파라미터**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| password | String | ❌ | 비밀번호 (설정한 경우 필수) |

**성공 응답**:
```http
HTTP/1.1 302 Found
Location: /counsel/detail/1
Flash Message: "댓글이 삭제되었습니다."
```

---

## 파일 관리 API

### 1. 파일 다운로드

**엔드포인트**: `GET /counsel/download/{fileId}`

**경로 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| fileId | Integer | ✅ | 파일 ID |

**요청 예시**:
```http
GET /counsel/download/5
```

**성공 응답**:
```http
HTTP/1.1 200 OK
Content-Type: image/jpeg
Content-Disposition: attachment; filename*=UTF-8''%ED%8C%8C%EC%9D%BC%EB%AA%85.jpg
Content-Length: 102400

[파일 바이너리 데이터]
```

**실패 응답**:
```http
HTTP/1.1 404 Not Found
```

---

## 커뮤니티 API

### 1. 게시글 목록 조회

**엔드포인트**: `GET /community/list`

**요청 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| subject | String | ✅ | 게시판 구분 (notice, free) | - |
| page | Integer | ❌ | 페이지 번호 (0부터 시작) | 0 |

**요청 예시**:
```http
GET /community/list?subject=notice&page=0
```

**응답 (HTML)**:
- View: `community/noticeList.html`
- Model 속성:
  - `posts`: List<CommunityPostDto>
  - `page`: PageResponse
  - `subject`: String

---

## 시스템 설정 API

### 1. 시스템 설정 조회 (관리자)

**엔드포인트**: `GET /api/system/config`

**권한**: ROLE_ADMIN

**응답 예시**:
```json
{
  "multiLoginEnabled": true,
  "fileUploadEnabled": true,
  "maxFileSize": 5242880
}
```

---

## 공통 응답 형식

### 성공 응답 (페이지네이션)

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0
  },
  "totalElements": 112,
  "totalPages": 12,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 10,
  "empty": false
}
```

### Flash 메시지

**성공**:
```html
<div class="alert alert-success">게시글이 등록되었습니다.</div>
```

**실패**:
```html
<div class="alert alert-danger">비밀번호가 올바르지 않습니다.</div>
```

---

## 에러 코드

| HTTP 상태 | 에러 코드 | 설명 | 대응 방법 |
|----------|-----------|------|----------|
| 401 | UNAUTHORIZED | 인증 실패 | 로그인 필요 |
| 403 | FORBIDDEN | 권한 없음 | 관리자 권한 필요 |
| 404 | NOT_FOUND | 리소스 없음 | ID 확인 필요 |
| 500 | INTERNAL_ERROR | 서버 오류 | 관리자 문의 |

---

## 변경 이력

### [3.5.3] - 2025-11-11
#### 추가
- 최초 API 명세서 작성
- 인증 API (로그인, 회원가입, 로그아웃)
- 온라인상담 API (CRUD, 댓글, 파일 다운로드)
- 커뮤니티 API (목록 조회)
- 시스템 설정 API (조회)

---

**문서 버전**: 1.0  
**최종 검토**: 2025-11-11  
**담당자**: Jeongmin Lee

