# 좋아요 기능 UI 개선 - 아코디언 방식 적용

**작성일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: UI/UX 개선

---

## 📋 변경 요약

3개 게시판(Counsel, Community, Photo)의 좋아요 기능을 **아코디언 방식**으로 개선하여 사용자 경험을 향상시켰습니다.

---

## ✅ 주요 변경 사항

### 1. **탭 순서 변경 (Counsel만 해당)**
- **이전**: 좋아요 탭 먼저 표출
- **변경**: 답변 탭 먼저 표출

### 2. **좋아요 UI 구조 변경 (3개 게시판 공통)**

**이전 구조**:
```
[❤️ 좋아요 (5)] ← 탭 클릭 시 패널 전환
```

**변경 후 구조**:
```
┌─────────────────────────────────┐
│ ❤️ 좋아요 (5)              ▼   │ ← 헤더 영역
├─────────────────────────────────┤
│ (접힌 상태 - 패널 숨김)         │
└─────────────────────────────────┘

화살표(▼) 클릭 시:

┌─────────────────────────────────┐
│ ❤️ 좋아요 (5)              ▲   │
├─────────────────────────────────┤
│ 📌 이 글에 공감한 사용자        │
│ [홍길동] [김철수] [이영희] ... │ ← 패널 펼쳐짐
└─────────────────────────────────┘
```

**기능 분리**:
- **하트(❤️) 클릭**: 좋아요 상태 토글 (`♡` ⟷ `❤️`), 카운트 변경
- **화살표(▼) 클릭**: 좋아요 누른 사용자 목록 패널 펼침/접힘 (아코디언)

---

## 📝 상세 변경 내역

### 1. Counsel (온라인상담) 게시판

**파일**: `counselDetail.html`

**변경 사항**:
1. 탭 구조에서 아코디언 구조로 변경
2. 답변 탭 먼저 표출 (`class="nav-link active"`)
3. 좋아요는 탭 외부에 독립된 아코디언 패널로 배치

**HTML 구조**:
```html
<!-- 좋아요 헤더 (li 태그, 탭 아님) -->
<li class="nav-item" role="presentation">
  <div class="d-flex align-items-center">
    <!-- 하트: toggleLike(event) -->
    <span onclick="toggleLike(event)">
      <i id="likeIcon"></i>
    </span>
    <!-- 화살표: Bootstrap collapse -->
    <button data-bs-toggle="collapse" data-bs-target="#likeCollapsePanel">
      <i class="fa fa-chevron-down"></i>
    </button>
  </div>
</li>

<!-- 답변 탭 (active) -->
<li class="nav-item" role="presentation">
  <button class="nav-link active" id="comment-tab">
    답변 (3)
  </button>
</li>

<!-- 좋아요 아코디언 패널 (탭 외부) -->
<div class="collapse" id="likeCollapsePanel">
  <!-- 좋아요 사용자 목록 -->
</div>
```

**JavaScript**:
```javascript
function toggleLike(event) {
  // 이벤트 전파 방지 (아코디언과 독립)
  if (event) {
    event.stopPropagation();
    event.preventDefault();
  }
  // AJAX 요청...
}
```

---

### 2. Community (공지사항) 게시판

**파일**: `noticeDetail.html`

**변경 사항**:
1. 탭 구조 제거 (공지사항은 답변 없음)
2. 좋아요만 단독 아코디언 형식으로 표시

**HTML 구조**:
```html
<div class="border rounded">
  <!-- 좋아요 헤더 -->
  <div class="d-flex align-items-center px-3 py-2 bg-light">
    <span onclick="toggleLike(postId, event)">
      <i id="likeIcon"></i>
    </span>
    <span>좋아요 (5)</span>
    <button data-bs-toggle="collapse" data-bs-target="#likeCollapsePanel">
      <i class="fa fa-chevron-down"></i>
    </button>
  </div>

  <!-- 아코디언 패널 -->
  <div class="collapse" id="likeCollapsePanel">
    <!-- 좋아요 사용자 목록 -->
  </div>
</div>
```

