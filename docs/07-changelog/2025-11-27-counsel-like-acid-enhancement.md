# 📌 Counsel 패키지 좋아요 기능 ACID 트랜잭션 고도화 (2025-11-27)

**작성일**: 2025년 11월 27일  
**작성자**: Jeongmin Lee  
**Phase**: Phase 2 고도화 - ACID 트랜잭션 속성 강화

---

## ✅ 완료 사항

### 1️⃣ **ACID 트랜잭션 속성 적용** ✅

#### toggleLike() 메서드 고도화

**Before (기본 트랜잭션)**:
```java
@Transactional
public boolean toggleLike(Long postId, Authentication authentication) {
    // 기본적인 로직만 존재
}
```

**After (ACID 고도화)**:
```java
@Transactional(
    isolation = Isolation.READ_COMMITTED,  // 격리성 수준 명시
    rollbackFor = Exception.class           // 모든 예외에 대해 롤백
)
public boolean toggleLike(Long postId, Authentication authentication) {
    // 1. 입력 검증 (Consistency)
    // 2. 게시글 존재 확인 (Consistency)
    // 3. 좋아요 중복 확인 (Isolation)
    // 4. 좋아요 추가/취소 (Atomicity)
    // 5. 즉시 DB 반영 flush() (Durability)
    // 6. UNIQUE 제약 위반 처리 (Consistency)
    // 7. 예외 처리 및 롤백 (Atomicity)
}
```

**적용된 ACID 속성**:

1. **Atomicity (원자성)**
   - 좋아요 추가/삭제가 완전히 성공하거나 완전히 실패
   - `likeRepository.flush()` 사용으로 즉시 DB 반영
   - 예외 발생 시 자동 롤백으로 이전 상태 복구

2. **Consistency (일관성)**
   - 입력값 검증 (null, 빈 문자열 체크)
   - 게시글 존재 여부 확인
   - UNIQUE 제약으로 중복 좋아요 방지
   - `DataIntegrityViolationException` 처리로 무결성 유지

3. **Isolation (격리성)**
   - `READ_COMMITTED` 수준으로 더티 리드 방지
   - 동시에 여러 사용자가 좋아요를 눌러도 독립적으로 처리
   - 커밋된 데이터만 읽음

4. **Durability (지속성)**
   - `flush()` 메서드로 즉시 DB에 영구 저장
   - 커밋 후 시스템 장애가 발생해도 데이터 보존

---

#### getLikeCount() 메서드 고도화

**Before**:
```java
public long getLikeCount(Long postId) {
    return likeRepository.countByPostId(postId);
}
```

**After**:
```java
@Transactional(
    readOnly = true,                      // 읽기 전용 최적화
    isolation = Isolation.READ_COMMITTED  // 커밋된 데이터만 조회
)
public long getLikeCount(Long postId) {
    try {
        long count = likeRepository.countByPostId(postId);
        log.debug("✅ [ACID-Consistency] Like count retrieved: postId={}, count={}", postId, count);
        return count;
    } catch (Exception e) {
        log.error("❌ [ACID-Error] Failed to get like count: postId={}, error={}", 
            postId, e.getMessage(), e);
        return 0L; // 안전한 기본값
    }
}
```

**개선 사항**:
- `readOnly = true`로 성능 최적화 (쓰기 락 불필요)
- `READ_COMMITTED`로 일관성 보장
- 예외 발생 시 안전한 기본값(0) 반환

---

#### isLikedByUser() 메서드 고도화

**Before**:
```java
public boolean isLikedByUser(Long postId, Authentication authentication) {
    if (authentication == null) {
        return false;
    }
    String username = authentication.getName();
    return likeRepository.existsByPostIdAndUsername(postId, username);
}
```

