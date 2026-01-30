/**
 * Quill Editor Toolbar Tooltip Utility (Updated)
 * 설명: 퀼 에디터 툴바 버튼에 마우스 오버 시 한글 설명(title)을 표시함
 * 업데이트: 2026-01-30 (글자크기, 인용구, 들여쓰기 추가)
 */
function addToolbarTooltips() {
    // 툴바 구성요소와 보여줄 한글 설명 매핑
    var tooltipMap = {
        'size': '글자 크기',                  // [추가]
        'header': '문단 제목 (Header)',       // [수정] 의미 명확화
        'bold': '굵게 (Bold)',
        'italic': '기울임꼴 (Italic)',
        'underline': '밑줄 (Underline)',
        'strike': '취소선 (Strike)',
        'blockquote': '인용구',               // [추가]
        'list[value="ordered"]': '번호 매기기',
        'list[value="bullet"]': '글머리 기호',
        'indent[value="-1"]': '내어쓰기',     // [추가]
        'indent[value="+1"]': '들여쓰기',     // [추가]
        'color': '글자 색상',
        'background': '배경 색상',
        'align': '정렬',
        'link': '링크 삽입',
        'image': '이미지 업로드',
        'video': '동영상 링크 삽입',          // [수정] 기능에 맞게 설명 변경
        'clean': '서식 지우기'
    };

    // 맵에 정의된 키를 순회하며 툴팁 적용
    for (var key in tooltipMap) {
        if (tooltipMap.hasOwnProperty(key)) {
            // 해당 클래스를 가진 모든 버튼/피커를 찾음 (ql- 접두사 자동 처리)
            var selector = '.ql-' + key;
            var elements = document.querySelectorAll(selector);

            // IE11 호환성을 위해 for 루프 사용
            for (var i = 0; i < elements.length; i++) {
                // 이미 title이 있는 경우 건너뛰기 (중복 적용 방지)
                if (!elements[i].getAttribute('title')) {
                    elements[i].setAttribute('title', tooltipMap[key]);
                }
            }
        }
    }
}
