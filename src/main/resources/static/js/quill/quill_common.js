/**
 * Quill Editor용 이미지 업로드 핸들러 생성 함수 (Vanilla JS 버전)
 * - jQuery 의존성 제거 ($ is not defined 해결)
 * - IE11 호환성 준수 (XMLHttpRequest 사용)
 *
 * @param {String} domainType - 게시판 종류 ('photo', 'counsel', 'notice' 등)
 * @returns {Function} Quill 툴바 핸들러 함수
 */
function getQuillImageHandler(domainType) {
    return function() {
        // [핵심 해결] Quill 인스턴스를 'this'에서 가져와 변수에 저장
        // 핸들러 함수 내부에서 'this'는 Quill Toolbar 모듈을 가리키며,
        // 'this.quill'로 에디터 객체에 접근할 수 있습니다.
        var editor = this.quill;

        // 1. 파일 선택창 생성
        var input = document.createElement('input');
        input.setAttribute('type', 'file');
        input.setAttribute('accept', 'image/*');
        input.click();

        // 2. 파일 선택 시 동작
        input.onchange = function() {
            var file = input.files[0];
            if (!file) return;

            // [유효성 검사] 용량 체크 (10MB)
            var maxSize = 10 * 1024 * 1024;
            if (file.size > maxSize) {
                alert('이미지는 10MB 이하만 업로드 가능합니다.');
                return;
            }

            // 3. 전송 데이터 구성
            var formData = new FormData();
            formData.append('file', file);
            formData.append('domain', domainType);

            // 4. AJAX 요청 (XMLHttpRequest 사용)
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/api/files/upload', true);

            // ============================================================
            // [추가] CSRF 토큰 설정 (Spring Security 403 에러 해결)
            // ============================================================
            var csrfToken = document.querySelector("meta[name='_csrf']");
            var csrfHeader = document.querySelector("meta[name='_csrf_header']");

            if (csrfToken && csrfHeader) {
                xhr.setRequestHeader(csrfHeader.getAttribute("content"), csrfToken.getAttribute("content"));
            }
            // ============================================================

            // IE11 호환성을 위해 onload 사용
            xhr.onload = function() {
                if (xhr.status === 200) {
                    // 성공 시 JSON 파싱
                    try {
                        var response = JSON.parse(xhr.responseText);

                        // [수정] global 'quill' 변수 대신 위에서 저장한 'editor' 변수 사용
                        // 파일 선택창 사용 후 포커스가 풀릴 수 있으므로 getSelection(true) 사용 권장
                        var range = editor.getSelection(true);

                        // range가 null일 경우(포커스 완전 상실) 대비, 맨 끝으로 설정하거나 처리
                        if (!range) {
                            range = { index: editor.getLength(), length: 0 };
                        }

                        // 에디터에 이미지 삽입
                        editor.insertEmbed(range.index, 'image', response.url);
                        editor.setSelection(range.index + 1);

                        // [추가] 업로드된 파일 ID를 폼에 담기 (글 저장 시 연결용)
                        // 주의: HTML에 id="hidden-file-area" 인 div나 span이 있어야 합니다.
                        var hiddenArea = document.getElementById('hidden-file-area');
                        if (hiddenArea) {
                            var hiddenInput = document.createElement('input');
                            hiddenInput.setAttribute('type', 'hidden');
                            hiddenInput.setAttribute('name', 'attachmentIds');
                            hiddenInput.setAttribute('value', response.id);
                            hiddenArea.appendChild(hiddenInput);
                        }

                        console.log("[" + domainType + "] 업로드 성공:", response.url);

                    } catch (e) {
                        console.error('JSON 파싱 에러:', e);
                        alert('서버 응답을 처리하는 중 오류가 발생했습니다.');
                    }
                } else {
                    // 실패 시
                    console.error('업로드 실패:', xhr.status, xhr.statusText);
                    alert('이미지 업로드에 실패했습니다. (상태코드: ' + xhr.status + ')');
                }
            };

            xhr.onerror = function() {
                alert('네트워크 오류로 업로드에 실패했습니다.');
            };

            // 전송 시작
            xhr.send(formData);
        };
    };
}
