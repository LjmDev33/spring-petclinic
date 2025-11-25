package org.springframework.samples.petclinic.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 테스트 컨트롤러
 * - Toast 알림 시스템 테스트 페이지
 * - 개발 환경에서만 사용
 *
 * @author Jeongmin Lee
 * @since 2025-11-25
 */
@Controller
@RequestMapping("/test")
public class TestController {

	/**
	 * Toast 알림 시스템 테스트 페이지
	 * - TOAST 객체 전역 등록 확인
	 * - 4가지 타입 메서드 테스트
	 * - XSS 방지 테스트
	 * - 메모리 누수 방지 테스트
	 * - 자동 사라짐 테스트
	 *
	 * @return 테스트 페이지 템플릿
	 */
	@GetMapping("/toast")
	public String toastTest() {
		return "test/toast-test";
	}
}

