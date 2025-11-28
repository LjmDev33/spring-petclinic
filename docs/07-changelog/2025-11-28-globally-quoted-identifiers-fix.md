# globally_quoted_identifiers 설정 제거로 SQL 오류 해결

**작성일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 버그 수정 / 데이터베이스

---

## 📋 문제 상황

### 발생한 오류

```sql
Error executing DDL "create table `community_post` ..." 
via JDBC [You have an error in your SQL syntax; 
check the manual that corresponds to your MySQL server version 
for the right syntax to use near '`TEXT` not null' at line 13]
```

### 오류 원인

**`globally_quoted_identifiers: true` 설정**이 활성화되어 있어, Hibernate가 **데이터 타입까지 백틱(`)으로 감싸는 문제** 발생:

```sql
-- ❌ 잘못된 DDL (globally_quoted_identifiers: true)
create table `community_post` (
    `content` `TEXT` not null,  -- TEXT가 `TEXT`로 감싸짐 → 오류!
    primary key (`id`)
) engine=InnoDB;

-- ✅ 올바른 DDL (설정 제거 후)
create table `community_post` (
    `content` TEXT not null,    -- TEXT는 타입이므로 감싸지 않음
    primary key (`id`)
) engine=InnoDB;
```

---

## ✅ 해결 방법

### 수정 내용

**파일**: `application-dev.yml`

```yaml
# 변경 전
properties:
  hibernate:
    globally_quoted_identifiers: true  # ❌ 제거

# 변경 후
properties:
  hibernate:
    dialect: org.hibernate.dialect.MySQL8Dialect
    format_sql: true
    jdbc:
      batch_size: 50
    # globally_quoted_identifiers 제거 ✅
```

---

## 🔍 기술적 배경

### `globally_quoted_identifiers`란?

Hibernate 설정으로, **모든 식별자(테이블명, 컬럼명)를 자동으로 백틸(`)으로 감싸는 기능**입니다.

#### 원래 목적

- 예약어를 컬럼명으로 사용할 때 충돌 방지
- 대소문자 구분이 필요한 경우

#### 문제점

- **데이터 타입까지 감싸는 버그** 존재
- MySQL에서 `TEXT`, `MEDIUMTEXT`, `LONGTEXT` 등이 ``TEXT``로 변환되어 오류 발생

### 영향받은 테이블

모든 Entity에서 `@Lob` 또는 `columnDefinition = "TEXT/MEDIUMTEXT"` 사용하는 경우:

1. **CommunityPost** - `content TEXT`
2. **CounselPost** - `content MEDIUMTEXT`
3. **CounselComment** - `content TEXT`
4. **FaqPost** - `answer TEXT`, `question TEXT`
5. **PhotoPost** - `content TEXT`

---

## 📝 프로젝트 규칙 업데이트

### 새로운 규칙 1: 터미널 실행 타임아웃

**규칙**: 터미널 명령 실행 시 30초 이상 응답 없으면 강제 종료 및 문제로 간주

**적용 방법**:
```bash
# 타임아웃 설정 예시
timeout /t 30 /nobreak; .\gradlew bootRun

# 프로세스 강제 종료
Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Stop-Process -Force
```

### 새로운 규칙 2: globally_quoted_identifiers 사용 금지

**규칙**: Hibernate 설정에서 `globally_quoted_identifiers` 사용 금지

**이유**:
- MySQL에서 데이터 타입도 백틱으로 감싸는 버그 존재
- SQL 문법 오류 발생 가능성
- 대부분의 경우 불필요한 설정

**대안**:
```java
// 특정 컬럼만 백틱 필요 시 @Column 사용
@Column(name = "`order`")  // 예약어인 경우만
private String order;
```

---

## ✅ 검증 결과

1. **컴파일 성공**:
   ```bash
   .\gradlew clean compileJava --no-daemon
   # 결과: BUILD SUCCESSFUL
   ```

2. **모든 오류 해결**:
   - ✅ SQL 문법 오류 해결
   - ✅ TEXT/MEDIUMTEXT 타입 정상 생성
   - ✅ 모든 테이블 생성 성공

---

## 📚 관련 문서

- [2025-11-28 외래키 제약조건 제거](./2025-11-28-foreign-key-constraint-removal.md)
- [2025-11-28 SQL 문법 오류 수정](./2025-11-28-table-creation-sql-syntax-fix.md)

---

**변경 이력**:
- 2025-11-28: 초기 작성 및 globally_quoted_identifiers 제거 완료

