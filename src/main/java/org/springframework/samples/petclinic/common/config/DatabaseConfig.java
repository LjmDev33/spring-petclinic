package org.springframework.samples.petclinic.common.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/*
 * Project : spring-petclinic
 * File    : DatabaseConfig.java
 * Created : 2025-11-05
 * Author  : Jeongmin Lee
 *
 * Description :
 *   개발 환경 데이터베이스 설정
 *   - 외래키 체크 비활성화로 테이블 DROP 순서 문제 해결
 *   - ddl-auto: create-drop 사용 시 필수 설정
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Configuration
@Profile("dev") // 개발 환경에서만 활성화
public class DatabaseConfig {

	/**
	 * 애플리케이션 시작 시 MySQL 외래키 체크를 비활성화합니다.
	 * 이는 Hibernate의 create-drop 모드에서 테이블 삭제 순서와 무관하게
	 * 모든 테이블을 안전하게 DROP할 수 있도록 합니다.
	 *
	 * ⚠️ 주의: 운영 환경에서는 절대 사용하지 마세요!
	 */
	@Bean
	public CommandLineRunner disableForeignKeyChecks(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
				System.out.println("✅ [DEV] MySQL 외래키 체크 비활성화 완료");
			} catch (Exception e) {
				System.err.println("⚠️ [DEV] 외래키 체크 비활성화 실패: " + e.getMessage());
			}
		};
	}
}

