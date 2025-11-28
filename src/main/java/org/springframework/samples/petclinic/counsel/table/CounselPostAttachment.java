package org.springframework.samples.petclinic.counsel.table;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;
import org.springframework.samples.petclinic.common.table.Attachment;

/**
 * Project : spring-petclinic
 * File    : CounselPostAttachment.java
 * Created : 2025-11-26 (마이그레이션)
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인 상담 게시글과 첨부파일의 N:M 관계 중간 테이블
 *   - common.table.Attachment 사용 (통합)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "counsel_post_attachments")
public class CounselPostAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counsel_post_id", nullable = false)
    private CounselPost counselPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CounselPost getCounselPost() {
        return counselPost;
    }

    public void setCounselPost(CounselPost counselPost) {
        this.counselPost = counselPost;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}

