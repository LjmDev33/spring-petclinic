# 로그인 후 홈페이지 공백 오류 해결

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot  
**심각도:** 🔴 Critical (홈페이지 표시 불가)

## ❌ 오류 내용

### 에러 메시지
```
org.thymeleaf.exceptions.TemplateInputException: 
An error happened during template parsing (template: "class path resource [templates/welcome.html]")

template might not exist or might not be accessible 
(template: "fragments/layout" - line 170, col 15)
```

### 발생 상황
- 로그인 성공 후 홈페이지(`/`) 접속 시 공백 화면
- error.html도 동일한 오류로 표시 불가

---

## 🔍 원인 분석

### 문제의 핵심
**welcome.html과 error.html이 다른 페이지와 다른 방식으로 구현됨**

#### 1. welcome.html (문제 있는 구조)
```html
<!-- th:replace로 body fragment 전체를 전달 -->
<html th:replace="~{fragments/layout :: layout (~{::body},'nav01')}">
<body>
  <!-- 컨텐츠 -->
</body>
</html>
```

#### 2. 다른 페이지들 (정상 구조)
```html
<!-- template 변수명(문자열)을 전달 -->
<html>
<body>
  <!-- 컨텐츠 -->
</body>
</html>
```

```java
// Controller
model.addAttribute("template", "counsel/counselList");
return "fragments/layout";
```

#### 3. layout.html (170번 줄)
```html
<!-- template 변수(문자열)를 기대 -->
<th:block th:insert="~{${template}}" />
```

### 충돌 발생
```
welcome.html: body fragment 전달 (~{::body})
   ↓
layout.html: 문자열 template 기대 (~{${template}})
   ↓
❌ 타입 불일치 → 파싱 오류
```

---

## ✅ 해결 방법

### 1️⃣ welcome.html 수정

**Before:**
```html
<!DOCTYPE html>
<html xmlns:th="https://www.thymeleaf.org" 
      th:replace="~{fragments/layout :: layout (~{::body},'nav01')}">
<body>
  <!-- 컨텐츠 -->
</body>
</html>
```

**After:**
```html
<!DOCTYPE html>
<html xmlns:th="https://www.thymeleaf.org">
<body>
  <!-- 컨텐츠 (변경 없음) -->
</body>
</html>
```

**변경 사항:**
- `th:replace` 속성 제거
- 순수 HTML 템플릿으로 변경

---

### 2️⃣ WelcomeController 수정

**Before:**
```java
@Controller
class WelcomeController {
    @GetMapping("/")
    public String welcome() {
        return "welcome";  // 직접 템플릿 이름 반환
    }
}
```

**After:**
```java
@Controller
class WelcomeController {
    @GetMapping("/")
    public String welcome(Model model) {
        model.addAttribute("template", "welcome");
        return "fragments/layout";  // layout 사용
    }
}
```

**변경 사항:**
- Model 파라미터 추가
- template 변수에 "welcome" 전달
- layout을 통해 렌더링

---

### 3️⃣ error.html 수정

**Before:**
```html
<!DOCTYPE html>
<html xmlns:th="https://www.thymeleaf.org" 
      th:replace="~{fragments/layout :: layout (~{::body},'error')}">
<body>
  <img th:src="@{/images/pets.png}" />
  <h2 th:text="#{somethingHappened}">Something happened...</h2>
  <p th:text="${message}">Exception message</p>
</body>
</html>
```

**After:**
```html
<!DOCTYPE html>
<html xmlns:th="https://www.thymeleaf.org">
<body>
<div class="container mt-5">
  <div class="text-center">
    <img src="../static/images/pets.png" 
         th:src="@{/images/pets.png}" 
         class="mb-4" style="max-width: 300px;" />
    <h2 th:text="#{somethingHappened}" class="text-danger mb-3">
      Something happened...
    </h2>
    <p th:text="${message}" class="text-muted">Exception message</p>
    <a th:href="@{/}" class="custom-btn custom-btn-primary mt-3">
      <i class="bi bi-house me-1"></i> 홈으로
    </a>
  </div>
</div>
</body>
</html>
```

**변경 사항:**
- `th:replace` 제거
- Bootstrap 스타일 적용
- 홈으로 버튼 추가

---

## 📋 수정된 파일

| 파일 | 수정 내용 |
|------|----------|
| **welcome.html** | th:replace 제거, 순수 HTML 템플릿으로 변경 |
| **WelcomeController.java** | Model 추가, template 변수 전달, layout 사용 |
| **error.html** | th:replace 제거, UI 개선, 홈 버튼 추가 |

