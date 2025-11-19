# Spring Petclinic 개편 구조 점검 (2025-11-18)

## 1. 디렉터리 / 모듈 구성
- 루트: Gradle 기반 (`build.gradle`, `settings.gradle`), Docker/MySQL 설정(`docker-compose.yml`, `init.sql`, `my.cnf`).
- 소스: `spring-petclinic/src/main/java/org/springframework/samples/petclinic` 아래 도메인별 패키지.
- 리소스: `src/main/resources` (application-*.yml, `messages`, `templates`, `static`).
- 문서: `docs/01~09` 섹션과 `DOCUMENTATION_GUIDE.md`, 모든 변경은 `docs/changes` 및 `docs/07-changelog/CHANGELOG.md` 동시 관리.

## 2. 패키지 계층 요약
| 패키지 | 주요 역할 |
| --- | --- |
| `common` | 공통 DTO(`PageResponse`), Base Entity, init, config.
| `community` | 공지/커뮤니티 게시판. Controller → Service → Repository(QueryDSL Custom) → Table + Mapper → DTO.
| `counsel` | 온라인 상담. 비공개 비번/조회수/Soft delete/스케줄러/첨부/댓글. Controller에서 세션 제어, Service가 검증·트랜잭션, Repository Impl이 QueryDSL.
| `faq` | FAQ 게시판. Controller/Service/Repository/Table/init.
| `user` | 로그인/회원가입/마이페이지. AuthController, MyPageController, DTO, Repository, Service, Password 관리.
| `security` | `SecurityConfig`, 인증 성공 핸들러, PasswordEncoder, Remember-Me, 세션 정책.
| `system` | 시스템 설정/캐시/WebConfig, 관리자 설정 UI 연계, BooleanToYNConverter, Cron 스케줄러.

## 3. 요청 흐름 패턴
1. Controller (`@Controller`)가 `/module/*` URL 매핑 및 파라미터 검증, Model에 DTO 담아 `template` 속성으로 화면 지정.
2. Service (`@Service @Transactional`)가 비즈니스 규칙, Soft delete, 권한/비밀번호 검증, Mapper를 통해 DTO↔Entity 변환.
3. Repository (`JpaRepository + CustomImpl`)가 CRUD + QueryDSL 검색/페이징/PrevNext/통계 처리.
4. Entity(`table` 패키지)는 LocalDateTime 필드, `delFlag`, 연관관계 명시. DTO만 외부 노출.
5. Scheduler(`counsel/scheduler`)는 삭제 예약된 파일/게시글을 2주 후 물리 삭제하고 로그 기록.

## 4. 템플릿 / UI 구조
- 공통 레이아웃: `templates/fragments/layout.html` (헤더, 네비, footer, `th:insert=${template}`).
- 화면: `templates/community/*`, `counsel/*`, `faq/*`, `admin/*`, `user/*` 등. 공통 fragment(`fragments/inputField`, `selectField`, `pagination`).
- Bootstrap 기반, 인라인 스타일 최소화, 사용자 직관성/일관성 규칙 적용. 버튼 공통 CSS 사용(bootstrap 기본 제거, 커스텀 클래스).
- UI 규칙: Uppy 기반 업로드, 댓글 모달, 반응형 레이아웃, alert 대신 모달, 버튼 동일 크기·간격.

## 5. 데이터 / 보안 규칙
- 날짜·시간은 `LocalDateTime`, 뷰 포맷은 `#temporals.format`.
- Entity 직접 노출 금지, DTO + Mapper 필수.
- QueryDSL 검색/페이징 로직은 Impl 내부 전담.
- Soft delete (`delFlag`), Scheduler가 2주 후 실제 삭제 + 로그 남김.
- 파일 삭제는 Soft 정책 + try/catch로 리소스 누수 방지.
- 관리자 권한, 멀티 로그인 설정, Remember-Me, PasswordEncoder(BCrypt) 필수.
- SQL Injection 방지, CDN 금지, 라이브러리는 프로젝트 내장(WebJar/Uppy local)만 사용.
- 로그: 사용자가 생성/수정 시 DB에 기록.

