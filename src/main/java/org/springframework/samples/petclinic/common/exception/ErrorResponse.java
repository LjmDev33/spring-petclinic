package org.springframework.samples.petclinic.common.exception;

import java.time.LocalDateTime;

/**
 * Project : spring-petclinic
 * File    : ErrorResponse.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   에러 응답 표준 DTO
 *
 * Purpose (만든 이유):
 *   1. 클라이언트에게 일관된 에러 응답 형식 제공
 *   2. API와 화면 요청 모두에서 사용 가능한 표준 DTO
 *   3. 에러 발생 시간, 상태 코드, 에러 코드, 메시지, 요청 경로를 모두 포함
 *   4. GlobalExceptionHandler와 연동하여 자동 생성
 *
 * Response Format (응답 형식):
 *   {
 *     "timestamp": "2025-11-26T10:30:00",
 *     "status": 404,
 *     "code": "P001",
 *     "message": "[게시글 조회 실패] 해당 게시글을 찾을 수 없습니다. (에러코드: P001)",
 *     "path": "/counsel/detail/123"
 *   }
 *
 * Key Features (주요 기능):
 *   - Immutable 객체 (final 필드, private 생성자)
 *   - 정적 팩토리 메서드로 편리한 생성
 *   - ErrorCode 또는 BaseException으로부터 자동 생성
 *   - JSON 직렬화 지원 (REST API 응답)
 *   - Thymeleaf 템플릿에서도 사용 가능 (ModelAndView)
 *
 * Usage Examples (사용 예시):
 *   // ErrorCode로부터 생성
 *   ErrorResponse response = ErrorResponse.of(ErrorCode.POST_NOT_FOUND, "/counsel/detail/123");
 *
 *   // BaseException으로부터 생성
 *   ErrorResponse response = ErrorResponse.of(exception, request.getRequestURI());
 *
 *   // 직접 생성
 *   ErrorResponse response = ErrorResponse.of(404, "P001", "게시글을 찾을 수 없습니다.", "/counsel/detail/123");
 *
 * Used By (사용처):
 *   - GlobalExceptionHandler: 모든 예외를 ErrorResponse로 변환
 *   - REST API Controller: JSON 응답 생성
 *   - Thymeleaf 에러 페이지: 에러 정보 표시
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class ErrorResponse {

	private final LocalDateTime timestamp;
	private final int status;
	private final String code;
	private final String message;
	private final String path;

	private ErrorResponse(LocalDateTime timestamp, int status, String code, String message, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.code = code;
		this.message = message;
		this.path = path;
	}

	public static ErrorResponse of(ErrorCode errorCode, String path) {
		return new ErrorResponse(
			LocalDateTime.now(),
			errorCode.getStatus(),
			errorCode.getCode(),
			errorCode.getMessage(),
			path
		);
	}

	public static ErrorResponse of(BaseException exception, String path) {
		return new ErrorResponse(
			LocalDateTime.now(),
			exception.getStatus(),
			exception.getCode(),
			exception.getMessage(),
			path
		);
	}

	public static ErrorResponse of(int status, String code, String message, String path) {
		return new ErrorResponse(
			LocalDateTime.now(),
			status,
			code,
			message,
			path
		);
	}

	// Getters
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public int getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getPath() {
		return path;
	}
}

