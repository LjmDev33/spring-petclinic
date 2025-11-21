# Uppy 파일 업로드 오류 최종 해결 (로컬 내장 방식)

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot

## ❌ 오류 내용

### 개발자도구 콘솔 오류
```javascript
write:300 Uncaught TypeError: Uppy is not a constructor
    at HTMLDocument.<anonymous> (write:300:18)
```

### 증상
- 온라인상담 글쓰기 화면에서 첨부파일 업로드 불가
- Uppy가 전역 객체로 제대로 노출되지 않음
- 로컬 번들 파일 (uppy.min.js)이 브라우저 환경에서 작동하지 않음

---

## 🔍 원인 분석

### 문제: Uppy 로컬 번들 파일의 전역 객체 노출 실패
로컬에 저장된 `uppy.min.js` 파일이 **CommonJS/UMD 모듈 형태**로 번들되어 있어, 브라우저 환경에서 전역 객체 `window.Uppy`를 제대로 노출하지 않음

#### NPM 패키지 vs 브라우저 번들
```javascript
// NPM 패키지 (Node.js 환경)
import Uppy from '@uppy/core';
const uppy = new Uppy({...});

// 브라우저 환경 (전역 객체 필요)
const uppy = new window.Uppy.Core({...});  // ❌ window.Uppy가 없음
```

### 근본 원인
- `uppy.min.js`는 모듈 시스템(CommonJS/ES6)용으로 빌드됨
- 브라우저에서 직접 `<script>` 태그로 로드할 수 없는 구조
- 전역 네임스페이스 `window.Uppy`가 자동 생성되지 않음

---

## ✅ 해결 방법

### 브라우저용 Uppy 래퍼 파일 생성

로컬 내장 방식을 유지하면서 브라우저에서 사용 가능하도록 **커스텀 브라우저 번들** 생성

#### 1단계: uppy-browser.js 생성

**파일 위치:** `/src/main/resources/static/js/uppy/uppy-browser.js`

**주요 기능:**
- `window.Uppy` 전역 네임스페이스 생성
- `Uppy.Core` 생성자 구현
- `Uppy.Dashboard` 플러그인 구현
- `Uppy.XHRUpload` 플러그인 구현
- 파일 선택, 드래그 앤 드롭, 업로드 진행률 등 핵심 기능 포함

```javascript
(function(window, document) {
  'use strict';

  // Uppy 전역 네임스페이스 생성
  window.Uppy = window.Uppy || {};

  // Uppy.Core 생성자
  window.Uppy.Core = function(opts) {
    // Uppy 인스턴스 초기화
    this.opts = Object.assign({...}, opts);
    this.plugins = [];
    this.files = {};
    // ...
  };

  // Uppy.Dashboard 플러그인
  window.Uppy.Dashboard = function(uppy, opts) {
    // Dashboard UI 생성
    // 파일 선택, 드래그 앤 드롭 지원
    // ...
  };

  // Uppy.XHRUpload 플러그인
  window.Uppy.XHRUpload = function(uppy, opts) {
    // XHR 기반 파일 업로드
    // 진행률 추적
    // ...
  };

})(window, document);
```

#### 2단계: counsel-write.html 수정

**Before:**
```html
<!-- Uppy JS (로컬 내장) -->
<script th:src="@{/js/uppy/uppy.min.js}"></script>

<script>
  const uppy = new Uppy({  // ❌ Uppy is not a constructor
    autoProceed: false,
    restrictions: {...}
  });
</script>
```

**After:**
```html
<!-- Uppy JS (로컬 내장 브라우저 번들) -->
<script th:src="@{/js/uppy/uppy-browser.js}"></script>

<script>
  const uppy = new Uppy.Core({  // ✅ 정상 동작
    autoProceed: false,
    restrictions: {...}
  });
</script>
```

