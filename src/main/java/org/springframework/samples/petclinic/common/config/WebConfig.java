package org.springframework.samples.petclinic.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Project : spring-petclinic
 * File    : WebConfig.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   Spring Web MVC 전역 설정 (정적 리소스, CORS, Interceptor 등)
 *
 * Purpose (만든 이유):
 *   1. 정적 리소스 경로를 커스텀하게 설정
 *   2. /animal/** 경로를 프로젝트 내부 리소스로 매핑
 *   3. Git 배포 시에도 모든 사용자가 이미지를 볼 수 있도록 보장
 *   4. CDN 없이 오프라인 환경에서도 리소스 접근 가능
 *
 * Key Features (주요 기능):
 *   - 정적 리소스 핸들러 커스터마이징
 *   - classpath:/static/ 폴더의 리소스를 URL로 매핑
 *   - 캐싱 정책 적용 가능 (향후 확장)
 *   - CORS 설정 가능 (향후 확장)
 *
 * Current Configuration (현재 설정):
 *   - /animal/** → classpath:/static/animal/
 *     예: http://localhost:8080/animal/dog.jpg
 *         → src/main/resources/static/animal/dog.jpg
 *
 * Why Not External Storage (외부 저장소를 사용하지 않는 이유):
 *   1. 프로젝트 규칙: 모든 리소스는 프로젝트에 내장 (CDN 사용 금지)
 *   2. 오프라인 환경 지원: 인터넷 없이도 실행 가능
 *   3. Git 배포 용이: 클론만 하면 바로 실행 가능
 *   4. 유지보수 편의: 코드와 리소스를 함께 관리
 *
 * Usage Examples (사용 예시):
 *   // HTML/Thymeleaf에서 사용
 *   <img th:src="@{/animal/dog.jpg}" alt="Dog">
 *
 *   // CSS에서 사용
 *   background-image: url('/animal/cat.png');
 *
 * Future Enhancements (향후 확장 가능):
 *   - 캐싱 정책: .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
 *   - CORS 설정: @Override addCorsMappings()
 *   - Interceptor 추가: @Override addInterceptors()
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * 정적 리소스 핸들러 등록
	 * - /animal/** 요청을 src/main/resources/static/animal 폴더로 매핑
	 * - Git으로 배포 시에도 이미지가 프로젝트에 포함되어 모든 사용자가 볼 수 있음
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// /animal/** 경로를 classpath의 static/animal 폴더로 매핑
		registry.addResourceHandler("/animal/**")
			.addResourceLocations("classpath:/static/animal/");
	}
}

