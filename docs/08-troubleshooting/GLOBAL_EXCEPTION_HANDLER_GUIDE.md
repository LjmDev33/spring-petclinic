# GlobalExceptionHandler 적용 가이드

**작성일**: 2025-11-26  
**목적**: 프로젝트 전체에 통합 예외 처리 적용

---

## 📋 1. GlobalExceptionHandler란?

### 개요
- **위치**: `common/exception/GlobalExceptionHandler.java`
- **역할**: 모든 Controller에서 발생하는 예외를 한 곳에서 처리
- **장점**: 
  - 중복 코드 제거 (각 Controller마다 try-catch 불필요)
  - 일관된 에러 응답 형식
  - API와 화면 요청 자동 구분

### 작동 방식
```
Controller에서 예외 발생
         ↓
GlobalExceptionHandler가 자동 감지
         ↓
예외 타입에 따라 적절한 핸들러 실행
         ↓
API 요청: JSON 응답 (ErrorResponse)
화면 요청: Thymeleaf 에러 페이지 (ModelAndView)
```

---

## 🔧 2. 적용 방법 (3단계)

### Step 1: 기존 try-catch 제거 및 Custom Exception 사용

#### Before (기존 방식)
```java
@PostMapping("/write")
public String write(@ModelAttribute PostDto dto) {
    try {
        service.createPost(dto);
        return "redirect:/post/list";
    } catch (Exception e) {
        log.error("Error: {}", e.getMessage());
        return "error"; // 에러 페이지로
    }
}
```

#### After (GlobalExceptionHandler 적용)
```java
@PostMapping("/write")
public String write(@ModelAttribute PostDto dto) {
    // try-catch 제거
    service.createPost(dto); // 예외는 GlobalExceptionHandler가 처리
    return "redirect:/post/list";
}
```

### Step 2: Service에서 Custom Exception 던지기

#### Before (기존 방식)
```java
public void createPost(PostDto dto) {
    Post post = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Post not found"));
    // ...
}
```

#### After (Custom Exception 사용)
```java
public void createPost(PostDto dto) {
    Post post = repository.findById(id)
        .orElseThrow(() -> EntityNotFoundException.of("Post", id));
    // ...
}
```

### Step 3: 파일 I/O 작업에 FileException 적용

#### Before (기존 방식)
```java
public void saveFile(byte[] data, String path) {
    try {
        Files.write(Paths.get(path), data);
    } catch (IOException e) {
        throw new RuntimeException("File write error", e);
    }
}
```

#### After (FileException 사용)
```java
public void saveFile(byte[] data, String path) {
    try {
        Files.write(Paths.get(path), data);
    } catch (IOException e) {
        throw new FileException(ErrorCode.FILE_WRITE_ERROR, e);
    }
}
```

---

## 📝 3. Custom Exception 종류 및 사용 시나리오

### 3.1 EntityNotFoundException
**사용 시점**: DB 조회 시 데이터가 없을 때

```java
// 게시글 조회
Post post = repository.findById(id)
    .orElseThrow(() -> EntityNotFoundException.of("Post", id));

// 댓글 조회
Comment comment = commentRepository.findById(commentId)
    .orElseThrow(() -> EntityNotFoundException.of("Comment", commentId));
```

### 3.2 BusinessException
**사용 시점**: 비즈니스 로직 위반 시

```java
// 비밀번호 불일치
if (!passwordMatches) {
    throw new BusinessException(ErrorCode.INVALID_PASSWORD);
}

// 권한 없음
if (!hasPermission) {
    throw new BusinessException(ErrorCode.ACCESS_DENIED);
}

// 이미 삭제된 게시글
if (post.isDeleted()) {
    throw new BusinessException(ErrorCode.POST_ALREADY_DELETED);
}
```

### 3.3 FileException
**사용 시점**: 파일 I/O 작업 실패 시

```java
// 파일 업로드
try {
    file.transferTo(new File(path));
} catch (IOException e) {
    throw new FileException(ErrorCode.ATTACHMENT_UPLOAD_FAILED, e);
}

// 파일 다운로드
try {
    Resource resource = new UrlResource(filePath.toUri());
    if (!resource.exists()) {
        throw new FileException(ErrorCode.ATTACHMENT_NOT_FOUND);
    }
} catch (MalformedURLException e) {
    throw new FileException(ErrorCode.INVALID_ATTACHMENT_PATH, e);
}
```

---

## 🎯 4. 실제 적용 예시 (CounselService)

