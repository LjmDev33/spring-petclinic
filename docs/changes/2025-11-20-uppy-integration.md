# Uppy 파일 업로드 기능 구현 완료

**작업일**: 2025-11-20  
**작업자**: GitHub Copilot  
**카테고리**: 기능 추가

---

## 📋 작업 요약

Uppy 파일 업로드 라이브러리를 프로젝트에 완전히 통합하여 온라인상담 게시글 작성 시 첨부파일 업로드 기능을 구현했습니다.

---

## 🎯 구현 내용

### 1. Uppy 임시 업로드 엔드포인트 추가

**파일**: `CounselController.java`

- **엔드포인트**: `POST /counsel/upload-temp`
- **기능**: Uppy Dashboard에서 업로드한 파일을 임시 저장하고 파일 정보를 JSON으로 반환
- **응답 형식**:
  ```json
  {
    "success": true,
    "files": [
      {
        "id": "2025/11/uuid-123.jpg",
        "name": "example.jpg",
        "size": 1024000,
        "path": "2025/11/uuid-123.jpg"
      }
    ],
    "message": "1개 파일이 업로드되었습니다."
  }
  ```

### 2. CounselService 임시 파일 저장 메서드

**파일**: `CounselService.java`

- **메서드**: `storeFileTemp(MultipartFile file)`
- **기능**: `FileStorageService`를 통해 파일을 저장하고 경로 반환
- **로깅**: 업로드된 파일명, 경로, 크기를 INFO 레벨로 기록

### 3. counsel-write.html Uppy 초기화

**주요 구현 사항**:

#### (1) CSRF 토큰 포함
```javascript
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

uppy.use(Uppy.XHRUpload, {
  endpoint: '/counsel/upload-temp',
  headers: {
    [csrfHeader]: csrfToken
  }
});
```

#### (2) Progress Bar 실시간 업데이트
```javascript
uppy.on('upload-progress', (file, progress) => {
  const percent = Math.round((progress.bytesUploaded / progress.bytesTotal) * 100);
  progressBar.style.width = percent + '%';
  progressText.textContent = percent + '%';
});
```

#### (3) 업로드 완료 후 파일 경로 저장
```javascript
uppy.on('complete', (result) => {
  const filePaths = result.successful
    .flatMap(file => file.response.body.files)
    .map(f => f.path);
  
  document.getElementById('attachmentIds').value = filePaths.join(',');
});
```

#### (4) 제출 시 Uppy 업로드 먼저 수행
```javascript
form.addEventListener('submit', function (e) {
  e.preventDefault();
  
  if (uppy.getFiles().length > 0) {
    uppy.upload().then(() => {
      form.submit();
    });
  } else {
    form.submit();
  }
});
```

---

## 📂 변경된 파일

| 파일 | 변경 내용 |
|------|----------|
| `CounselController.java` | - `/upload-temp` 엔드포인트 추가<br>- import 추가 (MultipartFile, ResponseEntity, List, Map) |
| `CounselService.java` | - `storeFileTemp()` 메서드 추가 |
| `counsel-write.html` | - Uppy 초기화 코드 수정<br>- CSRF 토큰 포함<br>- Progress Bar 연동<br>- 파일 경로 저장 로직 개선 |
| `CHANGELOG.md` | - 변경 이력 추가 |

---

## ✅ 기능 검증 체크리스트

- [x] Uppy Dashboard가 정상적으로 렌더링되는가?
- [x] CSRF 토큰이 요청 헤더에 포함되는가?
- [x] `/counsel/upload-temp` 엔드포인트가 파일을 정상적으로 저장하는가?
- [x] Progress Bar가 업로드 진행률을 실시간으로 표시하는가?
- [x] 업로드 완료 후 파일 경로가 hidden 필드에 저장되는가?
- [x] 게시글 제출 시 Uppy 업로드가 먼저 수행되는가?
- [x] 파일 검증 (MIME 타입, 크기)이 작동하는가?

---

## 🚀 다음 단계 (완료)

1. **✅ 게시글 저장 시 attachmentPaths 연동 완료**: Uppy 업로드 파일이 게시글에 자동 첨부됨
2. ⏳ **에러 처리 강화**: 업로드 실패 시 사용자에게 명확한 에러 메시지 표시
3. ⏳ **다운로드 기능 구현**: 첨부파일 다운로드 엔드포인트 (`GET /counsel/download/{fileId}`)
4. ⏳ **권한 검증**: 비공개 게시글 첨부파일 다운로드 권한 확인

---

## 🔄 업데이트 (2025-11-20 오후)

### attachmentPaths 연동 완료

**추가된 기능**:
- `CounselPostWriteDto`에 `attachmentPaths` 필드 추가
- `CounselService.saveNew()`에서 attachmentPaths 파싱 및 Attachment 연결
- 파일 경로에서 파일명 추출 헬퍼 메서드 (`extractFileName()`)

**작동 과정**:
```
사용자 → Uppy Upload → /upload-temp → 파일 저장 
→ 응답 (filePath) → hidden 필드 저장 
→ 게시글 제출 → attachmentPaths 파싱 
→ Attachment 생성 → CounselPost 연결
```

**변경된 파일 (3개)**:
1. `CounselPostWriteDto.java` - attachmentPaths 필드 및 getter/setter
2. `CounselService.java` - attachmentPaths 파싱 로직, extractFileName() 메서드
3. `counsel-write.html` - hidden 필드명 변경 (attachmentIds → attachmentPaths)

---

## 📝 기술 스택

- **Frontend**: Uppy 3.x (로컬 내장)
- **Backend**: Spring Boot 3.5.0, Spring MVC
- **File Storage**: FileStorageService (로컬 파일 시스템)
- **Security**: Spring Security CSRF 토큰

---

## 🔒 보안 고려사항

1. ✅ **CSRF 보호**: 모든 업로드 요청에 CSRF 토큰 포함
2. ✅ **파일 검증**: MIME 타입 (Apache Tika), 크기 제한 (5MB)
3. ✅ **경로 역참조 방지**: `Path.normalize()` 사용
4. ✅ **파일명 난수화**: UUID 사용으로 파일명 예측 불가

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20

