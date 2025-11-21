# 오류 알림 시스템 사용 가이드

## 📋 개요

**작성일**: 2025-11-21  
**작성자**: Jeongmin Lee  
**목적**: 사용자 친화적인 오류 메시지 표시 시스템

---

## 🎯 주요 기능

### 1. **Toast 알림**
- 화면 우측 상단에 표시되는 간단한 알림
- 자동 사라짐 (기본 5초)
- 클릭 시 상세 정보 모달 표시
- 3가지 타입: `error`, `success`, `warning`

### 2. **오류 상세 모달**
- 오류 코드, 메시지, 발생 위치 등 상세 정보 표시
- 오류 정보 클립보드 복사 기능
- 관리자에게 전달 가능한 형식

### 3. **자동 로그 수집**
- 로컬 스토리지에 최대 50개 오류 로그 저장
- 사용자 환경 정보 (UserAgent, URL) 자동 수집
- 디버깅 및 문제 해결에 활용

---

## 🚀 사용 방법

### Toast 알림 표시

```javascript
// 기본 사용
ErrorNotification.showToast('제목', '메시지', 'error', 5000);

// 상세 정보 포함
ErrorNotification.showToast('제목', '메시지', 'error', 5000, {
  title: '상세 제목',
  message: '상세 메시지',
  code: 'ERROR_CODE',
  location: '발생 위치',
  details: {
    '키1': '값1',
    '키2': '값2'
  }
});
```

### 전용 오류 핸들러 사용

#### 1. 파일 업로드 오류
```javascript
ErrorNotification.handleFileUploadError({
  message: '파일 크기 초과',
  status: 413,
  code: 'FILE_SIZE_EXCEEDED',
  fileSize: file.size
}, file.name);
```

#### 2. 게시글 작성/수정 오류
```javascript
ErrorNotification.handlePostSubmitError({
  message: '서버 응답 없음',
  status: 500,
  code: 'SERVER_ERROR'
}, '작성'); // 또는 '수정'
```

#### 3. 파일 삭제 오류
```javascript
ErrorNotification.handleFileDeleteError({
  message: '권한 없음',
  code: 'PERMISSION_DENIED'
}, fileName);
```

#### 4. 네트워크 오류
```javascript
ErrorNotification.handleNetworkError({
  message: '서버 연결 실패'
});
```

---

## 📌 적용된 페이지

### ✅ counsel-write.html (게시글 작성)
- **검증 항목**:
  - 제목 필수 입력
  - 이름 필수 입력
  - 내용 필수 입력
- **오류 처리**:
  - 파일 크기 초과 (10MB)
  - 파일 형식 제한
  - 파일 개수 제한 (5개)
  - 업로드 실패
  - 네트워크 오류

### ✅ counsel-edit.html (게시글 수정)
- **검증 항목**:
  - 제목 필수 입력
  - 이름 필수 입력
  - 내용 필수 입력
- **오류 처리**:
  - 기존 파일 삭제 오류
  - 파일 크기 초과
  - 파일 형식 제한
  - 업로드 실패
  - 네트워크 오류

### ✅ counselDetail.html (게시글 상세)
- **검증 항목**:
  - 댓글 이름 필수 입력
  - 댓글 내용 필수 입력
  - 댓글 삭제 시 비밀번호 입력
- **오류 처리**:
  - 댓글 작성 실패
  - 댓글 삭제 실패

---

## 🎨 UI/UX 특징

### 1. **Toast 알림 디자인**
- **배경**: 그라데이션 효과 (error: 빨강, success: 초록, warning: 노랑)
- **애니메이션**: 우측에서 슬라이드 인
- **호버 효과**: 약간 왼쪽으로 이동 + 그림자 강화
- **자동 제거**: 설정된 시간 후 슬라이드 아웃

### 2. **오류 모달 디자인**
- **헤더**: 빨간색 그라데이션 배경
- **내용**: 섹션별 구분 (오류 메시지, 코드, 위치, 상세 정보)
- **액션**: 닫기, 오류 복사 버튼

### 3. **반응형 디자인**
- **모바일**: Toast 너비 자동 조정 (좌우 20px 여백)
- **태블릿/PC**: 고정 너비 350px

---

## 🔧 오류 타입별 처리

