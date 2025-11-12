# 📚 문서 관리 가이드 (Documentation Management Guide)

**프로젝트**: Spring PetClinic  
**버전**: 3.5.3  
**최종 수정일**: 2025-11-06  
**작성자**: Jeongmin Lee

---

## 📂 문서 폴더 구조

```
docs/
├── 01-project-overview/          # 프로젝트 개요 및 규칙
│   ├── PROJECT_DOCUMENTATION.md  # 프로젝트 전체 문서 (메인)
│   ├── PROJECT_RULES_UPDATE_20251106.md  # 규칙 업데이트 이력
│   └── FEATURE_UPGRADE.md        # 기능 업그레이드 가이드
│
├── 02-architecture/              # 아키텍처 및 설계
│   └── (추후 추가 예정)
│
├── 03-database/                  # 데이터베이스 설계
│   └── TABLE_DEFINITION.md       # 테이블 정의서
│
├── 04-api/                       # API 명세
│   └── (추후 추가 예정)
│
├── 05-ui-screens/                # UI 화면 정의
│   └── UI_SCREEN_DEFINITION.md   # UI 화면 정의서 ⭐NEW
│
├── 06-security/                  # 보안 관련
│   └── SECURITY_IMPLEMENTATION.md  # 보안 구현 문서
│
├── 07-changelog/                 # 변경 이력
│   ├── CHANGELOG.md              # 변경 이력 (메인)
│   └── WORK_SUMMARY_20251106.md  # 작업 요약
│
├── 08-troubleshooting/           # 문제 해결
│   ├── ERROR_RESOLUTION_20251106.md  # 오류 해결 보고서
│   ├── FIX_SUMMARY_20251106.md   # 수정 요약
│   └── SOFT_DELETE_VERIFICATION.md  # Soft Delete 검증
│
└── 09-quick-reference/           # 빠른 참조
    └── QUICK_REFERENCE.md        # 빠른 참조 가이드
```

---

## 📋 폴더별 설명

### 01-project-overview (프로젝트 개요)
**목적**: 프로젝트 전반적인 정보, 개발 규칙, 기능 명세

**주요 문서**:
- `PROJECT_DOCUMENTATION.md`: **메인 문서** (가장 먼저 읽어야 할 문서)
  - 프로젝트 개요, 아키텍처, 패키지 구조
  - 개발 규칙 (코딩 규칙, DDL 규칙, 테스트 규칙)
  - 주요 기능 명세
  
- `PROJECT_RULES_UPDATE_20251106.md`: 규칙 업데이트 이력
- `FEATURE_UPGRADE.md`: 기능 업그레이드 가이드

**업데이트 주기**: 주요 규칙 변경 시, 기능 추가 시

---

### 02-architecture (아키텍처)
**목적**: 시스템 아키텍처, 디자인 패턴, 클래스 다이어그램

**주요 문서**:
- `ARCHITECTURE.md`: **시스템 아키텍처** ⭐NEW
  - 전체 시스템 구조 다이어그램 (ASCII Art)
  - 레이어 구조 (Presentation, Business, Persistence)
  - 패키지 의존성 규칙
  - 데이터 흐름 (요청 처리, 게시글 등록)
  - 보안 아키텍처 (Spring Security)
  - 파일 저장 구조
  - 기술 스택

**업데이트 주기**: 아키텍처 변경 시 **권장**

---

### 03-database (데이터베이스)
**목적**: 데이터베이스 스키마, 테이블 정의, ERD

**주요 문서**:
- `TABLE_DEFINITION.md`: **테이블 정의서**
  - 모든 테이블의 컬럼 정보 (한글명, 데이터 타입, NULL 여부, 기본값)
  - 테이블 관계도
  - 변경 이력

**업데이트 주기**: 테이블 구조 변경 시 **필수**

**작성 규칙**:
- 테이블 추가 시 즉시 업데이트
- 변경 이력 섹션에 날짜와 변경 내용 기록
- Entity 클래스 경로 명시

