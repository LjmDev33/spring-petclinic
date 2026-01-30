package org.springframework.samples.petclinic.minimal.service;


import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
 * Project : spring-petclinic
 * File    : MinimalService.java
 * Created : 2026-01-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   최소침습수술 서비스 계층
 *
 * License :
 *   Copyright (c) 2026 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class MinimalService {
	private static final Logger log = LoggerFactory.getLogger(MinimalService.class);
}
