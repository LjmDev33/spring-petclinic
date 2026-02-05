package org.springframework.samples.petclinic.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Project : spring-petclinic
 * File    : WebMvcConfig.java
 * Description :
 * - 정적 리소스 핸들러 설정
 * - 로컬 파일 시스템의 업로드 폴더를 웹 URL로 매핑
 * - 예: /images/uploads/2026/02/abc.jpg -> C:/.../uploads/2026/02/abc.jpg
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	// application.yml에 정의된 업로드 경로 주입
	@Value("${petclinic.file.base-dir}")
	private String baseDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 1. 업로드 경로 문자열 보정
		// ResourceLocations는 "file:///" 접두어가 필수이며, 디렉토리일 경우 끝에 "/"가 있어야 함
		String resourcePath = baseDir;

		// 1. 접두어 처리
		if (!resourcePath.startsWith("file:///")) {
			// 윈도우 경로(C:/...)나 리눅스 경로(/home/...)에 맞게 프로토콜 추가
			resourcePath = "file:///" + resourcePath;
		}

		// 2. 접미어 처리
		if (!resourcePath.endsWith("/")) {
			resourcePath = resourcePath + "/";
		}

		// URL 요청: /images/photo/... -> data/photo/... 로 자동 매핑됨
		// URL 요청: /images/counsel/... -> data/counsel/... 로 자동 매핑됨
		registry.addResourceHandler("/images/**")
			.addResourceLocations(resourcePath)                // 1순위: 업로드 폴더 (C:/.../data/)
			.addResourceLocations("classpath:/static/images/"); // 2순위: 프로젝트 기본 이미지 (resources/static/images/)
	}
}
