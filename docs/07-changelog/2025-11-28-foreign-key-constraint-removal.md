# 외래키 제약조건 제거로 테이블 생성 순서 문제 해결

**작성일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 버그 수정 / 데이터베이스

---

## 📋 문제 상황

### 발생한 오류

애플리케이션 실행 시(`ddl-auto: create`) 외래키 참조 순서 문제로 다음과 같은 오류 발생:

```
1. Caused by: java.sql.SQLSyntaxErrorException: 
   Table 'petclinic.community_post_attachment' doesn't exist

2. Caused by: java.sql.SQLSyntaxErrorException: 
   Table 'petclinic.community_post_likes' doesn't exist

3. Caused by: java.sql.SQLSyntaxErrorException: 
   Table 'petclinic.counsel_comment' doesn't exist

4. Caused by: java.sql.SQLSyntaxErrorException: 
   Table 'petclinic.counsel_post_attachments' doesn't exist
```

### 원인 분석

**1. 테이블 생성 순서 문제**

Hibernate가 테이블을 생성할 때, 외래키가 참조하는 테이블이 아직 생성되지 않은 상태에서 외래키 제약조건을 생성하려고 시도:

```
예시:
1. counsel_post_likes 테이블 생성 시도
   → counsel_post 테이블 참조 (외래키)
   → counsel_post 테이블이 아직 없음 → 실패!

2. community_post_attachment 테이블 생성 시도
   → community_post와 attachment 테이블 참조
   → 테이블이 아직 없음 → 실패!
```

**2. 복잡한 연관관계**

```
테이블 의존 관계:
- counsel_post_likes → counsel_post
- community_post_likes → community_post
- photo_post_likes → photo_post
- counsel_post_attachments → counsel_post, attachment
- community_post_attachment → community_post, attachment
- photo_post_attachment → photo_post, attachment
- counsel_comment → counsel_post, counsel_comment (self-reference)
- counsel_comment_attachment → counsel_comment, attachment
```

**3. 개발 환경 URL 설정 불충분**

`application-dev.yml`에 `FOREIGN_KEY_CHECKS=0` 설정이 있었지만, Hibernate가 DDL 생성 시 외래키 제약조건을 명시적으로 추가하고 있었음.

---

## ✅ 해결 방법

### 1. **모든 Entity에서 외래키 제약조건 제거**

**해결 방식**: `@JoinColumn`에 `foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)` 속성 추가

#### 수정된 Entity 목록 (총 9개)

1. **CounselPostAttachment.java**
2. **CounselCommentAttachment.java**
3. **CounselComment.java**
4. **CounselPostLike.java**
5. **CommunityPostAttachment.java**
6. **CommunityPostLike.java**
7. **PhotoPostAttachment.java**
8. **PhotoPostLike.java**

#### 수정 예시

**변경 전**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "post_id", nullable = false)
private CounselPost post;
```

**변경 후**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
private CounselPost post;
```

### 2. **Import 충돌 해결**

Hibernate와 Jakarta Persistence의 `ForeignKey` 클래스 충돌 해결:

```java
// 명시적 import 추가
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;
```

### 3. **Hibernate 설정 보완**

`application-dev.yml`:
```yaml
jpa:
  properties:
    hibernate:
      hbm2ddl:
        auto: create
  hibernate:
    ddl-auto: create
    use-new-id-generator-mappings: true
datasource:
  url: jdbc:mysql://localhost:3306/petclinic?sessionVariables=FOREIGN_KEY_CHECKS=0
```

---

## 🔍 기술적 배경

### `ConstraintMode.NO_CONSTRAINT`란?

JPA 2.1부터 지원하는 기능으로, **외래키 제약조건을 DB에 생성하지 않도록** 지시하는 옵션입니다.

| 옵션 | 설명 | 외래키 생성 |
|------|------|------------|
| **NO_CONSTRAINT** | 외래키 제약조건 생성 안 함 | ❌ 생성 안 함 |
| **CONSTRAINT** | 외래키 제약조건 생성 | ✅ 생성 |
| **PROVIDER_DEFAULT** | JPA 구현체 기본값 (보통 생성) | ✅ 생성 |

### 장단점

#### 장점 ✅

1. **테이블 생성 순서 무관**
   - Hibernate가 임의 순서로 테이블을 생성해도 오류 없음
   - 복잡한 순환 참조도 문제없이 처리

2. **개발 속도 향상**
   - `ddl-auto: create` 사용 시 빠른 스키마 재생성
   - 외래키 제약조건 생성/삭제 오버헤드 제거

3. **유연한 테이블 관리**
   - 테이블 DROP 시 순서 고민 불필요
   - 대량 데이터 삽입 시 성능 향상

#### 단점 ⚠️

1. **데이터 무결성 보장 약화**
   - DB 레벨에서 참조 무결성 검증 안 됨
   - 잘못된 외래키 값 삽입 가능

2. **CASCADE 동작 안 됨**
   - `ON DELETE CASCADE` 등 DB 레벨 CASCADE 불가
   - 애플리케이션 코드에서 명시적 처리 필요

3. **운영 환경 부적합**
   - 외래키가 없으면 쿼리 성능 최적화 어려움
   - 데이터 정합성 문제 발생 가능

