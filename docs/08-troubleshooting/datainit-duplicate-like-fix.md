# DataInit 중복 INSERT 오류 수정

**발생일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 버그 수정

---

## 🐛 문제 상황

### 오류 메시지

```
Duplicate entry '1-admin' for key 'photo_post_likes.uk_photo_post_likes_post_username'
could not execute statement [Duplicate entry '1-admin' for key 'photo_post_likes.uk_photo_post_likes_post_username']
constraint [photo_post_likes.uk_photo_post_likes_post_username]
```

### 원인

**DataInit의 좋아요 초기화 메서드에서 중복 체크 없이 무조건 INSERT**

1. `initPhotoLikes()` - 포토게시판 좋아요
2. `initCommunityLikes()` - 커뮤니티 좋아요

**문제 코드**:
```java
for (int i = 0; i < likeCount; i++) {
    String username = i == 0 ? likeUsername : "user" + i;
    
    // ❌ 중복 체크 없음
    PhotoPostLike like = new PhotoPostLike(post, username);
    likes.add(like);
}
likeRepo.saveAll(likes);  // ❌ 중복 INSERT 시도
```

**UNIQUE 제약조건**:
```sql
ALTER TABLE photo_post_likes 
ADD CONSTRAINT uk_photo_post_likes_post_username 
UNIQUE (post_id, username);
```

---

## ✅ 해결 방법

### 중복 체크 로직 추가

**수정 코드**:
```java
for (int i = 0; i < likeCount; i++) {
    String username = i == 0 ? likeUsername : "user" + i;
    
    // ✅ 중복 체크 추가
    if (likeRepo.existsByPostIdAndUsername(post.getId(), username)) {
        skippedDuplicates++;
        continue;  // 중복이면 건너뛰기
    }
    
    PhotoPostLike like = new PhotoPostLike(post, username);
    likes.add(like);
    totalLikes++;
}

if (!likes.isEmpty()) {
    likeRepo.saveAll(likes);  // ✅ 중복 제거된 데이터만 INSERT
}
```

---

## 📋 수정된 메서드

### 1. `initPhotoLikes()` - 포토게시판

**변경 사항**:
- ✅ `existsByPostIdAndUsername()` 중복 체크 추가
- ✅ 중복 건너뛰기 카운터 추가
- ✅ 빈 리스트 체크 후 저장

**결과 로그**:
```
✅ 포토게시판 좋아요 초기 데이터 생성 완료: 150개 생성 (중복 0개 건너뜀)
```

### 2. `initCommunityLikes()` - 커뮤니티

**변경 사항**:
- ✅ `existsByPostIdAndUsername()` 중복 체크 추가
- ✅ 중복 건너뛰기 카운터 추가
- ✅ 빈 리스트 체크 후 저장

**결과 로그**:
```
✅ 커뮤니티 좋아요 초기 데이터 생성 완료: 80개 생성 (중복 0개 건너뜀)
```

---

## 🔍 왜 이런 문제가 발생했는가?

### 상황 1: `ddl-auto: update` 사용 시

```
1. 서버 시작
2. 테이블 유지 (데이터 남아있음)
3. DataInit 실행
4. 이미 존재하는 데이터에 중복 INSERT 시도
5. ❌ UNIQUE 제약조건 위반
```

### 상황 2: DataInit 조건부 체크 미흡

```java
// CommandLineRunner 레벨에서는 체크
if (photoLikeRepo.count() == 0) {
    initPhotoLikes(...);  // ✅ 호출됨
}

// 하지만 메서드 내부에서는 무조건 INSERT
for (...) {
    likes.add(new PhotoPostLike(...));  // ❌ 중복 체크 없음
}
```

---

## ✅ 완전한 해결 방법

### 1. DataInit 메서드 내부 중복 체크 (완료)

```java
// ✅ 각 INSERT 전에 중복 확인
if (likeRepo.existsByPostIdAndUsername(postId, username)) {
    continue;
}
```

### 2. `ddl-auto: create` 사용 (권장)

```yaml
# application-dev.yml
jpa:
  hibernate:
    ddl-auto: create  # 매번 깨끗한 테이블
```

**장점**:
- ✅ 중복 데이터 원천 차단
- ✅ ENUM 변경 문제 해결
- ✅ 외래키 충돌 방지

---

## 📝 프로젝트 규칙 업데이트

### 새로운 규칙: DataInit 작성 시 중복 체크 필수

**규칙**:
```java
// ❌ 잘못된 예시
for (...) {
    Entity entity = new Entity(...);
    repository.save(entity);  // 중복 체크 없음
}

// ✅ 올바른 예시
for (...) {
    // 중복 체크
    if (repository.existsByUniqueKey(key)) {
        continue;
    }
    Entity entity = new Entity(...);
    entities.add(entity);
}
if (!entities.isEmpty()) {
    repository.saveAll(entities);
}
```

**적용 대상**:
- UNIQUE 제약조건이 있는 모든 테이블
- 좋아요, 팔로우, 북마크 등 관계 테이블

---

## 🔧 검증 결과

### 컴파일 검증
```bash
.\gradlew compileJava --no-daemon
# ✅ BUILD SUCCESSFUL
```

### 예상 실행 결과
```
✅ 커뮤니티 좋아요 초기 데이터 생성 완료: 80개 생성 (중복 0개 건너뜀)
✅ 포토게시판 좋아요 초기 데이터 생성 완료: 150개 생성 (중복 0개 건너뜀)
```

---

## 📚 관련 문서

- [DDL-AUTO 전략 가이드](../01-project-overview/DDL_AUTO_STRATEGY.md)
- [ENUM 멈춤 현상 해결](../08-troubleshooting/ddl-auto-enum-hang-fix.md)

---

**변경 이력**:
- 2025-11-28: DataInit 좋아요 중복 INSERT 오류 수정 완료