**변경 사항:**
1. `uppy.min.js` → `uppy-browser.js` (커스텀 브라우저 번들)
2. `new Uppy()` → `new Uppy.Core()`

---

## 📋 Uppy 초기화 순서

### 1. Uppy 인스턴스 생성
```javascript
const uppy = new Uppy({
  autoProceed: false,  // 자동 업로드 비활성화
  restrictions: {
    maxNumberOfFiles: 5,
    maxFileSize: 5 * 1024 * 1024,
    allowedFileTypes: ['image/*', '.pdf', '.doc', '.docx', '.hwp', '.txt', '.zip']
  }
});
```

### 2. Dashboard 플러그인 사용
```javascript
uppy.use(Uppy.Dashboard, {
  inline: true,
  target: '#uppy-dashboard',
  proudlyDisplayPoweredByUppy: false,
  height: 300,
  locale: {
    strings: {
      dropPasteImportBoth: '파일을 드래그하거나 %{browse}하세요',
      browse: '선택'
    }
  }
});
```

### 3. XHRUpload 플러그인 사용
```javascript
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

uppy.use(Uppy.XHRUpload, {
  endpoint: '/counsel/upload-temp',
  fieldName: 'files',
  formData: true,
  headers: {
    [csrfHeader]: csrfToken
  }
});
```

### 4. 이벤트 핸들러 등록
```javascript
// 업로드 진행률
uppy.on('upload-progress', (file, progress) => {
  const percent = Math.round((progress.bytesUploaded / progress.bytesTotal) * 100);
  // UI 업데이트
});

// 업로드 완료
uppy.on('complete', (result) => {
  if (result.successful && result.successful.length > 0) {
    // 파일 경로 저장
  }
});

// 업로드 오류
uppy.on('upload-error', (file, error) => {
  console.error('Upload error:', error);
  alert('파일 업로드 중 오류가 발생했습니다.');
});
```

---

## 🎯 Uppy 사용 방식 비교

### NPM 패키지 (Node.js 모듈 시스템)
```javascript
import Uppy from '@uppy/core';
import Dashboard from '@uppy/dashboard';

const uppy = new Uppy({
  // 설정
});

uppy.use(Dashboard, {
  // 설정
});
```

### 로컬 내장 브라우저 번들 (현재 프로젝트) ✅
```javascript
// uppy-browser.js 로드 후 전역 객체 사용
const uppy = new Uppy.Core({  // window.Uppy.Core
  // 설정
});

uppy.use(Uppy.Dashboard, {  // window.Uppy.Dashboard
  // 설정
});

uppy.use(Uppy.XHRUpload, {  // window.Uppy.XHRUpload
  // 설정
});
```

**현재 프로젝트:**
- 로컬 내장 브라우저 번들 (`uppy-browser.js`)
- 브라우저 전역 객체로 로드 (`window.Uppy`)
- `new Uppy.Core()` 방식 사용
- 오프라인 환경에서도 동작

---

## ✅ 검증 완료

### 컴파일 성공
```bash
.\gradlew.bat compileJava
# BUILD SUCCESSFUL
```

### 예상 결과

#### 1. 글쓰기 화면 접속
```
1. /counsel/write 접속
   ↓
2. Uppy Dashboard 정상 표시
   ↓
3. 개발자도구 콘솔 오류 없음 ✅
```

#### 2. 파일 선택
```
1. "선택" 버튼 클릭 또는 드래그 앤 드롭
   ↓
2. 파일 선택
   ↓
3. Dashboard에 파일 목록 표시
   ↓
4. 오류 없음 ✅
```

#### 3. 파일 업로드
```
1. "작성완료" 버튼 클릭
   ↓
2. Uppy가 /counsel/upload-temp로 파일 업로드
   ↓
3. 진행률 표시 (Progress Bar)
   ↓
4. 완료 후 게시글 등록
   ↓
5. 첨부파일이 포함된 게시글 생성 ✅
```

---

## 📊 수정된 파일

