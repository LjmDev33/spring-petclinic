# 🍞 Toast 알림 시스템 구현

## 📅 작업 일자
- **날짜**: 2025-11-25
- **작업자**: Jeongmin Lee
- **카테고리**: 신규 기능 추가

---

## 🎯 작업 목적

**문제점**:
- 기존 코드에서 `TOAST.showSuccess()` 호출이 있었으나 실제 TOAST 객체가 정의되지 않음
- 사용자 친화적인 알림 시스템 부재
- 에러 발생 시 사용자가 원인을 파악하기 어려움

**해결 방법**:
- Bootstrap Toast 기반 커스텀 알림 시스템 구현
- 성공, 정보, 경고, 에러 4가지 타입 지원
- 자동 사라짐 + 수동 닫기 기능
- XSS 방지 (HTML 이스케이프)

---

## 📁 변경된 파일

### 1. `static/js/toast-util.js` (신규 생성)
**역할**: Toast 알림 유틸리티 라이브러리

**주요 메서드**:
```javascript
TOAST.showSuccess(message, duration)    // 성공 메시지
TOAST.showInfo(message, duration)       // 정보 메시지
TOAST.showWarning(message, duration)    // 경고 메시지
TOAST.showError(message, duration)      // 에러 메시지
```

**기능**:
- ✅ Bootstrap Toast 기반
- ✅ 자동 컨테이너 생성
- ✅ 우측 상단 표시 (z-index: 9999)
- ✅ 타입별 배경색 (성공: 초록, 정보: 파랑, 경고: 노랑, 에러: 빨강)
- ✅ 자동 사라짐 (기본값: 3~5초)
- ✅ 수동 닫기 버튼
- ✅ HTML 이스케이프 (XSS 방지)
- ✅ 전역 객체 등록 (`window.TOAST`)

**코드 예시**:
```javascript
// 성공 메시지
TOAST.showSuccess('게시글이 등록되었습니다.');

// 에러 메시지 (5초 표시)
TOAST.showError('파일 업로드에 실패했습니다.', 5000);

// 경고 메시지 (커스텀 시간)
TOAST.showWarning('비밀번호가 일치하지 않습니다.', 4000);
```

---

### 2. `fragments/layout.html` (수정)
**변경 사항**: `<script>` 태그 추가

**변경 전**:
```html
<script th:src="@{/webjars/bootstrap/dist/js/bootstrap.bundle.min.js}"></script>
</body>
</html>
```

**변경 후**:
```html
<script th:src="@{/webjars/bootstrap/dist/js/bootstrap.bundle.min.js}"></script>
<script th:src="@{/js/toast-util.js}"></script>
</body>
</html>
```

**효과**: 모든 페이지에서 `TOAST` 객체 사용 가능

---

## 🎨 UI 디자인

### Toast 레이아웃
```
┌─────────────────────────────────┐
│ ✓ 성공         [ X ]            │
│ ─────────────────────────────── │
│ 게시글이 등록되었습니다.         │
└─────────────────────────────────┘
```

### 타입별 색상
| 타입 | 배경색 | 아이콘 | 제목 |
|------|--------|--------|------|
| success | `bg-success` (초록) | ✓ | 성공 |
| info | `bg-info` (파랑) | ℹ | 안내 |
| warning | `bg-warning` (노랑) | ⚠ | 경고 |
| error | `bg-danger` (빨강) | ✕ | 오류 |

---

## 🔧 기술 스택

### 의존성
- **Bootstrap 5.x**: Toast 컴포넌트 기반
- **순수 JavaScript**: jQuery 없이 구현
- **DOM API**: `insertAdjacentHTML`, `addEventListener`

### 브라우저 호환성
- ✅ Chrome, Edge, Firefox, Safari (모던 브라우저)
- ✅ IE11 미지원 (ES6 사용)

---

## 📊 사용 예시

