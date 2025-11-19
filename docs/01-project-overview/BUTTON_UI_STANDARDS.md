# 버튼 UI 통일 규칙

> **작성일**: 2025-11-14  
> **작성자**: Jeongmin Lee  
> **목적**: 프로젝트 전체 버튼 UI/UX 일관성 확보

---

## 1. 버튼 기본 규칙

### 1.1 공통 스타일 속성

모든 버튼은 아래 기본 속성을 준수합니다:

```html
style="height: 38px; min-width: 110px; display: flex; align-items: center; justify-content: center;"
```

| 속성 | 값 | 설명 |
|------|-----|------|
| `height` | `38px` | 모든 버튼 높이 고정 |
| `min-width` | `90px` (탭/필터) ~ `110px` (액션) | 텍스트 길이와 무관하게 최소 폭 보장 |
| `display` | `flex` | flexbox 레이아웃 사용 |
| `align-items` | `center` | 수직 중앙 정렬 |
| `justify-content` | `center` | 수평 중앙 정렬 |

### 1.2 버튼 간 간격

- **수평 배치**: 간격 없음 (gap 제거)
- **flex-wrap**: 반응형 대응 시 `d-flex flex-wrap` 사용

---

## 2. 버튼 유형별 규칙

### 2.1 액션 버튼 (등록/수정/삭제/확인)

**용도**: 사용자 액션을 실행하는 주요 버튼

```html
<button type="submit" class="btn btn-primary" 
        style="height: 38px; min-width: 110px; display: flex; align-items: center; justify-content: center;">
  <i class="bi bi-check me-1"></i> 확인
</button>
```

- `min-width`: **110px**
- 아이콘과 텍스트 간격: `me-1` (margin-end: 4px)

**예시**:
- 글쓰기, 등록, 확인, 저장, 수정, 삭제 등

---

### 2.2 네비게이션 버튼 (목록/취소/뒤로가기)

**용도**: 페이지 이동 및 취소 동작

```html
<a th:href="@{/counsel/list}" class="btn btn-outline-secondary" 
   style="height: 38px; min-width: 110px; display: flex; align-items: center; justify-content: center;">
  <i class="bi bi-list me-1"></i> 목록
</a>
```

- `min-width`: **110px**
- Bootstrap 클래스: `btn-outline-secondary` 또는 `btn-secondary`

**예시**:
- 목록, 취소, 뒤로가기 등

---

### 2.3 탭/필터 버튼 (카테고리/상태 필터)

**용도**: 데이터 필터링 및 탭 전환

```html
<a th:href="@{/faq(category='일반')}" 
   class="btn btn-primary"
   style="height: 38px; min-width: 90px; display: flex; align-items: center; justify-content: center;">
  일반
</a>
```

- `min-width`: **90px**
- 활성 상태: `btn-primary`
- 비활성 상태: `btn-outline-secondary`

**예시**:
- FAQ 카테고리 필터 (전체/일반/진료/예약/수술/기타)
- 상태 필터 (전체/대기/완료/종료)

---

### 2.4 검색 버튼

**용도**: 검색 폼의 제출 버튼

```html
<button class="btn btn-outline-secondary" type="submit" style="height: 38px;">
  <i class="fa fa-search"></i>
</button>
```

- `height`: **38px** (입력 필드와 동일)
- `min-width`: 불필요 (아이콘만 표시)

---

## 3. 버튼 배치 규칙

### 3.1 수평 정렬

```html
<div class="d-flex flex-wrap">
  <button class="btn btn-primary" style="...">버튼1</button>
  <button class="btn btn-secondary" style="...">버튼2</button>
</div>
```

- `d-flex`: flexbox 활성화
- `flex-wrap`: 좁은 화면에서 줄바꿈 허용
- **gap 제거**: 버튼 간 간격 없음

### 3.2 양쪽 정렬 (좌우 분리)

```html
<div class="d-grid d-md-flex justify-content-md-between">
  <div class="d-flex flex-wrap">
    <!-- 왼쪽 버튼들 -->
  </div>
  <!-- 오른쪽 버튼 -->
</div>
```

### 3.3 오른쪽 정렬

```html
<div class="d-grid d-md-flex justify-content-md-end">
  <button class="btn btn-primary" style="...">등록</button>
</div>
```

---

## 4. 반응형 대응

### 4.1 모바일 (< 768px)

- `d-grid`: 전체 폭 버튼
- 여러 버튼이 있을 경우: `flex-wrap`으로 세로 배치

### 4.2 태블릿/데스크탑 (≥ 768px)

- `d-md-flex`: 수평 배치
- `gap-2`: 8px 간격 유지

---

## 5. 적용 페이지 목록

### 5.1 FAQ 게시판

- ✅ `faq/faqList.html`: 카테고리 필터, FAQ 등록 버튼
- ✅ `faq/faqDetail.html`: 목록 버튼

### 5.2 온라인상담 게시판

- ✅ `counsel/counselList.html`: 글쓰기 버튼
- ✅ `counsel/counselDetail.html`: 수정/삭제/목록 버튼
- ✅ `counsel/counsel-write.html`: 목록/등록 버튼
- ✅ `counsel/counsel-edit.html`: 취소/목록/수정 버튼
- ✅ `counsel/counsel-password.html`: 목록/확인 버튼

### 5.3 사용자 페이지

- 🔲 `user/login.html`: 로그인 버튼
- 🔲 `user/register.html`: 회원가입 버튼
- 🔲 `user/mypage.html`: 프로필 저장/비밀번호 변경/홈/로그아웃 버튼
- 🔲 `user/forgot-password.html`: 비밀번호 찾기 버튼

### 5.4 커뮤니티 게시판

- 🔲 `community/noticeList.html`: 글쓰기 버튼
- 🔲 `community/noticeDetail.html`: 수정/삭제/목록 버튼

---

## 6. 예외 처리

### 6.1 아이콘 전용 버튼

검색 버튼처럼 아이콘만 표시하는 경우:

```html
<button class="btn btn-outline-secondary" type="submit" style="height: 42px;">
  <i class="fa fa-search"></i>
</button>
```

- `min-width` 불필요
- `height`만 고정

### 6.2 로그아웃 버튼 (헤더)

헤더 내 텍스트 링크 형태:

```html
<button type="submit" class="btn btn-link px-2 text-decoration-none link-secondary p-0">
  로그아웃
</button>
```

- 일반 버튼 규칙 적용 불가
- 헤더 디자인에 맞춰 별도 스타일 유지

---

## 7. 검증 체크리스트

새 버튼 추가 또는 수정 시 아래 항목을 확인:

- [ ] `height: 38px` 고정
- [ ] `min-width: 90px` (탭/필터) 또는 `110px` (액션/네비게이션)
- [ ] `display: flex; align-items: center; justify-content: center;`
- [ ] 아이콘과 텍스트 간 `me-1` 간격
- [ ] 버튼 그룹 간 gap 제거 (간격 없음)
- [ ] 반응형 대응: `d-flex flex-wrap`

---

## 8. 변경 이력

| 일자 | 작성자 | 내용 |
|------|--------|------|
| 2025-11-14 | Jeongmin Lee | 버튼 UI 통일 규칙 초안 작성 |
| 2025-11-14 | Jeongmin Lee | FAQ/온라인상담 전체 페이지 적용 완료 |

---

## 9. 참고사항

- Bootstrap 5.3 기준
- 아이콘: Bootstrap Icons (`bi-*`) 및 Font Awesome (`fa-*`)
- 브라우저 호환: Chrome, Firefox, Safari, Edge 최신 버전
