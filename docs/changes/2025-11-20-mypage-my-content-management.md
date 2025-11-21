# 4단계: 마이페이지 - 내 게시글/댓글 관리 완료

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot

## ✅ 작업 완료 요약

### 구현된 기능
1. **내가 작성한 온라인상담 게시글 목록**
2. **내가 작성한 댓글 목록**
3. **마이페이지 빠른 메뉴 추가**

---

## 📝 상세 구현 내용

### 1️⃣ Repository 확장

#### CounselPostRepository
```java
/**
 * 작성자 이름으로 게시글 목록 조회 (페이징)
 */
Page<CounselPost> findByAuthorNameOrderByCreatedAtDesc(String authorName, Pageable pageable);
```

#### CounselCommentRepository
```java
/**
 * 작성자 이름으로 댓글 목록 조회 (페이징)
 */
Page<CounselComment> findByAuthorNameOrderByCreatedAtDesc(String authorName, Pageable pageable);
```

### 2️⃣ UserService 확장

```java
/**
 * 내가 작성한 온라인상담 게시글 목록 조회
 */
@Transactional(readOnly = true)
public PageResponse<CounselPostDto> getMyPosts(String nickname, Pageable pageable) {
    Page<CounselPost> entityPage = counselPostRepository
        .findByAuthorNameOrderByCreatedAtDesc(nickname, pageable);
    Page<CounselPostDto> dtoPage = entityPage.map(counselPostMapper::toDto);
    return new PageResponse<>(dtoPage);
}

/**
 * 내가 작성한 댓글 목록 조회
 */
@Transactional(readOnly = true)
public Page<CounselComment> getMyComments(String nickname, Pageable pageable) {
    return counselCommentRepository
        .findByAuthorNameOrderByCreatedAtDesc(nickname, pageable);
}
```

**의존성 주입:**
- CounselPostRepository
- CounselCommentRepository
- CounselPostMapper

### 3️⃣ MyPageController 확장

#### 내 게시글 조회
```java
@GetMapping("/my-posts")
public String myPosts(
    Authentication authentication,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    Model model) {
    
    String username = authentication.getName();
    User user = userService.findByUsername(username);
    String nickname = user.getNickname();

    Pageable pageable = PageRequest.of(page, size);
    PageResponse<CounselPostDto> posts = userService.getMyPosts(nickname, pageable);

    model.addAttribute("posts", posts);
    model.addAttribute("currentPage", page);
    model.addAttribute("template", "user/my-posts");
    return "fragments/layout";
}
```

#### 내 댓글 조회
```java
@GetMapping("/my-comments")
public String myComments(
    Authentication authentication,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    Model model) {
    
    String username = authentication.getName();
    User user = userService.findByUsername(username);
    String nickname = user.getNickname();

    Pageable pageable = PageRequest.of(page, size);
    Page<CounselComment> comments = userService.getMyComments(nickname, pageable);

    model.addAttribute("comments", comments);
    model.addAttribute("currentPage", page);
    model.addAttribute("template", "user/my-comments");
    return "fragments/layout";
}
```

### 4️⃣ 뷰 템플릿

#### my-posts.html (내 게시글 목록)
```html
<table class="table table-hover">
  <thead>
    <tr>
      <th>번호</th>
      <th>제목</th>
      <th>상태</th>
      <th>공개여부</th>
      <th>작성일</th>
      <th>조회수</th>
      <th>첨부</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="post : ${posts.content}">
      <td th:text="${post.id}"></td>
      <td>
        <a th:href="@{/counsel/detail/{id}(id=${post.id})}" 
           th:text="${post.title}"></a>
      </td>
      <td>
        <span th:if="${post.status.name() == 'WAIT'}" 
              class="badge bg-warning">답변대기</span>
        <span th:if="${post.status.name() == 'COMPLETE'}" 
              class="badge bg-success">답변완료</span>
        <span th:if="${post.status.name() == 'END'}" 
              class="badge bg-secondary">상담종료</span>
      </td>
      <td>
        <span th:if="${post.secret}" class="badge bg-danger">비공개</span>
        <span th:if="${!post.secret}" class="badge bg-info">공개</span>
      </td>
      <td th:text="${#temporals.format(post.createdAt, 'yyyy-MM-dd')}"></td>
      <td th:text="${post.viewCount}"></td>
      <td>
        <i th:if="${post.attachFlag}" class="bi bi-paperclip"></i>
      </td>
    </tr>
  </tbody>
</table>

<!-- 페이징 -->
<nav>
  <ul class="pagination">
    <li th:classappend="${posts.currentPage == 0} ? 'disabled'">
      <a th:href="@{/mypage/my-posts(page=${posts.currentPage - 1})}">이전</a>
    </li>
    <li th:each="i : ${#numbers.sequence(0, posts.totalPages - 1)}"
        th:classappend="${i == posts.currentPage} ? 'active'">
      <a th:href="@{/mypage/my-posts(page=${i})}" th:text="${i + 1}"></a>
    </li>
    <li th:classappend="${posts.currentPage + 1 >= posts.totalPages} ? 'disabled'">
      <a th:href="@{/mypage/my-posts(page=${posts.currentPage + 1})}">다음</a>
    </li>
  </ul>
</nav>
```

