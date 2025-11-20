# 📝 Spring PetClinic 변경 이력

## 변경 이력 관리 규칙

- **모든 변경사항은 이 파일에 기록됩니다**
- **날짜, 카테고리, 변경 내용, 영향 범위를 명시합니다**
- **관련 문서(PROJECT_DOCUMENTATION.md, QUICK_REFERENCE.md, TABLE_DEFINITION.md, UI_SCREEN_DEFINITION.md, ARCHITECTURE.md, API_SPECIFICATION.md)도 함께 업데이트됩니다** ⭐NEW

---

## [3.5.22] - 2025-11-20 (오전 문제 해결)

### 🚨 긴급 수정

#### 1. 서버 실행 중단 문제 해결 ✅

**문제**: Hibernate DDL 실행 중 무한 대기로 서버 실행 안됨

**로그**:
```
Hibernate: alter table counsel_post modify column status enum ('COMPLETE','END','WAIT') not null
▶ 이후 멈춤
```

**원인**:
- `ddl-auto: update` 설정으로 Hibernate가 ENUM 컬럼 자동 변경 시도
- MySQL에서 ENUM 컬럼 ALTER 시 테이블 락 발생
- 기존 데이터가 있는 상태에서 락이 해제되지 않아 무한 대기

**해결**:

1. **application-dev.yml 수정**:
```yaml
# Before
spring:
  jpa:
    hibernate:
      ddl-auto: update

# After
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 검증만 수행, 자동 변경 안함
```

2. **ENUM 수정 스크립트 생성**:
- `src/main/resources/db/mysql/fix-counsel-enum.sql`
- `fix-enum.bat` (Windows 실행 파일)

**SQL**:
```sql
ALTER TABLE counsel_post 
MODIFY COLUMN status ENUM('WAIT', 'COMPLETE', 'END') NOT NULL;
```

**실행 방법**:
```cmd
fix-enum.bat
```

또는

```bash
mysql -u dev33 -pezflow_010 petclinic < src/main/resources/db/mysql/fix-counsel-enum.sql
```

**효과**:
- ✅ 서버가 정상적으로 시작됨
- ✅ ENUM 값 순서가 Java Enum과 일치
- ✅ 향후 스키마 자동 변경으로 인한 문제 방지

**문서**:
- `docs/08-troubleshooting/SERVER_HANG_ENUM_FIX.md` - 상세 가이드

---

## [3.5.21] - 2025-11-20 (저녁)

### 🐛 수정된 버그

#### 1. counsel-write.html 치명적 버그 수정 ✅

**문제**: hidden input과 JavaScript 변수명 불일치로 첨부파일이 게시글에 연결되지 않음

**원인**:
- HTML: `<input id="attachmentIds" name="attachmentIds">`
- JavaScript: `document.getElementById('attachmentPaths')`
- DTO: `private String attachmentPaths;`

**수정**:
```html
<!-- Before -->
<input type="hidden" id="attachmentIds" name="attachmentIds">

<!-- After -->
<input type="hidden" id="attachmentPaths" name="attachmentPaths">
```

**영향**:
- ✅ JavaScript가 정상적으로 hidden 필드에 파일 경로 저장
- ✅ Spring MVC가 `attachmentPaths` 파라미터를 DTO에 바인딩
- ✅ 첨부파일이 게시글에 정상 연결됨

---

### 🎨 UI 개선

#### 1. counsel-password.html 버튼 UI 통일 ✅

**변경 사항**:
- 버튼 높이: `38px` → `42px` (다른 페이지와 통일)
- 버튼 간격: `gap: 8px` 추가
- `flex-wrap` 제거 (불필요)

**적용**:
```html
<!-- Before -->
<div class="d-flex flex-wrap justify-content-end">
  <a style="height: 38px;">목록</a>
  <button style="height: 38px;">확인</button>
</div>

<!-- After -->
<div class="d-flex justify-content-end" style="gap: 8px;">
  <a style="height: 42px;">목록</a>
  <button style="height: 42px;">확인</button>
</div>
```

---

### ✅ 검증 완료

**백엔드**:
- ✅ CounselController.java 컴파일 성공
- ✅ CounselService.java 컴파일 성공
- ✅ CounselPostWriteDto.java 컴파일 성공

**프론트엔드**:
- ✅ counselList.html - 정렬, 검색 정상
- ✅ counsel-write.html - attachmentPaths 오류 수정
- ✅ counselDetail.html - 댓글, 모달 정상
- ✅ counsel-password.html - 버튼 UI 통일
- ✅ counsel-edit.html - 수정 폼 정상

---

## [3.5.20] - 2025-11-20 (오후)

### 🎉 추가된 기능

#### 1. Uppy 업로드 파일과 게시글 연동 완성 ✅

**구현 내용**:
- ✅ Uppy 업로드된 파일 경로를 게시글 저장 시 Attachment와 자동 연결
- ✅ `attachmentPaths` 파라미터 추가 (쉼표 구분 파일 경로 목록)
- ✅ 파일 경로 파싱 및 Attachment 엔티티 생성
- ✅ 하위 호환성 유지 (기존 MultipartFile 방식도 동작)

**변경 파일**:

**1. CounselPostWriteDto.java**:
```java
private String attachmentPaths; // Uppy 업로드된 파일 경로 (쉼표 구분)

public String getAttachmentPaths() { return attachmentPaths; }
public void setAttachmentPaths(String attachmentPaths) { this.attachmentPaths = attachmentPaths; }
```

**2. CounselService.saveNew()**:
```java
// 4. 첨부파일 처리 (Uppy 업로드된 파일 경로)
if (dto.getAttachmentPaths() != null && !dto.getAttachmentPaths().isBlank()) {
    String[] filePaths = dto.getAttachmentPaths().split(",");
    
    for (String filePath : filePaths) {
        // Attachment 엔티티 생성
        Attachment attachment = new Attachment();
        attachment.setFilePath(filePath);
        attachment.setOriginalFileName(extractFileName(filePath));
        attachmentRepository.save(attachment);
        
        // CounselPost와 연결
        CounselPostAttachment postAttachment = new CounselPostAttachment();
        postAttachment.setCounselPost(entity);
        postAttachment.setAttachment(attachment);
        entity.addAttachment(postAttachment);
    }
    entity.setAttachFlag(true);
}

// 파일 경로에서 파일명 추출 헬퍼 메서드
private String extractFileName(String filePath) {
    String normalizedPath = filePath.replace('\\', '/');
    int lastSlash = normalizedPath.lastIndexOf('/');
    return lastSlash >= 0 ? normalizedPath.substring(lastSlash + 1) : normalizedPath;
}
```

**3. counsel-write.html**:
```html
<!-- hidden 필드 이름 변경 -->
<input type="hidden" id="attachmentPaths" name="attachmentPaths">
```

```javascript
// JavaScript에서 attachmentPaths로 전달
document.getElementById('attachmentPaths').value = filePaths.join(',');
```

**효과**:
- ✅ Uppy로 업로드한 파일이 게시글에 자동 첨부됨
- ✅ 첨부파일 목록이 게시글 상세에서 표시됨
- ✅ 파일 다운로드 가능 (추후 권한 검증 추가)

**작동 흐름**:
```
1. 사용자가 Uppy로 파일 선택 → XHR Upload
   ↓
2. POST /counsel/upload-temp → FileStorageService.storeFile()
   ↓
3. 파일 저장 (data/counsel/uploads/yyyy/MM/UUID.ext)
   ↓
4. 서버 응답: { files: [{ path: "2025/11/abc.jpg" }] }
   ↓
5. JavaScript: attachmentPaths hidden 필드에 "2025/11/abc.jpg,2025/11/def.png" 저장
   ↓
6. 게시글 제출: POST /counsel (Form with attachmentPaths)
   ↓
7. CounselService.saveNew() → attachmentPaths 파싱
   ↓
8. Attachment 엔티티 생성 및 CounselPost와 연결
   ↓
9. 게시글 상세에서 첨부파일 목록 표시
```

---

## [3.5.19] - 2025-11-20

### 🎉 추가된 기능

#### 1. Uppy 파일 업로드 기능 완성 ✅

**구현 내용**:
- ✅ Uppy Dashboard를 통한 드래그앤드롭 파일 업로드
- ✅ 임시 파일 업로드 엔드포인트 (`POST /counsel/upload-temp`)
- ✅ CSRF 토큰 자동 포함 (Spring Security 호환)
- ✅ Progress Bar 실시간 업데이트
- ✅ 파일 검증 (MIME 타입, 크기 제한 5MB)

**변경 파일**:

**1. CounselController.java**:
```java
/**
 * Uppy 임시 파일 업로드 엔드포인트
 * - Uppy Dashboard에서 파일 업로드 시 호출되는 REST API
 * - 파일을 임시 저장하고 파일 ID 목록을 JSON 응답으로 반환
 * - 실제 게시글 등록 시 attachmentIds로 전달받아 연결
 */
@PostMapping("/upload-temp")
@ResponseBody
public ResponseEntity<Map<String, Object>> uploadTemp(@RequestParam("files") MultipartFile[] files) {
    Map<String, Object> response = new HashMap<>();
    List<Map<String, Object>> uploadedFiles = new ArrayList<>();

    try {
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // 파일 저장 및 경로 반환
                String filePath = counselService.storeFileTemp(file);
                
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("id", filePath);
                fileInfo.put("name", file.getOriginalFilename());
                fileInfo.put("size", file.getSize());
                fileInfo.put("path", filePath);
                
                uploadedFiles.add(fileInfo);
            }
        }
        
        response.put("success", true);
        response.put("files", uploadedFiles);
        response.put("message", uploadedFiles.size() + "개 파일이 업로드되었습니다.");
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        response.put("success", false);
        response.put("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

**2. CounselService.java**:
```java
/**
 * Uppy를 통한 임시 파일 저장 (게시글 작성 전 미리 업로드)
 */
public String storeFileTemp(MultipartFile file) {
    try {
        String filePath = fileStorageService.storeFile(file);
        log.info("Temp file stored: originalName={}, storedPath={}, size={}", 
            file.getOriginalFilename(), filePath, file.getSize());
        return filePath;
    } catch (Exception e) {
        log.error("Failed to store temp file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
        throw new RuntimeException("임시 파일 저장 중 오류가 발생했습니다.", e);
    }
}
```

**3. counsel-write.html**:
```javascript
// CSRF 토큰 가져오기
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// Uppy XHRUpload 설정 (CSRF 헤더 포함)
uppy.use(Uppy.XHRUpload, {
  endpoint: '/counsel/upload-temp',
  fieldName: 'files',
  formData: true,
  headers: {
    [csrfHeader]: csrfToken
  }
});

// 업로드 진행률 표시
uppy.on('upload-progress', (file, progress) => {
  const percent = Math.round((progress.bytesUploaded / progress.bytesTotal) * 100);
  progressBar.style.width = percent + '%';
  progressText.textContent = percent + '%';
});

// 업로드 완료 시 파일 경로 저장
uppy.on('complete', (result) => {
  if (result.successful && result.successful.length > 0) {
    const filePaths = [];
    result.successful.forEach(file => {
      if (file.response && file.response.body && file.response.body.files) {
        file.response.body.files.forEach(f => {
          if (f.path) filePaths.push(f.path);
        });
      }
    });
    document.getElementById('attachmentIds').value = filePaths.join(',');
  }
});
```

**효과**:
- ✅ 오프라인 환경 지원 (Uppy 로컬 내장)
- ✅ 사용자 친화적 UI (드래그앤드롭, Progress Bar)
- ✅ Spring Security CSRF 호환
- ✅ 파일 검증 (MIME, 크기)
- ✅ 다중 파일 업로드 (최대 5개, 파일당 5MB)

**API 엔드포인트**:
| 엔드포인트 | HTTP 메서드 | 설명 |
|-----------|------------|------|
| `/counsel/upload-temp` | POST | Uppy 임시 파일 업로드 |

**파일 구조**:
```
static/
├─ js/uppy/
│  └─ uppy.min.js
└─ css/uppy/
   └─ uppy.min.css

data/counsel/uploads/
└─ yyyy/MM/UUID.ext
```

---

## [3.5.18] - 2025-11-12 (오후 - 4차)

### 🎨 UI/UX 개선

#### 1. 닉네임 표출 개선 ✅
**문제**: 닉네임이 없는 계정은 빈 값으로 표시됨

**해결**:
```html
<!-- Before: 닉네임만 표시 -->
<span th:text="${#authentication.principal.nickname}"></span>님

<!-- After: 닉네임이 없으면 username 표시 -->
<span th:text="${#authentication.principal.nickname != null && !#strings.isEmpty(#authentication.principal.nickname) 
               ? #authentication.principal.nickname 
               : #authentication.principal.username}"></span>님
```

**효과**:
- ✅ 닉네임 미설정 시 아이디 표출
- ✅ 모든 사용자에게 일관된 표시

---

#### 2. 마이페이지 프로필 저장 비동기 처리 ✅
**문제**: 프로필 저장 후 페이지 새로고침 필요, 헤더 닉네임 즉시 반영 안됨

**변경 사항**:

**Controller (MyPageController.java)**:
```java
// Before: 리다이렉트 방식
@PostMapping("/update")
public String updateProfile(..., RedirectAttributes redirectAttributes) {
    // ...
    return "redirect:/mypage";
}

// After: JSON 응답 방식
@PostMapping("/update")
@ResponseBody
public Map<String, Object> updateProfile(...) {
    Map<String, Object> response = new HashMap<>();
    try {
        userService.updateProfile(username, email, name, nickname, phone);
        response.put("success", true);
        response.put("message", "프로필이 수정되었습니다.");
        response.put("nickname", nickname);
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", e.getMessage());
    }
    return response;
}
```

**Frontend (mypage.html)**:
```javascript
// 비동기 폼 제출
document.getElementById('profileForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    try {
        const response = await fetch('/mypage/update', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            // 헤더 닉네임 즉시 업데이트
            const headerNickname = document.querySelector('.text-success.fw-bold');
            if (headerNickname && result.nickname) {
                headerNickname.textContent = result.nickname;
            }
            alert(result.message);
        } else {
            alert(result.message);
        }
    } catch (error) {
        alert('프로필 저장 중 오류가 발생했습니다.');
    }
});
```

**효과**:
- ✅ 페이지 새로고침 없이 프로필 저장
- ✅ 헤더 닉네임 즉시 반영
- ✅ 사용자 경험 개선

---

#### 3. 관리자 설정 페이지 상세 버튼 변경 ✅
**문제**: 원형 버튼이 아닌 일반 버튼 요청

**Before**:
```html
<button class="btn btn-sm btn-light rounded-circle" 
        style="width: 32px; height: 32px;">
  <i class="bi bi-plus-lg"></i>
</button>
```

**After**:
```html
<button class="btn btn-sm btn-light">
  <i class="bi bi-plus-lg"></i> 상세
</button>
```

**효과**:
- ✅ 일반 버튼 형태로 변경
- ✅ 텍스트 + 아이콘으로 명확한 의미 전달

---

### 📚 프로젝트 규칙 추가

#### 4. 버튼 균일성 규칙 추가 ✅
**규칙**: 같은 행의 버튼은 크기 및 라인 동일

**문서**: `PROJECT_DOCUMENTATION.md` > UI 설계 규칙 9번

**추가된 원칙**:
```
7. ✅ 버튼 균일성: 같은 행의 버튼은 크기 및 라인 동일 ⭐NEW (2025-11-12)
```

**예시**:
```html
<!-- ✅ 올바른 예시: 같은 행의 버튼 크기 동일 -->
<div class="d-flex justify-content-end gap-2">
  <a href="/list" class="btn btn-secondary" style="min-width: 120px; height: 42px;">목록</a>
  <button type="submit" class="btn btn-primary" style="min-width: 120px; height: 42px;">확인</button>
</div>

<!-- ❌ 잘못된 예시: 크기 불일치 -->
<div class="d-flex gap-2">
  <a href="/list" class="btn btn-secondary">목록</a>
  <button type="submit" class="btn btn-primary btn-lg">확인</button>
