# 파일 업로드 UX 개선 - 모달 프로그레스바 및 로딩 인디케이터

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot  
**카테고리:** UI/UX 개선

---

## 📝 개요

온라인상담 글쓰기 화면에서 파일 업로드 진행률을 화면 중앙 모달로 표시하고, 파일 추가 시 실시간 로딩 상태를 보여주는 기능 추가

---

## 🎯 개선 사항

### 1. 프로그레스바 모달화
**Before:** 화면 하단에 프로그레스바 표시
**After:** 화면 중앙에 모달로 표시

### 2. 파일 로딩 인디케이터 추가
**Before:** 파일 추가 시 즉시 목록에 표시만
**After:** 파일 추가 시 로딩 애니메이션 표시

---

## ✅ 구현 내용

### 1. 업로드 모달 CSS 생성

**파일:** `/src/main/resources/static/css/uppy/upload-modal.css` (신규 생성)

#### 모달 오버레이
```css
.upload-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(3px);  /* 배경 흐림 효과 */
}
```

#### 모달 컨테이너
```css
.upload-modal {
  background-color: white;
  border-radius: 12px;
  padding: 2rem;
  min-width: 400px;
  max-width: 500px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

#### 프로그레스바
```css
.upload-modal-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0d6efd 0%, #0b5ed7 100%);
  transition: width 0.3s ease;
}

/* 광택 효과 */
.upload-modal-progress-fill::after {
  content: '';
  position: absolute;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  animation: progressShine 1.5s infinite;
}
```

#### 스피너
```css
.upload-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: #0d6efd;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
```

#### 파일 로딩 인디케이터
```css
.file-loading-indicator::after {
  content: '●●●';
  animation: loadingDots 1.5s steps(4) infinite;
}

@keyframes loadingDots {
  0%, 20% {
    content: '●○○';
  }
  40% {
    content: '●●○';
  }
  60%, 100% {
    content: '●●●';
  }
}
```

### 2. HTML 구조 추가

#### 업로드 모달
```html
<div id="uploadModal" class="upload-modal-overlay" style="display: none;">
  <div class="upload-modal">
    <div class="upload-modal-header">
      <h3 class="upload-modal-title">
        <span class="upload-spinner"></span>
        파일 업로드 중
      </h3>
      <p class="upload-modal-subtitle" id="uploadModalSubtitle">
        업로드를 준비하고 있습니다...
      </p>
    </div>
    <div class="upload-modal-body">
      <div class="upload-modal-progress">
        <div class="upload-modal-progress-bar">
          <div class="upload-modal-progress-fill" id="uploadModalProgressFill">
          </div>
          <div class="upload-modal-progress-text" id="uploadModalProgressText">
            0%
          </div>
        </div>
      </div>
      <div class="upload-modal-status" id="uploadModalStatus">
        파일을 업로드하고 있습니다...
      </div>
    </div>
  </div>