#### my-comments.html (내 댓글 목록)
```html
<div class="list-group">
  <div th:each="comment : ${comments.content}" 
       class="list-group-item">
    <!-- 게시글 제목 -->
    <h6>
      <a th:href="@{/counsel/detail/{id}(id=${comment.post.id})}">
        <i class="bi bi-arrow-right-circle"></i>
        <span th:text="${comment.post.title}"></span>
      </a>
    </h6>
    
    <!-- 댓글 내용 -->
    <p th:text="${comment.content}"></p>
    
    <!-- 배지 -->
    <div>
      <span th:if="${comment.parentId != null}" 
            class="badge bg-info">답글</span>
      <span th:if="${comment.staffReply}" 
            class="badge bg-success">운영자</span>
      
      <!-- 원글 보기 버튼 -->
      <a th:href="@{/counsel/detail/{id}(id=${comment.post.id})}" 
         class="btn btn-sm btn-outline-primary">
        원글 보기
      </a>
    </div>
  </div>
</div>
```

#### mypage.html (빠른 메뉴 추가)
```html
<!-- 빠른 메뉴 -->
<div class="row mb-4">
  <div class="col-md-6 mb-3">
    <a th:href="@{/mypage/my-posts}">
      <div class="card border-primary">
        <div class="card-body text-center">
          <i class="bi bi-file-earmark-text fs-1 text-primary"></i>
          <h5 class="mt-3">내 게시글</h5>
          <p class="text-muted">작성한 온라인상담 게시글을 확인하세요</p>
        </div>
      </div>
    </a>
  </div>
  <div class="col-md-6 mb-3">
    <a th:href="@{/mypage/my-comments}">
      <div class="card border-success">
        <div class="card-body text-center">
          <i class="bi bi-chat-dots fs-1 text-success"></i>
          <h5 class="mt-3">내 댓글</h5>
          <p class="text-muted">작성한 댓글을 확인하세요</p>
        </div>
      </div>
    </a>
  </div>
</div>
```

---

## 📊 수정/생성된 파일

| 파일 | 작업 | 주요 변경사항 |
|------|------|-------------|
| **CounselPostRepository.java** | 수정 | findByAuthorNameOrderByCreatedAtDesc() 추가 |
| **CounselCommentRepository.java** | 수정 | findByAuthorNameOrderByCreatedAtDesc() 추가 |
| **UserService.java** | 수정 | getMyPosts(), getMyComments() 추가 |
| **MyPageController.java** | 수정 | myPosts(), myComments() 엔드포인트 추가 |
| **my-posts.html** | 생성 | 내 게시글 목록 뷰 |
| **my-comments.html** | 생성 | 내 댓글 목록 뷰 |
| **mypage.html** | 수정 | 빠른 메뉴 추가 |

**총 7개 파일 수정/생성**

---

## 🎨 UI 특징

### 1. 내 게시글 목록
- ✅ 테이블 형태로 깔끔하게 표시
- ✅ 상태별 배지 (답변대기/답변완료/상담종료)
- ✅ 공개여부 배지 (공개/비공개)
- ✅ 첨부파일 아이콘 표시
- ✅ 페이징 지원 (10개씩)
- ✅ 제목 클릭 시 상세 화면으로 이동

### 2. 내 댓글 목록
- ✅ 리스트 형태로 표시
- ✅ 어느 게시글의 댓글인지 표시
- ✅ 답글 여부 배지
- ✅ 운영자 댓글 배지
- ✅ 원글 보기 버튼
- ✅ 페이징 지원 (10개씩)

### 3. 마이페이지 빠른 메뉴
- ✅ 카드 형태의 시각적 메뉴
- ✅ 아이콘으로 직관적 표시
- ✅ Hover 효과

---

## 🔍 데이터 흐름

```
[사용자 요청]
   ↓
[MyPageController]
   ├─ /mypage/my-posts → myPosts()
   └─ /mypage/my-comments → myComments()
   ↓
[UserService]
   ├─ getMyPosts() → CounselPostRepository
   └─ getMyComments() → CounselCommentRepository
   ↓
[Repository]
   ├─ findByAuthorNameOrderByCreatedAtDesc()
   └─ Spring Data JPA 쿼리 자동 생성
   ↓
[Database]
   ├─ SELECT * FROM counsel_post WHERE author_name = ?
   └─ SELECT * FROM counsel_comment WHERE author_name = ?
   ↓
[DTO 변환]
   ├─ CounselPostMapper::toDto
   └─ CounselComment 엔티티 그대로
   ↓
[뷰 렌더링]
   ├─ my-posts.html
   └─ my-comments.html
```

---

## ✅ 검증 완료

### 컴파일 확인
```bash
.\gradlew.bat compileJava
# BUILD SUCCESSFUL
```

✅ 컴파일 성공  
✅ 의존성 문제 없음  
✅ 문법 오류 없음

### 기능 검증 (서버 실행 시)
- [ ] 로그인 후 마이페이지 접속
- [ ] 내 게시글 목록 조회
- [ ] 내 댓글 목록 조회
- [ ] 페이징 동작 확인
- [ ] 게시글/댓글 상세 이동

---

## 🚀 향후 개선 사항

### 1. 검색 기능
- 내 게시글에서 제목/내용 검색
- 날짜 범위 검색

### 2. 필터링
- 상태별 필터 (답변대기/답변완료/상담종료)
- 공개/비공개 필터

### 3. 정렬
- 작성일 오름차순/내림차순
- 조회수순 정렬

### 4. 통계
- 총 게시글 수 표시
- 총 댓글 수 표시
- 상태별 게시글 수

---

## 📈 프로젝트 진행 상황

### 완료된 단계
1. ✅ Uppy 파일 업로드 버그 수정
2. ✅ UI 일관성 개선
3. ✅ 비밀번호 찾기 기능
4. ✅ 마이페이지 - 내 게시글/댓글 관리

### 다음 단계
5. ⏳ 조회수 중복 방지 (세션/쿠키 기반)
6. ⏳ 검색 기능 강화 (고급 검색)
7. ⏳ 파일 다운로드 권한 검증
8. ⏳ 이메일 발송 기능 (비밀번호 찾기)

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20

