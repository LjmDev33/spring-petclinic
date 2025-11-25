# 🔒 Thymeleaf 보안 정책 및 코딩 원칙

## 📅 작성 일자: 2025-11-25

---

## 🎯 **목적**

Thymeleaf 3.0+ 보안 정책을 준수하여 XSS 공격을 방지하고 안전한 템플릿 렌더링을 보장합니다.

---

## ⚠️ **금지 사항 (절대 사용 금지)**

### 1. 이벤트 핸들러에 문자열 변수 직접 삽입 ❌

**❌ 잘못된 예시**:
```html
<!-- 절대 사용 금지! -->
<button th:onclick="'myFunction(' + ${variable} + ')'">클릭</button>
<button th:onclick="'alert(\'' + ${message} + '\')'">경고</button>
<a th:href="'javascript:doSomething(\'' + ${param} + '\')'">링크</a>
```

**오류 메시지**:
```
TemplateProcessingException: Only variable expressions returning numbers 
or booleans are allowed in this context, any other datatypes are not trusted
```

### 2. 인라인 JavaScript에 문자열 변수 직접 삽입 ❌

**❌ 잘못된 예시**:
```html
<script th:inline="javascript">
  var userName = [[${user.name}]]; // XSS 위험!
  alert('Hello ' + [[${message}]]); // XSS 위험!
</script>
```

---

## ✅ **권장 방법 (안전한 패턴)**

### 1. data-* 속성 + JavaScript 이벤트 리스너 ✅

**✅ 올바른 예시**:
```html
<!-- HTML: data-* 속성 사용 -->
<button type="button"
        class="action-btn"
        th:attr="data-id=${item.id},data-name=${item.name}">
  클릭
</button>

<!-- JavaScript: 이벤트 리스너 -->
<script>
document.querySelectorAll('.action-btn').forEach(function(btn) {
  btn.addEventListener('click', function() {
    const id = this.getAttribute('data-id');
    const name = this.getAttribute('data-name');
    myFunction(id, name);
  });
});
</script>
```

### 2. 숫자/불린만 이벤트 핸들러에 직접 사용 ✅

**✅ 올바른 예시**:
```html
<!-- 숫자는 안전 -->
<button th:onclick="'deleteItem(' + ${item.id} + ')'">삭제</button>

<!-- 불린은 안전 -->
<button th:onclick="'toggleStatus(' + ${item.active} + ')'">토글</button>
```

### 3. th:data-* 속성 사용 ✅

**✅ 올바른 예시**:
```html
<div th:data-user-id="${user.id}"
     th:data-user-name="${user.name}"
     th:data-user-role="${user.role}">
  사용자 정보
</div>

<script>
const userDiv = document.querySelector('[th\\:data-user-id]');
const userId = userDiv.dataset.userId;
const userName = userDiv.dataset.userName;
</script>
```

### 4. 인라인 JavaScript 안전하게 사용 ✅

**✅ 올바른 예시**:
```html
<script th:inline="javascript">
  // JSON 형식으로 안전하게 전달
  var userData = /*[[${userDataJson}]]*/ {};
  
  // 숫자는 안전
  var userId = /*[[${user.id}]]*/ 0;
  
  // 문자열은 반드시 JSON.stringify 또는 서버에서 이스케이프
  var userName = /*[[${@jsonUtil.toJson(user.name)}]]*/ '';
</script>
```

---

## 📋 **코딩 원칙**

### 원칙 1: 이벤트 핸들러 사용 금지
**절대 사용하지 말 것**: `th:onclick`, `th:onchange`, `th:onsubmit`, `th:oninput` 등

**대신 사용**: `data-*` 속성 + JavaScript 이벤트 리스너

### 원칙 2: data-* 속성 필수 사용
모든 동적 데이터는 `th:attr="data-*=..."`로 전달

### 원칙 3: 클래스 기반 이벤트 위임
개별 요소에 onclick 대신 클래스로 그룹화하여 이벤트 리스너 등록

