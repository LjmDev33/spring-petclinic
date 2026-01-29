package org.springframework.samples.petclinic.general.service;


import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
 * Project : spring-petclinic
 * File    : GeneralService.java
 * Created : 2026-01-29
 * Author  : Jeongmin Lee
 *
 * Description :
 *   일반 클리닉 서비스 계층
 *
 * License :
 *   Copyright (c) 2026 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class GeneralService {
	private static final Logger log = LoggerFactory.getLogger(GeneralService.class);
}
