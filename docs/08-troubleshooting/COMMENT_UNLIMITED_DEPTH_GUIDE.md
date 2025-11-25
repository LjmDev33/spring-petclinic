# 🔧 댓글 시스템 무제한 Depth 구현 가이드

## 📅 작성 일자: 2025-11-25

---

## ✅ **문제 해결 완료**

### **문제 1: 댓글 삭제 시 403 Forbidden 오류** ✅

#### 원인
- 댓글 삭제 폼에 CSRF 토큰 누락

#### 해결
```html
<form id="deleteCommentForm" method="post">
  <!-- CSRF 토큰 추가 -->
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
  ...
</form>
```

---

## 🎯 **문제 2: 무제한 Depth 댓글 구현**

### ✅ **현재 상태: 이미 무제한 Depth 지원 가능!**

현재 `CounselComment` 엔티티는 **자기 참조(Self-Referencing)** 구조로 되어 있어 **별도의 테이블 추가 없이** 무제한 depth를 지원합니다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private CounselComment parent; // 부모 댓글 (대댓글의 경우)
```

---

## 📊 **현재 구조 분석**

### **테이블 구조 (counsel_comment)**

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | 댓글 ID (PK) |
| post_id | BIGINT | 게시글 ID (FK) |
| **parent_id** | BIGINT | **부모 댓글 ID (FK, nullable)** |
| content | TEXT | 댓글 내용 |
| author_name | VARCHAR(100) | 작성자 이름 |
| author_email | VARCHAR(120) | 작성자 이메일 |
| password_hash | VARCHAR(100) | 비밀번호 해시 |
| is_staff_reply | BOOLEAN | 운영자 답변 여부 |
| created_at | DATETIME | 생성 일시 |
| updated_at | DATETIME | 수정 일시 |
| del_flag | BOOLEAN | 삭제 플래그 |
| deleted_at | DATETIME | 삭제 일시 |
| deleted_by | VARCHAR(60) | 삭제한 사용자 |

### **핵심 필드: parent_id**

- **NULL**: 최상위 댓글 (1-depth)
- **숫자**: 부모 댓글의 ID를 가리킴 (2-depth 이상)

이 구조로 무제한 depth가 가능합니다!

---

## 🏗️ **데이터 구조 예시**

### **예시 1: 3-depth 댓글**

```sql
-- 게시글 ID: 2

-- 1번 댓글 (최상위)
INSERT INTO counsel_comment (id, post_id, parent_id, content, author_name) 
VALUES (1, 2, NULL, '첫 번째 댓글', '사용자1');

-- 2번 댓글 (최상위)
INSERT INTO counsel_comment (id, post_id, parent_id, content, author_name) 
VALUES (2, 2, NULL, '두 번째 댓글', '사용자2');

-- 3번 댓글 (2번 댓글의 대댓글)
INSERT INTO counsel_comment (id, post_id, parent_id, content, author_name) 
VALUES (3, 2, 2, '2번 댓글에 대한 답글', '관리자');

-- 4번 댓글 (3번 댓글의 대댓글 = 2번의 대대댓글)
INSERT INTO counsel_comment (id, post_id, parent_id, content, author_name) 
VALUES (4, 2, 3, '3번 댓글에 대한 답글', '사용자3');
```

### **시각화**

```
게시글 #2
│
├── 댓글 #1 (parent_id = NULL)
│   "첫 번째 댓글" - 사용자1
│
└── 댓글 #2 (parent_id = NULL)
    "두 번째 댓글" - 사용자2
    │
    └── 댓글 #3 (parent_id = 2)
        "2번 댓글에 대한 답글" - 관리자
        │
        └── 댓글 #4 (parent_id = 3)
            "3번 댓글에 대한 답글" - 사용자3
