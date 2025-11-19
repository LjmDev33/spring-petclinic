# Uppy 파일 업로드 라이브러리 설치 가이드

> **작성일**: 2025-11-14  
> **작성자**: Jeongmin Lee  
> **목적**: Uppy를 프로젝트에 내장하여 오프라인 환경에서도 사용 가능하도록 설정

---

## 📌 왜 Uppy를 내장해야 하나요?

### 문제점
- ❌ **CDN 사용 금지**: 프로젝트 규칙상 외부 CDN 절대 사용 불가
- ❌ **오프라인 실행 불가**: CDN 의존 시 인터넷 없이 실행 불가능
- ❌ **WebJars 없음**: Uppy는 공식 WebJars가 제공되지 않음

### 해결책
- ✅ **로컬 내장**: `src/main/resources/static/js/uppy/` 폴더에 직접 파일 추가
- ✅ **오프라인 지원**: 프로젝트 내부에서 모든 리소스 제공
- ✅ **버전 고정**: 특정 버전 파일을 직접 관리

---

## 🚀 Uppy 설치 방법

### 1단계: Uppy 다운로드

#### 방법 A: npm을 통한 다운로드 (권장)

```bash
# Node.js가 설치되어 있는 경우
cd C:\eGovFrameDev-3.9.0-64bit\petclinic\spring-petclinic

# 임시 폴더 생성
mkdir temp-uppy
cd temp-uppy

# npm 초기화
npm init -y

# Uppy 패키지 설치
npm install @uppy/core@3.9.3
npm install @uppy/dashboard@3.7.4
npm install @uppy/xhr-upload@3.6.5

# 설치된 파일 확인
dir node_modules\@uppy\
```

#### 방법 B: 공식 웹사이트에서 직접 다운로드

1. **Uppy 공식 사이트 접속**
   - https://uppy.io/docs/
   - https://github.com/transloadit/uppy/releases

2. **필요한 파일 다운로드**
   - `@uppy/core` (필수)
   - `@uppy/dashboard` (UI)
   - `@uppy/xhr-upload` (파일 업로드)

3. **압축 해제**
   - 각 패키지의 `dist/` 폴더 내용 확인

---

### 2단계: 프로젝트에 파일 복사 ✅ 완료

#### 실제 설치된 디렉토리 구조

```
src/main/resources/static/
├── js/uppy/
│   ├── core/              (30개 파일 - Uppy Core 모듈)
│   │   ├── index.js
│   │   ├── Uppy.js
│   │   ├── BasePlugin.js
│   │   └── ... (기타 Core 파일들)
│   ├── dashboard/         (52개 파일 - Dashboard UI)
│   │   ├── index.js
│   │   └── components/... (UI 컴포넌트들)
│   ├── xhr-upload/        (XHR Upload 모듈)
│   │   └── index.js
│   ├── style.css
│   └── style.min.css
└── css/uppy/
    ├── uppy.min.css       (Dashboard 스타일)
    └── style.min.css      (Core 스타일)
```

**주의**: Uppy는 ES Module 방식으로 제공되므로 `import` 문을 사용해야 합니다.

#### 파일 복사 명령어 (Windows)

```cmd
REM 디렉토리 생성
cd C:\eGovFrameDev-3.9.0-64bit\petclinic\spring-petclinic\src\main\resources\static
mkdir js\uppy
mkdir css\uppy

REM npm으로 설치한 경우 (temp-uppy 폴더에서)
cd C:\eGovFrameDev-3.9.0-64bit\petclinic\spring-petclinic\temp-uppy

REM Core 파일 복사
copy node_modules\@uppy\core\dist\uppy.min.js ..\src\main\resources\static\js\uppy\

REM Dashboard 파일 복사
copy node_modules\@uppy\dashboard\dist\uppy.dashboard.min.js ..\src\main\resources\static\js\uppy\
copy node_modules\@uppy\dashboard\dist\style.min.css ..\src\main\resources\static\css\uppy\uppy.min.css

REM XHR Upload 파일 복사
### 3단계: HTML에서 Uppy 사용 (ES Module 방식)

REM 임시 폴더 삭제 (선택)
cd ..
rmdir /s /q temp-uppy
```

---

### 3단계: HTML에서 Uppy 사용

#### 기본 사용 예시

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="ko">
  <!-- Uppy JS (ES Module) -->
  <script type="module">
    // ES Module 방식 import
    import { Uppy } from '/js/uppy/core/index.js';
    import Dashboard from '/js/uppy/dashboard/index.js';
    import XHRUpload from '/js/uppy/xhr-upload/index.js';
  <link rel="stylesheet" th:href="@{/css/uppy/uppy.min.css}">
