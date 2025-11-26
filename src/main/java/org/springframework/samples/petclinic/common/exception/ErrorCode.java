package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : ErrorCode.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   프로젝트 전체 에러 코드 정의 Enum
 *   - HTTP 상태 코드, 에러 코드, 메시지를 통합 관리
 *   - 다국어 지원을 위한 메시지 키 포함 가능
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public enum ErrorCode {

	// Common (1000~1999)
	INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다."),
	INVALID_TYPE_VALUE(400, "C002", "잘못된 타입입니다."),
	ENTITY_NOT_FOUND(404, "C003", "엔티티를 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(500, "C004", "서버 오류가 발생했습니다."),
	METHOD_NOT_ALLOWED(405, "C005", "허용되지 않는 메서드입니다."),
	ACCESS_DENIED(403, "C006", "접근 권한이 없습니다."),
	INVALID_FILE_TYPE(400, "C007", "허용되지 않는 파일 형식입니다."),
	FILE_SIZE_EXCEEDED(400, "C008", "파일 크기가 초과되었습니다."),

	// User (2000~2999)
	USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
	DUPLICATE_EMAIL(409, "U002", "이미 존재하는 이메일입니다."),
	INVALID_PASSWORD(400, "U003", "비밀번호가 일치하지 않습니다."),
	USER_ALREADY_EXISTS(409, "U004", "이미 존재하는 사용자입니다."),
	INVALID_TOKEN(401, "U005", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(401, "U006", "만료된 토큰입니다."),

	// Post/Board (3000~3999)
	POST_NOT_FOUND(404, "P001", "게시글을 찾을 수 없습니다."),
	POST_ALREADY_DELETED(400, "P002", "이미 삭제된 게시글입니다."),
	UNAUTHORIZED_POST_ACCESS(403, "P003", "게시글 접근 권한이 없습니다."),
	INVALID_POST_PASSWORD(400, "P004", "게시글 비밀번호가 일치하지 않습니다."),
	POST_UPDATE_FAILED(500, "P005", "게시글 수정에 실패했습니다."),
	POST_DELETE_FAILED(500, "P006", "게시글 삭제에 실패했습니다."),

	// Comment (4000~4999)
	COMMENT_NOT_FOUND(404, "CM001", "댓글을 찾을 수 없습니다."),
	COMMENT_ALREADY_DELETED(400, "CM002", "이미 삭제된 댓글입니다."),
	UNAUTHORIZED_COMMENT_ACCESS(403, "CM003", "댓글 접근 권한이 없습니다."),
	INVALID_COMMENT_PASSWORD(400, "CM004", "댓글 비밀번호가 일치하지 않습니다."),
	COMMENT_HAS_CHILDREN(400, "CM005", "답글이 있는 댓글은 삭제할 수 없습니다."),
	STAFF_COMMENT_DELETE_DENIED(403, "CM006", "운영자 댓글은 삭제할 수 없습니다."),

	// Attachment (5000~5999)
	ATTACHMENT_NOT_FOUND(404, "A001", "첨부파일을 찾을 수 없습니다."),
	ATTACHMENT_UPLOAD_FAILED(500, "A002", "첨부파일 업로드에 실패했습니다."),
	ATTACHMENT_DELETE_FAILED(500, "A003", "첨부파일 삭제에 실패했습니다."),
	ATTACHMENT_DOWNLOAD_FAILED(500, "A004", "첨부파일 다운로드에 실패했습니다."),
	INVALID_ATTACHMENT_PATH(400, "A005", "잘못된 첨부파일 경로입니다."),

	// System (6000~6999)
	SYSTEM_CONFIG_NOT_FOUND(404, "S001", "시스템 설정을 찾을 수 없습니다."),
	SYSTEM_CONFIG_UPDATE_FAILED(500, "S002", "시스템 설정 업데이트에 실패했습니다."),

	// I/O (7000~7999)
	FILE_READ_ERROR(500, "IO001", "파일 읽기에 실패했습니다."),
	FILE_WRITE_ERROR(500, "IO002", "파일 쓰기에 실패했습니다."),
	FILE_DELETE_ERROR(500, "IO003", "파일 삭제에 실패했습니다."),
	DIRECTORY_CREATE_ERROR(500, "IO004", "디렉토리 생성에 실패했습니다.");

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(int status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
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
}