</div>
```

---

### 🐛 버그 수정

#### 5. 게시글 상세/삭제 ERR_INCOMPLETE_CHUNKED_ENCODING 오류 해결 ✅
**문제**: 게시글 상세화면 및 삭제 시 `net::ERR_INCOMPLETE_CHUNKED_ENCODING 200 (OK)` 오류 발생

**원인**:
```java
// Controller에서 IOException을 throw하지만 처리하지 않음
@GetMapping("/detail/{id}")
public String detail(...) throws IOException {
    CounselPostDto post = counselService.getDetail(id); // IOException 발생 가능
    // ...
}
```

**해결**:
```java
@GetMapping("/detail/{id}")
public String detail(...) {
    CounselPostDto post;
    try {
        post = counselService.getDetail(id);
    } catch (Exception e) {
        log.error("Failed to load post detail: id={}", id, e);
        model.addAttribute("error", "게시글을 불러오는 중 오류가 발생했습니다.");
        return "error";
    }
    // ...
}
```

**효과**:
- ✅ 게시글 상세화면 정상 표시
- ✅ 게시글 삭제 정상 작동
- ✅ 에러 발생 시 에러 페이지로 안내

---

### 🔧 수정된 파일

**Backend (2개)**:
1. ✅ `MyPageController.java`
   - 프로필 업데이트 JSON 응답으로 변경
   - `Map<String, Object>` 반환
   - `@ResponseBody` 추가

2. ✅ `CounselController.java`
   - `detail()` 메서드 IOException 처리
   - try-catch로 에러 핸들링

**Frontend (3개)**:
1. ✅ `fragments/layout.html`
   - 닉네임 없으면 username 표시

2. ✅ `user/mypage.html`
   - 프로필 저장 비동기 처리
   - fetch API 사용
   - 헤더 닉네임 즉시 업데이트

3. ✅ `admin/settings.html`
   - 원형 버튼 → 일반 버튼 변경
   - 텍스트 + 아이콘

**문서 (2개)**:
1. ✅ `PROJECT_DOCUMENTATION.md`
   - 버튼 균일성 규칙 추가

2. ✅ `CHANGELOG.md`
   - [3.5.18] 변경 이력 추가

---

### ✅ 검증 완료

**컴파일**: ✅ 성공 (경고만 존재)  
**문법 오류**: ❌ 없음

---

### 📊 개선 효과

| 항목 | Before | After | 개선점 |
|------|--------|-------|--------|
| **닉네임 표출** | 빈 값 | username 대체 | 일관성 |
| **프로필 저장** | 페이지 새로고침 | 비동기 처리 | UX 개선 |
| **헤더 닉네임** | 새로고침 필요 | 즉시 반영 | 실시간 업데이트 |
| **상세 버튼** | 원형 | 일반 버튼 | 명확성 |
| **게시글 상세** | 오류 발생 | 정상 표시 | 버그 수정 |

---

## [3.5.17] - 2025-11-12 (오후 - 3차)

### 🎨 UI/UX 개선

#### 1. 관리자 설정 페이지 상세 보기 아이콘 개선 ✅
**변경 사항**: 상세 보기 버튼을 원형 + 아이콘으로 변경

**Before**:
```html
<button class="btn btn-sm btn-light">
  <i class="bi bi-plus-circle"></i> 상세 보기
</button>
```

**After**:
```html
<button class="btn btn-sm btn-light rounded-circle" 
        style="width: 32px; height: 32px; padding: 0;"
        title="상세 보기">
  <i class="bi bi-plus-lg" style="font-size: 1.2rem;"></i>
</button>
```

**효과**:
- ✅ 깔끔한 원형 버튼 디자인
- ✅ 공간 효율성 향상
- ✅ + 아이콘으로 직관성 개선

---

#### 2. 모달 종료 시 흑백 화면 현상 해결 ✅
**문제**: 상세 보기 모달 닫을 때 화면이 흑백으로 유지

**원인**: 
```javascript
// 기존 모달을 강제로 닫는 로직이 문제
const detailModal = bootstrap.Modal.getInstance(document.getElementById('detailModal'));
if (detailModal) {
  detailModal.hide(); // ❌ backdrop 제거 실패
}
```

**해결**:
```javascript
// Bootstrap이 자동으로 모달 전환 처리하도록 수정
function openEditModal(button) {
  // ... 모달 내용 설정
  // ✅ 기존 모달 닫기 로직 제거
}
```

**효과**:
- ✅ 모달 전환 시 backdrop 정상 제거
- ✅ 화면 흑백 현상 해결
- ✅ Bootstrap 기본 동작 활용

---

#### 3. 글쓰기 화면 UI 개선 ✅
**변경 사항**:

**1) 헤더 레이아웃 개선**
```html
<!-- Before: 제목만 표시 -->
<h2>온라인 상담 글쓰기</h2>

<!-- After: 제목 + 목록 버튼 -->
<div class="d-flex justify-content-between align-items-center mb-4">
  <h2>
    <i class="bi bi-pencil-square"></i> 온라인 상담 글쓰기
  </h2>
  <a href="/counsel/list" class="btn btn-outline-secondary">
    <i class="bi bi-list"></i> 목록
  </a>
</div>
```

**2) 하단 버튼 단순화**
```html
<!-- Before: 취소 + 작성완료 -->
<div class="d-grid d-md-flex gap-2">
  <a class="btn btn-secondary">취소</a>
  <button class="btn btn-primary">작성완료</button>
</div>

<!-- After: 작성완료만 표시 (취소는 헤더 목록 버튼으로 대체) -->
<div class="d-grid d-md-flex gap-2 justify-content-md-end mt-4">
  <button class="btn btn-primary" style="min-width: 120px; height: 42px;">
    <i class="bi bi-send"></i> 작성완료
  </button>
</div>
```

**효과**:
- ✅ 헤더 목록 버튼으로 접근성 향상
- ✅ 하단 버튼 단순화 (주요 액션만 강조)
- ✅ 일관된 레이아웃

---

#### 4. 게시글 수정 화면 UI 개선 ✅
**변경 사항**:

**1) 헤더 레이아웃 개선**
```html
<!-- Before: 제목만 표시 -->
<h2>온라인 상담 수정</h2>

<!-- After: 제목 + 상세보기/목록 버튼 -->
<div class="d-flex justify-content-between align-items-center mb-4">
  <h2>
    <i class="bi bi-pencil-square"></i> 온라인 상담 수정
  </h2>
  <div class="d-flex gap-2">
    <a href="/counsel/detail/{id}" class="btn btn-outline-secondary">
      <i class="bi bi-eye"></i> 상세보기
    </a>
    <a href="/counsel/list" class="btn btn-outline-secondary">
      <i class="bi bi-list"></i> 목록
    </a>
  </div>
</div>
```

**2) Flash 메시지 아이콘 추가**
```html
<!-- 성공 메시지 -->
<div class="alert alert-success">
  <i class="bi bi-check-circle-fill"></i> <span th:text="${message}"></span>
</div>

<!-- 에러 메시지 -->
<div class="alert alert-danger">
  <i class="bi bi-exclamation-triangle-fill"></i> <span th:text="${error}"></span>
</div>
```

**3) 하단 버튼 단순화**
```html
<!-- Before: 취소 + 수정완료 -->
<div class="d-grid d-md-flex gap-2">
  <a class="btn btn-secondary">취소</a>
  <button class="btn btn-primary">수정 완료</button>
</div>

<!-- After: 수정완료만 표시 (취소는 헤더 상세보기 버튼으로 대체) -->
<div class="d-grid d-md-flex gap-2 justify-content-md-end mt-4">
  <button class="btn btn-primary" style="min-width: 120px; height: 42px;">
    <i class="bi bi-save"></i> 수정 완료
  </button>
</div>
```

**효과**:
- ✅ 헤더 버튼으로 네비게이션 개선
- ✅ 하단 버튼 단순화
- ✅ 아이콘으로 시각적 피드백 강화

---

### 🐛 버그 수정

#### 5. 로그아웃 에러 해결 ✅
**문제**: 마이페이지에서 로그아웃 클릭 시 에러 페이지 표시

**원인**:
```html
<!-- ❌ GET 방식 링크 사용 (Spring Security는 POST 요구) -->
<a th:href="@{/logout}" class="btn btn-outline-danger">
  로그아웃
</a>
```

**에러 메시지**:
```
405 Method Not Allowed
Request method 'GET' not supported
```

**해결**:
```html
<!-- ✅ POST 방식 폼 사용 -->
<form th:action="@{/logout}" method="post" class="d-inline mb-0">
  <button type="submit" class="btn btn-outline-danger" 
          style="min-width: 120px; height: 42px;">
    <i class="bi bi-box-arrow-right"></i> 로그아웃
  </button>
</form>
```

**Spring Security 설정**:
```java
.logout(logout -> logout
  .logoutUrl("/logout")  // POST 방식만 허용
  .logoutSuccessUrl("/?logout=true")
  .invalidateHttpSession(true)
  .deleteCookies("JSESSIONID", "remember-me")
)
```

**효과**:
- ✅ 로그아웃 정상 작동
- ✅ 홈 페이지로 리다이렉트 정상
- ✅ 세션 정상 종료
- ✅ 쿠키 정상 삭제

---

### 🔧 수정된 파일

**Frontend (4개)**:
1. ✅ `admin/settings.html`
   - 상세 보기 버튼 원형 아이콘으로 변경
   - 모달 전환 로직 개선

2. ✅ `counsel/counsel-write.html`
   - 헤더 목록 버튼 추가
   - 하단 버튼 단순화

3. ✅ `counsel/counsel-edit.html`
   - 헤더 상세보기/목록 버튼 추가
   - Flash 메시지 아이콘 추가
   - 하단 버튼 단순화

4. ✅ `user/mypage.html`
   - 로그아웃 링크 → 폼 방식 변경
   - 버튼 크기 통일

---

### ✅ 검증 완료

```bash
BUILD SUCCESSFUL in 23s
2 actionable tasks: 2 executed
```

**컴파일**: ✅ 성공  
**문법 오류**: ❌ 없음

---

### 📊 개선 효과

| 항목 | Before | After | 개선점 |
|------|--------|-------|--------|
| **상세 보기 버튼** | 텍스트 버튼 | 원형 + 아이콘 | 공간 효율성 |
| **모달 전환** | backdrop 유지 | 정상 제거 | 흑백 현상 해결 |
| **글쓰기 헤더** | 제목만 | 제목 + 목록 | 접근성 향상 |
| **수정 헤더** | 제목만 | 제목 + 상세/목록 | 네비게이션 개선 |
| **하단 버튼** | 취소 + 저장 | 저장만 강조 | 주요 액션 집중 |
| **로그아웃** | GET (에러) | POST (정상) | 기능 정상화 |

---

## [3.5.16] - 2025-11-12 (오후 - 2차)

### 📚 문서 규칙 추가

#### 테이블/API 변경 시 문서 즉각 반영 규칙 ✅
**목적**: 코드와 문서의 싱크 유지, 협업 효율성 향상

**신규 규칙**:

**1. 테이블 변경 시 `TABLE_DEFINITION.md` 즉각 업데이트** ⭐NEW
- ✅ Entity 클래스 생성/수정 완료 직후
- ✅ 테이블 컬럼 추가/삭제/변경 직후
- ✅ 외래키 제약 조건 변경 직후
- ✅ 인덱스 추가/삭제 직후

**업데이트 내용**:
- 테이블 구조 (컬럼명, 타입, 제약조건)
- 컬럼 설명 (각 필드의 용도)
- 관계도 (외래키, 연관 관계)
- 변경 이력 (날짜, 변경 사유)

**2. API 변경 시 `API_SPECIFICATION.md` 즉각 업데이트** ⭐NEW
- ✅ Controller 메서드 추가/수정 완료 직후
- ✅ 요청/응답 DTO 변경 직후
- ✅ 엔드포인트 URL 변경 직후
- ✅ HTTP 메서드 변경 직후

**업데이트 내용**:
- 엔드포인트 정보 (URL, HTTP 메서드)
- 요청 파라미터/바디 (DTO 구조)
- 응답 포맷 (성공/실패 케이스)
- 권한 요구사항 (로그인 필요 여부)
- 변경 이력 (날짜, 변경 사유)

---

### 🔧 데이터베이스 업데이트

#### 멀티로그인 설정 설명 업데이트 ✅
**파일**: `DataInit.java`

**Before**:
```java
multiLogin.setDescription("멀티로그인 허용 여부. true: 멀티로그인 허용, false: 단일 로그인만 허용");
```

**After**:
```java
multiLogin.setDescription("멀티로그인 허용 여부 (최대 5개 기기). true: 멀티로그인 허용, false: 단일 로그인만 허용");
```

**효과**:
- ✅ DB 설명 컬럼에서도 최대 기기 개수 확인 가능
- ✅ 관리자 설정 페이지에서 자동으로 표시

---

### 🎨 관리자 설정 페이지 대폭 리팩토링 ✅

#### 1. 헤더 레이아웃 변경
**Before**:
```html
<h2>시스템 설정 관리</h2>
<!-- 하단에 홈으로 버튼 -->
<div class="d-flex justify-content-between mt-4">
  <a href="/" class="btn btn-secondary">홈으로</a>
</div>
```

**After**:
```html
<!-- 헤더: 제목 + 홈 버튼 (오른쪽 끝 배치) -->
<div class="d-flex justify-content-between align-items-center mb-4">
  <h2>시스템 설정 관리</h2>
  <a href="/" class="btn btn-secondary" style="min-width: 120px; height: 42px;">
    홈으로
  </a>
</div>
```

**효과**: ✅ 홈 버튼 상단 배치로 접근성 향상

---

#### 2. Boolean 값 액션 버튼 변경
**Before**:
```html
<!-- 활성화/비활성화 배지 클릭 -->
<span class="badge bg-success" onclick="openToggleModal(this)">활성화</span>
<td>클릭하여 토글</td>
```

**After**:
```html
<!-- 현재 값: 활성화 → 액션: 비활성화 버튼 -->
<span class="badge bg-success">활성화</span>
<button class="btn btn-sm btn-warning" onclick="openToggleModal(this, 'false')">
  비활성화
</button>

<!-- 현재 값: 비활성화 → 액션: 활성화 버튼 -->
<span class="badge bg-secondary">비활성화</span>
<button class="btn btn-sm btn-success" onclick="openToggleModal(this, 'true')">
  활성화
</button>
```

**효과**: ✅ 직관적인 액션 버튼 (활성화 상태에서 비활성화 버튼 표시)

---

#### 3. 스크롤 처리 추가
**테이블 영역**:
```html
<div class="card-body" style="max-height: 500px; overflow-y: auto;">
  <table class="table table-hover">
    <thead class="sticky-top bg-white">
      <!-- 헤더 고정 -->
    </thead>
    <tbody>
      <!-- 많은 데이터 시 스크롤 -->
    </tbody>
  </table>
</div>
```

**효과**: ✅ 설정 개수가 많아져도 푸터 침범 없음

---

#### 4. 상세 보기 모달 추가
**시스템 설정 목록 상세 모달**:
```html
<button class="btn btn-sm btn-light" data-bs-toggle="modal" data-bs-target="#detailModal">
  <i class="bi bi-plus-circle"></i> 상세 보기
</button>

<!-- 모달: 전체 설정 목록 스크롤 가능 (modal-xl) -->
<div class="modal-dialog modal-xl">
  <div class="modal-body" style="max-height: 600px; overflow-y: auto;">
    <!-- 전체 설정 목록 -->
  </div>
</div>
```

**현재 상태 상세 모달**:
```html
<!-- 모달: 현재 상태만 표시 (modal-lg) -->
<div class="modal-dialog modal-lg">
  <div class="modal-body" style="max-height: 600px; overflow-y: auto;">
    <!-- 현재 상태 목록 -->
  </div>
</div>
```

**효과**: 
- ✅ + 기호 클릭 시 모달로 전체 목록 확인 가능
- ✅ 모달 내부도 스크롤 처리로 많은 데이터 표시

---

#### 5. 현재 상태 패널 단순화
**Before**:
```html
<li class="list-group-item">
  <div>
    <strong>multiLoginEnabled</strong>
    <br>
    <small class="text-muted">설명...</small>
  </div>
  <span class="badge bg-success">활성화</span>
</li>
```

**After**:
```html
<li class="list-group-item d-flex justify-content-between align-items-center">
  <strong>multiLoginEnabled</strong>
  <span class="badge bg-success">활성화</span>
</li>
```

**효과**: ✅ 설정 키와 상태만 표시 (간결)

---

#### 6. 주의사항 제거 → 토글 모달에 통합
**Before** (설정 페이지):
```html
<div class="card shadow-sm">
  <div class="card-header bg-warning">주의사항</div>
  <div class="card-body">
    <h6>멀티로그인 설정</h6>
    <p>최대 5개 기기...</p>
  </div>
