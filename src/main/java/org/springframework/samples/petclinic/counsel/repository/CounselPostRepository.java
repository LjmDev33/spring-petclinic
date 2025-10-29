package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.counsel.table.CounselPost;

/*
 * Project : spring-petclinic
 * File    : CounselPostRepository.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 온라인상담 게시판 레포지토리
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CounselPostRepository extends JpaRepository<CounselPost, Long> , CounselPostRepositoryCustom{

}