---

### 04-api (API 명세)
**목적**: REST API 명세, 엔드포인트 정의, 요청/응답 예시

**주요 문서**:
- `API_SPECIFICATION.md`: **API 명세서** ⭐NEW
  - 인증 API (로그인, 회원가입, 로그아웃)
  - 온라인상담 API (CRUD, 댓글, 파일 다운로드)
  - 커뮤니티 API (목록 조회)
  - 시스템 설정 API
  - 공통 응답 형식 및 에러 코드
  
- `FILE_UPLOAD_PROGRESSBAR.md`: **파일 업로드 가이드** ⭐NEW
  - 구현 방법 비교 (5가지)
  - 라이브러리 추천 (Uppy, Dropzone.js)
  - 순수 JavaScript 구현 예시
  - 라이브러리 사용 승인 요청

**업데이트 주기**: API 추가/변경 시 **필수**

---

### 05-ui-screens (UI 화면) ⭐NEW
**목적**: 화면 레이아웃, 입력 필드, 동작 명세

**주요 문서**:
- `UI_SCREEN_DEFINITION.md`: **UI 화면 정의서**
  - 화면별 레이아웃 다이어그램 (ASCII Art)
  - 입력 필드 테이블 (필드명, 타입, 필수 여부, 검증)
  - 화면 동작 명세
  - 변경 이력

**업데이트 주기**: 화면 추가/수정 시 **필수**

**작성 규칙**:
- 화면 추가 시 레이아웃 다이어그램 작성
- 입력 필드 테이블 작성
- 동작 명세 상세 기록
- 변경 이력에 날짜와 내용 기록

---

### 06-security (보안)
**목적**: 보안 정책, 인증/인가, 암호화

**주요 문서**:
- `SECURITY_IMPLEMENTATION.md`: 보안 구현 문서
  - Spring Security 설정
  - 로그인/회원가입
  - Remember-Me
  - 권한 관리

**업데이트 주기**: 보안 기능 추가/변경 시

---

### 07-changelog (변경 이력)
**목적**: 프로젝트 변경 이력 관리

**주요 문서**:
- `CHANGELOG.md`: **변경 이력 메인 문서**
  - 버전별 변경사항 (추가, 수정, 삭제, 보안)
  - 날짜, 카테고리, 영향 범위 명시
  
- `WORK_SUMMARY_YYYYMMDD.md`: 작업 요약 (날짜별)

**업데이트 주기**: 모든 변경 시 **필수**

**작성 규칙**:
- 변경사항은 즉시 기록
- 카테고리별로 분류 (추가/수정/삭제/보안/의존성)
- 관련 파일 모두 명시

---

### 08-troubleshooting (문제 해결)
**목적**: 발생한 문제 및 해결 방법 기록

**주요 문서**:
- `ERROR_RESOLUTION_YYYYMMDD.md`: 오류 해결 보고서
  - 오류 내용, 원인 분석, 해결 방법
  
- `FIX_SUMMARY_YYYYMMDD.md`: 수정 요약
- `SOFT_DELETE_VERIFICATION.md`: Soft Delete 검증

**업데이트 주기**: 문제 발생 및 해결 시

**작성 규칙**:
- 오류 메시지 전체 기록
- 원인 분석 상세 기록
- 해결 방법 단계별로 기록

---

### 09-quick-reference (빠른 참조)
**목적**: 개발 중 빠르게 참조할 수 있는 정보

**주요 문서**:
- `QUICK_REFERENCE.md`: 빠른 참조 가이드
  - 주요 URL 목록
  - 자주 사용하는 명령어
  - 코드 스니펫

**업데이트 주기**: 주요 정보 변경 시

**권장 사용법**: 출력하여 책상에 비치

---

## 📝 문서 작성 규칙

### 1. 마크다운 형식
- 모든 문서는 `.md` 파일로 작성
- UTF-8 인코딩 사용
- 제목은 `#`, `##`, `###` 계층 구조 사용