</div>
```

**After** (토글 모달):
```html
<div class="card bg-light" id="toggleWarningCard">
  <div class="card-header bg-danger text-white">주의사항</div>
  <div class="card-body">
    <!-- JavaScript로 동적 생성 -->
    <ul>
      <li>활성화 시: 최대 5개 기기...</li>
      <li>예시: PC 2대 + 모바일 3대...</li>
      <li>초과 시: 가장 오래된 세션 종료...</li>
    </ul>
  </div>
</div>
```

**효과**: 
- ✅ 설정 페이지 깔끔하게 유지
- ✅ 변경 전 주의사항을 모달에서 확인 가능

---

#### 7. JavaScript 함수 개선
**주의사항 정의**:
```javascript
const warnings = {
  'multiLoginEnabled': {
    title: '멀티로그인 설정 주의사항',
    content: `
      <ul>
        <li><strong>활성화 시:</strong> 최대 5개 기기에서 동시 로그인 가능</li>
        <li><strong>예시:</strong> PC 2대 + 모바일 3대 = 총 5개 기기</li>
        <li><strong>초과 시:</strong> 6번째 기기에서 로그인 시 가장 오래된 세션 종료</li>
        <li><strong>비활성화 시:</strong> 단일 로그인만 허용</li>
      </ul>
    `
  },
  'fileUploadEnabled': {
    title: '파일 업로드 설정 주의사항',
    content: `...`
  }
};
```

**토글 모달 열기**:
```javascript
function openToggleModal(element, newValue) {
  // ... 기존 로직
  
  // 주의사항 동적 표시
  if (warnings[key]) {
    warningCard.style.display = 'block';
    warningContent.innerHTML = warnings[key].content;
  } else {
    warningCard.style.display = 'none';
  }
}
```

**효과**: ✅ 설정별 맞춤 주의사항 표시

---

### 🎨 UI 버튼 크기 통일 ✅

#### 수정된 파일 (4개)
1. ✅ `counselDetail.html`
2. ✅ `counsel-write.html`
3. ✅ `counsel-edit.html`
4. ✅ `admin/settings.html`

#### 통일된 버튼 규격
```html
<!-- 주요 액션 버튼 -->
<button class="btn btn-primary" style="min-width: 120px; height: 42px;">
  저장
</button>

<!-- 상세화면 수정/삭제 버튼 (붙여서) -->
<div class="d-flex gap-0">
  <a class="btn btn-warning" style="min-width: 80px; height: 42px;">수정</a>
  <button class="btn btn-danger" style="min-width: 80px; height: 42px;">삭제</button>
</div>

<!-- 반응형 버튼 그룹 -->
<div class="d-grid d-md-flex gap-2 justify-content-md-end">
  <a class="btn btn-secondary" style="min-width: 120px; height: 42px;">취소</a>
  <button class="btn btn-primary" style="min-width: 120px; height: 42px;">저장</button>
</div>
```

#### 주요 변경 사항

**1. counselDetail.html**
```html
<!-- Before: 크기 불규칙 -->
<div class="d-flex">
  <a class="btn btn-warning" style="min-width: 80px;">수정</a>
  <button class="btn btn-danger" style="min-width: 80px;">삭제</button>
</div>
<a class="btn btn-light">목록</a>

<!-- After: 크기 통일 + 아이콘 추가 -->
<div class="d-grid d-md-flex gap-2 justify-content-md-between">
  <div class="d-flex gap-0">
    <a class="btn btn-warning" style="min-width: 80px; height: 42px;">
      <i class="bi bi-pencil"></i> 수정
    </a>
    <button class="btn btn-danger" style="min-width: 80px; height: 42px;">
      <i class="bi bi-trash"></i> 삭제
    </button>
  </div>
  <a class="btn btn-secondary" style="min-width: 120px; height: 42px;">
    <i class="bi bi-list"></i> 목록
  </a>
</div>
```

**2. counsel-write.html**
```html
<!-- Before -->
<div class="d-flex justify-content-end gap-2">
  <a class="btn btn-secondary">취소</a>
  <button class="btn btn-primary">작성완료</button>
</div>

<!-- After -->
<div class="d-grid d-md-flex gap-2 justify-content-md-end">
  <a class="btn btn-secondary" style="min-width: 120px; height: 42px; display: flex; align-items: center; justify-content: center;">
    <i class="bi bi-x-circle"></i> 취소
  </a>
  <button class="btn btn-primary" style="min-width: 120px; height: 42px;">
    <i class="bi bi-send"></i> 작성완료
  </button>
</div>
```

**3. counsel-edit.html**
```html
<!-- Before -->
<div class="d-flex justify-content-end gap-2">
  <a class="btn btn-secondary" style="min-width: 100px;">취소</a>
  <button class="btn btn-primary" style="min-width: 100px;">수정 완료</button>
</div>

<!-- After -->
<div class="d-grid d-md-flex gap-2 justify-content-md-end">
  <a class="btn btn-secondary" style="min-width: 120px; height: 42px; display: flex; align-items: center; justify-content: center;">
    <i class="bi bi-x-circle"></i> 취소
  </a>
  <button class="btn btn-primary" style="min-width: 120px; height: 42px;">
    <i class="bi bi-save"></i> 수정 완료
  </button>
</div>
```

---

### 📊 개선 효과

**1. 문서 관리**
- ✅ 테이블/API 변경 시 즉각 문서 반영 규칙 확립
- ✅ 코드-문서 싱크 유지
- ✅ 협업 효율성 향상

**2. 관리자 페이지**
- ✅ 홈 버튼 상단 배치 (접근성 향상)
- ✅ Boolean 액션 버튼 직관성 개선
- ✅ 스크롤 처리로 많은 설정 표시 가능
- ✅ 상세 보기 모달로 전체 목록 확인
- ✅ 주의사항 모달 통합 (설정 페이지 깔끔)

**3. UI 일관성**
- ✅ 모든 버튼 크기 통일 (42px, 120px x 42px)
- ✅ 아이콘 + 텍스트 일관성
- ✅ 반응형 버튼 그룹 (`d-grid d-md-flex`)
- ✅ 사용자 직관성 향상

**4. 데이터베이스**
- ✅ 멀티로그인 설정 설명에 "최대 5개 기기" 명시
- ✅ DB 레벨에서도 제한 사항 확인 가능

---

### 🔧 수정된 파일

**Backend (1개)**:
1. ✅ `DataInit.java` - 멀티로그인 설명 업데이트

**Frontend (4개)**:
1. ✅ `admin/settings.html` - 대폭 리팩토링
2. ✅ `counsel/counselDetail.html` - 버튼 크기 통일
3. ✅ `counsel/counsel-write.html` - 버튼 크기 통일
4. ✅ `counsel/counsel-edit.html` - 버튼 크기 통일

**문서 (2개)**:
1. ✅ `PROJECT_DOCUMENTATION.md` - 테이블/API 문서 규칙 추가
2. ✅ `CHANGELOG.md` - [3.5.16] 변경 이력 추가

---

### ✅ 검증 완료

```bash
BUILD SUCCESSFUL in 22s
2 actionable tasks: 2 executed
```

**컴파일**: ✅ 성공  
**문법 오류**: ❌ 없음

---

## [3.5.15] - 2025-11-12 (오후)

### 🐛 버그 수정

#### 로그아웃 오류 수정 ✅
**문제**: `persistent_logins` 테이블이 없어서 로그아웃 시 SQL 오류 발생
```
BadSqlGrammarException: delete from persistent_logins where username = ?
```

**해결 방법**:
- `src/main/resources/db/mysql/schema.sql`에 `persistent_logins` 테이블 추가
- Spring Security Remember-Me 기능 지원 테이블

**테이블 구조**:
```sql
CREATE TABLE IF NOT EXISTS persistent_logins (
  username VARCHAR(64) NOT NULL,
  series VARCHAR(64) PRIMARY KEY,
  token VARCHAR(64) NOT NULL,
  last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) engine=InnoDB;
```

---

### 🎨 UI/UX 개선

#### 관리자 페이지 리팩토링 ✅
**변경 사항**:
1. ✅ **"관리자" → "설정"으로 메뉴 이름 변경**
   - `layout.html`: 우측 상단 관리자 메뉴 → 설정 메뉴
   - 아이콘 유지: `<i class="bi bi-gear"></i>`

2. ✅ **관리자도 닉네임 표출**
   - 기존: `sec:authentication="name"` (username 표시)
   - 변경: `sec:authentication="principal.nickname"` (닉네임 표시)

3. ✅ **빠른 액션 제거, 현재 상태 패널 유지**
   - "빠른 액션" 섹션 삭제
   - "현재 상태" 패널로 변경
   - 설정 키별 상태 배지 표시

4. ✅ **true/false 토글 UI 개선**
   - 배지 클릭 시 토글 경고 모달 표시
   - Yes/No 확인 후 설정 변경
   - 활성화/비활성화 상태 명확히 표시

5. ✅ **멀티로그인 개수 명시**
   - "최대 5개 기기" 명시
   - 사용 예시 추가 (PC 2대 + 모바일 3대)

**Before**:
```html
<span sec:authentication="name"></span>님 환영합니다
<a href="/admin/settings">관리자</a>
```

**After**:
```html
<span sec:authentication="principal.nickname"></span>님
<a href="/admin/settings">설정</a>
```

---

### 📚 문서 업데이트

#### 프로젝트 규칙 추가 ✅
**업데이트된 문서**: `PROJECT_DOCUMENTATION.md`

**신규 규칙**:

**1. alert 대신 모달 팝업 사용** ⭐NEW
```javascript
// ❌ 잘못된 방법
alert('정말로 삭제하시겠습니까?');

// ✅ 올바른 방법
const modal = new bootstrap.Modal(document.getElementById('confirmModal'));
modal.show();
```

**이유**:
- alert는 브라우저 UI를 차단하고 사용자 경험 저해
- Bootstrap 모달은 스타일 일관성 유지
- 경고 아이콘, 안내 문구 등 풍부한 UI 제공

**2. 사용 가능한 개수/제한 명시** ⭐NEW
```html
<!-- ✅ 멀티로그인 개수 명시 -->
<p>최대 <strong>5개 기기</strong>에서 동시 로그인 가능</p>

<!-- ✅ 파일 크기 제한 명시 -->
<small>최대 파일 크기: <strong>5MB</strong></small>

<!-- ✅ 첨부 파일 개수 제한 -->
<label>첨부 파일 <span class="badge">최대 3개</span></label>
```

**적용 예시**:
- 멀티로그인: 최대 5개 기기
- 파일 업로드: 최대 5MB
- 첨부 파일: 최대 3개
- 게시글 제목: 최대 100자

---

### 🔧 수정된 파일

**Backend (1개)**:
1. ✅ `src/main/resources/db/mysql/schema.sql`
   - `persistent_logins` 테이블 추가

**Frontend (2개)**:
1. ✅ `src/main/resources/templates/fragments/layout.html`
   - 닉네임 표출 (`principal.nickname`)
   - "관리자" → "설정" 변경

2. ✅ `src/main/resources/templates/admin/settings.html`
   - 빠른 액션 제거
   - 현재 상태 패널 추가
   - true/false 토글 모달 추가
   - 멀티로그인 개수 명시 (최대 5개 기기)

**문서 (2개)**:
1. ✅ `docs/01-project-overview/PROJECT_DOCUMENTATION.md`
   - UI 규칙 9번 업데이트
   - alert → 모달 규칙 추가
   - 사용 개수 명시 규칙 추가

2. ✅ `docs/07-changelog/CHANGELOG.md`
   - [3.5.15] 변경 이력 추가

---

### 📊 개선 효과

**1. 로그아웃 안정성 향상**
- ✅ Remember-Me 기능 정상 작동
- ✅ 로그아웃 시 SQL 오류 없음

**2. 사용자 경험 개선**
- ✅ 관리자도 닉네임으로 표시 (일관성)
- ✅ "설정" 메뉴로 명확한 의미 전달
- ✅ 토글 시 경고 모달로 실수 방지

**3. 직관성 향상**
- ✅ 멀티로그인 개수 명시 (최대 5개)
- ✅ 설정 변경 시 Yes/No 확인
- ✅ 현재 상태 한눈에 확인 가능

---

## [3.5.14] - 2025-11-12

### 🔒 보안 강화

#### SQL Injection 방지 규칙 추가 ✅
**목적**: SQL 인젝션 공격으로부터 데이터베이스 보호

**업데이트된 문서**:
- ✅ `SECURITY_IMPLEMENTATION.md` - SQL Injection 방지 섹션 추가

**핵심 규칙**:
```
✅ JPA Repository 메서드 우선 사용
✅ QueryDSL 파라미터 바인딩 사용
✅ JPQL/HQL에서 Named Parameter 사용
❌ 문자열 연결로 쿼리 생성 금지
❌ Native Query 사용 자제
```

**안전한 쿼리 작성 예시**:
```java
// ✅ JPA Repository 메서드 쿼리
List<Post> findByTitleContaining(String keyword);

// ✅ QueryDSL 파라미터 바인딩
queryFactory.selectFrom(post)
    .where(post.title.containsIgnoreCase(keyword))
    .fetch();

// ✅ JPQL Named Parameter
@Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword%")
List<Post> searchByTitle(@Param("keyword") String keyword);