## 6. 문서 동기화 정책
- 변경 시 `docs/07-changelog/CHANGELOG.md`, 관련 세부 문서(API, UI, Table Definition 등) 동시 업데이트.
- `docs/changes/` 하위에 작업 요약 Markdown 작성(본 문서 포함).
- 프로젝트 관리 문서 참고 우선순위: `docs/01-project-overview/PROJECT_DOCUMENTATION.md`, `docs/05-ui-screens/UI_SCREEN_DEFINITION.md`, `docs/04-api/API_SPECIFICATION.md`, `docs/03-database/TABLE_DEFINITION.md`, `docs/09-quick-reference/QUICK_REFERENCE.md`.

## 7. 차후 작업 시 유의 사항
- 서버 기동은 사용자가 직접 수행하며, 애플리케이션 실행이 필요하면 반드시 사용자에게 bootRun 요청을 먼저 전달한다.
- 서버 실행 요청을 보내고 사용자가 기동했다고 알려주면, 그 다음 단계에서 API 검증과 이후 워크플로우를 진행한다.
- 검색 시 IDE find-in-files 대신 local-fs MCP로 파일을 직접 읽어 내용을 파악한다.
- 커맨드 체인에 `&&` 사용 불가.
- 서버/테스트 실행은 최소화, 실행 후 즉시 중지.
- 모든 UI 변경은 puppeteer/http-fetch로 검증.
- 요구사항 반영 시 자체 검증(빌드/로그/문서) 후 채팅에는 핵심 결과만 보고.

## 8. 워크플로우 단계 정의
1. **정적 분석(템플릿 + 컨트롤러 링크 점검)**  
   - 지정 템플릿 디렉터리와 관련 컨트롤러를 local-fs로 읽고, 템플릿의 `th:href`, `th:action`, form action, 버튼 링크를 추출한다.  
   - 컨트롤러의 `@GetMapping/@PostMapping/@RequestMapping` 목록을 정리하고, 링크와 비교해 존재하지 않는 URL, HTTP 메서드 불일치, path variable 오탈자를 "문제 목록"으로 기록한다.
2. **수정 계획 수립**  
   - 문제 목록별로 최소 변경 수정안을 정리하되 기존 네이밍/URL 패턴을 유지한다.  
   - 항목마다 대상 파일·줄, 변경 전/후 링크(또는 코드) 요약을 남긴다.
3. **실제 코드/템플릿 수정**  
   - 계획한 내용을 local-fs로 반영하며, 필요한 태그·메서드만 최소 범위로 수정한다.  
   - Bootstrap 레이아웃이 무너진 경우 row/col 짝과 정렬만 정돈하고, 기존 디자인 패턴을 유지한다.
4. **서버 실행(bootRun) 및 API 검증**  
   - 기존 인스턴스가 있다면 종료 후 `gradlew.bat bootRun`(또는 동등한 Gradle 명령)으로 기동한다.  
   - bootRun 오류(빌드 실패, 포트 충돌, Bean 오류 등)를 3~5줄 요약과 원인 가설로 정리하고 수정 후 재시도한다.  
   - 서버 기동 후 http-fetch로 모듈 대표 URL을 호출해 상태코드·응답을 확인하고, 4xx/5xx 발생 시 원인과 수정안을 정리한다.
5. **UI 수준 검증(가능 시)**  
   - puppeteer/Playwright MCP 사용이 가능하면 대표 화면에 접속해 DOM 요소와 주요 버튼·링크 상태를 확인하고, 클릭 시 오류 여부를 점검한다.  
   - 자바스크립트 오류나 심각한 레이아웃 깨짐이 발견되면 간단한 수정안을 제안하되, 위험할 경우 코드 제안만 남기고 실제 수정은 보류한다.  
   - 모든 단계가 끝나면 작업 내용을 `docs/changes/` 하위 Markdown으로 정리하고, 채팅 보고는 `“A 기능을 추가했고 B 기능을 수정했으며 관련 테스트와 검증을 완료했습니다.”` 형식 한 줄만 사용한다.

