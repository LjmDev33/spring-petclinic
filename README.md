# 📝 프로젝트 문서 (2인 협업프로젝트)

## 📌 프로젝트 개요
- 프로젝트명: 펫클리닉(petclinic)
- 한 줄 소개: 동물병원 웹사이트로 간편한 고객 예약시스템과 담당수의진 배치 및 수의진들의 로그인 시스템을 구현한 프로젝트입니다.
- 목적/배경: 천만 반려동물 시대에 아픈 반려동물들의 치료를 위하여 직접 동물병원 내원하여 접수하고 기다릴 필요없이 간편한 예약하나로 담당수의사 배치되어 원하시는 일자에 진료받을 수 있고, 병원 시스템 및 수의사들의 프로필 정보 등을 예약자분들께서 한눈에 보기 편하게 소개해드리는 목적을 갖고있습니다. 더 나아가 수의사들이 그룹웨어 사이트와 연동되어 로그인하고 담당 고객들의 Q&A에 답하고 자유롭게 소통할 수 있게 하는것을 목표로하고있습니다.

## 🎯 목표
- [ 고객예약 및 담당수의사 배치 ] 주요 기능 1
- [ 수의사 로그인 시스템 ] 주요 기능 2
- [ Q&A 및 다수의 게시판 ] 주요 기능 3

## 🛠 기술 스택
- Frontend: HTML5, Thymeleaf, Bootstrap 5.3.6, Toast UI Editor
- Backend: Spring Boot 3.5.0, Spring MVC, Spring Data JPA, QueryDSL 5.0.0
- Security: Spring Security 6.x (세션 기반 인증, BCrypt 암호화)
- Database: MySQL 8.4 (Docker Compose 환경)
- ORM: Hibernate 6.6.15
- Build Tool: Gradle 8.14.3
- IDE: IntelliJ IDEA
- 협업 툴: HackMD, GitHub, DBeaver (DB 관리)

### 주요 기능
- ✅ **파일 업로드 Progress Bar** (순수 JavaScript, 실시간 진행률 표시)
- ✅ **파일 다운로드 권한 검증** (세션 기반, 비공개 게시글 보호)
- ✅ **Soft Delete** (논리 삭제, 2주 후 물리 삭제)
- ✅ **조회수 중복 방지** (세션 + IP 기반, Proxy 환경 고려)
- ✅ **비밀번호 기반 비공개 게시글** (BCrypt 암호화)
- ✅ **댓글 모달** (Bootstrap 모달, UI 일관성)
- ✅ **마이페이지** (프로필 수정, 닉네임 관리, 비밀번호 변경)
- ✅ **비밀번호 찾기** (준비 페이지, 향후 이메일 연동 예정)
- ✅ **전화번호 자동 포맷팅** (010-0000-0000 형식)
- ✅ **시스템 설정 관리** (관리자 전용, 멀티로그인/파일 업로드 설정)
- ✅ **오프라인 실행 지원** (CDN 사용 금지, 로컬 라이브러리 내장)

## 📆 진행 상황
| 날짜 | 담당자 | 내용 | 상태 |
|------|--------|------|------|
| 2025-09-01 | dev33 | 초기 세팅 | ✅ 완료 |
| YYYY-MM-DD | 팀원 | 작업 내용 | ⬜ 진행중 |

## 📚 프로젝트 문서

### 📖 문서 구조
이 프로젝트는 **체계적인 문서 관리 시스템**을 운영합니다.

```
docs/
├── 01-project-overview/          # 프로젝트 개요 및 규칙
│   ├── PROJECT_DOCUMENTATION.md  # ⭐ 메인 문서 (필수 읽기)
│   ├── PROJECT_RULES_UPDATE_20251106.md
├── 05-ui-screens/                # UI 화면 정의
│   ├── UI_CONSISTENCY_GUIDE.md   # ⭐ UI/UX 일관성 가이드 (필수)
│   └── FEATURE_UPGRADE.md
├── 02-architecture/              # 아키텍처 및 설계
│   └── ARCHITECTURE.md           # ⭐NEW 시스템 아키텍처
├── 03-database/                  # 데이터베이스 설계
│   └── TABLE_DEFINITION.md       # 테이블 정의서
├── 04-api/                       # API 명세
│   ├── API_SPECIFICATION.md      # ⭐NEW API 명세서
│   └── FILE_UPLOAD_PROGRESSBAR.md # ⭐NEW 파일 업로드 가이드
├── 05-ui-screens/                # UI 화면 정의
│   └── UI_SCREEN_DEFINITION.md   # UI 화면 정의서
├── 06-security/                  # 보안 관련
│   └── SECURITY_IMPLEMENTATION.md
├── 07-changelog/                 # 변경 이력
│   ├── CHANGELOG.md              # 변경 이력 (메인)
│   └── WORK_SUMMARY_20251106.md
├── 08-troubleshooting/           # 문제 해결
│   ├── ERROR_RESOLUTION_20251106.md
│   ├── FIX_SUMMARY_20251106.md
│   └── SOFT_DELETE_VERIFICATION.md
└── 09-quick-reference/           # 빠른 참조
    └── QUICK_REFERENCE.md        # ⭐ 출력 비치 권장
```

### 📋 주요 문서

