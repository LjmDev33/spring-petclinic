package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : BaseException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   프로젝트 전체 예외 처리의 기본 추상 클래스
 *   - 에러 코드, 메시지, HTTP 상태 코드를 통합 관리
 *   - 모든 Custom Exception은 이 클래스를 상속
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public abstract class BaseException extends RuntimeException {

	private final ErrorCode errorCode;

	protected BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	protected BaseException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

	protected BaseException(ErrorCode errorCode, String customMessage) {
		super(customMessage);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String getCode() {
		return errorCode.getCode();
	}

	public int getStatus() {
		return errorCode.getStatus();
	}
}