## 9. 워크플로우 실습 - counsel 모듈 (2025-11-18)
- 정적 분석 결과: counsel 템플릿의 Uppy 업로드 경로(`/counsel/upload-temp`)는 컨트롤러 매핑이 없어서 404 발생, 첨부파일 DTO에 다운로드 URL 미설정 → 뷰에서 링크 노출 시 무의미한 `#` 표시, 페이징/상세/수정 링크는 기존 매핑과 일치.
- 수정 계획: `CounselPostDto`에 AttachmentDto 목록을 설정할 때 downloadUrl을 채우도록 `CounselService.getDetail`에서 AttachmentMapper 주입 후 변환, `CounselPostWriteDto`에 `attachmentIds` 필드 추가해 Uppy 임시 업로드 ID 전달 준비, 추후 `/counsel/upload-temp` 엔드포인트 구현 계획.
- 코드 수정: `CounselService`에 AttachmentMapper를 주입해 조회 시 첨부파일 DTO에 `/counsel/download/{id}`를 설정, `CounselPostWriteDto`에 `attachmentIds` 컬렉션 추가.
- 서버 실행: `gradlew.bat bootRun`. 최초 시도 시 8080 포트 이미 사용 중이라 실패 → `Get-NetTCPConnection`으로 PID 25072 확인 후 `taskkill /PID 25072 /F`로 종료하고 재실행. 두 번째 시도는 Gradle이 응답을 돌려주지 않아(콘솔 무응답) 추가 진단 필요. (현재 bootRun 세션은 강제 종료 상태)
- API/UI 검증: 서버 정상 기동 전이라 http-fetch/브라우저 검증은 보류. 부팅 가능 상태 확보 후 `/counsel/list`, `/counsel/detail/{id}` 확인 예정.
- TODO: `/counsel/upload-temp` 컨트롤러 및 서비스 구현, bootRun 재시도 시 Gradle 프로세스 로그 재확인, http-fetch와 puppeteer 검증 단계 수행.

## 10. 관리자 설정 CSRF 토큰 누락 수정 (2025-11-18)
- 문제: 관리자 설정 페이지에서 멀티로그인 등 토글 액션 클릭 시 403 Forbidden 오류 발생. Spring Security가 POST 요청에 CSRF 토큰을 요구하나 JavaScript 동적 폼 제출 시 토큰이 누락됨.
- 수정: `fragments/layout.html`의 `<head>`에 CSRF 메타 태그(`<meta name="_csrf" th:content="${_csrf.token}"/>`, `<meta name="_csrf_header" th:content="${_csrf.headerName}"/>`) 추가, `admin/settings.html`의 `submitToggle()` 함수에서 메타 태그에서 토큰을 읽어 hidden input으로 폼에 추가하도록 수정.
- 결과: 토글 액션 실행 시 CSRF 토큰이 정상적으로 전송되어 403 오류 해결, 관리자는 멀티로그인 활성화/비활성화 등 모든 설정 변경 가능.

## 11. 온라인상담 게시판 UI 개선 및 관리자 설정 피드백 강화 (2025-11-18)
- 문제: 온라인상담 목록 페이지에서 검색 input(38px)과 버튼(미지정) 높이 불일치, 글쓰기 버튼 코드 중복 및 오류, 관리자 설정 변경 시 사용자 피드백 부족.
- 수정:
  1. `counsel/counselList.html`: 검색 input과 select 박스를 모두 42px로 통일, 검색 버튼에도 `height: 42px` 명시적 지정, 글쓰기 버튼 중복 코드 제거 및 깔끔한 레이아웃으로 정리.
  2. `admin/settings.html`: 성공 모달(`#successModal`) 추가하여 설정 변경 완료 시 화면 중앙에 "A기능 비활성화에 성공하였습니다." 형태의 메시지를 모달로 표시. Flash 메시지를 hidden 처리하고 페이지 로드 시 DOMContentLoaded 이벤트에서 모달 자동 표시.
- 결과: 온라인상담 검색 영역 UI 일관성 확보, 관리자가 설정 변경 시 직관적인 시각적 피드백 제공으로 사용자 경험 개선.

## 12. 글쓰기 버튼 및 FAQ 페이지 UI 개선 (2025-11-18)
- 문제: 
  1. `counsel-write.html` 헤더 목록 버튼 코드 중복 및 닫는 태그 오류
  2. FAQ 페이지 체크박스가 사용되지 않고 혼란 유발
  3. FAQ 검색 input과 버튼이 분리되어 일관성 부족
  4. select 박스와 input 높이 불일치
- 수정:
  1. `counsel-write.html`: 중복된 목록 버튼 코드 제거, custom-btn 클래스 사용, 42px 높이 통일
  2. `faqList.html`: 체크박스 및 삭제 버튼 완전 제거, 아코디언 클릭만으로 FAQ 내용 확인 가능
  3. `faqList.html`: 검색 input과 버튼을 `input-group`으로 그룹화하여 시각적 일관성 확보
  4. 모든 select 박스와 input, 버튼 높이를 42px로 통일
