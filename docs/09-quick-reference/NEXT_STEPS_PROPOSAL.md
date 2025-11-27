# 🚀 다음 작업 및 개선 사항 제안

**작성일**: 2025-11-26  
**작성자**: GitHub Copilot  
**목적**: 프로젝트 완성도 향상을 위한 다음 단계 제안

---

## 📊 프로젝트 현황 분석

### ✅ 최근 완료된 작업 (버전 3.5.3)

| 작업 | 상태 | 버전 | 완료일 |
|------|------|------|--------|
| **Phase 2: 좋아요 기능 구현** | ✅ 완료 | 3.5.3 | 2025-11-27 |
| - Counsel 패키지 좋아요 기능 | ✅ | 3.5.3 | 2025-11-26 |
| - Community 패키지 좋아요 기능 | ✅ | 3.5.3 | 2025-11-27 |
| - Photo 패키지 좋아요 기능 | ✅ | 3.5.3 | 2025-11-27 |
| - FAQ 게시판 (좋아요 제외) | ✅ | 3.5.3 | 2025-11-27 |
| **로그인 기능 고도화** | ✅ 완료 | 3.5.27 | 2025-11-26 |
| - ID 저장 기능 (쿠키 30일) | ✅ | | |
| - Remember-Me (자동 로그인 7일) | ✅ | | |
| - 공개 권한 확장 (FAQ, Photo) | ✅ | | |
| **포토게시판 개선** | ✅ 완료 | 3.5.26 | 2025-11-26 |
| **FAQ 게시판** | ✅ 완료 | 3.5.26 | 2025-11-26 |
| **Toast 알림 시스템** | ✅ 완료 | 3.5.24 | 2025-11-25 |
| **댓글 트리 구조** | ✅ 완료 | 3.5.23 | 2025-11-25 |
| **파일 다운로드** | ✅ 완료 | 3.5.1 | 2025-11-06 |
| **게시글 수정/삭제** | ✅ 완료 | 3.5.1 | 2025-11-06 |

### ❌ 미완료 작업 (우선순위별)

#### 🔴 **우선순위 높음** (즉시 작업 필요)

1. **파일 다운로드 권한 검증** ✅ **완료 (2025-11-27)**
   - 완료: 비공개 게시글 첨부파일 권한 검증 구현
   - 기능: 공개 게시글 - 모든 사용자 다운로드 가능 / 비공개 게시글 - 관리자 또는 세션 unlock 필요

2. **게시글 수정 시 첨부파일 관리** ✅ **완료 (2025-11-27)**
   - 완료: Counsel/Community/Photo 패키지 모두 구현 완료
   - 기능: 기존 파일 삭제/추가, Uppy UI 통합, Soft Delete 적용

3. **작성자 권한 검증** ✅ **완료 (2025-11-27)**
   - 완료: 로그인 사용자 = 작성자 확인 구현
   - 기능: User.nickname과 post.authorName 비교, 작성자는 비밀번호 불필요

4. **관리자 권한 체계 강화** ✅ **완료 (2025-11-27)**
   - 완료: 관리자 전용 상태 변경 기능 구현
   - 기능: 게시글 상태(WAIT/COMPLETE/END) 변경, 모든 게시글 관리 권한

#### 🟡 **우선순위 중간** (기능 추가)

5. **ACID 트랜잭션 속성 보장** ✅ **완료 (2025-11-27)**
   - 현재: 기본적인 @Transactional만 사용
   - 완료: 프로젝트 규칙에 ACID 속성 보장 규칙 추가
   - 내용: Atomicity, Consistency, Isolation, Durability 준수
   - 적용: 모든 Service 클래스 비즈니스 로직
   - 관련 문서: [PROJECT_RULES_UPDATE_20251106.md](../01-project-overview/PROJECT_RULES_UPDATE_20251106.md)

6. **멀티 로그인 제어** ✅ **완료 (2025-11-27)**
   - 완료: SystemConfig 기반 동적 제어 구현
   - 기능: multiLoginEnabled 설정에 따라 최대 5개 (On) 또는 1개 (Off) 세션 허용

