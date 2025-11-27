package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : ErrorCode.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   프로젝트 전체 에러 코드 정의 Enum
 *
 * Purpose (만든 이유):
 *   1. 에러 코드, HTTP 상태, 메시지를 한 곳에서 중앙 관리
 *   2. 일관된 에러 응답 형식 제공 (사용자 친화적 메시지)
 *   3. 유지보수 편의성 향상 (에러 코드 추가/수정 용이)
 *   4. 전산팀 보고를 위한 명확한 에러 코드 제공
 *
 * Key Features (주요 기능):
 *   - HTTP 상태 코드 (400, 403, 404, 500 등)
 *   - 고유 에러 코드 (C001, U001, P001 등)
 *   - 사용자 친화적 메시지 ("[카테고리] 설명 (에러코드: CODE)" 형식)
 *   - 도메인별 에러 코드 범위 구분 (1000번대: Common, 2000번대: User 등)
 *
 * Error Code Ranges (에러 코드 범위):
 *   - Common (1000~1999): 공통 오류 (입력 오류, 서버 오류 등)
 *   - User (2000~2999): 사용자 관련 오류 (로그인, 회원가입 등)
 *   - Post/Board (3000~3999): 게시글 관련 오류
 *   - Comment (4000~4999): 댓글 관련 오류
 *   - Attachment (5000~5999): 첨부파일 관련 오류
 *   - System (6000~6999): 시스템 설정 오류
 *   - I/O (7000~7999): 파일 입출력 오류
 *
 * Message Format (메시지 형식):
 *   "[카테고리] 상세 설명. 해결 방법 안내. (에러코드: CODE)"
 *   예: "[파일 업로드 실패] 파일 업로드 중 오류가 발생했습니다.
 *        파일 크기와 형식을 확인하거나 전산팀에 문의해주세요. (에러코드: A002)"
 *
 * Usage (사용 방법):
 *   throw new BusinessException(ErrorCode.INVALID_PASSWORD);
 *   throw new FileException(ErrorCode.FILE_WRITE_ERROR, cause);
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public enum ErrorCode {

	// Common (1000~1999) - 공통 오류
	INVALID_INPUT_VALUE(400, "C001", "[입력 오류] 입력하신 값이 올바르지 않습니다. 입력 형식을 확인해주세요. (에러코드: C001)"),
	INVALID_TYPE_VALUE(400, "C002", "[타입 오류] 데이터 형식이 올바르지 않습니다. 전산팀에 문의해주세요. (에러코드: C002)"),
	ENTITY_NOT_FOUND(404, "C003", "[조회 실패] 요청하신 데이터를 찾을 수 없습니다. 삭제되었거나 존재하지 않는 데이터입니다. (에러코드: C003)"),
	INTERNAL_SERVER_ERROR(500, "C004", "[서버 오류] 일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도하거나 전산팀에 문의해주세요. (에러코드: C004)"),
	METHOD_NOT_ALLOWED(405, "C005", "[요청 오류] 허용되지 않은 요청 방식입니다. 전산팀에 문의해주세요. (에러코드: C005)"),
	ACCESS_DENIED(403, "C006", "[권한 오류] 해당 기능에 접근할 권한이 없습니다. 관리자에게 문의해주세요. (에러코드: C006)"),
	INVALID_FILE_TYPE(400, "C007", "[파일 형식 오류] 허용되지 않는 파일 형식입니다. JPG, PNG, PDF 파일만 업로드 가능합니다. (에러코드: C007)"),
	FILE_SIZE_EXCEEDED(400, "C008", "[파일 크기 오류] 파일 크기가 허용 한도(10MB)를 초과했습니다. 파일 크기를 줄여주세요. (에러코드: C008)"),

	// User (2000~2999) - 사용자 관련 오류
	USER_NOT_FOUND(404, "U001", "[사용자 조회 실패] 해당 사용자를 찾을 수 없습니다. 사용자 ID를 확인해주세요. (에러코드: U001)"),
	DUPLICATE_EMAIL(409, "U002", "[이메일 중복] 이미 사용 중인 이메일입니다. 다른 이메일을 사용해주세요. (에러코드: U002)"),
	INVALID_PASSWORD(400, "U003", "[비밀번호 오류] 비밀번호가 일치하지 않습니다. 비밀번호를 확인해주세요. (에러코드: U003)"),
	USER_ALREADY_EXISTS(409, "U004", "[회원가입 실패] 이미 가입된 사용자입니다. 로그인을 시도해주세요. (에러코드: U004)"),
	INVALID_TOKEN(401, "U005", "[인증 오류] 유효하지 않은 인증 토큰입니다. 다시 로그인해주세요. (에러코드: U005)"),
	EXPIRED_TOKEN(401, "U006", "[세션 만료] 로그인 세션이 만료되었습니다. 다시 로그인해주세요. (에러코드: U006)"),

	// Post/Board (3000~3999) - 게시글 관련 오류
	POST_NOT_FOUND(404, "P001", "[게시글 조회 실패] 해당 게시글을 찾을 수 없습니다. 삭제되었거나 존재하지 않는 게시글입니다. (에러코드: P001)"),
	POST_ALREADY_DELETED(400, "P002", "[게시글 삭제 불가] 이미 삭제된 게시글입니다. (에러코드: P002)"),
	UNAUTHORIZED_POST_ACCESS(403, "P003", "[게시글 접근 불가] 게시글 열람 권한이 없습니다. 작성자 또는 관리자만 접근 가능합니다. (에러코드: P003)"),
	INVALID_POST_PASSWORD(400, "P004", "[비밀번호 불일치] 게시글 비밀번호가 일치하지 않습니다. 비밀번호를 다시 확인해주세요. (에러코드: P004)"),
	POST_UPDATE_FAILED(500, "P005", "[게시글 수정 실패] 게시글 수정 중 오류가 발생했습니다. 전산팀에 문의해주세요. (에러코드: P005)"),
	POST_DELETE_FAILED(500, "P006", "[게시글 삭제 실패] 게시글 삭제 중 오류가 발생했습니다. 전산팀에 문의해주세요. (에러코드: P006)"),

	// Comment (4000~4999) - 댓글 관련 오류
	COMMENT_NOT_FOUND(404, "CM001", "[댓글 조회 실패] 해당 댓글을 찾을 수 없습니다. 삭제되었거나 존재하지 않는 댓글입니다. (에러코드: CM001)"),
	COMMENT_ALREADY_DELETED(400, "CM002", "[댓글 삭제 불가] 이미 삭제된 댓글입니다. (에러코드: CM002)"),
	UNAUTHORIZED_COMMENT_ACCESS(403, "CM003", "[댓글 접근 불가] 댓글 수정/삭제 권한이 없습니다. 작성자 또는 관리자만 가능합니다. (에러코드: CM003)"),
	INVALID_COMMENT_PASSWORD(400, "CM004", "[비밀번호 불일치] 댓글 비밀번호가 일치하지 않습니다. 비밀번호를 다시 확인해주세요. (에러코드: CM004)"),
	COMMENT_HAS_CHILDREN(400, "CM005", "[댓글 삭제 불가] 답글이 있는 댓글은 삭제할 수 없습니다. 먼저 답글을 삭제해주세요. (에러코드: CM005)"),
	STAFF_COMMENT_DELETE_DENIED(403, "CM006", "[관리자 댓글 삭제 불가] 관리자가 작성한 댓글은 사용자가 삭제할 수 없습니다. 관리자에게 문의해주세요. (에러코드: CM006)"),

	// Attachment (5000~5999) - 첨부파일 관련 오류
	ATTACHMENT_NOT_FOUND(404, "A001", "[첨부파일 조회 실패] 해당 첨부파일을 찾을 수 없습니다. 삭제되었거나 존재하지 않는 파일입니다. (에러코드: A001)"),
	ATTACHMENT_UPLOAD_FAILED(500, "A002", "[파일 업로드 실패] 파일 업로드 중 오류가 발생했습니다. 파일 크기와 형식을 확인하거나 전산팀에 문의해주세요. (에러코드: A002)"),
	ATTACHMENT_DELETE_FAILED(500, "A003", "[파일 삭제 실패] 파일 삭제 중 오류가 발생했습니다. 전산팀에 문의해주세요. (에러코드: A003)"),
	ATTACHMENT_DOWNLOAD_FAILED(500, "A004", "[파일 다운로드 실패] 파일 다운로드 중 오류가 발생했습니다. 파일 경로를 확인하거나 전산팀에 문의해주세요. (에러코드: A004)"),
	INVALID_ATTACHMENT_PATH(400, "A005", "[파일 경로 오류] 잘못된 파일 경로입니다. 전산팀에 문의해주세요. (에러코드: A005)"),

	// System (6000~6999) - 시스템 설정 오류
	SYSTEM_CONFIG_NOT_FOUND(404, "S001", "[시스템 설정 조회 실패] 시스템 설정을 찾을 수 없습니다. 전산팀에 문의해주세요. (에러코드: S001)"),
	SYSTEM_CONFIG_UPDATE_FAILED(500, "S002", "[시스템 설정 실패] 시스템 설정 변경 중 오류가 발생했습니다. 전산팀에 문의해주세요. (에러코드: S002)"),

	// I/O (7000~7999) - 파일 입출력 오류
	FILE_READ_ERROR(500, "IO001", "[파일 읽기 오류] 파일을 읽는 중 오류가 발생했습니다. 파일이 손상되었거나 권한이 없습니다. 전산팀에 문의해주세요. (에러코드: IO001)"),
	FILE_WRITE_ERROR(500, "IO002", "[파일 쓰기 오류] 파일을 저장하는 중 오류가 발생했습니다. 디스크 공간 또는 권한을 확인하거나 전산팀에 문의해주세요. (에러코드: IO002)"),
	FILE_DELETE_ERROR(500, "IO003", "[파일 삭제 오류] 파일 삭제 중 오류가 발생했습니다. 파일이 사용 중이거나 권한이 없습니다. 전산팀에 문의해주세요. (에러코드: IO003)"),
	DIRECTORY_CREATE_ERROR(500, "IO004", "[디렉토리 생성 오류] 폴더 생성 중 오류가 발생했습니다. 디스크 공간 또는 권한을 확인하거나 전산팀에 문의해주세요. (에러코드: IO004)");

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