- 결과: FAQ 페이지가 더 직관적으로 변경되어 사용자가 질문을 클릭하면 바로 답변 확인 가능, 검색 영역 UI 일관성 확보, 전체적인 버튼/입력 요소 높이 통일로 깔끔한 레이아웃 완성.

## 13. Pagination Fragment 오류 수정 (2025-11-18)
- 문제: FAQ 페이지 접근 시 `org.springframework.expression.spel.SpelEvaluationException: EL1008E: Property or field 'currentPage' cannot be found on object of type 'org.springframework.data.domain.PageImpl'` 오류 발생. Spring Data의 `Page` 인터페이스는 `currentPage` 속성이 없고 `number` 속성을 사용함.
- 수정: `fragments/pagination.html`에서 `page.currentPage`를 모두 `page.number`로 변경, Thymeleaf 표현식에서 빼기 연산자 주변에 공백 추가 (`page.number-1` → `page.number - 1`), FAQ 페이지에서 사용하는 `category`와 `pageSize` 파라미터도 URL에 포함하도록 추가.
- 결과: FAQ 페이지 pagination이 정상 동작하고, 모든 게시판(counsel, community, faq)에서 페이징 기능 정상 작동 확인.

## 14. input-group 내 검색 input과 버튼 높이/정렬 표준화 (2025-11-18)
- 문제: templates 내 여러 화면에서 input-group 안의 검색 input과 버튼 높이가 불일치하고 정렬이 어긋남. 특히 `community/noticeList.html`에 중첩된 input-group 구조와 불필요한 div 중복 존재, 각 페이지마다 인라인 스타일(`style="height: 42px"`)을 반복 사용하여 유지보수 어려움.
- 수정:
  1. `custom-buttons.css`에 `.input-group` 전용 스타일 규칙 추가: input-group 내부의 `.custom-btn`, `.form-control`, `.form-select` 모두 자동으로 42px 높이 적용, border-radius도 자동 조정.
  2. `community/noticeList.html`: 중첩된 input-group 제거, row/col 그리드 구조로 깔끔하게 재구성, 글쓰기 버튼을 custom-btn으로 변경.
  3. `counsel/counselList.html`, `faq/faqList.html`: 모든 인라인 height 스타일 제거, CSS가 자동으로 높이를 관리하도록 변경.
  4. 모든 select 박스에서 인라인 height 스타일 제거.
- 결과: 
  - input-group 사용 시 별도 인라인 스타일 없이도 input/select/button이 자동으로 42px 높이로 통일.
  - 코드 중복 제거로 유지보수성 향상.
  - Bootstrap input-group의 표준 규칙을 준수하면서 프로젝트 일관성 확보.
  - 향후 새로운 검색 폼 추가 시 custom-btn과 input-group만 사용하면 자동으로 정렬 적용.

## 15. FAQ 검색 기능 강화 및 레이아웃 개선 (2025-11-18)
- 문제: FAQ 검색이 질문+답변을 모두 검색하는 단일 방식만 지원, 온라인상담 게시판처럼 제목/제목+내용 선택 불가능. 검색 조건 초기화 링크 위치가 왼쪽에 있어 직관적이지 않음.
- 수정:
  1. `FaqService.searchFaqs()`: type 파라미터 추가하여 "question"(제목만) 또는 "all"(제목+내용) 검색 지원.
  2. `FaqController.list()`: type 파라미터 받아서 Service에 전달, Model에 추가하여 뷰에서 선택 상태 유지.
  3. `faqList.html` 레이아웃 재구성:
     - 왼쪽: 10개씩 보기(pageSize), 카테고리 선택
     - 오른쪽: 검색 타입 선택(제목/제목+내용), 검색 input, 검색 버튼
     - 검색조건 초기화 링크를 검색 영역 바로 아래 우측 정렬로 이동
  4. JavaScript 함수(changeCategory, changePageSize) 유지: URL 파라미터 자동 보존.
- 결과: 
  - FAQ 검색 시 제목만 또는 제목+내용을 선택하여 검색 가능, 온라인상담 게시판과 일관된 UX.
  - 필터(pageSize, category)와 검색(type, keyword)이 좌우로 명확히 분리되어 직관적인 레이아웃.
  - 검색조건 초기화 링크가 검색 버튼 아래 우측에 위치해 사용자 경험 개선.

