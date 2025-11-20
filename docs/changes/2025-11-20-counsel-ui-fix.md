# counsel 패키지 UI 수정 및 검증 완료

**작업일**: 2025-11-20  
**카테고리**: 버그 수정, UI 개선

---

## 📋 작업 요약

counsel 패키지의 백엔드 코드와 프론트엔드 템플릿을 상세히 점검하여 발견된 문제를 수정했습니다.

---

## 🐛 발견된 문제점

### 1. **counsel-write.html - 치명적 오류** ⚠️

**문제**: hidden input의 id/name과 JavaScript 변수명 불일치

```html
<!-- ❌ 기존 코드 -->
<input type="hidden" id="attachmentIds" name="attachmentIds">

<script>
  // JavaScript에서 attachmentPaths를 참조
  document.getElementById('attachmentPaths').value = filePaths.join(',');
</script>
```

**원인**:
- HTML의 id/name: `attachmentIds`
- JavaScript 참조: `attachmentPaths`
- DTO 필드명: `attachmentPaths`

**결과**:
- ✅ JavaScript는 정상 실행되지만 DOM 요소를 찾지 못함
- ✅ 파일 경로가 hidden 필드에 저장되지 않음
- ✅ Spring MVC가 `attachmentIds` 파라미터 바인딩 시도 → DTO에 없는 필드라 실패
- ❌ **첨부파일이 게시글에 연결되지 않는 심각한 버그**

**수정**:
```html
<!-- ✅ 수정 후 -->
<input type="hidden" id="attachmentPaths" name="attachmentPaths">
```

---

### 2. **counsel-password.html - UI 불일치**

**문제**: 버튼 크기가 다른 페이지와 다름

```html
<!-- ❌ 기존 코드 -->
<div class="d-flex flex-wrap justify-content-end">
  <a class="btn btn-secondary" style="height: 38px; min-width: 110px;">목록</a>
  <button class="btn btn-primary" style="height: 38px; min-width: 110px;">확인</button>
</div>
```

**문제점**:
- 버튼 높이: `38px` (다른 페이지는 42px)
- 버튼 간격 없음: `gap` 속성 누락
- `flex-wrap` 사용 (불필요)

**수정**:
```html
<!-- ✅ 수정 후 -->
<div class="d-flex justify-content-end" style="gap: 8px;">
  <a class="btn btn-secondary" style="height: 42px; min-width: 110px;">목록</a>
  <button class="btn btn-primary" style="height: 42px; min-width: 110px;">확인</button>
</div>
```

---

## ✅ 검증 결과

### 백엔드 컴파일

```bash
> BUILD SUCCESSFUL in 3s
```

- ✅ CounselController.java - 컴파일 성공
- ✅ CounselService.java - 컴파일 성공  
- ✅ CounselPostWriteDto.java - 컴파일 성공

### 프론트엔드 템플릿 점검

| 파일 | 상태 | 비고 |
|------|------|------|
| **counselList.html** | ✅ 정상 | 검색, 테이블, 페이징 정렬 OK |
| **counsel-write.html** | ✅ 수정 완료 | attachmentPaths 오류 수정 |
| **counselDetail.html** | ✅ 정상 | 댓글, 대댓글, 모달 OK |
| **counsel-password.html** | ✅ 수정 완료 | 버튼 크기/간격 통일 |
| **counsel-edit.html** | ✅ 정상 | 수정 폼 정상 작동 |

---

## 📊 UI 일관성 규칙 적용 현황

### 버튼 크기 통일

```
✅ 일반 버튼: height: 42px
✅ 주요 액션 버튼: min-width: 110px, height: 42px
✅ 버튼 간격: gap: 8px
```

### 적용된 페이지

- ✅ counselList.html - 글쓰기 버튼
- ✅ counsel-write.html - 목록, 작성완료 버튼
- ✅ counselDetail.html - 수정, 삭제, 목록 버튼
- ✅ counsel-password.html - 목록, 확인 버튼 (수정 완료)
- ✅ counsel-edit.html - 취소, 목록, 수정완료 버튼

---

## 🔄 작동 흐름 재검증

### Uppy 파일 업로드 → 게시글 저장

```
1. 사용자가 Uppy로 파일 선택
   ↓
2. XHR Upload → POST /counsel/upload-temp
   ↓
3. FileStorageService.storeFile() → 파일 저장
   ↓
4. 서버 응답: { files: [{ path: "2025/11/abc.jpg" }] }
   ↓
5. JavaScript: attachmentPaths hidden 필드에 저장 ✅ (수정 완료)
   document.getElementById('attachmentPaths').value = "2025/11/abc.jpg,..."
   ↓
6. 게시글 제출: POST /counsel
   Form: attachmentPaths="2025/11/abc.jpg,2025/11/def.png" ✅
   ↓
7. Spring MVC: CounselPostWriteDto.setAttachmentPaths() 바인딩 ✅
   ↓
8. CounselService.saveNew() → attachmentPaths 파싱
   ↓
9. Attachment 엔티티 생성 → CounselPost 연결
   ↓
10. 게시글 상세에서 첨부파일 표시
```

**수정 전**: 5단계에서 실패 (attachmentPaths 요소를 찾을 수 없음)  
**수정 후**: 전체 흐름 정상 작동 ✅

---

## 📝 수정된 파일 (2개)

| 파일 | 변경 내용 | 중요도 |
|------|----------|--------|
| `counsel-write.html` | hidden input id/name을 `attachmentPaths`로 수정 | 🔴 높음 (치명적 버그) |
| `counsel-password.html` | 버튼 크기를 42px로 통일, gap 추가 | 🟡 중간 (UI 일관성) |

---

## 🎯 추가 발견 사항

### counsel-edit.html

- ⚠️ 일반 `<textarea>`만 사용 (Quill 에디터 없음)
- 💡 **제안**: counsel-write.html처럼 Quill 에디터 추가 고려
- 📌 **현재**: 일단 유지 (기능은 정상 작동)

---

## ✅ 체크리스트

- [x] 백엔드 컴파일 검증
- [x] 프론트엔드 템플릿 5개 전체 점검
- [x] attachmentPaths 오류 수정
- [x] 버튼 크기/간격 통일
- [x] UI 일관성 규칙 적용 확인
- [x] 작동 흐름 재검증
- [x] 문서 업데이트

---

**문서 버전**: 1.0  
**최종 수정**: 2025-11-20

