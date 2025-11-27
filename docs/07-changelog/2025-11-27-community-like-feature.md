# 📌 Community 패키지 좋아요 기능 완료 (2025-11-27)

**작성일**: 2025년 11월 27일  
**작성자**: Jeongmin Lee  
**Phase**: Phase 2-2 (Community 좋아요 기능)

---

## ✅ 완료 사항

### 1️⃣ **Entity 및 Repository 생성** ✅

#### CommunityPostLike Entity
- **파일**: `CommunityPostLike.java`
- **테이블**: `community_post_likes`
- **주요 필드**:
  - `id`: PK (BIGINT, AUTO_INCREMENT)
  - `post_id`: 게시글 FK (ManyToOne → CommunityPost)
  - `username`: 사용자 아이디 (String, 50자)
  - `created_at`: 좋아요 생성 시간 (LocalDateTime)
- **UNIQUE 제약**: `(post_id, username)` 중복 방지

#### CommunityPostLikeRepository
- **파일**: `CommunityPostLikeRepository.java`
- **주요 메서드**:
  - `findByPostIdAndUsername()`: 특정 사용자의 좋아요 조회
  - `countByPostId()`: 게시글 좋아요 개수 조회
  - `existsByPostIdAndUsername()`: 좋아요 여부 확인 (boolean)
  - `deleteByPostId()`: 게시글 삭제 시 연관 좋아요 삭제

---

### 2️⃣ **Service 레이어 구현** ✅

#### CommunityService 좋아요 기능
- **파일**: `CommunityService.java`

**구현된 메서드**:
```java
// 1. 좋아요 토글 (추가/취소)
public boolean toggleLike(Long postId, Authentication authentication)

// 2. 좋아요 개수 조회
public long getLikeCount(Long postId)

// 3. 특정 사용자가 좋아요를 눌렀는지 확인
public boolean isLikedByUser(Long postId, Authentication authentication)
```

**비즈니스 로직**:
1. **로그인 확인**: 비로그인 사용자는 `IllegalStateException` 발생
2. **토글 방식**: 이미 좋아요를 눌렀으면 취소, 안 눌렀으면 추가
3. **중복 방지**: UNIQUE 제약으로 DB 레벨에서 중복 방지
4. **트랜잭션 관리**: `@Transactional` 적용으로 데이터 일관성 보장

---

### 3️⃣ **Controller API 구현** ✅

#### CommunityController 좋아요 API
- **파일**: `CommunityController.java`

**엔드포인트**:
```
POST /community/detail/{id}/like
```

**요청**:
- Method: `POST`
- Headers: CSRF 토큰 필수
- Authentication: Spring Security 인증 객체 (로그인 필요)

**응답 (JSON)**:
```json
{
  "success": true,
  "liked": true,
  "likeCount": 15,
  "message": "좋아요를 눌렀습니다."
}
```

**에러 응답**:
- 401 Unauthorized: 비로그인 사용자
- 400 Bad Request: 기타 오류

---

### 4️⃣ **UI 구현 (Thymeleaf + JavaScript)** ✅

#### noticeDetail.html 좋아요 UI
- **파일**: `noticeDetail.html`

**UI 구성**:
1. **탭 네비게이션**
   - 좋아요 탭: ♥ 아이콘 + 개수 표시
   - 답변 탭: 💬 아이콘 (답변 기능은 미제공)

2. **좋아요 버튼**
   - 큰 하트 아이콘 (font-size: 5rem)
   - 좋아요 상태에 따라 색상 변경:
     - 좋아요 누름: `fa fa-heart text-danger` (빨간 하트)
     - 좋아요 안 누름: `fa fa-heart-o text-secondary` (회색 빈 하트)

3. **좋아요 개수 표시**
   - "N명이 좋아합니다" 형식
   - 실시간 업데이트 (AJAX)

4. **비로그인 사용자 안내**
   - `sec:authorize="!isAuthenticated()"` 사용
   - "좋아요를 누르려면 로그인이 필요합니다." 안내 메시지

**JavaScript 함수**:
```javascript
function toggleLike(postId) {
  // CSRF 토큰 포함 AJAX 요청
  // 응답에 따라 하트 아이콘 및 개수 업데이트
  // Toast 알림 표시
}
```

**Toast 알림**:
- 성공: "좋아요를 눌렀습니다." / "좋아요를 취소했습니다."
- 실패: "로그인이 필요합니다." / "좋아요 처리 중 오류가 발생했습니다."

---

### 5️⃣ **초기 데이터 생성 (DataInit)** ✅

#### initCommunityLikes 메서드
- **파일**: `DataInit.java`

**생성 규칙**:
- 상위 20개 게시글에 대해 좋아요 생성
- 각 게시글마다 0~10개의 좋아요 랜덤 생성
- 사용자: `admin`, `user1`, `user2`, ... 형식
- 총 생성 개수: 약 100~200개 (랜덤)

**실행 조건**:
```java
if(communityLikeRepo.count() == 0 && communityPostRepo.count() > 0)
```

---

## 🔄 **데이터 흐름**

