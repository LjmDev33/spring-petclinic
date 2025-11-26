package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : BusinessException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   비즈니스 로직 처리 중 발생하는 예외
 *   - Service 계층에서 주로 사용
 *   - 예: 중복 데이터, 권한 부족, 비즈니스 규칙 위반 등
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class BusinessException extends BaseException {

	public BusinessException(ErrorCode errorCode) {
		super(errorCode);
	}

	public BusinessException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}

	public BusinessException(ErrorCode errorCode, String customMessage) {
		super(errorCode, customMessage);
	}
}