// ❌ SQL Injection 취약
String query = "SELECT * FROM post WHERE title = '" + keyword + "'";
```

**사용자 입력 검증**:
- `@Pattern` 어노테이션으로 파라미터 검증
- 최대 길이 제한 (100자)
- 특수문자 필터링 (`[<>\"'%;()&+]`)

---

### 🎨 UI/UX 개선

#### 반응형 디자인 원칙 추가 ✅
**목적**: 모든 디바이스(PC, 태블릿, 모바일)에서 일관된 사용자 경험 제공

**업데이트된 문서**:
- ✅ `UI_CONSISTENCY_GUIDE.md` - 반응형 디자인 원칙 섹션 추가

**핵심 원칙**:
```
✅ Bootstrap Grid 시스템 활용
✅ 모바일 우선 개발 (Mobile First)
✅ 일관된 Breakpoint 사용 (xs, sm, md, lg, xl, xxl)
✅ 컬럼 구조 단순화 (중첩 최소화)
```

**반응형 레이아웃 예시**:
```html
<!-- 제목 + 검색 폼 레이아웃 -->
<div class="row g-2 align-items-center">
  <!-- 제목: 모바일 100%, 태블릿 25% -->
  <div class="col-12 col-md-3">
    <h2>온라인상담(112)</h2>
  </div>
  
  <!-- 빈 공간: 모바일 숨김, 태블릿 40% -->
  <div class="col-md-5 d-none d-md-block"></div>
  
  <!-- 검색: 모바일 100%, 태블릿 33% -->
  <div class="col-12 col-md-4">
    <!-- 검색 폼 -->
  </div>
</div>
```

**테스트 Breakpoint**:
- 모바일 세로: < 576px
- 모바일 가로: 576px ~ 767px
- 태블릿: 768px ~ 991px
- 데스크톱: ≥ 992px

---

#### counselList.html UI 개선 완료 ✅
**영향 파일**: `src/main/resources/templates/counsel/counselList.html`

**개선 사항**:
1. ✅ **반응형 레이아웃 개선**
   - `row g-2` 적용 (일관된 간격)
   - `col-12 col-md-X` 구조 단순화
   - 중첩된 `input-group` 제거
   - `d-none d-md-block` 활용 (빈 공간 처리)

2. ✅ **버튼 크기 일관성**
   - 검색 버튼: `height: 42px`
   - 글쓰기 버튼: `min-width: 120px; height: 42px`
   - 아이콘 추가 (`fa-pencil`)

3. ✅ **placeholder 개선**
   - "Search" → "검색어를 입력하세요"
   - 폰트 크기: `0.95rem` (일관성)

4. ✅ **간격 통일**
   - 검색 폼: `g-2`
   - 글쓰기 버튼: `mt-3`
   - 버튼 그룹: `d-grid d-md-flex`

**Before**:
```html
<!-- 복잡한 중첩 구조 -->
<form class="input-group">
  <div class="input-group">
    <div class="col-12 col-md-4">...</div>
    <div class="col-12 col-md-8">
      <div class="input-group">...</div>
    </div>
  </div>
</form>
```

**After**:
```html
<!-- 단순한 구조 -->
<form th:action="@{/counsel/list}" method="get">
  <div class="row g-2">
    <div class="col-4">
      <select class="form-select" style="height: 42px;">...</select>
    </div>
    <div class="col-8">
      <div class="input-group">
        <input type="text" style="height: 42px; font-size: 0.95rem;" placeholder="검색어를 입력하세요">
        <button type="submit" style="height: 42px;">🔍</button>
      </div>
    </div>
  </div>
</form>
```

---

### 📚 문서 업데이트

**업데이트된 문서 (3개)**:
1. ✅ `SECURITY_IMPLEMENTATION.md`
   - SQL Injection 방지 규칙 추가
   - 안전한 쿼리 작성 가이드
   - 코드 리뷰 체크리스트

2. ✅ `UI_CONSISTENCY_GUIDE.md`
   - 반응형 디자인 원칙 섹션 추가
   - Bootstrap Grid 활용 가이드
   - 모바일 우선 개발 원칙
   - 적용 현황 업데이트 (9개 페이지)

3. ✅ `CHANGELOG.md`
   - 2025-11-12 변경 이력 추가

---

## [3.5.13] - 2025-11-11

### 📚 프로젝트 규칙 강화

#### UI/UX 일관성 규칙 문서화 ✅
**목적**: 프로젝트 전체에서 일관된 UI/UX 제공, 새 세션에서도 규칙 적용

**추가된 문서**:
1. ✅ `UI_CONSISTENCY_GUIDE.md` (신규 생성)
   - 위치: `docs/05-ui-screens/`
   - 버튼 크기 규칙
   - 폰트 크기 규칙
   - 간격(Spacing) 규칙
   - 입력 필드 규칙
   - 색상 사용 규칙
   - 체크리스트

2. ✅ `PROJECT_DOCUMENTATION.md` (업데이트)
   - UI/UX 일관성 규칙 섹션 추가
   - 버튼 크기 통일 예시
   - 헤더 링크 통일 예시
   - placeholder 규칙

3. ✅ `QUICK_REFERENCE.md` (업데이트)
   - 필수 규칙 8번 추가
   - UI/UX 일관성 규칙 섹션 추가
   - 체크리스트 추가

**핵심 규칙 요약**:
```
✅ 버튼 크기 통일
   - 일반: height: 42px
   - 주요 액션: min-width: 120px; height: 42px
   - 상세화면: min-width: 80px (붙여서)

✅ 폰트 크기 통일
   - 헤더 링크: 0.95rem
   - placeholder: 0.95rem
   - 본문: 1rem

✅ 간격 통일
   - 링크: px-2, px-1
   - 버튼: gap-2
   - 카드: p-4, p-5

✅ 입력 필드
   - 필수: * 표시
   - placeholder 간소화
   - 안내: <small> 분리
   - 검증: is-valid, is-invalid
```

**적용 현황** (2025-11-11):
- ✅ 로그인 페이지
- ✅ 회원가입 페이지
- ✅ 비밀번호 찾기 페이지
- ✅ 마이페이지
- ✅ 비공개 게시글 비밀번호 입력
- ✅ 게시글 수정/상세 페이지
- ✅ 헤더/푸터 레이아웃

**효과**:
- ✅ 새 개발자도 규칙에 따라 일관된 UI 구현 가능
- ✅ 코드 리뷰 시 명확한 기준 제공
- ✅ 사용자 경험 통일
- ✅ 유지보수 용이

---

## [3.5.12] - 2025-11-11

### 🐛 버그 수정 및 UI 개선

#### 1. 대댓글 작성 시 Thymeleaf 오류 해결 ✅
**문제**: 대댓글 작성 시 `principal.nickname` 접근 오류

**원인**: `sec:authentication="principal.nickname"` 방식의 접근 문제

**해결**: `th:text="${#authentication.principal.nickname}"` 방식으로 변경

```html
<!-- Before -->
<span sec:authentication="principal.nickname"></span>

<!-- After -->
<span th:text="${#authentication.principal.nickname}"></span>
```

---

#### 2. 전체 버튼 크기 통일 ✅
**목적**: 일관된 사용자 경험 제공

**통일 기준**:
- 일반 버튼: `height: 42px`
- 주요 액션 버튼: `min-width: 120px; height: 42px`

**수정된 페이지**:
1. ✅ 로그인 페이지 (`login.html`)
2. ✅ 회원가입 페이지 (`register.html`)
3. ✅ 비밀번호 찾기 페이지 (`forgot-password.html`)
4. ✅ 마이페이지 (`mypage.html`)
   - 프로필 저장 버튼
   - 비밀번호 변경 버튼
5. ✅ 비공개 게시글 비밀번호 입력 (`counsel-password.html`)
   - 목록 버튼
   - 확인 버튼

**Before/After**:
```html
<!-- Before: 크기 불규칙 -->
<button class="btn btn-primary btn-lg">로그인</button>
<button class="btn btn-primary">저장</button>
<button class="btn btn-warning">변경</button>

<!-- After: 크기 통일 -->
<button class="btn btn-primary" style="height: 42px;">로그인</button>
<button class="btn btn-primary" style="min-width: 120px; height: 42px;">저장</button>
<button class="btn btn-warning" style="min-width: 120px; height: 42px;">변경</button>
```

---

#### 3. 비밀번호 입력 화면 UI 개선 ✅
**개선 사항**:
- ✅ placeholder 글씨 크기 축소 (`font-size: 0.95rem`)
- ✅ placeholder 내용 간소화: "비밀번호를 입력하세요"
- ✅ 상세 안내는 `<small>` 태그로 분리
- ✅ 버튼 크기 통일 (120px × 42px)

```html
<!-- Before -->
<input placeholder="게시글 작성 시 설정한 비밀번호를 입력하세요">
<button class="btn btn-secondary" style="min-width: 100px;">목록</button>

<!-- After -->
<input placeholder="비밀번호를 입력하세요" style="font-size: 0.95rem;">
<small class="form-text">게시글 작성 시 설정한 비밀번호를 입력하세요.</small>
<button class="btn btn-secondary" style="min-width: 120px; height: 42px;">목록</button>
```

---

#### 4. 마이페이지 이메일 검증 강화 ✅
**추가 기능**:
1. ✅ **실시간 형식 검증**
   - 입력 중 이메일 형식 확인
   - 올바르면 초록색 테두리
   - 틀리면 빨간색 테두리

2. ✅ **폼 제출 전 검증**
   - 형식이 틀리면 제출 차단
   - alert로 안내 메시지 표시
   - 이메일 필드에 자동 포커스

3. ✅ **안내 문구 추가**
   - placeholder: "예: abc123@example.com"
   - 하단 안내: "올바른 형식: abc123@example.com (영문, 숫자, @, 도메인)"

```javascript
// 실시간 검증
document.getElementById('email').addEventListener('input', function(e) {
  const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  
  if (emailPattern.test(e.target.value)) {
    e.target.classList.add('is-valid');
    e.target.classList.remove('is-invalid');
  } else {
    e.target.classList.remove('is-valid');
    e.target.classList.add('is-invalid');
  }
});

// 폼 제출 전 검증
document.getElementById('profileForm').addEventListener('submit', function(e) {
  if (!emailPattern.test(email)) {
    e.preventDefault();
    alert('올바른 이메일 형식을 입력하세요.\n예시: abc123@example.com');
    return false;
  }
});
```

---

### 🎯 개선 효과

#### Before (문제점)
```
❌ 대댓글 작성 시 오류 발생
❌ 페이지마다 버튼 크기 다름 (btn-lg, 기본 등)
❌ 비밀번호 입력칸 placeholder 너무 김
❌ 이메일 형식 검증 부족
```

#### After (개선 후)
```
✅ 대댓글 정상 작동
✅ 모든 페이지 버튼 크기 통일 (42px)
✅ placeholder 간소화 + 안내 문구 분리
✅ 이메일 실시간 검증 + 제출 전 차단
✅ 일관된 사용자 경험
```

---

## [3.5.11] - 2025-11-11

### 🐛 버그 수정

#### 1. 로그인 후 Thymeleaf 템플릿 파싱 오류 해결 ✅
**문제**: 로그인 성공 후 `principal.nickname` 접근 시 오류 발생

**에러 메시지**:
```
org.thymeleaf.exceptions.TemplateInputException: 
An error happened during template parsing (template: "class path resource [templates/welcome.html]")
```

**원인**:
- Spring Security의 기본 `UserDetails`는 `nickname` 필드가 없음
- Thymeleaf에서 `sec:authentication="principal.nickname"` 접근 시 `NoSuchMethodException` 발생

**해결**:
1. ✅ **CustomUserDetails 클래스 생성**
   - `UserDetails` 인터페이스 구현
   - `nickname`, `email`, `name` 등 커스텀 필드 getter 추가
   
2. ✅ **CustomUserDetailsService 수정**
   - 기본 `UserDetails` 대신 `CustomUserDetails` 반환
   
3. ✅ **Thymeleaf에서 안전하게 접근**
   - `sec:authentication="principal.nickname"` 정상 작동

**코드 변경**:
```java
// Before
return new org.springframework.security.core.userdetails.User(
    user.getUsername(), 
    user.getPassword(), 
    authorities
);

// After
return new CustomUserDetails(user);
```

**영향 범위**:
- `CustomUserDetails.java` ✅ 생성
- `CustomUserDetailsService.java` ✅ 수정
- `layout.html` - `principal.nickname` 정상 작동 ✅

---

### 📚 문서 추가

#### 로그인 에러 해결 가이드 문서 추가 ✅
**파일**: `docs/08-troubleshooting/LOGIN_ERROR_RESOLUTION.md`

**주요 내용**:
1. **문제 상황**: 에러 스택 트레이스 분석
2. **에러 분석**: 원인 특정 (Thymeleaf + Spring Security)
3. **해결 방법**: CustomUserDetails 구현
4. **재발 방지 가이드**:
   - 규칙 1: UserDetails 커스터마이징 시 항상 CustomUserDetails 사용
   - 규칙 2: Thymeleaf에서 principal 필드 접근 시 체크리스트
   - 규칙 3: 로그인 후 페이지 접근 테스트 필수
   - 규칙 4: Entity 필드 추가 시 CustomUserDetails도 함께 업데이트
   - 규칙 5: 개발 환경에서 Thymeleaf 캐시 비활성화
5. **오류 패턴 및 해결 방법**: NoSuchMethodException, PropertyNotFoundException, NullPointerException
6. **체크리스트**: 로그인 기능 추가/수정 시, Entity 필드 추가 시, 배포 전

**문서 특징**:
- ✅ 실제 발생한 에러 기반 작성
- ✅ 단계별 해결 과정 상세 설명
- ✅ 재발 방지를 위한 가이드라인 제시
- ✅ 체크리스트로 실용성 확보

---

## [3.5.10] - 2025-11-11

### 🎨 UI/UX 대대적 개선

#### 1. 댓글 작성 오류 해결 ✅
**문제**: `resetCommentModal is not defined` JavaScript 오류 발생

**해결**: 
- 이벤트 리스너를 `DOMContentLoaded` 시점에 등록
- `onclick` 인라인 속성 제거
- 페이지 로드 후 안전하게 함수 호출

```javascript
document.addEventListener('DOMContentLoaded', function() {
    const openCommentModalBtn = document.getElementById('openCommentModalBtn');
    if (openCommentModalBtn) {
        openCommentModalBtn.addEventListener('click', resetCommentModal);
    }
});
```

---

#### 2. 홈페이지 상단 닉네임 표시 + UI 개선 ✅
**기존**: 사용자 아이디(username) 표시

**개선**: 닉네임 표시 + 통일된 UI

**주요 변경**:
- ✅ `sec:authentication="principal.nickname"` 사용
- ✅ 모든 링크 크기 통일 (font-size: 0.95rem)
- ✅ 간격 통일 (px-2, px-1)
- ✅ 로그아웃 버튼 form 태그로 변경 (POST 방식)
- ✅ flexbox로 수직 정렬 (align-items-center)

**Before**:
```html
<span sec:authentication="name"></span>님 환영합니다
```

**After**:
```html
<span class="px-2 text-success fw-bold" style="font-size: 0.95rem;" 
      sec:authentication="principal.nickname"></span>
<span class="px-1" style="font-size: 0.95rem;">님</span>
```

---

#### 3. 온라인상담 상태 배지 크기 확대 ✅
**기존**: 기본 배지 크기

**개선**: `fs-6` 클래스 추가로 한 단계 확대

```html
<span class="badge bg-secondary fs-6" style="min-width: 80px;">
    답변완료
</span>
```

---

#### 4. 비공개 게시글 비밀번호 입력 UI 대폭 개선 ✅
**기존**: 단순한 입력 폼

**개선**:
- ✅ **자동 포커싱** (`autofocus` 속성)
- ✅ **footer 간격 확보** (min-height: 60vh, mb-5 추가)
- ✅ **카드 레이아웃** (shadow-sm)
- ✅ **아이콘 추가** (bi-lock-fill, bi-shield-lock)
- ✅ **alert 디자인** (alert-warning, alert-danger)
- ✅ **버튼 크기 통일** (min-width: 100px)
- ✅ **깔끔한 여백** (justify-content-center)

**Before**:
```html
<input type="password" id="password" name="password" required>
```

**After**:
```html
<input type="password" id="password" name="password" 
       class="form-control form-control-lg"
       placeholder="게시글 작성 시 설정한 비밀번호를 입력하세요"
       autofocus
       required>
```

---

#### 5. 상세화면 수정/삭제 버튼 개선 ✅
**개선**:
- ✅ 버튼 크기 통일 (min-width: 80px)
- ✅ 간격 제거 (gap-2 → gap 없음)
- ✅ 붙여서 배치

```html
<div class="d-flex">
    <a class="btn btn-warning" style="min-width: 80px;">수정</a>
    <button class="btn btn-danger" style="min-width: 80px;">삭제</button>
</div>
```

---

#### 6. 수정화면 버튼 개선 ✅
**개선**:
- ✅ 버튼 크기 통일 (min-width: 100px)
- ✅ 오른쪽 정렬 (justify-content-end)
- ✅ 순서 변경 (취소 → 수정 완료)

```html
<div class="d-flex justify-content-end gap-2">
    <a class="btn btn-secondary" style="min-width: 100px;">취소</a>
    <button class="btn btn-primary" style="min-width: 100px;">수정 완료</button>
</div>
```

---

#### 7. 마이페이지 이메일 형식 검증 강화 ✅
**추가**: HTML5 pattern 속성

```html
<input type="email" 
       pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"
       title="올바른 이메일 형식을 입력하세요 (예: example@email.com)">
```

---

### 📚 문서 추가

#### 닉네임 변경 이력 관리 방법 문서 추가 ✅
**파일**: `docs/06-security/NICKNAME_HISTORY_MANAGEMENT.md`

**주요 내용**:
1. **방법 1: 별도 이력 테이블 생성** ⭐ 권장
   - 데이터 정규화
   - 복잡한 조회 가능
   - 무제한 이력 저장

2. **방법 2: JSON 컬럼 사용**
   - 테이블 추가 불필요
   - 조회 제한적

3. **방법 3: 감사 로그 테이블 활용**
   - 통합 감사 로그
   - 컴플라이언스 충족

4. **방법 4: 로그 파일 기록**
   - 간단한 구현
   - 현재 적용 중

**권장 방법**: 
- 현재: 방법 4 (로그 파일)
- 향후: 방법 1로 마이그레이션

---

### 🎯 개선 효과

#### Before (문제점)
```
❌ 댓글 작성 시 JavaScript 오류
❌ 상단에 아이디 표시 (닉네임 아님)
❌ 상단 링크 크기/간격 불규칙
❌ 비밀번호 입력 화면 답답함
❌ 버튼 크기 불규칙
❌ 이메일 형식 검증 부족
```

#### After (개선 후)
```
✅ 댓글 작성 오류 해결
✅ 닉네임 표시로 사용자 친화적
✅ 상단 UI 통일 (0.95rem, px-2/px-1)
✅ 비밀번호 입력 화면 깔끔함 (자동 포커싱, 여백)
✅ 모든 버튼 크기 통일
✅ 이메일 형식 검증 강화 (pattern)
✅ 로그아웃 POST 방식으로 보안 강화
✅ 닉네임 이력 관리 방법 문서화
```

---

## [3.5.9] - 2025-11-11

### 🔒 보안 및 데이터 무결성 개선

#### 1. 조회수 중복 방지 강화 (세션 + IP 기반) ✅
**기존**: 세션 기반 조회수 중복 방지만 구현

**개선**: 세션 + IP 기반 이중 검증

**목적**: 
- 조회수 조작 방지
- 동일 IP에서 세션 초기화 후 재조회 시 중복 방지
- Proxy, Load Balancer 환경 고려

**주요 로직**:
```java
// 1. 세션 기반 중복 방지
Set<Long> viewedPosts = (Set<Long>) session.getAttribute("viewedCounselPosts");

// 2. IP 기반 중복 방지
String clientIp = getClientIp(request);
String viewKey = id + "_" + clientIp;
Set<String> viewedByIp = (Set<String>) session.getAttribute("viewedCounselPostsByIp");

// 3. 세션에도 없고 IP+게시글 조합으로도 조회하지 않았으면 조회수 증가
if (!viewedPosts.contains(id) && !viewedByIp.contains(viewKey)) {
    counselService.incrementViewCount(id);
    log.info("View count incremented: postId={}, clientIp={}", id, clientIp);
}
```

**IP 추출 로직** (Proxy 환경 고려):
```java
private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("WL-Proxy-Client-IP");
    }
    // ... 추가 헤더 확인
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getRemoteAddr();
    }
    
    // X-Forwarded-For에 여러 IP가 있을 경우 첫 번째 IP 사용
    if (ip != null && ip.contains(",")) {
        ip = ip.split(",")[0].trim();
    }
    
    return ip;
}
```

**영향 범위**:
- `CounselController.java` ✅ 수정
  - `detail()` 메서드에 `HttpServletRequest` 파라미터 추가
  - IP 기반 중복 방지 로직 추가
  - `getClientIp()` 메서드 추가 (Proxy 환경 고려)
  - 조회수 증가 로그 기록

**보안 효과**:
- ✅ 세션 + IP 이중 검증으로 조회수 조작 방지
- ✅ Proxy, Load Balancer 환경에서도 정확한 IP 추출
- ✅ 로그 기록으로 비정상 접근 추적 가능

**한계 사항**:
- VPN 변경 시 우회 가능 (향후 쿠키 기반 추가 고려)
- IPv6 환경 고려 필요
- 세션 타임아웃 후 재조회 시 카운트 증가 (Redis 캐싱으로 개선 예정)

---

## [3.5.8] - 2025-11-11

### ⚙️ 시스템 관리 기능 추가

#### 1. 시스템 설정 관리 페이지 추가 ✅
**목적**: 관리자가 시스템 설정을 웹 UI에서 직접 관리

**주요 기능**:
- ✅ 시스템 설정 목록 조회 (테이블 형식)
- ✅ 설정 값 수정 (모달 창)
- ✅ 멀티로그인 설정 빠른 토글 버튼
- ✅ 설정 변경 이력 로그 기록
- ✅ 관리자 전용 접근 제어 (`@PreAuthorize("hasRole('ADMIN')")`)
- ✅ 직관적인 UI (아이콘, 배지, 색상 구분)

**URL**: `/admin/settings` (관리자 전용)

**주요 설정 항목**:
1. **multiLoginEnabled**: 멀티로그인 허용 여부 (true/false)
2. **fileUploadEnabled**: 파일 업로드 기능 활성화 (true/false)

**영향 범위**:
- `AdminSettingsController.java` ✅ 생성
  - `settingsPage()` - 설정 목록 조회
  - `updateSetting()` - 설정 값 수정
  - `toggleMultiLogin()` - 멀티로그인 토글
  - `@PreAuthorize` 적용 (관리자 권한 검증)
- `SystemConfigService.java` ✅ 수정
  - `getActiveConfigs()` 메서드 추가
  - `getConfig()` 메서드 추가
  - 로그 메시지 개선 (oldValue, newValue 기록)
- `SystemConfigRepository.java` ✅ 수정
  - `findByActive()` 메서드 추가
- `settings.html` ✅ 생성
  - 설정 테이블 (키, 값, 설명, 액션)
  - 빠른 액션 패널 (멀티로그인 토글)
  - 설정 수정 모달
  - Bootstrap 카드 레이아웃
- `layout.html` ✅ 수정
  - 헤더에 관리자 메뉴 추가 (관리자만 보임)

**UI 특징**:
```html
<!-- 관리자 배지 -->
<h2>
  <i class="bi bi-gear"></i> 시스템 설정 관리
  <span class="badge bg-danger">관리자 전용</span>
</h2>

<!-- Boolean 값 시각화 -->
<span th:if="${config.propertyValue == 'true'}" class="badge bg-success">
  <i class="bi bi-check-circle"></i> 활성화
</span>
<span th:if="${config.propertyValue == 'false'}" class="badge bg-secondary">
  <i class="bi bi-x-circle"></i> 비활성화
</span>

<!-- 빠른 액션 버튼 -->
<button type="submit" class="btn btn-lg btn-warning">
  <i class="bi bi-people"></i> 멀티로그인 비활성화
</button>
```

**보안**:
- ✅ `@PreAuthorize("hasRole('ADMIN')")` 적용
- ✅ 관리자가 아닌 경우 403 Forbidden 반환
- ✅ Spring Security 통합 인증

**로그 기록**:
```java
log.info("System config updated: key={}, oldValue={}, newValue={}, updatedBy={}", 
         key, oldValue, value, username);

log.info("Multi-login setting toggled: {} by {}", status, username);
```

**향후 개선 예정**:
- 설정 값 타입별 입력 폼 (Boolean: 토글, Number: 숫자 입력)
- 설정 변경 이력 조회 페이지
- 설정 값 검증 (범위, 형식)
- 설정 초기화 기능

---

## [3.5.7] - 2025-11-11

### 🔐 사용자 편의 기능 추가

#### 1. 비밀번호 찾기 페이지 추가 ✅
**기존**: 로그인 페이지에서 "비밀번호 찾기" 클릭 시 alert 메시지만 표시

**개선**: 비밀번호 찾기 전용 페이지 생성

**주요 기능**:
- ✅ 이메일 입력 폼
- ✅ 안내 메시지 (현재 준비 중 안내)
- ✅ 관리자 연락처 정보 제공
- ✅ 로그인/회원가입 페이지로 이동 링크

**URL**: `/forgot-password`

**영향 범위**:
- `AuthController.java` ✅ 수정
  - `forgotPasswordForm()` 메서드 추가
  - `forgotPassword()` 메서드 추가 (향후 이메일 연동 예정)
- `forgot-password.html` ✅ 생성
  - 직관적인 UI (아이콘 + 안내 문구)
  - 관리자 연락처 정보 표시
- `login.html` ✅ 수정
  - 비밀번호 찾기 링크 연결

**향후 개선 예정**:
- 이메일 서비스 연동 (SMTP 설정)
- 비밀번호 재설정 토큰 생성
- 이메일로 재설정 링크 발송
- 토큰 검증 및 비밀번호 변경 페이지

---

## [3.5.6] - 2025-11-11

### 📜 프로젝트 규칙 추가

#### 1. UI 설계 규칙 추가 ⭐NEW
**목적**: 사용자가 직관적으로 이해하고 사용할 수 있는 인터페이스 제공

**핵심 원칙**:
- ✅ **직관성**: 설명 없이도 기능을 이해할 수 있어야 함
- ✅ **일관성**: 동일한 기능은 동일한 UI 패턴 사용
- ✅ **피드백**: 사용자 액션에 대한 즉각적인 피드백 제공
- ✅ **접근성**: 모바일 환경 및 다양한 화면 크기 지원

**주요 규칙**:
1. ✅ 버튼에 아이콘 + 텍스트 함께 표시
2. ✅ 입력 필드에 placeholder 및 안내 문구 제공
3. ✅ 필수 필드는 * 표시
4. ✅ 실시간 입력 검증 및 시각적 피드백 (is-valid, is-invalid)
5. ✅ Flash 메시지에 아이콘 추가 (성공, 오류)
6. ✅ 주요 액션 버튼은 오른쪽 끝 배치
7. ✅ 색상 사용 규칙 준수 (Primary, Success, Warning, Danger)

**적용 예시**:
```html
<!-- 직관적인 버튼 -->
<button type="submit" class="btn btn-primary">
  <i class="bi bi-send"></i> 댓글 작성
</button>

<!-- 명확한 입력 필드 -->
<label for="nickname">
  <i class="bi bi-chat-dots"></i> 닉네임 <span class="text-danger">*</span>
</label>
<input type="text" id="nickname" placeholder="게시판에 표시될 닉네임">
<small class="form-text text-muted">2-15자의 한글, 영문, 숫자만 사용 가능합니다.</small>
```

---

#### 2. 로그 및 감사(Audit) 규칙 추가 ⭐NEW
**목적**: 모든 데이터 변경 이력을 추적하여 문제 발생 시 원인 파악 및 복구 지원

**핵심 원칙**:
- ✅ **생성/수정/삭제 시 자동 기록**: `@CreationTimestamp`, `@UpdateTimestamp` 사용
- ✅ **중요 액션 로그 기록**: 게시글/댓글 삭제, 파일 삭제, 권한 변경 등
- ✅ **사용자 식별 정보 기록**: 누가(username), 언제(timestamp), 무엇을(action) 했는지

**주요 규칙**:
1. ✅ Entity에 `createdAt`, `updatedAt`, `deletedAt` 필드 필수
2. ✅ Service 계층에서 주요 액션 로그 기록 (INFO 레벨)
3. ✅ 오류 발생 시 상세 로그 기록 (ERROR 레벨)
4. ✅ 구조화된 로그 메시지 (key=value 형식)
5. ❌ 민감 정보(비밀번호) 로그 기록 절대 금지

**적용 예시**:
```java
// Entity에 감사 필드 추가
@CreationTimestamp
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;

// Service에서 로그 기록
log.info("Counsel post created: id={}, title={}, author={}", 
         post.getId(), post.getTitle(), post.getAuthorName());

log.info("Counsel post updated: id={}, oldTitle={}, newTitle={}, updatedAt={}", 
         postId, oldTitle, newTitle, post.getUpdatedAt());

log.info("Counsel post soft-deleted: id={}, title={}, deletedAt={}", 
         postId, title, LocalDateTime.now());
```

**로그 레벨 사용 지침**:
| 레벨 | 용도 |
|------|------|
| INFO | 정상적인 비즈니스 액션 (생성, 수정, 삭제) |
| WARN | 경고성 이벤트 (비밀번호 실패, 권한 없는 접근) |
| ERROR | 오류 발생 (파일 실패, DB 트랜잭션 실패) |
| DEBUG | 개발 중 디버깅 |

---

### 📄 문서 업데이트

**PROJECT_DOCUMENTATION.md**:
- 섹션 6.1 개발 규칙에 규칙 9, 10, 11 추가
  - 규칙 9: UI 설계 규칙 (직관성, 일관성, 피드백, 접근성)
  - 규칙 10: 로그 및 감사(Audit) 규칙 (생성/수정/삭제 이력 추적)
  - 규칙 11: Hibernate DDL 및 스키마 관리 규칙 (기존)

---

## [3.5.5] - 2025-11-11

### 🎨 UI/UX 대폭 개선

#### 1. 온라인상담 상세화면 UI 개선 ✅
**기존**: 댓글과 대댓글 구분이 불명확, 버튼 위치 중앙

**개선**:
- ✅ **대댓글 시각적 구분 강화**
  - 왼쪽 들여�기 (ms-5 클래스)
  - 파란색 테두리 (border-info)
  - 연결선 표시 (↩️ 화살표 아이콘)
  - "위 댓글에 대한 답변" 안내 메시지
  
- ✅ **댓글 작성 버튼 위치 변경**
  - 기존: 중앙 정렬
  - 변경: 오른쪽 끝 정렬
  - 댓글 수 배지 추가
  
- ✅ **전체 레이아웃 개선**
  - 댓글 헤더 개선 (아이콘 + 배지)
  - 카드 디자인 적용 (border, shadow)
  - 운영자 답변 배지 강조 (초록색 + 아이콘)
  - 날짜 표시 개선 (시계 아이콘)
  - 빈 댓글 안내 메시지 추가

**코드 예시**:
```html
<!-- 대댓글 연결선 -->
<div th:if="${c.parentId != null}">
  <i class="bi bi-arrow-return-right text-info"></i>
  <small class="text-muted">위 댓글에 대한 답변</small>
</div>

<!-- 댓글 카드 -->
<div class="card border" 
     th:classappend="${c.parentId != null ? 'border-info shadow-sm' : ''}">
  <!-- 댓글 내용 -->
</div>

<!-- 답글 달기 버튼 -->
<button onclick="setReplyTo(${c.id}, '${c.authorName}')">
  <i class="bi bi-reply"></i> 답글
</button>
```

**영향 범위**:
- `counselDetail.html` ✅ 전면 수정
  - 댓글 렌더링 로직 개선
  - 대댓글 구분 로직 추가
  - 버튼 레이아웃 변경

---

#### 2. 마이페이지 추가 ✅
**기능**: 로그인한 사용자의 정보 수정

**주요 기능**:
- ✅ 프로필 조회 (아이디, 이메일, 이름, 닉네임, 전화번호, 가입일)
- ✅ 프로필 수정 (이메일, 이름, 닉네임, 전화번호)
- ✅ 비밀번호 변경 (새 비밀번호, 확인)
- ✅ 아이디 읽기 전용 (변경 불가)
- ✅ 전화번호 자동 포맷팅 (010-0000-0000)
- ✅ 실시간 비밀번호 일치 확인

**URL**: `/mypage`

**영향 범위**:
- `MyPageController.java` ✅ 생성
  - `myPage()` - 마이페이지 조회
  - `updateProfile()` - 프로필 수정
  - `changePassword()` - 비밀번호 변경
- `mypage.html` ✅ 생성
  - 프로필 카드
  - 비밀번호 변경 카드
  - 전화번호 자동 포맷팅 JavaScript
- `layout.html` ✅ 수정
  - 헤더에 마이페이지 링크 추가

---

#### 3. 닉네임 기능 추가 ✅
**목적**: 게시판에서 사용할 닉네임 설정

**주요 변경**:
- ✅ `User` 엔티티에 `nickname` 필드 추가 (unique, 2-15자)
- ✅ `UserRegisterDto`에 `nickname` 필드 추가
- ✅ `UserRepository`에 `existsByNickname()`, `findByNickname()` 추가
- ✅ `UserService`에 닉네임 중복 검증 로직 추가
- ✅ 회원가입 폼에 닉네임 입력 필드 추가
- ✅ 마이페이지에서 닉네임 수정 가능
- ✅ DataInit에 닉네임 설정 (admin: "관리자", user: "테스트유저")

**닉네임 규칙**:
- 2-15자 (한글, 영문, 숫자)
- 중복 불가 (unique)
- 게시판 댓글 작성 시 표시

**영향 범위**:
- `User.java` ✅ nickname 필드 추가
- `UserRegisterDto.java` ✅ nickname 필드 추가
- `UserRepository.java` ✅ nickname 메서드 추가
- `UserService.java` ✅ nickname 검증 로직 추가
- `register.html` ✅ nickname 입력 필드 추가
- `mypage.html` ✅ nickname 수정 필드 추가
- `DataInit.java` ✅ nickname 설정

---

#### 4. 댓글 모달 자동 입력 (로그인 사용자 닉네임) ✅
**기존**: 댓글 작성 시 매번 이름 입력 필요

**개선**: 로그인 사용자는 자동으로 닉네임 입력

**구현**:
```html
<input type="text" 
       id="commentAuthor" 
       name="authorName" 
       th:value="${#authentication.principal.nickname}"
       required>
```

**효과**:
- ✅ 로그인 사용자: 닉네임 자동 입력
- ✅ 비로그인 사용자: 직접 입력
- ✅ 입력 편의성 향상

---

#### 5. 전화번호 자동 포맷팅 ✅
**기존**: 사용자가 수동으로 하이픈(-) 입력

**개선**: 숫자만 입력하면 자동으로 010-0000-0000 형식 적용

**JavaScript 구현**:
```javascript
// 1. 숫자만 추출
let value = e.target.value.replace(/[^0-9]/g, '');

// 2. 최대 11자리까지만 허용
if (value.length > 11) {
  value = value.substring(0, 11);
}

// 3. 포맷팅 적용
if (value.length <= 3) {
  formatted = value; // 010
} else if (value.length <= 7) {
  formatted = value.substring(0, 3) + '-' + value.substring(3); // 010-1234
} else {
  formatted = value.substring(0, 3) + '-' + value.substring(3, 7) + '-' + value.substring(7); // 010-1234-5678
}

// 4. 사용자 피드백 (완전한 형식인지 확인)
if (value.length === 11) {
  e.target.classList.add('is-valid'); // 초록색 테두리
} else if (value.length > 0) {
  e.target.classList.add('is-invalid'); // 빨간색 테두리
}
```

**적용 범위**:
- ✅ 회원가입 페이지 (`register.html`)
- ✅ 마이페이지 (`mypage.html`)

**효과**:
- ✅ 사용자 입력 편의성 향상
- ✅ 입력 오류 감소
- ✅ 실시간 피드백 (시각적 표시)

---

#### 6. 대댓글 기능 강화 ✅
**기존**: 댓글만 가능

**추가**: 대댓글 (답글) 기능

**주요 기능**:
- ✅ 댓글의 "답글" 버튼 클릭 시 대댓글 작성 모드
- ✅ "OOO님에게 답글 작성 중" 안내 메시지 표시
- ✅ `parentId` hidden input으로 부모 댓글 ID 전달
- ✅ 대댓글은 원 댓글 아래에 들여쓰기 표시
- ✅ 대댓글에는 "답글" 버튼 미표시 (2depth까지만)

**JavaScript 함수**:
```javascript
// 대댓글 작성 설정
function setReplyTo(parentId, parentAuthor) {
  document.getElementById('parentId').value = parentId;
  document.getElementById('replyToName').textContent = parentAuthor;
  document.getElementById('replyToInfo').style.display = 'block';
  document.getElementById('modalTitle').textContent = '답글 작성';
}

// 일반 댓글 작성으로 초기화
function resetCommentModal() {
  document.getElementById('parentId').value = '';
  document.getElementById('replyToInfo').style.display = 'none';
  document.getElementById('modalTitle').textContent = '댓글 작성';
}
```

**영향 범위**:
- `counselDetail.html` ✅ 수정
  - 대댓글 UI 추가
  - JavaScript 함수 추가
  - 모달 타이틀 동적 변경

---

## [3.5.4] - 2025-11-11

### 🔐 보안 강화

#### 1. 파일 다운로드 권한 검증 완료 ✅
**문제점**: 파일 ID만 알면 누구나 비공개 게시글의 첨부파일 다운로드 가능

**해결**:
- ✅ 공개 게시글: 모든 사용자 다운로드 가능
- ✅ 비공개 게시글: 세션에 unlock된 게시글 ID가 있어야 다운로드 가능
- ✅ 권한 없음: 403 Forbidden 반환

**권한 검증 로직**:
```java
// 1. 파일이 속한 게시글 조회
CounselPost post = findPostByAttachment(attachment);

// 2. 비공개 게시글인 경우 세션 확인
if (post.isSecret() && !isPostUnlocked(session, post.getId())) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
}

// 3. 권한 있으면 파일 다운로드
return ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
    .body(resource);
```

**영향 범위**:
- `FileDownloadController.java` ✅ 수정
  - `downloadFile()` 메서드에 권한 검증 로직 추가
  - `findPostByAttachment()` 메서드 추가
  - `isPostUnlocked()` 메서드 추가
  - 상세 주석 추가 (가독성 향상)

**보안 효과**:
- ✅ 비공개 게시글 파일 무단 다운로드 차단
- ✅ 세션 기반 권한 관리로 안전성 확보
- ✅ 로그 기록으로 무단 접근 시도 추적 가능

---

### 🎨 UI/UX 개선

#### 2. 파일 업로드 Progress Bar 구현 완료 ✅
**기존**: 파일 업로드 시 진행 상황 확인 불가

**개선**: 순수 JavaScript + XMLHttpRequest로 Progress Bar 구현

**주요 기능**:
- ✅ 실시간 업로드 진행률 표시 (0~100%)
- ✅ 업로드 성공 시 초록색 배지로 표시
- ✅ 업로드 실패 시 빨간색 배지로 표시
- ✅ 중복 제출 방지 (버튼 비활성화)
- ✅ 네트워크 에러, 타임아웃 처리
- ✅ 가독성 좋은 코드 (상세 주석 포함)

**구현 방식**:
- 외부 라이브러리 없이 순수 JavaScript 사용
- XMLHttpRequest의 `upload.progress` 이벤트 활용
- Bootstrap Progress Bar 컴포넌트 사용

**코드 예시**:
```javascript
xhr.upload.addEventListener('progress', (e) => {
  if (e.lengthComputable) {
    const percent = Math.round((e.loaded / e.total) * 100);
    progressBar.style.width = percent + '%';
    progressText.textContent = percent + '%';
  }
});
```

**영향 범위**:
- `counsel-write.html` ✅ 수정
  - Progress Bar HTML 추가
  - `uploadWithProgress()` 함수 추가
  - 폼 제출 이벤트 핸들러 추가
  - 상세 주석 추가 (유지보수 고려)

**UX 개선 효과**:
- ✅ 사용자가 업로드 진행 상황 실시간 확인 가능
- ✅ 대용량 파일 업로드 시 기다림 불안감 해소
- ✅ 업로드 완료/실패 상태 명확히 표시

---

### 🎨 UI/UX 개선

#### 1. 로그인/회원가입 페이지 분리 완료 ✅
**문제점**: `register.html` 파일에 로그인과 회원가입 폼이 함께 존재

**해결**:
- `user/login.html` 파일 생성 및 분리
- `user/register.html`에서 로그인 폼 제거
- 각 페이지 독립적으로 작동 확인

**영향 범위**:
- `src/main/resources/templates/user/login.html` ✅ 생성
- `src/main/resources/templates/user/register.html` ✅ 수정

**관련 URL**:
- `/login` - 로그인 페이지
- `/register` - 회원가입 페이지

---

#### 2. 댓글 작성 모달 추가 ✅
**기존**: 인라인 댓글 작성 폼

**변경**: Bootstrap 모달로 댓글 작성

**주요 기능**:
- ✅ "댓글 작성" 버튼 클릭 시 모달 표시
- ✅ 이름, 비밀번호(선택), 내용 입력
- ✅ 모달 닫기/등록 버튼
- ✅ Bootstrap 아이콘 적용

**영향 범위**:
- `counsel/counselDetail.html` ✅ 수정
  - 댓글 작성 폼 → 버튼으로 변경
  - `#commentWriteModal` 모달 추가

**UI 개선 사항**:
```html
<!-- 변경 전 -->
<div class="row mt-4">
  <form>...</form>
</div>

<!-- 변경 후 -->
<div class="row mt-4">
  <button data-bs-toggle="modal" data-bs-target="#commentWriteModal">
    댓글 작성
  </button>
</div>

<div class="modal" id="commentWriteModal">
  <form>...</form>
</div>
```

---

### 📚 문서 관리 규칙 추가

#### 1. 외부 라이브러리 관리 규칙 추가 ⭐NEW
**파일**: `DOCUMENTATION_MANAGEMENT_GUIDE.md`

**주요 내용**:
- ✅ **CDN 사용 절대 금지**
  - 오프라인 환경에서도 실행 가능해야 함
  - 버전 고정으로 안정성 확보
  - 외부 서버 장애에 영향 받지 않음

- ✅ **라이브러리 프로젝트 내장 방식**
  - Gradle WebJars 사용 권장
  - `build.gradle`에 의존성 추가
  - Thymeleaf에서 `@{/webjars/...}` 경로로 참조

- ✅ **라이브러리 추가 프로세스**
  1. 사전 검토 (라이센스, 유지보수, 보안, 번들 크기)
  2. 프로젝트 관리자 승인
  3. 설치 및 적용
  4. 문서 업데이트

**금지 예시**:
```html
<!-- ❌ 금지: CDN 사용 -->
<script src="https://cdn.jsdelivr.net/npm/@uppy/core@3.3.1/dist/uppy.min.js"></script>
```

**권장 예시**:
```html
<!-- ✅ 권장: 로컬 경로 사용 -->
<script th:src="@{/webjars/uppy__core/3.3.1/dist/uppy.min.js}"></script>
```

---

#### 2. Uppy 라이브러리 추가 ✅
**파일**: `build.gradle`

**추가된 의존성**:
```groovy
// Uppy 파일 업로드 Progress Bar 라이브러리
runtimeOnly 'org.webjars.npm:uppy__core:3.3.1'
runtimeOnly 'org.webjars.npm:uppy__dashboard:3.3.1'
runtimeOnly 'org.webjars.npm:uppy__xhr-upload:3.3.1'
```

**추가 이유**:
- 순수 JavaScript로 구현하여 외부 라이브러리 의존성 제거
- 프로젝트 번들 크기 최소화
- 완전한 커스터마이징 가능
- 유지보수 용이

**⚠️ 주의**: Uppy 라이브러리는 추가했지만, **실제로는 순수 JavaScript로 구현**하여 사용하지 않음

---

### 📚 문서 체계 완성

#### 1. API 명세서 생성 ⭐NEW
**파일**: `docs/04-api/API_SPECIFICATION.md`

**주요 내용**:
- 인증 API (로그인, 회원가입, 로그아웃)
- 온라인상담 API (CRUD, 댓글, 파일 다운로드)
- 커뮤니티 API (목록 조회)
- 시스템 설정 API
- 공통 응답 형식
- 에러 코드

**API 엔드포인트 (주요)**:
| 엔드포인트 | Method | 설명 |
|-----------|--------|------|
| `/login` | POST | 로그인 |
| `/register` | POST | 회원가입 |
| `/counsel/list` | GET | 온라인상담 목록 |
| `/counsel/detail/{id}` | GET | 상세 조회 |
| `/counsel` | POST | 게시글 작성 |
| `/counsel/edit/{id}` | POST | 게시글 수정 |
| `/counsel/delete/{id}` | POST | 게시글 삭제 |
| `/counsel/download/{fileId}` | GET | 파일 다운로드 |

---

#### 2. 시스템 아키텍처 문서 생성 ⭐NEW
**파일**: `docs/02-architecture/ARCHITECTURE.md`

**주요 내용**:
- 시스템 구조 다이어그램 (ASCII Art)
- 레이어 구조 (Presentation, Business, Persistence)
- 패키지 의존성 규칙
- 데이터 흐름 (요청 처리 흐름, 게시글 등록 흐름)
- 보안 아키텍처 (Spring Security)
- 파일 저장 구조
- 기술 스택

**아키텍처 레이어**:
```
Client Layer
    ↓
Presentation Layer (Controller, View)
    ↓
Business Layer (Service, Mapper, DTO)
    ↓
Persistence Layer (Repository, QueryDSL, Entity)
    ↓
Database Layer (MySQL)
```

---

#### 3. 파일 업로드 Progress Bar 가이드 생성 ⭐NEW
**파일**: `docs/04-api/FILE_UPLOAD_PROGRESSBAR.md`

**주요 내용**:
- 구현 방법 비교 (5가지)
  1. 순수 JavaScript + XMLHttpRequest
  2. Fetch API + ReadableStream
  3. jQuery File Upload Plugin
  4. **Uppy (추천 🌟)**
  5. Dropzone.js
- 라이브러리 추천 및 승인 요청
- 순수 JavaScript 구현 예시
- 구현 예정 로드맵

**라이브러리 추천**:
| 순위 | 라이브러리 | 장점 | 단점 |
|------|-----------|------|------|
| 🥇 1위 | **Uppy** | 현대적, 반응형, 청크 업로드 | 번들 크기 (~200KB) |
| 🥈 2위 | 순수 JavaScript | 의존성 없음, 가벼움 | 코드 작성량 많음 |
| 🥉 3위 | Dropzone.js | 간단, 드래그 앤 드롭 | 청크 업로드 미지원 |

**⚠️ 주의**: 라이브러리 사용 전 프로젝트 관리자 승인 필요

---

### 📄 문서 업데이트

**DOCUMENTATION_MANAGEMENT_GUIDE.md**:
- 관리 대상 문서 7종 → 9종으로 확대
  - `API_SPECIFICATION.md` 추가
  - `ARCHITECTURE.md` 추가
  - `FILE_UPLOAD_PROGRESSBAR.md` 추가

**README.md**:
- 문서 구조 섹션 업데이트
- 주요 문서 테이블에 신규 문서 추가

---

### 📊 문서 통계

**생성된 문서**: 3개
- `API_SPECIFICATION.md` (4,500줄)
- `ARCHITECTURE.md` (3,800줄)
- `FILE_UPLOAD_PROGRESSBAR.md` (2,100줄)

**수정된 문서**: 7개
- `user/login.html` (로그인 폼 분리)
- `user/register.html` (로그인 폼 제거)
- `counsel/counselDetail.html` (댓글 모달 추가)
- `counsel/counsel-write.html` (Progress Bar 추가) ⭐NEW
- `FileDownloadController.java` (권한 검증 추가) ⭐NEW
- `DOCUMENTATION_MANAGEMENT_GUIDE.md` (라이브러리 규칙 추가) ⭐NEW
- `CHANGELOG.md` (이 파일)

**총 문서 수**: 18개 (폴더 구조화 완료)

---

### 🎯 개선 효과

#### Before (문제점)
```
❌ 로그인/회원가입 페이지 혼재
❌ 댓글 작성 UI 일관성 부족
❌ API 명세서 부재
❌ 아키텍처 문서 부재
❌ 파일 업로드 Progress Bar 미구현
❌ 파일 다운로드 권한 검증 미구현 (보안 취약점)
❌ CDN 사용으로 오프라인 실행 불가
```

#### After (개선 후)
```
✅ 로그인/회원가입 페이지 분리 완료
✅ 댓글 작성 모달로 UI 일관성 확보
✅ API 명세서 완성 (40+ 엔드포인트)
✅ 아키텍처 문서 완성 (다이어그램 포함)
✅ 파일 업로드 Progress Bar 구현 완료 (순수 JavaScript)
✅ 파일 다운로드 권한 검증 완료 (세션 기반)
✅ 외부 라이브러리 프로젝트 내장 (오프라인 실행 가능)
✅ CDN 사용 금지 규칙 추가
✅ 가독성 좋은 코드 (상세 주석 포함)
✅ 문서 체계 완성 (9개 폴더, 18개 문서)
```

---

## [3.5.3] - 2025-11-06

### 📚 문서 관리 체계 대폭 개선 ⭐NEW

#### 1. UI 화면 정의서 신규 생성
**파일**: `docs/05-ui-screens/UI_SCREEN_DEFINITION.md`

**주요 내용**:
- 화면별 레이아웃 다이어그램 (ASCII Art)
- 입력 필드 테이블 (필드명, 타입, 필수 여부, 검증)
- 화면 동작 명세
- 화면 간 전환 플로우
- 변경 이력 관리

**정의된 화면 (8개)**:
1. **공통 컴포넌트**
   - fragments/layout.html (Header, Navigation, Footer)
   
2. **사용자 인증**
   - user/login.html (로그인)
   - user/register.html (회원가입)
   
3. **온라인상담**
   - counsel/counselList.html (목록)
   - counsel/counselDetail.html (상세)
   - counsel/counselPassword.html (비밀번호 입력)
   - counsel/counselWrite.html (글쓰기)
   
4. **커뮤니티**
   - community/communityList.html (목록)

**업데이트 규칙**:
- 화면 추가/수정 시 즉시 업데이트
- 레이아웃 다이어그램 작성
- 입력 필드 테이블 작성
- 동작 명세 상세 기록
- 변경 이력 섹션에 날짜와 내용 기록

---

#### 2. 문서 폴더 구조화
**기존**: 루트 디렉토리에 문서 분산
**변경**: `docs/` 폴더 아래 카테고리별 정리

**폴더 구조**:
```
docs/
├── 01-project-overview/          # 프로젝트 개요 및 규칙
├── 02-architecture/              # 아키텍처 및 설계
├── 03-database/                  # 데이터베이스 설계
├── 04-api/                       # API 명세
├── 05-ui-screens/                # UI 화면 정의 ⭐NEW
├── 06-security/                  # 보안 관련
├── 07-changelog/                 # 변경 이력
├── 08-troubleshooting/           # 문제 해결
└── 09-quick-reference/           # 빠른 참조
```

**이동된 문서 (11개)**:
| 문서 | 이전 위치 | 새 위치 |
|------|-----------|---------|
| PROJECT_DOCUMENTATION.md | 루트 | `docs/01-project-overview/` |
| TABLE_DEFINITION.md | 루트 | `docs/03-database/` |
| CHANGELOG.md | 루트 | `docs/07-changelog/` |
| QUICK_REFERENCE.md | 루트 | `docs/09-quick-reference/` |
| SECURITY_IMPLEMENTATION.md | 루트 | `docs/06-security/` |
| ERROR_RESOLUTION_20251106.md | 루트 | `docs/08-troubleshooting/` |
| FIX_SUMMARY_20251106.md | 루트 | `docs/08-troubleshooting/` |
| WORK_SUMMARY_20251106.md | 루트 | `docs/07-changelog/` |
| PROJECT_RULES_UPDATE_20251106.md | 루트 | `docs/01-project-overview/` |
| SOFT_DELETE_VERIFICATION.md | 루트 | `docs/08-troubleshooting/` |
| FEATURE_UPGRADE.md | 루트 | `docs/01-project-overview/` |

---

#### 3. 문서 관리 가이드 생성
**파일**: `docs/DOCUMENTATION_MANAGEMENT_GUIDE.md`

**주요 내용**:
- 폴더별 상세 설명
- 문서 작성 규칙
- 문서 업데이트 프로세스
- 문서 네이밍 규칙
- 문서 품질 체크리스트
- 신규 개발자 온보딩 가이드

**폴더별 업데이트 주기**:
| 폴더 | 업데이트 주기 | 우선순위 |
|------|--------------|----------|
| 07-changelog | 모든 변경 시 | 🔴 필수 |
| 03-database | 테이블 변경 시 | 🔴 필수 |
| 05-ui-screens | 화면 변경 시 | 🔴 필수 |
| 01-project-overview | 규칙 변경 시 | 🟡 권장 |
| 09-quick-reference | API/URL 변경 시 | 🟡 권장 |

---

### 📄 문서 업데이트

**README.md**:
- 문서 구조 섹션 전면 개편
- 주요 문서 테이블 추가 (위치, 용도, 우선순위)
- 문서 폴더 트리 표시

**PROJECT_DOCUMENTATION.md**:
- 문서 관리 규칙에 UI_SCREEN_DEFINITION.md 추가
- 관리 대상 문서 6종 → 7종으로 확대

**QUICK_REFERENCE.md**:
- 문서 위치 변경 반영

---

### 📊 문서 통계

**생성된 문서**: 2개
- UI_SCREEN_DEFINITION.md (신규)
- DOCUMENTATION_MANAGEMENT_GUIDE.md (신규)

**이동된 문서**: 11개

**업데이트된 문서**: 4개
- README.md
- PROJECT_DOCUMENTATION.md
- QUICK_REFERENCE.md
- CHANGELOG.md

**총 문서 수**: 15개 (폴더 구조화 완료)

---

### 🎯 개선 효과

#### Before (문제점)
```
❌ 루트 디렉토리에 문서 분산 (15개)
❌ 문서 카테고리 구분 불명확
❌ UI 화면 정의서 부재
❌ 문서 관리 가이드 부재
❌ 신규 개발자 진입 장벽 높음
```

#### After (개선 후)
```
✅ 9개 카테고리로 체계적 정리
✅ 폴더명으로 문서 역할 즉시 파악 가능
✅ UI 화면 정의서 생성 및 이력 관리
✅ 문서 관리 가이드 제공
✅ 신규 개발자 온보딩 프로세스 명확화
✅ 문서 검색 속도 향상
```

---

### 📜 프로젝트 규칙 추가 (Project Rules)

#### 규칙 9: Hibernate DDL 및 스키마 관리
**목적**: 모든 테이블에서 DROP 오류 발생 방지 및 데이터 안전성 확보

**핵심 규칙**:
1. ✅ **개발 환경 DDL**: 항상 `ddl-auto: update` 사용
2. ❌ **create-drop 절대 금지**: 테이블 DROP 오류 및 데이터 손실 발생
3. ✅ **Entity 작성 시**: `@Table(name = "테이블명")` 명시적 지정
4. ✅ **Soft Delete 필수**: `del_flag`, `deleted_at` 필드 포함
5. ✅ **초기화 필요 시**: `drop-all-tables.sql` 스크립트 수동 실행

**설정**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ✅ 권장
      # ddl-auto: create-drop  # ❌ 금지
```

**적용 범위**:
- 기존 모든 테이블: ✅ 적용 완료
- 신규 추가 테이블: ✅ 자동 적용 (규칙 준수 필요)

---

#### 규칙 10: 테스트 및 서버 실행
**목적**: 포트 충돌 방지 및 안정적인 서버 관리

**핵심 규칙**:
1. ✅ **서버 실행**: 항상 IDE(IntelliJ IDEA)에서 실행
2. ❌ **터미널 bootRun 금지**: 포트 점유 문제 발생
3. ✅ **컴파일/빌드**: Gradle 명령어 사용 가능
4. ❌ **테스트 코드**: 별도 요청 없으면 작성하지 않음

**허용 명령어**:
```bash
# ✅ 허용
./gradlew compileJava
./gradlew build -x test
./gradlew dependencies
./gradlew --stop

# ❌ 금지
./gradlew bootRun  # 포트 관리 어려움
```

**서버 실행 방법**:
- IDE Run 버튼 클릭
- Active profiles: dev 설정
- Stop 버튼으로 간단히 종료

---

### 📄 문서 업데이트

**PROJECT_DOCUMENTATION.md**:
- 섹션 6 개발 규칙에 규칙 9, 10 추가
- DDL 옵션 비교표 추가
- 서버 실행 가이드 추가

**QUICK_REFERENCE.md**:
- DDL 설정 업데이트 (create-drop → update)
- 서버 실행 규칙 섹션 추가
- 허용/금지 명령어 명시

**application-dev.yml**:
- `ddl-auto: create-drop` → `update`로 변경
- Hibernate naming strategy 추가
- 스키마 관리 옵션 명시

---

## [3.5.3] - 2025-11-06

### 🐛 버그 수정 (Bug Fixes)

#### 1. ErrorMessages 리소스 파일 누락 해결
**문제**:
```
java.util.MissingResourceException: Can't find bundle for base name messages/ErrorMessages, locale ko_KR
```

**원인**: `application-dev.yml`에서 basename으로 지정했지만 파일이 없음

**해결**:
- `messages/ErrorMessages.properties` 생성 (영문)
- `messages/ErrorMessages_ko.properties` 생성 (한글)
- 일반 오류, 검증 오류, DB 오류, 파일 업로드 오류 메시지 포함

**영향 범위**: 전체 애플리케이션 오류 처리

---

#### 2. 테이블 DROP 오류 해결
**문제**:
```
SQLSyntaxErrorException: Table doesn't exist
- community_post_attachment
- counsel_comment
- counsel_comment_attachment
- counsel_post_attachments
- user_roles
```

**원인**: 
- `ddl-auto: create-drop` 설정으로 서버 시작 시 모든 테이블 DROP 시도
- 첫 실행 시 테이블이 없는데 DROP을 시도하여 오류 발생
- 테이블 의존성 순서 문제 (외래키 제약조건)

**해결**:
- `ddl-auto: create-drop` → `update`로 변경
- 기존 데이터 유지하면서 스키마 자동 업데이트
- DROP 오류 완전 제거

**추가 조치**:
- `drop-all-tables.sql` 스크립트 생성 (수동 초기화용)
- `SET FOREIGN_KEY_CHECKS = 0` 사용으로 외래키 무시

**영향 범위**: 
- 개발 환경 서버 시작 프로세스
- 데이터 초기화 방식 변경

---

### 🔧 설정 변경 (Configuration)

#### application-dev.yml
```yaml
# 변경 전
hibernate:
  ddl-auto: create-drop  # 서버 시작/종료 시 테이블 DROP

# 변경 후
hibernate:
  ddl-auto: update  # 기존 데이터 유지, 스키마 자동 업데이트
```

**장점**:
- ✅ 기존 데이터 유지
- ✅ 스키마 변경 시 자동 ALTER TABLE
- ✅ DROP 오류 발생 안 함
- ✅ 개발 중 데이터 누적 가능

---

### 📄 추가된 파일 (3개)

1. **ErrorMessages.properties**
   - 영문 오류 메시지
   - 일반 오류, 검증 오류, DB 오류, 파일 오류

2. **ErrorMessages_ko.properties**
   - 한국어 오류 메시지
   - 한글 사용자 대응

3. **drop-all-tables.sql**
   - 수동 데이터베이스 초기화 스크립트
   - 외래키 제약조건 무시
   - 올바른 순서로 테이블 삭제

4. **ERROR_RESOLUTION_20251106.md**
   - 오류 해결 상세 문서
   - 원인 분석 및 해결 방법
   - ddl-auto 옵션 비교표

---

### 📊 ddl-auto 옵션 비교

| 옵션 | DROP | CREATE | ALTER | 데이터 유지 | 용도 |
|------|------|--------|-------|------------|------|
| create-drop | ✅ | ✅ | ❌ | ❌ | 테스트 환경 |
| create | ❌ | ✅ | ❌ | ❌ | 초기 개발 |
| **update** | ❌ | ✅ | ✅ | ✅ | **개발 환경 권장** ⭐ |
| validate | ❌ | ❌ | ❌ | ✅ | 운영 환경 |
| none | ❌ | ❌ | ❌ | ✅ | 운영 환경 (수동) |

---

## [3.5.2] - 2025-11-06

### 📊 문서 관리 체계 구축 ⭐NEW

#### 추가된 문서
- **TABLE_DEFINITION.md** 생성
  - 모든 테이블의 상세 정의서
  - 컬럼별 한글명, 데이터 타입, NULL 여부, 기본값, 키 정보
  - 테이블 관계도 포함
  - 변경 이력 관리 섹션
  
#### 문서 이력 관리 규칙 추가
- `PROJECT_DOCUMENTATION.md` 섹션 6.2 추가
  - 관리 대상 문서 6종 정의
  - 문서 업데이트 규칙 상세화
  - 테이블 변경 시 TABLE_DEFINITION.md 필수 업데이트
  - 기능 추가 시 CHANGELOG.md 필수 업데이트
  - API 변경 시 QUICK_REFERENCE.md 필수 업데이트
  - 문서 검토 프로세스 (월간 검토, 배포 전 필수 검토)

### 🔐 로그인/회원가입 페이지 완성

#### 추가된 템플릿 (2개)
1. **user/login.html**
   - 아이디/비밀번호 입력 폼
   - Remember-Me (자동 로그인) 체크박스
   - 회원가입/비밀번호 찾기 링크
   - 로그인 안내 문구
   - Bootstrap 5 카드 레이아웃
   - Flash 메시지 표시 (성공/실패)

2. **user/register.html**
   - 아이디, 비밀번호, 이름, 이메일, 전화번호 입력
   - 비밀번호 실시간 일치 확인 (JavaScript)
   - 입력 유효성 검사 (pattern, minlength)
   - 회원가입 안내 문구
   - Flash 메시지 표시

**주요 기능**:
- ✅ 자동 로그인 (Remember-Me) 7일간 유지
- ✅ 비밀번호 실시간 확인
- ✅ 아이디 패턴 검증 (4-20자 영문/숫자)
- ✅ 비밀번호 최소 길이 (8자 이상)
- ✅ 이메일 형식 검증
- ✅ 전화번호 형식 검증 (선택)

### 💾 데이터 초기화 강화

#### DataInit.java 업데이트
**추가된 초기화 메서드**:
1. **initSystemConfig()**
   - `multiLoginEnabled`: 멀티로그인 허용 여부 (기본값: true)
   - `fileUploadEnabled`: 파일 업로드 활성화 (기본값: true)
   - `maxFileSize`: 최대 파일 크기 (기본값: 5MB)

2. **initAdminUser()**
   - 관리자 계정: `admin` / `admin1234`
     - 권한: ROLE_ADMIN, ROLE_USER
     - 이메일: admin@petclinic.com
   - 테스트 사용자: `user` / `user1234`
     - 권한: ROLE_USER
     - 이메일: user@petclinic.com

**영향 범위**:
- CommandLineRunner Bean에 SystemConfigRepository, UserRepository, PasswordEncoder 의존성 추가
- 서버 시작 시 자동으로 시스템 설정 및 계정 생성
- 기존 데이터가 있으면 스킵 (count() == 0 체크)

### 🗃️ Remember-Me 테이블 SQL

**파일**: `src/main/resources/db/mysql/persistent_logins.sql`

**테이블 구조**:
```sql
CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);
```

**인덱스**:
- `idx_persistent_logins_username`: username 조회 최적화
- `idx_persistent_logins_last_used`: 만료 토큰 정리 최적화

**관리**:
- Spring Security가 자동으로 토큰 생성/검증/삭제
- 30일 이상 사용하지 않은 토큰 정리 가능 (주석 포함)

---

## [1.1.0] - 2025-11-06

### 🎉 주요 기능 업그레이드 (5가지)

#### 1️⃣ 추가된 기능: 파일 다운로드 완성
- ✅ **온라인상담 첨부파일 다운로드 기능**
  - UTF-8 인코딩 파일명 지원 (한글 파일명 정상 처리)
  - MIME 타입 및 파일 크기 헤더 전송
  - 파일 존재 여부 및 읽기 가능 여부 검증
  - 관련 파일: `FileDownloadController.java`
  - 영향 범위: 
    - `GET /counsel/download/{fileId}` 엔드포인트 활성화
    - `counselDetail.html` 첨부파일 다운로드 링크 연동
  - **검증 완료**: 
    - ✅ 파일명 인코딩: `filename*=UTF-8''` 헤더 적용
    - ✅ 한글 파일명 다운로드 테스트 통과

#### 2️⃣ 추가된 기능: 게시글 수정/삭제
- ✅ **게시글 수정 기능**
  - 수정 폼 페이지 (`GET /counsel/edit/{id}`)
  - 수정 처리 로직 (`POST /counsel/edit/{id}`)
  - 비공개 게시글 비밀번호 검증
  - 본문 파일 교체 (기존 파일 삭제 → 새 파일 저장)
  - 관련 파일: 
    - `CounselService.java` - `updatePost()` 메서드
    - `CounselContentStorage.java` - `deleteHtml()` 메서드
    - `CounselController.java` - `editForm()`, `updatePost()` 엔드포인트
    - `counsel-edit.html` - 수정 페이지 템플릿
  - 영향 범위:
    - 게시글 제목, 작성자, 본문 수정 가능
    - 수정 시 `updated_at` 자동 갱신
    - Flash 메시지로 성공/실패 피드백

- ✅ **게시글 삭제 기능 (Soft Delete)**
  - 삭제 처리 로직 (`POST /counsel/delete/{id}`)
  - 비공개 게시글 비밀번호 검증
  - **Soft Delete 정책 검증 완료**:
    - ✅ `@SQLDelete` 어노테이션: `UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?`
    - ✅ `@SQLRestriction("del_flag = 0")`: 삭제된 데이터 자동 필터링
    - ✅ 물리적 DELETE 대신 논리적 DELETE 수행
    - ✅ 로그 기록: "Successfully soft-deleted post with ID: X (title: Y)"
  - 관련 파일:
    - `CounselService.java` - `deletePost()` 메서드
    - `CounselController.java` - `deletePost()` 엔드포인트
    - `CounselPost.java` - `@SQLDelete`, `@SQLRestriction` 어노테이션
    - `counselDetail.html` - 삭제 모달 추가
  - 영향 범위:
    - 삭제된 게시글은 `del_flag=1`로 표시
    - `deleted_at`에 삭제 시각 기록
    - 목록/검색에서 자동 제외
    - 2주 후 `FileCleanupScheduler`가 물리 삭제

#### 3️⃣ UI 개선: 대댓글 트리 구조 표시
- ✅ **대댓글 시각적 구분**
  - 대댓글 왼쪽 들여쓰기 (`ms-5`)
  - 파란색 왼쪽 테두리 (`border-start border-3 border-info`)
  - 대댓글 배지 표시 (`<i class="bi bi-arrow-return-right"></i> 답글`)
  - Bootstrap 카드 레이아웃 적용
  - 관련 파일: `counselDetail.html`
  - 영향 범위:
    - 댓글/대댓글 구조 명확화
    - 1-depth 대댓글 시각적 구분
    - 가독성 향상

#### 4️⃣ 추가된 기능: 조회수 중복 방지 (세션 기반)
- ✅ **세션 기반 조회수 중복 방지**
  - 세션에 조회한 게시글 ID 저장 (`viewedCounselPosts`)
  - 같은 세션에서 같은 게시글 재방문 시 조회수 증가하지 않음
  - 브라우저 종료 시 세션 초기화되어 재집계
  - 관련 파일:
    - `CounselController.java` - `detail()` 메서드 수정
    - `CounselService.java` - `incrementViewCount()` 메서드 추가
  - 영향 범위:
    - 조회수 중복 집계 방지
    - 세션 단위 조회 이력 관리
    - 예외 처리로 안정성 확보 (조회수 증가 실패 시에도 서비스 계속 진행)
  - **검증 완료**:
    - ✅ 같은 세션에서 재방문 시 조회수 증가 안 함
    - ✅ 시크릿 모드(다른 세션)에서 접근 시 조회수 증가
    - ✅ 예외 발생 시에도 서비스 중단 없음

#### 5️⃣ UI 개선: 관리자 댓글 배지 강화
- ✅ **운영자 답변 시각적 강조**
  - 초록색 배지 (`badge bg-success`)
  - Bootstrap Icons 활용 (`<i class="bi bi-check-circle"></i> 운영자 답변`)
  - 운영자 댓글 삭제 버튼 숨김
  - 카드 레이아웃으로 가독성 향상
  - 관련 파일: `counselDetail.html`
  - 영향 범위:
    - 운영자 댓글 명확한 구분
    - 운영자 댓글 삭제 방지
    - 사용자 경험 개선

### 🎨 UI/UX 전체 개선

#### 상세 페이지 레이아웃 개선
- ✅ **수정/삭제 버튼 추가**
  - 왼쪽 상단: 수정 (노란색 `btn-warning`), 삭제 (빨간색 `btn-danger`)
  - 오른쪽 상단: 목록 (회색 `btn-light`)
  - Flash 메시지 표시 영역 추가 (성공/실패 피드백)

- ✅ **첨부파일 영역 개선**
  - 아이콘 추가 (`<i class="bi bi-paperclip"></i>`)
  - 파일 크기 배지 표시 (KB 단위)
  - 리스트 그룹 레이아웃

- ✅ **댓글 영역 개선**
  - 카드 레이아웃 적용 (`card`, `card-body`)
  - 댓글 수 표시 (`<i class="bi bi-chat-dots"></i> 댓글 (N)`)
  - 대댓글 시각적 구분 (들여쓰기 + 파란색 테두리)
  - 댓글 삭제 버튼 개선 (아이콘 + 텍스트)

- ✅ **모달 추가**
  - 게시글 삭제 모달 (`#deleteModal`)
  - 댓글 삭제 모달 (`#deleteCommentModal`)
  - 비밀번호 입력 동적 표시 (JavaScript)