---

### 3. Photo (포토게시판)

**파일**: `photoDetail.html`

**변경 사항**:
1. Community와 동일한 구조 적용 (답변 없음)
2. 좋아요 사용자 목록은 아직 미구현 (TODO 표시)

**HTML 구조**:
```html
<!-- Community와 동일 -->
<div class="border rounded">
  <div class="d-flex align-items-center px-3 py-2 bg-light">
    <!-- 하트 + 화살표 -->
  </div>
  <div class="collapse" id="likeCollapsePanel">
    <!-- TODO: 좋아요 사용자 목록 기능 준비 중 -->
  </div>
</div>
```

---

## 🎨 UI/UX 개선 효과

### 1. **공간 효율성 향상**
- 좋아요 사용자 목록이 필요할 때만 펼쳐짐
- 답변(댓글) 영역을 먼저 확인 가능

### 2. **사용자 의도에 맞는 동작**
- 하트 클릭 = 좋아요 토글 (즉각적 피드백)
- 화살표 클릭 = 누가 좋아요 눌렀는지 궁금할 때만 확인

### 3. **일관된 경험**
- 3개 게시판 모두 동일한 UI/UX
- 사용자 학습 곡선 감소

---

## 🔧 기술적 개선 사항

### 1. **이벤트 전파 방지**
```javascript
function toggleLike(event) {
  if (event) {
    event.stopPropagation();  // 부모 요소로 전파 방지
    event.preventDefault();    // 기본 동작 방지
  }
}
```

**이유**:
- 하트를 클릭해도 아코디언이 펼쳐지지 않음
- 화살표를 클릭해도 좋아요가 토글되지 않음

### 2. **Bootstrap Collapse 활용**
```html
<button data-bs-toggle="collapse" data-bs-target="#likeCollapsePanel">
  <i class="fa fa-chevron-down"></i>
</button>

<div class="collapse" id="likeCollapsePanel">
  <!-- 내용 -->
</div>
```

**장점**:
- 추가 JavaScript 불필요
- Bootstrap 내장 애니메이션 사용
- 접근성(ARIA) 자동 처리

---

## 📊 변경 전후 비교

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| **좋아요 표시** | 탭 안 패널 | 독립 아코디언 |
| **공간 활용** | 항상 표출 (공간 낭비) | 필요 시 펼침 (효율적) |
| **답변 우선 순위** | 좋아요 먼저 (Counsel) | 답변 먼저 |
| **하트 클릭** | 탭 전환 (혼란) | 좋아요 토글 (명확) |
| **사용자 목록** | 항상 표시 | 화살표 클릭 시 표시 |

---

## ✅ 검증 결과

- ✅ 컴파일 성공
- ✅ Counsel 게시판: 답변 탭 먼저 표출 확인
- ✅ Community 게시판: 아코디언 방식 적용
- ✅ Photo 게시판: 아코디언 방식 적용
- ✅ 하트 클릭 → 좋아요 토글 (이벤트 전파 방지)
- ✅ 화살표 클릭 → 패널 펼침/접힘

---

## 📝 향후 작업

### 1. Photo 게시판 좋아요 사용자 목록 구현
- PhotoService에 `getLikedUsernames()` 추가
- PhotoController에 User 정보 조회 로직 추가
- photoDetail.html에 사용자 목록 UI 추가

### 2. 아코디언 상태 저장 (선택사항)
- 사용자가 패널을 펼친 상태를 localStorage에 저장
- 페이지 새로고침 후에도 상태 유지

### 3. 애니메이션 개선 (선택사항)
- 화살표 아이콘이 펼쳐짐/접힘에 따라 회전
- CSS transition 추가

---

## 📚 관련 문서

- [Community 좋아요 기능 고도화](./2025-11-28-community-like-enhancement.md)
- [UI 일관성 가이드](../05-ui-screens/UI_CONSISTENCY_GUIDE.md)

---

**변경 이력**:
- 2025-11-28: 3개 게시판 좋아요 기능 아코디언 방식 적용 완료

