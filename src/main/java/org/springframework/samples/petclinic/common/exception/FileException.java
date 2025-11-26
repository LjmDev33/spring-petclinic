package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : FileException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   파일 처리 중 발생하는 예외 (업로드, 다운로드, 삭제, I/O 오류)
 *   - 메모리 누수 방지를 위한 try-catch-finally 패턴과 함께 사용
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class FileException extends BaseException {

	public FileException(ErrorCode errorCode) {
		super(errorCode);
	}

	public FileException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}

	public FileException(ErrorCode errorCode, String customMessage) {
		super(errorCode, customMessage);
	}
}