### 2. 문서 헤더
모든 문서는 다음 헤더를 포함:
```markdown
# 📚 문서 제목

**프로젝트**: Spring PetClinic  
**버전**: X.X.X  
**최종 수정일**: YYYY-MM-DD  
**작성자**: 이름
```

### 3. 변경 이력 섹션
모든 문서 하단에 변경 이력 포함:
```markdown
## 외부 라이브러리 관리 규칙 ⭐NEW

### CDN 사용 금지 원칙

**규칙**: 외부 라이브러리는 **절대 CDN을 사용하지 않고** 프로젝트에 내장

**이유**:
1. ✅ 오프라인 환경에서도 실행 가능
2. ✅ 버전 고정으로 안정성 확보
3. ✅ 외부 서버 장애에 영향 받지 않음
4. ✅ 보안 취약점 통제 가능

**금지 예시**:
```html
<!-- ❌ 금지: CDN 사용 -->
<script src="https://cdn.jsdelivr.net/npm/@uppy/core@3.3.1/dist/uppy.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@uppy/core@3.3.1/dist/uppy.min.css">
```

**권장 방식**:
```bash
# ✅ 권장: npm 또는 Gradle로 프로젝트에 내장
npm install @uppy/core @uppy/dashboard @uppy/xhr-upload
```

```html
<!-- ✅ 권장: 로컬 경로 사용 -->
<script th:src="@{/webjars/uppy/dist/uppy.min.js}"></script>
<link rel="stylesheet" th:href="@{/webjars/uppy/dist/uppy.min.css}">
```

### 라이브러리 추가 프로세스

1. **사전 검토**
   - 라이센스 확인 (MIT, Apache 2.0 권장)
   - 유지보수 상태 확인 (최근 6개월 내 업데이트)
   - 보안 이슈 확인
   - 번들 크기 검토

2. **프로젝트 관리자 승인**
   - FILE_UPLOAD_PROGRESSBAR.md 같은 가이드 문서 작성
   - 장단점 비교표 제시
   - 대안 제시

3. **설치 및 적용**
   - `build.gradle` 또는 `package.json`에 의존성 추가
   - `src/main/resources/static/` 또는 `webjars/`에 배치
   - 로컬 경로로 import

4. **문서 업데이트**
   - `CHANGELOG.md`에 추가 이력 기록
   - `README.md`에 기술 스택 추가
   - `ARCHITECTURE.md`에 의존성 추가

---

## 변경 이력

### [3.5.4] - 2025-11-11
#### 추가
- 외부 라이브러리 관리 규칙 추가
- CDN 사용 금지 원칙 명시
- 라이브러리 추가 프로세스 정의

### [3.5.3] - 2025-11-06
#### 추가
- 추가된 내용

#### 수정
- 수정된 내용

#### 삭제
- 삭제된 내용
```

### 4. 이모지 사용
가독성 향상을 위해 이모지 사용:
- ✅ 완료
- ❌ 금지
- ⚠️ 주의
- 📝 메모
- 🔴 중요
- 🟡 보통
- 🟢 낮음
- ⭐ 신규

---

## 🔄 문서 업데이트 프로세스

### 테이블 변경 시
1. Entity 클래스 수정
2. `docs/03-database/TABLE_DEFINITION.md` 업데이트
3. 변경 이력 섹션에 기록
4. `docs/07-changelog/CHANGELOG.md` 업데이트

### 화면 추가/수정 시
1. Thymeleaf 템플릿 작성
2. `docs/05-ui-screens/UI_SCREEN_DEFINITION.md` 업데이트
   - 레이아웃 다이어그램 작성
   - 입력 필드 테이블 작성
   - 동작 명세 작성
3. 변경 이력 섹션에 기록
4. `docs/07-changelog/CHANGELOG.md` 업데이트