## 16. FAQ 화면 높이 통일, init 데이터 추가, pagination fragment 개선 (2025-11-18)
- 문제: 
  1. FAQ 검색 영역에서 select, input, button 높이가 row g-2 구조로 인해 미세하게 어긋남.
  2. FAQ init 데이터가 10개로 페이지네이션 테스트 부족.
  3. 온라인상담 화면에서 pagination fragment가 존재하지 않는 파라미터(subject, category 등)를 참조하여 UI 깨짐 발생.
- 수정:
  1. `faqList.html`: 검색 영역의 row g-2 구조 제거, `d-flex` + `gap: 8px`로 변경하여 모든 요소가 동일한 42px 높이로 정렬되도록 수정.
  2. `FaqDataInit.java`: "예방접종은 언제 받아야 하나요?" FAQ 추가하여 총 11개 데이터 생성, 페이지네이션 테스트 가능하도록 개선.
  3. `fragments/pagination.html`: 모든 파라미터를 `param.containsKey()` 조건으로 감싸서 optional 처리, 파라미터가 없는 페이지에서도 정상 동작하도록 개선.
- 결과:
  - FAQ 화면의 모든 검색 요소가 완벽하게 정렬되어 시각적 일관성 확보.
  - FAQ 11개 데이터로 10개씩 보기 시 2페이지가 생성되어 페이지네이션 정상 작동 테스트 가능.
  - pagination fragment가 모든 게시판(counsel, community, faq)에서 파라미터 존재 여부와 관계없이 안정적으로 동작.
  - 온라인상담 화면 UI 깨짐 현상 완전 해결.

## 17. Pagination Fragment Thymeleaf 오류 수정 및 온라인상담 에디터 추가 (2025-11-18)
- 문제: 
  1. `param.containsKey()` 메서드가 Thymeleaf에서 지원되지 않아 `TemplateInputException` 발생, 공지사항 및 온라인상담 페이지네이션 오류.
  2. 온라인상담 글쓰기 화면에 에디터 div만 존재하고 실제 에디터 라이브러리가 연결되지 않아 내용 작성 불가능.
  3. 첨부파일 업로드 UI(Uppy)는 구현되어 있으나 Progress Bar와 연동이 미흡.
- 수정:
  1. `fragments/pagination.html`: `param.containsKey()` 제거, Thymeleaf param은 직접 접근 시 없으면 빈 배열 반환하므로 조건 없이 `${param.subject}` 형태로 단순화.
  2. `counsel-write.html`: Quill Editor 라이브러리 추가 (CDN 방식), Quill 초기화 스크립트 작성하여 에디터 활성화.
  3. Quill Editor 설정: snow 테마, toolbar(헤더, 볼드, 이탤릭, 리스트, 색상, 정렬, 링크, 이미지 등) 포함.
  4. 폼 제출 시 Quill 내용을 hidden textarea(`#content`)에 HTML 형태로 동기화하여 서버 전송.
  5. Uppy 업로드 진행률이 Progress Bar에 실시간 표시되도록 `upload-progress` 이벤트 연동, 파일 업로드 완료 후 1초 뒤 Progress Bar 자동 숨김.
- 결과:
  - 공지사항, 온라인상담, FAQ 모든 페이지에서 페이지네이션 정상 작동.
  - 온라인상담 글쓰기 화면에서 Quill Editor를 통해 Rich Text 작성 가능 (텍스트 포맷, 색상, 링크, 이미지 삽입 등).
  - 파일 업로드 시 Progress Bar에 실시간 진행률(%) 표시, 사용자가 업로드 상태를 직관적으로 확인 가능.
  - 첨부파일 업로드 완료 후 파일 ID가 hidden input에 저장되어 폼 제출 시 서버로 전달.

## 18. PageResponse와 Spring Data Page 인터페이스 호환성 문제 해결 (2025-11-18)
- 문제: 
  1. `PageResponse` 객체가 `currentPage` 속성을 사용하지만, pagination fragment는 Spring Data `Page` 인터페이스의 `number` 속성을 참조.
  2. `EL1008E: Property or field 'number' cannot be found on object of type 'PageResponse'` 오류 발생.
  3. community(공지사항)와 counsel(온라인상담) 컨트롤러가 `PageResponse`를 사용하여 페이지네이션 완전 불가능.
- 수정:
  1. `PageResponse.java`: `getNumber()` 메서드 추가하여 `currentPage` 값을 반환, Spring Data `Page` 인터페이스 호환성 확보.
  2. pagination fragment가 `page.number`, `page.totalPages`, `page.size` 등을 호출할 때 `PageResponse`와 `Page` 객체 모두 정상 작동.
