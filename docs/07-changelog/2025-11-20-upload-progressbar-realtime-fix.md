# 파일 업로드 프로그레스바 실시간 표시 개선

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot  
**카테고리:** 기능 개선

---

## 📝 개요

온라인상담 글쓰기 화면에서 파일 업로드 시 프로그레스바가 실시간으로 표시되지 않는 문제 해결

---

## ❌ 문제점

### 증상
1. 파일 선택 및 드래그 앤 드롭: 정상 작동 ✅
2. **프로그레스바**: 표시되지 않음 ❌
3. 업로드 진행률을 사용자가 확인할 수 없음
4. 파일 업로드 중인지 알 수 없음

### 원인
```javascript
// 문제 1: XHR progress 이벤트가 Uppy에 제대로 전달되지 않음
xhr.upload.addEventListener('progress', function(e) {
  // setState() 호출 누락
  // console.log 디버깅 없음
});

// 문제 2: upload-start 이벤트 처리 누락
// 진행률 초기화 안 됨
```

---

## ✅ 해결 방법

### 1. XHR 진행률 이벤트 개선

#### uppy-browser.js 수정

**Before:**
```javascript
xhr.upload.addEventListener('progress', function(e) {
  if (e.lengthComputable) {
    file.progress.bytesUploaded = e.loaded;
    file.progress.bytesTotal = e.total;
    file.progress.percentage = Math.round((e.loaded / e.total) * 100);
    self.uppy.emit('upload-progress', file, file.progress);
  }
});
```

**After:**
```javascript
xhr.upload.addEventListener('progress', function(e) {
  if (e.lengthComputable) {
    var bytesUploaded = e.loaded;
    var bytesTotal = e.total;
    var percentage = Math.round((bytesUploaded / bytesTotal) * 100);
    
    // 파일 진행률 업데이트
    file.progress.bytesUploaded = bytesUploaded;
    file.progress.bytesTotal = bytesTotal;
    file.progress.percentage = percentage;
    file.progress.uploadStarted = file.progress.uploadStarted || Date.now();
    
    console.log('Upload progress:', file.name, percentage + '%');
    
    // Uppy 진행률 이벤트 발생
    self.uppy.emit('upload-progress', file, {
      bytesUploaded: bytesUploaded,
      bytesTotal: bytesTotal
    });
    
    // Dashboard 진행률 업데이트
    self.uppy.setState({
      files: self.uppy.state.files
    });
  }
});
```

**주요 개선 사항:**
1. ✅ `setState()` 호출 추가 → 상태 변경 알림
2. ✅ `console.log` 추가 → 디버깅 용이
3. ✅ `uploadStarted` 타임스탬프 기록
4. ✅ 명확한 이벤트 데이터 구조

### 2. 업로드 시작 이벤트 추가

#### counsel-write.html 수정

**추가된 코드:**
```javascript
// 업로드 시작 시 진행률 초기화
uppy.on('upload-start', (files) => {
  console.log('Upload started:', files.length, 'files');
  if (progressContainer) {
    progressContainer.style.display = 'block';
    progressBar.style.width = '0%';
    progressBar.setAttribute('aria-valuenow', '0');
    progressText.textContent = '0%';
  }
});
```

**기능:**
- 업로드 시작 시 프로그레스바를 즉시 표시
- 진행률을 0%로 초기화
- 사용자에게 업로드가 시작되었음을 알림

### 3. 진행률 업데이트 이벤트 개선

**Before:**
```javascript
uppy.on('upload-progress', (file, progress) => {
  if (!progressContainer) return;
  progressContainer.style.display = 'block';
  const percent = Math.round((progress.bytesUploaded / progress.bytesTotal) * 100);
  progressBar.style.width = percent + '%';
  progressBar.setAttribute('aria-valuenow', String(percent));
  progressText.textContent = percent + '%';
});
```

**After:**
```javascript
uppy.on('upload-progress', (file, progress) => {
  console.log('Progress event:', file.name, progress);
  if (!progressContainer) return;
  
  progressContainer.style.display = 'block';
  const percent = Math.round((progress.bytesUploaded / progress.bytesTotal) * 100);
  progressBar.style.width = percent + '%';
  progressBar.setAttribute('aria-valuenow', String(percent));
  progressText.textContent = percent + '%';
  
  console.log('Progress bar updated:', percent + '%');
});
```

**추가된 기능:**
- 이벤트 수신 확인 로그
- 프로그레스바 업데이트 확인 로그

---

## 📊 수정된 파일

| 파일 | 변경 내용 | 라인 |
|------|----------|------|
| **uppy-browser.js** | XHR progress 이벤트 개선 | 533-558 |
| **counsel-write.html** | upload-start 이벤트 추가 | 176-184 |
| **counsel-write.html** | upload-progress 로깅 추가 | 186-197 |

**총 2개 파일, 3곳 수정**

---

## 🔍 데이터 흐름

### Before (문제 상황)
```
파일 업로드 시작
   ↓
XHR progress 이벤트 발생
   ↓
file.progress 업데이트 (내부만)
   ↓
emit('upload-progress')
   ↓
❌ 이벤트 리스너가 제대로 작동하지 않음
   ↓
프로그레스바 업데이트 안 됨
```

### After (해결 후)
```
파일 업로드 시작
   ↓
emit('upload-start') ✅
   └─> 프로그레스바 표시 (0%)
   ↓
XHR progress 이벤트 발생
   ↓
file.progress 업데이트
   ↓
console.log (디버깅) ✅
   ↓
emit('upload-progress', { bytesUploaded, bytesTotal }) ✅
   ↓
setState() 호출 (상태 변경 알림) ✅
   ↓
이벤트 리스너 실행
   ↓
프로그레스바 실시간 업데이트 ✅
   └─> 0% → 25% → 50% → 75% → 100%
```