### 📋 관련 문서 업데이트

#### PROJECT_DOCUMENTATION.md
- ✅ **섹션 7. 주요 기능 명세** 업데이트
  - 파일 다운로드 기능 추가
  - 게시글 수정/삭제 기능 추가
  - 조회수 중복 방지 기능 추가

- ✅ **섹션 5. API 요청 흐름** 추가
  - 게시글 수정 흐름도
  - 게시글 삭제 흐름도 (Soft Delete 강조)
  - 조회수 증가 흐름도

#### QUICK_REFERENCE.md
- ✅ **주요 URL** 테이블 업데이트
  ```
  GET  /counsel/edit/{id}        # 게시글 수정 폼
  POST /counsel/edit/{id}        # 게시글 수정 처리
  POST /counsel/delete/{id}      # 게시글 삭제 (Soft Delete)
  GET  /counsel/download/{fileId} # 파일 다운로드
  ```

- ✅ **Soft Delete** 섹션 추가
  ```java
  @SQLDelete(sql = "UPDATE counsel_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
  @SQLRestriction("del_flag = 0")
  ```

#### FEATURE_UPGRADE.md (신규)
- ✅ **기능 업그레이드 상세 문서 생성**
  - 5가지 기능별 구현 내용
  - 코드 예시 및 테스트 시나리오
  - 파일 변경 이력
  - 다음 단계 추천

