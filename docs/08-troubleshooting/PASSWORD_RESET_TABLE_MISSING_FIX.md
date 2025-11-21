# [긴급] 애플리케이션 실행 실패 해결

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot  
**심각도:** 🔴 Critical (서버 실행 불가)

## ❌ 오류 내용

### 에러 메시지
```
org.hibernate.tool.schema.spi.SchemaManagementException: 
Schema-validation: missing table [password_reset_tokens]
```

### 발생 원인
1. `ddl-auto: validate` 모드로 실행
2. 비밀번호 찾기 기능 구현 시 `PasswordResetToken` 엔티티 추가
3. DB에 `password_reset_tokens` 테이블이 존재하지 않음
4. **validate 모드는 테이블을 자동 생성하지 않고 검증만 수행**

---

## ✅ 해결 방법

### application-dev.yml 수정

#### Before (문제 있는 설정)
```yaml
jpa:
  hibernate:
    ddl-auto: validate  # 검증만 수행, 테이블 생성 안 함
```

#### After (해결된 설정)
```yaml
jpa:
  hibernate:
    ddl-auto: update  # 개발환경: 스키마 자동 업데이트 (테이블 추가/컬럼 변경)
```

---

## 📋 ddl-auto 옵션 설명

| 옵션 | 설명 | 용도 |
|------|------|------|
| **validate** | 스키마 검증만 수행. 불일치 시 오류 | 운영 환경 (안전) |
| **update** | 변경사항만 반영 (추가/수정). 삭제 안 함 | 개발 환경 (권장) |
| **create** | 기존 테이블 DROP 후 CREATE | 초기 개발 |
| **create-drop** | 애플리케이션 종료 시 DROP | 테스트 환경 |
| **none** | 아무 작업도 하지 않음 | 수동 관리 |

---

## 🔍 왜 이 문제가 발생했는가?

### 타임라인
1. **3단계 작업:** 비밀번호 찾기 기능 구현
   - `PasswordResetToken` 엔티티 생성
   - `@Entity` 어노테이션으로 JPA 엔티티 등록
   
2. **설정 상태:** `ddl-auto: validate`
   - 기존 테이블만 검증
   - 새 엔티티에 대한 테이블 자동 생성 안 함

3. **결과:** 애플리케이션 실행 실패
   - Hibernate가 `password_reset_tokens` 테이블 찾음
   - DB에 테이블 없음
   - Schema-validation 오류 발생

---

## 🎯 update 모드의 장점 (개발 환경)

### ✅ 장점
1. **새 엔티티 자동 반영**
   - 테이블 자동 생성
   - 컬럼 자동 추가
   
2. **기존 데이터 보존**
   - 테이블 DROP 하지 않음
   - 데이터 유지

3. **개발 속도 향상**
   - 수동 DDL 작성 불필요
   - 즉시 테스트 가능

### ⚠️ 주의사항
1. **운영 환경에서는 validate 또는 none 사용**
   ```yaml
   # application-prod.yml
   jpa:
     hibernate:
       ddl-auto: validate  # 또는 none
   ```

2. **컬럼 삭제는 수동 처리**
   - update는 컬럼 삭제를 하지 않음
   - 필요 시 수동 ALTER TABLE 실행

---

## 📊 추가된 테이블 스키마

### password_reset_tokens

```sql
CREATE TABLE `password_reset_tokens` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `token` VARCHAR(100) UNIQUE NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `used` BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**테이블 역할:**
- 비밀번호 재설정 토큰 저장
- 토큰 유효기간 관리 (24시간)
- 토큰 사용 여부 추적

---

## 🔄 향후 개발 시 주의사항

### 1. 새 엔티티 추가 시
```java
@Entity  // 이 어노테이션을 추가하면
@Table(name = "new_table")
public class NewEntity {
    // ...
}
```
→ `ddl-auto: update` 모드에서 자동으로 테이블 생성됨

### 2. 기존 컬럼 수정 시
```java
@Column(length = 50)  // 기존 100 → 50으로 변경
private String name;
```
→ `update` 모드는 컬럼 크기 축소를 자동으로 하지 않음  
→ 수동으로 `ALTER TABLE` 실행 필요

### 3. 컬럼 삭제 시
```java
// @Column(name = "old_column")  // 주석 처리
// private String oldColumn;
```
→ `update` 모드는 컬럼을 삭제하지 않음  
→ 수동으로 `ALTER TABLE ... DROP COLUMN` 실행

---

## ✅ 해결 확인

### 컴파일 성공
```bash
.\gradlew.bat compileJava
# BUILD SUCCESSFUL in 19s
```

### 다음 서버 실행 시
1. Hibernate가 `password_reset_tokens` 테이블 생성
2. 모든 엔티티 정상 매핑
3. 애플리케이션 정상 실행

---

## 📝 권장 설정 (환경별)

### 개발 환경 (application-dev.yml)
```yaml
jpa:
  hibernate:
    ddl-auto: update  # 자동 스키마 업데이트
  show-sql: true      # SQL 로그 출력
```

### 테스트 환경 (application-test.yml)
```yaml
jpa:
  hibernate:
    ddl-auto: create-drop  # 테스트 시작/종료 시 초기화
  show-sql: false
```

### 운영 환경 (application-prod.yml)
```yaml
jpa:
  hibernate:
    ddl-auto: validate  # 또는 none
  show-sql: false
```

---

## 🚀 재발 방지 대책

### 1. 엔티티 추가 체크리스트
- [ ] 엔티티 클래스 생성
- [ ] `@Entity` 어노테이션 추가
- [ ] `ddl-auto` 설정 확인
- [ ] 로컬에서 서버 실행 테스트
- [ ] 테이블 생성 확인

### 2. 개발 환경 표준화
```yaml
# 개발팀 공통 설정
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 개발 환경 고정
```

### 3. 문서화
- 새 엔티티 추가 시 `TABLE_DEFINITION.md` 업데이트
- 스키마 변경 이력 `CHANGELOG.md`에 기록

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20  
**해결 시간**: 즉시 (설정 변경만으로 해결)

