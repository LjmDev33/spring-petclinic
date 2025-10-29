package org.springframework.samples.petclinic.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.community.table.CommunityPost;

/*
 * Project : spring-petclinic
 * File    : CommunityPostRepository.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: CommunityPost 엔티티의 데이터 접근 계층으로,
 *       JPA가 제공하는 기본 CRUD 기능과 QueryDSL 기반의 커스텀 검색 기능을 함께 제공하기 위한 인터페이스입니다.
 *       Service 레이어에서는 비즈니스 로직만 담당하고,
 *       실제 데이터 조회·저장은 Repository를 통해 수행하여 역할을 명확히 분리합니다.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long>, CommunityPostRepositoryCustom { }