7. **검색 기능 강화** ✅ **완료 (2025-11-27)**
   - 완료: 날짜 범위 필터링, 상태별 필터링 추가
   - 기능: startDate, endDate, status 파라미터로 고급 검색 지원

8. **이메일 발송 기능** ❌ (보류 - Phase 마지막으로 연기)
   - 현재: 비밀번호 찾기 시 콘솔 로그만 출력
   - 필요: 실제 이메일 발송 (SMTP 연동)
   - 게시글 상세 페이지: 좋아요 탭 + 답변(댓글) 탭 분리 ✅
   - 좋아요 탭: ♡ 아이콘, 로그인 사용자만 클릭 가능 ✅
   - 클릭 시 빨간 하트(♥)로 변경, 다시 클릭하면 취소 ✅
   - 답변 탭: 화살표 클릭 시 댓글 펼치기/접기 ✅
   - 각 탭에 카운트 표시 (예: 답변(3), 좋아요(10)) ✅
   - 패키지별 구현: counsel ✅ → community ✅ → photo ✅
   - FAQ는 제외 (관리자 전용 정보성 게시판)
   - 관련 문서:
     - [Counsel 좋아요 기능](../07-changelog/2025-11-26-counsel-like-feature.md)
     - [Community 좋아요 기능](../07-changelog/2025-11-27-community-like-feature.md)
     - [Photo 좋아요 기능](../07-changelog/2025-11-27-photo-like-feature.md)

#### 🟢 **우선순위 낮음** (추후 고려)

9. **통계 대시보드** ❌
   - 일별/월별 상담 통계
   - 상태별 통계 차트

10. **OAuth2 소셜 로그인** ❌
    - Google, Kakao, Naver 연동

11. **알림 기능** ❌
    - 댓글 작성 시 이메일/푸시 알림
    - 상태 변경 알림

---

## 🎯 단계별 작업 계획

### **Step 1: 권한 관리 강화** (예상 소요: 2-3시간)

#### 1️⃣ 파일 다운로드 권한 검증

**목표**: 비공개 게시글 첨부파일은 작성자/관리자만 다운로드

**구현 계획**:
```java
// FileDownloadController.java 수정
@GetMapping("/download/{fileId}")
public ResponseEntity<Resource> downloadFile(
    @PathVariable Integer fileId,
    HttpSession session,
    Authentication authentication) {
    
    Attachment attachment = attachmentRepository.findById(fileId).orElseThrow();
    
    // 첨부파일이 속한 게시글 조회
    CounselPostAttachment postAttachment = 
        postAttachmentRepository.findByAttachmentId(fileId).orElseThrow();
    CounselPost post = postAttachment.getPost();
    
    // 비공개 게시글인 경우 권한 검증
    if (post.isSecret()) {
        // 1. 관리자인지 확인
        boolean isAdmin = authentication != null && 
            authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // 2. 세션에 unlock된 게시글인지 확인
        Set<Long> unlockedPosts = (Set<Long>) session.getAttribute("unlockedCounselPosts");
        boolean isUnlocked = unlockedPosts != null && unlockedPosts.contains(post.getId());
        
        if (!isAdmin && !isUnlocked) {
            throw new AccessDeniedException("비공개 게시글의 첨부파일은 권한이 없습니다.");
        }
    }
    
    // 기존 다운로드 로직...
}
```

**예상 결과**:
- ✅ 공개 게시글 첨부파일: 모두 다운로드 가능
- ✅ 비공개 게시글 첨부파일: 비밀번호 입력 또는 관리자만 다운로드

#### 2️⃣ 게시글 작성자 권한 검증

**목표**: 로그인 사용자가 작성자인 경우 비밀번호 없이 수정/삭제

