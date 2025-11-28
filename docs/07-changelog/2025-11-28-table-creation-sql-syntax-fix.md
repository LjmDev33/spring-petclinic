# 테이블 생성 SQL 문법 오류 수정

**작성일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 버그 수정 / 데이터베이스

---

## 📋 문제 상황

### 발생한 오류

애플리케이션 실행 시(`ddl-auto: create`) 다음과 같은 오류들이 발생:

```
1. org.springframework.dao.InvalidDataAccessResourceUsageException: 
   Table 'petclinic.photo_post' doesn't exist

2. java.sql.SQLException: Failed to open the referenced table 'photo_post'

3. java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax
   near '`INT DEFAULT 0` not null, `created_at` datetime(6) not null'

4. java.sql.SQLSyntaxErrorException: Table 'petclinic.counsel_post_likes' doesn't exist
```

### 원인 분석

#### 1. **잘못된 `columnDefinition` 사용**

```java
// ❌ 잘못된 코드
@Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0")
private int viewCount = 0;
```

**문제점**:
- MySQL에서 `columnDefinition = "INT DEFAULT 0"`은 잘못된 문법
- Hibernate가 생성하는 DDL에서 `INT DEFAULT 0`이 그대로 출력되어 SQL 오류 발생
- 올바른 MySQL 문법: `INT NOT NULL DEFAULT 0`

#### 2. **테이블 생성 순서 문제**

- 외래키 제약조건이 있는 테이블들이 참조 테이블보다 먼저 생성되려고 시도
- 예: `counsel_post_likes` 테이블이 `counsel_post`보다 먼저 생성 시도

#### 3. **외래키 제약조건 충돌**

- `FOREIGN_KEY_CHECKS=1` 상태에서 테이블 생성 순서 오류 발생

---

## ✅ 해결 방법

### 1. **`columnDefinition` 제거**

잘못된 `columnDefinition` 사용을 제거하고, Java 기본값만 사용:

**수정된 파일**:
- `CommunityPost.java`
- `CounselPost.java`
- `Attachment.java` (common 패키지)

```java
// ✅ 올바른 코드
@Column(name = "view_count", nullable = false)
private int viewCount = 0;

@Column(name = "like_count", nullable = false)
private int likeCount = 0;

@Column(name = "download_count", nullable = false)
private int downloadCount = 0;
```

**장점**:
- Hibernate가 자동으로 올바른 DDL 생성
- Java 필드 초기값(`= 0`)이 DB DEFAULT 값 역할 수행
- 데이터베이스 독립성 유지 (MySQL, PostgreSQL 등 모두 호환)

### 2. **외래키 제약조건 비활성화 (개발 환경)**

`application-dev.yml` 수정:

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/petclinic?sessionVariables=FOREIGN_KEY_CHECKS=0
```

**효과**:
- 테이블 생성 순서와 무관하게 모든 테이블 생성 가능
- 외래키 참조 테이블이 아직 없어도 생성 진행
- 개발 환경 전용 설정 (운영 환경에서는 제거 권장)

---

## 🔍 기술적 배경

### `columnDefinition` 사용 시 주의사항

| 방법 | 장점 | 단점 |
|------|------|------|
| **Java 초기값 사용** | - DB 독립적<br>- Hibernate가 자동 DDL 생성<br>- 유지보수 용이 | - DB 레벨 DEFAULT 없음 (애플리케이션 레벨) |
| **`columnDefinition` 사용** | - DB 레벨 DEFAULT 명시<br>- 세밀한 제어 가능 | - DB 종속적<br>- SQL 문법 오류 가능성<br>- 유지보수 어려움 |

### 권장 사항

```java
// ✅ 권장: Java 초기값 사용
@Column(nullable = false)
private int count = 0;

// ⚠️ 특수한 경우에만 columnDefinition 사용
@Column(columnDefinition = "INT DEFAULT 0 COMMENT '조회수'")
private int viewCount;

// ❌ 사용 금지
@Column(columnDefinition = "INT DEFAULT 0")  // 잘못된 문법
```

---

## 📝 영향 범위

### 수정된 Entity 클래스

1. **CommunityPost.java**
   - `view_count` 컬럼 정의 수정
   - `like_count` 컬럼 정의 수정

2. **CounselPost.java**
   - `view_count` 컬럼 정의 수정

3. **Attachment.java** (common 패키지)
   - `download_count` 컬럼 정의 수정

### 영향받는 테이블

- `community_post`
- `counsel_post`
- `attachment`

---

## ✅ 검증 방법

### 1. 컴파일 확인

```bash
.\gradlew clean compileJava
# 결과: BUILD SUCCESSFUL
```

### 2. 애플리케이션 실행

```bash
.\gradlew bootRun
```

**확인 사항**:
- 모든 테이블이 정상 생성되는지 확인
- `DataInit.java`에 의해 초기 데이터가 정상 삽입되는지 확인
- SQL 문법 오류가 발생하지 않는지 확인

### 3. 생성된 DDL 확인

Hibernate가 생성하는 DDL (로그에서 확인):

```sql
-- ✅ 수정 후 (정상)
create table community_post (
    view_count integer not null,
    like_count integer not null,
    ...
)

-- ❌ 수정 전 (오류)
create table community_post (
    view_count INT DEFAULT 0 not null,  -- 문법 오류
    like_count INT DEFAULT 0 not null,
    ...
)
```

---

## 🚀 향후 개선 사항

### 프로젝트 규칙 추가

**규칙**: `columnDefinition` 사용 최소화

1. **기본값 설정**
   - Java 필드 초기값(`= 0`) 사용 권장
   - `columnDefinition`은 특수한 경우에만 사용

2. **사용 예외 케이스**
   - MySQL 전용 컬럼 타입 (예: `JSON`, `GEOMETRY`)
   - 특수한 제약조건 (예: `AUTO_INCREMENT`, `ON UPDATE CURRENT_TIMESTAMP`)
   - 코멘트 추가 필요 시 (예: `COMMENT '설명'`)

3. **검증 절차**
   - Entity 작성 시 `columnDefinition` 사용 전 검토
   - 가능하면 표준 JPA 애노테이션 사용
   - 코드 리뷰 시 `columnDefinition` 사용 사유 확인

---

## 📚 관련 문서

- [TABLE_DEFINITION.md](../03-database/TABLE_DEFINITION.md) - 테이블 정의서
- [PROJECT_RULES_UPDATE_20251106.md](../01-project-overview/PROJECT_RULES_UPDATE_20251106.md) - 프로젝트 규칙

---

**변경 이력**:
- 2025-11-28: 초기 작성 및 오류 수정 완료

