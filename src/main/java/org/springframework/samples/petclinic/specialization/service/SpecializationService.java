package org.springframework.samples.petclinic.specialization.service;


import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
 * Project : spring-petclinic
 * File    : SpecializationService.java
 * Created : 2026-01-29
 * Author  : Jeongmin Lee
 *
 * Description :
 *   특화 클리닉 서비스 계층
 *
 * License :
 *   Copyright (c) 2026 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class SpecializationService {
	private static final Logger log =  LoggerFactory.getLogger(SpecializationService.class);
}
