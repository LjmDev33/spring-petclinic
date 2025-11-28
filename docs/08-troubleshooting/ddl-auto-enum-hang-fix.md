# DDL-AUTO UPDATE 모드에서 ENUM 변경 시 멈춤 현상 해결

**발생일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 문제 해결 / Hibernate

---

## 🐛 문제 상황

### 발생한 오류

```
2025-11-28T13:49:04.897+09:00  INFO 23096 --- [petclinic] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available
Hibernate: alter table counsel_post modify column status enum ('COMPLETE','END','WAIT') not null
← 여기서 멈춤 (무한 대기)
```

### 증상
- 애플리케이션 시작 시 Hibernate DDL 실행 중 멈춤
- `counsel_post` 테이블의 `status` ENUM 컬럼 변경 중 멈춤
- 로그 출력 중단, 서버 시작 실패

---

## 🔍 원인 분석

### 1. `ddl-auto: update` 모드의 ENUM 변경 문제

**문제점**:
```sql
-- Hibernate가 실행하려는 DDL
ALTER TABLE counsel_post 
MODIFY COLUMN status ENUM('COMPLETE','END','WAIT') NOT NULL;
```

**왜 멈추는가?**:
1. **테이블 재구성 (Table Rebuild)**
   - MySQL에서 ENUM 타입 변경은 테이블 전체를 복사하여 재구성
   - 데이터가 많으면 시간이 오래 걸림
   - 트랜잭션 타임아웃 발생 가능

2. **외래키 제약조건 충돌**
   - `counsel_post`를 참조하는 테이블들:
     - `counsel_post_attachments`
     - `counsel_post_likes`
     - `counsel_comment`
   - ALTER TABLE 실행 중 외래키 락 대기 상태 발생

3. **Hibernate의 부정확한 DDL 감지**
   - 기존 ENUM과 새로운 ENUM이 동일해도 변경 시도
   - 불필요한 ALTER TABLE 실행

---

## ✅ 해결 방법

### 임시 해결: `ddl-auto: create` 모드로 전환

**파일**: `application-dev.yml`

```yaml
# 변경 전
jpa:
  hibernate:
    ddl-auto: update  # ❌ ENUM 변경 시 멈춤

# 변경 후 (임시)
jpa:
  hibernate:
    ddl-auto: create  # ✅ 테이블 재생성
```

**절차**:
1. 멈춘 Java 프로세스 강제 종료
2. `ddl-auto: create`로 변경
3. 애플리케이션 실행 (테이블 재생성 + 데이터 초기화)
4. 정상 실행 확인 후 `ddl-auto: update`로 복구

---

## 🛡️ 근본 해결책

### 1. ENUM 컬럼 사용 자제

**비추천**:
```java
@Enumerated(EnumType.STRING)
@Column(columnDefinition = "ENUM('COMPLETE','END','WAIT')")
private CounselStatus status;
```

**추천**:
```java
@Enumerated(EnumType.STRING)
@Column(length = 20)
private CounselStatus status;  // VARCHAR(20)로 매핑
```

**장점**:
- ALTER TABLE 불필요 (ENUM 값 추가/변경 시)
- DDL 멈춤 현상 없음
- 유연한 확장 가능

### 2. Flyway/Liquibase 도입 (운영 환경)

```yaml
# 운영 환경
jpa:
  hibernate:
    ddl-auto: validate  # DDL 자동 실행 금지
```

```sql
-- Flyway 마이그레이션 스크립트
-- V1__alter_counsel_post_status.sql
ALTER TABLE counsel_post 
MODIFY COLUMN status VARCHAR(20) NOT NULL;
```

### 3. 개발 환경: 테이블 재생성 전략

```yaml
# 개발 환경
jpa:
  hibernate:
    ddl-auto: create  # 개발 시작 시 재생성
    # 또는
    ddl-auto: create-drop  # 종료 시 삭제
```

---

## 📋 체크리스트

**ENUM 변경 시 확인 사항**:
- [ ] `ddl-auto: update` 사용 중인가?
- [ ] ENUM 컬럼을 가진 테이블에 외래키가 있는가?
- [ ] 테이블에 대량의 데이터가 있는가?
- [ ] 트랜잭션 타임아웃 설정이 충분한가?

**1개라도 해당되면** → `ddl-auto: create`로 재생성 권장

---

## 🔄 복구 절차

### 1. 테이블 재생성 완료 후

```yaml
# application-dev.yml 복구
jpa:
  hibernate:
    ddl-auto: update  # create → update로 변경
```

### 2. 데이터 백업 필요 시

```bash
# 백업
mysqldump -u dev33 -p petclinic > backup_$(date +%Y%m%d).sql

# 복구
mysql -u dev33 -p petclinic < backup_20251128.sql
```

---

## 📝 프로젝트 규칙 업데이트

### 새로운 규칙: ENUM 사용 금지

**규칙**: Entity에서 `columnDefinition = "ENUM(...)"` 사용 금지

**이유**:
- DDL 변경 시 테이블 재구성 발생
- `ddl-auto: update` 모드에서 멈춤 현상
- 외래키 충돌 위험

**대안**:
```java
// ❌ 비추천
@Column(columnDefinition = "ENUM('A','B','C')")
private Status status;

// ✅ 추천
@Column(length = 20)
private Status status;  // VARCHAR로 자동 매핑
```

---

## 📚 관련 문서

- [2025-11-28 DDL-AUTO 설정 최적화](../07-changelog/2025-11-28-ddl-auto-update-fix.md)
- [Hibernate DDL 전략](../02-architecture/ARCHITECTURE.md)

---

**변경 이력**:
- 2025-11-28: ENUM 변경 멈춤 현상 문서화 및 해결 방법 정리