</div>
```

### 3. JavaScript 이벤트 처리

#### 파일 추가 시 로딩 인디케이터
```javascript
uppy.on('file-added', (file) => {
  console.log('File added:', file.name);
  
  // 파일 목록에서 해당 파일 찾기
  setTimeout(() => {
    const fileItem = document.querySelector(`[data-file-id="${file.id}"]`);
    if (fileItem) {
      const fileName = fileItem.querySelector('.uppy-Dashboard-Item-name');
      if (fileName) {
        // 로딩 인디케이터 추가
        const indicator = document.createElement('span');
        indicator.className = 'file-loading-indicator';
        indicator.title = '로딩 중...';
        fileName.appendChild(indicator);
        
        // 성공 애니메이션
        fileItem.classList.add('file-added-success');
        
        // 0.8초 후 제거
        setTimeout(() => {
          fileItem.classList.remove('file-added-success');
          indicator.remove();
        }, 800);
      }
    }
  }, 100);
});
```

#### 업로드 시작 시 모달 표시
```javascript
uppy.on('upload-start', (files) => {
  console.log('Upload started:', files.length, 'files');
  
  uploadModal.style.display = 'flex';
  uploadModalSubtitle.textContent = `${files.length}개 파일을 업로드하고 있습니다...`;
  uploadModalProgressFill.style.width = '0%';
  uploadModalProgressText.textContent = '0%';
  uploadModalStatus.textContent = '파일을 업로드하고 있습니다...';
});
```

#### 진행률 업데이트
```javascript
uppy.on('upload-progress', (file, progress) => {
  const percent = Math.round((progress.bytesUploaded / progress.bytesTotal) * 100);
  
  uploadModalProgressFill.style.width = percent + '%';
  uploadModalProgressText.textContent = percent + '%';
  uploadModalStatus.textContent = `${file.name} (${percent}%)`;
});
```

#### 업로드 완료
```javascript
uppy.on('complete', (result) => {
  // 성공 메시지 표시
  uploadModalSubtitle.textContent = '업로드가 완료되었습니다!';
  uploadModalProgressFill.style.width = '100%';
  uploadModalProgressText.textContent = '100%';
  uploadModalStatus.textContent = '모든 파일이 성공적으로 업로드되었습니다.';
  
  // 1.5초 후 모달 자동 숨김
  setTimeout(() => {
    uploadModal.style.display = 'none';
  }, 1500);
});
```

---

## 📊 수정된 파일

| 파일 | 변경 내용 | 크기 |
|------|----------|------|
| **upload-modal.css** | 신규 생성 (모달 스타일) | 200줄 |
| **counsel-write.html** | CSS 링크 추가 | 1곳 |
| **counsel-write.html** | 기존 프로그레스바 제거 | 1곳 |
| **counsel-write.html** | 모달 HTML 추가 | 1곳 |
| **counsel-write.html** | JavaScript 이벤트 수정 | 3곳 |

**총 2개 파일 (1개 신규 생성, 1개 수정)**

---

## 🎨 UI 변화

### Before (개선 전)

#### 프로그레스바 위치
```
┌─────────────────────────────────┐
│                                 │
│   [폼 입력 영역]                │
│                                 │
│   ┌───────────────────────┐    │
│   │ 첨부파일 영역         │    │
│   └───────────────────────┘    │
│                                 │
│   ▓▓▓▓▓▓░░░░░░░░ 50%           │ ← 화면 하단
│                                 │
│   [작성완료 버튼]               │
└─────────────────────────────────┘
```

#### 파일 추가 시
```
파일 선택 → 즉시 목록에 추가
(로딩 상태 표시 없음)
```

### After (개선 후)

#### 모달 프로그레스바
```
┌─────────────────────────────────┐
│   [폼 입력 영역]                │
│                                 │
│   ╔═══════════════════════╗    │
│   ║                       ║    │
│   ║  ⟳ 파일 업로드 중     ║    │
│   ║  3개 파일을 업로드... ║    │
│   ║                       ║    │
│   ║  ████████░░░░ 50%     ║    │ ← 화면 중앙 모달
│   ║                       ║    │
│   ║  test.jpg (50%)       ║    │
│   ║                       ║    │
│   ╚═══════════════════════╝    │
│                                 │
│   [배경 흐림 처리]              │
└─────────────────────────────────┘
```

#### 파일 추가 시
```
파일 선택
   ↓
┌──────────────────────┐
│ 📄 test.jpg ●●●     │ ← 로딩 인디케이터
└──────────────────────┘
   ↓ (0.8초 후)
