package org.springframework.samples.petclinic.common.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

/*
 * Project : spring-petclinic
 * File    : CommunityPostDto.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
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
