# DDL-AUTO 전략 및 ENUM 타입 처리 가이드

**작성일**: 2025-11-28  
**작성자**: Jeongmin Lee  
**카테고리**: 프로젝트 규칙 / Hibernate 전략

---

## 🎯 결론: 환경별 DDL-AUTO 전략

### **개발 환경 (application-dev.yml)**

```yaml
jpa:
  hibernate:
    ddl-auto: create  # 매번 테이블 재생성
```

**이유**:
- ✅ ENUM 타입 변경 문제 회피
- ✅ 외래키 제약조건 충돌 없음
- ✅ DataInit으로 자동 데이터 생성
- ✅ 스키마 변경 자유로움

### **운영 환경 (application-prod.yml)**

```yaml
jpa:
  hibernate:
    ddl-auto: validate  # 검증만, DDL 실행 금지
```

**이유**:
- ✅ 안전성 최우선
- ✅ 수동 마이그레이션 (Flyway/Liquibase)
- ✅ 데이터 유실 방지

---

## 🐛 문제 배경

### ENUM 타입의 구조적 문제

**Entity 정의**:
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private CounselStatus status;
```

**Hibernate의 MySQL DDL 생성**:
```sql
CREATE TABLE counsel_post (
    status ENUM('COMPLETE','END','WAIT') NOT NULL
);
```

**문제점**:
1. **`ddl-auto: update` 시 ENUM 변경 감지**
   ```sql
   ALTER TABLE counsel_post 
   MODIFY COLUMN status ENUM('COMPLETE','END','WAIT') NOT NULL;
   ```

2. **테이블 재구성 (Table Rebuild)**
   - MySQL은 ENUM 변경 시 테이블 전체를 복사하여 재구성
   - 외래키가 있으면 락 대기 → **무한 멈춤**

3. **Hibernate의 불완전한 감지**
   - 실제 ENUM 값이 동일해도 변경으로 감지
   - 매번 ALTER TABLE 시도

---

## ✅ 해결 방안 분석

### 방안 1: `ddl-auto: create` 유지 (채택)

**설정**:
```yaml
# application-dev.yml
jpa:
  hibernate:
    ddl-auto: create
```

**장점**:
| 항목 | 설명 |
|------|------|
| ✅ ENUM 안전 | 매번 DROP → CREATE로 ENUM 변경 문제 없음 |
| ✅ 외래키 안전 | 모든 테이블 재생성으로 순서 문제 없음 |
| ✅ 개발 편의성 | 스키마 자유롭게 변경 가능 |
| ✅ DataInit 활용 | 자동 초기 데이터 생성 |

**단점**:
| 항목 | 설명 | 대응 방안 |
|------|------|-----------|
| ❌ 데이터 초기화 | 서버 재시작 시 데이터 손실 | 개발 환경에선 문제 없음 |

**DataInit 동작**:
```java
@Bean
CommandLineRunner initCommunityData(...) {
    return args -> {
        // count() == 0 체크로 중복 방지
        if (counselPostRepo.count() == 0) {
            // 112개 게시글 생성 (WAIT/COMPLETE/END 랜덤)
            initCounselDataRandom(...);
        }
    };
}
```

---

### 방안 2: `ddl-auto: update` + VARCHAR 변환 (비추천)

**Entity 수정**:
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
private CounselStatus status;
```

**문제점**:
- ❌ Hibernate가 `columnDefinition`을 무시할 수 있음
- ❌ 버전별로 동작이 다름 (불안정)
- ❌ 여전히 ENUM 생성될 가능성 높음

**결론**: ❌ 권장하지 않음

---

### 방안 3: Flyway/Liquibase 도입 (운영 환경)

**개발 환경**:
```yaml
ddl-auto: create  # Hibernate 자동 DDL
```

**운영 환경**:
```yaml
ddl-auto: validate  # DDL 금지
```

**Flyway 마이그레이션 스크립트**:
```sql
-- V1__create_counsel_post.sql
CREATE TABLE counsel_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    ...
);

-- V2__alter_counsel_post_status.sql
ALTER TABLE counsel_post 
MODIFY COLUMN status VARCHAR(20) NOT NULL;
```

**장점**:
- ✅ 운영 환경에서 안전한 스키마 변경
- ✅ 버전 관리 및 롤백 가능
- ✅ 팀 협업 용이

**단점**:
- ❌ 초기 학습 곡선
- ❌ 마이그레이션 스크립트 수동 작성

**결론**: ✅ 운영 환경에서 권장

---

## 📋 환경별 전략 매트릭스