```

---

## 🔍 **현재 구현 상태**

### ✅ **이미 구현된 기능**

1. **무제한 depth 지원**
   - `parent_id` 필드로 자기 참조
   - 재귀적 구조 지원

2. **UI에서 depth 표시**
   - 최상위 댓글: 좌측 정렬
   - 대댓글: 48px 들여쓰기 + 파란색 테두리

3. **답글 작성 기능**
   - 모든 댓글에 "답글" 버튼
   - 대댓글 작성 시 `parent_id` 자동 설정

---

## 🚀 **개선 방안**

### **문제점: UI 렌더링 개선 필요**

현재 Thymeleaf 템플릿에서는 **평면적(Flat) 데이터**를 받아서 렌더링합니다:

```java
List<CounselCommentDto> comments = counselService.getCommentsForPost(postId);
```

이 구조는:
- ✅ 간단하고 빠름
- ❌ depth가 깊어질수록 UI에서 정렬 순서가 복잡해짐

---

## 📋 **구현 방안 제시**

### **방안 1: 계층 구조(Tree) 변환 (권장)** ⭐

#### **장점**
- 댓글 순서가 자연스러움 (부모-자식 순서 유지)
- UI에서 depth 표시 용이
- 대댓글이 부모 댓글 바로 아래 표시

#### **단점**
- 서버에서 Tree 구조로 변환 필요
- 약간의 성능 오버헤드

#### **구현 예시**

**1. DTO에 children 필드 추가**

```java
public class CounselCommentDto {
    private Long id;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private Long parentId;
    private String parentAuthorName;
    
    // 추가: 자식 댓글 목록
    private List<CounselCommentDto> children = new ArrayList<>();
    private int depth = 0; // 깊이 (0 = 최상위)
    
    // getter/setter...
}
```

**2. Service에서 Tree 구조 생성**

```java
public List<CounselCommentDto> getCommentsTree(Long postId) {
    List<CounselComment> allComments = commentRepository.findByPost_IdOrderByCreatedAtAsc(postId);
    
    // DTO로 변환
    Map<Long, CounselCommentDto> commentMap = new HashMap<>();
    List<CounselCommentDto> rootComments = new ArrayList<>();
    
    for (CounselComment comment : allComments) {
        CounselCommentDto dto = CounselCommentMapper.toDto(comment);
        commentMap.put(dto.getId(), dto);
        
        if (comment.getParent() == null) {
            // 최상위 댓글
            rootComments.add(dto);
        } else {
            // 대댓글: 부모의 children에 추가
            CounselCommentDto parent = commentMap.get(comment.getParent().getId());
            if (parent != null) {
                parent.getChildren().add(dto);
                dto.setDepth(parent.getDepth() + 1);
            }
        }
    }
    
    return rootComments;
}
```

**3. Thymeleaf 재귀 렌더링**

```html
<!-- 재귀 프래그먼트 정의 -->
<th:block th:fragment="commentTree(comments, depth)">
  <div th:each="c : ${comments}">
    <!-- 댓글 카드 -->
    <div th:style="'margin-left: ' + ${depth * 48} + 'px; ...'">
      <div class="card">
        <div class="card-body">
          <p th:text="${c.authorName}"></p>
          <p th:text="${c.content}"></p>
          <button class="reply-btn" th:attr="data-comment-id=${c.id}">답글</button>
        </div>
      </div>
    </div>
    
    <!-- 자식 댓글 재귀 렌더링 -->
    <th:block th:if="${!#lists.isEmpty(c.children)}" 
              th:replace="~{:: commentTree(${c.children}, ${depth + 1})}">
    </th:block>
  </div>
</th:block>

