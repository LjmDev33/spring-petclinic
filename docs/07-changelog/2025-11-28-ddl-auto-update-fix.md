# DDL-AUTO 설정 최적화 및 외래키 제약조건 복원

**작성일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 버그 수정 / 실무 표준 적용

---

## 📋 문제 상황

### 발생한 문제

애플리케이션 실행 시 외래키 DROP 작업에서 무한 대기 발생:

```
Hibernate: alter table community_post_attachment drop foreign key FK57m40mq145cwgpsohwdcb9do3
Hibernate: alter table counsel_comment drop foreign key FK7hclqhf76hs23vlp06g4rrl5q
...
종료 코드 -1(으)로 완료된 프로세스
```

### 근본 원인

1. **`ddl-auto: create` 모드의 문제점**
   - 매번 모든 테이블을 DROP하고 재생성
   - 외래키가 있는 테이블들을 순서 없이 DROP 시도
   - 참조 테이블이 이미 삭제되어 외래키 DROP 실패
   - MySQL 락 대기로 인한 무한 대기 상태

2. **실무 환경 부적합**
   - `create` 모드는 개발 초기에만 사용
   - 데이터 유지가 필요한 개발 환경에서는 부적합
   - 매번 초기 데이터 재생성으로 개발 속도 저하

---

## ✅ 해결 방법

### 1. `ddl-auto` 설정 변경

**실무 표준에 맞게 `create` → `update`로 변경**

```yaml
# 변경 전 (문제 발생)
jpa:
  hibernate:
    ddl-auto: create  # 매번 DROP & CREATE

# 변경 후 (실무 표준)
jpa:
  hibernate:
    ddl-auto: update  # 스키마만 업데이트, 데이터 유지
```

### 2. 외래키 제약조건 복원

**NO_CONSTRAINT 제거하고 실무 표준 외래키 사용**

- 총 13개 Entity에서 `foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)` 제거
- 표준 JPA 외래키 제약조건 사용

**변경된 Entity**:
- CounselPostAttachment
- CounselCommentAttachment
- CounselComment
- CounselPostLike
- CommunityPostAttachment
- CommunityPostLike
- PhotoPostAttachment
- PhotoPostLike

---

## 🔍 DDL-AUTO 모드별 특징

| 모드 | 용도 | 장점 | 단점 |
|------|------|------|------|
| **create** | 개발 초기 | 깨끗한 스키마 생성 | 데이터 손실, 외래키 DROP 오류 |
| **create-drop** | 테스트 | 테스트 후 자동 정리 | 데이터 보존 불가 |
| **update** | 개발 중 | 데이터 유지, 스키마 자동 변경 | ✅ **실무 권장** |
| **validate** | 운영 | 안전성 최고 | 스키마 변경 불가 |
| **none** | 운영 | 수동 제어 | DDL 자동화 없음 |

---

## 📝 실무 표준 정리

### 환경별 권장 설정

```yaml
# 개발 환경 (application-dev.yml)
spring:
  jpa:
    hibernate:
      ddl-auto: update  ✅ 데이터 유지하며 스키마 변경
    show-sql: true

# 운영 환경 (application-prod.yml)
spring:
  jpa:
    hibernate:
      ddl-auto: validate  ✅ 스키마 검증만
    show-sql: false
```

### 외래키 제약조건 사용

```java
// ✅ 실무 표준 (권장)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "post_id", nullable = false)
private Post post;

// ❌ NO_CONSTRAINT (개발용, 비권장)
@JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
```

---

## ✅ 검증 결과

### 변경 후 효과

1. **외래키 DROP 오류 해결**
   - `update` 모드는 DROP 작업 없음
   - 기존 테이블 유지하며 컬럼만 추가/변경

2. **개발 효율성 향상**
   - 데이터 유지로 매번 초기화 불필요
   - 애플리케이션 재시작 시간 단축

3. **실무 표준 준수**
   - 외래키 제약조건으로 데이터 무결성 보장
   - 운영 환경과 유사한 개발 환경 구축

---

## 🚀 프로젝트 규칙 업데이트

### 새로운 규칙: DDL-AUTO 설정

**개발 환경**:
- ✅ `ddl-auto: update` 사용 (데이터 유지)
- ✅ 외래키 제약조건 사용 (실무 표준)
- ✅ 데이터 초기화는 DataInit으로 관리

**운영 환경**:
- ✅ `ddl-auto: validate` 사용 (검증만)
- ✅ 스키마 변경은 Flyway/Liquibase로 관리
- ✅ 수동 마이그레이션 스크립트 사용

### 테이블 재생성이 필요한 경우

```bash
# 1. DBeaver에서 수동 DROP
DROP TABLE IF EXISTS counsel_post_likes;
DROP TABLE IF EXISTS counsel_comment_attachment;
# ... (자식 테이블부터 순서대로)

# 2. 애플리케이션 실행
# update 모드가 자동으로 테이블 생성

# 3. DataInit이 초기 데이터 자동 생성
```

---

## 📚 관련 문서

- [2025-11-28 외래키 제약조건 제거](./2025-11-28-foreign-key-constraint-removal.md)
- [2025-11-28 globally_quoted_identifiers 수정](./2025-11-28-globally-quoted-identifiers-fix.md)

---

**변경 이력**:
- 2025-11-28: ddl-auto를 create에서 update로 변경, 외래키 제약조건 복원 완료

