# Community 게시판 좋아요 기능 고도화

**작성일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 기능 개선 / UI/UX

---

## 📋 작업 요약

Community(공지사항) 게시판의 좋아요 기능을 사용자 친화적으로 개선하고, 좋아요 누른 사용자 목록을 시각적으로 표시하도록 UI를 고도화했습니다.

---

## ✅ 완료된 작업

### 1. User Entity 확장 (프로필 이미지 지원)

**파일**: `User.java`

**변경 사항**:
- `profileImageUrl` 컬럼 추가 (nullable, 향후 마이페이지에서 업로드 예정)
- 프로필 이미지 유무 확인 메서드 추가: `hasProfileImage()`
- 이니셜 추출 메서드 추가: `getInitial()` (한글 1자, 영문 2자)
- 아바타 배경색 생성 메서드 추가: `getAvatarColor()` (username 해시 기반 6색 중 선택)

```java
@Column(name = "profile_image_url", length = 500)
private String profileImageUrl;

public String getInitial() {
    // 한글: "홍길동" → "홍"
    // 영문: "John" → "JO"
}

public String getAvatarColor() {
    // username 해시값 → 6가지 색상 중 고정 반환
    // #3b82f6 (파란색), #10b981 (초록색), ...
}
```

---

### 2. Repository 확장

**파일**: `CommunityPostLikeRepository.java`, `UserRepository.java`

**추가 메서드**:

```java
// 좋아요 누른 사용자 목록 조회 (생성일시 오름차순)
List<CommunityPostLike> findAllByPostIdOrderByCreatedAtAsc(Long postId);

// username 리스트로 User 조회
List<User> findByUsernameIn(List<String> usernames);
```

---

### 3. Service 계층 확장

**파일**: `CommunityService.java`

**추가 메서드**:

```java
/**
 * 특정 게시글에 좋아요를 누른 사용자 목록 조회
 * @param postId 게시글 ID
 * @return 좋아요 누른 사용자의 username 리스트
 */
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public List<String> getLikedUsernames(Long postId);
```

---

### 4. Controller 수정

**파일**: `CommunityController.java`

**변경 사항**:
- `UserRepository` 의존성 주입
- `detail` 메서드에서 좋아요 누른 사용자의 User 정보 조회
- Model에 `likedUsers` 추가 (List<User>)

```java
// 좋아요 누른 사용자 정보 조회
List<String> likedUsernames = communityService.getLikedUsernames(id);
List<User> likedUsers = userRepository.findByUsernameIn(likedUsernames);

model.addAttribute("likedUsers", likedUsers);
```

---

### 5. UI 개선 (noticeDetail.html)

**주요 변경 사항**:

#### ✅ 답변 탭 제거
- 공지사항은 답변 기능을 제공하지 않으므로 답변 탭 전체 제거
- 좋아요 탭만 단독 표시

#### ✅ 좋아요 패널 UI 개선

**변경 전**:
```
좋아요 버튼만 표시 + "00명이 좋아합니다" 문구
```

**변경 후**:
```
┌───────────────────────────────────────────────────────────┐
│  탭: [❤️ 좋아요 (5)] ← 탭에서 직접 클릭 가능               │
│  ─────────────────────────────────────────────────────── │
│  📌 이 글에 공감한 사용자                                  │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐                │
│  │ [홍] │  │ [김] │  │ [이] │  │ [J]  │  ← 한 줄에 4명 │
│  │홍길동│  │김철수│  │이영희│  │ John │                  │
│  └──────┘  └──────┘  └──────┘  └──────┘                │
│  ┌──────┐  ┌──────┐  ┌──────┐                          │
│  │ [박] │  │ [A]  │  │ [최] │                            │
│  │박민수│  │ Amy  │  │최영수│                            │
│  └──────┘  └──────┘  └──────┘                          │
└───────────────────────────────────────────────────────────┘

반응형 대응:
- PC (lg): 4명/줄
- 태블릿 (md): 3명/줄
- 모바일 (sm): 2명/줄
```

**UI 구성**:
- **좋아요 탭**: 탭 버튼 자체에서 좋아요 클릭 가능
  - 좋아요 안 누름: `♡ 좋아요 (0)` (빈 하트)
  - 좋아요 누름: `❤️ 좋아요 (5)` (빨간 하트)
- **헤더**: "📌 이 글에 공감한 사용자"
- **바디**: 4열 그리드 레이아웃 (한 줄에 4명씩, 반응형)
  - PC: 4명/줄 (col-lg-3)
  - 태블릿: 3명/줄 (col-md-4)
  - 모바일: 2명/줄 (col-sm-6)
