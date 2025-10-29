package org.springframework.samples.petclinic.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.community.table.CommunityPostAttachment;

/*
 * Project : spring-petclinic
 * File    : CommunityPostAttachmentRepository.java
 * Created : 2025-10-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 커뮤니티 게시글 첨부 레포지토리
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CommunityPostAttachmentRepository extends JpaRepository<CommunityPostAttachment, Long> {
}