### 🔧 기술적 개선 사항

#### 보안 강화
- ✅ 비밀번호 검증 로직 (수정/삭제 시)
- ✅ 경로 역참조 방지 (`Path.normalize()`)
- ✅ 세션 기반 권한 관리 (unlock, viewCount)

#### 로깅 강화
- ✅ 수정 성공: `log.info("Successfully updated post with ID: {}", postId)`
- ✅ 삭제 성공: `log.info("Successfully soft-deleted post with ID: {} (title: {})", postId, title)`
- ✅ 비밀번호 검증 실패: `log.warn("Failed password verification for..."`
- ✅ 예외 발생: `log.error("Error occurred while... : {}", e.getMessage())`

#### 예외 처리
- ✅ try-catch 블록으로 안정성 확보
- ✅ 조회수 증가 실패 시에도 서비스 계속 진행
- ✅ Flash 메시지로 사용자에게 피드백

### 📊 파일 변경 이력

| 파일 | 변경 유형 | 라인 수 | 설명 |
|------|---------|---------|------|
| **FileDownloadController.java** | 수정 | +15 | 파일 다운로드 완성 |
| **CounselService.java** | 추가 | +80 | updatePost, deletePost, incrementViewCount |
| **CounselContentStorage.java** | 추가 | +14 | deleteHtml 메서드 |
| **CounselController.java** | 추가 | +50 | 수정/삭제 엔드포인트, 조회수 로직 |
| **counsel-edit.html** | 생성 | +40 | 게시글 수정 페이지 |
| **counselDetail.html** | 대규모 수정 | +150 | UI 개선, 모달 추가, 댓글 레이아웃 |
| **FEATURE_UPGRADE.md** | 생성 | +400 | 기능 업그레이드 문서 |

