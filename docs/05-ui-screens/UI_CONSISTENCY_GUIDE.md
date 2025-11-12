# 🎨 UI/UX 일관성 가이드

**작성일**: 2025-11-11  
**작성자**: Jeongmin Lee  
**문서 버전**: 1.0  
**목적**: 프로젝트 전체에서 일관된 UI/UX 제공

---

## 📋 목차
1. [개요](#개요)
2. [반응형 디자인 원칙](#반응형-디자인-원칙) ⭐NEW
3. [버튼 크기 규칙](#버튼-크기-규칙)
4. [폰트 크기 규칙](#폰트-크기-규칙)
5. [간격(Spacing) 규칙](#간격spacing-규칙)
6. [입력 필드 규칙](#입력-필드-규칙)
7. [색상 사용 규칙](#색상-사용-규칙)
8. [체크리스트](#체크리스트)

---

## 개요

### 목적
- 모든 페이지에서 일관된 사용자 경험 제공
- 개발자 간 협업 시 UI 통일성 유지
- 새로운 기능 추가 시 빠른 적용 가능

### 핵심 원칙
```
✅ 일관성: 동일한 요소는 동일한 스타일
✅ 직관성: 사용자가 설명 없이도 이해 가능
✅ 피드백: 사용자 액션에 즉각적인 반응
✅ 접근성: 모바일 포함 다양한 환경 지원
```

---

## 반응형 디자인 원칙 ⭐NEW (2025-11-12)

### 목적
- 모든 디바이스(PC, 태블릿, 모바일)에서 일관된 사용자 경험 제공
- Bootstrap Grid 시스템을 활용한 레이아웃 구성
- 미디어 쿼리를 통한 세밀한 UI 조정

### Bootstrap Breakpoint 기준
```
xs (Extra Small): < 576px   - 모바일 세로
sm (Small):       ≥ 576px   - 모바일 가로
md (Medium):      ≥ 768px   - 태블릿
lg (Large):       ≥ 992px   - 작은 데스크톱
xl (Extra Large): ≥ 1200px  - 큰 데스크톱
xxl:              ≥ 1400px  - 와이드 모니터
```

### 반응형 컬럼 구조 규칙

#### 1. 검색 폼 레이아웃 (권장)
```html
<!-- ✅ 올바른 반응형 구조 -->
<div class="container-fluid px-0">
  <div class="row g-2">
    <!-- 제목 영역: 모바일 100%, 태블릿 이상 30% -->
    <div class="col-12 col-md-3 d-flex align-items-center">
      <h2>온라인상담(<span th:text="${page.totalElements}">112</span>)</h2>
    </div>
    
    <!-- 빈 공간: 모바일에서 숨김, 태블릿 이상에서 40% -->
    <div class="col-md-5 d-none d-md-block"></div>
    
    <!-- 검색 폼: 모바일 100%, 태블릿 이상 30% -->
    <div class="col-12 col-md-4">
      <form th:action="@{/counsel/list}" method="get">
        <div class="row g-2">
          <div class="col-4">
            <select class="form-select" name="type">
              <option value="title">제목</option>
              <option value="author">글쓴이</option>
            </select>
          </div>
          <div class="col-8">
            <div class="input-group">
              <input type="text" class="form-control" name="keyword" placeholder="Search">
              <button class="btn btn-outline-secondary" type="submit">
                <i class="fa fa-search"></i>
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</div>
```

**핵심 포인트**:
- `g-2`: row에 간격(gutter) 2 적용
- `d-none d-md-block`: 모바일에서 숨기고 태블릿부터 표시
- `col-12 col-md-X`: 모바일 100%, 태블릿 이상 X/12 비율

#### 2. 버튼 그룹 레이아웃
```html
<!-- ✅ 모바일: 세로 정렬, 데스크톱: 가로 정렬 -->
<div class="d-grid d-md-flex gap-2 justify-content-md-end">
  <a href="/list" class="btn btn-secondary" style="min-width: 120px; height: 42px;">취소</a>
  <button type="submit" class="btn btn-primary" style="min-width: 120px; height: 42px;">저장</button>
</div>
```

**핵심 포인트**:
- `d-grid`: 모바일에서 세로 정렬 (버튼 전체 너비)
- `d-md-flex`: 태블릿부터 가로 정렬
- `justify-content-md-end`: 태블릿부터 오른쪽 정렬

#### 3. 테이블 반응형
```html
<!-- ✅ 테이블 가로 스크롤 -->
<div class="table-responsive">
  <table class="table text-center align-middle">
    <thead>
      <tr>
        <th>번호</th>
        <th>제목</th>
        <th class="d-none d-md-table-cell">글쓴이</th>
        <th class="d-none d-md-table-cell">날짜</th>
        <th>상태</th>
      </tr>
    </thead>
    <tbody>
      <!-- 내용 -->
    </tbody>
  </table>
</div>
```

**핵심 포인트**:
- `table-responsive`: 모바일에서 가로 스크롤
- `d-none d-md-table-cell`: 모바일에서 특정 컬럼 숨김

#### 4. 카드 레이아웃
```html
<!-- ✅ 모바일: 1열, 태블릿: 2열, 데스크톱: 3열 -->
<div class="row g-4">
  <div class="col-12 col-md-6 col-lg-4" th:each="item : ${items}">
    <div class="card">
      <div class="card-body">
        <!-- 카드 내용 -->
      </div>
    </div>
  </div>
</div>
```

### ❌ 피해야 할 패턴

#### 1. 중첩된 input-group
```html
<!-- ❌ 불필요한 중첩 -->
<form th:action="@{/counsel/list}" method="get" class="input-group">
  <div class="input-group">
    <div class="col-12 col-md-4">
      <select class="form-select form-select-md" name="type">
        <!-- ... -->
      </select>
    </div>
    <div class="col-12 col-md-8">
      <div class="input-group">  <!-- 불필요 -->
        <input type="text" class="form-control" name="keyword">
        <button class="btn btn-outline-secondary" type="submit">
          <i class="fa fa-search"></i>
        </button>
      </div>
    </div>
  </div>
</form>
```

**문제점**:
- `input-group` 3단 중첩
- 모바일에서 레이아웃 깨짐
- 불필요한 마진/패딩 발생

#### 2. 고정 너비 사용
```html
<!-- ❌ 모바일에서 레이아웃 깨짐 -->
<div style="width: 800px;">
  <!-- 콘텐츠 -->
</div>

<!-- ✅ 반응형 너비 -->
<div class="container">
  <!-- 콘텐츠 -->
</div>
```

#### 3. 불규칙한 간격
```html
<!-- ❌ mt-md-2, mb-md-2 혼재 -->
<div class="col-12 col-md-3 mt-md-2">
  <h2>제목</h2>
</div>
<div class="col-12 col-md-4 mb-md-2">
  <!-- 폼 -->
</div>

<!-- ✅ 일관된 간격 (row에 g-2) -->
<div class="row g-2">
  <div class="col-12 col-md-3">
    <h2>제목</h2>
  </div>
  <div class="col-12 col-md-4">
    <!-- 폼 -->
  </div>
</div>
```

### 모바일 우선 개발 (Mobile First)

#### 기본 원칙
```
1. 기본 스타일: 모바일 기준으로 작성
2. 미디어 쿼리: 큰 화면으로 확장
3. 콘텐츠 우선: 중요한 콘텐츠 먼저 배치
```

#### 적용 예시
```html
<!-- 기본: 모바일 (col-12) -->
<!-- 태블릿: col-md-6 (50%) -->
<!-- 데스크톱: col-lg-4 (33.3%) -->
<div class="col-12 col-md-6 col-lg-4">
  <!-- 콘텐츠 -->
</div>
```

### 테스트 체크리스트

**새 페이지 개발 시 테스트**:
- [ ] 모바일 세로 (< 576px) 레이아웃 확인
- [ ] 모바일 가로 (576px ~ 767px) 레이아웃 확인
- [ ] 태블릿 (768px ~ 991px) 레이아웃 확인
- [ ] 데스크톱 (≥ 992px) 레이아웃 확인
- [ ] 버튼/폼 요소 터치 가능 크기 (최소 44px)
- [ ] 텍스트 가독성 (최소 16px)
- [ ] 가로 스크롤 발생하지 않음

**크롬 개발자 도구 활용**:
```
F12 → Device Toolbar (Ctrl+Shift+M)
테스트 기기:
- iPhone SE (375px)
- iPad (768px)
- Desktop (1920px)
```

---

## 버튼 크기 규칙

### 기본 규칙
```
일반 버튼:        height: 42px
주요 액션 버튼:   min-width: 120px; height: 42px
상세화면 버튼:    min-width: 80px (수정/삭제 붙여서)
전체 너비 버튼:   width: 100%; height: 42px (d-grid)
```

### 적용 예시

#### 1. 로그인/회원가입 페이지
```html
<!-- 전체 너비 버튼 -->
<div class="d-grid gap-2">
  <button type="submit" class="btn btn-primary" style="height: 42px;">
    <i class="bi bi-box-arrow-in-right"></i> 로그인
  </button>
</div>
```

#### 2. 마이페이지/수정 페이지
```html
<!-- 주요 액션 버튼 (오른쪽 정렬) -->
<div class="d-flex justify-content-end gap-2">
  <a href="/list" class="btn btn-secondary" style="min-width: 120px; height: 42px;">
    <i class="bi bi-arrow-left"></i> 취소
  </a>
  <button type="submit" class="btn btn-primary" style="min-width: 120px; height: 42px;">
    <i class="bi bi-save"></i> 저장
  </button>
</div>
```

#### 3. 상세화면 수정/삭제 버튼
```html
<!-- 붙여서 배치 (gap 없음) -->
<div class="d-flex">
  <a href="/edit" class="btn btn-warning" style="min-width: 80px;">수정</a>
  <button type="button" class="btn btn-danger" style="min-width: 80px;">삭제</button>
</div>
```

#### 4. 댓글 작성 버튼
```html
<!-- 오른쪽 끝 배치 -->
<div class="d-flex justify-content-end">
  <button type="button" class="btn btn-primary" 
          data-bs-toggle="modal" data-bs-target="#commentModal">
    <i class="bi bi-pencil-square"></i> 댓글 작성
  </button>
</div>
```

### ❌ 피해야 할 패턴
```html
<!-- ❌ 크기 불규칙 -->
<button class="btn btn-primary btn-lg">저장</button>
<button class="btn btn-secondary">취소</button>
<button class="btn btn-warning btn-sm">수정</button>

<!-- ❌ min-width 누락 -->
<button class="btn btn-primary" style="height: 42px;">저장</button>
<button class="btn btn-secondary" style="height: 40px;">취소</button>
```

---

## 폰트 크기 규칙

### 기본 규칙
```
헤더 링크:    font-size: 0.95rem
placeholder:  font-size: 0.95rem (축소)
본문:         1rem (기본, 명시 불필요)
제목:         h1~h6 태그 사용 (크기 자동)
```

### 적용 예시

#### 1. 헤더 링크
```html
<div class="d-flex align-items-center">
  <a href="/" class="px-2" style="font-size: 0.95rem;">HOME</a>
  <span class="px-1" style="font-size: 0.95rem;">|</span>
  <a href="/login" class="px-2" style="font-size: 0.95rem;">로그인</a>
  <span class="px-1" style="font-size: 0.95rem;">|</span>
  <span class="px-2 text-success fw-bold" style="font-size: 0.95rem;">
    닉네임
  </span>
</div>
```

#### 2. placeholder
```html
<!-- placeholder 글씨 크기 축소 -->
<input type="password" 
       placeholder="비밀번호를 입력하세요"
       style="font-size: 0.95rem;">
```

#### 3. 안내 문구
```html
<small class="form-text text-muted">
  게시글 작성 시 설정한 비밀번호를 입력하세요.
</small>
```

---

## 간격(Spacing) 규칙

### 기본 규칙
```
링크 좌우 간격:    px-2
구분선(|) 간격:    px-1
버튼 간격:         gap-2
카드 내부 여백:    p-4 또는 p-5
섹션 상단 여백:    mt-4 또는 mt-5
```

### 적용 예시

#### 1. 헤더 링크 간격
```html
<a href="/" class="px-2">HOME</a>
<span class="px-1">|</span>
<a href="/login" class="px-2">로그인</a>
```

#### 2. 버튼 간격
```html
<!-- 버튼 그룹 간격 -->
<div class="d-flex justify-content-end gap-2">
  <button class="btn btn-secondary">취소</button>
  <button class="btn btn-primary">저장</button>
</div>
```

#### 3. 카드 여백
```html
<div class="card shadow-sm">
  <div class="card-body p-5">
    <!-- 카드 내용 -->
  </div>
</div>
```

---

## 입력 필드 규칙

### 1. 필수 필드 표시
```html
<label for="email" class="form-label">
  <i class="bi bi-envelope"></i> 이메일 <span class="text-danger">*</span>
</label>
```

### 2. placeholder + 안내 문구 분리
```html
<!-- ✅ 올바른 방법: placeholder 간단히 + 안내 문구 분리 -->
<input type="email" 
       placeholder="예: abc123@example.com"
       style="font-size: 0.95rem;">
<small class="form-text text-muted">
  <i class="bi bi-info-circle"></i> 
  올바른 형식: abc123@example.com (영문, 숫자, @, 도메인)
</small>

<!-- ❌ 잘못된 방법: placeholder에 모든 내용 -->
<input placeholder="이메일을 입력하세요. 형식은 abc123@example.com입니다. 영문, 숫자, @, 도메인을 사용하세요.">
```

### 3. 실시간 검증 피드백
```html
<!-- 올바른 입력: 초록색 테두리 -->
<input type="email" class="form-control is-valid">

<!-- 잘못된 입력: 빨간색 테두리 -->
<input type="email" class="form-control is-invalid">
```

```javascript
// 실시간 검증 예시
document.getElementById('email').addEventListener('input', function(e) {
  const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  
  if (emailPattern.test(e.target.value)) {
    e.target.classList.add('is-valid');
    e.target.classList.remove('is-invalid');
  } else {
    e.target.classList.remove('is-valid');
    e.target.classList.add('is-invalid');
  }
});
```

---

## 색상 사용 규칙

### Bootstrap 색상 체계
```
Primary (파란색):   주요 액션 (저장, 등록, 확인)
Success (초록색):   성공 메시지, 운영자 표시
Warning (노란색):   경고, 수정
Danger (빨간색):    삭제, 오류
Secondary (회색):   보조 액션 (취소, 목록)
Info (하늘색):      정보, 안내
```

### 적용 예시
```html
<!-- 주요 액션 -->
<button class="btn btn-primary">저장</button>

<!-- 수정 -->
<button class="btn btn-warning">수정</button>

<!-- 삭제 -->
<button class="btn btn-danger">삭제</button>

<!-- 취소 -->
<button class="btn btn-secondary">취소</button>

<!-- 운영자 배지 -->
<span class="badge bg-success">
  <i class="bi bi-shield-check"></i> 운영자
</span>

<!-- 상태 배지 -->
<span class="badge bg-secondary fs-6">답변완료</span>
<span class="badge bg-warning text-dark fs-6">답변대기</span>
```

---

## 체크리스트

### 새 페이지/기능 추가 시 확인 사항

#### HTML/Thymeleaf
- [ ] 버튼 크기 통일 (42px 또는 120px×42px)
- [ ] 폰트 크기 통일 (0.95rem)
- [ ] 간격 통일 (px-2, px-1, gap-2)
- [ ] placeholder 간소화 (font-size: 0.95rem)
- [ ] 안내 문구 `<small>` 태그 사용
- [ ] 필수 필드 `*` 표시
- [ ] 아이콘 + 텍스트 함께 표시
- [ ] 버튼 오른쪽 끝 배치 (justify-content-end)

#### JavaScript
- [ ] 실시간 검증 피드백 (is-valid, is-invalid)
- [ ] 폼 제출 전 검증
- [ ] 에러 메시지 alert 또는 flash 메시지

#### CSS
- [ ] 인라인 스타일 최소화 (공통 클래스 우선)
- [ ] Bootstrap 클래스 활용
- [ ] 커스텀 스타일은 petclinic.css에 추가

---

## 예외 사항

### 크기 조정이 필요한 경우
1. **모바일 환경**: 버튼 크기 확대 가능 (min-height: 48px)
2. **특수 기능**: 아이콘 전용 버튼 (btn-sm 허용)
3. **레거시 페이지**: 단계적 적용 (우선순위: 사용자 페이지 → 관리자 페이지)

### 예외 승인 프로세스
```
1. 팀 리더 승인 필요
2. 문서에 예외 사유 기록
3. CHANGELOG에 이력 남기기
```

---

## 적용 현황

### ✅ 완료된 페이지 (2025-11-12 기준)
1. `login.html` - 로그인 페이지
2. `register.html` - 회원가입 페이지
3. `forgot-password.html` - 비밀번호 찾기 페이지
4. `mypage.html` - 마이페이지
5. `counsel-password.html` - 비공개 게시글 비밀번호 입력
6. `counsel-edit.html` - 게시글 수정 페이지
7. `counselDetail.html` - 게시글 상세 페이지
8. `counselList.html` - 게시글 목록 페이지 ⭐NEW
9. `layout.html` - 헤더/푸터 레이아웃

### 🔄 진행 예정
1. `counsel-write.html` - 게시글 작성 페이지
2. 관리자 페이지들

---

## 참고 자료

### Bootstrap 공식 문서
- [Buttons](https://getbootstrap.com/docs/5.3/components/buttons/)
- [Forms](https://getbootstrap.com/docs/5.3/forms/overview/)
- [Spacing](https://getbootstrap.com/docs/5.3/utilities/spacing/)

### 내부 문서
- `PROJECT_DOCUMENTATION.md` - 프로젝트 전체 규칙
- `QUICK_REFERENCE.md` - 빠른 참조 가이드

---

## 변경 이력

### [1.1] - 2025-11-12 ⭐NEW
- **반응형 디자인 원칙** 섹션 추가
- Bootstrap Grid 시스템 활용 가이드
- 모바일 우선 개발 원칙 정의
- 반응형 레이아웃 예시 및 안티패턴 추가
- `counselList.html` UI 개선 완료
  - 검색 폼 반응형 레이아웃 개선
  - 버튼 크기 일관성 적용 (42px, 120px×42px)
  - placeholder 폰트 크기 조정 (0.95rem)
  - 간격 통일 (g-2, mt-3)

### [1.0] - 2025-11-11
- 최초 문서 작성
- 버튼 크기, 폰트 크기, 간격 규칙 정의
- 8개 페이지 UI/UX 통일 완료

---

**문서 버전**: 1.1  
**최종 검토**: 2025-11-12  
**담당자**: Jeongmin Lee  
**다음 업데이트**: 새 페이지 추가 시 또는 규칙 변경 시

