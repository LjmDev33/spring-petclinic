package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.counsel.table.CounselComment;

/*
 * Project : spring-petclinic
 * File    : CounselCommentRepository.java
 * Created : 2025-10-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 온라인상담 댓글 저장소
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CounselCommentRepository extends JpaRepository<CounselComment,Long> {
}