### API 추가/변경 시
1. Controller 메서드 작성
2. `docs/04-api/API_SPECIFICATION.md` 업데이트 (추후 추가)
3. `docs/09-quick-reference/QUICK_REFERENCE.md` URL 테이블 업데이트
4. `docs/07-changelog/CHANGELOG.md` 업데이트

### 규칙 추가/변경 시
1. `docs/01-project-overview/PROJECT_DOCUMENTATION.md` 업데이트
2. `docs/09-quick-reference/QUICK_REFERENCE.md` 업데이트
3. `docs/07-changelog/CHANGELOG.md` 업데이트

---

## 📊 문서 우선순위

### 🔴 필수 (즉시 업데이트)
1. `CHANGELOG.md` - 모든 변경 시
2. `TABLE_DEFINITION.md` - 테이블 변경 시
3. `UI_SCREEN_DEFINITION.md` - 화면 변경 시
4. `API_SPECIFICATION.md` - API 추가/변경 시 ⭐NEW

### 🟡 권장 (변경 후 24시간 내)
1. `PROJECT_DOCUMENTATION.md` - 주요 기능 추가 시
2. `QUICK_REFERENCE.md` - API/URL 변경 시
3. `ARCHITECTURE.md` - 아키텍처 변경 시 ⭐NEW

### 🟢 선택 (주간 리뷰 시)
1. `FILE_UPLOAD_PROGRESSBAR.md` - 구현 가이드 ⭐NEW
2. 문제 해결 문서

---

## 🎯 신규 개발자 온보딩

### 읽어야 할 문서 순서
1. **README.md** (루트 디렉토리)
2. **docs/01-project-overview/PROJECT_DOCUMENTATION.md** (필수)
3. **docs/09-quick-reference/QUICK_REFERENCE.md** (권장, 출력 비치)
4. **docs/02-architecture/ARCHITECTURE.md** (권장) ⭐NEW
5. **docs/03-database/TABLE_DEFINITION.md**
6. **docs/04-api/API_SPECIFICATION.md** ⭐NEW
7. **docs/05-ui-screens/UI_SCREEN_DEFINITION.md**
8. **docs/07-changelog/CHANGELOG.md**

### 예상 소요 시간
- 1~2단계: 2시간
- 3~4단계: 1.5시간
- 5~6단계: 1시간
- 7~8단계: 1시간
- **총 소요 시간**: 약 5.5시간

---

## 📁 문서 네이밍 규칙

### 메인 문서
- 대문자 + 언더스코어: `PROJECT_DOCUMENTATION.md`
- 축약어 사용 금지

### 날짜별 문서
- 형식: `CATEGORY_SUMMARY_YYYYMMDD.md`
- 예시: `WORK_SUMMARY_20251106.md`

### 카테고리별 문서
- 형식: `CATEGORY_SPECIFICATION.md`
- 예시: `UI_SCREEN_DEFINITION.md`

---

## 🔍 문서 검색 방법

### IDE에서 검색 (IntelliJ IDEA)
```
Ctrl + Shift + F
검색 범위: docs/ 디렉토리
```

### 파일명으로 검색
```bash
# Windows
dir /s /b docs\*.md | findstr "키워드"

# Git Bash
find docs/ -name "*키워드*.md"
```

### 내용으로 검색
```bash
# Git Bash
grep -r "검색어" docs/
```

---

## ✅ 문서 품질 체크리스트

### 작성 시
- [ ] 문서 헤더 작성 (프로젝트, 버전, 날짜, 작성자)
- [ ] 목차 작성 (섹션 3개 이상 시)
- [ ] 변경 이력 섹션 포함
- [ ] 코드 블록에 언어 지정 (```java, ```yaml 등)
- [ ] 테이블 형식 사용 (필드 정의, 비교 등)

### 검토 시
- [ ] 맞춤법 확인
- [ ] 일관된 용어 사용
- [ ] 날짜 형식 통일 (YYYY-MM-DD)
- [ ] 링크 정상 작동 확인

---

**문서 버전**: 1.0  
**최종 검토**: 2025-11-06  
**담당자**: Jeongmin Lee

