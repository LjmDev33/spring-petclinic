# 드래그 앤 드롭 새 창 열림 문제 해결

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot  
**카테고리:** 버그 수정

---

## 📝 개요

온라인상담 글쓰기 화면에서 파일을 드래그 앤 드롭하면 파일이 첨부되지 않고 새 창(탭)이 열리는 브라우저 기본 동작 문제 해결

---

## ❌ 문제점

### 증상
1. **파일 선택 버튼**: 정상 작동 ✅
2. **드래그 앤 드롭**: 
   - 파일을 드롭하면 새 창/탭이 열림 ❌
   - 파일이 첨부되지 않음 ❌
   - 브라우저가 파일을 직접 열려고 시도 ❌

### 원인
```javascript
// 문제: preventDefault()와 stopPropagation() 부족
dropArea.addEventListener('drop', function(e) {
  e.preventDefault();  // drop 이벤트만 방지
  // dragenter, dragover 이벤트 처리 누락
  // 상위 요소 이벤트 전파 차단 누락
});
```

#### 브라우저 기본 동작
1. 파일을 드래그하면 `dragenter` → `dragover` → `drop` 이벤트 발생
2. 이 중 하나라도 기본 동작을 막지 않으면 브라우저가 파일을 열려고 시도
3. 특히 `dragenter`와 `dragover`에서 `preventDefault()` 필수
4. 이벤트 버블링으로 상위 요소에도 전파되어 기본 동작 발생

---

## ✅ 해결 방법

### 1. 모든 드래그 이벤트에 기본 동작 방지 추가

#### dragenter 이벤트 추가
```javascript
// dragenter 이벤트 처리 (드래그 시작 시)
dropArea.addEventListener('dragenter', function(e) {
  e.preventDefault();        // ✅ 기본 동작 방지
  e.stopPropagation();       // ✅ 이벤트 전파 차단
  dropArea.classList.add('uppy-Dashboard-inner--isDraggingOver');
});
```

#### dragover 이벤트 개선
```javascript
// dragover 이벤트 처리 (드래그 중)
dropArea.addEventListener('dragover', function(e) {
  e.preventDefault();        // ✅ 기본 동작 방지 (매우 중요!)
  e.stopPropagation();       // ✅ 이벤트 전파 차단
  dropArea.classList.add('uppy-Dashboard-inner--isDraggingOver');
});
```

#### dragleave 이벤트 개선
```javascript
// dragleave 이벤트 처리 (드래그 벗어남)
dropArea.addEventListener('dragleave', function(e) {
  e.preventDefault();
  e.stopPropagation();
  // 자식 요소로 이동할 때는 클래스 제거하지 않음
  if (e.target === dropArea) {
    dropArea.classList.remove('uppy-Dashboard-inner--isDraggingOver');
  }
});
```

#### drop 이벤트 개선
```javascript
// drop 이벤트 처리 (파일 드롭)
dropArea.addEventListener('drop', function(e) {
  e.preventDefault();        // ✅ 기본 동작 방지
  e.stopPropagation();       // ✅ 이벤트 전파 차단
  dropArea.classList.remove('uppy-Dashboard-inner--isDraggingOver');
  
  console.log('Files dropped:', e.dataTransfer.files.length);  // ✅ 디버깅
  
  var files = Array.from(e.dataTransfer.files);
  files.forEach(function(file) {
    try {
      self.uppy.addFile({
        name: file.name,
        type: file.type,
        data: file,
        size: file.size
      });
    } catch (err) {
      console.error('Error adding file:', err);
      alert('파일 추가 중 오류가 발생했습니다: ' + err.message);
    }
  });
  self.render();
});
```

### 2. Dashboard 루트 요소에도 기본 동작 방지

```javascript
// 전체 Dashboard에서도 기본 동작 방지
if (dashboardRoot) {
  dashboardRoot.addEventListener('dragover', function(e) {
    e.preventDefault();
    e.stopPropagation();
  });

  dashboardRoot.addEventListener('drop', function(e) {
    e.preventDefault();
    e.stopPropagation();
  });
}
```