| 문서 | 위치 | 용도 | 우선순위 |
|------|------|------|----------|
| **PROJECT_DOCUMENTATION.md** | `docs/01-project-overview/` | 프로젝트 전체 문서 (개요, 규칙, 기능) | 🔴 필수 |
| **ARCHITECTURE.md** ⭐NEW | `docs/02-architecture/` | 시스템 아키텍처 (레이어, 의존성, 데이터 흐름) | 🔴 필수 |
| **TABLE_DEFINITION.md** | `docs/03-database/` | 테이블 정의서 (컬럼, 관계, 이력) | 🔴 필수 |
| **API_SPECIFICATION.md** ⭐NEW | `docs/04-api/` | API 명세서 (엔드포인트, 요청/응답) | 🔴 필수 |
| **UI_SCREEN_DEFINITION.md** | `docs/05-ui-screens/` | UI 화면 정의서 (레이아웃, 동작) | 🔴 필수 |
| **CHANGELOG.md** | `docs/07-changelog/` | 변경 이력 관리 | 🔴 필수 |
| **QUICK_REFERENCE.md** | `docs/09-quick-reference/` | 빠른 참조 가이드 | 🟡 권장 |
| **FILE_UPLOAD_PROGRESSBAR.md** ⭐NEW | `docs/04-api/` | 파일 업로드 Progress Bar 가이드 | 🟡 권장 |
| **SECURITY_IMPLEMENTATION.md** | `docs/06-security/` | 보안 구현 문서 | 🟡 권장 |
| **DOCUMENTATION_MANAGEMENT_GUIDE.md** | `docs/` | 문서 관리 가이드 | 🟢 참고 |

### 📝 문서 이력 관리 규칙

| 문서 | 용도 | 대상 |
|------|------|------|
| **[CHANGELOG.md](./CHANGELOG.md)** | 모든 변경 이력 기록 | 개발자 전체 |
| **[PROJECT_DOCUMENTATION.md](./PROJECT_DOCUMENTATION.md)** | 상세 아키텍처, 설계 문서 (40+ KB) | 신규 개발자, 온보딩 |
| **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** | 빠른 참조 가이드 (8+ KB) | 일상 개발 시 참조 |
| **[DOCUMENTATION_GUIDE.md](./DOCUMENTATION_GUIDE.md)** | 문서 관리 방법 | 문서 작성자 |

### 🔄 문서 업데이트 규칙
- ✅ 모든 변경사항은 **즉시** CHANGELOG.md에 기록
- ✅ 관련 문서(PROJECT_DOCUMENTATION.md, QUICK_REFERENCE.md) 자동 업데이트
- ✅ 소스코드 주석(JavaDoc)도 함께 업데이트

### 📋 신규 개발자 가이드
1. **[PROJECT_DOCUMENTATION.md](./PROJECT_DOCUMENTATION.md)** 전체 읽기 (필수)
2. **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** 출력하여 책상에 비치 (권장)
3. 개발 시 **[CHANGELOG.md](./CHANGELOG.md)** 참조

---

## 🚀 서버 실행 방법

### ✅ 권장: IDE에서 실행
```
1. IntelliJ IDEA에서 PetClinicApplication.java 열기
2. main 메서드 옆 실행 버튼 클릭
3. Active profiles: dev 설정
4. http://localhost:8080 접속
```

### ❌ 금지: 터미널 bootRun
```bash
# ❌ 사용하지 마세요
./gradlew bootRun  # 포트 관리 문제 발생
```

**이유**: 백그라운드 실행 시 포트가 살아있어 수동 종료 필요

### ✅ 허용: 빌드 및 컴파일
```bash
# 컴파일만
./gradlew compileJava

# 빌드 (테스트 제외)
./gradlew build -x test

# Gradle Daemon 종료
./gradlew --stop
```

---

## 📚 참고 자료
- 디자인 시안: 
- API 문서: [PROJECT_DOCUMENTATION.md - 섹션 5, 7](./PROJECT_DOCUMENTATION.md)
- 회의록: 
- 개발 규칙: [PROJECT_DOCUMENTATION.md - 섹션 6](./PROJECT_DOCUMENTATION.md)

---

## 📌 할 일 (To-Do)

### ✅ 완료된 기능 (2025-11-06)
- [x] **파일 다운로드 기능** (UTF-8 한글 파일명 지원)
- [x] **게시글 수정/삭제 기능** (Soft Delete)
- [x] **대댓글 트리 구조 UI 개선** (들여쓰기 + 배지)
- [x] **조회수 중복 방지** (세션 기반)
- [x] **관리자 댓글 UI 개선** (배지 강화)

### 🔴 우선순위 높음
- [ ] 로그인/회원가입 기능 (Spring Security)
- [ ] 관리자 권한 관리
- [ ] 파일 다운로드 권한 검증 (비공개 글)

### 🟡 우선순위 중간
- [ ] 게시글 수정 시 첨부파일 관리
- [ ] 파일 업로드 진행률 표시
- [ ] 대댓글 작성 기능 (답글 버튼)

### 🟢 우선순위 낮음
- [ ] 좋아요 기능
- [ ] 이미지 썸네일 자동 생성
- [ ] 알림 기능 (이메일)

