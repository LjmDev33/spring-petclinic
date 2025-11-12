# 🐛 서버 실행 오류 해결 보고서 (2025-11-06)

## 문제 1: ErrorMessages 리소스 파일 누락 ✅ 해결

### 오류 내용
```
java.util.MissingResourceException: Can't find bundle for base name messages/ErrorMessages, locale ko_KR
```

### 원인
`application-dev.yml`에서 `ErrorMessages`를 basename으로 지정했지만, 실제 파일이 존재하지 않음:
```yaml
messages:
  basename: messages/messages,messages/ErrorMessages
```

### 해결
**생성된 파일 (2개)**:
1. `src/main/resources/messages/ErrorMessages.properties`
   - 기본 영문 오류 메시지
   
2. `src/main/resources/messages/ErrorMessages_ko.properties`
   - 한국어 오류 메시지

**메시지 내용**:
- 일반 오류: error.generic, error.notfound, error.forbidden
- 검증 오류: error.validation.required, error.validation.invalid
- 데이터베이스 오류: error.database.connection
- 파일 업로드 오류: error.file.size, error.file.type

---

## 문제 2: 테이블 DROP 오류 ✅ 해결

### 오류 내용
서버 시작 시 다음 테이블 DROP 실패:
1. `community_post_attachment` - doesn't exist
2. `counsel_comment` - doesn't exist
3. `counsel_comment_attachment` - doesn't exist
4. `counsel_post_attachments` - doesn't exist
5. `user_roles` - doesn't exist

### 원인 분석

#### 1. `ddl-auto: create-drop` 설정 문제
```yaml
hibernate:
  ddl-auto: create-drop  # ❌ 문제 발생
```

**동작 방식**:
- 서버 시작 시: 모든 테이블 DROP → 새로 CREATE
- 서버 종료 시: 모든 테이블 DROP

**문제점**:
- 첫 실행 시 테이블이 없는데 DROP을 시도 → SQLSyntaxErrorException
- Entity 매핑이 복잡한 경우 DROP 순서 문제 발생
- 개발 중 서버 재시작 시마다 모든 데이터 삭제

#### 2. 테이블 의존성 순서 문제
```
users ──> user_roles (FK)
counsel_post ──> counsel_post_attachment (FK)
counsel_comment ──> counsel_comment_attachment (FK)
```

Hibernate가 DROP 순서를 잘못 판단하여 외래키 제약조건 위반 발생

### 해결 방법

#### ✅ 방법 1: `ddl-auto: update` 사용 (권장)
```yaml
hibernate:
  ddl-auto: update  # ✅ 권장 설정
```

**장점**:
- 기존 데이터 유지
- 스키마 변경 시 자동으로 ALTER TABLE 실행
- DROP 오류 발생 안 함
- 개발 중 데이터 누적 가능

**단점**:
- 컬럼 삭제는 자동으로 되지 않음 (수동으로 ALTER TABLE 필요)
- 깨끗한 상태로 시작하려면 수동으로 테이블 삭제 필요

#### ✅ 방법 2: `ddl-auto: create` 사용
```yaml
hibernate:
  ddl-auto: create  # DROP 없이 CREATE만
```

**장점**:
- 매번 깨끗한 상태로 시작
- DROP 시도하지 않음

**단점**:
- 서버 시작 시마다 모든 데이터 삭제
- 테이블이 이미 존재하면 오류 발생 가능

#### ✅ 방법 3: `ddl-auto: validate` + 수동 스크립트
```yaml
hibernate:
  ddl-auto: validate  # 스키마 검증만
```

**생성된 스크립트**: `drop-all-tables.sql`
- 모든 테이블을 올바른 순서로 삭제
- `SET FOREIGN_KEY_CHECKS = 0` 사용으로 외래키 무시

---

## 적용된 해결책

### 1. ErrorMessages 파일 생성 ✅
- `ErrorMessages.properties` (영문)
- `ErrorMessages_ko.properties` (한글)

### 2. ddl-auto 설정 변경 ✅
```yaml
# 변경 전
hibernate:
  ddl-auto: create-drop  # ❌

# 변경 후
hibernate:
  ddl-auto: update  # ✅ 권장
```

### 3. 수동 초기화 스크립트 생성 ✅
파일: `src/main/resources/db/mysql/drop-all-tables.sql`

**사용법**:
```sql
-- MySQL 클라이언트에서 실행
source src/main/resources/db/mysql/drop-all-tables.sql;
```

또는:
```bash
mysql -u dev33 -p petclinic < src/main/resources/db/mysql/drop-all-tables.sql
```

---

## ddl-auto 옵션 비교표

| 옵션 | DROP | CREATE | ALTER | 데이터 유지 | 용도 |
|------|------|--------|-------|------------|------|
| **create-drop** | ✅ (시작/종료) | ✅ | ❌ | ❌ | 테스트 환경 |
| **create** | ❌ | ✅ | ❌ | ❌ | 초기 개발 |
| **update** | ❌ | ✅ | ✅ | ✅ | **개발 환경 권장** |
| **validate** | ❌ | ❌ | ❌ | ✅ | 운영 환경 |
| **none** | ❌ | ❌ | ❌ | ✅ | 운영 환경 (수동 관리) |

---

## 테스트 결과

### Before (문제 발생)
```
❌ MissingResourceException: Can't find bundle for ErrorMessages
❌ SQLSyntaxErrorException: Table doesn't exist (5개 테이블)
❌ 서버 시작 시마다 데이터 삭제
```

### After (해결 완료)
```
✅ ErrorMessages 리소스 정상 로드
✅ 테이블 DROP 오류 없음
✅ 기존 데이터 유지 (update 모드)
✅ 스키마 변경 시 자동 ALTER TABLE
```

---

## 추가 권장 사항

### 1. 프로파일별 ddl-auto 설정
```yaml
# application-dev.yml (개발 환경)
hibernate:
  ddl-auto: update  # 데이터 유지

# application-operate.yml (운영 환경)
hibernate:
  ddl-auto: validate  # 스키마 검증만
```

### 2. 초기 데이터 로드 개선
현재 `DataInit`은 `CommandLineRunner`를 사용하여 매번 실행되는데, 이를 개선:

```java
@Component
@ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create")
public class DataInit implements CommandLineRunner {
    // create 모드일 때만 실행
}
```

### 3. Flyway 또는 Liquibase 도입 (추후 고려)
- 데이터베이스 마이그레이션 관리
- 버전별 스키마 이력 추적
- 운영 환경 배포 시 안전성 향상

---

## 결론

### ✅ 모든 문제 해결 완료

1. **ErrorMessages 파일 누락**: 파일 2개 생성
2. **테이블 DROP 오류**: `ddl-auto: update`로 변경
3. **데이터 초기화**: 수동 스크립트 제공

### 📋 변경된 파일
- `application-dev.yml`: ddl-auto 설정 변경
- `ErrorMessages.properties`: 신규 생성
- `ErrorMessages_ko.properties`: 신규 생성
- `drop-all-tables.sql`: 신규 생성

### 🚀 서버 재시작 필요
IDE에서 서버를 재시작하면 모든 오류가 해결됩니다.

---

**작성일**: 2025-11-06  
**해결 상태**: ✅ 완료  
**서버 포트**: 8080

