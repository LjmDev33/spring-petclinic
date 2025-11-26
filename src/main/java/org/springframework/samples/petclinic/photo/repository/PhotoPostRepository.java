package org.springframework.samples.petclinic.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPost;
import org.springframework.stereotype.Repository;

/**
 * Project : spring-petclinic
 * File    : PhotoPostRepository.java
 * Created : 2025-11-25 (QueryDSL 통합: 2025-11-26)
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 Repository
 *   - 기본 CRUD: JpaRepository
 *   - 복잡한 쿼리: PhotoPostRepositoryCustom (QueryDSL)
 */
@Repository
public interface PhotoPostRepository extends JpaRepository<PhotoPost, Long>, PhotoPostRepositoryCustom {
	// 기본 CRUD는 JpaRepository에서 제공
	// 복잡한 검색 및 동적 쿼리는 PhotoPostRepositoryCustom (Impl)에서 구현
}

