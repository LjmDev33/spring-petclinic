# ✅ 프로젝트 규칙 추가 완료 (2025-11-14 업데이트)

## 최근 추가된 규칙 (2025-11-14)

### 📋 규칙 11: 요구사항 구현 후 검증 프로세스 ⭐NEW

#### 핵심 내용
**목적**: 모든 요구사항 구현 후 자체 검증을 통해 품질 보증

**필수 검증 항목**:
1. ✅ **컴파일 검증**: `gradlew compileJava` 또는 `build -x test` 실행
2. ✅ **에러 체크**: 모든 수정 파일에 대해 컴파일 에러 확인
3. ✅ **요구사항 재확인**: 사용자가 요청한 모든 항목 구현 확인
4. ✅ **파일 상태 검증**: 생성/수정/삭제된 파일의 존재 및 크기 확인
5. ✅ **문서 업데이트**: 변경사항을 관련 문서에 반영

#### 검증 프로세스
```
1. 요구사항 구현 완료
   ↓
2. 컴파일 검증 (gradlew compileJava)
   ↓
3. 에러 체크 (get_errors 도구 사용)
   ↓
4. 요구사항 대조 (모든 항목 체크리스트)
   ↓
5. 파일 상태 확인 (생성/수정된 파일 검증)
   ↓
6. 문서 업데이트 (CHANGELOG, 관련 문서)
   ↓
7. 최종 보고 (완료된 작업 요약)
```

#### 검증 완료 기준
- ✅ 컴파일 에러 0개
- ✅ 요구사항 100% 구현
- ⚠️ 경고는 허용 (기능에 영향 없는 경우)
- ✅ 문서 업데이트 완료

---

## 기존 프로젝트 규칙 (2025-11-06)

### 📜 규칙 9: Hibernate DDL 및 스키마 관리 ⭐NEW

#### 핵심 내용
**목적**: 모든 테이블(기존 + 신규)에서 DROP 오류 발생 방지

**필수 규칙**:
1. ✅ **개발 환경 DDL**: 항상 `ddl-auto: update` 사용
2. ❌ **create-drop 절대 금지**: 테이블 DROP 오류 및 데이터 손실
3. ✅ **Entity 작성 시**: `@Table(name = "테이블명")` 명시
4. ✅ **Soft Delete 필수**: `del_flag`, `deleted_at` 필드 포함
5. ✅ **초기화 필요 시**: `drop-all-tables.sql` 수동 실행

#### 설정 (application-dev.yml)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ✅ 권장
      # ddl-auto: create-drop  # ❌ 절대 금지