| 오류 타입 | 코드 | 설명 | 해결 방법 |
|---------|------|------|---------|
| `FILE_SIZE_EXCEEDED` | 파일 크기 초과 | 10MB를 초과하는 파일 | 파일 압축 또는 크기 줄이기 |
| `FILE_TYPE_NOT_ALLOWED` | 파일 형식 불가 | 허용되지 않는 파일 형식 | 허용 형식으로 변환 |
| `MAX_FILES_EXCEEDED` | 파일 개수 초과 | 5개 초과 업로드 시도 | 기존 파일 삭제 후 재시도 |
| `UPLOAD_ERROR` | 업로드 실패 | 서버 업로드 실패 | 네트워크 확인 후 재시도 |
| `NETWORK_ERROR` | 네트워크 오류 | 서버 연결 실패 | 인터넷 연결 확인 |
| `POST_ERROR` | 게시글 오류 | 게시글 작성/수정 실패 | 입력 내용 확인 |
| `DELETE_ERROR` | 삭제 오류 | 파일/댓글 삭제 실패 | 권한 확인 |

---

## 📊 로그 관리

### 로그 저장 위치
- **로컬 스토리지**: `errorLogs` 키
- **최대 저장**: 50개 (FIFO 방식)

### 로그 포함 정보
```javascript
{
  title: "오류 제목",
  message: "오류 메시지",
  type: "error|success|warning",
  timestamp: "ISO 8601 형식",
  details: { /* 상세 정보 */ },
  userAgent: "사용자 브라우저 정보",
  url: "오류 발생 URL"
}
```

### 로그 확인 방법
```javascript
// 브라우저 콘솔에서
const logs = JSON.parse(localStorage.getItem('errorLogs'));
console.table(logs);
```

---

## 🛠️ 커스터마이징

### 1. Toast 지속 시간 변경
```javascript
// 기본 5초 → 10초로 변경
ErrorNotification.showToast('제목', '메시지', 'error', 10000);

// 수동 제거 (duration = 0)
ErrorNotification.showToast('제목', '메시지', 'error', 0);
```

### 2. 새로운 오류 타입 추가
```javascript
// error-notification.js에 함수 추가
function handleCustomError(error, additionalInfo) {
  const details = {
    title: 'Custom 오류',
    message: error.message,
    code: 'CUSTOM_ERROR',
    location: '커스텀 처리',
    details: additionalInfo
  };

  showToast('Custom 오류', error.message, 'error', 5000, details);
}

// Public API에 추가
return {
  // ...existing methods...
  handleCustomError
};
```

---

## 🚨 주의사항

1. **Toast 중복 방지**
   - 같은 오류가 연속으로 발생하면 화면이 Toast로 가득 찰 수 있음
   - 짧은 시간 내 중복 Toast는 디바운싱 처리 권장

2. **로그 저장 용량**
   - 로컬 스토리지는 5~10MB 제한
   - 50개 이상 저장 시 자동 삭제되지만, 큰 오류 객체는 주의

3. **민감한 정보**
   - 오류 메시지에 비밀번호, 개인정보 포함 금지
   - 로그에 민감한 정보 저장 주의

4. **전역 오류 핸들러**
   - `window.error`, `unhandledrejection` 이벤트 자동 처리
   - 예상치 못한 오류도 Toast로 표시

---

## 📝 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2025-11-21 | 1.0.0 | 초기 버전 생성 |
|  |  | - Toast 알림 시스템 |
|  |  | - 오류 상세 모달 |
|  |  | - 자동 로그 수집 |
|  |  | - counsel 패키지 적용 |

---

## 🔗 관련 파일

- **CSS**: `/css/error-notification.css`
- **JavaScript**: `/js/error-notification.js`
- **적용 페이지**:
  - `/templates/counsel/counsel-write.html`
  - `/templates/counsel/counsel-edit.html`
  - `/templates/counsel/counselDetail.html`

---

## 💡 향후 개선 사항

- [ ] Toast 중복 방지 디바운싱
- [ ] 오류 로그 서버 전송 기능
- [ ] 관리자 페이지에서 오류 로그 조회
- [ ] 오류 통계 대시보드
- [ ] 다국어 지원 (영어, 일본어)
- [ ] 소리 알림 옵션

