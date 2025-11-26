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
 *   전역 예외 처리 핸들러
 *   - REST API 요청: JSON 응답 (ResponseEntity<ErrorResponse>)
 *   - 일반 화면 요청: Thymeleaf 에러 페이지 (ModelAndView)
 *   - 모든 예외를 로그로 기록하여 추적 가능
 *
 * Performance:
 *   - 예외 발생 시 불필요한 스택트레이스 출력 최소화
 *   - 로그 레벨에 따른 선택적 상세 정보 출력
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