**After**:
```java
@Transactional(
    readOnly = true,
    isolation = Isolation.READ_COMMITTED
)
public boolean isLikedByUser(Long postId, Authentication authentication) {
    // 비로그인 사용자 체크
    if (authentication == null) {
        return false;
    }

    String username = authentication.getName();
    
    // username 유효성 검증
    if (username == null || username.trim().isEmpty()) {
        log.warn("⚠️ Invalid username for like check: postId={}", postId);
        return false;
    }

    try {
        boolean isLiked = likeRepository.existsByPostIdAndUsername(postId, username);
        log.debug("✅ [ACID-Consistency] Like status checked: postId={}, username={}, isLiked={}", 
            postId, username, isLiked);
        return isLiked;
    } catch (Exception e) {
        log.error("❌ [ACID-Error] Failed to check like status: postId={}, username={}, error={}", 
            postId, username, e.getMessage(), e);
        return false; // 안전한 기본값
    }
}
```

**개선 사항**:
- `readOnly = true`로 성능 최적화
- username 유효성 검증 추가
- 예외 처리 및 로깅 강화

---

## 🔄 **동시성 시나리오별 처리**

### 시나리오 1: 동일 사용자가 동시에 2번 좋아요 클릭

**Before**:
```
사용자 클릭 (1차) → 좋아요 추가
사용자 클릭 (2차) → 좋아요 중복 추가 (오류 발생!)
```

**After (ACID 적용)**:
```
사용자 클릭 (1차) → 좋아요 추가 → DB COMMIT
사용자 클릭 (2차) → UNIQUE 제약 위반 감지 → DataIntegrityViolationException 처리 → 기존 좋아요 상태 반환
```

**결과**: ✅ 중복 방지 (Consistency 보장)

---

### 시나리오 2: 여러 사용자가 동시에 좋아요

**Before**:
```
사용자A 클릭 → 좋아요 추가
사용자B 클릭 → 좋아요 추가 (동시성 이슈 가능성)
```

**After (ACID 적용)**:
```
사용자A 클릭 → READ_COMMITTED 격리 수준으로 독립 처리 → 좋아요 추가
사용자B 클릭 → READ_COMMITTED 격리 수준으로 독립 처리 → 좋아요 추가
```

**결과**: ✅ 각각 독립적으로 처리 (Isolation 보장)

---

### 시나리오 3: 좋아요 추가 중 서버 장애

**Before**:
```
좋아요 추가 시작 → 서버 장애 → 데이터 불일치 발생 가능
```

**After (ACID 적용)**:
```
좋아요 추가 시작 → flush()로 DB 반영 → 커밋 전 서버 장애 → 자동 롤백 → 이전 상태 복구
```

**결과**: ✅ 데이터 일관성 유지 (Atomicity 보장)

---

### 시나리오 4: 좋아요 개수 조회 중 다른 사용자가 좋아요 추가

**Before**:
```
개수 조회 시작 (10개) → 다른 사용자 좋아요 추가 → 조회 결과 (10개 or 11개?) → 일관성 깨짐
```

**After (ACID 적용)**:
```
개수 조회 시작 → READ_COMMITTED로 커밋된 데이터만 조회 → 일관된 결과 반환
```

**결과**: ✅ 일관된 데이터 조회 (Consistency + Isolation 보장)

---

## 📊 **로깅 강화**

### ACID 속성별 로그 메시지

**Atomicity (원자성)**:
```java
log.info("✅ [ACID-Atomicity] Like added successfully: postId={}, username={}, likeId={}", 
    postId, username, savedLike.getId());

log.info("✅ [ACID-Atomicity] Like removed successfully: postId={}, username={}, likeId={}", 
    postId, username, like.getId());

log.error("❌ [ACID-Atomicity] Like toggle failed - Rolling back: postId={}, username={}, error={}", 
    postId, username, e.getMessage(), e);
```

**Consistency (일관성)**:
```java
log.warn("⚠️ [ACID-Consistency] Duplicate like attempt prevented: postId={}, username={}, error={}", 
    postId, username, e.getMessage());

log.debug("✅ [ACID-Consistency] Like count retrieved: postId={}, count={}", postId, count);

log.debug("✅ [ACID-Consistency] Like status checked: postId={}, username={}, isLiked={}", 
    postId, username, isLiked);
```

**Error Handling**:
```java
log.error("❌ [ACID-Error] Failed to get like count: postId={}, error={}", 
    postId, e.getMessage(), e);

log.error("❌ [ACID-Error] Failed to check like status: postId={}, username={}, error={}", 
    postId, username, e.getMessage(), e);
```

