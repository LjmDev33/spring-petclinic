package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : EntityNotFoundException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   엔티티를 찾을 수 없을 때 발생하는 예외
 *
 * Purpose (만든 이유):
 *   1. DB 조회 실패 시 명확한 예외 타입 제공
 *   2. 404 NOT FOUND 상태 코드와 자동 매핑
 *   3. 어떤 엔티티의 어떤 ID가 존재하지 않는지 명확한 메시지 생성
 *
 * When to Use (사용 시점):
 *   - Repository.findById()가 Optional.empty() 반환 시
 *   - DB 조회 결과가 null일 때
 *   - 삭제되었거나 존재하지 않는 리소스 접근 시도
 *
 * Key Features (주요 기능):
 *   - BaseException 상속으로 ErrorCode 통합 관리
 *   - 정적 팩토리 메서드 of()로 편리한 생성
 *   - 엔티티 타입과 ID를 포함한 상세 메시지 자동 생성
 *   - GlobalExceptionHandler에서 404 응답으로 자동 변환
 *
 * Usage Examples (사용 예시):
 *   // 게시글 조회 실패
 *   Post post = repository.findById(id)
 *       .orElseThrow(() -> EntityNotFoundException.of("Post", id));
 *   // 메시지: "Post(id=123)을(를) 찾을 수 없습니다."
 *
 *   // 댓글 조회 실패
 *   Comment comment = commentRepository.findById(commentId)
 *       .orElseThrow(() -> EntityNotFoundException.of("Comment", commentId));
 *
 *   // 사용자 조회 실패
 *   User user = userRepository.findById(userId)
 *       .orElseThrow(() -> EntityNotFoundException.of("User", userId));
 *
 * vs IllegalArgumentException:
 *   - IllegalArgumentException: 잘못된 파라미터 입력 (클라이언트 오류)
 *   - EntityNotFoundException: DB에 데이터가 없음 (리소스 미존재)
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

