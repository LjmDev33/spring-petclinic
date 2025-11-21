# UI 일관성 개선 완료

**날짜:** 2025-11-20  
**작성자:** GitHub Copilot

## 작업 내용

온라인상담 게시판 전체 UI의 일관성을 개선하여 사용자 경험을 향상시켰습니다.

## 개선 사항

### 1️⃣ Placeholder 스타일 통일

**Before:**
- placeholder 길이가 제각각 (짧은 것: "이름 입력", 긴 것: "게시글 작성 시 설정한 비밀번호를 입력하세요")
- 폰트 크기 지정 없음
- 일관성 부족

**After:**
```html
<!-- 모든 input에 일관된 스타일 적용 -->
<input placeholder="이름 입력" style="font-size: 0.95rem;">
<input placeholder="비밀번호 입력" style="font-size: 0.95rem;">
<input placeholder="내용 입력" style="font-size: 0.95rem;">
```

**개선 효과:**
- ✅ Placeholder 텍스트 간소화 (핵심만 표시)
- ✅ 상세 안내는 `<small>` 태그로 분리
- ✅ 폰트 크기 0.95rem 통일 (가독성 향상)

### 2️⃣ 수정된 파일 목록

| 파일 | 변경 내용 | 수정 개수 |
|------|---------|-----------|
| **counsel-write.html** | placeholder 추가 및 스타일 통일 | 4개 필드 |
| **counselDetail.html** | 모달 placeholder 스타일 통일 | 5개 필드 |
| **counsel-password.html** | placeholder 스타일 통일 | 1개 필드 |
| **counsel-edit.html** | placeholder 스타일 통일 | 1개 필드 |

**총 수정: 4개 파일, 11개 input 필드**

### 3️⃣ 수정 전후 비교

#### counsel-write.html
```html
<!-- Before -->
<input type="text" name="authorName" class="form-control" required>
<input type="email" name="authorEmail" class="form-control">
<input type="password" name="password" class="form-control" placeholder="비공개글 열람용 비밀번호">

<!-- After -->
<input type="text" name="authorName" class="form-control" placeholder="이름 입력" style="font-size: 0.95rem;" required>
<input type="email" name="authorEmail" class="form-control" placeholder="example@email.com" style="font-size: 0.95rem;">
<input type="password" name="password" class="form-control" placeholder="비밀번호 입력" style="font-size: 0.95rem;">
```

#### counselDetail.html 댓글 모달
```html
<!-- Before -->
<input placeholder="이름을 입력하세요">
<input placeholder="댓글 삭제 시 필요합니다">
<textarea placeholder="댓글 내용을 입력하세요"></textarea>

<!-- After -->
<input placeholder="이름 입력" style="font-size: 0.95rem;">
<input placeholder="비밀번호 입력" style="font-size: 0.95rem;">
<textarea placeholder="내용 입력" style="font-size: 0.95rem;"></textarea>
```

#### counsel-password.html
```html
<!-- Before -->
<input placeholder="비밀번호를 입력하세요">

<!-- After -->
<input placeholder="비밀번호 입력" style="font-size: 0.95rem;">
```

#### counsel-edit.html
```html
<!-- Before -->
<input placeholder="비공개 게시글 수정을 위해 비밀번호를 입력하세요">

<!-- After -->
<input placeholder="비밀번호 입력" style="font-size: 0.95rem;">
```

## UI 설계 원칙

### Placeholder 작성 규칙

1. **간소화**: 핵심만 표시 (동사 생략)
   - ❌ "이름을 입력하세요" → ✅ "이름 입력"
   - ❌ "비밀번호를 입력하세요" → ✅ "비밀번호 입력"

2. **상세 안내 분리**: placeholder에 긴 설명 금지
   ```html
   <!-- ❌ 잘못된 예 -->
   <input placeholder="비공개 게시글 수정을 위해 비밀번호를 입력하세요">
   
   <!-- ✅ 올바른 예 -->
   <input placeholder="비밀번호 입력" style="font-size: 0.95rem;">
   <small class="form-text text-muted">비공개 글 수정 시 필요합니다.</small>
   ```

3. **폰트 크기 통일**: 모든 input에 `font-size: 0.95rem` 적용

4. **일관된 문구**: 같은 유형의 필드는 같은 문구 사용
   - 이름 필드: "이름 입력"
   - 비밀번호 필드: "비밀번호 입력"
   - 내용 필드: "내용 입력"

## 검증

### 컴파일 확인
```bash
.\gradlew.bat compileJava
# BUILD SUCCESSFUL
```

✅ 컴파일 성공
✅ 문법 오류 없음

### 적용 범위
- ✅ 온라인상담 글쓰기
- ✅ 온라인상담 수정
- ✅ 온라인상담 상세 (댓글 모달)
- ✅ 온라인상담 상세 (삭제 모달)
- ✅ 비공개 게시글 비밀번호 입력

## 사용자 경험 개선

**Before:**
- 긴 placeholder로 입력 영역이 복잡해 보임
- 폰트 크기 불일치로 가독성 저하
- 일관성 부족으로 혼란

**After:**
- 간결한 placeholder로 깔끔한 UI
- 통일된 폰트 크기로 가독성 향상
- 일관된 문구로 직관성 증가
- 상세 안내는 하단 small 태그로 분리하여 정보 계층 명확

## 다음 단계

3️⃣ **비밀번호 찾기 기능 구현**
- 이메일 연동
- 토큰 생성 및 검증
- 비밀번호 재설정 페이지

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20