```

#### 적용 범위
- ✅ **기존 테이블**: 모든 테이블 적용 완료
- ✅ **신규 테이블**: 규칙 준수 시 자동 적용
- ✅ **외래키 제약조건**: 개발 환경에서는 생성 안 함

#### ddl-auto 옵션 비교

| 옵션 | DROP | CREATE | ALTER | 데이터 유지 | 권장 |
|------|------|--------|-------|------------|------|
| **update** | ❌ | ✅ | ✅ | ✅ | ✅ 개발 환경 권장 |
| create | ❌ | ✅ | ❌ | ❌ | ⚠️ 초기 개발만 |
| create-drop | ✅ | ✅ | ❌ | ❌ | ❌ 절대 금지 |
| validate | ❌ | ❌ | ❌ | ✅ | ✅ 운영 환경 |
| none | ❌ | ❌ | ❌ | ✅ | ✅ 운영 환경 |

---

### 🚀 규칙 10: 테스트 및 서버 실행 ⭐NEW

#### 핵심 내용
**목적**: 포트 충돌 방지 및 안정적인 서버 관리

**필수 규칙**:
1. ✅ **서버 실행**: 항상 IDE(IntelliJ IDEA)에서 실행
2. ❌ **터미널 bootRun 금지**: 포트 점유 문제 발생
3. ✅ **컴파일/빌드**: Gradle 명령어 사용 가능
4. ❌ **테스트 코드**: 별도 요청 없으면 작성하지 않음

#### 허용/금지 명령어

| 명령어 | 용도 | 허용 여부 |
|--------|------|----------|
| `./gradlew compileJava` | 컴파일 확인 | ✅ 허용 |
| `./gradlew build -x test` | 빌드 확인 | ✅ 허용 |
| `./gradlew dependencies` | 의존성 확인 | ✅ 허용 |
| `./gradlew --stop` | Daemon 종료 | ✅ 허용 |
| `./gradlew bootRun` | 서버 실행 | ❌ **금지** |

#### 서버 실행 방법 (IDE)
```
1. IntelliJ IDEA에서 PetClinicApplication.java 열기
2. main 메서드 옆 실행 버튼 클릭
3. Active profiles: dev 확인
4. Run 또는 Debug 실행
5. http://localhost:8080 접속
```

#### bootRun 금지 이유
- 백그라운드 실행 시 포트가 살아있어 수동 종료 필요
- `taskkill` 명령어로 강제 종료해야 함
- IDE Stop 버튼이 훨씬 간편하고 안전

---

## 📝 업데이트된 문서 (5개)

### 1. PROJECT_DOCUMENTATION.md
**섹션 6. 개발 규칙**:
- ✅ 규칙 9: Hibernate DDL 및 스키마 관리
  - ddl-auto 옵션 상세 설명
  - Entity 작성 가이드
  - DROP 오류 대응 방법
  
- ✅ 규칙 10: 테스트 및 서버 실행
  - IDE 실행 방법
  - 허용/금지 명령어
  - 포트 충돌 해결 방법

### 2. QUICK_REFERENCE.md
**설정 파일 섹션**:
- ✅ ddl-auto: update로 변경
- ✅ DDL 옵션 선택 가이드 추가

**신규 섹션**:
- ✅ 서버 실행 규칙
- ✅ 허용/금지 명령어 명시

### 3. CHANGELOG.md
**버전 3.5.3**:
- ✅ 프로젝트 규칙 추가 이력
- ✅ 규칙 9, 10 상세 내용
- ✅ 문서 업데이트 내역

### 4. README.md
**신규 섹션**:
- ✅ 서버 실행 방법
- ✅ 허용/금지 명령어
- ✅ 빌드 및 컴파일 가이드

### 5. application-dev.yml
**Hibernate 설정**:
- ✅ ddl-auto: update
- ✅ naming strategy 추가
- ✅ 스키마 관리 옵션 명시

---

## 🎯 적용 효과

### Before (문제 발생)
```
❌ 5개 테이블 DROP 오류
❌ 신규 테이블 추가 시 매번 오류 발생 가능성
❌ 서버 시작 시마다 데이터 삭제
❌ bootRun 사용 시 포트 점유 문제
```

### After (규칙 적용 후)
```
✅ 모든 테이블 DROP 오류 없음
✅ 신규 테이블 자동으로 규칙 적용
✅ 기존 데이터 유지 (update 모드)
✅ IDE 실행으로 포트 관리 간편
✅ 스키마 변경 시 자동 ALTER TABLE
```

---

## 📋 개발자 체크리스트

### 신규 Entity 추가 시
- [ ] `@Table(name = "테이블명")` 명시
- [ ] `del_flag`, `deleted_at` 필드 추가 (Soft Delete)
- [ ] `@SQLDelete` 어노테이션 추가
- [ ] `@SQLRestriction("del_flag = 0")` 추가
- [ ] TABLE_DEFINITION.md 업데이트
- [ ] CHANGELOG.md에 이력 기록

### 서버 실행 시
- [ ] IDE에서 실행 (Run 버튼)
- [ ] Active profiles: dev 확인
- [ ] http://localhost:8080 접속 테스트
- [ ] ❌ `./gradlew bootRun` 사용하지 않기

### 데이터 초기화 필요 시
- [ ] IDE에서 서버 중지
- [ ] MySQL에서 `drop-all-tables.sql` 실행
- [ ] 서버 재시작
- [ ] DataInit이 자동으로 데이터 생성

---

## 🔄 다음 단계

### 개발 환경 확인
1. ✅ application-dev.yml 설정 확인
2. ✅ IDE 실행 설정 확인 (Active profiles: dev)
3. ✅ MySQL 데이터베이스 실행 확인

### 서버 재시작
1. IDE에서 서버 중지
2. PetClinicApplication.java 실행
3. 콘솔에서 오류 없는지 확인
4. http://localhost:8080 접속 테스트

### 예상 결과
```
✅ 서버 정상 기동
✅ 테이블 자동 생성 (CREATE TABLE)
✅ 데이터 자동 생성 (DataInit)
✅ 메인 페이지 정상 접속
✅ 로그인/회원가입 페이지 정상 작동
```

---

**규칙 추가 완료**: 2025-11-06  
**업데이트된 문서**: 5개  
**적용 범위**: 모든 테이블 (기존 + 신규)  
**서버 재시작**: IDE에서 실행하세요!

