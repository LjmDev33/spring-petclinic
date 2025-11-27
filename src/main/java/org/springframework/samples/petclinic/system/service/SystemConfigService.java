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
 *   시스템 설정 관리 Service (기능 활성화/비활성화, 멀티로그인 등)
 *
 * Purpose (만든 이유):
 *   1. 시스템 전체 설정을 DB에서 관리 (코드 수정 없이 설정 변경)
 *   2. 멀티로그인 허용 여부 등 정책 설정 중앙 관리
 *   3. 관리자가 런타임에 설정을 변경할 수 있도록 지원
 *   4. 설정 변경 이력 추적 (updatedBy, updatedAt)
 *   5. 활성화/비활성화 플래그로 설정 on/off 가능
 *
 * Key Features (주요 기능):
 *   - 설정 값 조회 (기본값 지원)
 *   - Boolean 설정 값 조회 (true/false 파싱)
 *   - 멀티로그인 허용 여부 조회
 *   - 모든 설정 목록 조회
 *   - 설정 값 업데이트 (관리자)
 *   - 활성화된 설정만 조회
 *
 * Business Rules (비즈니스 규칙):
 *   - 설정은 key-value 형태로 저장
 *   - 활성화된 설정만 실제 적용 (active = true)
 *   - 설정 변경 시 변경자(updatedBy) 기록 필수
 *   - 존재하지 않는 키 조회 시 기본값 반환
 *
 * Configuration Keys (주요 설정 키):
 *   - multiLoginEnabled: 멀티로그인 허용 여부 (true/false)
 *   - (향후 추가 가능: maxLoginDevices, sessionTimeout 등)
 *
 * Usage Examples (사용 예시):
 *   // 멀티로그인 허용 여부 조회
 *   boolean multiLogin = systemConfigService.isMultiLoginEnabled();
 *
 *   // 설정 값 조회 (기본값 지원)
 *   String value = systemConfigService.getConfigValue("someKey", "defaultValue");
 *
 *   // Boolean 설정 조회
 *   boolean flag = systemConfigService.getBooleanConfig("featureFlag", false);
 *
 *   // 설정 업데이트 (관리자)
 *   systemConfigService.updateConfig("multiLoginEnabled", "false", "admin");
 *
 *   // 모든 설정 조회 (관리자 페이지)
 *   List<SystemConfig> configs = systemConfigService.getAllConfigs();
 *
 * vs Hard-coded Config (하드코딩 방식과 비교):
 *   - Hard-coded: 설정 변경 시 코드 수정 및 재배포 필요
 *   - SystemConfigService: 관리자 페이지에서 즉시 변경 가능
 *
 * Transaction Management (트랜잭션 관리):
 *   - @Transactional 클래스 레벨 적용
 *   - 조회 메서드는 @Transactional(readOnly = true)
 *   - 설정 변경 시 트랜잭션 보장
 *
 * Dependencies (의존 관계):
 *   - SystemConfigRepository: DB 접근
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
