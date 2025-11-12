# 📝 Spring PetClinic 프로젝트 빠른 참조 가이드

## 🎯 핵심 요약

### 프로젝트 구조
```
Controller → Service → Repository → Entity
    ↓          ↓          ↓           ↓
   DTO ←── Mapper ←── Entity ←── Database
```

### 필수 규칙
1. ❌ Entity를 뷰/API에 직접 노출 금지
2. ✅ DTO + Mapper 사용 필수
3. ✅ QueryDSL은 RepositoryImpl에서만
4. ✅ LocalDateTime 사용 (Date 사용 금지)
5. ✅ Soft Delete (@SQLDelete, @SQLRestriction)
6. ✅ UI는 사용자 직관적이어야 함 (아이콘 + 텍스트, 안내 문구)
7. ✅ 모든 수정/생성 행위는 DB에 일자 기록 (@CreationTimestamp, @UpdateTimestamp)
8. ✅ **UI/UX 일관성 유지** (버튼 크기, 폰트 크기, 간격 통일) ⭐NEW

---

## 🎨 UI/UX 일관성 규칙 (필수 준수)

### 버튼 크기 통일
```html
<!-- 일반 버튼 -->
<button class="btn btn-primary" style="height: 42px;">버튼</button>

<!-- 주요 액션 버튼 -->
<button class="btn btn-primary" style="min-width: 120px; height: 42px;">저장</button>

<!-- 상세화면 수정/삭제 (붙여서) -->
<a class="btn btn-warning" style="min-width: 80px;">수정</a>
<button class="btn btn-danger" style="min-width: 80px;">삭제</button>
```

### 폰트 크기 통일
```
- 헤더 링크: font-size: 0.95rem
- placeholder: font-size: 0.95rem
- 본문: 1rem (기본)
```

### 간격(spacing) 통일
```
- 링크 간격: px-2 (좌우), px-1 (구분선)
- 버튼 간격: gap-2
- 카드 여백: p-4 또는 p-5
```

### 입력 필드 규칙
```html
<!-- 필수 필드 -->
<label>닉네임 <span class="text-danger">*</span></label>

<!-- placeholder + 안내 문구 -->
<input placeholder="간단명료하게" style="font-size: 0.95rem;">
<small class="form-text text-muted">상세 안내는 여기에</small>

<!-- 실시간 검증 -->
<input class="form-control is-valid">   <!-- 초록 -->
<input class="form-control is-invalid"> <!-- 빨강 -->
```

### 체크리스트 (새 기능 추가 시)
- [ ] 버튼 크기: 42px 또는 120px×42px
- [ ] 폰트 크기: 0.95rem 또는 1rem
- [ ] 간격: px-2, px-1, gap-2
- [ ] placeholder 간소화
- [ ] 안내 문구 <small> 사용
- [ ] 아이콘 + 텍스트 함께 표시

---

## 📁 패키지 구조

```
org.springframework.samples.petclinic
├── common/        # 공통 (Config, DTO, Entity, Init)
├── community/     # 커뮤니티 게시판
├── counsel/       # 온라인상담 게시판
└── system/        # 시스템 설정
```

---

## 🗄️ 주요 테이블

### counsel_post (온라인상담 게시글)
```sql
id, title, content, content_path,
author_name, password_hash, is_secret,
status (WAIT/COMPLETE/END),
view_count, comment_count,
created_at, updated_at,
del_flag, deleted_at
```

**공개/비공개 기준**:
- `secret=false` (공개): passwordHash = null
- `secret=true` (비공개): BCrypt 해시 저장, 비밀번호 입력 필요

**상태별 댓글**:
- `WAIT` (답변대기): 댓글 없음
- `COMPLETE` (답변완료): 댓글 1개 보장
- `END` (상담종료): 댓글 랜덤

