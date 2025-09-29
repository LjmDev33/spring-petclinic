package org.springframework.samples.petclinic.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.community.CommunityPost;

/*
 * Project : spring-petclinic
 * File    : CommunityPostRepository.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

}
