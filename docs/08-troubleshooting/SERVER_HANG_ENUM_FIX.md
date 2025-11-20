# 서버 실행 중단 문제 해결 가이드

**발생일**: 2025-11-20  
**카테고리**: Hibernate DDL, MySQL ENUM  
**심각도**: 🔴 높음 (서버 실행 불가)

---

## 🚨 문제 상황

**증상**:
```
2025-11-20T10:10:50.183+09:00  INFO 19236 --- [petclinic] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration) 
Hibernate: alter table counsel_post modify column status enum ('COMPLETE','END','WAIT') not null

▶ 이 로그 이후 서버가 무한 대기 상태로 멈춤
```

**원인**:
1. `ddl-auto: update` 설정으로 Hibernate가 자동으로 스키마 변경 시도
2. `counsel_post.status` 컬럼이 ENUM 타입
3. Hibernate가 ENUM 값 순서를 변경하려고 시도 (`COMPLETE, END, WAIT`)
4. MySQL에서 ENUM 컬럼 ALTER 시 **테이블 락 발생**
5. 기존 데이터가 있는 상태에서 락이 해제되지 않아 무한 대기

---

## 🔧 해결 방법

### 방법 1: ddl-auto를 validate로 변경 (권장) ✅

**적용 완료**:

`application-dev.yml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # update → validate로 변경
```

**효과**:
- ✅ Hibernate가 스키마를 변경하지 않고 검증만 수행
- ✅ 서버 실행 중단 문제 해결
- ✅ 기존 데이터 보호

---

### 방법 2: 수동으로 ENUM 컬럼 수정

**SQL 스크립트**: `src/main/resources/db/mysql/fix-counsel-enum.sql`

```sql
USE petclinic;

-- ENUM 값 순서 수정: WAIT, COMPLETE, END
ALTER TABLE counsel_post 
MODIFY COLUMN status ENUM('WAIT', 'COMPLETE', 'END') NOT NULL;
```

**실행 방법**:

**Option A: 배치 파일 실행** (권장)
```cmd
fix-enum.bat
```

**Option B: MySQL 직접 실행**
```bash
mysql -u dev33 -pezflow_010 petclinic < src/main/resources/db/mysql/fix-counsel-enum.sql
```

**Option C: MySQL Workbench/HeidiSQL 등 GUI 도구**
1. petclinic 데이터베이스 연결
2. `fix-counsel-enum.sql` 파일 열기
3. 실행 (Ctrl+Shift+Enter)

---

## 📊 ENUM 값 순서

### Java Enum (CounselStatus.java)
```java
public enum CounselStatus {
    WAIT("답변대기"),      // 1번째
    COMPLETE("답변완료"),   // 2번째
    END("상담종료");        // 3번째
}
```

### MySQL ENUM (이전)
```sql
ENUM('COMPLETE', 'END', 'WAIT')  -- ❌ 순서 불일치
```

### MySQL ENUM (수정 후)
```sql
ENUM('WAIT', 'COMPLETE', 'END')  -- ✅ Java Enum과 동일
```

---

## 🚀 서버 재기동 절차

### 1. 현재 실행 중인 서버 종료

**방법 A: IDE에서 종료**
- IntelliJ IDEA의 Stop 버튼 클릭

**방법 B: 프로세스 강제 종료**
```powershell
# Java 프로세스 확인
Get-Process -Name java | Where-Object {$_.MainWindowTitle -like "*petclinic*"}

# 강제 종료
taskkill /F /IM java.exe
```

### 2. application-dev.yml 수정 확인

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ✅ validate로 되어 있는지 확인
```

### 3. (선택) ENUM 컬럼 수정 (방법 2 참고)

만약 스키마 불일치 오류가 발생하면 `fix-enum.bat` 실행

### 4. 서버 재기동

**IDE에서 실행** (권장):
- PetClinicApplication.java 우클릭 → Run

---

## 🔍 검증 방법

### 1. 서버 로그 확인

```
✅ 정상 시작:
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http)
c.s.s.p.PetClinicApplication             : Started PetClinicApplication in X.XXX seconds

❌ 오류 발생:
Hibernate: alter table counsel_post modify column status...
(로그가 멈춤)
```

### 2. 데이터베이스 확인

```sql
-- ENUM 컬럼 확인
DESCRIBE counsel_post;

-- status 컬럼이 다음과 같아야 함:
-- Field: status
-- Type: enum('WAIT','COMPLETE','END')
-- Null: NO
```

### 3. 브라우저 접속

```
http://localhost:8080/counsel/list
```

---

## 📝 향후 대책

### 1. DDL 설정 규칙 변경

**개발 환경**:
```yaml
ddl-auto: validate  # 검증만 수행, 자동 변경 안함
```

**초기 개발/테스트**:
```yaml
ddl-auto: create  # 매번 테이블 재생성 (데이터 삭제 주의)
```

**운영 환경**:
```yaml
ddl-auto: none  # Hibernate가 스키마 관리 안함
```

### 2. 스키마 변경 프로세스

**원칙**:
- ✅ 스키마 변경은 **수동으로만** 수행
- ✅ 변경 전 백업 필수
- ✅ SQL 스크립트로 버전 관리
- ❌ `ddl-auto: update` 절대 사용 금지 (운영/개발 모두)

**수동 변경 절차**:
1. Entity 클래스 수정
2. SQL 스크립트 작성
3. 개발 DB에서 테스트
4. 스크립트 커밋
5. 운영 DB 변경 (야간 작업)
6. `ddl-auto: validate`로 검증

### 3. ENUM 사용 지침

**주의사항**:
- ⚠️ ENUM은 ALTER 시 테이블 락 발생
- ⚠️ 값 추가/삭제 시 전체 테이블 재작성
- ⚠️ 대량 데이터 테이블에서는 사용 지양

**대안**:
```java
// ENUM 대신 VARCHAR + 검증
@Column(name = "status", length = 20)
private String status;

@PrePersist
@PreUpdate
private void validateStatus() {
    if (!Arrays.asList("WAIT", "COMPLETE", "END").contains(status)) {
        throw new IllegalArgumentException("Invalid status: " + status);
    }
}
```

---

## 📚 관련 문서

- `application-dev.yml` - Hibernate DDL 설정
- `CounselStatus.java` - ENUM 정의
- `CounselPost.java` - Entity 클래스
- `fix-enum.bat` - ENUM 수정 배치 파일
- `fix-counsel-enum.sql` - ENUM 수정 SQL 스크립트

---

## ✅ 체크리스트

서버 재기동 전 확인 사항:

- [ ] 현재 실행 중인 서버 종료 완료
- [ ] `application-dev.yml`의 `ddl-auto: validate` 확인
- [ ] (선택) `fix-enum.bat` 실행 완료
- [ ] 포트 8080이 사용 가능한지 확인
- [ ] MySQL 서비스 실행 중인지 확인

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20  
**작성자**: GitHub Copilot