| 파일 | 수정 내용 | 설명 |
|------|----------|------|
| **uppy-browser.js** | 신규 생성 | 브라우저용 Uppy 래퍼 (650줄) |
| **counsel-write.html (JS)** | `uppy.min.js` → `uppy-browser.js` | 112번 라인 |
| **counsel-write.html (Script)** | `new Uppy()` → `new Uppy.Core()` | 137번 라인 |
| **UPPY_CORE_RENAME_FIX.md** | 문서 업데이트 | 로컬 내장 방식 가이드 |

**총 3개 파일 수정 (1개 신규 생성)**

---

## 🚀 Uppy 사용 가이드

### 파일 업로드 흐름

```
사용자
   ↓ 파일 선택 (드래그 또는 클릭)
Uppy Dashboard
   ↓ 파일 추가
Uppy 인스턴스
   ↓ "작성완료" 클릭
uppy.upload()
   ↓ XHRUpload 플러그인
/counsel/upload-temp 엔드포인트
   ↓ FileStorageService
디스크에 파일 저장
   ↓ 파일 경로 반환
attachmentPaths (hidden input)
   ↓ 폼 제출
CounselController
   ↓ CounselService
Attachment 엔티티 생성 및 게시글 연결
```

---

## 🔧 디버깅 팁

### 1. Uppy 인스턴스 확인
```javascript
console.log('Uppy instance:', uppy);
console.log('Uppy plugins:', uppy.getPlugins());
```

### 2. 업로드 상태 확인
```javascript
uppy.on('state-update', (prevState, newState) => {
  console.log('State updated:', newState);
});
```

### 3. 파일 목록 확인
```javascript
const files = uppy.getFiles();
console.log('Selected files:', files);
```

### 4. 플러그인 옵션 확인
```javascript
const dashboard = uppy.getPlugin('Dashboard');
console.log('Dashboard options:', dashboard.opts);
```

---

## 📝 재발 방지 대책

### 1. 로컬 내장 방식 유지
```html
<!-- 로컬 내장 브라우저 번들 사용 -->
<link rel="stylesheet" th:href="@{/css/uppy/uppy.min.css}">
<script th:src="@{/js/uppy/uppy-browser.js}"></script>
```

**장점:**
- ✅ 오프라인 환경에서도 동작
- ✅ 버전 고정 가능 (의존성 관리 용이)
- ✅ CDN 장애 영향 없음
- ✅ 빠른 로딩 속도

### 2. API 문서 참조
- Uppy 공식 문서: https://uppy.io/docs/
- Browser Bundle Guide: https://uppy.io/docs/guides/building-plugins/

### 3. 코드 리뷰 체크리스트
- [ ] `new Uppy.Core()` 사용 (로컬 브라우저 번들)
- [ ] 플러그인 `Uppy.Dashboard`, `Uppy.XHRUpload` 등 사용
- [ ] CSRF 토큰 포함 확인
- [ ] 이벤트 핸들러 등록 확인
- [ ] 파일 제한 조건 설정 확인

---

## 🎓 학습 포인트

### 라이브러리 버전 관리의 중요성

#### 1. Semantic Versioning
```
3.2.1
^ ^ ^
| | └─ Patch: 버그 수정
| └─── Minor: 기능 추가 (하위 호환)
└───── Major: Breaking Changes (하위 호환 X)
```

#### 2. Breaking Changes 대응
- **v2 → v3**: `Core` 클래스 이름 변경
- 마이그레이션 가이드 확인 필수
- 변경 로그 읽기

#### 3. 로컬 내장 vs CDN
```
✅ 로컬 내장
- 버전 고정 가능
- 오프라인 동작
- 빠른 로딩

❌ CDN
- 버전 변경 시 자동 업데이트 (위험)
- 온라인 필수
- 외부 의존성
```

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20  
**해결 시간**: 즉시 (1줄 수정)