- 결과:
  - `PageResponse`를 사용하는 공지사항, 온라인상담 게시판에서 페이지네이션 정상 작동.
  - `Page` 객체를 직접 사용하는 FAQ 게시판도 기존처럼 정상 작동.
  - 단일 pagination fragment로 두 가지 페이징 객체 타입 모두 지원 가능.
  - `AuthorizationDeniedException` 오류는 페이지네이션 오류로 인한 2차 오류로, 근본 원인 해결로 함께 해소.

## 19. 온라인상담 글쓰기 및 목록 UI 개선 (2025-11-18)
- 문제:
  1. 온라인상담 글쓰기 페이지에서 Quill Editor가 화면에 표시되지 않아 내용 작성 불가능.
  2. Uppy 파일 업로드 UI가 ES Module 방식으로 구현되어 브라우저에서 import 실패, 파일 업로드 불가능.
  3. 온라인상담 목록 페이지에서 검색 타입 select와 검색 input의 높이가 미세하게 불일치 (row g-2 구조 문제).
  4. 검색 영역과 테이블 사이 간격이 너무 좁아 답답한 레이아웃.
- 수정:
  1. `counsel-write.html`: Quill Editor 스크립트가 있었으나 초기화 코드가 없었음 → DOMContentLoaded 이벤트에서 Quill 초기화 코드 추가.
  2. Uppy를 ES Module에서 CDN 전역 객체 방식으로 변경: `<script src="https://releases.transloadit.com/uppy/v3.3.1/uppy.min.js"></script>` 추가, `window.Uppy` 전역 객체 사용.
  3. Uppy Dashboard와 XHRUpload 플러그인을 `Uppy.Dashboard`, `Uppy.XHRUpload`로 참조하여 파일 업로드 UI 활성화.
  4. `counselList.html`: 검색 폼을 row g-2에서 `d-flex` + `gap: 8px`로 변경, select와 input-group이 동일한 42px 높이로 정렬.
  5. 테이블에 `mt-4` 클래스 추가하여 검색 영역과 테이블 사이 간격 확보.
- 결과:
  - 온라인상담 글쓰기 화면에서 Quill Editor가 정상 표시되어 Rich Text 작성 가능 (볼드, 색상, 리스트, 링크 등).
  - Uppy Dashboard가 화면에 표시되어 드래그&드롭 또는 파일 선택으로 첨부파일 업로드 가능, Progress Bar에 업로드 진행률 실시간 표시.
  - 온라인상담 목록 페이지의 검색 타입 select와 검색 input이 완벽하게 정렬되어 시각적 일관성 확보.
  - 검색 영역과 게시글 테이블 사이 여유 공간 추가로 깔끔한 레이아웃 완성.

## 20. Quill Editor 및 Uppy 오프라인 사용 가능하도록 로컬 내장 (2025-11-18)
- 문제:
  1. Quill Editor와 Uppy가 CDN 방식으로 로드되어 오프라인 환경에서 사용 불가능.
  2. Uppy를 npm으로 설치했으나 ES Module 방식만 제공되어 브라우저에서 직접 사용 불가능.
  3. 프로젝트 내장 방식으로 변경 필요.
- 수정:
  1. Quill Editor 로컬화:
     - `src/main/resources/static/js/quill/quill.js` (583KB)
     - `src/main/resources/static/css/quill/quill.snow.css` (24KB)
  2. Uppy 로컬화:
     - `src/main/resources/static/js/uppy/uppy.min.js` (533KB, 브라우저용 번들)
     - `src/main/resources/static/css/uppy/uppy.min.css` (88KB)
  3. `counsel-write.html`: CDN 링크를 Thymeleaf `th:src`, `th:href` 속성으로 로컬 경로 참조로 변경.
  4. PowerShell `Invoke-WebRequest`로 CDN에서 최종 번들 파일 다운로드 후 static 폴더에 배치.
- 결과:
  - 오프라인 환경에서도 Quill Editor와 Uppy 정상 작동.
  - 네트워크 없이 프로젝트 독립적으로 실행 가능.
  - 프로젝트 전체 크기: Quill(607KB) + Uppy(621KB) = 약 1.2MB 추가.
  - 향후 라이브러리 업데이트 시 static 폴더 파일만 교체하면 됨.
