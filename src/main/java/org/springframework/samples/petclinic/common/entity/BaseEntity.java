package org.springframework.samples.petclinic.common.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

/**
 * Project : spring-petclinic
 * File    : BaseEntity.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   모든 Entity의 공통 기본 클래스
 *   - ID 자동 생성 전략 (GenerationType.IDENTITY)
 *   - isNew() 메서드를 통한 신규 엔티티 판별 지원
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */

@MappedSuperclass
public class BaseEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public boolean isNew() {
		return this.id == null;
	}
}