### 1. 게시글 등록 성공
```javascript
document.getElementById('submitBtn').addEventListener('click', function() {
    // 게시글 등록 로직...
    
    if (success) {
        TOAST.showSuccess('게시글이 등록되었습니다.');
        setTimeout(() => {
            window.location.href = '/counsel/list';
        }, 1000);
    }
});
```

### 2. 파일 업로드 실패
```javascript
uppy.on('upload-error', (file, error) => {
    TOAST.showError(`"${file.name}" 업로드에 실패했습니다: ${error.message}`, 5000);
});
```

### 3. 비밀번호 검증 실패
```javascript
if (password !== confirmPassword) {
    TOAST.showWarning('비밀번호가 일치하지 않습니다.', 4000);
    return false;
}
```

### 4. 정보 안내
```javascript
TOAST.showInfo('비공개 게시글은 비밀번호 입력이 필요합니다.', 3000);
```

---

## ✅ 검증 완료

### 1. 컴파일 검증
```bash
PS C:\...\spring-petclinic> .\gradlew.bat compileJava
BUILD SUCCESSFUL in 13s
```

### 2. 파일 생성 확인
- ✅ `static/js/toast-util.js` 생성
- ✅ `fragments/layout.html` 수정 완료

### 3. 기능 검증 (수동 테스트 필요)
- [ ] 서버 실행 후 브라우저 콘솔에서 `TOAST` 객체 확인
- [ ] `TOAST.showSuccess('테스트')` 호출하여 Toast 표시 확인
- [ ] 4가지 타입 모두 정상 작동 확인
- [ ] 자동 사라짐 확인 (3~5초 후)
- [ ] 수동 닫기 버튼 작동 확인

---

## 🔄 연관 작업

### ErrorNotification 시스템과의 관계
- **ErrorNotification**: 상세한 에러 정보 + 모달 팝업 (복잡한 오류)
- **TOAST**: 간단한 알림 메시지 (성공, 정보, 경고, 단순 오류)

**역할 분담**:
```javascript
// 파일 업로드 실패 (상세 정보 필요)
ErrorNotification.handleFileUploadError(error, fileName);

// 게시글 등록 성공 (간단한 알림)
TOAST.showSuccess('게시글이 등록되었습니다.');
```

---

## 📚 문서 업데이트

### 업데이트 필요 문서
1. **CHANGELOG.md**: Toast 알림 시스템 추가 기록
2. **QUICK_REFERENCE.md**: TOAST API 사용법 추가
3. **UI_CONSISTENCY_GUIDE.md**: 알림 UI 규칙 추가

---

## 🎯 다음 단계

### 2단계: Uppy 파일 업로드 최종 점검
- [ ] `/counsel/upload-temp` 엔드포인트 동작 확인
- [ ] 글쓰기 시 첨부파일 정상 저장 확인
- [ ] Progress Bar 실시간 업데이트 확인
- [ ] TOAST 알림 연동 (업로드 성공/실패)

### 3단계: 에러 메시지 UI 완성
- [ ] 파일 업로드 실패 시 TOAST + ErrorNotification 연동
- [ ] 게시글 등록/수정 실패 시 원인 표시
- [ ] 네트워크 오류 시 재시도 안내

---

## 💡 개선 아이디어

### 향후 추가 기능
1. **Toast 큐 관리**: 동시에 여러 Toast 표시 시 순서 관리
2. **Toast 위치 옵션**: 좌측 상단, 중앙, 하단 등 선택 가능
3. **Toast 클릭 이벤트**: 클릭 시 상세 정보 모달 표시
4. **Toast 애니메이션**: 부드러운 슬라이드 인/아웃 효과
5. **Toast 그룹화**: 같은 타입의 메시지 하나로 묶기

---

**작성자**: Jeongmin Lee  
**최종 수정**: 2025-11-25  
**상태**: ✅ 완료 (서버 테스트 대기 중)

