package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPostAttachment;

/*
 * Project : spring-petclinic
 * File    : CounselPostAttachmentRepository.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 온라인상담 게시판 첨부 레포지토리
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CounselPostAttachmentRepository extends JpaRepository<CounselPostAttachment,Long> {
}
