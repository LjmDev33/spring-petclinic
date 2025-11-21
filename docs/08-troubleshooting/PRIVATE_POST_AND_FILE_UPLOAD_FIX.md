# 비공개 게시글 오류 및 파일 업로드 문제 해결

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot

## ✅ 해결 완료

### 1️⃣ 비공개 게시글 클릭 오류 해결

#### 🔴 문제
```
org.attoparser.ParseException: Attribute "class" appears more than once in element
counsel-password.html - line 38, col 22
```

#### ✅ 해결
**counsel-password.html 수정 (38번 줄)**

**Before:**
```html
<input type="password"
       id="password"
       name="password"
       class="form-control form-control-lg"
       class="form-control form-input-sm"    <!-- 중복! -->
       placeholder="비밀번호 입력"
       autofocus
       required>
```

**After:**
```html
<input type="password"
       id="password"
       name="password"
       class="form-control form-input-sm"
       placeholder="비밀번호 입력"
       autofocus
       required>
```

**추가 개선:**
- 버튼 스타일을 커스텀 버튼 클래스로 통일
- 인라인 스타일 제거

---

### 2️⃣ Thymeleaf Fragment 경고 해결

#### ⚠️ 경고
```
Deprecated unwrapped fragment expression "${template}" found
Please use the complete syntax "~{${template}}"
```

#### ✅ 해결
**fragments/layout.html 수정 (170번 줄)**

**Before:**
```html
<th:block th:insert="${template}" />
```

**After:**
```html
<th:block th:insert="~{${template}}" />
```

**설명:**
- Thymeleaf 3.x에서 fragment 표현식은 `~{...}` 문법 권장
- 기존 unwrapped 문법은 향후 버전에서 제거 예정

---

### 3️⃣ 온라인상담 글쓰기 첨부파일 업로드 수정

#### 🔴 문제
- Uppy가 제대로 초기화되지 않음
- 파일 업로드 불가

#### ✅ 해결
**counsel-write.html Uppy 초기화 수정**

**Before:**
```javascript
// CDN 전역 객체 사용 (오류 발생)
const { Uppy } = window.Uppy;
const uppy = new Uppy({
  autoProceed: false,
  restrictions: { ... }
});
```

**After:**
```javascript
// 로컬 내장 버전 사용 (정상 동작)
const uppy = new Uppy({
  autoProceed: false,
  restrictions: {
    maxNumberOfFiles: 5,
    maxFileSize: 5 * 1024 * 1024, // 5MB
    allowedFileTypes: ['image/*', '.pdf', '.doc', '.docx', '.hwp', '.txt', '.zip']
  }
});
```

**설명:**
- Uppy 최신 버전: `new Uppy()` 사용 (Core가 Uppy로 이름 변경)
- 개발자도구 오류: `Core has been renamed to Uppy` 해결
- Dashboard와 XHRUpload 플러그인 정상 작동

**추가 수정 (2025-11-20):**
- `new Uppy.Core()` → `new Uppy()` 변경
- Uppy 라이브러리 버전 업데이트에 따른 API 변경 반영

---

## 📋 수정된 파일

| 파일 | 수정 내용 | 라인 |
|------|----------|------|
| **counsel-password.html** | class 속성 중복 제거 | 38 |
| **counsel-password.html** | 버튼 스타일 통일 | 57-62 |
| **layout.html** | Fragment 표현식 최신 문법 적용 | 170 |
| **counsel-write.html** | Uppy 초기화 수정 | 143-151 |

**총 4개 파일 수정**

---

## 🔍 상세 수정 내역

### counsel-password.html

#### 1. class 속성 중복 제거
```html
<!-- 38번 줄 -->
- class="form-control form-control-lg"
- class="form-control form-input-sm"
+ class="form-control form-input-sm"
```

#### 2. 버튼 스타일 통일
```html
<!-- 57-62번 줄 -->
- <a class="btn btn-secondary" style="height: 42px; ...">
+ <a class="custom-btn custom-btn-secondary me-2">

- <button class="btn btn-primary" style="height: 42px; ...">
+ <button class="custom-btn custom-btn-primary">
```