### 3. 전체 페이지 레벨에서 기본 동작 방지

```javascript
// 전체 페이지에서 드래그 앤 드롭 기본 동작 방지
var preventDefaults = function(e) {
  e.preventDefault();
  e.stopPropagation();
};

['dragenter', 'dragover', 'dragleave', 'drop'].forEach(function(eventName) {
  document.body.addEventListener(eventName, preventDefaults, false);
});
```

---

## 📊 수정된 파일

| 파일 | 변경 내용 | 라인 |
|------|----------|------|
| **uppy-browser.js** | dragenter 이벤트 추가 | 295-300 |
| **uppy-browser.js** | dragover에 stopPropagation 추가 | 302-307 |
| **uppy-browser.js** | dragleave 개선 (자식 요소 처리) | 309-317 |
| **uppy-browser.js** | drop에 로깅 추가 | 320 |
| **uppy-browser.js** | Dashboard 루트 이벤트 처리 추가 | 340-351 |
| **uppy-browser.js** | 전체 페이지 이벤트 방지 추가 | 353-361 |

**총 1개 파일, 6곳 수정**

---

## 🔍 이벤트 흐름

### Before (문제 상황)
```
파일 드래그
   ↓
dragenter (처리 안 함)
   ↓
dragover (preventDefault만)
   ↓
drop (preventDefault만)
   ↓
이벤트 버블링 → body → window
   ↓
브라우저 기본 동작: 파일 열기
   ↓
새 창/탭 열림 ❌
```

### After (해결 후)
```
파일 드래그
   ↓
dragenter
   └─> preventDefault() ✅
   └─> stopPropagation() ✅
   └─> 시각적 피드백 (배경색 변경)
   ↓
dragover
   └─> preventDefault() ✅
   └─> stopPropagation() ✅
   └─> 계속 드래그 가능
   ↓
drop
   └─> preventDefault() ✅
   └─> stopPropagation() ✅
   └─> 파일 추가
   └─> 목록 렌더링
   ↓
완료! ✅
```

---

## 🎯 preventDefault()가 필요한 이유

### 드래그 앤 드롭 기본 동작
```javascript
// 브라우저 기본 동작:
1. dragenter: 드롭 영역 하이라이트
2. dragover: 드롭 가능 커서 표시
3. drop: 파일 열기/다운로드

// preventDefault() 없으면:
→ 브라우저가 파일을 직접 열려고 시도
→ 새 창에서 파일 표시
→ 현재 페이지 이탈 가능
```

### stopPropagation()이 필요한 이유
```javascript
// 이벤트 버블링:
dropArea (target)
   ↓
Dashboard
   ↓
body
   ↓
window

// stopPropagation() 없으면:
→ 상위 요소로 이벤트 전파
→ body에서 기본 동작 발생
→ 여전히 파일이 열림
```

---

## 🧪 테스트 시나리오

### 1. 드래그 시작
```
✅ 1. 파일을 Dashboard 영역으로 드래그
✅ 2. 배경색 변경 (파란색)
✅ 3. "파일을 여기에 드롭하세요" 메시지 표시
✅ 4. 새 창 열리지 않음
```

### 2. 드래그 중
```
✅ 1. 마우스 커서가 Dashboard 위에 있음
✅ 2. 배경색 유지
✅ 3. 드롭 가능 커서 표시
✅ 4. 자식 요소로 이동 시에도 배경색 유지
```

### 3. 드롭
```
✅ 1. 파일 드롭
✅ 2. 배경색 원래대로
✅ 3. 파일 목록에 추가
✅ 4. 개발자 도구에 "Files dropped: N" 로그
✅ 5. 새 창 열리지 않음 ✅
```