<!-- 최상위 댓글부터 시작 -->
<th:block th:replace="~{:: commentTree(${comments}, 0)}"></th:block>
```

---

### **방안 2: 평면 구조 + 정렬 개선 (현재 방식)**

#### **장점**
- 구현이 간단
- 쿼리가 단순

#### **단점**
- 댓글 순서가 부자연스러울 수 있음
- depth가 깊어지면 정렬 복잡

#### **개선 방안**

**1. SQL에서 정렬 개선**

```java
// 계층 순서 쿼리 (MySQL 8.0+ WITH RECURSIVE 사용)
@Query(value = "WITH RECURSIVE comment_tree AS ( " +
    "  SELECT id, post_id, parent_id, content, author_name, created_at, 0 as depth, " +
    "         CAST(id AS CHAR(200)) as path " +
    "  FROM counsel_comment " +
    "  WHERE post_id = :postId AND parent_id IS NULL " +
    "  UNION ALL " +
    "  SELECT c.id, c.post_id, c.parent_id, c.content, c.author_name, c.created_at, " +
    "         ct.depth + 1, CONCAT(ct.path, '-', c.id) " +
    "  FROM counsel_comment c " +
    "  INNER JOIN comment_tree ct ON c.parent_id = ct.id " +
    ") " +
    "SELECT * FROM comment_tree ORDER BY path", nativeQuery = true)
List<CounselComment> findCommentsTreeByPostId(@Param("postId") Long postId);
```

---

## 🎯 **권장 구현 순서**

### **Phase 1: 빠른 개선 (현재 구조 유지)** ⚡

1. ✅ CSRF 토큰 추가 (완료)
2. 댓글 정렬 개선 (SQL 쿼리 수정)
3. UI에서 depth 시각화 개선

**예상 시간**: 1~2시간

---

### **Phase 2: 완전한 Tree 구조** 🌳

1. DTO에 `children` 필드 추가
2. Service에서 Tree 구조 생성 로직 구현
3. Thymeleaf 재귀 렌더링 구현
4. CSS에서 depth별 스타일 정의

**예상 시간**: 4~6시간

---

## 💡 **추천 방안**

### **단기 (지금 바로)**: Phase 1 구현
- CSRF 토큰 추가 (✅ 완료)
- SQL 정렬 개선으로 자연스러운 댓글 순서

### **중장기 (다음 버전)**: Phase 2 구현
- Tree 구조로 완전한 계층형 댓글 시스템
- 대댓글이 부모 바로 아래 표시
- 무제한 depth 완벽 지원

---

## 🔄 **현재 상태 요약**

| 항목 | 상태 | 비고 |
|------|------|------|
| 테이블 구조 | ✅ 완료 | parent_id로 무제한 depth 지원 |
| 댓글 삭제 403 오류 | ✅ 해결 | CSRF 토큰 추가 |
| 답글 작성 기능 | ✅ 완료 | 모든 댓글에 답글 가능 |
| UI 들여쓰기 | ✅ 완료 | 48px per depth |
| Tree 구조 변환 | ⏳ 대기 | Phase 2에서 구현 권장 |
| 재귀 렌더링 | ⏳ 대기 | Phase 2에서 구현 권장 |

---

## 📚 **참고 자료**

### **Self-Referencing (자기 참조) 패턴**
- JPA에서 `@ManyToOne`으로 자기 자신을 참조
- 트리 구조 데이터 표현에 최적

### **Tree 구조 쿼리**
- MySQL 8.0+: `WITH RECURSIVE`
- PostgreSQL: `WITH RECURSIVE`
- Oracle: `CONNECT BY PRIOR`

### **Thymeleaf 재귀**
- `th:replace`와 `th:fragment`를 이용한 재귀 렌더링
- depth를 파라미터로 전달

---

## ✅ **체크리스트**

### 즉시 해결됨
- [x] 댓글 삭제 403 오류 (CSRF 토큰 추가)

### 이미 지원됨
- [x] 무제한 depth 테이블 구조
- [x] 답글 작성 기능
- [x] UI 들여쓰기

### 향후 개선 가능
- [ ] Tree 구조 변환
- [ ] 재귀 렌더링
- [ ] SQL 정렬 최적화

---

**작성자**: GitHub Copilot (AI Assistant)  
**작성 일시**: 2025-11-25  
**상태**: ✅ 문제 1 해결 완료, 문제 2 구현 가이드 제공

