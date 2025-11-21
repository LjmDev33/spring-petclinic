# Uppy 파일 업로드 로컬 내장 방식 구현

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot  
**카테고리:** 기능 개선

---

## 📝 개요

온라인상담 글쓰기 화면에서 Uppy 파일 업로드가 작동하지 않는 문제를 **로컬 내장 방식**으로 해결

---

## ❌ 문제점

### 오류 내용
```javascript
write:300 Uncaught TypeError: Uppy is not a constructor
    at HTMLDocument.<anonymous> (write:300:18)
```

### 원인
- 로컬 `uppy.min.js` 파일이 CommonJS/UMD 모듈 형태로 번들됨
- 브라우저에서 `<script>` 태그로 직접 로드 시 전역 객체 `window.Uppy`가 생성되지 않음
- NPM 패키지 구조로 되어 있어 모듈 시스템 없이는 사용 불가

---

## ✅ 해결 방법

### 1. 브라우저용 Uppy 래퍼 파일 생성

**파일:** `/src/main/resources/static/js/uppy/uppy-browser.js` (신규 생성, 650줄)

**주요 기능:**
```javascript
(function(window, document) {
  'use strict';

  // 1. window.Uppy 전역 네임스페이스 생성
  window.Uppy = window.Uppy || {};

  // 2. Uppy.Core 생성자 구현
  window.Uppy.Core = function(opts) {
    this.opts = {...};
    this.plugins = [];
    this.files = {};
    // 파일 관리, 이벤트 처리 등
  };

  // 3. Uppy.Dashboard 플러그인
  window.Uppy.Dashboard = function(uppy, opts) {
    // 파일 선택 UI
    // 드래그 앤 드롭
    // 파일 목록 표시
  };

  // 4. Uppy.XHRUpload 플러그인
  window.Uppy.XHRUpload = function(uppy, opts) {
    // XHR 기반 파일 업로드
    // 진행률 추적
    // CSRF 토큰 헤더 설정
  };

})(window, document);
```

### 2. counsel-write.html 수정

**변경 전:**
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

**변경 후:**
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

---

## 🎯 구현 세부 사항

### Uppy.Core 생성자

**기능:**
- 파일 추가/제거
- 파일 ID 생성
- 상태 관리
- 이벤트 시스템 (on/emit)
- 플러그인 등록 (use)
- 업로드 실행

**주요 메서드:**
```javascript
// 파일 추가
uppy.addFile({
  name: 'example.jpg',
  type: 'image/jpeg',
  data: fileBlob,
  size: 1024000
});

// 파일 가져오기
const file = uppy.getFile(fileId);
const allFiles = uppy.getFiles();

// 파일 제거
uppy.removeFile(fileId);

// 업로드 실행
uppy.upload().then(result => {
  console.log('Successful:', result.successful);
  console.log('Failed:', result.failed);
});

// 이벤트 등록
uppy.on('file-added', file => { ... });
uppy.on('upload-progress', (file, progress) => { ... });
uppy.on('complete', result => { ... });
```

### Uppy.Dashboard 플러그인

**기능:**
- 파일 선택 버튼 UI
- 드래그 앤 드롭 영역
- 파일 목록 표시
- 파일별 진행률 표시
- 파일 삭제 버튼

**UI 구조:**
```html
<div class="uppy-Dashboard">
  <div class="uppy-Dashboard-inner">
    <!-- 드래그 앤 드롭 힌트 -->
    <div class="uppy-Dashboard-dropFilesHereHint">
      파일을 여기에 드롭하세요
    </div>
    
    <!-- 파일 선택 버튼 -->
    <div class="uppy-DashboardContent-bar">
      <button class="uppy-Dashboard-browse">선택</button>
    </div>
    
    <!-- 파일 목록 -->
    <div class="uppy-Dashboard-files">
      <!-- 동적으로 생성됨 -->
    </div>
    
    <!-- Hidden 파일 입력 -->
    <input type="file" class="uppy-Dashboard-input" multiple>
  </div>
</div>
```

### Uppy.XHRUpload 플러그인