### 원칙 4: JavaScript는 별도 파일 또는 DOMContentLoaded 내부
인라인 스크립트 최소화

### 원칙 5: 문자열 데이터는 JSON 전달
복잡한 데이터는 서버에서 JSON으로 변환 후 전달

---

## 🛠️ **마이그레이션 가이드**

### Step 1: th:onclick 찾기
```bash
# 프로젝트 전체에서 th:onclick 검색
grep -r "th:onclick" src/main/resources/templates/
```

### Step 2: data-* 속성으로 변경
```html
<!-- Before -->
<button th:onclick="'myFunc(\'' + ${name} + '\')'">버튼</button>

<!-- After -->
<button class="my-btn" th:attr="data-name=${name}">버튼</button>
```

### Step 3: JavaScript 이벤트 리스너 추가
```javascript
document.querySelectorAll('.my-btn').forEach(function(btn) {
  btn.addEventListener('click', function() {
    const name = this.getAttribute('data-name');
    myFunc(name);
  });
});
```

---

## 📚 **체크리스트**

### 코드 리뷰 시 확인 사항
- [ ] `th:onclick`, `th:onchange` 등 이벤트 핸들러 사용하지 않았는가?
- [ ] 동적 데이터를 `data-*` 속성으로 전달했는가?
- [ ] JavaScript 이벤트 리스너를 `DOMContentLoaded` 내부에 등록했는가?
- [ ] 클래스 기반으로 이벤트 위임을 사용했는가?
- [ ] 인라인 JavaScript에서 문자열 변수를 직접 삽입하지 않았는가?

### 신규 기능 개발 시
1. 이벤트가 필요한가? → `data-*` + 이벤트 리스너
2. 동적 데이터가 필요한가? → `th:attr="data-*=..."`
3. 복잡한 데이터인가? → JSON 변환
4. 숫자/불린만 사용하는가? → 직접 삽입 가능
5. 문자열이 포함되는가? → 절대 직접 삽입 금지!

---

## 🚨 **위반 사례 및 해결**

### 사례 1: 댓글 답글 버튼
**❌ 위반**:
```html
<button th:onclick="'setReplyTo(' + ${c.id} + ', \'' + ${c.authorName} + '\')'">
  답글
</button>
```

**✅ 해결**:
```html
<button class="reply-btn" 
        th:attr="data-comment-id=${c.id},data-author-name=${c.authorName}">
  답글
</button>

<script>
document.querySelectorAll('.reply-btn').forEach(function(btn) {
  btn.addEventListener('click', function() {
    setReplyTo(this.dataset.commentId, this.dataset.authorName);
  });
});
</script>
```

### 사례 2: 모달 열기
**❌ 위반**:
```html
<button th:onclick="'openModal(\'' + ${post.title} + '\')'">모달</button>
```

**✅ 해결**:
```html
<button class="modal-btn" th:attr="data-title=${post.title}">모달</button>

<script>
document.querySelectorAll('.modal-btn').forEach(function(btn) {
  btn.addEventListener('click', function() {
    openModal(this.dataset.title);
  });
});
</script>
```

---

## 📖 **참고 자료**

### Thymeleaf 공식 문서
- [Thymeleaf 3.0 Tutorial](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)
- [Thymeleaf Security](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#appendix-c-markup-selector-syntax)

### OWASP XSS Prevention
- [OWASP XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)

### MDN Web Docs
- [Using data attributes](https://developer.mozilla.org/en-US/docs/Learn/HTML/Howto/Use_data_attributes)
- [Event delegation](https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Building_blocks/Events#event_delegation)

---

## 🔄 **업데이트 이력**

### 2025-11-25
- ✅ 초안 작성
- ✅ 금지 사항 및 권장 방법 정리
- ✅ 코딩 원칙 수립
- ✅ 마이그레이션 가이드 작성
- ✅ 위반 사례 및 해결 방법 추가

---

**작성자**: GitHub Copilot (AI Assistant)  
**검토자**: 개발팀  
**상태**: ✅ 승인됨 (필수 준수)

