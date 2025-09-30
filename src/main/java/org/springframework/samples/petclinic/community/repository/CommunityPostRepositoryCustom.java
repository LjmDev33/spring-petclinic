package org.springframework.samples.petclinic.community.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.CommunityPost;

/*
 * Project : spring-petclinic
 * File    : CommunityPostRepositoryCustom.java
 * Created : 2025-09-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CommunityPostRepositoryCustom {
	PageResponse<CommunityPost> search(String type, String keyword, Pageable pageable);
}