### counsel_comment (댓글)
```sql
id, post_id, parent_id,
content, author_name, password_hash,
is_staff_reply (운영자 답변 여부),
created_at, del_flag
```

### counsel_attachments (첨부파일)
```sql
id, file_path (yyyy/MM/UUID.ext),
original_file_name, file_size, mime_type,
created_at, del_flag, deleted_at
```

**Soft Delete**: del_flag=true → 2주 후 스케줄러가 물리 삭제

---

## 🔄 API 요청 흐름 (온라인상담 목록)

```
GET /counsel/list?page=0&type=title&keyword=수술
    ↓
CounselController.list()
    ↓
CounselService.search()
    ↓
CounselPostRepositoryImpl.search() (QueryDSL)
    ↓
MySQL: SELECT * WHERE title LIKE '%수술%' LIMIT 10
    ↓
Entity → DTO 변환 (CounselPostMapper)
    ↓
Thymeleaf 렌더링 (counselList.html)
    ↓
HTML 응답
```

---

## 📋 주요 URL

| 기능 | URL | Method |
|------|-----|--------|
| 온라인상담 목록 | `/counsel/list` | GET |
| 온라인상담 검색 | `/counsel/list?type=title&keyword=수술` | GET |
| 상세 조회 | `/counsel/detail/{id}` | GET |
| 비밀번호 입력 | `/counsel/detail/{id}/password` | GET |
| 비밀번호 검증 | `/counsel/detail/{id}/unlock` | POST |
| 글쓰기 폼 | `/counsel/write` | GET |
| 글 등록 | `/counsel` | POST |
| **글 수정 폼 ⭐NEW** | `/counsel/edit/{id}` | GET |
| **글 수정 처리 ⭐NEW** | `/counsel/edit/{id}` | POST |
| **글 삭제 (Soft Delete) ⭐NEW** | `/counsel/delete/{id}` | POST |
| 댓글 등록 | `/counsel/detail/{postId}/comments` | POST |
| 댓글 삭제 | `/counsel/detail/{postId}/comments/{commentId}/delete` | POST |
| **파일 다운로드 ⭐NEW** | `/counsel/download/{fileId}` | GET |
| 커뮤니티 목록 | `/community/list?subject=notice` | GET |

---

## 🆕 신규 기능 (2025-11-06)

### 1️⃣ 파일 다운로드
```java
@GetMapping("/download/{fileId}")
public ResponseEntity<Resource> downloadFile(@PathVariable Integer fileId) {
    Attachment attachment = attachmentRepository.findById(fileId).orElseThrow();
    Resource resource = new UrlResource(filePath.toUri());
    
    String contentDisposition = "attachment; filename*=UTF-8''" + 
        URLEncoder.encode(attachment.getOriginalFileName(), StandardCharsets.UTF_8);
    
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
        .body(resource);
}
```
- ✅ UTF-8 한글 파일명 지원
- ✅ MIME 타입 및 파일 크기 전송

### 2️⃣ 게시글 수정/삭제
```java
// 수정
public boolean updatePost(Long postId, CounselPostWriteDto dto, String password) {
    // 비밀번호 검증 → 본문 파일 교체 → 저장
}

// 삭제 (Soft Delete)
public boolean deletePost(Long postId, String password) {
    repository.delete(entity); // @SQLDelete 실행
}
```
- ✅ 비밀번호 검증 (비공개 글)
- ✅ Soft Delete 적용

### 3️⃣ 조회수 중복 방지 (세션 기반)
```java
Set<Long> viewedPosts = (Set<Long>) session.getAttribute("viewedCounselPosts");
if (!viewedPosts.contains(id)) {
    counselService.incrementViewCount(id);
    viewedPosts.add(id);
    session.setAttribute("viewedCounselPosts", viewedPosts);
}
```
- ✅ 같은 세션에서 재방문 시 조회수 증가 안 함
- ✅ 예외 처리로 안정성 확보

