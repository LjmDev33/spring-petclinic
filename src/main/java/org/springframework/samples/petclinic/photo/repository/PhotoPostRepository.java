package org.springframework.samples.petclinic.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPost;
import org.springframework.stereotype.Repository;

/**
 * Project : spring-petclinic
 * File    : PhotoPostRepository.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 Repository
 */
@Repository
public interface PhotoPostRepository extends JpaRepository<PhotoPost, Long> {
	// 기본 CRUD는 JpaRepository에서 제공
}