### Before (기존 코드)
```java
public CounselPostDto getDetail(Long postId) {
    try {
        CounselPost entity = repository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));
        
        // HTML 파일 읽기
        String html = contentStorage.readHtml(entity.getContentPath());
        dto.setContent(html);
        
        return dto;
    } catch (IOException e) {
        log.error("Failed to read content file: {}", e.getMessage());
        throw new RuntimeException("Error reading post content", e);
    } catch (Exception e) {
        log.error("Unexpected error: {}", e.getMessage());
        throw new RuntimeException("Error retrieving post", e);
    }
}
```

### After (GlobalExceptionHandler 적용)
```java
public CounselPostDto getDetail(Long postId) {
    // 게시글 조회 (EntityNotFoundException)
    CounselPost entity = repository.findById(postId)
        .orElseThrow(() -> EntityNotFoundException.of("CounselPost", postId));
    
    // HTML 파일 읽기 (FileException)
    try {
        String html = contentStorage.readHtml(entity.getContentPath());
        dto.setContent(html);
    } catch (IOException e) {
        throw new FileException(ErrorCode.FILE_READ_ERROR, e);
    }
    
    return dto;
}
```

**변화**:
- ✅ try-catch 블록 제거
- ✅ Custom Exception 사용
- ✅ 불필요한 로그 제거 (GlobalExceptionHandler가 자동 로깅)
- ✅ 일관된 에러 응답

---

## 🔄 5. 적용 체크리스트

### Controller 수준
- [ ] 불필요한 try-catch 블록 제거
- [ ] `return "error"` 제거 (GlobalExceptionHandler가 처리)
- [ ] 에러 로그 제거 (GlobalExceptionHandler가 자동 로깅)

### Service 수준
- [ ] `RuntimeException` → Custom Exception 변경
- [ ] `IllegalArgumentException` → `BusinessException` 변경
- [ ] `NullPointerException` → `EntityNotFoundException` 변경
- [ ] `IOException` → `FileException` 변경

### 전체 프로젝트
- [ ] `counsel` 패키지 적용 완료
- [ ] `community` 패키지 적용 완료
- [ ] `photo` 패키지 적용 완료
- [ ] `faq` 패키지 적용 완료
- [ ] `user` 패키지 적용 완료
- [ ] `system` 패키지 적용 완료

---

## 📊 6. 적용 효과

### Before vs After

| 항목 | Before | After | 개선 |
|------|--------|-------|------|
| **코드 중복** | 각 Controller마다 try-catch | GlobalExceptionHandler | ✅ 중복 제거 |
| **에러 메시지** | 불일치 (개발자마다 다름) | ErrorCode 통일 | ✅ 일관성 확보 |
| **로깅** | 각 Controller마다 수동 | 자동 로깅 | ✅ 누락 방지 |
| **API 응답** | 형식 불일치 | ErrorResponse 통일 | ✅ 표준화 |
| **화면 에러** | 형식 불일치 | Thymeleaf 에러 페이지 통일 | ✅ UX 개선 |
| **유지보수** | 어려움 | 쉬움 | ✅ 생산성 향상 |

---

## ⚠️ 7. 주의사항

### 1. try-catch를 유지해야 하는 경우

#### 리소스 정리가 필요한 경우
```java
// ✅ try-with-resources는 유지
try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path))) {
    // 파일 읽기
} catch (IOException e) {
    throw new FileException(ErrorCode.FILE_READ_ERROR, e);
}
```

#### 부분적 오류 허용이 필요한 경우
```java
// ✅ for문 내부에서 개별 실패를 허용
for (File file : files) {
    try {
        processFile(file);
    } catch (Exception e) {
        log.warn("Failed to process file: {}", file.getName());
        // 다음 파일 계속 처리
    }
}
```

### 2. @Transactional과 함께 사용 시

```java
@Transactional
public void updatePost(Long id, PostDto dto) {
    // Custom Exception 발생 시 자동 롤백
    Post post = repository.findById(id)
        .orElseThrow(() -> EntityNotFoundException.of("Post", id));
    
    // 비즈니스 로직 오류 시 롤백
    if (조건) {
        throw new BusinessException(ErrorCode.POST_UPDATE_FAILED);
    }
}
```

### 3. 비동기 메서드에서 사용 시

```java
@Async
public CompletableFuture<Void> processAsync() {
    // 비동기 메서드에서도 동일하게 사용
    throw new BusinessException(ErrorCode.ASYNC_PROCESS_FAILED);
}
```

---

## 📚 8. 참고 자료

- **ErrorCode.java**: 모든 에러 코드 정의
- **BaseException.java**: 예외 계층 구조
- **GlobalExceptionHandler.java**: 예외 처리 로직
- **ErrorResponse.java**: 표준 에러 응답 형식

---

**작성 완료일**: 2025-11-26  
**다음 적용 대상**: counsel, community, photo, faq 패키지