### 권장 사용 시나리오

| 환경 | 외래키 제약조건 | 설명 |
|------|---------------|------|
| **개발 환경** | ❌ NO_CONSTRAINT | 빠른 스키마 재생성 필요 |
| **테스트 환경** | ❌ NO_CONSTRAINT | 테스트 데이터 자유롭게 생성 |
| **운영 환경** | ✅ CONSTRAINT | 데이터 무결성 보장 필수 |

---

## 📝 영향 범위

### 수정된 Entity 및 테이블

| Entity 클래스 | 테이블명 | 외래키 제거 개수 |
|--------------|---------|----------------|
| CounselPostAttachment | counsel_post_attachments | 2 |
| CounselCommentAttachment | counsel_comment_attachment | 2 |
| CounselComment | counsel_comment | 2 (post_id, parent_id) |
| CounselPostLike | counsel_post_likes | 1 |
| CommunityPostAttachment | community_post_attachment | 2 |
| CommunityPostLike | community_post_likes | 1 |
| PhotoPostAttachment | photo_post_attachment | 2 |
| PhotoPostLike | photo_post_likes | 1 |

**총 제거된 외래키 제약조건**: 13개

---

## ✅ 검증 방법

### 1. 컴파일 확인

```bash
.\gradlew clean compileJava --no-daemon
# 결과: BUILD SUCCESSFUL
```

### 2. 애플리케이션 실행

```bash
.\gradlew bootRun
```

**확인 사항**:
- ✅ 모든 테이블이 순서 문제 없이 생성되는지
- ✅ 외래키 제약조건 없이 테이블이 생성되는지
- ✅ DataInit에 의해 초기 데이터가 정상 삽입되는지

### 3. 생성된 DDL 확인

Hibernate 로그에서 확인:

```sql
-- ✅ 수정 후 (정상)
create table counsel_post_likes (
    id bigint not null auto_increment,
    post_id bigint not null,
    username varchar(50) not null,
    created_at datetime(6) not null,
    primary key (id)
) engine=InnoDB;
-- 외래키 제약조건 없음!

-- ❌ 수정 전 (오류)
create table counsel_post_likes (
    ...
    constraint FKxxx foreign key (post_id) references counsel_post (id)
) engine=InnoDB;
-- counsel_post가 아직 생성 안 됨 → 오류!
```

### 4. 테이블 생성 확인

MySQL에서 확인:

```sql
-- 외래키 제약조건 확인
SELECT 
    TABLE_NAME, 
    CONSTRAINT_NAME, 
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'petclinic'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 결과: 0 rows (외래키 제약조건 없음 확인)
```

---

## 🚀 향후 개선 사항

### 운영 환경 전환 시 고려사항

**1. 외래키 제약조건 복원**

운영 환경에서는 외래키 제약조건을 다시 활성화하는 것을 권장:

```yaml
# application-prod.yml
jpa:
  hibernate:
    ddl-auto: validate  # 운영: 검증만 수행
datasource:
  url: jdbc:mysql://localhost:3306/petclinic  # FOREIGN_KEY_CHECKS 제거
```

**2. 마이그레이션 스크립트 작성**

Flyway 또는 Liquibase를 사용하여 외래키 제약조건 추가:

```sql
-- V2__add_foreign_keys.sql
ALTER TABLE counsel_post_likes
ADD CONSTRAINT fk_counsel_post_likes_post
FOREIGN KEY (post_id) REFERENCES counsel_post(id)
ON DELETE CASCADE;

ALTER TABLE community_post_likes
ADD CONSTRAINT fk_community_post_likes_post
FOREIGN KEY (post_id) REFERENCES community_post(id)
ON DELETE CASCADE;

-- ... 나머지 외래키 추가
```

**3. 환경별 설정 분리**

```java
// Entity에서 환경별로 외래키 설정 다르게 적용 (선택사항)
@Profile("dev")
@Configuration
public class DevDatabaseConfig {
    // NO_CONSTRAINT 사용
}

@Profile("prod")
@Configuration
public class ProdDatabaseConfig {
    // CONSTRAINT 사용
}
```

---

## 📚 관련 문서

- [TABLE_DEFINITION.md](../03-database/TABLE_DEFINITION.md) - 테이블 정의서
- [ARCHITECTURE.md](../02-architecture/ARCHITECTURE.md) - 프로젝트 아키텍처
- [2025-11-28 SQL 문법 오류 수정](./2025-11-28-table-creation-sql-syntax-fix.md)

---

## 📌 프로젝트 규칙 업데이트

### 새로운 규칙: 외래키 제약조건 관리

**개발 환경**:
- ✅ `@JoinColumn`에 `foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)` 사용
- ✅ 테이블 생성 순서 문제 방지
- ✅ 빠른 스키마 재생성 지원

**운영 환경**:
- ⚠️ 외래키 제약조건 복원 권장
- ⚠️ 마이그레이션 도구(Flyway/Liquibase) 사용
- ⚠️ 데이터 무결성 보장 필수

---

**변경 이력**:
- 2025-11-28: 초기 작성 및 외래키 제약조건 제거 완료

