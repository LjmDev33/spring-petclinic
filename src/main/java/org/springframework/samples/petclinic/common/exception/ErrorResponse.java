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
 *   - 클라이언트에게 일관된 에러 응답 형식 제공
 *   - JSON 응답 및 Thymeleaf 에러 페이지에 사용
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

