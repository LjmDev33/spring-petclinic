/**
 * 오류 알림 시스템 JavaScript
 * - Toast 알림 생성 및 관리
 * - 오류 상세 모달 표시
 * - 자동 로그 수집
 *
 * @author Jeongmin Lee
 * @description 사용자 친화적인 오류 메시지 표시 시스템
 */

const ErrorNotification = (function() {
    'use strict';

    // Toast 컨테이너 초기화
    function initToastContainer() {
        if (!document.getElementById('toastContainer')) {
            const container = document.createElement('div');
            container.id = 'toastContainer';
            document.body.appendChild(container);
        }
    }

    // 오류 모달 초기화
    function initErrorModal() {
        if (!document.getElementById('errorDetailModal')) {
            const modalHTML = `
                <div class="modal fade error-detail-modal" id="errorDetailModal" tabindex="-1" aria-hidden="true">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">
                                    <i class="bi bi-exclamation-triangle-fill"></i>
                                    <span id="errorModalTitle">오류 상세 정보</span>
                                </h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                            </div>
                            <div class="modal-body" id="errorModalBody">
                                <!-- 동적으로 채워짐 -->
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="custom-btn custom-btn-secondary" data-bs-dismiss="modal">
                                    <i class="bi bi-x-circle me-1"></i> 닫기
                                </button>
                                <button type="button" class="custom-btn custom-btn-primary" onclick="ErrorNotification.copyErrorToClipboard()">
                                    <i class="bi bi-clipboard me-1"></i> 오류 복사
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            document.body.insertAdjacentHTML('beforeend', modalHTML);
        }
    }

    // Toast 생성
    function showToast(title, message, type = 'error', duration = 5000, details = null) {
        initToastContainer();

        const container = document.getElementById('toastContainer');
        const toastId = 'toast-' + Date.now();

        const icons = {
            error: 'bi-exclamation-circle-fill',
            success: 'bi-check-circle-fill',
            warning: 'bi-exclamation-triangle-fill'
        };

        const toastHTML = `
            <div class="error-toast ${type}" id="${toastId}">
                <div class="error-toast-icon">
                    <i class="bi ${icons[type] || icons.error}"></i>
                </div>
                <div class="error-toast-content">
                    <div class="error-toast-title">${title}</div>
                    <div class="error-toast-message">${message}</div>
                </div>
                <div class="error-toast-close">
                    <i class="bi bi-x"></i>
                </div>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', toastHTML);

        const toastElement = document.getElementById(toastId);

        // 상세 정보가 있으면 클릭 시 모달 표시
        if (details) {
            toastElement.style.cursor = 'pointer';
            toastElement.addEventListener('click', function(e) {
                if (!e.target.closest('.error-toast-close')) {
                    showErrorModal(details);
                }
            });
        }

        // 닫기 버튼
        toastElement.querySelector('.error-toast-close').addEventListener('click', function(e) {
            e.stopPropagation();
            removeToast(toastId);
        });

        // 자동 제거
        if (duration > 0) {
            setTimeout(() => {
                removeToast(toastId);
            }, duration);
        }

        // 로그 기록
        logError({
            title,
            message,
            type,
            timestamp: new Date().toISOString(),
            details
        });

        return toastId;
    }

    // Toast 제거
    function removeToast(toastId) {
        const toast = document.getElementById(toastId);
        if (toast) {
            toast.classList.add('removing');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }
    }

    // 오류 상세 모달 표시
    function showErrorModal(errorDetails) {
        initErrorModal();

        const modal = document.getElementById('errorDetailModal');
        const modalBody = document.getElementById('errorModalBody');
        const modalTitle = document.getElementById('errorModalTitle');

        modalTitle.textContent = errorDetails.title || '오류 상세 정보';

        // 모달 내용 구성
        let bodyHTML = '';

        // 오류 메시지
        if (errorDetails.message) {
            bodyHTML += `
                <div class="error-section">
                    <div class="error-section-title">오류 메시지</div>
                    <div class="error-section-content">
                        <div class="error-message">${errorDetails.message}</div>
                    </div>
                </div>
            `;
        }

        // 오류 코드
        if (errorDetails.code) {
            bodyHTML += `
                <div class="error-section">
                    <div class="error-section-title">오류 코드</div>
                    <div class="error-section-content">
                        <div class="error-code">${errorDetails.code}</div>
                    </div>
                </div>
            `;
        }

        // 발생 위치
        if (errorDetails.location) {
            bodyHTML += `
                <div class="error-section">
                    <div class="error-section-title">발생 위치</div>
                    <div class="error-section-content">
                        <div class="error-message">${errorDetails.location}</div>
                    </div>
                </div>
            `;
        }

        // 상세 정보
        if (errorDetails.details) {
            bodyHTML += `
                <div class="error-section">
                    <div class="error-section-title">상세 정보</div>
                    <div class="error-section-content">
                        <ul class="error-details-list">
            `;

            if (typeof errorDetails.details === 'object') {
                for (const [key, value] of Object.entries(errorDetails.details)) {
                    bodyHTML += `<li><strong>${key}:</strong> ${value}</li>`;
                }
            } else {
                bodyHTML += `<li>${errorDetails.details}</li>`;
            }

            bodyHTML += `
                        </ul>
                    </div>
                </div>
            `;
        }

        // 시간 정보
        bodyHTML += `
            <div class="error-section">
                <div class="error-timestamp">
                    <i class="bi bi-clock"></i> 발생 시각: ${new Date().toLocaleString('ko-KR')}
                </div>
            </div>
        `;

        modalBody.innerHTML = bodyHTML;

        // 모달 표시
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();

        // 오류 정보 저장 (복사용)
        modal.dataset.errorInfo = JSON.stringify(errorDetails);
    }

    // 오류 정보 클립보드 복사
    function copyErrorToClipboard() {
        const modal = document.getElementById('errorDetailModal');
        const errorInfo = JSON.parse(modal.dataset.errorInfo || '{}');

        const errorText = `
[오류 보고서]
제목: ${errorInfo.title || 'N/A'}
메시지: ${errorInfo.message || 'N/A'}
코드: ${errorInfo.code || 'N/A'}
위치: ${errorInfo.location || 'N/A'}
시각: ${new Date().toLocaleString('ko-KR')}
상세: ${JSON.stringify(errorInfo.details, null, 2)}
        `.trim();

        navigator.clipboard.writeText(errorText).then(() => {
            showToast('복사 완료', '오류 정보가 클립보드에 복사되었습니다.', 'success', 2000);
        }).catch(err => {
            console.error('Failed to copy:', err);
        });
    }

    // 로그 기록 (로컬 스토리지)
    function logError(errorInfo) {
        try {
            const logs = JSON.parse(localStorage.getItem('errorLogs') || '[]');
            logs.push({
                ...errorInfo,
                userAgent: navigator.userAgent,
                url: window.location.href
            });

            // 최대 50개까지만 저장
            if (logs.length > 50) {
                logs.shift();
            }

            localStorage.setItem('errorLogs', JSON.stringify(logs));
        } catch (e) {
            console.error('Failed to log error:', e);
        }
    }

    // 파일 업로드 오류 처리
    function handleFileUploadError(error, fileName) {
        const details = {
            title: '파일 업로드 실패',
            message: `"${fileName}" 파일을 업로드하는 중 오류가 발생했습니다.`,
            code: error.code || 'UPLOAD_ERROR',
            location: '파일 업로드 처리',
            details: {
                '파일명': fileName,
                '오류 원인': error.message || '알 수 없는 오류',
                '상태 코드': error.status || 'N/A',
                '파일 크기': error.fileSize ? formatFileSize(error.fileSize) : 'N/A',
                '허용 크기': '최대 10MB'
            }
        };

        showToast(
            '파일 업로드 실패',
            `"${fileName}" 업로드에 실패했습니다. 클릭하여 상세 정보를 확인하세요.`,
            'error',
            8000,
            details
        );
    }

    // 게시글 작성/수정 오류 처리
    function handlePostSubmitError(error, action = '작성') {
        const details = {
            title: `게시글 ${action} 실패`,
            message: `게시글을 ${action}하는 중 오류가 발생했습니다.`,
            code: error.code || 'POST_ERROR',
            location: `게시글 ${action} 처리`,
            details: {
                '작업': action,
                '오류 원인': error.message || '서버 응답 없음',
                '상태 코드': error.status || 'N/A',
                '응답 메시지': error.responseText || 'N/A'
            }
        };

        showToast(
            `게시글 ${action} 실패`,
            `게시글을 ${action}할 수 없습니다. 클릭하여 상세 정보를 확인하세요.`,
            'error',
            8000,
            details
        );
    }

    // 첨부파일 삭제 오류 처리
    function handleFileDeleteError(error, fileName) {
        const details = {
            title: '첨부파일 삭제 실패',
            message: `"${fileName}" 파일을 삭제하는 중 오류가 발생했습니다.`,
            code: error.code || 'DELETE_ERROR',
            location: '첨부파일 삭제 처리',
            details: {
                '파일명': fileName,
                '오류 원인': error.message || '알 수 없는 오류',
                '권한': '파일 삭제 권한을 확인하세요'
            }
        };

        showToast(
            '파일 삭제 실패',
            `"${fileName}" 삭제에 실패했습니다.`,
            'error',
            5000,
            details
        );
    }

    // 네트워크 오류 처리
    function handleNetworkError(error) {
        const details = {
            title: '네트워크 오류',
            message: '서버와의 연결에 실패했습니다. 인터넷 연결을 확인해주세요.',
            code: 'NETWORK_ERROR',
            location: '서버 통신',
            details: {
                '오류 유형': '네트워크 연결 실패',
                '원인': error.message || '서버 응답 없음',
                '해결 방법': '1. 인터넷 연결 확인\n2. 잠시 후 다시 시도\n3. 관리자에게 문의'
            }
        };

        showToast(
            '네트워크 오류',
            '서버와의 연결에 실패했습니다. 클릭하여 자세히 보기',
            'error',
            10000,
            details
        );
    }

    // 파일 크기 포맷팅
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    }

    // Public API
    return {
        showToast,
        showErrorModal,
        copyErrorToClipboard,
        handleFileUploadError,
        handlePostSubmitError,
        handleFileDeleteError,
        handleNetworkError
    };
})();

// 전역 오류 핸들러 등록
window.addEventListener('error', function(event) {
    console.error('Global error caught:', event.error);

    ErrorNotification.showToast(
        '예상치 못한 오류',
        '페이지에서 오류가 발생했습니다. 페이지를 새로고침해주세요.',
        'error',
        8000,
        {
            title: '예상치 못한 오류',
            message: event.message || '알 수 없는 오류',
            code: 'UNHANDLED_ERROR',
            location: event.filename + ':' + event.lineno,
            details: {
                '오류 메시지': event.message,
                '파일': event.filename,
                '줄 번호': event.lineno + ':' + event.colno
            }
        }
    );
});

// Promise rejection 핸들러
window.addEventListener('unhandledrejection', function(event) {
    console.error('Unhandled promise rejection:', event.reason);

    ErrorNotification.handleNetworkError({
        message: event.reason?.message || '비동기 작업 실패'
    });
});