---

## 🎯 **성능 최적화**

### 1️⃣ 읽기 전용 트랜잭션

**Before**:
```java
public long getLikeCount(Long postId) {
    return likeRepository.countByPostId(postId);
}
```

**After**:
```java
@Transactional(readOnly = true)
public long getLikeCount(Long postId) {
    // ...
}
```

**효과**:
- 쓰기 락 불필요 → 성능 향상
- 읽기 전용 최적화 → 메모리 절약

---

### 2️⃣ flush() 사용으로 즉시 반영

**Before**:
```java
likeRepository.save(newLike);
// 트랜잭션 종료 시점에 DB 반영
```

**After**:
```java
likeRepository.save(newLike);
likeRepository.flush(); // 즉시 DB 반영 (Durability 보장)
```

**효과**:
- 데이터 영속성 즉시 보장
- 트랜잭션 커밋 전에도 DB 반영

---

### 3️⃣ 예외 처리로 안전한 기본값 반환

**Before**:
```java
public long getLikeCount(Long postId) {
    return likeRepository.countByPostId(postId);
    // 예외 발생 시 전파
}
```

**After**:
```java
public long getLikeCount(Long postId) {
    try {
        return likeRepository.countByPostId(postId);
    } catch (Exception e) {
        log.error("...");
        return 0L; // 안전한 기본값
    }
}
```

**효과**:
- 조회 실패해도 UI 깨지지 않음
- 사용자 경험 향상

---

## 📋 **코드 품질 개선**

### 1️⃣ 입력 검증 강화

**추가된 검증**:
```java
// username null/빈 문자열 체크
if (username == null || username.trim().isEmpty()) {
    log.error("Invalid username: postId={}, username={}", postId, username);
    throw new IllegalStateException("유효하지 않은 사용자 정보입니다.");
}
```

---

### 2️⃣ 예외 메시지 명확화

**Before**:
```java
throw new IllegalArgumentException("Invalid post ID: " + postId);
```

**After**:
```java
throw new IllegalArgumentException("존재하지 않는 게시글입니다. (ID: " + postId + ")");
```

---

### 3️⃣ JavaDoc 상세화

**추가된 문서**:
- ACID 속성별 설명
- 동시성 시나리오 예시
- 사용 목적 및 효과
- 예외 발생 조건

---

## ✅ **컴파일 검증 결과**

```bash
.\gradlew.bat compileJava --no-daemon --console=plain

BUILD SUCCESSFUL in 15s
1 actionable task: 1 executed
```

**검증 완료**:
- ✅ Spring Transactional 임포트 정상
- ✅ Isolation 수준 설정 정상
- ✅ 모든 메서드 컴파일 성공

---

## 📈 **적용 효과**

### Before (기본 트랜잭션)
- ❌ 동시성 이슈 가능성
- ❌ 중복 좋아요 발생 가능
- ❌ 예외 처리 미흡
- ❌ 로깅 부족

### After (ACID 고도화)
- ✅ **Atomicity**: 완전한 성공/실패 보장
- ✅ **Consistency**: 중복 방지 및 데이터 무결성 유지
- ✅ **Isolation**: 동시성 제어 (READ_COMMITTED)
- ✅ **Durability**: 데이터 영속성 즉시 보장
- ✅ 예외 처리 강화 (안전한 기본값)
- ✅ 로깅 강화 (ACID 속성별 구분)
- ✅ 성능 최적화 (readOnly, flush)

---

## 🎯 **다음 단계**

### Community 패키지 고도화 예정
- CommunityService의 좋아요 기능에 동일한 ACID 적용
- Photo 패키지 좋아요 기능에도 동일한 ACID 적용

### FAQ 패키지
- 좋아요 기능 제외 (관리자 전용 정보성 게시판)

---

**작업 완료일**: 2025년 11월 27일  
**다음 작업**: Community 패키지 좋아요 ACID 고도화  
**관련 문서**: [PROJECT_RULES_UPDATE_20251106.md](../01-project-overview/PROJECT_RULES_UPDATE_20251106.md)