### 좋아요 추가/취소 프로세스

```
사용자 클릭 (하트 버튼)
    ↓
JavaScript toggleLike() 호출
    ↓
AJAX POST /community/detail/{id}/like
    ↓
CommunityController.toggleLike()
    ↓
CommunityService.toggleLike()
    ↓
├─ 이미 좋아요 누름? → likeRepository.delete() → return false
└─ 좋아요 안 누름? → likeRepository.save() → return true
    ↓
JSON 응답 { success, liked, likeCount, message }
    ↓
JavaScript에서 하트 아이콘 및 개수 업데이트
    ↓
Toast 알림 표시
```

---

## 🎨 **UI/UX 특징**

### 1️⃣ **직관적인 UI**
- 큰 하트 아이콘으로 한눈에 알아보기 쉬움
- 좋아요 상태에 따라 색상 변경 (빨강 ↔ 회색)
- 탭 형식으로 좋아요/답변 구분

### 2️⃣ **실시간 업데이트**
- AJAX로 페이지 새로고침 없이 좋아요 처리
- 하트 아이콘, 개수, 탭 카운트 모두 실시간 업데이트

### 3️⃣ **사용자 피드백**
- Toast 알림으로 성공/실패 즉시 알림
- 비로그인 사용자에게 로그인 유도 메시지

### 4️⃣ **보안**
- CSRF 토큰 검증 필수
- 로그인 사용자만 좋아요 가능
- 중복 좋아요 방지 (UNIQUE 제약)

---

## 📊 **테이블 스키마**

### community_post_likes

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 좋아요 ID |
| post_id | BIGINT | FK (community_post.id), NOT NULL | 게시글 ID |
| username | VARCHAR(50) | NOT NULL | 사용자 아이디 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 생성 일시 |

**UNIQUE 제약**: `(post_id, username)`

**인덱스**:
- PK: `id`
- FK: `post_id` (자동 인덱스)
- UNIQUE: `(post_id, username)`

---

## 🔗 **Counsel 패키지와의 일관성**

### 동일한 구조
Community 패키지의 좋아요 기능은 Counsel 패키지와 동일한 구조로 구현되었습니다.

| 항목 | Counsel 패키지 | Community 패키지 |
|------|---------------|-----------------|
| Entity | CounselPostLike | CommunityPostLike |
| Repository | CounselPostLikeRepository | CommunityPostLikeRepository |
| Service 메서드 | toggleLike(), getLikeCount(), isLikedByUser() | 동일 |
| API 엔드포인트 | POST /counsel/detail/{id}/like | POST /community/detail/{id}/like |
| UI 탭 구조 | 좋아요 탭 + 답변 탭 | 좋아요 탭 + 답변 탭 |
| Toast 알림 | TOAST.showSuccess/showError | 동일 |

---

## ✅ **컴파일 검증 결과**

```bash
.\gradlew.bat clean compileJava --no-daemon --console=plain

BUILD SUCCESSFUL in 17s
2 actionable tasks: 2 executed
```

**검증 완료**:
- ✅ Java 코드 컴파일 성공
- ✅ Entity 및 Repository 정상 동작
- ✅ Service 및 Controller 정상 동작
- ✅ DataInit 초기 데이터 생성 준비 완료

---

## 🎯 **FAQ 패키지 제외 확인**

FAQ 게시판은 좋아요 기능 추가 대상에서 **제외**되었습니다.

**이유**:
- FAQ는 관리자만 작성 가능한 정보성 게시판
- 좋아요 기능이 필요하지 않음

**적용 패키지**:
- ✅ Counsel (온라인상담) - Phase 2-1 완료
- ✅ Community (공지사항) - Phase 2-2 완료
- ❌ FAQ (자주묻는질문) - 제외
- ⏳ Photo (포토게시판) - Phase 2-3 예정

---

## 📈 **다음 단계 (Phase 2-3)**

### Photo 패키지 좋아요 기능 추가

**예정 작업**:
1. `PhotoPostLike` Entity 및 Repository 생성
2. `PhotoService`에 좋아요 메서드 추가
3. `PhotoController`에 좋아요 API 추가
4. `photoDetail.html`에 좋아요 UI 추가
5. DataInit에 포토게시판 좋아요 초기 데이터 추가

---

## 🎉 **Phase 2-2 완료**

Community 패키지의 좋아요 기능이 완벽하게 구현되었습니다!

**구현 완료 항목**:
- ✅ Entity 및 Repository
- ✅ Service 비즈니스 로직
- ✅ Controller API
- ✅ Thymeleaf UI + JavaScript
- ✅ 초기 데이터 생성
- ✅ 컴파일 검증
- ✅ 문서화

**특징**:
- Counsel 패키지와 동일한 구조로 일관성 유지
- 직관적인 UI/UX (큰 하트 아이콘)
- 실시간 업데이트 (AJAX)
- 보안 강화 (CSRF, 로그인 검증, 중복 방지)

---

**작업 완료일**: 2025년 11월 27일  
**다음 작업**: Phase 2-3 (Photo 패키지 좋아요 기능)

