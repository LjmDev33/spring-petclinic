package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : BaseException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   프로젝트 전체 예외 처리의 기본 추상 클래스
 *
 * Purpose (만든 이유):
 *   1. 모든 Custom Exception의 공통 부모 클래스로 일관성 확보
 *   2. ErrorCode를 통한 표준화된 예외 정보 관리
 *   3. GlobalExceptionHandler와 연동하여 통합 예외 처리
 *
 * Key Features (주요 기능):
 *   - ErrorCode 객체를 통한 에러 코드, 메시지, HTTP 상태 코드 통합 관리
 *   - RuntimeException 상속으로 Unchecked Exception 처리
 *   - 원인 예외(cause)를 함께 전달하여 예외 추적성 향상
 *   - 커스텀 메시지 지원 (ErrorCode 메시지를 동적으로 변경 가능)
 *
 * Usage (사용 방법):
 *   - 직접 사용하지 않고 하위 클래스(BusinessException, EntityNotFoundException 등)를 통해 사용
 *   - 새로운 예외 타입이 필요할 경우 이 클래스를 상속하여 구현
 *
 * Hierarchy (예외 계층):
 *   BaseException (추상)
 *   ├── BusinessException (비즈니스 로직 오류)
 *   ├── EntityNotFoundException (엔티티 미존재)
 *   └── FileException (파일 I/O 오류)
 *
 * Example:
 *   throw new BusinessException(ErrorCode.INVALID_PASSWORD);
 *   throw EntityNotFoundException.of("Post", 123L);
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

