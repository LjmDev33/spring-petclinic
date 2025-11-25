/**
 * Toast 알림 유틸리티
 * - Bootstrap Toast를 사용한 사용자 친화적 알림 시스템
 * - 성공, 정보, 경고, 에러 메시지 표시
 *
 * @author Jeongmin Lee
 * @since 2025-11-25
 */

const TOAST = {
    /**
     * 성공 메시지 표시
     * @param {string} message - 표시할 메시지
     * @param {number} duration - 표시 시간 (밀리초, 기본값: 3000)
     */
    showSuccess: function(message, duration = 3000) {
        this._show('success', '✓ 성공', message, duration);
    },

    /**
     * 정보 메시지 표시
     * @param {string} message - 표시할 메시지
     * @param {number} duration - 표시 시간 (밀리초, 기본값: 3000)
     */
    showInfo: function(message, duration = 3000) {
        this._show('info', 'ℹ 안내', message, duration);
    },

    /**
     * 경고 메시지 표시
     * @param {string} message - 표시할 메시지
     * @param {number} duration - 표시 시간 (밀리초, 기본값: 4000)
     */
    showWarning: function(message, duration = 4000) {
        this._show('warning', '⚠ 경고', message, duration);
    },

    /**
     * 에러 메시지 표시
     * @param {string} message - 표시할 메시지
     * @param {number} duration - 표시 시간 (밀리초, 기본값: 5000)
     */
    showError: function(message, duration = 5000) {
        this._show('error', '✕ 오류', message, duration);
    },

    /**
     * Toast 메시지를 실제로 생성하고 표시하는 내부 메서드
     * @private
     */
    _show: function(type, title, message, duration) {
        // Toast 컨테이너가 없으면 생성
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container position-fixed top-0 end-0 p-3';
            container.style.zIndex = '9999';
            document.body.appendChild(container);
        }

        // 배경색 결정
        const bgClass = {
            'success': 'bg-success',
            'info': 'bg-info',
            'warning': 'bg-warning',
            'error': 'bg-danger'
        }[type] || 'bg-secondary';

        // Toast HTML 생성
        const toastId = 'toast-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        const toastHtml = `
            <div id="${toastId}" class="toast" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="${duration}">
                <div class="toast-header ${bgClass} text-white">
                    <strong class="me-auto">${title}</strong>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body">
                    ${this._escapeHtml(message)}
                </div>
            </div>
        `;

        // Toast를 컨테이너에 추가
        container.insertAdjacentHTML('beforeend', toastHtml);
        const toastElement = document.getElementById(toastId);

        // Bootstrap Toast 초기화 및 표시
        const bsToast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: duration
        });
        bsToast.show();

        // Toast가 숨겨진 후 DOM에서 제거
        toastElement.addEventListener('hidden.bs.toast', function() {
            toastElement.remove();
        });
    },

    /**
     * HTML 특수문자 이스케이프 (XSS 방지)
     * @private
     */
    _escapeHtml: function(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
};

// 전역 객체로 등록
window.TOAST = TOAST;