**구현 계획**:
```java
// CounselService.java 수정
public boolean canEditPost(Long postId, Authentication authentication, String password) {
    CounselPost post = repository.findById(postId).orElseThrow();
    
    // 1. 관리자는 항상 허용
    if (isAdmin(authentication)) {
        return true;
    }
    
    // 2. 로그인 사용자가 작성자인지 확인
    if (authentication != null) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null && post.getAuthorName().equals(user.getNickname())) {
            return true; // 작성자는 비밀번호 불필요
        }
    }
    
    // 3. 비밀번호 검증
    if (post.getPasswordHash() != null) {
        return BCrypt.checkpw(password, post.getPasswordHash());
    }
    
    return false;
}
```

**예상 결과**:
- ✅ 관리자: 모든 게시글 수정/삭제 가능
- ✅ 로그인 작성자: 본인 게시글 수정/삭제 (비밀번호 불필요)
- ✅ 비로그인 또는 타인: 비밀번호 입력 필요

#### 3️⃣ 관리자 전용 기능

**목표**: 관리자가 모든 게시글 상태 변경 가능

**구현 계획**:
```html
<!-- counselDetail.html 수정 -->
<div sec:authorize="hasRole('ADMIN')" class="mb-3">
  <label>상태 변경</label>
  <form th:action="@{'/counsel/detail/' + ${post.id} + '/status'}" method="post" class="d-inline">
    <select name="status" class="form-select d-inline w-auto">
      <option value="WAIT" th:selected="${post.status.name() == 'WAIT'}">답변대기</option>
      <option value="COMPLETE" th:selected="${post.status.name() == 'COMPLETE'}">답변완료</option>
      <option value="END" th:selected="${post.status.name() == 'END'}">상담종료</option>
    </select>
    <button type="submit" class="btn btn-sm btn-primary">변경</button>
  </form>
</div>
```

---

### **Step 2: 게시글 첨부파일 관리** (예상 소요: 2-3시간)

#### 1️⃣ 수정 페이지에 첨부파일 목록 표시

**목표**: 기존 첨부파일 목록 표시 및 삭제 기능

**구현 계획**:
```html
<!-- counsel-edit.html 추가 -->
<div class="mb-3">
  <label>기존 첨부파일</label>
  <ul class="list-group">
    <li class="list-group-item d-flex justify-content-between" 
        th:each="attachment : ${attachments}">
      <span>
        <i class="bi bi-paperclip"></i>
        <span th:text="${attachment.originalFileName}"></span>
        <span class="badge bg-secondary" th:text="${attachment.fileSizeFormatted}"></span>
      </span>
      <button type="button" 
              class="btn btn-sm btn-danger" 
              onclick="deleteAttachment([[${attachment.id}]])">
        <i class="bi bi-trash"></i>
      </button>
    </li>
  </ul>
</div>

<script>
function deleteAttachment(fileId) {
  if (confirm('파일을 삭제하시겠습니까?')) {
    // 삭제할 파일 ID를 hidden input에 추가
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'deleteFileIds';
    input.value = fileId;
    document.getElementById('editForm').appendChild(input);
    
    // 화면에서 제거
    event.target.closest('li').remove();
  }
}
</script>
```

#### 2️⃣ 새 파일 업로드 기능 추가

**목표**: Uppy를 사용하여 새 파일 추가

**구현 계획**:
```javascript
// counsel-edit.html 추가
const uppy = new Uppy.Core({
  restrictions: {
    maxFileSize: 5 * 1024 * 1024, // 5MB
    allowedFileTypes: ['image/*', '.pdf']
  }
});

uppy.use(Uppy.Dashboard, {
  target: '#uppy-dashboard',
  inline: true,
  height: 300
});

uppy.use(Uppy.XHRUpload, {
  endpoint: '/counsel/upload-temp',
  fieldName: 'files'
});
```

---

### **Step 3: 멀티 로그인 제어** (예상 소요: 1-2시간)

#### 1️⃣ SystemConfig 기반 동적 제어

**목표**: 관리자 설정 페이지에서 멀티 로그인 허용 개수 설정