---

## 🎨 프로그레스바 UI

### 업로드 전
```
[숨김 상태]
```

### 업로드 시작 (0%)
```
┌─────────────────────────────────┐
│ 0%                              │
└─────────────────────────────────┘
```

### 업로드 중 (50%)
```
┌─────────────────────────────────┐
│ ████████████████░░░░░░░░░  50%  │
└─────────────────────────────────┘
```

### 업로드 완료 (100%)
```
┌─────────────────────────────────┐
│ ████████████████████████████ 100%│
└─────────────────────────────────┘
    ↓
1초 후 자동 숨김
```

---

## 🧪 테스트 시나리오

### 1. 작은 파일 (< 1MB)
```
✅ 1. 파일 선택
✅ 2. "작성완료" 버튼 클릭
✅ 3. 프로그레스바 즉시 표시 (0%)
✅ 4. 빠르게 100%로 증가
✅ 5. 1초 후 자동 숨김
```

### 2. 큰 파일 (> 5MB)
```
✅ 1. 파일 선택
✅ 2. "작성완료" 버튼 클릭
✅ 3. 프로그레스바 즉시 표시 (0%)
✅ 4. 점진적으로 증가 (10% → 20% → ... → 100%)
✅ 5. 1초 후 자동 숨김
```

### 3. 여러 파일
```
✅ 1. 파일 3개 선택
✅ 2. "작성완료" 버튼 클릭
✅ 3. 프로그레스바 표시
✅ 4. 각 파일마다 진행률 표시
✅ 5. 모든 파일 완료 후 숨김
```

### 4. 개발자 도구 콘솔
```
✅ Upload started: 1 files
✅ Upload progress: test.jpg 25%
✅ Progress bar updated: 25%
✅ Upload progress: test.jpg 50%
✅ Progress bar updated: 50%
✅ Upload progress: test.jpg 75%
✅ Progress bar updated: 75%
✅ Upload progress: test.jpg 100%
✅ Progress bar updated: 100%
✅ Uppy upload complete: {...}
```

---

## 🐛 해결된 문제

### Before
```
❌ 프로그레스바 표시 안 됨
❌ 업로드 진행 상황 알 수 없음
❌ 사용자 불안감 증가
❌ 업로드 중인지 완료인지 불명확
```

### After
```
✅ 프로그레스바 실시간 표시
✅ 진행률 정확히 표시 (0% ~ 100%)
✅ 사용자 경험 개선
✅ 업로드 상태 명확히 전달
✅ 디버깅 용이 (console.log)
```

---

## 🎓 기술적 의의

### 1. XMLHttpRequest.upload.progress 이벤트
```javascript
xhr.upload.addEventListener('progress', function(e) {
  if (e.lengthComputable) {
    // e.loaded: 업로드된 바이트 수
    // e.total: 전체 파일 크기
    var percent = (e.loaded / e.total) * 100;
  }
});
```
- 실시간 업로드 진행률 추적
- 브라우저가 자동으로 호출
- `lengthComputable`로 진행률 계산 가능 여부 확인

### 2. 상태 관리 및 이벤트 전파
```javascript
// 1. 내부 상태 업데이트
file.progress = { ... };

// 2. 이벤트 발생 (컴포넌트 간 통신)
self.uppy.emit('upload-progress', file, progress);

// 3. 전역 상태 업데이트 (리렌더링 트리거)
self.uppy.setState({ files: ... });
```
- 3단계 상태 전파로 일관성 유지
- 이벤트 기반 아키텍처

### 3. 사용자 피드백의 중요성
```
업로드 시작 → 즉시 피드백 (0%)
진행 중 → 실시간 피드백 (25%, 50%, 75%)
완료 → 최종 피드백 (100%) + 자동 숨김
```
- 사용자가 대기 시간을 예측 가능
- 불안감 감소, 신뢰도 증가

---

## 🚀 향후 개선 방향

### 1. 예상 남은 시간 표시
```javascript
var uploadStarted = file.progress.uploadStarted;
var elapsed = Date.now() - uploadStarted;
var speed = bytesUploaded / (elapsed / 1000); // bytes/sec
var remaining = (bytesTotal - bytesUploaded) / speed;

progressText.textContent = percent + '% (' + formatTime(remaining) + ' 남음)';
```

### 2. 업로드 속도 표시
```javascript
var speed = bytesUploaded / (elapsed / 1000);
var speedText = formatBytes(speed) + '/s';
progressText.textContent = percent + '% (' + speedText + ')';
```

### 3. 파일별 개별 진행률
```javascript
// 여러 파일 업로드 시 각각의 진행률 표시
files.forEach(file => {
  var fileProgress = document.querySelector('[data-file-id="' + file.id + '"] .progress-bar');
  fileProgress.style.width = file.progress.percentage + '%';
});
```

### 4. 애니메이션 효과
```css
.progress-bar {
  transition: width 0.3s ease;
}

.progress-bar-animated {
  animation: progress-bar-stripes 1s linear infinite;
}
```

---

## 📚 참고 문서

- [MDN - XMLHttpRequest.upload](https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/upload)
- [MDN - ProgressEvent](https://developer.mozilla.org/en-US/docs/Web/API/ProgressEvent)
- [Bootstrap Progress Bars](https://getbootstrap.com/docs/5.3/components/progress/)
- [Uppy Upload Progress](https://uppy.io/docs/uppy/#upload-progress)

---

**작성 시간:** 20분  
**난이도:** ★★☆☆☆  
**중요도:** ★★★★☆

**핵심:** XHR progress 이벤트에서 `setState()` 호출과 명확한 이벤트 데이터 전달이 프로그레스바 실시간 표시의 핵심입니다!