<body>
    const uppy = new Uppy({

  <!-- Uppy JS -->
  <script th:src="@{/js/uppy/uppy.min.js}"></script>
  <script th:src="@{/js/uppy/uppy.dashboard.min.js}"></script>
  <script th:src="@{/js/uppy/uppy.xhr-upload.min.js}"></script>

  <script>
    .use(Dashboard, {
    const uppy = new Uppy.Core({
      autoProceed: false,
      restrictions: {
        maxFileSize: 10 * 1024 * 1024, // 10MB
      proudlyDisplayPoweredByUppy: false,
      locale: {
        strings: {
          dropPasteImportBoth: '파일을 드래그하거나 %{browse}하세요',
          browse: '선택'
        }
      }
        allowedFileTypes: ['image/*', '.pdf', '.doc', '.docx']
    .use(XHRUpload, {
      endpoint: '/counsel/upload',  // 업로드 엔드포인트
    .use(Uppy.Dashboard, {
      target: '#drag-drop-area',
      inline: true,
      height: 350,
      width: '100%',
      proudlyDisplayPoweredByUppy: false
      console.log('업로드 성공:', file.name, response);
    .use(Uppy.XHRUpload, {
      endpoint: '/upload',
      fieldName: 'files',
      formData: true
    });
      alert('파일 업로드 중 오류가 발생했습니다.');

    // 업로드 성공 이벤트
### 파일 존재 여부 확인 ✅ 완료
      console.log('업로드 성공:', file.name);
    });

#### 주의사항
- **ES Module 필수**: `<script type="module">` 사용 필수
- **경로 주의**: `/js/uppy/core/index.js` (절대 경로)
**실제 설치된 파일**:

    // 업로드 에러 이벤트
  - core/ (30개 파일)
  - dashboard/ (52개 파일)
  - xhr-upload/ (모듈 파일들)
  - style.css
  - style.min.css
  </script>
</body>
</html>
  - style.min.css
```

---

## 🔍 설치 확인

### 파일 존재 여부 확인

```cmd
dir C:\eGovFrameDev-3.9.0-64bit\petclinic\spring-petclinic\src\main\resources\static\js\uppy\
dir C:\eGovFrameDev-3.9.0-64bit\petclinic\spring-petclinic\src\main\resources\static\css\uppy\
```

**예상 출력**:
```
js/uppy/
  - uppy.min.js
  - uppy.dashboard.min.js
  - uppy.xhr-upload.min.js

css/uppy/
  - uppy.min.css
```

### 브라우저에서 확인

1. 서버 실행 후 개발자 도구 열기 (F12)
2. Network 탭에서 Uppy 파일 로드 확인:
   - `http://localhost:8080/js/uppy/uppy.min.js` → 200 OK
   - `http://localhost:8080/css/uppy/uppy.min.css` → 200 OK

---

## 📦 권장 버전

| 패키지 | 버전 | 파일 크기 (대략) |
|--------|------|------------------|
| @uppy/core | 3.9.3 | ~150KB |
| @uppy/dashboard | 3.7.4 | ~200KB |
| @uppy/xhr-upload | 3.6.5 | ~50KB |

---

## ⚠️ 주의사항

### 1. 버전 호환성
- Uppy Core와 플러그인 버전이 호환되는지 확인
- 공식 문서에서 버전 호환성 테이블 참고

### 2. 파일명 주의
- 일부 패키지는 `uppy.core.min.js` 대신 `uppy.min.js` 사용
- 파일명이 다르면 HTML에서 경로 수정 필요

### 3. CDN 절대 금지
```html
<!-- ❌ 절대 사용 금지 -->
<link href="https://cdn.jsdelivr.net/npm/@uppy/core@3.9.3/dist/style.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/@uppy/core@3.9.3/dist/uppy.min.js"></script>

<!-- ✅ 올바른 방법 -->
<link rel="stylesheet" th:href="@{/css/uppy/uppy.min.css}">
<script th:src="@{/js/uppy/uppy.min.js}"></script>
```

---

## 🔧 문제 해결

### 문제 1: "Uppy is not defined" 에러
**원인**: Uppy Core 파일이 로드되지 않음

**해결**:
1. 파일 경로 확인: `/js/uppy/uppy.min.js` 존재 여부
2. HTML에서 스크립트 순서 확인: Core → Dashboard → XHRUpload

### 문제 2: 파일 업로드 실패
**원인**: 백엔드 엔드포인트가 설정되지 않음

**해결**:
1. Controller에서 `/upload` 엔드포인트 구현
- [x] `src/main/resources/static/js/uppy/` 폴더 생성
- [x] `core/` 모듈 복사 (30개 파일)
- [x] `dashboard/` 모듈 복사 (52개 파일)
- [x] `xhr-upload/` 모듈 복사
- [x] `src/main/resources/static/css/uppy/` 폴더 생성
- [x] `uppy.min.css` 파일 복사
- [ ] HTML에서 Uppy ES Module 로드 확인
1. `/css/uppy/uppy.min.css` 파일 존재 확인
- [ ] 백엔드 업로드 엔드포인트 구현
2. 브라우저 개발자 도구에서 404 에러 확인
3. Thymeleaf 경로 문법 확인: `th:href="@{/css/uppy/uppy.min.css}"`

---

## 📚 참고 자료

- **Uppy 공식 문서**: https://uppy.io/docs/
| 2025-11-14 | Jeongmin Lee | 실제 설치 완료, ES Module 방식 적용 |
- **GitHub 저장소**: https://github.com/transloadit/uppy
- **NPM 패키지**: https://www.npmjs.com/package/@uppy/core

---

## ✅ 체크리스트

설치 완료 후 아래 항목을 확인하세요:

- [ ] `src/main/resources/static/js/uppy/` 폴더 생성
- [ ] `uppy.min.js` 파일 복사
- [ ] `uppy.dashboard.min.js` 파일 복사
- [ ] `uppy.xhr-upload.min.js` 파일 복사
- [ ] `src/main/resources/static/css/uppy/` 폴더 생성
- [ ] `uppy.min.css` 파일 복사
- [ ] HTML에서 Uppy 스크립트 로드 확인
- [ ] 브라우저에서 파일 로드 확인 (Network 탭)
- [ ] 파일 업로드 테스트 완료

---

## 📝 업데이트 이력

| 일자 | 작성자 | 내용 |
|------|--------|------|
| 2025-11-14 | Jeongmin Lee | 최초 작성 |

