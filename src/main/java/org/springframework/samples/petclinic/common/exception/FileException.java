package org.springframework.samples.petclinic.common.exception;

/**
 * Project : spring-petclinic
 * File    : FileException.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   파일 처리 중 발생하는 예외 (업로드, 다운로드, 삭제, I/O 오류)
 *
 * Purpose (만든 이유):
 *   1. 파일 I/O 관련 예외를 비즈니스 예외와 명확히 구분
 *   2. IOException을 Unchecked Exception으로 래핑하여 처리 편의성 향상
 *   3. 파일 작업 실패 원인을 명확하게 추적 (원인 예외 포함)
 *   4. 메모리 누수 방지를 위한 리소스 정리와 연계
 *
 * When to Use (사용 시점):
 *   - 파일 업로드 실패 (디스크 공간 부족, 권한 없음)
 *   - 파일 다운로드 실패 (파일 미존재, 경로 오류)
 *   - 파일 읽기/쓰기 실패 (IOException 발생 시)
 *   - 파일 삭제 실패 (파일 사용 중, 권한 없음)
 *   - 디렉토리 생성 실패
 *
 * Key Features (주요 기능):
 *   - BaseException 상속으로 ErrorCode 통합 관리
 *   - IOException을 래핑하여 원인 추적
 *   - GlobalExceptionHandler에서 500 응답으로 자동 변환
 *   - 메모리 누수 방지를 위한 try-with-resources와 함께 사용
 *
 * Usage Examples (사용 예시):
 *   // 파일 업로드
 *   try {
 *       file.transferTo(new File(path));
 *   } catch (IOException e) {
 *       throw new FileException(ErrorCode.ATTACHMENT_UPLOAD_FAILED, e);
 *   }
 *
 *   // 파일 읽기 (try-with-resources)
 *   try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path))) {
 *       // 파일 읽기
 *   } catch (IOException e) {
 *       throw new FileException(ErrorCode.FILE_READ_ERROR, e);
 *   }
 *
 *   // 파일 삭제
 *   try {
 *       Files.delete(filePath);
 *   } catch (IOException e) {
 *       throw new FileException(ErrorCode.FILE_DELETE_ERROR, e);
 *   }
 *
 * Best Practice (모범 사례):
 *   - 항상 try-with-resources를 사용하여 리소스 자동 정리
 *   - 원인 예외(IOException)를 함께 전달하여 스택트레이스 보존
 *   - 파일 작업 전에 경로 및 권한 검증 수행
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