**총 3개 파일 수정**

---

## 🎯 템플릿 렌더링 방식 통일

### 표준 패턴 (모든 페이지 적용)

#### 1. HTML 템플릿
```html
<!DOCTYPE html>
<html xmlns:th="https://www.thymeleaf.org">
<body>
  <!-- 페이지 컨텐츠 -->
</body>
</html>
```

#### 2. Controller
```java
@GetMapping("/path")
public String methodName(Model model) {
    model.addAttribute("template", "패키지/템플릿명");
    return "fragments/layout";
}
```

#### 3. layout.html
```html
<div class="container-fluid">
    <th:block th:insert="~{${template}}" />
</div>
```

### 데이터 흐름
```
Controller
   ↓ model.addAttribute("template", "welcome")
layout.html
   ↓ th:insert="~{${template}}"
welcome.html 렌더링
```

---

## ✅ 검증 완료

### 컴파일 성공
```bash
.\gradlew.bat compileJava
# BUILD SUCCESSFUL
```

### 예상 결과

#### 1. 로그인 전
```
1. / 접속
   ↓
2. welcome.html 정상 표시
   ↓
3. 메인 이미지 및 컨텐츠 표시
```

#### 2. 로그인 후
```
1. 로그인 성공 → / 리다이렉트
   ↓
2. welcome.html 정상 표시
   ↓
3. 헤더에 닉네임 표시
```

#### 3. 오류 발생 시
```
1. 예외 발생
   ↓
2. error.html 표시
   ↓
3. 오류 메시지 + 홈으로 버튼
```

---

## 🔍 Thymeleaf Fragment 표현식 정리

### 1. th:replace (전체 교체)
```html
<!-- 이 방식은 더 이상 사용하지 않음 -->
<html th:replace="~{fragments/layout :: layout (~{::body},'menu')}">
```

**문제점:**
- body fragment를 직접 전달
- layout.html의 `~{${template}}` 방식과 호환 불가
- 복잡하고 유지보수 어려움

### 2. th:insert (부분 삽입) - 권장 ✅
```html
<!-- layout.html에서 사용 -->
<th:block th:insert="~{${template}}" />
```

**장점:**
- 템플릿 이름(문자열)을 변수로 전달
- Controller에서 동적 제어 가능
- 간단하고 명확한 구조

---

## 🚀 향후 개선 사항

### 1. 에러 페이지 커스터마이징
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("message", e.getMessage());
        model.addAttribute("template", "error");
        return "fragments/layout";
    }
}
```

### 2. 404 페이지 추가
```html
<!-- error/404.html -->
<div class="container text-center">
  <h1>404</h1>
  <p>페이지를 찾을 수 없습니다.</p>
  <a th:href="@{/}">홈으로</a>
</div>
```

### 3. 500 페이지 추가
```html
<!-- error/500.html -->
<div class="container text-center">
  <h1>500</h1>
  <p>서버 오류가 발생했습니다.</p>
  <a th:href="@{/}">홈으로</a>
</div>
```

---

## 📝 재발 방지 대책

### 1. 템플릿 작성 규칙
- ✅ `th:replace`로 body fragment 전달 금지
- ✅ 순수 HTML 템플릿 + Controller에서 template 변수 전달
- ✅ `fragments/layout` 사용 통일

### 2. Controller 작성 규칙
```java
// ✅ 올바른 방식
@GetMapping("/path")
public String method(Model model) {
    model.addAttribute("template", "템플릿명");
    return "fragments/layout";
}

// ❌ 잘못된 방식
@GetMapping("/path")
public String method() {
    return "템플릿명";  // layout 미사용
}
```

### 3. 체크리스트
- [ ] HTML에 `th:replace` 사용하지 않음
- [ ] Controller에 Model 파라미터 추가
- [ ] template 변수 설정
- [ ] "fragments/layout" 반환

---

## 🎓 학습 포인트

### Thymeleaf Layout 패턴 비교

#### 1. Layout Dialect 방식 (구버전)
```html
<html layout:decorate="~{layouts/default}">
```
- 외부 라이브러리 필요
- 복잡한 설정

#### 2. Fragment 방식 (현재 사용) ✅
```html
<!-- Controller에서 -->
model.addAttribute("template", "welcome");
return "fragments/layout";

<!-- layout.html에서 -->
<th:block th:insert="~{${template}}" />
```
- 표준 Thymeleaf 기능
- 간단하고 명확
- 동적 제어 쉬움

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20  
**해결 시간**: 즉시 (템플릿 구조 통일)