| 환경 | ddl-auto | 데이터 초기화 | ENUM 처리 | 스키마 변경 |
|------|----------|---------------|-----------|-------------|
| **개발 (로컬)** | `create` | DataInit 자동 | ✅ 안전 | 자유 |
| **테스트** | `create-drop` | 테스트 데이터 | ✅ 안전 | 자동 |
| **스테이징** | `validate` | 수동/스크립트 | ⚠️ 주의 | Flyway |
| **운영** | `validate` | ❌ 금지 | ⚠️ 주의 | Flyway |

---

## 🔧 프로젝트 적용 방안

### 현재 상태 (2025-11-28)

**설정**:
```yaml
# application-dev.yml
jpa:
  hibernate:
    ddl-auto: create  # ✅ 개발 환경 최종 설정
```

**DataInit**:
- ✅ 이미 조건부 초기화 구현 완료 (`count() == 0` 체크)
- ✅ 온라인상담: 112개 게시글 + 댓글 자동 생성
- ✅ 공개/비공개, 상태(WAIT/COMPLETE/END) 랜덤 배분
- ✅ 수정 불필요

**Entity**:
```java
// CounselPost.java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private CounselStatus status;  // ✅ 변경 불필요
```

---

## 📝 프로젝트 규칙 업데이트

### 새로운 규칙: 환경별 DDL-AUTO 전략

#### 개발 환경
```yaml
# ✅ 필수 설정
ddl-auto: create

# ✅ 장점
# - ENUM 타입 안전 처리
# - 외래키 문제 없음
# - 스키마 자유 변경
# - DataInit 자동 실행

# ❌ 주의사항
# - 서버 재시작 시 데이터 초기화 (개발 중엔 문제 없음)
```

#### 운영 환경 (향후 적용)
```yaml
# ✅ 필수 설정
ddl-auto: validate

# ✅ 필수 도구
# - Flyway 또는 Liquibase

# ✅ 장점
# - 스키마 변경 이력 관리
# - 안전한 마이그레이션
# - 팀 협업 용이
```

### 기존 규칙 폐기

**❌ 사용 금지**:
```yaml
ddl-auto: update  # ENUM 문제로 사용 금지
```

**이유**:
- ENUM 타입 변경 시 테이블 재구성
- 외래키 락 대기로 무한 멈춤
- 불안정한 스키마 관리

---

## 🎓 학습 포인트

### ENUM vs VARCHAR 비교

| 항목 | ENUM | VARCHAR |
|------|------|---------|
| **저장 공간** | 1~2 bytes | 최대 20 bytes |
| **성능** | 약간 빠름 | 거의 동일 |
| **유연성** | ❌ 낮음 (값 추가 시 ALTER TABLE) | ✅ 높음 (자유롭게 추가) |
| **Hibernate 호환** | ⚠️ 불안정 | ✅ 안정 |
| **권장 여부** | ❌ 비추천 | ✅ 추천 |

### Hibernate DDL 모드 비교

| 모드 | 동작 | 데이터 유지 | 위험도 | 권장 환경 |
|------|------|-------------|---------|-----------|
| `none` | DDL 없음 | ✅ | ✅ 안전 | 운영 |
| `validate` | 검증만 | ✅ | ✅ 안전 | 운영 |
| `update` | 변경만 | ✅ | ⚠️ 중간 | ❌ 비추천 |
| `create` | 재생성 | ❌ | ⚠️ 중간 | ✅ 개발 |
| `create-drop` | 생성 후 삭제 | ❌ | ⚠️ 높음 | 테스트 |

---

## 🚀 다음 단계 (운영 환경 준비)

### 1. Flyway 의존성 추가 (향후)

```gradle
// build.gradle
dependencies {
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
}
```

### 2. 마이그레이션 스크립트 작성

```
src/main/resources/db/migration/
├── V1__create_initial_schema.sql
├── V2__add_user_profile_image.sql
└── V3__alter_counsel_status_to_varchar.sql
```

### 3. 운영 환경 설정

```yaml
# application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true
```

---

## 📚 관련 문서

- [DDL-AUTO ENUM 멈춤 현상 해결](../08-troubleshooting/ddl-auto-enum-hang-fix.md)
- [DDL-AUTO 설정 최적화](../07-changelog/2025-11-28-ddl-auto-update-fix.md)
- [프로젝트 아키텍처](../02-architecture/ARCHITECTURE.md)

---

**변경 이력**:
- 2025-11-28: DDL-AUTO 전략 정립 및 ENUM 처리 가이드 작성
- 2025-11-28: 개발 환경 `ddl-auto: create` 최종 결정

