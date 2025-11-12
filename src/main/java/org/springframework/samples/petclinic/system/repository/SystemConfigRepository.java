package org.springframework.samples.petclinic.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.system.table.SystemConfig;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Project : spring-petclinic
 * File    : SystemConfigRepository.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 시스템 설정 데이터 접근
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

	/**
	 * 설정 키로 조회
	 * @param propertyKey 설정 키
	 * @return 시스템 설정
	 */
	Optional<SystemConfig> findByPropertyKey(String propertyKey);

	/**
	 * 설정 키와 활성화 여부로 조회
	 * @param propertyKey 설정 키
	 * @param active 활성화 여부
	 * @return 시스템 설정
	 */
	Optional<SystemConfig> findByPropertyKeyAndActive(String propertyKey, boolean active);

	/**
	 * 활성화 여부로 조회
	 * @param active 활성화 여부
	 * @return 시스템 설정 목록
	 */
	java.util.List<SystemConfig> findByActive(boolean active);
}