**구현 계획**:
```java
// SecurityConfig.java 수정
@Bean
public SecurityFilterChain filterChain(HttpSecurity http, 
                                      SystemConfigService configService) throws Exception {
    
    // 시스템 설정에서 멀티 로그인 허용 여부 조회
    boolean multiLoginEnabled = configService.isMultiLoginEnabled();
    int maxSessions = multiLoginEnabled ? 5 : 1;
    
    http.sessionManagement(session -> session
        .maximumSessions(maxSessions)
        .maxSessionsPreventsLogin(false)
    );
    
    return http.build();
}
```

**예상 결과**:
- ✅ 설정 페이지에서 멀티 로그인 On/Off 가능
- ✅ On: 최대 5개 기기 동시 로그인
- ✅ Off: 1개 기기만 로그인 (기존 세션 만료)

---

### **Step 4: 이메일 발송 기능** (예상 소요: 2-3시간)

#### 1️⃣ SMTP 설정 추가

**구현 계획**:
```yaml
# application-dev.yml 추가
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### 2️⃣ 이메일 발송 서비스

**구현 계획**:
```java
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[PetClinic] 비밀번호 재설정 안내");
        message.setText(
            "안녕하세요.\n\n" +
            "비밀번호 재설정을 요청하셨습니다.\n\n" +
            "아래 링크를 클릭하여 비밀번호를 재설정해주세요:\n" +
            "http://localhost:8080/reset-password?token=" + token + "\n\n" +
            "링크는 24시간 동안 유효합니다.\n\n" +
            "감사합니다."
        );
        
        mailSender.send(message);
    }
}
```

---

## 🎨 UI/UX 개선 사항

### 1️⃣ 게시글 상세 페이지

**현재 문제**:
- 첨부파일 다운로드 버튼에 권한 표시 없음
- 작성자/관리자 구분 없음

**개선 방안**:
```html
<!-- 작성자 표시 개선 -->
<div class="d-flex align-items-center">
  <strong th:text="${post.authorName}"></strong>
  <span th:if="${isMyPost}" class="badge bg-info ms-2">
    <i class="bi bi-person-check"></i> 내 글
  </span>
  <span sec:authorize="hasRole('ADMIN')" class="badge bg-danger ms-2">
    <i class="bi bi-shield-check"></i> 관리자 권한
  </span>
</div>

<!-- 비공개 첨부파일 안내 -->
<div th:if="${post.secret}" class="alert alert-warning">
  <i class="bi bi-lock"></i>
  비공개 게시글의 첨부파일은 작성자와 관리자만 다운로드할 수 있습니다.