### 4️⃣ Soft Delete 정책
```java
@SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CounselPost extends BaseEntity {
    @Column(name = "del_flag", nullable = false)
    private boolean delFlag = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```
- ✅ 물리적 DELETE 대신 논리적 DELETE
- ✅ 2주 후 FileCleanupScheduler가 물리 삭제
- ✅ 로그 기록: "Successfully soft-deleted post with ID: X"

### 5️⃣ UI 개선
- ✅ 대댓글 트리 구조 (들여쓰기 + 파란색 테두리)
- ✅ 운영자 댓글 배지 강화 (초록색 + 아이콘)
- ✅ 수정/삭제 버튼 추가
- ✅ 모달 추가 (삭제 확인, 댓글 삭제)

---

## ⚙️ 설정 파일

### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/petclinic?sessionVariables=FOREIGN_KEY_CHECKS=0
  jpa:
    hibernate:
      ddl-auto: update  # ✅ 권장: 기존 데이터 유지, DROP 오류 없음
      # ddl-auto: create-drop  # ❌ 금지: 테이블 DROP 오류 발생
    show-sql: true
```

**DDL 옵션 선택 가이드**:
- `update`: 개발 환경 권장 (데이터 유지, 스키마 자동 업데이트)
- `create`: 초기 개발 시작 시 (매번 데이터 삭제)
- `create-drop`: 절대 사용 금지 (DROP 오류 및 데이터 손실)
- `validate`: 운영 환경 (스키마 검증만)

**외래키 체크 비활성화 이유**: 개발 환경에서 테이블 DROP/CREATE 순서 문제 해결

---

## 🚀 서버 실행 규칙 ⭐NEW

### ✅ 권장: IDE에서 실행
```
1. IntelliJ IDEA에서 PetClinicApplication.java 열기
2. main 메서드 옆 실행 버튼 클릭
3. Active profiles: dev 설정
4. Run 또는 Debug 실행
```

### ❌ 금지: 터미널 bootRun
```bash
# ❌ 사용 금지
./gradlew bootRun  # 포트 점유 문제 발생, 종료 어려움
```

**이유**: 
- 백그라운드 실행 시 포트가 살아있어 수동 종료 필요
- 프로세스 관리 어려움
- IDE에서 실행하면 Stop 버튼으로 간단히 종료 가능

### ✅ 허용: 컴파일 및 빌드
```bash
# 컴파일만
./gradlew compileJava

# 빌드 (테스트 제외)
./gradlew build -x test

# Gradle Daemon 종료
./gradlew --stop
```

---

## 💾 데이터 초기화 (DataInit)

### 커뮤니티
- 공지사항 3개 + 더미 103개 = 총 106개

### 온라인상담
- 총 112개 게시글 (페이지당 10개 기준)
- 상태: WAIT/COMPLETE/END 랜덤 (각 1/3 확률)
- 공개/비공개: 랜덤 (50% 확률)
- COMPLETE 상태는 운영자 댓글 1개 자동 생성
- 비공개 게시글 비밀번호: `1234`

---

## 🔒 비밀번호 검증 흐름

```
1. 사용자가 비공개 글 클릭
   ↓
2. Session에 unlock된 ID가 없으면
   → /counsel/detail/{id}/password (비밀번호 입력)
   ↓
3. POST /counsel/detail/{id}/unlock (비밀번호 제출)
   ↓
4. BCrypt.checkpw(입력값, DB의 passwordHash) 검증
   ↓
5. 성공: Session에 ID 저장 → 상세 페이지로 이동
   실패: 비밀번호 입력 페이지로 다시 이동
