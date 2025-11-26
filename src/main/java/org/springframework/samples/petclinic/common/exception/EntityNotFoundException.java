package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : EntityNotFoundException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   엔티티를 찾을 수 없을 때 발생하는 예외
 *   - Repository 조회 시 null 또는 Optional.empty() 반환 시 사용
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class EntityNotFoundException extends BaseException {

	public EntityNotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

	public EntityNotFoundException(ErrorCode errorCode, String customMessage) {
		super(errorCode, customMessage);
	}

	/**
	 * 엔티티 타입과 ID를 포함한 메시지 생성
	 */
	public static EntityNotFoundException of(String entityName, Object id) {
		String message = String.format("%s(id=%s)을(를) 찾을 수 없습니다.", entityName, id);
		return new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, message);
	}
}

