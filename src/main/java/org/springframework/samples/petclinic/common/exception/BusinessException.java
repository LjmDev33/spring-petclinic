package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : BusinessException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   비즈니스 로직 처리 중 발생하는 예외
 *
 * Purpose (만든 이유):
 *   1. 비즈니스 규칙 위반 시 명확한 예외 타입 제공
 *   2. 기술적 오류(DB, I/O)와 비즈니스 오류를 구분
 *   3. 예상 가능한 오류에 대한 일관된 처리 방식 제공
 *
 * When to Use (사용 시점):
 *   - 비밀번호 불일치
 *   - 권한 부족 (접근 권한 없음)
 *   - 중복 데이터 (이미 존재하는 이메일, 닉네임 등)
 *   - 비즈니스 규칙 위반 (답글이 있는 댓글 삭제 시도 등)
 *   - 잘못된 상태 전환 (이미 삭제된 게시글 수정 시도 등)
 *
 * Key Features (주요 기능):
 *   - BaseException 상속으로 ErrorCode 통합 관리
 *   - Service 계층에서 주로 사용
 *   - GlobalExceptionHandler에서 자동 처리 (WARN 레벨 로깅)
 *   - 사용자에게 명확한 에러 메시지 전달
 *
 * Usage Examples (사용 예시):
 *   // 비밀번호 불일치
 *   if (!passwordMatches) {
 *       throw new BusinessException(ErrorCode.INVALID_PASSWORD);
 *   }
 *
 *   // 권한 없음
 *   if (!hasPermission) {
 *       throw new BusinessException(ErrorCode.ACCESS_DENIED);
 *   }
 *
 *   // 이미 삭제된 게시글
 *   if (post.isDeleted()) {
 *       throw new BusinessException(ErrorCode.POST_ALREADY_DELETED);
 *   }
 *
 * vs RuntimeException:
 *   - RuntimeException: 개발자가 의도하지 않은 버그 (NullPointerException 등)
 *   - BusinessException: 비즈니스 규칙 위반으로 예상 가능한 오류
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