</div>
```

### 2️⃣ 마이페이지 추가

**새 기능**:
- 내가 작성한 게시글 목록
- 내가 작성한 댓글 목록
- 프로필 수정

**URL 구조**:
```
/mypage              # 프로필 정보
/mypage/posts        # 내 게시글 목록
/mypage/comments     # 내 댓글 목록
```

---

## 📊 데이터베이스 개선

### 1️⃣ 작성자 연결

**현재**: `author_name` 문자열만 저장

**개선**: `user_id` 외래키 추가

```sql
ALTER TABLE counsel_post 
ADD COLUMN user_id BIGINT,
ADD FOREIGN KEY (user_id) REFERENCES users(id);
```

**장점**:
- ✅ 작성자 확인 정확
- ✅ 닉네임 변경 시 자동 반영
- ✅ 내 게시글 조회 빠름

### 2️⃣ 좋아요 테이블 추가

```sql
CREATE TABLE counsel_post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT,          -- 로그인 사용자
    session_id VARCHAR(255),  -- 비로그인 사용자
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES counsel_post(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_user_like (post_id, user_id),
    UNIQUE KEY unique_session_like (post_id, session_id)
);
```

---

## 📝 문서 업데이트 필요

### 1️⃣ API_SPECIFICATION.md

**추가 필요**:
- 파일 다운로드 권한 검증 API
- 게시글 상태 변경 API (관리자 전용)
- 좋아요 API

### 2️⃣ UI_SCREEN_DEFINITION.md

**추가 필요**:
- 마이페이지 화면 정의
- 게시글 수정 시 첨부파일 관리 UI

### 3️⃣ TABLE_DEFINITION.md

**추가 필요**:
- `counsel_post.user_id` 컬럼 추가
- `counsel_post_likes` 테이블 추가

---

## ✅ 최종 권장 순서

### **Phase 1: 보안 강화** (1주차) ⏸️ 보류
1. ⏸️ 파일 다운로드 권한 검증
2. ⏸️ 작성자 권한 검증
3. ⏸️ 관리자 기능 강화
4. ⏸️ `user_id` 외래키 추가

### **Phase 2: 좋아요 기능 구현** (2주차) ✅ **완료 (2025-11-27)**
5. ✅ Counsel 패키지 좋아요 기능 (2025-11-26)
6. ✅ Community 패키지 좋아요 기능 (2025-11-27)
7. ✅ Photo 패키지 좋아요 기능 (2025-11-27)
8. ✅ FAQ 게시판은 제외 (관리자 전용 정보성 게시판)
9. ✅ ACID 트랜잭션 속성 보장 규칙 추가 (2025-11-27)

### **Phase 3: 기능 추가** (3주차) ✅ **완료 (2025-11-27)**
10. ✅ 게시글 첨부파일 관리 (2025-11-27)
    - ✅ Counsel 패키지: 첨부파일 추가/삭제 완료
    - ✅ Community 패키지: 첨부파일 추가/삭제 완료
    - ✅ Photo 패키지: 첨부파일 추가/삭제 완료

### **Phase 4: 보안 강화** (4주차) ✅ **완료 (2025-11-27)**
11. ✅ 파일 다운로드 권한 검증 (2025-11-27)
12. ✅ 작성자 권한 검증 강화 (2025-11-27)
13. ✅ 관리자 권한 체계 강화 (2025-11-27)
14. ✅ 멀티 로그인 제어 (2025-11-27)

### **Phase 5: 사용자 경험** (5주차) ✅ **완료 (2025-11-27)**
15. ✅ 마이페이지 구현 (2025-11-27)
    - ✅ 프로필 조회/수정 (이메일, 이름, 닉네임, 전화번호)
    - ✅ 비밀번호 변경
    - ✅ 내가 작성한 게시글 목록 (/mypage/my-posts)
    - ✅ 내가 작성한 댓글 목록 (/mypage/my-comments)

### **Phase 7: 검색 기능 강화** (7주차) ✅ **완료 (2025-11-27)**
16. ✅ 고급 검색 구현 (2025-11-27)
    - ✅ 날짜 범위 필터링 (startDate ~ endDate)
    - ✅ 상태별 필터링 (WAIT, COMPLETE, END)
    - ✅ 기존 검색 타입 유지 (title, content, author, 전체)
    - ✅ QueryDSL BooleanBuilder로 동적 쿼리 구현

### **Phase 4: 사용자 경험** (4주차) ⏳ 예정
14. ❌ 이메일 발송 기능
15. ❌ UI/UX 최적화
16. ❌ 문서 업데이트

### **Phase 5: 추가 기능** (5주차) ⏳ 예정
17. ❌ 통계 대시보드
18. ❌ 알림 기능
19. ❌ OAuth2 소셜 로그인

---

## 🎯 즉시 시작 가능한 작업

### 1️⃣ 파일 다운로드 권한 검증 (예상 1시간)

**작업 파일**:
- `FileDownloadController.java`

**작업 내용**:
- 비공개 게시글 확인
- 세션 unlock 상태 확인
- 관리자 권한 확인

**테스트 시나리오**:
1. 비공개 게시글 → 비밀번호 입력 → 첨부파일 다운로드 성공
2. 비공개 게시글 → 비밀번호 미입력 → 첨부파일 다운로드 실패 (403)
3. 관리자 로그인 → 비공개 게시글 첨부파일 다운로드 성공
4. 공개 게시글 → 모두 다운로드 가능

---

### 2️⃣ 작성자 권한 검증 (예상 1시간)

**작업 파일**:
- `CounselService.java`
- `CounselController.java`

**작업 내용**:
- `canEditPost()` 메서드 추가
- 로그인 사용자 = 작성자 확인
- 수정/삭제 권한 검증

**테스트 시나리오**:
1. 작성자 로그인 → 본인 게시글 수정 (비밀번호 불필요)
2. 타인 로그인 → 타인 게시글 수정 시도 → 권한 없음 (403)
3. 관리자 로그인 → 모든 게시글 수정 가능

---

## 📌 체크리스트

### Phase 1 시작 전 확인 사항
- [ ] 로그인 기능 테스트 완료
- [ ] 게시글 CRUD 기능 정상 작동
- [ ] 파일 업로드/다운로드 기능 정상 작동
- [ ] 데이터베이스 백업 완료
- [ ] Git 커밋 완료 (현재 상태 저장)

### 각 기능 완료 시 확인 사항
- [ ] 컴파일 성공
- [ ] 기능 테스트 완료
- [ ] 문서 업데이트
- [ ] Git 커밋 (변경 이력 기록)
- [ ] CHANGELOG.md 업데이트

---

**작성 완료**: 2025-11-26  
**최근 업데이트**: 2025-11-27 (Phase 7 완료)  
**문서 버전**: 1.6  
**Changelog**: 
- [Phase 3 & Phase 4-1 완료](../07-changelog/2025-11-27-phase3-and-phase4-completion.md)
- [Phase 4 완료](../07-changelog/2025-11-27-phase4-completion.md)
- [Phase 5 완료](../07-changelog/2025-11-27-phase5-completion.md)
- [Phase 7 완료](../07-changelog/2025-11-27-phase7-completion.md)

---

# 🎉 Phase 3 완료! 다음 단계: 보안 강화

## ✅ Phase 2 완료 사항 (2025-11-27)

### 좋아요 기능 구현 완료
- ✅ Counsel 패키지 좋아요 기능
- ✅ Community 패키지 좋아요 기능
- ✅ Photo 패키지 좋아요 기능
- ✅ 모든 패키지에서 동일한 구조로 일관성 유지
- ✅ 실시간 AJAX 업데이트
- ✅ Toast 알림 시스템
- ✅ 보안 강화 (CSRF, 로그인 검증, 중복 방지)

### ACID 트랜잭션 규칙 추가
- ✅ Atomicity (원자성) 보장
- ✅ Consistency (일관성) 유지
- ✅ Isolation (격리성) 제어
- ✅ Durability (지속성) 확보
- ✅ 모든 Service 계층에 적용

## ✅ Phase 3 완료 사항 (2025-11-27)

### 게시글 첨부파일 관리 완료
- ✅ Counsel 패키지: 첨부파일 추가/삭제 완료
- ✅ Community 패키지: 첨부파일 추가/삭제 완료
- ✅ Photo 패키지: 첨부파일 추가/삭제 완료
- ✅ 수정 페이지에 기존 첨부파일 목록 표시
- ✅ Soft Delete 정책 적용 (2주 후 물리 삭제)
- ✅ Uppy Dashboard 통합 (임시 업로드 → 최종 저장)
- ✅ 오류 알림 시스템 통합 (ErrorNotification.js)

## ✅ Phase 4 완료 사항 (2025-11-27)

### 파일 다운로드 권한 검증 완료
- ✅ FileDownloadController 권한 검증 로직 구현
- ✅ 공개 게시글: 모든 사용자 다운로드 가능
- ✅ 비공개 게시글 + 관리자(ROLE_ADMIN): 무조건 다운로드 가능
- ✅ 비공개 게시글 + 일반 사용자: 세션 unlock 필요
- ✅ 권한 없음: 403 Forbidden 반환
- ✅ NPE 방지 (fileId, session null 체크)
- ✅ 상세한 로깅 (모든 권한 검증 과정 기록)

---

**다음 추천 작업**: **Phase 4 계속 - 작성자 권한 검증 강화**  
**예상 소요 시간**: 1시간  
**난이도**: ⭐⭐ (중간)