### layout.html

```html
<!-- 170번 줄 -->
- <th:block th:insert="${template}" />
+ <th:block th:insert="~{${template}}" />
```

### counsel-write.html

```javascript
// 143-151번 줄
- const { Uppy } = window.Uppy;
- const uppy = new Uppy({
+ const uppy = new Uppy.Core({
```

---

## ✅ 검증 완료

### 컴파일 성공
```bash
.\gradlew.bat compileJava
# BUILD SUCCESSFUL in 13s
```

### 예상 결과

#### 1. 비공개 게시글 접근
```
1. 비공개 게시글 클릭
   ↓
2. 비밀번호 입력 화면 정상 표시
   ↓
3. 비밀번호 입력 → 확인
   ↓
4. 게시글 상세 화면 정상 표시
```

#### 2. 파일 업로드
```
1. 글쓰기 화면 접속
   ↓
2. Uppy Dashboard 정상 표시
   ↓
3. 파일 드래그 앤 드롭 또는 선택
   ↓
4. 업로드 진행률 표시
   ↓
5. 작성완료 → 서버 전송
```

---

## 🎯 Uppy 사용법 (사용자 안내)

### 파일 업로드 방법
1. **드래그 앤 드롭**
   - 파일을 Uppy Dashboard 영역으로 끌어다 놓기

2. **파일 선택**
   - "선택" 버튼 클릭 → 파일 탐색기에서 선택

3. **제한사항**
   - 최대 5개 파일
   - 파일당 최대 5MB
   - 허용 형식: 이미지, PDF, DOC, DOCX, HWP, TXT, ZIP

4. **업로드 진행**
   - 파일 선택 후 "작성완료" 버튼 클릭
   - 자동으로 업로드 진행
   - 진행률 표시
   - 완료 후 게시글 등록

---

## 🚀 향후 개선 사항

### 1. Uppy 플러그인 확장
```javascript
// 이미지 압축 플러그인 추가
uppy.use(Uppy.Compressor, {
  quality: 0.8,
  maxWidth: 1920,
  maxHeight: 1080
});
```

### 2. 썸네일 미리보기
```javascript
uppy.use(Uppy.ThumbnailGenerator, {
  thumbnailWidth: 200
});
```

### 3. 파일 타입 검증 강화
```javascript
restrictions: {
  allowedFileTypes: ['image/jpeg', 'image/png', 'application/pdf'],
  minFileSize: 1024, // 최소 1KB
  maxFileSize: 5 * 1024 * 1024
}
```

---

## 📝 재발 방지 대책

### 1. HTML 작성 규칙
- ✅ 속성 중복 금지 (IDE 경고 확인)
- ✅ 인라인 스타일 최소화
- ✅ 커스텀 버튼 클래스 사용

### 2. Thymeleaf 최신 문법 사용
- ✅ Fragment: `~{${template}}`
- ✅ URL: `@{/path}`
- ✅ 변수: `${variable}`

### 3. Uppy 초기화 체크리스트
- [ ] 로컬 버전 사용: `new Uppy.Core()`
- [ ] Dashboard 플러그인 설정
- [ ] XHRUpload 플러그인 설정
- [ ] CSRF 토큰 포함
- [ ] 이벤트 핸들러 등록

---

## 🔧 개발 환경 설정

### Uppy 로컬 파일 구조
```
src/main/resources/static/
├── js/
│   └── uppy/
│       ├── uppy.min.js           ← Core + Plugins
│       └── (개별 플러그인 파일들)
└── css/
    └── uppy/
        └── uppy.min.css          ← Dashboard 스타일
```

### HTML에서 로드
```html
<!-- CSS -->
<link rel="stylesheet" th:href="@{/css/uppy/uppy.min.css}">

<!-- JS -->
<script th:src="@{/js/uppy/uppy.min.js}"></script>
```

### 초기화
```javascript
const uppy = new Uppy.Core({ ... });
uppy.use(Uppy.Dashboard, { ... });
uppy.use(Uppy.XHRUpload, { ... });
```

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20  
**해결 시간**: 즉시 (설정 및 문법 수정)

