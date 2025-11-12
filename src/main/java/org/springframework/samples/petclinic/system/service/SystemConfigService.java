package org.springframework.samples.petclinic.system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.samples.petclinic.system.repository.SystemConfigRepository;
import org.springframework.samples.petclinic.system.table.SystemConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Project : spring-petclinic
 * File    : SystemConfigService.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 시스템 설정 관리 서비스
 *   - 기능 활성화/비활성화 조회
 *   - 멀티로그인 허용 여부 조회
 *   연관 기능: 로그인 정책
 *   미구현: 관리자 설정 변경 API
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class SystemConfigService {

	private static final Logger log = LoggerFactory.getLogger(SystemConfigService.class);
	private final SystemConfigRepository repository;

	public SystemConfigService(SystemConfigRepository repository) {
		this.repository = repository;
	}

	/**
	 * 설정 값 조회 (기본값 지원)
	 * @param key 설정 키
	 * @param defaultValue 기본값
	 * @return 설정 값
	 */
	public String getConfigValue(String key, String defaultValue) {
		return repository.findByPropertyKeyAndActive(key, true)
			.map(SystemConfig::getPropertyValue)
			.orElse(defaultValue);
	}

	/**
	 * Boolean 설정 값 조회
	 * @param key 설정 키
	 * @param defaultValue 기본값
	 * @return Boolean 설정 값
	 */
	public boolean getBooleanConfig(String key, boolean defaultValue) {
		String value = getConfigValue(key, String.valueOf(defaultValue));
		return Boolean.parseBoolean(value);
	}

	/**
	 * 멀티로그인 허용 여부 조회
	 * @return true: 멀티로그인 허용, false: 단일 로그인만 허용
	 */
	public boolean isMultiLoginEnabled() {
		return getBooleanConfig("multiLoginEnabled", true);
	}

	/**
	 * 모든 설정 조회
	 * @return 시스템 설정 목록
	 */
	@Transactional(readOnly = true)
	public List<SystemConfig> getAllConfigs() {
		return repository.findAll();
	}

	/**
	 * 설정 값 업데이트
	 * @param key 설정 키
	 * @param value 설정 값
	 * @param updatedBy 수정자
	 */
	public void updateConfig(String key, String value, String updatedBy) {
		SystemConfig config = repository.findByPropertyKey(key)
			.orElseThrow(() -> new IllegalArgumentException("설정을 찾을 수 없습니다: " + key));

		String oldValue = config.getPropertyValue();
		config.setPropertyValue(value);
		config.setUpdatedBy(updatedBy);
		repository.save(config);

		log.info("System config updated: key={}, oldValue={}, newValue={}, updatedBy={}",
				 key, oldValue, value, updatedBy);
	}

	/**
	 * 설정 키로 조회
	 * @param key 설정 키
	 * @return SystemConfig 엔티티
	 */
	@Transactional(readOnly = true)
	public SystemConfig getConfig(String key) {
		return repository.findByPropertyKey(key)
			.orElseThrow(() -> new IllegalArgumentException("설정을 찾을 수 없습니다: " + key));
	}

	/**
	 * 활성화된 설정 목록 조회
	 * @return 활성화된 시스템 설정 목록
	 */
	@Transactional(readOnly = true)
	public List<SystemConfig> getActiveConfigs() {
		return repository.findByActive(true);
	}
}