┌──────────────────────┐
│ 📄 test.jpg         │ ← 정상 표시
└──────────────────────┘
```

---

## 🎬 애니메이션 효과

### 1. 모달 등장 애니메이션
```css
@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```
- 페이드 인 + 위에서 아래로 슬라이드
- 부드러운 등장 효과

### 2. 프로그레스바 광택 효과
```css
@keyframes progressShine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
```
- 왼쪽에서 오른쪽으로 이동하는 광택
- 진행 중임을 시각적으로 표현

### 3. 스피너 회전
```css
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
```
- 360도 회전
- 업로드 진행 중임을 알림

### 4. 로딩 점 애니메이션
```css
@keyframes loadingDots {
  0%, 20% {
    content: '●○○';
  }
  40% {
    content: '●●○';
  }
  60%, 100% {
    content: '●●●';
  }
}
```
- 점이 순차적으로 채워짐
- 로딩 중임을 직관적으로 표현

### 5. 파일 추가 펄스 효과
```css
@keyframes fileAddedPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
}
```
- 파일이 추가될 때 살짝 커졌다 작아짐
- 파일이 성공적으로 추가되었음을 알림

---

## 🧪 테스트 시나리오

### 1. 파일 선택 버튼으로 추가
```
✅ 1. "📎 파일 선택" 버튼 클릭
✅ 2. 파일 1개 선택
✅ 3. 파일 목록에 추가됨
✅ 4. 파일 이름 옆에 "●●●" 로딩 인디케이터 표시
✅ 5. 0.8초 후 로딩 인디케이터 사라짐
✅ 6. 파일 아이템 펄스 애니메이션
```

### 2. 드래그 앤 드롭으로 추가
```
✅ 1. 파일을 Dashboard로 드래그
✅ 2. 배경색 파란색으로 변경
✅ 3. 파일 드롭
✅ 4. 파일 목록에 추가됨
✅ 5. 로딩 인디케이터 표시 (●●●)
✅ 6. 0.8초 후 사라짐
```

### 3. 업로드 진행
```
✅ 1. "작성완료" 버튼 클릭
✅ 2. 화면 중앙에 모달 표시
✅ 3. 배경 흐림 처리 (backdrop-filter)
✅ 4. 스피너 회전
✅ 5. "3개 파일을 업로드하고 있습니다..." 메시지
✅ 6. 프로그레스바 0% → 100% 증가
✅ 7. 광택 효과 애니메이션
✅ 8. 각 파일 이름 및 진행률 표시
✅ 9. 100% 도달 시 성공 메시지
✅ 10. 1.5초 후 모달 자동 사라짐
```

### 4. 여러 파일 동시 추가
```
✅ 1. 파일 5개 동시 선택
✅ 2. 5개 파일 모두 목록에 추가
✅ 3. 각 파일마다 로딩 인디케이터 표시
✅ 4. 순차적으로 로딩 인디케이터 사라짐
```

---

## ✨ 개선 효과

### Before (개선 전)
```
❌ 프로그레스바가 화면 하단에 있어 눈에 잘 안 띔
❌ 파일 추가 시 피드백 부족
❌ 로딩 상태를 알 수 없음
❌ 사용자가 업로드 진행 상황 확인 어려움
```

### After (개선 후)
```
✅ 모달로 화면 중앙에 집중
✅ 배경 흐림으로 집중도 증가
✅ 파일 추가 시 로딩 인디케이터 (●●●)
✅ 실시간 진행률 및 파일 이름 표시
✅ 광택 효과로 진행 중임을 명확히 표현
✅ 업로드 완료 시 성공 메시지 및 자동 사라짐
✅ 전반적인 사용자 경험 대폭 개선
```

---

## 🎓 기술적 의의

### 1. 모달을 통한 집중도 향상
```css
backdrop-filter: blur(3px);  /* 배경 흐림 */
z-index: 9999;               /* 최상위 레이어 */
```
- 사용자 시선을 업로드 진행 상황에 집중
- 다른 동작 차단으로 안정성 확보

### 2. CSS 애니메이션 활용
```css
/* 5가지 애니메이션 */
1. modalFadeIn      - 모달 등장
2. progressShine    - 프로그레스바 광택
3. spin             - 스피너 회전
4. loadingDots      - 로딩 점
5. fileAddedPulse   - 파일 추가 펄스
```
- 부드러운 사용자 경험
- 프로페셔널한 느낌

### 3. 실시간 피드백
```javascript
// 파일 추가 → 즉시 피드백
uppy.on('file-added', (file) => {
  // 로딩 인디케이터 표시
});

// 업로드 진행 → 실시간 업데이트
uppy.on('upload-progress', (file, progress) => {
  // 진행률 업데이트
});
```
- 모든 단계에서 시각적 피드백 제공
- 사용자 불안감 감소

---

## 🚀 향후 개선 방향

### 1. 다중 파일 진행률 표시
```html
<div class="upload-modal-files">
  <div class="upload-modal-file">
    <span>test1.jpg</span>
    <span>100%</span>
  </div>
  <div class="upload-modal-file">
    <span>test2.jpg</span>
    <span>45%</span> ← 현재 진행 중
  </div>
</div>
```

### 2. 업로드 취소 버튼
```html
<button class="upload-modal-cancel">
  취소
</button>
```

### 3. 파일 크기 및 속도 표시
```javascript
uploadModalStatus.textContent = 
  `${file.name} (${percent}%) - ${speed} MB/s`;
```

### 4. 에러 상태 표시
```css
.upload-modal-error {
  border: 2px solid #dc3545;
  background-color: #f8d7da;
}
```

---

## 📚 참고 문서

- [CSS backdrop-filter](https://developer.mozilla.org/en-US/docs/Web/CSS/backdrop-filter)
- [CSS Animations](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Animations)
- [Uppy Events](https://uppy.io/docs/uppy/#events)
- [Bootstrap Modals](https://getbootstrap.com/docs/5.3/components/modal/)

---

**작성 시간:** 30분  
**난이도:** ★★☆☆☆  
**중요도:** ★★★★☆

**핵심:** 모달을 통한 집중도 향상과 실시간 피드백으로 사용자 경험을 대폭 개선했습니다!