- **아바타**:
  - 프로필 이미지 있으면: 실제 이미지 표시 (원형, 36px)
  - 프로필 이미지 없으면: 닉네임 이니셜 + 배경색 (원형, 36px)
- **닉네임**: 아바타 오른쪽에 표시 (text-truncate로 넘침 방지)
- **패널 안의 큰 하트 제거**: 탭에서만 클릭 가능

---

## 🎨 아바타 시스템

### 이미지 없을 때 처리 방안

**방식**: 닉네임 이니셜 + username 해시 기반 배경색

```html
<!-- 프로필 이미지 있을 때 -->
<div style="background-image: url('/uploads/profile.jpg')"></div>

<!-- 프로필 이미지 없을 때 -->
<div style="background-color: #3b82f6; color: white;">홍</div>
```

**배경색 선택 로직**:
```java
String[] colors = {
    "#3b82f6", // 파란색
    "#10b981", // 초록색
    "#8b5cf6", // 보라색
    "#f59e0b", // 주황색
    "#ec4899", // 분홍색
    "#06b6d4"  // 청록색
};
int index = Math.abs(username.hashCode() % colors.length);
```

**장점**:
- ✅ 사용자별 고유한 색상 (username 기반 해시)
- ✅ 추가 라이브러리 불필요 (순수 Java + CSS)
- ✅ 향후 프로필 이미지 업로드 기능 추가 시 코드 변경 최소
- ✅ 시각적 구분 용이 (UX 향상)

---

## 📊 데이터베이스 변경

### User 테이블 컬럼 추가

```sql
ALTER TABLE users
ADD COLUMN profile_image_url VARCHAR(500) NULL COMMENT '프로필 이미지 경로';
```

**특징**:
- `ddl-auto: update` 설정으로 자동 반영
- NULL 허용 (기존 사용자 데이터에 영향 없음)
- 향후 마이페이지에서 업로드 기능 추가 예정

---

## 🔄 작업 흐름

### 게시글 상세 조회 시 좋아요 정보 로드

```
1. Controller: detail() 호출
   ↓
2. Service: getLikedUsernames(postId) 호출
   ↓ 
3. Repository: findAllByPostIdOrderByCreatedAtAsc() 실행
   ↓
4. Service: username 리스트 반환
   ↓
5. Controller: userRepository.findByUsernameIn(usernames) 호출
   ↓
6. User 정보(닉네임, 프로필 이미지) 조회
   ↓
7. Model에 likedUsers 추가
   ↓
8. Thymeleaf: 좋아요 패널에 사용자 목록 렌더링
```

---

## ✅ 검증 결과

### 컴파일 성공

```bash
.\gradlew clean compileJava --no-daemon
# 결과: BUILD SUCCESSFUL
```

### 주요 검증 항목

- ✅ User Entity에 profileImageUrl 컬럼 정상 추가
- ✅ 아바타 메서드(getInitial, getAvatarColor) 정상 동작
- ✅ Repository 메서드 정상 컴파일
- ✅ Service 로직 정상 컴파일
- ✅ Controller 의존성 주입 정상
- ✅ Thymeleaf 템플릿 문법 오류 없음

---

## 📝 향후 작업

### 1. 마이페이지 프로필 이미지 업로드 기능

```java
// MyPageController에 추가 예정
@PostMapping("/profile/image")
public String uploadProfileImage(@RequestParam MultipartFile image, Authentication auth) {
    String storedPath = fileService.store(image);
    userService.updateProfileImage(auth.getName(), storedPath);
    return "redirect:/mypage";
}
```

### 2. Counsel, Photo 게시판에도 동일 적용

**작업 순서**:
1. Counsel 패키지: 좋아요 탭 우선 순위 변경 + 사용자 목록 표시
2. Photo 패키지: 좋아요 탭 우선 순위 변경 + 사용자 목록 표시

### 3. 좋아요 알림 기능 (선택사항)

- 게시글 작성자에게 좋아요 알림
- 웹소켓 기반 실시간 알림

---

## 📚 관련 문서

- [TABLE_DEFINITION.md](../03-database/TABLE_DEFINITION.md) - User 테이블 스키마
- [UI_SCREEN_DEFINITION.md](../05-ui-screens/UI_SCREEN_DEFINITION.md) - 좋아요 패널 UI 정의

---

**변경 이력**:
- 2025-11-28: Community 게시판 좋아요 기능 고도화 완료
- 2025-11-28: Counsel(온라인상담) 게시판 좋아요 기능 고도화 완료