**기능:**
- XHR 기반 파일 업로드
- 진행률 이벤트 처리
- CSRF 토큰 헤더 자동 추가
- 에러 처리

**업로드 흐름:**
```
파일 선택
   ↓
FormData 생성
   ↓
CSRF 토큰 헤더 설정
   ↓
XHR 전송
   ↓ (진행 중)
upload-progress 이벤트 발생
   ↓ (완료)
upload-success 이벤트 발생
   ↓
complete 이벤트 발생
```

---

## 📊 수정된 파일

| 파일 | 변경 내용 | 라인/크기 |
|------|----------|----------|
| **uppy-browser.js** | 신규 생성 | 650줄 |
| **counsel-write.html** | uppy.min.js → uppy-browser.js | 112번 라인 |
| **counsel-write.html** | new Uppy() → new Uppy.Core() | 137번 라인 |
| **UPPY_CORE_RENAME_FIX.md** | 문서 업데이트 | - |

---

## 🔍 테스트 시나리오

### 1. 파일 선택 버튼
```
1. /counsel/write 접속
   ↓
2. "선택" 버튼 클릭
   ↓
3. 파일 선택 다이얼로그 표시
   ↓
4. 파일 선택 (최대 5개)
   ↓
5. Dashboard에 파일 목록 표시 ✅
```

### 2. 드래그 앤 드롭
```
1. /counsel/write 접속
   ↓
2. 파일을 Dashboard 영역으로 드래그
   ↓
3. 드롭 힌트 표시
   ↓
4. 파일 드롭
   ↓
5. Dashboard에 파일 목록 추가 ✅
```

### 3. 파일 업로드
```
1. 파일 선택 완료
   ↓
2. "작성완료" 버튼 클릭
   ↓
3. uppy.upload() 실행
   ↓
4. XHRUpload로 /counsel/upload-temp 호출
   ↓
5. 진행률 표시 (Progress Bar)
   ↓
6. 서버 응답 (파일 경로)
   ↓
7. complete 이벤트 발생
   ↓
8. 게시글 등록 ✅
```

### 4. 제한 조건 검증
```
✅ 최대 5개 파일
✅ 최대 5MB per file
✅ 허용 파일 타입: image/*, .pdf, .doc, .docx, .hwp, .txt, .zip
✅ 제한 초과 시 alert 표시
```

---

## 🎓 기술적 의의

### 1. 오프라인 환경 지원
- CDN 없이 로컬 번들만으로 동작
- 외부 네트워크 의존성 제거
- 안정적인 서비스 제공

### 2. 버전 고정 및 제어
- 라이브러리 버전 완전 제어
- 예상치 못한 업데이트 방지
- 프로젝트 의존성 명확화

### 3. 커스터마이징 가능
- 소스 코드 직접 수정 가능
- 프로젝트 요구사항에 맞춤 구현
- 디버깅 및 문제 해결 용이

---

## 🚀 향후 개선 방향

### 1. 파일 미리보기
```javascript
// 이미지 썸네일 표시
if (file.type.startsWith('image/')) {
  const reader = new FileReader();
  reader.onload = (e) => {
    // 썸네일 생성
  };
  reader.readAsDataURL(file.data);
}
```

### 2. 파일 유효성 검사 강화
```javascript
// MIME 타입 검증
// 파일 확장자 검증
// 파일 내용 검증 (매직 넘버)
```

### 3. 업로드 재시도 기능
```javascript
uppy.on('upload-error', (file, error) => {
  // 자동 재시도 로직
  uppy.retryUpload(file.id);
});
```

### 4. 진행률 UI 개선
```javascript
// 파일별 상세 진행률
// 전체 진행률 표시
// 예상 남은 시간 계산
```

---

## 📚 참고 문서

- [Uppy 공식 문서](https://uppy.io/docs/)
- [Building Plugins Guide](https://uppy.io/docs/guides/building-plugins/)
- [UPPY_CORE_RENAME_FIX.md](../08-troubleshooting/UPPY_CORE_RENAME_FIX.md)

---

**작성 시간:** 1시간  
**난이도:** ★★★☆☆  
**중요도:** ★★★★☆

