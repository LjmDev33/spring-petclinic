package org.springframework.samples.petclinic.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.community.table.CommunityPostAttachment;
import org.springframework.samples.petclinic.community.table.CommunityPostAttachmentId;

/*
 * Project : spring-petclinic
 * File    : CommunityPostAttachmentIdRepository.java
 * Created : 2025-10-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 공용 Attachment테이블과 게시글-Attachment 테이블이 분리되어있고 이 두 테이블의 연결관계를
 *         테이블의 복합키(post_id , attachment_id)로 설계했기 때문에 JPA에서 그 PK를 표현하려고
 *         @Embeddable ID 클래스(ex: CommunityPostAttachmentId)를 사용 함.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public interface CommunityPostAttachmentIdRepository extends JpaRepository<CommunityPostAttachment, CommunityPostAttachmentId> {
}