### 4. Dashboard 외부 드롭
```
✅ 1. Dashboard 바깥에 파일 드롭
✅ 2. 아무 동작 안 함
✅ 3. 새 창 열리지 않음
```

---

## 🐛 해결된 문제

### Before
```
❌ 드래그 앤 드롭 시 새 창 열림
❌ 파일이 첨부되지 않음
❌ 사용자 혼란
❌ 작성 중인 내용 손실 위험
```

### After
```
✅ 드래그 앤 드롭 정상 동작
✅ 파일이 목록에 추가됨
✅ 시각적 피드백 제공
✅ 새 창 열리지 않음
✅ 안전한 파일 업로드
```

---

## 🎓 기술적 의의

### 1. 이벤트 기본 동작 제어
```javascript
e.preventDefault();      // 브라우저 기본 동작 방지
e.stopPropagation();     // 이벤트 전파 차단
```
- 모든 드래그 관련 이벤트에 적용 필수
- 하나라도 빠지면 기본 동작 발생

### 2. 이벤트 버블링 이해
```
Target (dropArea)
   ↓ 버블링
Parent (Dashboard)
   ↓ 버블링
body
   ↓ 버블링
window
```
- `stopPropagation()`으로 버블링 차단
- 상위 요소에서 기본 동작 방지

### 3. 다층 방어 (Defense in Depth)
```javascript
// 1차 방어: dropArea
dropArea.addEventListener('drop', preventDefaults);

// 2차 방어: Dashboard
dashboardRoot.addEventListener('drop', preventDefaults);

// 3차 방어: body
document.body.addEventListener('drop', preventDefaults);
```
- 여러 레벨에서 기본 동작 방지
- 하나가 실패해도 다음 레벨에서 방어

### 4. dragleave 최적화
```javascript
// 자식 요소로 이동할 때는 클래스 유지
if (e.target === dropArea) {
  dropArea.classList.remove('uppy-Dashboard-inner--isDraggingOver');
}
```
- 자식 요소 간 이동 시 깜빡임 방지
- 부드러운 사용자 경험

---

## 🚀 추가 개선 사항

### 1. 파일 타입 검증
```javascript
dropArea.addEventListener('drop', function(e) {
  e.preventDefault();
  e.stopPropagation();
  
  var files = Array.from(e.dataTransfer.files);
  
  // 파일 타입 검증
  var invalidFiles = files.filter(function(file) {
    return !isValidFileType(file);
  });
  
  if (invalidFiles.length > 0) {
    alert('허용되지 않는 파일 형식입니다.');
    return;
  }
  
  // 파일 추가
});
```

### 2. 드래그 오버 애니메이션
```css
.uppy-Dashboard-inner--isDraggingOver {
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}
```

### 3. 드롭 존 확장
```javascript
// 전체 페이지를 드롭 존으로 만들기
document.addEventListener('drop', function(e) {
  e.preventDefault();
  if (e.target.closest('.uppy-Dashboard')) {
    // Dashboard 내부 - 처리됨
  } else {
    // Dashboard 외부 - 안내 메시지
    alert('파일을 첨부파일 영역에 드롭해 주세요.');
  }
});
```

---

## 📚 참고 문서

- [MDN - Drag and Drop API](https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API)
- [MDN - Event.preventDefault()](https://developer.mozilla.org/en-US/docs/Web/API/Event/preventDefault)
- [MDN - Event.stopPropagation()](https://developer.mozilla.org/en-US/docs/Web/API/Event/stopPropagation)
- [HTML5 Drag and Drop](https://www.w3.org/TR/html5/editing.html#drag-and-drop)

---

**작성 시간:** 20분  
**난이도:** ★★☆☆☆  
**중요도:** ★★★★★

**핵심:** 드래그 앤 드롭 이벤트는 `dragenter`, `dragover`, `drop` 모두에서 `preventDefault()`와 `stopPropagation()`을 호출해야 브라우저 기본 동작을 완전히 차단할 수 있습니다!

