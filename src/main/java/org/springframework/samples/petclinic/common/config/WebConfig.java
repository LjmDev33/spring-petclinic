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
 *   Web MVC 설정
 *   - 정적 리소스 경로 설정
 *   - /animal/ 폴더를 정적 리소스로 등록
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

