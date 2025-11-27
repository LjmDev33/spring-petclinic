package org.springframework.samples.petclinic.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

/**
 * Project : spring-petclinic
 * File    : GlobalExceptionHandler.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   전역 예외 처리 핸들러 (모든 Controller의 예외를 한 곳에서 처리)
 *
 * Purpose (만든 이유):
 *   1. 모든 Controller에서 발생하는 예외를 중앙에서 통합 처리
 *   2. 중복 코드 제거 (각 Controller마다 try-catch 불필요)
 *   3. 일관된 에러 응답 형식 제공 (API: JSON, 화면: HTML)
 *   4. 자동 로깅으로 예외 누락 방지 및 추적성 향상
 *   5. API 요청과 화면 요청을 자동 구분하여 적절한 응답 반환
 *
 * How It Works (작동 방식):
 *   1. Controller에서 예외 발생
 *   2. @RestControllerAdvice가 예외를 자동 감지
 *   3. 예외 타입에 맞는 @ExceptionHandler 메서드 실행
 *   4. 요청 타입 확인 (API인지 화면인지)
 *   5. API 요청 → JSON 응답 (ResponseEntity<ErrorResponse>)
 *      화면 요청 → Thymeleaf 에러 페이지 (ModelAndView)
 *
 * Request Type Detection (요청 타입 구분):
 *   - URL 패턴: /api/**로 시작하면 API 요청
 *   - Accept 헤더: application/json 포함 시 API 요청
 *   - Content-Type 헤더: application/json 포함 시 API 요청
 *   - 그 외: 화면 요청 (Thymeleaf)
 *
 * Exception Handlers (예외 핸들러):
 *   - BusinessException: 비즈니스 로직 오류 (WARN 레벨)
 *   - EntityNotFoundException: 엔티티 미존재 (WARN 레벨)
 *   - FileException: 파일 I/O 오류 (ERROR 레벨)
 *   - IllegalArgumentException: 잘못된 인자 (WARN 레벨)
 *   - IllegalStateException: 잘못된 상태 (WARN 레벨)
 *   - Exception: 모든 예외 (ERROR 레벨, Fallback)
 *
 * Log Levels (로그 레벨):
 *   - WARN: 예상 가능한 오류 (비즈니스 로직 위반, 데이터 미존재)
 *   - ERROR: 예상 불가능한 오류 (시스템 장애, 파일 I/O 실패)
 *
 * Key Features (주요 기능):
 *   - 자동 예외 감지 및 처리 (@RestControllerAdvice)
 *   - API와 화면 요청 자동 구분 (isApiRequest 메서드)
 *   - 일관된 에러 응답 (ErrorResponse DTO)
 *   - 자동 로깅 (예외 타입에 따른 로그 레벨 구분)
 *   - 스택트레이스 출력 최소화 (성능 최적화)
 *
 * Usage Examples (사용 예시):
 *   // Controller (try-catch 불필요)
 *   @PostMapping("/write")
 *   public String write(@ModelAttribute PostDto dto) {
 *       service.createPost(dto); // 예외는 GlobalExceptionHandler가 처리
 *       return "redirect:/post/list";
 *   }
 *
 *   // Service (Custom Exception 사용)
 *   public Post getPost(Long id) {
 *       return repository.findById(id)
 *           .orElseThrow(() -> EntityNotFoundException.of("Post", id));
 *       // → GlobalExceptionHandler가 404 응답 또는 에러 페이지 반환
 *   }
 *
 * Response Examples (응답 예시):
 *   // API 요청 (JSON 응답)
 *   {
 *     "timestamp": "2025-11-26T10:30:00",
 *     "status": 404,
 *     "code": "P001",
 *     "message": "[게시글 조회 실패] 해당 게시글을 찾을 수 없습니다. (에러코드: P001)",
 *     "path": "/api/posts/123"
 *   }
 *
 *   // 화면 요청 (Thymeleaf 에러 페이지)
 *   - templates/error.html 렌더링
 *   - Model에 status, message, path, timestamp 전달
 *
 * Performance Optimization (성능 최적화):
 *   - 스택트레이스는 ERROR 레벨에서만 출력 (WARN은 메시지만)
 *   - 불필요한 로그 출력 최소화
 *   - 빠른 요청 타입 판별 (URL 패턴 우선 체크)
 *
 * Benefits (효과):
 *   - 코드 중복 제거: Controller에서 try-catch 불필요
 *   - 일관된 에러 응답: 모든 예외가 동일한 형식으로 반환
 *   - 자동 로깅: 예외 누락 방지
 *   - 유지보수 용이: 예외 처리 로직이 한 곳에 집중
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * BusinessException 처리 (비즈니스 로직 오류)
	 */
	@ExceptionHandler(BusinessException.class)
	public Object handleBusinessException(BusinessException ex, HttpServletRequest request) {
		log.warn("BusinessException: code={}, message={}, path={}",
			ex.getCode(), ex.getMessage(), request.getRequestURI());

		if (isApiRequest(request)) {
			ErrorResponse response = ErrorResponse.of(ex, request.getRequestURI());
			return ResponseEntity.status(ex.getStatus()).body(response);
		} else {
			return createErrorView(ex.getStatus(), ex.getMessage(), request);
		}
	}

	/**
	 * EntityNotFoundException 처리 (엔티티 미존재)
	 */
	@ExceptionHandler(EntityNotFoundException.class)
	public Object handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
		log.warn("EntityNotFoundException: code={}, message={}, path={}",
			ex.getCode(), ex.getMessage(), request.getRequestURI());

		if (isApiRequest(request)) {
			ErrorResponse response = ErrorResponse.of(ex, request.getRequestURI());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		} else {
			return createErrorView(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request);
		}
	}

	/**
	 * FileException 처리 (파일 처리 오류)
	 */
	@ExceptionHandler(FileException.class)
	public Object handleFileException(FileException ex, HttpServletRequest request) {
		log.error("FileException: code={}, message={}, path={}",
			ex.getCode(), ex.getMessage(), request.getRequestURI(), ex);

		if (isApiRequest(request)) {
			ErrorResponse response = ErrorResponse.of(ex, request.getRequestURI());
			return ResponseEntity.status(ex.getStatus()).body(response);
		} else {
			return createErrorView(ex.getStatus(), "파일 처리 중 오류가 발생했습니다.", request);
		}
	}

	/**
	 * IllegalArgumentException 처리 (잘못된 인자)
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public Object handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
		log.warn("IllegalArgumentException: message={}, path={}",
			ex.getMessage(), request.getRequestURI());

		if (isApiRequest(request)) {
			ErrorResponse response = ErrorResponse.of(
				ErrorCode.INVALID_INPUT_VALUE.getStatus(),
				ErrorCode.INVALID_INPUT_VALUE.getCode(),
				ex.getMessage() != null ? ex.getMessage() : ErrorCode.INVALID_INPUT_VALUE.getMessage(),
				request.getRequestURI()
			);
			return ResponseEntity.badRequest().body(response);
		} else {
			return createErrorView(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request);
		}
	}

	/**
	 * IllegalStateException 처리 (잘못된 상태)
	 */
	@ExceptionHandler(IllegalStateException.class)
	public Object handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
		log.warn("IllegalStateException: message={}, path={}",
			ex.getMessage(), request.getRequestURI());

		if (isApiRequest(request)) {
			ErrorResponse response = ErrorResponse.of(
				ErrorCode.INVALID_INPUT_VALUE.getStatus(),
				ErrorCode.INVALID_INPUT_VALUE.getCode(),
				ex.getMessage() != null ? ex.getMessage() : "잘못된 상태입니다.",
				request.getRequestURI()
			);
			return ResponseEntity.badRequest().body(response);
		} else {
			return createErrorView(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request);
		}
	}

	/**
	 * 모든 예외 처리 (Fallback)
	 */
	@ExceptionHandler(Exception.class)
	public Object handleException(Exception ex, HttpServletRequest request) {
		log.error("Unhandled Exception: message={}, path={}",
			ex.getMessage(), request.getRequestURI(), ex);

		if (isApiRequest(request)) {
			ErrorResponse response = ErrorResponse.of(
				ErrorCode.INTERNAL_SERVER_ERROR,
				request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		} else {
			return createErrorView(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
				request
			);
		}
	}

	/**
	 * API 요청 여부 판단 (JSON 응답 vs HTML 응답)
	 */
	private boolean isApiRequest(HttpServletRequest request) {
		String accept = request.getHeader("Accept");
		String contentType = request.getHeader("Content-Type");
		String requestUri = request.getRequestURI();

		// API 경로 패턴 체크
		if (requestUri.startsWith("/api/")) {
			return true;
		}

		// Accept 헤더 체크
		if (accept != null && accept.contains("application/json")) {
			return true;
		}

		// Content-Type 체크
		if (contentType != null && contentType.contains("application/json")) {
			return true;
		}

		return false;
	}

	/**
	 * 에러 화면 생성 (Thymeleaf)
	 */
	private ModelAndView createErrorView(int status, String message, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("status", status);
		mav.addObject("message", message);
		mav.addObject("path", request.getRequestURI());
		mav.addObject("timestamp", java.time.LocalDateTime.now());
		mav.setStatus(org.springframework.http.HttpStatus.valueOf(status));
		return mav;
	}
}