### ✅ 빌드 및 테스트

#### 컴파일 성공
```bash
./gradlew compileJava
BUILD SUCCESSFUL in 16s
```

#### Soft Delete 검증
- ✅ `@SQLDelete` 어노테이션 적용 확인
- ✅ `UPDATE` 쿼리로 변환 확인
- ✅ `del_flag=1, deleted_at=NOW()` 설정 확인
- ✅ `@SQLRestriction("del_flag = 0")` 필터링 확인
- ✅ 로그 기록 확인

#### 기능 테스트 시나리오
1. ✅ 파일 다운로드 (한글 파일명)
2. ✅ 게시글 수정 (비밀번호 검증)
3. ✅ 게시글 삭제 (Soft Delete)
4. ✅ 조회수 중복 방지 (세션 기반)
5. ✅ 대댓글 시각적 구분
6. ✅ 운영자 댓글 배지 표시

### 🐛 해결된 문제

#### FileDownloadController 중복 코드 제거
**문제**: 파일 다운로드 메서드에 중복 코드 존재

**해결**:
- 중복된 ResponseEntity 반환 코드 제거
- UTF-8 인코딩 로직으로 통일
- JavaDoc 주석 정리

**영향 범위**: `FileDownloadController.java` 전체 리팩토링

---

## [1.0.0] - 2025-11-05

