# 📊 파일 업로드 Progress Bar 구현 가이드

**프로젝트**: Spring PetClinic  
**버전**: 3.5.3  
**최종 수정일**: 2025-11-11  
**작성자**: Jeongmin Lee

---

## 📋 목차
1. [개요](#개요)
2. [구현 방법 비교](#구현-방법-비교)
3. [순수 JavaScript 구현](#순수-javascript-구현)
4. [라이브러리 추천](#라이브러리-추천)
5. [구현 예정](#구현-예정)

---

## 개요

### 현재 상태
- ⚠️ **미구현** - 파일 업로드 시 진행률 표시 없음
- 사용자가 대용량 파일 업로드 시 진행 상황 확인 불가
- 업로드 완료 여부를 알 수 없어 UX 저하

### 목표
- ✅ 파일 업로드 진행률을 실시간으로 표시
- ✅ 여러 파일 동시 업로드 시 각각의 진행률 표시
- ✅ 업로드 완료/실패 상태 명확히 표시
- ✅ 모바일 환경에서도 정상 작동

---

## 구현 방법 비교

### 1. 순수 JavaScript + XMLHttpRequest

**장점**:
- ✅ 외부 라이브러리 불필요
- ✅ 가볍고 빠름
- ✅ 커스터마이징 자유로움

**단점**:
- ❌ 코드 작성량 많음
- ❌ 크로스 브라우저 호환성 직접 처리
- ❌ 에러 핸들링 복잡

**코드 예시**:
```javascript
const xhr = new XMLHttpRequest();
xhr.upload.addEventListener('progress', (e) => {
  if (e.lengthComputable) {
    const percent = (e.loaded / e.total) * 100;
    progressBar.style.width = percent + '%';
  }
});
xhr.open('POST', '/counsel');
xhr.send(formData);
```

---

### 2. Fetch API + ReadableStream

**장점**:
- ✅ 최신 JavaScript 표준
- ✅ Promise 기반으로 코드 간결
- ✅ async/await 사용 가능

**단점**:
- ❌ 업로드 진행률 추적 어려움 (다운로드만 지원)
- ❌ IE 11 미지원
- ❌ 추가 polyfill 필요

**코드 예시**:
```javascript
// ⚠️ 업로드 진행률은 Fetch API로 불가능
// 다운로드 진행률만 가능
const response = await fetch('/counsel/download/1');
const reader = response.body.getReader();
// ...
```

---

### 3. jQuery File Upload Plugin

**장점**:
- ✅ 매우 안정적 (10년 이상 검증)
- ✅ 풍부한 기능 (드래그 앤 드롭, 썸네일, 다중 파일)
- ✅ IE 10+ 지원
- ✅ 대용량 파일 청크 업로드 지원

**단점**:
- ❌ jQuery 의존성 (100KB 이상)
- ❌ 무거움
- ❌ 현대적인 프로젝트에 부적합

**설치**:
```bash
npm install blueimp-file-upload
```

---

### 4. Uppy (추천 🌟)

**장점**:
- ✅ **현대적이고 가볍다** (모듈화)
- ✅ **반응형 UI** (모바일 최적화)
- ✅ 다양한 업로드 소스 (로컬, URL, Dropbox, Google Drive)
- ✅ **아름다운 기본 UI**
- ✅ TypeScript 지원
- ✅ 활발한 유지보수
- ✅ 청크 업로드 지원

**단점**:
- ❌ 번들 크기 (최소 50KB, UI 포함 시 200KB+)
- ❌ 학습 곡선 있음

**설치**:
```bash
npm install @uppy/core @uppy/dashboard @uppy/xhr-upload
```

**라이센스**: MIT (무료)

---

### 5. Dropzone.js

**장점**:
- ✅ 드래그 앤 드롭 지원
- ✅ 이미지 썸네일 미리보기
- ✅ 가볍다 (30KB)
- ✅ 간단한 API

**단점**:
- ❌ 청크 업로드 미지원
- ❌ 대용량 파일에 부적합

**설치**:
```bash
npm install dropzone
```

---

## 라이브러리 추천

### 🥇 1순위: Uppy

**이유**:
- 현재 프로젝트에 가장 적합 (Bootstrap 5 + 현대적인 UI)
- 반응형 디자인으로 모바일 환경 지원
- 활발한 커뮤니티 및 유지보수
- MIT 라이센스로 상업적 사용 가능

**설치 방법** (Gradle):
```groovy
// build.gradle
dependencies {
    // Uppy via WebJars
    implementation 'org.webjars.npm:uppy__core:3.3.1'
    implementation 'org.webjars.npm:uppy__dashboard:3.3.1'
    implementation 'org.webjars.npm:uppy__xhr-upload:3.3.1'
}
```

**사용 예시** (Thymeleaf):
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <!-- ✅ 로컬 경로 사용 (CDN 사용 금지) -->
  <link rel="stylesheet" th:href="@{/webjars/uppy__core/3.3.1/dist/uppy.min.css}">
  <link rel="stylesheet" th:href="@{/webjars/uppy__dashboard/3.3.1/dist/style.min.css}">
</head>
<body>
  <!-- HTML -->
  <div id="uppy-dashboard"></div>

  <!-- JavaScript -->
  <script th:src="@{/webjars/uppy__core/3.3.1/dist/uppy.min.js}"></script>
  <script th:src="@{/webjars/uppy__dashboard/3.3.1/dist/index.min.js}"></script>
  <script th:src="@{/webjars/uppy__xhr-upload/3.3.1/dist/index.min.js}"></script>

  <script>
  /**
   * Uppy 파일 업로드 초기화
   * - 최대 파일 크기: 5MB
   * - 허용 타입: JPEG, PNG, GIF
   */
  const uppy = new Uppy.Core({
    restrictions: {
      maxFileSize: 5 * 1024 * 1024, // 5MB
      allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif']
    }
  });

  // 대시보드 UI 추가
  uppy.use(Uppy.Dashboard, {
    target: '#uppy-dashboard',
    inline: true,
    height: 300,
    locale: {
      strings: {
        // 한국어 지원
        dropPasteFiles: '파일을 드래그하거나 클릭하여 선택하세요',
        addMore: '파일 추가',
        upload: '업로드',
        cancel: '취소'
      }
    }
  });

  // XHR 업로드 설정
  uppy.use(Uppy.XHRUpload, {
    endpoint: '/counsel',
    fieldName: 'files',
    formData: true
  });

  // 업로드 성공 이벤트
  uppy.on('upload-success', (file, response) => {
    console.log('File uploaded:', file.name);
  });

  // 업로드 실패 이벤트
  uppy.on('upload-error', (file, error) => {
    console.error('Upload failed:', file.name, error);
    alert('업로드 실패: ' + error.message);
  });
  </script>
</body>
</html>
```

**⚠️ CDN 사용 금지**:
- 오프라인 환경에서 실행 불가
- 외부 서버 장애 시 서비스 중단
- 버전 불일치 위험
- **반드시 WebJars 또는 로컬 파일 사용**

---

### 🥈 2순위: 순수 JavaScript

**이유**:
- 외부 의존성 없음
- 프로젝트 번들 크기 최소화
- 완전한 커스터마이징 가능

**구현 코드**:
```javascript
// HTML
<div class="progress" id="uploadProgress" style="display: none;">
  <div class="progress-bar progress-bar-striped progress-bar-animated" 
       role="progressbar" 
       id="uploadProgressBar" 
       style="width: 0%;">0%</div>
</div>

// JavaScript
function uploadWithProgress(formData, url) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const progressBar = document.getElementById('uploadProgressBar');
    const progressContainer = document.getElementById('uploadProgress');
    
    // 진행률 이벤트
    xhr.upload.addEventListener('progress', (e) => {
      if (e.lengthComputable) {
        const percent = Math.round((e.loaded / e.total) * 100);
        progressBar.style.width = percent + '%';
        progressBar.textContent = percent + '%';
        progressContainer.style.display = 'block';
      }
    });
    
    // 완료 이벤트
    xhr.addEventListener('load', () => {
      if (xhr.status === 200) {
        progressBar.classList.remove('progress-bar-animated');
        progressBar.classList.add('bg-success');
        progressBar.textContent = '완료!';
        setTimeout(() => {
          progressContainer.style.display = 'none';
          resolve(xhr.response);
        }, 1000);
      } else {
        progressBar.classList.add('bg-danger');
        progressBar.textContent = '실패';
        reject(new Error('Upload failed'));
      }
    });
    
    // 에러 이벤트
    xhr.addEventListener('error', () => {
      progressBar.classList.add('bg-danger');
      progressBar.textContent = '오류 발생';
      reject(new Error('Network error'));
    });
    
    xhr.open('POST', url);
    xhr.send(formData);
  });
}

// 사용 예시
document.getElementById('uploadForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const formData = new FormData(e.target);
  try {
    await uploadWithProgress(formData, '/counsel');
    alert('업로드 완료!');
    window.location.href = '/counsel/list';
  } catch (error) {
    alert('업로드 실패: ' + error.message);
  }
});
```

---

### 🥉 3순위: Dropzone.js

**이유**:
- 간단하고 가볍다
- 드래그 앤 드롭 지원
- 이미지 썸네일 미리보기

**사용 예시**:
```html
<form action="/counsel" class="dropzone" id="my-dropzone">
  <div class="fallback">
    <input name="files" type="file" multiple />
  </div>
</form>

<script src="https://unpkg.com/dropzone@6/dist/dropzone-min.js"></script>
<link rel="stylesheet" href="https://unpkg.com/dropzone@6/dist/dropzone.css">

<script>
Dropzone.options.myDropzone = {
  maxFilesize: 5, // MB
  acceptedFiles: 'image/jpeg,image/png,image/gif',
  success: function(file, response) {
    console.log('File uploaded:', file.name);
  }
};
</script>
```

---

## 순수 JavaScript 구현

### counsel-write.html 수정 예시

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div class="container mt-4">
  <h2>온라인 상담 글쓰기</h2>
  
  <form id="counselForm" method="post" enctype="multipart/form-data">
    <!-- ...existing fields... -->
    
    <div class="mb-3">
      <label for="files" class="form-label">첨부파일</label>
      <input type="file" id="files" name="files" class="form-control" multiple
             accept="image/jpeg,image/png,image/gif">
      <small class="form-text text-muted">최대 5MB, JPEG/PNG/GIF만 가능</small>
    </div>
    
    <!-- Progress Bar -->
    <div class="progress mt-3" id="uploadProgress" style="display: none; height: 30px;">
      <div class="progress-bar progress-bar-striped progress-bar-animated" 
           role="progressbar" 
           id="uploadProgressBar" 
           style="width: 0%;">
        <span id="uploadProgressText">0%</span>
      </div>
    </div>
    
    <button type="submit" class="btn btn-primary" id="submitBtn">
      <i class="bi bi-send"></i> 등록
    </button>
  </form>
</div>

<script>
document.getElementById('counselForm').addEventListener('submit', async function(e) {
  e.preventDefault();
  
  const formData = new FormData(this);
  const submitBtn = document.getElementById('submitBtn');
  const progressContainer = document.getElementById('uploadProgress');
  const progressBar = document.getElementById('uploadProgressBar');
  const progressText = document.getElementById('uploadProgressText');
  
  // 버튼 비활성화
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 업로드 중...';
  
  // Progress Bar 표시
  progressContainer.style.display = 'block';
  
  try {
    const response = await uploadWithProgress(formData, '/counsel');
    
    // 성공
    progressBar.classList.remove('progress-bar-animated');
    progressBar.classList.add('bg-success');
    progressText.textContent = '완료!';
    
    setTimeout(() => {
      alert('게시글이 등록되었습니다.');
      window.location.href = '/counsel/list';
    }, 1000);
    
  } catch (error) {
    // 실패
    progressBar.classList.add('bg-danger');
    progressText.textContent = '오류 발생';
    alert('업로드 실패: ' + error.message);
    
    // 버튼 복구
    submitBtn.disabled = false;
    submitBtn.innerHTML = '<i class="bi bi-send"></i> 등록';
  }
});

function uploadWithProgress(formData, url) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const progressBar = document.getElementById('uploadProgressBar');
    const progressText = document.getElementById('uploadProgressText');
    
    // 진행률 이벤트
    xhr.upload.addEventListener('progress', (e) => {
      if (e.lengthComputable) {
        const percent = Math.round((e.loaded / e.total) * 100);
        progressBar.style.width = percent + '%';
        progressText.textContent = percent + '%';
      }
    });
    
    // 완료 이벤트
    xhr.addEventListener('load', () => {
      if (xhr.status === 200 || xhr.status === 302) {
        resolve(xhr.response);
      } else {
        reject(new Error('Upload failed with status ' + xhr.status));
      }
    });
    
    // 에러 이벤트
    xhr.addEventListener('error', () => {
      reject(new Error('Network error'));
    });
    
    // 중단 이벤트
    xhr.addEventListener('abort', () => {
      reject(new Error('Upload aborted'));
    });
    
    xhr.open('POST', url);
    xhr.send(formData);
  });
}
</script>
</body>
</html>
```

---

## 구현 예정

### 🔴 우선순위 높음
- [ ] 라이브러리 선택 (Uppy vs 순수 JavaScript)
- [ ] counsel-write.html에 Progress Bar 추가
- [ ] 에러 핸들링 강화
- [ ] 모바일 환경 테스트

### 🟡 우선순위 중간
- [ ] 다중 파일 업로드 시 각각의 진행률 표시
- [ ] 드래그 앤 드롭 지원
- [ ] 이미지 썸네일 미리보기

### 🟢 우선순위 낮음
- [ ] 청크 업로드 (대용량 파일)
- [ ] 업로드 일시정지/재개 기능
- [ ] 클라우드 스토리지 연동 (S3, GCS)

---

## 라이브러리 사용 승인 요청

### Uppy 라이브러리

**공식 사이트**: https://uppy.io/  
**GitHub**: https://github.com/transloadit/uppy  
**라이센스**: MIT  
**번들 크기**: ~200KB (gzip 후 ~60KB)  
**유지보수**: 활발 (주간 업데이트)  
**사용자 수**: 6.7K+ GitHub Stars

**장점**:
1. 현대적이고 반응형 UI
2. Bootstrap 5와 잘 어울림
3. 청크 업로드 지원 (대용량 파일)
4. TypeScript 지원
5. 활발한 커뮤니티

**단점**:
1. 번들 크기 증가 (~200KB)
2. 학습 곡선

**추천 여부**: ✅ **추천** (현재 프로젝트에 가장 적합)

---

**대안**: 순수 JavaScript로 구현 (외부 라이브러리 없음)

---

## 변경 이력

### [3.5.3] - 2025-11-11
#### 추가
- 파일 업로드 Progress Bar 구현 가이드 작성
- 구현 방법 비교 (5가지)
- 라이브러리 추천 (Uppy, Dropzone.js)
- 순수 JavaScript 구현 예시
- 라이브러리 사용 승인 요청

---

**문서 버전**: 1.0  
**최종 검토**: 2025-11-11  
**담당자**: Jeongmin Lee

**⚠️ 주의**: 라이브러리 사용 전 프로젝트 관리자 승인 필요