```

---

## 📂 파일 저장 구조

```
data/
├── counsel/
│   ├── contents/              # 본문 HTML
│   │   └── 2025/
│   │       └── 06/
│   │           └── {UUID}.html
│   └── uploads/               # 첨부파일
│       └── 2025/
│           └── 06/
│               └── {UUID}.jpg
```

**파일 검증**:
- 허용 MIME: `image/jpeg`, `image/png`, `image/gif`
- 최대 크기: 5MB
- Apache Tika로 MIME 타입 검증

---

## 🧹 Soft Delete + 스케줄러

```java
@Entity
@SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CounselPost {
    @Column(name = "del_flag")
    private boolean delFlag = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

**FileCleanupScheduler**:
- 매일 자정 실행 (`@Scheduled(cron = "0 0 0 * * ?")`)
- `del_flag=true && deleted_at < 2주 전` 파일 물리 삭제
- 로그 기록: `log.info("Deleted file: {}", fileName)`

---

## 🔧 QueryDSL 사용법

```java
// 1. RepositoryCustom 인터페이스
public interface CounselPostRepositoryCustom {
    PageResponse<CounselPost> search(String type, String keyword, Pageable pageable);
}

// 2. RepositoryImpl 구현
public class CounselPostRepositoryImpl implements CounselPostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    
    @Override
    public PageResponse<CounselPost> search(...) {
        QCounselPost post = QCounselPost.counselPost;
        
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null) {
            switch (type) {
                case "title":
                    builder.and(post.title.containsIgnoreCase(keyword));
                    break;
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

// 3. JpaRepository 상속
public interface CounselPostRepository extends 
    JpaRepository<CounselPost, Long>, 
    CounselPostRepositoryCustom {
}
```

---

## 🎨 Thymeleaf 사용법

### 날짜 포맷
```html
<span th:text="${#temporals.format(post.createdAt, 'yyyy-MM-dd HH:mm:ss')}"></span>
```

### 조건부 렌더링
```html
<span th:if="${post.secret}" class="badge bg-warning">비공개</span>
<span th:unless="${post.secret}" class="badge bg-primary">공개</span>
```

### 반복문
```html
<tr th:each="post : ${posts}">
    <td th:text="${post.id}"></td>
    <td th:text="${post.title}"></td>
</tr>
```

### 페이지네이션
```html
<div th:replace="fragments/pagination :: pagination('/counsel/list', ${page})"></div>
```

---

## 🐛 문제 해결 이력

### 1. 외래키 DROP 에러
**문제**: 서버 종료 시 `Cannot drop table 'counsel_post'`

**해결**:
```yaml
# application-dev.yml
datasource:
  url: jdbc:mysql://...?sessionVariables=FOREIGN_KEY_CHECKS=0
```

### 2. Entity 이름 충돌
**문제**: `Attachment` 엔티티 중복

**해결**:
```java
@Entity(name = "CounselAttachment")
@Table(name = "counsel_attachments")
public class Attachment { }
```

### 3. 데이터 초기화 개선
**변경 전**: COMPLETE 72개 고정

**변경 후**: WAIT/COMPLETE/END 랜덤 (1/3 확률)

---

## 📌 체크리스트

### 새 Entity 추가 시
- [ ] BaseEntity 또는 NamedEntity 상속
- [ ] @Entity, @Table 어노테이션
- [ ] @SQLDelete, @SQLRestriction (Soft Delete)
- [ ] Getter/Setter 생성
- [ ] 컬럼별 주석 추가

### 새 기능 개발 시
- [ ] Controller → Service → Repository 순서
- [ ] DTO 클래스 생성
- [ ] Mapper 클래스 생성
- [ ] Entity 직접 노출 금지 확인
- [ ] @Transactional 추가
- [ ] 로그 추가 (log.info, log.error)
- [ ] try-catch로 예외 처리
- [ ] JavaDoc 주석 작성

---

## 🚀 서버 실행

```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

**접속 URL**:
- 홈: http://localhost:8080
- 온라인상담: http://localhost:8080/counsel/list
- 커뮤니티: http://localhost:8080/community/list?subject=notice

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-05  
**참조**: PROJECT_DOCUMENTATION.md