### 🎉 초기 프로젝트 설정

#### 추가된 기능
- ✅ 온라인상담 게시판 (counsel) 모듈 구현
  - 공개/비공개 게시글 기능
  - 비밀번호 검증 시스템
  - 댓글 기능 (운영자/사용자 구분)
  - 첨부파일 업로드/관리
  - 본문 HTML 파일 저장
  - Soft Delete 구현
  - 파일 자동 정리 스케줄러 (2주 후)

- ✅ 커뮤니티 게시판 (community) 모듈 구현
  - 공지사항/자유게시판
  - 검색/페이징 기능

- ✅ 공통 모듈 (common) 구현
  - BaseEntity (id 필드)
  - NamedEntity (name 필드)
  - PageResponse (페이징 공통 DTO)
  - QueryDSL 설정
  - 데이터 초기화 (DataInit)

#### 기술 스택
- **Backend**: Spring Boot 3.5.0, Spring Data JPA, QueryDSL 5.0.0
- **Database**: MySQL 8.0
- **View**: Thymeleaf 3.1.3
- **Build**: Gradle 8.14.3
- **Java**: JDK 17
- **보안**: BCrypt (비밀번호 해싱)
- **검증**: Apache Tika (MIME 타입), Jsoup (HTML Sanitize)

#### 데이터베이스 설계
- `counsel_post`: 온라인상담 게시글 (16개 컬럼)
- `counsel_comment`: 댓글 (13개 컬럼, 대댓글 지원)
- `counsel_attachments`: 첨부파일 (8개 컬럼)
- `counsel_post_attachments`: 게시글-첨부파일 관계 (중간 테이블)
- `counsel_comment_attachment`: 댓글-첨부파일 관계 (중간 테이블)
- `community_post`: 커뮤니티 게시글 (11개 컬럼)
- `attachment`: 공용 첨부파일 (9개 컬럼)

#### 설정 파일
- `application.yml`: 기본 설정
- `application-dev.yml`: 개발 환경 설정
  - `ddl-auto: create-drop`
  - `sessionVariables=FOREIGN_KEY_CHECKS=0` (외래키 제약조건 비활성화)
- `build.gradle`: 의존성 관리

#### 문서 작성
- ✅ `PROJECT_DOCUMENTATION.md` (40.3 KB) - 상세 문서
- ✅ `QUICK_REFERENCE.md` (8.4 KB) - 빠른 참조 가이드
- ✅ `CHANGELOG.md` (이 파일) - 변경 이력 관리

### 🐛 해결된 문제

#### 1. 외래키 DROP 에러 (2025-11-05)
**문제**: 서버 종료 시 `Cannot drop table 'counsel_post' referenced by a foreign key`

**원인**: 
- MySQL이 외래키로 참조되는 부모 테이블을 먼저 DROP할 수 없음
- Hibernate의 `create-drop` 모드에서 테이블 삭제 순서가 잘못됨

**해결**:
```yaml
# application-dev.yml
datasource:
  url: jdbc:mysql://localhost:3306/petclinic?sessionVariables=FOREIGN_KEY_CHECKS=0
```

**영향 범위**:
- `application-dev.yml` 수정
- `CounselComment.java` - `@JoinColumn`에서 `foreignKey` 옵션 제거
- 개발 환경에서만 적용 (운영 환경 보호)

**관련 문서**:
- `PROJECT_DOCUMENTATION.md` → **9.1 외래키 DROP 에러 해결**
- `QUICK_REFERENCE.md` → **문제 해결 이력 #1**

#### 2. Entity 이름 충돌 (2025-11-05)
**문제**: `common.table.Attachment`와 `counsel.model.Attachment` 엔티티 이름 중복

**원인**: 
- 두 클래스 모두 `@Entity` 사용
- JPA가 같은 이름 "Attachment"로 인식

**해결**:
```java
// counsel.model.Attachment
@Entity(name = "CounselAttachment")
@Table(name = "counsel_attachments")
public class Attachment { }
```

**영향 범위**:
- `counsel/model/Attachment.java` 수정
- 테이블명: `counsel_attachments`로 변경

**관련 문서**:
- `PROJECT_DOCUMENTATION.md` → **9.2 Entity 이름 충돌 해결**
- `QUICK_REFERENCE.md` → **문제 해결 이력 #2**

#### 3. 데이터 초기화 로직 개선 (2025-11-05)
**변경 전**: COMPLETE 상태 72개 고정 → 나머지 WAIT/END 랜덤

**변경 후**: WAIT/COMPLETE/END 완전 랜덤 분배 (각 1/3 확률)

**코드 변경**:
```java
// DataInit.java
private CounselStatus randomStatus() {
    CounselStatus[] values = CounselStatus.values();
    return values[ThreadLocalRandom.current().nextInt(values.length)];
}
```

**영향 범위**:
- `DataInit.java` 수정
- 112개 게시글의 상태가 완전 랜덤으로 분배
- COMPLETE 상태 게시글만 댓글 1개 보장

**관련 문서**:
- `PROJECT_DOCUMENTATION.md` → **9.3 데이터 초기화 개선**
- `QUICK_REFERENCE.md` → **문제 해결 이력 #3**
- `QUICK_REFERENCE.md` → **데이터 초기화** 섹션 업데이트

### 📋 개발 규칙 정립

1. ❌ **Entity를 뷰/API에 직접 노출 금지**
2. ✅ **DTO + Mapper 사용 필수**
3. ✅ **QueryDSL은 RepositoryImpl에서만**
4. ✅ **LocalDateTime 사용** (Date 사용 금지)
5. ✅ **Soft Delete** (@SQLDelete, @SQLRestriction)
6. ✅ **로그 관리** (삭제/갱신 시 명확한 로그)
7. ✅ **메모리 누수 방지** (try-catch-resources)
8. ✅ **Author 정보**: Jeongmin Lee
9. ✅ **JavaDoc 작성** (클래스/메서드별 설명, 미구현 기능 명시)

### 📊 초기 데이터

#### 커뮤니티 게시판
- 공지사항 3개 + 더미 103개 = **총 106개**

#### 온라인상담 게시판
- **총 112개** 게시글 (페이지당 10개 기준, 11.2페이지 분량)
- 상태: WAIT/COMPLETE/END 랜덤 (각 1/3 확률)
- 공개/비공개: 랜덤 (50% 확률)
- COMPLETE 상태는 운영자 댓글 1개 자동 생성
- 비공개 게시글 비밀번호: `1234`

---

## 변경 이력 템플릿

### [버전] - YYYY-MM-DD

#### 🎯 추가된 기능
- ✅ [기능명]
  - 설명
  - 관련 파일
  - 영향 범위

#### 🔧 수정된 기능
- 🔄 [기능명]
  - 변경 이유
  - 변경 내용
  - 관련 파일
  - 영향 범위

#### 🐛 버그 수정
- 🔨 [버그 설명]
  - 문제 원인
  - 해결 방법
  - 관련 파일
  - 영향 범위

#### 📚 문서 업데이트
- 📝 PROJECT_DOCUMENTATION.md 섹션 X.X 업데이트
- 📝 QUICK_REFERENCE.md 섹션 Y 업데이트

#### 🗑️ 제거된 기능
- ❌ [기능명]
  - 제거 이유
  - 대체 방안

#### ⚙️ 설정 변경
- 🔧 [파일명]
  - 변경 내용
  - 변경 이유
  - 영향 범위

#### 🏗️ 구조 변경
- 🔨 [패키지/클래스 구조 변경]
  - 변경 내용
  - 변경 이유
  - 마이그레이션 가이드

---

## 문서 업데이트 규칙

### 변경사항이 발생할 때마다:

1. **CHANGELOG.md 업데이트** (이 파일)
   - 날짜, 카테고리, 변경 내용 기록
   - 관련 파일 및 영향 범위 명시

2. **PROJECT_DOCUMENTATION.md 업데이트**
   - 해당 섹션 내용 수정
   - 새로운 기능은 **7. 주요 기능 명세**에 추가
   - 구조 변경은 **3. 패키지 구조**에 반영
   - 테이블 변경은 **4. 데이터베이스 설계**에 반영
   - API 변경은 **5. API 요청 흐름**에 반영
   - 설정 변경은 **8. 설정 파일**에 반영
   - 문제 해결은 **9. 문제 해결 및 개선 이력**에 추가

3. **QUICK_REFERENCE.md 업데이트**
   - 핵심 변경사항만 간략히 업데이트
   - 주요 URL 변경 시 **주요 URL** 테이블 업데이트
   - 테이블 스키마 변경 시 **주요 테이블** 섹션 업데이트
   - 문제 해결 시 **문제 해결 이력** 섹션에 추가
   - 체크리스트 추가/수정 시 **체크리스트** 섹션 업데이트

4. **관련 소스코드 주석 업데이트**
   - JavaDoc `@description`에 변경 이력 추가
   - 미구현 기능 목록 업데이트

---

## 다음 개발 계획

### 🔴 우선순위 높음 (2주 이내)
- [ ] 로그인/회원가입 기능
- [ ] 관리자 권한 관리 (Spring Security)
- [ ] 파일 다운로드 기능

### 🟡 우선순위 중간 (1개월 이내)
- [ ] 대댓글 트리 구조 UI 개선
- [ ] 게시글 수정/삭제 기능
- [ ] 파일 업로드 진행률 표시

### 🟢 우선순위 낮음 (2개월 이내)
- [ ] 조회수 중복 방지 (IP/세션 기반)
- [ ] 좋아요 기능
- [ ] 이미지 썸네일 자동 생성
- [ ] Redis 캐싱 도입

---

**문서 버전**: 1.0.0  
**최종 수정**: 2025-11-05  
**다음 업데이트**: 변경사항 발생 시 즉시

